using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using IpoDekho.Backend.Data;
using IpoDekho.Backend.Models.Dtos;
using IpoDekho.Backend.Models.Entities;

namespace IpoDekho.Backend.Controllers;

[ApiController]
[Route("api/v1/[controller]")]
public class IposController : ControllerBase
{
    private readonly AppDbContext _context;

    public IposController(AppDbContext context)
    {
        _context = context;
    }

    /// <summary>
    /// GET /api/v1/ipos?status=OPEN&category=MAINBOARD&q=tech
    /// Returns filtered IPO list formatted for IPODekho mobile client.
    /// </summary>
    [HttpGet]
    public async Task<ActionResult<List<IpoDto>>> GetAll(
        [FromQuery] string? status,
        [FromQuery] string? category,
        [FromQuery] string? q)
    {
        var query = _context.Ipos
            .Include(x => x.GmpHistories)
            .Include(x => x.Subscriptions)
            .AsNoTracking()
            .AsQueryable();

        if (!string.IsNullOrWhiteSpace(status))
        {
            query = query.Where(x => x.Status == status.ToUpperInvariant());
        }

        if (!string.IsNullOrWhiteSpace(category))
        {
            query = query.Where(x => x.Category == category.ToUpperInvariant());
        }

        if (!string.IsNullOrWhiteSpace(q))
        {
            var search = q.Trim().ToLower();
            query = query.Where(x => x.Name.ToLower().Contains(search) || x.Symbol.ToLower().Contains(search));
        }

        var ipos = await query.OrderByDescending(x => x.OpenDate).ToListAsync();
        return Ok(ipos.Select(MapToDto).ToList());
    }

    /// <summary>
    /// GET /api/v1/ipos/{id}
    /// Full detail for a single IPO.
    /// </summary>
    [HttpGet("{id}")]
    public async Task<ActionResult<IpoDto>> GetById(string id)
    {
        var ipo = await _context.Ipos
            .Include(x => x.GmpHistories)
            .Include(x => x.Subscriptions)
            .AsNoTracking()
            .FirstOrDefaultAsync(x => x.Id == id);

        if (ipo == null)
        {
            return NotFound(new { message = $"IPO with id '{id}' not found." });
        }

        return Ok(MapToDto(ipo));
    }

    /// <summary>
    /// GET /api/v1/ipos/{id}/subscription
    /// Day-by-day bidding breakdown.
    /// </summary>
    [HttpGet("{id}/subscription")]
    public async Task<ActionResult<SubscriptionDetailsDto>> GetSubscription(string id)
    {
        var ipo = await _context.Ipos
            .Include(x => x.Subscriptions)
            .AsNoTracking()
            .FirstOrDefaultAsync(x => x.Id == id);

        if (ipo == null)
        {
            return NotFound(new { message = $"IPO '{id}' not found." });
        }

        var dayBreakdown = ipo.Subscriptions
            .OrderBy(s => s.Id)
            .Select(s => new SubscriptionDayDto
            {
                DayLabel = s.DayLabel,
                Date = s.Date,
                QibTimes = (double)s.QibTimes,
                NiiTimes = (double)s.NiiTimes,
                RetailTimes = (double)s.RetailTimes,
                TotalTimes = (double)s.TotalTimes
            })
            .ToList();

        var result = new SubscriptionDetailsDto
        {
            IpoId = ipo.Id,
            QibTimes = (double)(ipo.QibTimes ?? 0),
            NiiTimes = (double)(ipo.NiiTimes ?? 0),
            RetailTimes = (double)(ipo.RetailTimes ?? 0),
            TotalTimes = (double)(ipo.CurrentSubscriptionTimes ?? 0),
            DayBreakdown = dayBreakdown
        };

        return Ok(result);
    }

