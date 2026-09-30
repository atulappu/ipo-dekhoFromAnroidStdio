using Microsoft.EntityFrameworkCore;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace IpoDekho.Backend.Data
{
    public class IpoDbContext : DbContext
    {
        public IpoDbContext(DbContextOptions<IpoDbContext> options) : base(options) { }

        public DbSet<IpoRecord> Ipos { get; set; } = null!;
        public DbSet<DataSourceRecord> DataSources { get; set; } = null!;
        public DbSet<RawSourceResponseRecord> RawSourceResponses { get; set; } = null!;
        public DbSet<IPOExternalIdentifierRecord> ExternalIdentifiers { get; set; } = null!;
        public DbSet<IpoValidationErrorRecord> ValidationErrors { get; set; } = null!;
        public DbSet<GmpRecord> GmpHistory { get; set; } = null!;
        public DbSet<SubscriptionRecord> Subscriptions { get; set; } = null!;
        public DbSet<RegistrarRecord> Registrars { get; set; } = null!;
        public DbSet<AdminNoticeRecord> AdminNotices { get; set; } = null!;
        public DbSet<ExchangeConfigRecord> ExchangeConfigs { get; set; } = null!;
        public DbSet<NotificationRecord> Notifications { get; set; } = null!;
    }

    [Table("DataSources")]
    public class DataSourceRecord
    {
        [Key]
        [MaxLength(50)]
        public string DataSourceId { get; set; } = string.Empty;

        [Required]
        [MaxLength(100)]
        public string SourceName { get; set; } = string.Empty;

        [Required]
        [MaxLength(50)]
        public string SourceType { get; set; } = string.Empty; // EXCHANGE, GMP_AGGREGATOR, DIRECTORY, REGISTRAR

        [Required]
        [MaxLength(500)]
        public string BaseUrl { get; set; } = string.Empty;

        [Required]
        [MaxLength(500)]
        public string Endpoint { get; set; } = string.Empty;

        [MaxLength(50)]
        public string DataCategory { get; set; } = "ALL";

        [MaxLength(50)]
        public string MarketType { get; set; } = "ALL";

        public int Priority { get; set; } = 1;
        public bool IsActive { get; set; } = true;
        public bool RequiresAuthentication { get; set; } = false;

        [MaxLength(50)]
        public string? AuthType { get; set; } = "NONE";

        public int PollingIntervalMinutes { get; set; } = 15;
        public DateTimeOffset? LastSuccessfulFetch { get; set; }
        public DateTimeOffset? LastFailedFetch { get; set; }
        public int? LastHttpStatus { get; set; }
        public string? LastError { get; set; }
        public DateTimeOffset CreatedDate { get; set; } = DateTimeOffset.UtcNow;
        public DateTimeOffset ModifiedDate { get; set; } = DateTimeOffset.UtcNow;
    }

    [Table("RawSourceResponses")]
    public class RawSourceResponseRecord
    {
        [Key]
        public long RawSourceResponseId { get; set; }

        [Required]
        [MaxLength(50)]
        public string DataSourceId { get; set; } = string.Empty;

        [Required]
        [MaxLength(1000)]
        public string RequestUrl { get; set; } = string.Empty;

        public DateTimeOffset RequestTime { get; set; } = DateTimeOffset.UtcNow;
        public int HttpStatus { get; set; }
        public string? ResponseHeadersSummary { get; set; }
        public string? ResponseBody { get; set; }

        [MaxLength(128)]
        public string ContentHash { get; set; } = string.Empty;

        public DateTimeOffset FetchedAt { get; set; } = DateTimeOffset.UtcNow;
        public bool IsSuccess { get; set; }
        public string? ErrorMessage { get; set; }
    }

    [Table("Ipos")]
    public class IpoRecord
    {
        [Key]
        [MaxLength(100)]
        public string Id { get; set; } = string.Empty;

        [Required]
        [MaxLength(250)]
        public string Name { get; set; } = string.Empty;

        [MaxLength(250)]
        public string NormalizedName { get; set; } = string.Empty;

        [MaxLength(50)]
        public string? Symbol { get; set; }

        [MaxLength(20)]
        public string? Isin { get; set; }

        [MaxLength(50)]
        public string Exchange { get; set; } = "BSE, NSE";

        [MaxLength(20)]
        public string Type { get; set; } = "MAINBOARD"; // MAINBOARD / SME

        [MaxLength(30)]
        public string Status { get; set; } = "UPCOMING";

        public decimal PriceBandMin { get; set; }
        public decimal PriceBandMax { get; set; }
        public int LotSize { get; set; } = 1;
        public decimal IssueSizeCr { get; set; }
        public decimal FreshIssueCr { get; set; }
        public decimal OfsCr { get; set; }

        // Explicitly NULLABLE GMP
        public decimal? CurrentGmp { get; set; }
        public decimal? EstimatedListingPrice { get; set; }
        public decimal? EstimatedGainPercent { get; set; }

        [MaxLength(50)]
        public string? GmpSource { get; set; }
        public DateTimeOffset? GmpFetchedAt { get; set; }
        public int FireRating { get; set; } = 0;

        public DateTimeOffset? OpenDate { get; set; }
        public DateTimeOffset? CloseDate { get; set; }
        public DateTimeOffset? AllotmentDate { get; set; }
        public DateTimeOffset? ListingDate { get; set; }

        public decimal? ListingPrice { get; set; }
        public decimal? ListingGainPercent { get; set; }
        public decimal? CurrentMarketPrice { get; set; }
        public decimal? CurrentReturnPercent { get; set; }

        public string? RegistrarName { get; set; }
        public string? RegistrarUrl { get; set; }
        public bool IsAllotmentOut { get; set; }

        [MaxLength(30)]
        public string AllotmentStatus { get; set; } = "WAITING";

        [MaxLength(30)]
        public string ValidationStatus { get; set; } = "VALIDATED";

        public DateTimeOffset CreatedAt { get; set; } = DateTimeOffset.UtcNow;
        public DateTimeOffset UpdatedAt { get; set; } = DateTimeOffset.UtcNow;
    }

    [Table("IPOExternalIdentifiers")]
    public class IPOExternalIdentifierRecord
    {
        [Key]
        public long IPOExternalIdentifierId { get; set; }

        [Required]
        [MaxLength(100)]
        public string IPOId { get; set; } = string.Empty;

        [Required]
        [MaxLength(50)]
        public string DataSourceId { get; set; } = string.Empty;

        [Required]
        [MaxLength(150)]
        public string ExternalId { get; set; } = string.Empty;

        [MaxLength(50)]
        public string? Symbol { get; set; }

        [MaxLength(20)]
        public string? ISIN { get; set; }

        [MaxLength(250)]
        public string ExternalName { get; set; } = string.Empty;

        public DateTimeOffset? ExternalOpenDate { get; set; }
        public DateTimeOffset? ExternalCloseDate { get; set; }
        public DateTimeOffset LastSeenAt { get; set; } = DateTimeOffset.UtcNow;
    }

    [Table("IpoValidationErrors")]
    public class IpoValidationErrorRecord
    {
        [Key]
        public long ValidationErrorId { get; set; }

        public long? RawSourceResponseId { get; set; }

        [MaxLength(100)]
        public string? IPOId { get; set; }

        [MaxLength(100)]
        public string FieldName { get; set; } = string.Empty;

        public string? RawValue { get; set; }

        [MaxLength(200)]
        public string ValidationRule { get; set; } = string.Empty;

        [MaxLength(500)]
        public string ErrorMessage { get; set; } = string.Empty;

        public DateTimeOffset LoggedAt { get; set; } = DateTimeOffset.UtcNow;
    }

    [Table("GmpHistory")]
    public class GmpRecord
    {
        [Key]
        public long Id { get; set; }

        [Required]
        public string IpoId { get; set; } = string.Empty;

        public decimal Gmp { get; set; }
        public decimal GainPercent { get; set; }

        [MaxLength(50)]
        public string Source { get; set; } = "InvestorGain";

        public DateTimeOffset RecordedAt { get; set; } = DateTimeOffset.UtcNow;
    }

    [Table("Subscriptions")]
    public class SubscriptionRecord
    {
        [Key]
        public long Id { get; set; }

        [Required]
        public string IpoId { get; set; } = string.Empty;

        public decimal Qib { get; set; }
        public decimal Nii { get; set; }
        public decimal Retail { get; set; }
        public decimal Employee { get; set; }
        public decimal Total { get; set; }
        public long ApplicationsCount { get; set; }
        public DateTimeOffset LastUpdated { get; set; } = DateTimeOffset.UtcNow;
    }

    [Table("Registrars")]
    public class RegistrarRecord
    {
        [Key]
        [MaxLength(100)]
        public string Id { get; set; } = string.Empty;

        [Required]
        [MaxLength(250)]
        public string Name { get; set; } = string.Empty;

        [Required]
        [MaxLength(500)]
        public string Url { get; set; } = string.Empty;

        public int IssuesManaged { get; set; } = 0;
        public decimal IssueAmountCr { get; set; } = 0.0m;
        public DateTimeOffset CreatedDate { get; set; } = DateTimeOffset.UtcNow;
        public DateTimeOffset ModifyDate { get; set; } = DateTimeOffset.UtcNow;

        [MaxLength(100)]
        public string ModifyBy { get; set; } = "Admin";

        public string? Comment { get; set; }
        public bool IsActive { get; set; } = true;
    }

    [Table("AdminNotices")]
    public class AdminNoticeRecord
    {
        [Key]
        [MaxLength(100)]
        public string Id { get; set; } = string.Empty;

        [Required]
        [MaxLength(300)]
        public string Title { get; set; } = string.Empty;

        [Required]
        public string Message { get; set; } = string.Empty;

        [MaxLength(50)]
        public string Category { get; set; } = "ANNOUNCEMENT";

        public string? TargetIpoId { get; set; }
        public bool IsActive { get; set; } = true;
        public DateTimeOffset CreatedAt { get; set; } = DateTimeOffset.UtcNow;
    }

    [Table("ExchangeConfigs")]
    public class ExchangeConfigRecord
    {
        [Key]
        [MaxLength(50)]
        public string ExchangeKey { get; set; } = string.Empty;

        [Required]
        [MaxLength(500)]
        public string SourceUrl { get; set; } = string.Empty;

        [MaxLength(500)]
        public string DefaultUrl { get; set; } = string.Empty;

        public bool IsActive { get; set; } = true;
        public DateTimeOffset? LastSyncedAt { get; set; }
        public string? LastStatus { get; set; } = "IDLE";
        public DateTimeOffset UpdatedAt { get; set; } = DateTimeOffset.UtcNow;
    }

    [Table("Notifications")]
    public class NotificationRecord
    {
        [Key]
        public long NotificationId { get; set; }

        public string? UserId { get; set; }

        [Required]
        [MaxLength(100)]
        public string IPOId { get; set; } = string.Empty;

        [Required]
        [MaxLength(50)]
        public string Type { get; set; } = "ALLOTMENT_AVAILABLE";

        [Required]
        [MaxLength(300)]
        public string Title { get; set; } = string.Empty;

        [Required]
        [MaxLength(1000)]
        public string Message { get; set; } = string.Empty;

        [Required]
        [MaxLength(150)]
        public string EventKey { get; set; } = string.Empty;

        public bool IsRead { get; set; } = false;

        public DateTimeOffset CreatedAt { get; set; } = DateTimeOffset.UtcNow;
    }
}
