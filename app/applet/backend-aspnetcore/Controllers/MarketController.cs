using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using IpoDekho.Backend.Data;
using IpoDekho.Backend.Models.Dtos;

namespace IpoDekho.Backend.Controllers;

[ApiController]
[Route("api/v1/[controller]")]
public class MarketController : ControllerBase
{
    private readonly AppDbContext _context;

    public MarketController(AppDbContext context)
    {
        _context = context;
    }

    /// <summary>
    /// GET /api/v1/market/indices
    /// Live index tickers: Nifty 50, Sensex, Bank Nifty, etc.
    /// </summary>
    [HttpGet("indices")]
    public async Task<ActionResult<List<MarketIndexDto>>> GetIndices()
    {
        var indices = await _context.MarketIndices
            .AsNoTracking()
            .Select(i => new MarketIndexDto
            {
                Symbol = i.Symbol,
                Name = i.Name,
                CurrentValue = (double)i.CurrentValue,
                ChangeValue = (double)i.ChangeValue,
                ChangePercent = (double)i.ChangePercent
            })
            .ToListAsync();

        return Ok(indices);
    }
}