    /// <summary>
    /// GET /api/v1/ipos/{id}/gmp-history
    /// Historical grey market premium data points for charting.
    /// </summary>
    [HttpGet("{id}/gmp-history")]
    public async Task<ActionResult<List<GmpHistoryItemDto>>> GetGmpHistory(string id)
    {
        var history = await _context.GmpHistories
            .Where(x => x.IpoId == id)
            .OrderBy(x => x.Date)
            .Select(x => new GmpHistoryItemDto
            {
                Date = x.Date,
                GmpAmount = (double)x.GmpAmount,
                EstimatedListingPrice = (double)(x.EstimatedPrice ?? 0),
                EstimatedGainPercent = (double)(x.EstimatedGainPercent ?? 0),
                Rating = x.Rating ?? "Neutral"
            })
            .ToListAsync();

        return Ok(history);
    }

    /// <summary>
    /// GET /api/v1/ipos/search?q=tata
    /// </summary>
    [HttpGet("search")]
    public async Task<ActionResult<List<IpoDto>>> Search([FromQuery] string q)
    {
        if (string.IsNullOrWhiteSpace(q))
        {
            return Ok(new List<IpoDto>());
        }

        var search = q.Trim().ToLower();
        var results = await _context.Ipos
            .AsNoTracking()
            .Where(x => x.Name.ToLower().Contains(search) || x.Symbol.ToLower().Contains(search))
            .Take(20)
            .ToListAsync();

        return Ok(results.Select(MapToDto).ToList());
    }

    private static IpoDto MapToDto(IpoEntity entity)
    {
        return new IpoDto
        {
            Id = entity.Id,
            Name = entity.Name,
            Symbol = entity.Symbol,
            Category = entity.Category,
            Status = entity.Status,
            PriceBandMin = (double?)entity.PriceBandMin,
            PriceBandMax = (double?)entity.PriceBandMax,
            LotSize = entity.LotSize,
            MinInvestment = (double?)entity.MinInvestment,
            IssueSizeCr = (double?)entity.IssueSizeCr,
            FreshIssueCr = (double?)entity.FreshIssueCr,
            OfsCr = (double?)entity.OfsCr,
            OpenDate = entity.OpenDate?.ToString("yyyy-MM-dd"),
            CloseDate = entity.CloseDate?.ToString("yyyy-MM-dd"),
            AllotmentDate = entity.AllotmentDate?.ToString("yyyy-MM-dd"),
            ListingDate = entity.ListingDate?.ToString("yyyy-MM-dd"),
            CurrentGmp = (double?)entity.CurrentGmp,
            EstimatedListingPrice = (double?)entity.EstimatedListingPrice,
            EstimatedGainPercent = (double?)entity.EstimatedGainPercent,
            LastGmpUpdated = entity.LastGmpUpdated?.ToString("yyyy-MM-dd HH:mm"),
            CurrentSubscriptionTimes = (double?)entity.CurrentSubscriptionTimes,
            QibTimes = (double?)entity.QibTimes,
            NiiTimes = (double?)entity.NiiTimes,
            RetailTimes = (double?)entity.RetailTimes,
            ListingPrice = (double?)entity.ListingPrice,
            ListingGainPercent = (double?)entity.ListingGainPercent,
            CurrentMarketPrice = (double?)entity.CurrentMarketPrice,
            CurrentReturnPercent = (double?)entity.CurrentReturnPercent,
            Description = entity.Description,
            Sector = entity.Sector,
            ListingExchanges = entity.ListingExchanges,
            FaceValue = (double?)entity.FaceValue,
            LeadManagers = entity.LeadManagers,
            Registrar = entity.Registrar,
            PromoterHoldingPre = (double?)entity.PromoterHoldingPre,
            PromoterHoldingPost = (double?)entity.PromoterHoldingPost,
            GmpHistory = entity.GmpHistories?.Select(g => new GmpHistoryItemDto
            {
                Date = g.Date,
                GmpAmount = (double)g.GmpAmount,
                EstimatedListingPrice = (double)(g.EstimatedPrice ?? 0),
                EstimatedGainPercent = (double)(g.EstimatedGainPercent ?? 0),
                Rating = g.Rating ?? "Neutral"
            }).ToList()
        };
    }
}
