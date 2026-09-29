using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using IPODekho.Domain.Enums;

namespace IPODekho.Domain.Entities;

[Table("Roles")]
public class Role
{
    [Key]
    public int RoleId { get; set; }
    [Required, MaxLength(50)]
    public string RoleName { get; set; } = string.Empty;
    [MaxLength(250)]
    public string? Description { get; set; }
    public DateTime CreatedDate { get; set; } = DateTime.UtcNow;

    public virtual ICollection<UserRole> UserRoles { get; set; } = new List<UserRole>();
}

[Table("Users")]
public class User
{
    [Key]
    public int UserId { get; set; }
    [Required, MaxLength(100)]
    public string Username { get; set; } = string.Empty;
    [Required, MaxLength(150), EmailAddress]
    public string Email { get; set; } = string.Empty;
    [Required]
    public string PasswordHash { get; set; } = string.Empty;
    [Required, MaxLength(150)]
    public string FullName { get; set; } = string.Empty;
    [MaxLength(20)]
    public string? PhoneNumber { get; set; }
    public bool IsActive { get; set; } = true;
    public DateTime CreatedDate { get; set; } = DateTime.UtcNow;
    public DateTime? ModifiedDate { get; set; }

    public virtual ICollection<UserRole> UserRoles { get; set; } = new List<UserRole>();
    public virtual ICollection<Watchlist> Watchlists { get; set; } = new List<Watchlist>();
}

[Table("UserRoles")]
public class UserRole
{
    [Key]
    public int UserRoleId { get; set; }
    public int UserId { get; set; }
    public int RoleId { get; set; }
    public DateTime AssignedDate { get; set; } = DateTime.UtcNow;

    [ForeignKey(nameof(UserId))]
    public virtual User? User { get; set; }
    [ForeignKey(nameof(RoleId))]
    public virtual Role? Role { get; set; }
}

[Table("Companies")]
public class Company
{
    [Key]
    public int CompanyId { get; set; }
    [Required, MaxLength(250)]
    public string CompanyName { get; set; } = string.Empty;
    [MaxLength(150)]
    public string? Industry { get; set; }
    [MaxLength(250)]
    public string? Website { get; set; }
    public int? IncorporationYear { get; set; }
    [MaxLength(150)]
    public string? ManagingDirector { get; set; }
    public string? RegisteredAddress { get; set; }
}

[Table("Registrars")]
public class Registrar
{
    [Key]
    public int RegistrarId { get; set; }
    [Required, MaxLength(200)]
    public string RegistrarName { get; set; } = string.Empty;
    [MaxLength(250)]
    public string? PortalUrl { get; set; }
    [MaxLength(50)]
    public string? Phone { get; set; }
    [MaxLength(100)]
    public string? Email { get; set; }
}

[Table("IPOs")]
public class Ipo
{
    [Key]
    public int IPOId { get; set; }
    public int? CompanyId { get; set; }
    [Required, MaxLength(250)]
    public string IPOName { get; set; } = string.Empty;
    [Required, MaxLength(50)]
    public string Symbol { get; set; } = string.Empty;
    [Required, MaxLength(20)]
    public string MarketType { get; set; } = "MAINBOARD"; // MAINBOARD | SME
    [Required, MaxLength(20)]
    public string Status { get; set; } = "UPCOMING";     // OPEN | UPCOMING | CLOSED | LISTED
    public decimal? PriceBandMin { get; set; }
    public decimal? PriceBandMax { get; set; }
    public int? LotSize { get; set; }
    public decimal? IssueSize { get; set; } // In Crores
    public decimal? FreshIssue { get; set; }
    public decimal? OfferForSale { get; set; }
    public decimal? FaceValue { get; set; }
    public DateTime? OpenDate { get; set; }
    public DateTime? CloseDate { get; set; }
    public DateTime? AllotmentDate { get; set; }
    public DateTime? ListingDate { get; set; }
    public decimal? ListingPrice { get; set; }
    public decimal? CurrentMarketPrice { get; set; }
    public int? RegistrarId { get; set; }
    [MaxLength(50)]
    public string? ListingExchanges { get; set; } = "NSE, BSE";
    public string? Description { get; set; }
    public bool IsActive { get; set; } = true;
    public DateTime CreatedDate { get; set; } = DateTime.UtcNow;
    public DateTime? ModifiedDate { get; set; }

    [ForeignKey(nameof(CompanyId))]
    public virtual Company? Company { get; set; }

    [ForeignKey(nameof(RegistrarId))]
    public virtual Registrar? Registrar { get; set; }

