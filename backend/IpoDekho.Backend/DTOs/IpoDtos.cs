using System.ComponentModel.DataAnnotations;

namespace IpoDekho.Backend.DTOs
{
    public class IpoDto
    {
        public string Id { get; set; } = string.Empty;
        public string Name { get; set; } = string.Empty;
        public string? Symbol { get; set; }
        public string Exchange { get; set; } = "BSE, NSE";
        public string Type { get; set; } = "MAINBOARD"; // MAINBOARD, SME
        public string Status { get; set; } = "UPCOMING"; // UPCOMING, OPEN, CLOSED, LISTED, ALLOTMENT_AVAILABLE
        public decimal PriceBandMin { get; set; }
        public decimal PriceBandMax { get; set; }
        public int LotSize { get; set; }
        public decimal IssueSizeCr { get; set; }
        public decimal CurrentGmp { get; set; }
        public decimal ExpectedListingPrice => PriceBandMax + CurrentGmp;
        public decimal ExpectedGainPercent => PriceBandMax > 0 ? (CurrentGmp / PriceBandMax) * 100 : 0;
        public DateTimeOffset? OpenDate { get; set; }
        public DateTimeOffset? CloseDate { get; set; }
        public DateTimeOffset? AllotmentDate { get; set; }
        public DateTimeOffset? ListingDate { get; set; }
        public string? RegistrarName { get; set; }
        public string? RegistrarUrl { get; set; }
        public bool IsAllotmentOut { get; set; }
        public string AllotmentStatus { get; set; } = "WAITING";
        public bool AllotmentAvailable => IsAllotmentOut || AllotmentStatus == "AVAILABLE";
        public string? AllotmentUrl => RegistrarUrl;
        public string IpoId => Id;
        public string CompanyName => Name;
        public DateTimeOffset UpdatedAt { get; set; }
    }

    public class IpoDetailDto : IpoDto
    {
        public List<GmpTickDto> GmpHistory { get; set; } = new();
        public SubscriptionDto? Subscription { get; set; }
    }

    public class GmpTickDto
    {
        public decimal Gmp { get; set; }
        public decimal GainPercent { get; set; }
        public DateTimeOffset RecordedAt { get; set; }
    }

    public class SubscriptionDto
    {
        public decimal Qib { get; set; }
        public decimal Nii { get; set; }
        public decimal Retail { get; set; }
        public decimal Employee { get; set; }
        public decimal Total { get; set; }
        public long ApplicationsCount { get; set; }
        public DateTimeOffset LastUpdated { get; set; }
    }

    public class BroadcastNoticeRequest
    {
        [Required]
        public string Title { get; set; } = string.Empty;

        [Required]
        public string Message { get; set; } = string.Empty;

        public string Category { get; set; } = "ANNOUNCEMENT";
        public string? TargetIpoId { get; set; }
    }

    public class ManualGmpUpdateRequest
    {
        [Required]
        public string IpoId { get; set; } = string.Empty;

        [Required]
        public decimal NewGmp { get; set; }
    }

    public class ExchangeConfigDto
    {
        public string ExchangeKey { get; set; } = string.Empty; // "NSE", "BSE"
        public string SourceUrl { get; set; } = string.Empty;
        public string DefaultUrl { get; set; } = string.Empty;
        public bool IsActive { get; set; } = true;
        public DateTimeOffset? LastSyncedAt { get; set; }
        public string? LastStatus { get; set; }
        public DateTimeOffset UpdatedAt { get; set; }
    }

    public class UpdateExchangeUrlRequest
    {
        [Required]
        public string ExchangeKey { get; set; } = string.Empty; // "NSE" or "BSE"

        [Required]
        public string SourceUrl { get; set; } = string.Empty;
    }
}
