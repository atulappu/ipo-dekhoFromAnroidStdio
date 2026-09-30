using Microsoft.EntityFrameworkCore;
using IpoDekho.Backend.Data;
using IpoDekho.Backend.DTOs;
using System.Text.RegularExpressions;
using HtmlAgilityPack;
using System.Security.Cryptography;
using System.Text;

namespace IpoDekho.Backend.Services
{
    public interface IIpoScraperService
    {
        Task<int> SyncIpoDataAsync();
        Task UpdateGmpAsync(string ipoId, decimal newGmp);
        Task MarkAllotmentOutAsync(string ipoId);
    }

    public class IpoScraperService : IIpoScraperService
    {
        private readonly IpoDbContext _context;
        private readonly ILogger<IpoScraperService> _logger;
        private readonly HttpClient _httpClient;
        private readonly IFirebaseNotificationService _notificationService;

        public IpoScraperService(
            IpoDbContext context,
            ILogger<IpoScraperService> logger,
            IHttpClientFactory httpClientFactory,
            IFirebaseNotificationService notificationService)
        {
            _context = context;
            _logger = logger;
            _httpClient = httpClientFactory.CreateClient();
            _httpClient.DefaultRequestHeaders.Add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");
            _notificationService = notificationService;
        }

        public async Task<int> SyncIpoDataAsync()
        {
            _logger.LogInformation("Starting automated live IPO ingestion and reconciliation cycle...");
            int updatedCount = 0;

            try
            {
                // 1. Ingest InvestorGain Live GMP Feed (Authoritative for GMP, Sentiment & Ratings)
                var gmpItems = await IngestInvestorGainAsync();

                // 2. Ingest NSE Live Public Issues (Authoritative for NSE Schedules, Symbols & Series)
                var nseItems = await IngestNseAsync();

                // 3. Ingest BSE Live Public Issues
                var bseItems = await IngestBseAsync();

                // 4. Multi-Source Reconciliation & Master Matching
                var masterList = new List<IpoRecord>();

                // Seed with official Exchange records first
                foreach (var nse in nseItems)
                {
                    masterList.Add(nse);
                }

                foreach (var bse in bseItems)
                {
                    var match = FindMatchingRecord(masterList, bse.Name, bse.Symbol, bse.OpenDate, bse.CloseDate);
                    if (match != null)
                    {
                        if (!match.Exchange.Contains("BSE")) match.Exchange += ", BSE";
                    }
                    else
                    {
                        masterList.Add(bse);
                    }
                }

                // Enrich with InvestorGain GMP without altering official exchange dates
                foreach (var gmp in gmpItems)
                {
                    var match = FindMatchingRecord(masterList, gmp.Name, gmp.Symbol, gmp.OpenDate, gmp.CloseDate);
                    if (match != null)
                    {
                        if (gmp.Name.Contains("Vans", StringComparison.OrdinalIgnoreCase) || match.Name.Contains("Vans", StringComparison.OrdinalIgnoreCase))
                        {
                            _logger.LogInformation("[AUDIT] VANS_IDENTITY_MATCHED: Successfully resolved identity '{Name}' to existing master issue '{Match}'", gmp.Name, match.Name);
                        }
                        match.CurrentGmp = gmp.CurrentGmp;
                        match.EstimatedListingPrice = gmp.EstimatedListingPrice;
                        match.EstimatedGainPercent = gmp.EstimatedGainPercent;
                        match.FireRating = gmp.FireRating;
                        match.GmpSource = "InvestorGain";
                        match.GmpFetchedAt = DateTimeOffset.UtcNow;
                        if (match.PriceBandMax <= 0 && gmp.PriceBandMax > 0)
                        {
                            match.PriceBandMin = gmp.PriceBandMin;
                            match.PriceBandMax = gmp.PriceBandMax;
                        }
                        if (match.LotSize <= 1 && gmp.LotSize > 1) match.LotSize = gmp.LotSize;
                        if (match.IssueSizeCr <= 0 && gmp.IssueSizeCr > 0) match.IssueSizeCr = gmp.IssueSizeCr;
                    }
                    else
                    {
                        // Verified issue listed in InvestorGain
                        masterList.Add(gmp);
                    }
                }

                // 5. Validation Pipeline & Persistence into SQL Server
                foreach (var candidate in masterList)
                {
                    // Validation Rule 1: Name must be valid
                    if (string.IsNullOrWhiteSpace(candidate.Name) || candidate.Name.Length < 3)
                    {
                        await LogValidationErrorAsync(candidate.Id, "Name", candidate.Name, "VR-01", "Name is too short or empty.");
                        continue;
                    }

                    // Validation Rule 2: Chronological Sequence
                    if (candidate.OpenDate.HasValue && candidate.CloseDate.HasValue && candidate.OpenDate > candidate.CloseDate)
                    {
                        await LogValidationErrorAsync(candidate.Id, "Dates", $"{candidate.OpenDate} > {candidate.CloseDate}", "VR-02", "Open date is after close date.");
                        continue;
                    }

                    // Validation Rule 3: Price Band Integrity
                    if (candidate.PriceBandMin > 0 && candidate.PriceBandMax > 0 && candidate.PriceBandMin > candidate.PriceBandMax)
                    {
                        await LogValidationErrorAsync(candidate.Id, "PriceBand", $"{candidate.PriceBandMin} > {candidate.PriceBandMax}", "VR-03", "Min price exceeds max price.");
                        continue;
                    }

                    candidate.NormalizedName = NormalizeName(candidate.Name);
                    candidate.ValidationStatus = "VALIDATED";

                    var existing = await _context.Ipos.FirstOrDefaultAsync(i => i.Id == candidate.Id);
                    if (existing == null)
                    {
                        candidate.AllotmentStatus = IpoStatusCalculator.GetAllotmentStatus(candidate);
                        candidate.Status = IpoStatusCalculator.GetIPOStatus(candidate);
                        if (candidate.Name.Contains("Vans", StringComparison.OrdinalIgnoreCase) || candidate.Name.Contains("Orient", StringComparison.OrdinalIgnoreCase))
                        {
                            _logger.LogInformation("[AUDIT] DATABASE_SAVED: Persisting new issue '{Name}', Exchange='{Exchange}', Type='{Type}', Status='{Status}', Allotment='{AllotmentStatus}'", 
                                candidate.Name, candidate.Exchange, candidate.Type, candidate.Status, candidate.AllotmentStatus);
                        }
                        _context.Ipos.Add(candidate);
                        await _context.SaveChangesAsync();

                        if (candidate.CurrentGmp.HasValue)
                        {
                            _context.GmpHistory.Add(new GmpRecord
                            {
                                IpoId = candidate.Id,
                                Gmp = candidate.CurrentGmp.Value,
                                GainPercent = candidate.EstimatedGainPercent ?? 0m,
                                Source = candidate.GmpSource ?? "InvestorGain",
                                RecordedAt = DateTimeOffset.UtcNow
                            });
                        }

                        if (candidate.AllotmentStatus == "AVAILABLE")
                        {
                            var eventKey = $"ALLOTMENT_AVAILABLE:{candidate.Id}";
                            var alreadyNotified = await _context.Notifications.AnyAsync(n => n.EventKey == eventKey);
                            if (!alreadyNotified)
                            {
                                _context.Notifications.Add(new NotificationRecord
                                {
                                    IPOId = candidate.Id,
                                    Type = "ALLOTMENT_AVAILABLE",
                                    Title = "🎯 Allotment Out",
                                    Message = $"{candidate.Name} allotment status is now available.",
                                    EventKey = eventKey,
                                    IsRead = false,
                                    CreatedAt = DateTimeOffset.UtcNow
                                });

                                await _notificationService.SendNotificationToTopicAsync(
                                    topic: "allotment_alerts",
                                    title: $"🎯 Allotment Out: {candidate.Name}",
                                    body: $"{candidate.Name} allotment status is now available.",
                                    data: new Dictionary<string, string> { { "destination", "allotment" }, { "ipoId", candidate.Id } });
                            }
                        }

                        updatedCount++;
                    }
                    else
                    {
                        // Check for GMP Movement
                        if (candidate.CurrentGmp.HasValue && existing.CurrentGmp != candidate.CurrentGmp)
                        {
                            var oldGmp = existing.CurrentGmp ?? 0m;
                            existing.CurrentGmp = candidate.CurrentGmp;
                            existing.EstimatedListingPrice = candidate.EstimatedListingPrice;
                            existing.EstimatedGainPercent = candidate.EstimatedGainPercent;
                            existing.FireRating = candidate.FireRating;
                            existing.GmpSource = candidate.GmpSource;
                            existing.GmpFetchedAt = DateTimeOffset.UtcNow;
                            existing.UpdatedAt = DateTimeOffset.UtcNow;

                            _context.GmpHistory.Add(new GmpRecord
                            {
                                IpoId = existing.Id,
                                Gmp = candidate.CurrentGmp.Value,
                                GainPercent = candidate.EstimatedGainPercent ?? 0m,
                                Source = "InvestorGain",
                                RecordedAt = DateTimeOffset.UtcNow
                            });

                            var diff = candidate.CurrentGmp.Value - oldGmp;
                            var direction = diff >= 0 ? $"+₹{diff:N0} Jump" : $"-₹{-diff:N0} Drop";
                            await _notificationService.SendNotificationToTopicAsync(
                                topic: "gmp_updates",
                                title: $"📈 GMP {direction}: {existing.Name}",
                                body: $"Latest GMP: ₹{candidate.CurrentGmp.Value:N0} ({candidate.EstimatedGainPercent:F1}% gain).",
                                data: new Dictionary<string, string> { { "destination", "detail" }, { "ipoId", existing.Id } });

                            updatedCount++;
                        }

                        // Check for Allotment Status Update & WAITING -> AVAILABLE transition
                        var oldAllotmentStatus = existing.AllotmentStatus ?? "WAITING";
                        if (candidate.AllotmentDate.HasValue) existing.AllotmentDate = candidate.AllotmentDate;
                        if (!string.IsNullOrEmpty(candidate.RegistrarName)) existing.RegistrarName = candidate.RegistrarName;
                        if (!string.IsNullOrEmpty(candidate.RegistrarUrl)) existing.RegistrarUrl = candidate.RegistrarUrl;
                        if (candidate.IsAllotmentOut) existing.IsAllotmentOut = true;

                        var newAllotmentStatus = IpoStatusCalculator.GetAllotmentStatus(existing);
                        if (candidate.AllotmentStatus == "AVAILABLE" || existing.IsAllotmentOut)
                        {
                            newAllotmentStatus = "AVAILABLE";
                        }

                        existing.AllotmentStatus = newAllotmentStatus;
                        existing.Status = IpoStatusCalculator.GetIPOStatus(existing);

                        if (oldAllotmentStatus != "AVAILABLE" && newAllotmentStatus == "AVAILABLE")
                        {
                            existing.IsAllotmentOut = true;
                            existing.Status = "ALLOTMENT_AVAILABLE";
                            existing.UpdatedAt = DateTimeOffset.UtcNow;

                            var eventKey = $"ALLOTMENT_AVAILABLE:{existing.Id}";
                            var alreadyNotified = await _context.Notifications.AnyAsync(n => n.EventKey == eventKey);
                            if (!alreadyNotified)
                            {
                                _context.Notifications.Add(new NotificationRecord
                                {
                                    IPOId = existing.Id,
                                    Type = "ALLOTMENT_AVAILABLE",
                                    Title = "🎯 Allotment Out",
                                    Message = $"{existing.Name} allotment status is now available.",
                                    EventKey = eventKey,
                                    IsRead = false,
                                    CreatedAt = DateTimeOffset.UtcNow
                                });

                                await _notificationService.SendNotificationToTopicAsync(
                                    topic: "allotment_alerts",
                                    title: $"🎯 Allotment Out: {existing.Name}",
                                    body: $"{existing.Name} allotment status is now available.",
                                    data: new Dictionary<string, string> { { "destination", "allotment" }, { "ipoId", existing.Id } });
                            }

                            updatedCount++;
                        }
                    }
                }

                await _context.SaveChangesAsync();
                _logger.LogInformation("Real ingestion cycle completed. Processed {Count} updates with zero mock data.", updatedCount);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Exception in live data ingestion pipeline.");
            }

            return updatedCount;
        }

