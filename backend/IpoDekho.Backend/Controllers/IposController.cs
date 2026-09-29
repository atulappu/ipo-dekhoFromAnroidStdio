using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using IpoDekho.Backend.Data;

namespace IpoDekho.Backend.Controllers
{
    [ApiController]
    [Route("api/v1/[controller]")]
    public class IposController : ControllerBase
    {
        private readonly IpoDbContext _context;

        public IposController(IpoDbContext context)
        {
            _context = context;
        }

        [HttpGet]
        public async Task<IActionResult> GetIpos([FromQuery] string? status)
        {
            var query = _context.Ipos.AsQueryable();

            if (!string.IsNullOrWhiteSpace(status) && status != "ALL")
            {
                query = query.Where(i => i.Status == status);
            }

            var list = await query.OrderByDescending(i => i.OpenDate).ToListAsync();
            return Ok(list);
        }

        [HttpGet("{id}")]
        public async Task<IActionResult> GetIpoById(string id)
        {
            var ipo = await _context.Ipos.FirstOrDefaultAsync(i => i.Id == id);
            if (ipo == null) return NotFound(new { message = "IPO not found" });

            var gmpHistory = await _context.GmpHistory
                .Where(g => g.IpoId == id)
                .OrderBy(g => g.RecordedAt)
                .ToListAsync();

            var subscription = await _context.Subscriptions
                .FirstOrDefaultAsync(s => s.IpoId == id);

            return Ok(new
            {
                ipo,
                gmpHistory,
                subscription
            });
        }
    }
}
