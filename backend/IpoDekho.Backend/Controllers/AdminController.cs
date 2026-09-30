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

        /// <summary>
        /// Get all active exchange scraping endpoints (NSE, BSE)
        /// </summary>
        [HttpGet("exchange-urls")]
        public async Task<IActionResult> GetExchangeUrls()
        {
            var configs = await _context.ExchangeConfigs.ToListAsync();
            if (configs.Count == 0)
            {
                // Seed default verified exchange URLs
                configs = new List<ExchangeConfigRecord>
                {
                    new ExchangeConfigRecord
                    {
                        ExchangeKey = "NSE",
                        SourceUrl = "https://www.nseindia.com/market-data/all-upcoming-issues-ipo",
                        DefaultUrl = "https://www.nseindia.com/market-data/all-upcoming-issues-ipo",
                        IsActive = true,
                        LastStatus = "ACTIVE",
                        UpdatedAt = DateTimeOffset.UtcNow
                    },
                    new ExchangeConfigRecord
                    {
                        ExchangeKey = "BSE",
                        SourceUrl = "https://www.bseindia.com/markets/publicissues/ipoissues?expandable=4&id=1&Type=p",
                        DefaultUrl = "https://www.bseindia.com/markets/publicissues/ipoissues?expandable=4&id=1&Type=p",
                        IsActive = true,
                        LastStatus = "ACTIVE",
                        UpdatedAt = DateTimeOffset.UtcNow
                    }
                };
                _context.ExchangeConfigs.AddRange(configs);
                await _context.SaveChangesAsync();
            }

            var dtos = configs.Select(c => new ExchangeConfigDto
            {
                ExchangeKey = c.ExchangeKey,
                SourceUrl = c.SourceUrl,
                DefaultUrl = c.DefaultUrl,
                IsActive = c.IsActive,
                LastSyncedAt = c.LastSyncedAt,
                LastStatus = c.LastStatus,
                UpdatedAt = c.UpdatedAt
            }).ToList();

            return Ok(dtos);
        }

        /// <summary>
        /// Update an exchange scraping URL (Allows admin to change NSE or BSE URL anytime)
        /// </summary>
        [HttpPost("exchange-urls")]
        public async Task<IActionResult> UpdateExchangeUrl([FromBody] UpdateExchangeUrlRequest request)
        {
            if (string.IsNullOrWhiteSpace(request.ExchangeKey) || string.IsNullOrWhiteSpace(request.SourceUrl))
            {
                return BadRequest(new { message = "ExchangeKey and SourceUrl are required." });
            }

            var key = request.ExchangeKey.Trim().ToUpperInvariant();
            var config = await _context.ExchangeConfigs.FirstOrDefaultAsync(c => c.ExchangeKey == key);

            if (config == null)
            {
                config = new ExchangeConfigRecord
                {
                    ExchangeKey = key,
                    SourceUrl = request.SourceUrl.Trim(),
                    DefaultUrl = request.SourceUrl.Trim(),
                    IsActive = true,
                    UpdatedAt = DateTimeOffset.UtcNow
                };
                _context.ExchangeConfigs.Add(config);
            }
            else
            {
                config.SourceUrl = request.SourceUrl.Trim();
                config.UpdatedAt = DateTimeOffset.UtcNow;
            }

            await _context.SaveChangesAsync();

            return Ok(new
            {
                success = true,
                message = $"{key} source URL updated successfully.",
                exchangeKey = key,
                sourceUrl = config.SourceUrl,
                updatedAt = config.UpdatedAt
            });
        }

        /// <summary>
        /// Reset exchange URLs back to verified default addresses
        /// </summary>
        [HttpPost("reset-exchange-urls")]
        public async Task<IActionResult> ResetExchangeUrls()
        {
            var configs = await _context.ExchangeConfigs.ToListAsync();
            foreach (var cfg in configs)
            {
                if (cfg.ExchangeKey == "NSE")
                {
                    cfg.SourceUrl = "https://www.nseindia.com/market-data/all-upcoming-issues-ipo";
                }
                else if (cfg.ExchangeKey == "BSE")
                {
                    cfg.SourceUrl = "https://www.bseindia.com/markets/publicissues/ipoissues?expandable=4&id=1&Type=p";
                }
                cfg.UpdatedAt = DateTimeOffset.UtcNow;
            }
            await _context.SaveChangesAsync();

            return Ok(new { success = true, message = "Exchange URLs restored to official defaults." });
        }
    }
}