        private async Task<List<IpoRecord>> IngestInvestorGainAsync()
        {
            var results = new List<IpoRecord>();
            const string url = "https://www.investorgain.com/report/live-ipo-gmp/331/";
            const string sourceId = "SRC_INVESTORGAIN_GMP";

            try
            {
                var response = await _httpClient.GetAsync(url);
                var body = await response.Content.ReadAsStringAsync();
                await RecordRawResponseAsync(sourceId, url, (int)response.StatusCode, body, response.IsSuccessStatusCode, null);

                if (!response.IsSuccessStatusCode) return results;

                var doc = new HtmlDocument();
                doc.LoadHtml(body);

                var rows = doc.DocumentNode.SelectNodes("//table//tbody//tr");
                if (rows == null) return results;

                foreach (var row in rows)
                {
                    var cols = row.SelectNodes("td");
                    if (cols != null && cols.Count >= 10)
                    {
                        var col0Text = cols[0].InnerText;
                        var linkNode = cols[0].SelectSingleNode(".//a");
                        var rawAnchor = linkNode != null ? linkNode.InnerText.Trim() : col0Text.Trim();
                        var cleanName = CleanText(rawAnchor);
                        cleanName = Regex.Replace(cleanName, @"\b(BSE|NSE)\s*(SME|MAINBOARD)?\b.*$", "", RegexOptions.IgnoreCase).Trim();
                        if (string.IsNullOrEmpty(cleanName) || cleanName.Contains("Company") || cleanName.Contains("Name")) continue;

                        var isBse = col0Text.Contains("BSE", StringComparison.OrdinalIgnoreCase);
                        var isNse = col0Text.Contains("NSE", StringComparison.OrdinalIgnoreCase);
                        var exchange = (isBse && isNse) ? "BSE, NSE" : (isBse ? "BSE" : (isNse ? "NSE" : "BSE, NSE"));
                        var isSme = col0Text.Contains("SME", StringComparison.OrdinalIgnoreCase) || cleanName.Contains("SME", StringComparison.OrdinalIgnoreCase);

                        var rawGmp = cols[1].InnerText.Trim();
                        var rawRating = cols[2].InnerText.Trim();
                        var rawSub = cols[3].InnerText.Trim();
                        var rawPrice = cols[4].InnerText.Trim();
                        var rawSize = cols[5].InnerText.Trim();
                        var rawLot = cols[6].InnerText.Trim();
                        var rawOpen = cols[7].InnerText.Trim();
                        var rawClose = cols[8].InnerText.Trim();
                        var rawAllotment = cols.Count > 9 ? cols[9].InnerText.Trim() : "";
                        var rawListing = cols.Count > 10 ? cols[10].InnerText.Trim() : "";

                        var cleanId = GenerateSlug(cleanName);

                        decimal? gmpVal = ParseDecimal(rawGmp);
                        decimal priceVal = ParseDecimal(rawPrice) ?? 0m;
                        decimal? estListing = (priceVal > 0 && gmpVal.HasValue) ? (priceVal + gmpVal.Value) : null;
                        decimal? gainPercent = (priceVal > 0 && gmpVal.HasValue) ? (gmpVal.Value / priceVal) * 100m : null;

                        var openDt = ParseDate(rawOpen);
                        var closeDt = ParseDate(rawClose);
                        var allotmentDt = ParseDate(rawAllotment);
                        var listingDt = ParseDate(rawListing);
                        var normName = NormalizeName(cleanName);

                        var isAllottedBadge = col0Text.Contains("Allotted", StringComparison.OrdinalIgnoreCase) || col0Text.Contains("Allotment", StringComparison.OrdinalIgnoreCase);
                        var nowIst = TimeZoneInfo.ConvertTime(DateTimeOffset.UtcNow, IpoStatusCalculator.IstZone);
                        var isAllotmentAvailable = isAllottedBadge || (allotmentDt.HasValue && nowIst.Date >= TimeZoneInfo.ConvertTime(allotmentDt.Value, IpoStatusCalculator.IstZone).Date && closeDt.HasValue && nowIst >= closeDt.Value);

                        string? regName = null;
                        string? regUrl = null;
                        if (col0Text.Contains("kfin", StringComparison.OrdinalIgnoreCase) || cleanName.Contains("Orient Cables", StringComparison.OrdinalIgnoreCase))
                        {
                            regName = "Kfin Technologies Ltd.";
                            regUrl = "https://ipostatus.kfintech.com/";
                        }
                        else if (col0Text.Contains("maashitla", StringComparison.OrdinalIgnoreCase))
                        {
                            regName = "Maashitla Securities Pvt.Ltd.";
                            regUrl = "https://maashitla.com/allotment-status/public-issues";
                        }
                        else if (col0Text.Contains("linkintime", StringComparison.OrdinalIgnoreCase) || col0Text.Contains("mufg", StringComparison.OrdinalIgnoreCase))
                        {
                            regName = "MUFG Intime India Pvt.Ltd.";
                            regUrl = "https://in.mpms.mufg.com/Initial_Offer/public-issues.html";
                        }
                        else if (col0Text.Contains("bigshare", StringComparison.OrdinalIgnoreCase))
                        {
                            regName = "Bigshare Services Pvt.Ltd.";
                            regUrl = "https://www.bigshareonline.com/ipo_allotment.html";
                        }

                        var tempRecord = new IpoRecord
                        {
                            OpenDate = openDt,
                            CloseDate = closeDt,
                            AllotmentDate = allotmentDt,
                            ListingDate = listingDt,
                            IsAllotmentOut = isAllotmentAvailable,
                            AllotmentStatus = isAllotmentAvailable ? "AVAILABLE" : "WAITING"
                        };
                        var statusCalculated = IpoStatusCalculator.GetIPOStatus(tempRecord);
                        var allotmentStatusCalculated = IpoStatusCalculator.GetAllotmentStatus(tempRecord);

                        if (cleanName.Contains("Orient", StringComparison.OrdinalIgnoreCase) || cleanName.Contains("Vans", StringComparison.OrdinalIgnoreCase))
                        {
                            _logger.LogInformation("[AUDIT] PARSED_ISSUE: Found in {SourceId}. Name='{Name}', Open={Open}, Close={Close}, AllotmentDate={Allotment}, Status='{Status}', AllotmentStatus='{AllotmentStatus}'", 
                                sourceId, cleanName, openDt, closeDt, allotmentDt, statusCalculated, allotmentStatusCalculated);
                        }

                        results.Add(new IpoRecord
                        {
                            Id = cleanId,
                            Name = cleanName,
                            NormalizedName = normName,
                            Symbol = cleanName.Split(' ').FirstOrDefault()?.ToUpper() ?? "IPO",
                            Exchange = exchange,
                            Type = isSme ? "SME" : "MAINBOARD",
                            Status = statusCalculated,
                            AllotmentStatus = allotmentStatusCalculated,
                            IsAllotmentOut = (allotmentStatusCalculated == "AVAILABLE"),
                            PriceBandMin = priceVal,
                            PriceBandMax = priceVal,
                            LotSize = ParseInt(rawLot) ?? (isSme ? 1200 : 1),
                            IssueSizeCr = ParseDecimal(rawSize) ?? 0m,
                            CurrentGmp = gmpVal,
                            EstimatedListingPrice = estListing,
                            EstimatedGainPercent = gainPercent,
                            GmpSource = "InvestorGain",
                            GmpFetchedAt = DateTimeOffset.UtcNow,
                            FireRating = ParseRating(rawRating),
                            OpenDate = openDt,
                            CloseDate = closeDt,
                            AllotmentDate = allotmentDt,
                            ListingDate = listingDt,
                            RegistrarName = regName,
                            RegistrarUrl = regUrl,
                            UpdatedAt = DateTimeOffset.UtcNow
                        });
                    }
                }
            }
            catch (Exception ex)
            {
                _logger.LogWarning("InvestorGain ingestion warning: {Msg}", ex.Message);
            }

            return results;
        }

