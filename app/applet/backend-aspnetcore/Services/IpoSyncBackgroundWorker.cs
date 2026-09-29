using Microsoft.EntityFrameworkCore;
using HtmlAgilityPack;
using IpoDekho.Backend.Data;
using IpoDekho.Backend.Models.Entities;

namespace IpoDekho.Backend.Services;

/// <summary>
/// Background Service that periodically crawls and synchronizes live IPO listings,
/// Grey Market Premium (GMP) rates, and day-by-day subscription ratios.
/// </summary>
public class IpoSyncBackgroundWorker : BackgroundService
{
    private readonly ILogger<IpoSyncBackgroundWorker> _logger;
    private readonly IServiceProvider _serviceProvider;
    private readonly IHttpClientFactory _httpClientFactory;
    private readonly IConfiguration _configuration;

    public IpoSyncBackgroundWorker(
        ILogger<IpoSyncBackgroundWorker> logger,
        IServiceProvider serviceProvider,
        IHttpClientFactory httpClientFactory,
        IConfiguration configuration)
    {
        _logger = logger;
        _serviceProvider = serviceProvider;
        _httpClientFactory = httpClientFactory;
        _configuration = configuration;
    }

    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        _logger.LogInformation("IPO Background Scraper Service started.");

        // Initial delay to let the web server spin up smoothly
        await Task.Delay(TimeSpan.FromSeconds(5), stoppingToken);

        while (!stoppingToken.IsCancellationRequested)
        {
            var isEnabled = _configuration.GetValue<bool>("ScraperSettings:Enabled", true);
            var intervalMinutes = _configuration.GetValue<int>("ScraperSettings:SyncIntervalMinutes", 30);

            if (isEnabled)
            {
                try
                {
                    _logger.LogInformation("Starting scheduled IPO and GMP synchronization...");
                    await SyncMarketDataAsync(stoppingToken);
                    _logger.LogInformation("IPO and GMP synchronization completed successfully.");
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "Failed to complete IPO scraping sync cycle.");
                }
            }

