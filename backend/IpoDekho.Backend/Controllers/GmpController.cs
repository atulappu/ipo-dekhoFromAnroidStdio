using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using IpoDekho.Backend.Data;
using IpoDekho.Backend.DTOs;
using IpoDekho.Backend.Services;

namespace IpoDekho.Backend.Controllers
{
    [ApiController]
    [Route("api/v1/[controller]")]
    public class GmpController : ControllerBase
    {
        private readonly IpoDbContext _context;
        private readonly IIpoScraperService _scraperService;

        public GmpController(IpoDbContext context, IIpoScraperService scraperService)
        {
            _context = context;
            _scraperService = scraperService;
        }

        /// <summary>
        /// Get top GMP gainers ranked by estimated % listing gain
        /// </summary>
        [HttpGet("top-movers")]
        public async Task<IActionResult> GetTopMovers()
        {
            var ipos = await _context.Ipos
                .Where(i => i.CurrentGmp > 0 && i.PriceBandMax > 0)
                .OrderByDescending(i => (i.CurrentGmp / i.PriceBandMax))
                .Take(10)
                .Select(i => new IpoDto
                {
                    Id = i.Id,
                    Name = i.Name,
                    Symbol = i.Symbol,
                    Type = i.Type,
                    Status = i.Status,
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
                })
                .ToListAsync();

            return Ok(ipos);
        }

        /// <summary>
        /// Update GMP rate manually or via external webhook
        /// </summary>
        [HttpPost("update")]
        public async Task<IActionResult> UpdateGmp([FromBody] ManualGmpUpdateRequest request)
        {
            await _scraperService.UpdateGmpAsync(request.IpoId, request.NewGmp);
            return Ok(new { success = true, ipoId = request.IpoId, newGmp = request.NewGmp, message = "GMP updated and push alert broadcasted." });
        }
    }
}