        private async Task<List<IpoRecord>> IngestNseAsync()
        {
            var results = new List<IpoRecord>();
            const string url = "https://www.nseindia.com/market-data/all-upcoming-issues-ipo";
            const string sourceId = "SRC_NSE_LIVE";

            try
            {
                // Pre-warm handshake
                try
                {
                    using var handshakeReq = new HttpRequestMessage(HttpMethod.Get, "https://www.nseindia.com/");
                    handshakeReq.Headers.Add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                    await _httpClient.SendAsync(handshakeReq);
                }
                catch { }

                using var request = new HttpRequestMessage(HttpMethod.Get, url);
                request.Headers.Add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                request.Headers.Add("Referer", "https://www.nseindia.com/");

                var response = await _httpClient.SendAsync(request);
                var body = await response.Content.ReadAsStringAsync();
                await RecordRawResponseAsync(sourceId, url, (int)response.StatusCode, body, response.IsSuccessStatusCode, null);

                if (!response.IsSuccessStatusCode) return results;

                var doc = new HtmlDocument();
                doc.LoadHtml(body);
                var rows = doc.DocumentNode.SelectNodes("//table//tbody//tr");
                if (rows == null) return results;

                foreach (var row in rows)
                {
                    var cols = row.SelectNodes("td");
                    if (cols != null && cols.Count >= 4)
                    {
                        var rawName = cols[0].InnerText.Trim();
                        var cleanName = CleanText(rawName);
                        if (string.IsNullOrEmpty(cleanName) || cleanName.Contains("Company") || cleanName.Contains("Symbol")) continue;

                        var symbol = cols.Count > 1 ? cols[1].InnerText.Trim() : cleanName.Split(' ').FirstOrDefault()?.ToUpper();
                        var isSme = cleanName.Contains("SME", StringComparison.OrdinalIgnoreCase);

                        results.Add(new IpoRecord
                        {
                            Id = GenerateSlug(cleanName),
                            Name = cleanName,
                            NormalizedName = NormalizeName(cleanName),
                            Symbol = symbol,
                            Exchange = "NSE",
                            Type = isSme ? "SME" : "MAINBOARD",
                            Status = "OPEN",
                            CurrentGmp = null, // NSE does not provide GMP
                            GmpSource = null,
                            UpdatedAt = DateTimeOffset.UtcNow
                        });
                    }
                }
            }
            catch (Exception ex)
            {
                _logger.LogWarning("NSE ingestion warning: {Msg}", ex.Message);
            }

            return results;
        }

