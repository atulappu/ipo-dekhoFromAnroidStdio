using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using IpoDekho.Backend.Data;
using IpoDekho.Backend.Services;

namespace IpoDekho.Backend.Controllers
{
    [ApiController]
    [Route("api/v1/[controller]")]
    public class AllotmentController : ControllerBase
    {
        private readonly IpoDbContext _context;
        private readonly IIpoScraperService _scraperService;

        public AllotmentController(IpoDbContext context, IIpoScraperService scraperService)
        {
            _context = context;
            _scraperService = scraperService;
        }

        /// <summary>
        /// Get all official registrar allotment portals
        /// </summary>
        [HttpGet("registrars")]
        public IActionResult GetRegistrars()
        {
            var registrars = new[]
            {
                new { Name = "Link Intime India Pvt Ltd", Url = "https://linkintime.co.in/initial_offer/public-issues.html", Description = "Primary registrar for large Indian mainboard issues" },
                new { Name = "KFin Technologies Ltd", Url = "https://kosmic.kfintech.com/ipostatus", Description = "Major registrar for BSE/NSE IPO allotments" },
                new { Name = "Bigshare Services Pvt Ltd", Url = "https://www.bigshareonline.com/ipo_Allotment.html", Description = "Leading registrar for SME IPO allotments" },
                new { Name = "Skyline Financial Services", Url = "https://www.skylinerta.com/ipo.php", Description = "Registrar for corporate SME and mainboard issues" },
                new { Name = "Cameo Corporate Services", Url = "https://ipo.cameoindia.com/", Description = "South India major registrar for IPO allotment" }
            };
            return Ok(registrars);
        }

        /// <summary>
        /// Admin action to mark an IPO allotment as live and trigger urgent notification
        /// </summary>
        [HttpPost("{ipoId}/mark-out")]
        public async Task<IActionResult> MarkAllotmentOut(string ipoId)
        {
            await _scraperService.MarkAllotmentOutAsync(ipoId);
            return Ok(new { success = true, ipoId, message = "Allotment status marked OUT and alert dispatched to all mobile devices." });
        }
    }
}
