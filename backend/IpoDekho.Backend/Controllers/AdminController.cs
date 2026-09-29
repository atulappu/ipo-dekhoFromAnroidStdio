using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using IpoDekho.Backend.Data;

namespace IpoDekho.Backend.Controllers
{
    [ApiController]
    [Route("api/v1/[controller]")]
    public class AdminController : ControllerBase
    {
        private readonly IpoDbContext _context;

        public AdminController(IpoDbContext context)
        {
            _context = context;
        }

        [HttpGet("notices")]
        public async Task<IActionResult> GetNotices()
        {
            var notices = await _context.AdminNotices
                .Where(n => n.IsActive)
                .OrderByDescending(n => n.CreatedAt)
                .ToListAsync();
            return Ok(notices);
        }

        [HttpPost("broadcast")]
        public async Task<IActionResult> BroadcastNotice([FromBody] AdminNoticeRecord notice)
        {
            if (string.IsNullOrWhiteSpace(notice.Id))
            {
                notice.Id = Guid.NewGuid().ToString();
            }
            notice.CreatedAt = DateTimeOffset.UtcNow;
            notice.IsActive = true;

            _context.AdminNotices.Add(notice);
            await _context.SaveChangesAsync();

            return Ok(new { success = true, noticeId = notice.Id, message = "Notice broadcasted successfully to all users" });
        }
    }
}