        private async Task<List<IpoRecord>> IngestBseAsync()
        {
            var results = new List<IpoRecord>();
            const string url = "https://www.bseindia.com/markets/publicissues/ipoissues.aspx?id=1&type=pso";
            const string sourceId = "SRC_BSE_PUBLIC";

            try
            {
                using var request = new HttpRequestMessage(HttpMethod.Get, url);
                request.Headers.Add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                request.Headers.Add("Referer", "https://www.bseindia.com/");

                var response = await _httpClient.SendAsync(request);
                var body = await response.Content.ReadAsStringAsync();
                await RecordRawResponseAsync(sourceId, url, (int)response.StatusCode, body, response.IsSuccessStatusCode, null);

                if (!response.IsSuccessStatusCode) return results;

                // Check for BSE redirect or updated notice
                if (body.Contains("The URLs on the site have been updated", StringComparison.OrdinalIgnoreCase))
                {
                    _logger.LogWarning("BSE notice detected: URLs updated. Flagging as SOURCE_UNAVAILABLE.");
                    return results;
                }

                var doc = new HtmlDocument();
                doc.LoadHtml(body);
                var rows = doc.DocumentNode.SelectNodes("//table//tbody//tr");
                if (rows == null) return results;

                foreach (var row in rows)
                {
                    var cols = row.SelectNodes("td");
                    if (cols != null && cols.Count >= 4)
                    {
                        var cleanName = CleanText(cols[0].InnerText.Trim());
                        if (string.IsNullOrEmpty(cleanName) || cleanName.Contains("Security") || cleanName.Contains("Company")) continue;

                        var isSme = cleanName.Contains("SME", StringComparison.OrdinalIgnoreCase);
                        results.Add(new IpoRecord
                        {
                            Id = GenerateSlug(cleanName),
                            Name = cleanName,
                            NormalizedName = NormalizeName(cleanName),
                            Symbol = cleanName.Split(' ').FirstOrDefault()?.ToUpper(),
                            Exchange = "BSE",
                            Type = isSme ? "SME" : "MAINBOARD",
                            Status = "OPEN",
                            CurrentGmp = null,
                            UpdatedAt = DateTimeOffset.UtcNow
                        });
                    }
                }
            }
            catch (Exception ex)
            {
                _logger.LogWarning("BSE ingestion notice: {Msg}", ex.Message);
            }

            return results;
        }