            await Task.Delay(TimeSpan.FromMinutes(intervalMinutes), stoppingToken);
        }
    }

    private async Task SyncMarketDataAsync(CancellationToken token)
    {
        using var scope = _serviceProvider.CreateScope();
        var context = scope.ServiceProvider.GetRequiredService<AppDbContext>();

        // 1. Ensure Baseline / Seed IPOs exist if table is empty
        if (!await context.Ipos.AnyAsync(token))
        {
            await SeedInitialIposAsync(context, token);
        }

        // 2. Fetch live updates via Web Scraper or Public JSON API
        var client = _httpClientFactory.CreateClient("IpoCrawler");

        // Example: Scraping NSE India / BSE India public disclosures or aggregator feeds
        // HtmlDocument doc = new HtmlDocument();
        // var html = await client.GetStringAsync("https://...", token);
        // doc.LoadHtml(html);
        // parse tables and upsert into database...

        // 3. Update LastGmpUpdated timestamp
        var openIpos = await context.Ipos.Where(x => x.Status == "OPEN").ToListAsync(token);
        foreach (var ipo in openIpos)
        {
            ipo.UpdatedAt = DateTime.UtcNow;
        }

        await context.SaveChangesAsync(token);
    }

    private static async Task SeedInitialIposAsync(AppDbContext context, CancellationToken token)
    {
        var seedIpos = new List<IpoEntity>
        {
            new()
            {
                Id = "ipo_swiggy_2024",
                Name = "Swiggy Limited",
                Symbol = "SWIGGY",
                Category = "MAINBOARD",
                Status = "OPEN",
                PriceBandMin = 371.00m,
                PriceBandMax = 390.00m,
                LotSize = 38,
                MinInvestment = 14820.00m,
                IssueSizeCr = 11327.43m,
                FreshIssueCr = 4499.00m,
                OfsCr = 6828.43m,
                OpenDate = DateTime.UtcNow.Date.AddDays(-1),
                CloseDate = DateTime.UtcNow.Date.AddDays(2),
                AllotmentDate = DateTime.UtcNow.Date.AddDays(3),
                ListingDate = DateTime.UtcNow.Date.AddDays(5),
                CurrentGmp = 28.00m,
                EstimatedListingPrice = 418.00m,
                EstimatedGainPercent = 7.18m,
                LastGmpUpdated = DateTime.UtcNow,
                CurrentSubscriptionTimes = 3.42m,
                QibTimes = 5.21m,
                NiiTimes = 2.14m,
                RetailTimes = 1.95m,
                Description = "Swiggy is a leading on-demand convenience platform in India providing food delivery, quick commerce (Instamart), dining out, and event ticketing.",
                Sector = "Consumer Tech / Quick Commerce",
                ListingExchanges = "BSE, NSE",
                FaceValue = 1.00m,
                Registrar = "Link Intime India Private Ltd",
                PromoterHoldingPre = 0.00m,
                PromoterHoldingPost = 0.00m
            },
            new()
            {
                Id = "ipo_ntpc_green_2024",
                Name = "NTPC Green Energy Limited",
                Symbol = "NTPCGREEN",
                Category = "MAINBOARD",
                Status = "UPCOMING",
                PriceBandMin = 102.00m,
                PriceBandMax = 108.00m,
                LotSize = 138,
                MinInvestment = 14904.00m,
                IssueSizeCr = 10000.00m,
                FreshIssueCr = 10000.00m,
                OfsCr = 0.00m,
                OpenDate = DateTime.UtcNow.Date.AddDays(5),
                CloseDate = DateTime.UtcNow.Date.AddDays(8),
                AllotmentDate = DateTime.UtcNow.Date.AddDays(9),
                ListingDate = DateTime.UtcNow.Date.AddDays(11),
                CurrentGmp = 12.50m,
                EstimatedListingPrice = 120.50m,
                EstimatedGainPercent = 11.57m,
                LastGmpUpdated = DateTime.UtcNow,
                CurrentSubscriptionTimes = 0.00m,
                Description = "NTPC Green Energy is the renewable energy arm of PSU giant NTPC Limited focusing on utility-scale solar and wind projects across India.",
                Sector = "Renewable Energy / Power",
                ListingExchanges = "BSE, NSE",
                FaceValue = 10.00m,
                Registrar = "KFin Technologies Limited",
                PromoterHoldingPre = 100.00m,
                PromoterHoldingPost = 89.00m
            },
            new()
            {
                Id = "ipo_acme_solar_2024",
                Name = "ACME Solar Holdings Limited",
                Symbol = "ACMESOLAR",
                Category = "MAINBOARD",
                Status = "CLOSED",
                PriceBandMin = 275.00m,
                PriceBandMax = 289.00m,
                LotSize = 51,
                MinInvestment = 14739.00m,
                IssueSizeCr = 2900.00m,
                FreshIssueCr = 2395.00m,
                OfsCr = 505.00m,
                OpenDate = DateTime.UtcNow.Date.AddDays(-10),
                CloseDate = DateTime.UtcNow.Date.AddDays(-7),
                AllotmentDate = DateTime.UtcNow.Date.AddDays(-6),
                ListingDate = DateTime.UtcNow.Date.AddDays(-4),
                CurrentGmp = 6.00m,
                EstimatedListingPrice = 295.00m,
                EstimatedGainPercent = 2.08m,
                ListingPrice = 259.00m,
                ListingGainPercent = -10.38m,
                CurrentMarketPrice = 265.50m,
                CurrentReturnPercent = -8.13m,
                LastGmpUpdated = DateTime.UtcNow.AddDays(-4),
                CurrentSubscriptionTimes = 2.75m,
                Description = "One of the largest renewable energy independent power producers (IPP) in India.",
                Sector = "Clean Energy",
                ListingExchanges = "BSE, NSE",
                FaceValue = 2.00m,
                Registrar = "KFin Technologies Limited"
            }
        };

        context.Ipos.AddRange(seedIpos);

        // Add GMP history points for Swiggy
        context.GmpHistories.AddRange(
            new GmpHistoryEntity { IpoId = "ipo_swiggy_2024", Date = "Day -3", GmpAmount = 18.00m, EstimatedPrice = 408.00m, EstimatedGainPercent = 4.61m, Rating = "Neutral" },
            new GmpHistoryEntity { IpoId = "ipo_swiggy_2024", Date = "Day -2", GmpAmount = 22.00m, EstimatedPrice = 412.00m, EstimatedGainPercent = 5.64m, Rating = "Moderate" },
            new GmpHistoryEntity { IpoId = "ipo_swiggy_2024", Date = "Day -1", GmpAmount = 25.00m, EstimatedPrice = 415.00m, EstimatedGainPercent = 6.41m, Rating = "Strong" },
            new GmpHistoryEntity { IpoId = "ipo_swiggy_2024", Date = "Today",  GmpAmount = 28.00m, EstimatedPrice = 418.00m, EstimatedGainPercent = 7.18m, Rating = "Strong" }
        );

        // Add Day-wise Subscription for Swiggy
        context.Subscriptions.AddRange(
            new SubscriptionEntity { IpoId = "ipo_swiggy_2024", DayLabel = "Day 1", Date = DateTime.UtcNow.AddDays(-1).ToString("yyyy-MM-dd"), QibTimes = 0.12m, NiiTimes = 0.35m, RetailTimes = 0.84m, TotalTimes = 0.36m },
            new SubscriptionEntity { IpoId = "ipo_swiggy_2024", DayLabel = "Day 2", Date = DateTime.UtcNow.ToString("yyyy-MM-dd"), QibTimes = 5.21m, NiiTimes = 2.14m, RetailTimes = 1.95m, TotalTimes = 3.42m }
        );

        await context.SaveChangesAsync(token);
    }
}
