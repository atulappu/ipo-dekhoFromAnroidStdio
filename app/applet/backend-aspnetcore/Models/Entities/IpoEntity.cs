using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace IpoDekho.Backend.Models.Entities;

[Table("Ipos")]
public class IpoEntity
{
    [Key]
    [MaxLength(64)]
    public string Id { get; set; } = string.Empty;

    [Required]
    [MaxLength(256)]
    public string Name { get; set; } = string.Empty;

    [Required]
    [MaxLength(32)]
    public string Symbol { get; set; } = string.Empty;

    [Required]
    [MaxLength(32)]
    public string Category { get; set; } = "MAINBOARD"; // MAINBOARD | SME

    [Required]
    [MaxLength(32)]
    public string Status { get; set; } = "UPCOMING";    // OPEN | UPCOMING | CLOSED | LISTED

    [Column(TypeName = "decimal(18,2)")]
    public decimal? PriceBandMin { get; set; }

    [Column(TypeName = "decimal(18,2)")]
    public decimal? PriceBandMax { get; set; }

    public int? LotSize { get; set; }

    [Column(TypeName = "decimal(18,2)")]
    public decimal? MinInvestment { get; set; }

    [Column(TypeName = "decimal(18,2)")]
    public decimal? IssueSizeCr { get; set; }

    [Column(TypeName = "decimal(18,2)")]
    public decimal? FreshIssueCr { get; set; }

    [Column(TypeName = "decimal(18,2)")]
    public decimal? OfsCr { get; set; }

    public DateTime? OpenDate { get; set; }
    public DateTime? CloseDate { get; set; }
    public DateTime? AllotmentDate { get; set; }
    public DateTime? ListingDate { get; set; }

    [Column(TypeName = "decimal(18,2)")]
    public decimal? CurrentGmp { get; set; }

    [Column(TypeName = "decimal(18,2)")]
    public decimal? EstimatedListingPrice { get; set; }

    [Column(TypeName = "decimal(18,2)")]
    public decimal? EstimatedGainPercent { get; set; }

    public DateTime? LastGmpUpdated { get; set; }

    [Column(TypeName = "decimal(18,2)")]
    public decimal? CurrentSubscriptionTimes { get; set; }

    [Column(TypeName = "decimal(18,2)")]
    public decimal? QibTimes { get; set; }

    [Column(TypeName = "decimal(18,2)")]
    public decimal? NiiTimes { get; set; }

    [Column(TypeName = "decimal(18,2)")]
    public decimal? RetailTimes { get; set; }

    [Column(TypeName = "decimal(18,2)")]
    public decimal? ListingPrice { get; set; }

    [Column(TypeName = "decimal(18,2)")]
    public decimal? ListingGainPercent { get; set; }

    [Column(TypeName = "decimal(18,2)")]
    public decimal? CurrentMarketPrice { get; set; }

    [Column(TypeName = "decimal(18,2)")]
    public decimal? CurrentReturnPercent { get; set; }

    public string? Description { get; set; }

    [MaxLength(128)]
    public string? Sector { get; set; }

    [MaxLength(64)]
    public string? ListingExchanges { get; set; }

    [Column(TypeName = "decimal(18,2)")]
    public decimal? FaceValue { get; set; }

    public string? LeadManagers { get; set; }

    [MaxLength(256)]
    public string? Registrar { get; set; }

    [Column(TypeName = "decimal(18,2)")]
    public decimal? PromoterHoldingPre { get; set; }

    [Column(TypeName = "decimal(18,2)")]
    public decimal? PromoterHoldingPost { get; set; }

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime UpdatedAt { get; set; } = DateTime.UtcNow;

    // Navigation Properties
    public List<GmpHistoryEntity> GmpHistories { get; set; } = new();
    public List<SubscriptionEntity> Subscriptions { get; set; } = new();
}