        private async Task RecordRawResponseAsync(string dataSourceId, string requestUrl, int status, string body, bool isSuccess, string? error)
        {
            try
            {
                using var sha256 = SHA256.Create();
                var hash = Convert.ToHexString(sha256.ComputeHash(Encoding.UTF8.GetBytes(body ?? "")));

                _context.RawSourceResponses.Add(new RawSourceResponseRecord
                {
                    DataSourceId = dataSourceId,
                    RequestUrl = requestUrl,
                    HttpStatus = status,
                    ResponseBody = (body != null && body.Length > 2000) ? body.Substring(0, 2000) : body,
                    ContentHash = hash,
                    IsSuccess = isSuccess,
                    ErrorMessage = error,
                    FetchedAt = DateTimeOffset.UtcNow
                });
                await _context.SaveChangesAsync();
            }
            catch { }
        }

        private async Task LogValidationErrorAsync(string ipoId, string fieldName, string? rawVal, string rule, string msg)
        {
            try
            {
                _context.ValidationErrors.Add(new IpoValidationErrorRecord
                {
                    IPOId = ipoId,
                    FieldName = fieldName,
                    RawValue = rawVal,
                    ValidationRule = rule,
                    ErrorMessage = msg,
                    LoggedAt = DateTimeOffset.UtcNow
                });
                await _context.SaveChangesAsync();
            }
            catch { }
        }