    public virtual GMPDetail? GMPDetail { get; set; }
    public virtual SubscriptionDetail? SubscriptionDetail { get; set; }
    public virtual ICollection<GMPHistory> GMPHistories { get; set; } = new List<GMPHistory>();
    public virtual ICollection<FinancialDetail> FinancialDetails { get; set; } = new List<FinancialDetail>();
    public virtual ICollection<SubscriptionBreakdown> SubscriptionBreakdowns { get; set; } = new List<SubscriptionBreakdown>();
}

[Table("GMPDetails")]
public class GMPDetail
{
    [Key]
    public int GMPId { get; set; }
    public int IPOId { get; set; }
    public decimal CurrentGMP { get; set; }
    public decimal? EstimatedListingPrice { get; set; }
    public decimal? EstimatedGainPercent { get; set; }
    [MaxLength(50)]
    public string? Rating { get; set; } = "Neutral";
    public decimal? KostakRate { get; set; }
    public decimal? SubjectToSauda { get; set; }
    public DateTime LastUpdated { get; set; } = DateTime.UtcNow;

    [ForeignKey(nameof(IPOId))]
    public virtual Ipo? Ipo { get; set; }
}

[Table("GMPHistory")]
public class GMPHistory
{
    [Key]
    public long HistoryId { get; set; }
    public int IPOId { get; set; }
    [Required, MaxLength(30)]
    public string EntryDate { get; set; } = string.Empty;
    public decimal GMPAmount { get; set; }
    public decimal? EstimatedPrice { get; set; }
    public decimal? GainPercent { get; set; }
    public DateTime RecordedAt { get; set; } = DateTime.UtcNow;

    [ForeignKey(nameof(IPOId))]
    public virtual Ipo? Ipo { get; set; }
}

[Table("SubscriptionDetails")]
public class SubscriptionDetail
{
    [Key]
    public int SubscriptionId { get; set; }
    public int IPOId { get; set; }
    public decimal QIBTimes { get; set; }
    public decimal NIITimes { get; set; }
    public decimal RetailTimes { get; set; }
    public decimal TotalTimes { get; set; }
    public DateTime LastUpdated { get; set; } = DateTime.UtcNow;

    [ForeignKey(nameof(IPOId))]
    public virtual Ipo? Ipo { get; set; }
}

[Table("SubscriptionBreakdown")]
public class SubscriptionBreakdown
{
    [Key]
    public long BreakdownId { get; set; }
    public int IPOId { get; set; }
    [Required, MaxLength(50)]
    public string DayLabel { get; set; } = string.Empty;
    public DateTime BiddingDate { get; set; }
    public decimal QIBTimes { get; set; }
    public decimal NIITimes { get; set; }
    public decimal RetailTimes { get; set; }
    public decimal TotalTimes { get; set; }

    [ForeignKey(nameof(IPOId))]
    public virtual Ipo? Ipo { get; set; }
}

[Table("FinancialDetails")]
public class FinancialDetail
{
    [Key]
    public int FinancialId { get; set; }
    public int IPOId { get; set; }
    [Required, MaxLength(20)]
    public string FinancialYear { get; set; } = string.Empty;
    public decimal? RevenueCr { get; set; }
    public decimal? ExpenseCr { get; set; }
    public decimal? ProfitAfterTaxCr { get; set; }
    public decimal? NetWorthCr { get; set; }
    public decimal? TotalAssetsCr { get; set; }

    [ForeignKey(nameof(IPOId))]
    public virtual Ipo? Ipo { get; set; }
}

[Table("Watchlists")]
public class Watchlist
{
    [Key]
    public int WatchlistId { get; set; }
    public int UserId { get; set; }
    public int IPOId { get; set; }
    public DateTime AddedDate { get; set; } = DateTime.UtcNow;

    [ForeignKey(nameof(UserId))]
    public virtual User? User { get; set; }
    [ForeignKey(nameof(IPOId))]
    public virtual Ipo? Ipo { get; set; }
}

[Table("AuditLogs")]
public class AuditLog
{
    [Key]
    public long LogId { get; set; }
    public int? UserId { get; set; }
    [Required, MaxLength(100)]
    public string Action { get; set; } = string.Empty;
    [Required, MaxLength(100)]
    public string Entity { get; set; } = string.Empty;
    [MaxLength(100)]
    public string? EntityId { get; set; }
    public string? OldValue { get; set; }
    public string? NewValue { get; set; }
    public DateTime Timestamp { get; set; } = DateTime.UtcNow;
    [MaxLength(50)]
    public string? IpAddress { get; set; }
}
