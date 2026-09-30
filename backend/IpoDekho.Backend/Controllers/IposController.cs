using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using IpoDekho.Backend.Data;
using IpoDekho.Backend.DTOs;
using IpoDekho.Backend.Services;

namespace IpoDekho.Backend.Controllers
{
    [ApiController]
    [Route("api/v1/[controller]")]
    public class IposController : ControllerBase
    {
        private readonly IpoDbContext _context;
        private readonly IIpoScraperService _scraperService;

        public IposController(IpoDbContext context, IIpoScraperService scraperService)
        {
            _context = context;
            _scraperService = scraperService;
        }

        /// <summary>
        /// Get list of IPOs with optional status (OPEN, UPCOMING, ALLOTMENT_AVAILABLE, LISTED) and type (MAINBOARD, SME) filters
        /// </summary>
        [HttpGet]
        public async Task<IActionResult> GetIpos(
            [FromQuery] string? status,
            [FromQuery] string? type,
            [FromQuery] string? search)
        {
            var query = _context.Ipos.AsQueryable();

            if (!string.IsNullOrWhiteSpace(status) && status.ToUpper() != "ALL")
            {
                query = query.Where(i => i.Status == status);
            }

            if (!string.IsNullOrWhiteSpace(type) && type.ToUpper() != "ALL")
            {
                query = query.Where(i => i.Type == type);
            }

            if (!string.IsNullOrWhiteSpace(search))
            {
                var term = search.Trim().ToLower();
                query = query.Where(i => i.Name.ToLower().Contains(term) || (i.Symbol != null && i.Symbol.ToLower().Contains(term)));
            }

            var rawList = await query
                .OrderByDescending(i => i.OpenDate)
                .ToListAsync();

            var list = rawList.Select(i => new IpoDto
            {
                Id = i.Id,
                Name = i.Name,
                Symbol = i.Symbol,
                Type = i.Type,
                Status = IpoStatusCalculator.GetIPOStatus(i),
                PriceBandMin = i.PriceBandMin,
                PriceBandMax = i.PriceBandMax,
                LotSize = i.LotSize,
                IssueSizeCr = i.IssueSizeCr,
                CurrentGmp = i.CurrentGmp,
                OpenDate = i.OpenDate,
                CloseDate = i.CloseDate,
                AllotmentDate = i.AllotmentDate,
                ListingDate = i.ListingDate,
                RegistrarName = i.RegistrarName,
                RegistrarUrl = i.RegistrarUrl,
                IsAllotmentOut = i.IsAllotmentOut,
                UpdatedAt = i.UpdatedAt
            }).ToList();

            if (!string.IsNullOrWhiteSpace(status) && status.ToUpper() != "ALL")
            {
                var targetStatus = status.Trim().ToUpper();
                list = list.Where(i => i.Status.Equals(targetStatus, StringComparison.OrdinalIgnoreCase)).ToList();
            }

            return Ok(list);
        }

        /// <summary>
        /// Get single IPO details including GMP history and live subscription numbers
        /// </summary>
        [HttpGet("{id}")]
        public async Task<IActionResult> GetIpoById(string id)
        {
            var ipo = await _context.Ipos.FirstOrDefaultAsync(i => i.Id == id);
            if (ipo == null) return NotFound(new { message = $"IPO with id '{id}' not found." });

            var gmpTicks = await _context.GmpHistory
                .Where(g => g.IpoId == id)
                .OrderBy(g => g.RecordedAt)
                .Select(g => new GmpTickDto
                {
                    Gmp = g.Gmp,
                    GainPercent = g.GainPercent,
                    RecordedAt = g.RecordedAt
                })
                .ToListAsync();

            var sub = await _context.Subscriptions
                .FirstOrDefaultAsync(s => s.IpoId == id);

            var detail = new IpoDetailDto
            {
                Id = ipo.Id,
                Name = ipo.Name,
                Symbol = ipo.Symbol,
                Type = ipo.Type,
                Status = IpoStatusCalculator.GetIPOStatus(ipo),
                PriceBandMin = ipo.PriceBandMin,
                PriceBandMax = ipo.PriceBandMax,
                LotSize = ipo.LotSize,
                IssueSizeCr = ipo.IssueSizeCr,
                CurrentGmp = ipo.CurrentGmp,
                OpenDate = ipo.OpenDate,
                CloseDate = ipo.CloseDate,
                AllotmentDate = ipo.AllotmentDate,
                ListingDate = ipo.ListingDate,
                RegistrarName = ipo.RegistrarName,
                RegistrarUrl = ipo.RegistrarUrl,
                IsAllotmentOut = ipo.IsAllotmentOut,
                UpdatedAt = ipo.UpdatedAt,
                GmpHistory = gmpTicks,
                Subscription = sub == null ? null : new SubscriptionDto
                {
                    Qib = sub.Qib,
                    Nii = sub.Nii,
                    Retail = sub.Retail,
                    Employee = sub.Employee,
                    Total = sub.Total,
                    ApplicationsCount = sub.ApplicationsCount,
                    LastUpdated = sub.LastUpdated
                }
            };

            return Ok(detail);
        }

        /// <summary>
        /// Trigger on-demand sync from live market feeds immediately
        /// </summary>
        [HttpPost("sync")]
        public async Task<IActionResult> TriggerSync()
        {
            var syncedCount = await _scraperService.SyncIpoDataAsync();
            return Ok(new { success = true, syncedCount, message = $"Successfully synced {syncedCount} records from live exchange feeds." });
        }
    }
}