        private IpoRecord? FindMatchingRecord(List<IpoRecord> list, string name, string? symbol, DateTimeOffset? openDate = null, DateTimeOffset? closeDate = null)
        {
            // Priority 1: Symbol match (if valid and not generic)
            if (!string.IsNullOrWhiteSpace(symbol) && !symbol.Equals("IPO", StringComparison.OrdinalIgnoreCase))
            {
                var bySymbol = list.FirstOrDefault(x => !string.IsNullOrEmpty(x.Symbol) && x.Symbol.Equals(symbol, StringComparison.OrdinalIgnoreCase));
                if (bySymbol != null) return bySymbol;
            }

            // Priority 2: Exact Name match
            var exactName = list.FirstOrDefault(x => x.Name.Equals(name, StringComparison.OrdinalIgnoreCase));
            if (exactName != null) return exactName;

            // Priority 3: Normalized Name + Date Proximity Validation
            var norm = NormalizeName(name);
            var candidates = list.Where(x => x.NormalizedName == norm || 
                                             (!string.IsNullOrEmpty(x.NormalizedName) && !string.IsNullOrEmpty(norm) && 
                                              (x.NormalizedName.Contains(norm) || norm.Contains(x.NormalizedName)))).ToList();

            foreach (var cand in candidates)
            {
                // If both records have dates, verify they are within 3 days to avoid merging distinct issues
                if (openDate.HasValue && cand.OpenDate.HasValue)
                {
                    var diffDays = Math.Abs((openDate.Value - cand.OpenDate.Value).TotalDays);
                    if (diffDays <= 3) return cand;
                }
                else if (closeDate.HasValue && cand.CloseDate.HasValue)
                {
                    var diffDays = Math.Abs((closeDate.Value - cand.CloseDate.Value).TotalDays);
                    if (diffDays <= 3) return cand;
                }
                else
                {
                    return cand;
                }
            }

            return null;
        }

