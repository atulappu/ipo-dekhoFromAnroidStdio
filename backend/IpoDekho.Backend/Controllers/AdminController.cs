using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using IpoDekho.Backend.Data;
using IpoDekho.Backend.DTOs;
using IpoDekho.Backend.Services;

namespace IpoDekho.Backend.Controllers
{
    [ApiController]
    [Route("api/v1/[controller]")]
    public class AdminController : ControllerBase
    {
        private readonly IpoDbContext _context;
        private readonly IFirebaseNotificationService _notificationService;

        public AdminController(IpoDbContext context, IFirebaseNotificationService notificationService)
        {
            _context = context;
            _notificationService = notificationService;
        }

        /// <summary>
        /// Get all active admin announcements
        /// </summary>
        [HttpGet("notices")]
        public async Task<IActionResult> GetNotices()
        {
            var notices = await _context.AdminNotices
                .Where(n => n.IsActive)
                .OrderByDescending(n => n.CreatedAt)
                .ToListAsync();

            return Ok(notices);
        }

        /// <summary>
        /// Broadcast an urgent notice to all mobile app users
        /// </summary>
        [HttpPost("broadcast")]
        public async Task<IActionResult> BroadcastNotice([FromBody] BroadcastNoticeRequest request)
        {
            var record = new AdminNoticeRecord
            {
                Id = Guid.NewGuid().ToString(),
                Title = request.Title,
                Message = request.Message,
                Category = request.Category,
                TargetIpoId = request.TargetIpoId,
                IsActive = true,
                CreatedAt = DateTimeOffset.UtcNow
            };

            _context.AdminNotices.Add(record);
            await _context.SaveChangesAsync();

            // Broadcast push notification
            await _notificationService.BroadcastAdminNoticeAsync(request.Title, request.Message, request.TargetIpoId);

            return Ok(new
            {
                success = true,
                noticeId = record.Id,
                message = "Notice broadcasted successfully to all devices."
            });
        }
    }
}