        public static string NormalizeName(string name)
        {
            if (string.IsNullOrWhiteSpace(name)) return string.Empty;
            var lower = name.ToLowerInvariant();
            // Remove corporate suffixes, exchange tags, SME tags, and status pills
            lower = Regex.Replace(lower, @"\b(limited|ltd|pvt|private|corporation|corp|india|bse|nse|sme|mainboard|ipo|[ouac])\b", " ");
            // Remove non-alphanumeric characters
            lower = Regex.Replace(lower, @"[^\w]", "");
            return lower.Trim();
        }

        private static string GenerateSlug(string name)
        {
            return Regex.Replace(name.ToLowerInvariant(), @"[^a-z0-9]", "-").Trim('-');
        }

        private static string CleanText(string input)
        {
            return Regex.Replace(input, @"<[^>]*>|&nbsp;", " ").Trim();
        }

        private static decimal? ParseDecimal(string raw)
        {
            if (string.IsNullOrWhiteSpace(raw) || raw == "--" || raw == "-") return null;
            var clean = Regex.Replace(raw, @"[^\d\.\-]", "");
            return decimal.TryParse(clean, out var res) ? res : null;
        }

        private static int? ParseInt(string raw)
        {
            if (string.IsNullOrWhiteSpace(raw)) return null;
            var clean = Regex.Replace(raw, @"[^\d]", "");
            return int.TryParse(clean, out var res) ? res : null;
        }

        private static int ParseRating(string raw)
        {
            return Regex.Matches(raw, "🔥").Count;
        }

        public static DateTimeOffset? ParseDate(string? raw, int defaultYear = 2026)
        {
            if (string.IsNullOrWhiteSpace(raw) || raw == "-" || raw == "--") return null;

            var clean = Regex.Replace(raw, @"<[^>]*>", " ").Trim();
            var match = Regex.Match(clean, @"\d{1,4}[-/][0-9a-zA-Z]{1,4}(?:[-/]\d{2,4})?(?:T\d{2}:\d{2}:\d{2}(?:[+-]\d{2}:\d{2})?)?");
            if (!match.Success) return null;
            var token = match.Value;

            string[] formatsWithYear = {
                "yyyy-MM-dd",
                "dd-MMM-yyyy",
                "d-MMM-yyyy",
                "dd/MM/yyyy",
                "d/M/yyyy",
                "dd-MM-yyyy",
                "d-M-yyyy",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:sszzz"
            };

            foreach (var fmt in formatsWithYear)
            {
                if (DateTime.TryParseExact(token, fmt, System.Globalization.CultureInfo.InvariantCulture, System.Globalization.DateTimeStyles.None, out var dt))
                {
                    return new DateTimeOffset(dt, IpoStatusCalculator.IstZone.BaseUtcOffset);
                }
            }

            string[] formatsWithoutYear = {
                "dd-MMM",
                "d-MMM",
                "dd/MM",
                "d/M",
                "dd-MM",
                "d-M"
            };

            foreach (var fmt in formatsWithoutYear)
            {
                if (DateTime.TryParseExact(token, fmt, System.Globalization.CultureInfo.InvariantCulture, System.Globalization.DateTimeStyles.None, out var dt))
                {
                    var fullDt = new DateTime(defaultYear, dt.Month, dt.Day, 0, 0, 0);
                    return new DateTimeOffset(fullDt, IpoStatusCalculator.IstZone.BaseUtcOffset);
                }
            }

            if (DateTimeOffset.TryParse(token, out var dto))
            {
                return new DateTimeOffset(dto.DateTime, IpoStatusCalculator.IstZone.BaseUtcOffset);
            }

            return null;
        }

        private static string CalculateStatusFromDates(string? open, string? close)
        {
            var op = ParseDate(open);
            var cl = ParseDate(close);
            return IpoStatusCalculator.CalculateStatus(op, cl);
        }

        public async Task UpdateGmpAsync(string ipoId, decimal newGmp)
        {
            var ipo = await _context.Ipos.FirstOrDefaultAsync(i => i.Id == ipoId);
            if (ipo == null) return;

            var oldGmp = ipo.CurrentGmp ?? 0m;
            ipo.CurrentGmp = newGmp;
            ipo.GmpSource = "InvestorGain";
            ipo.GmpFetchedAt = DateTimeOffset.UtcNow;
            ipo.UpdatedAt = DateTimeOffset.UtcNow;

            var gainPercent = ipo.PriceBandMax > 0 ? (newGmp / ipo.PriceBandMax) * 100 : 0;
            _context.GmpHistory.Add(new GmpRecord
            {
                IpoId = ipo.Id,
                Gmp = newGmp,
                GainPercent = gainPercent,
                Source = "InvestorGain",
                RecordedAt = DateTimeOffset.UtcNow
            });

            await _context.SaveChangesAsync();
        }

        public async Task MarkAllotmentOutAsync(string ipoId)
        {
            var ipo = await _context.Ipos.FirstOrDefaultAsync(i => i.Id == ipoId);
            if (ipo == null) return;

            ipo.IsAllotmentOut = true;
            ipo.Status = "ALLOTMENT_AVAILABLE";
            ipo.UpdatedAt = DateTimeOffset.UtcNow;
            await _context.SaveChangesAsync();
        }
    }
}
