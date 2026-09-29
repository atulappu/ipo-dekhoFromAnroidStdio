using Microsoft.EntityFrameworkCore;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace IpoDekho.Backend.Data
{
    public class IpoDbContext : DbContext
    {
        public IpoDbContext(DbContextOptions<IpoDbContext> options) : base(options) { }

        public DbSet<IpoRecord> Ipos { get; set; } = null!;
        public DbSet<GmpRecord> GmpHistory { get; set; } = null!;
        public DbSet<SubscriptionRecord> Subscriptions { get; set; } = null!;
        public DbSet<AdminNoticeRecord> AdminNotices { get; set; } = null!;
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

        [MaxLength(50)]
        public string? Symbol { get; set; }

        [MaxLength(20)]
        public string Type { get; set; } = "MAINBOARD"; // MAINBOARD / SME

        [MaxLength(30)]
        public string Status { get; set; } = "UPCOMING";

        public decimal PriceBandMin { get; set; }
        public decimal PriceBandMax { get; set; }
        public int LotSize { get; set; } = 1;
        public decimal IssueSizeCr { get; set; }
        public decimal CurrentGmp { get; set; }

        public DateTimeOffset? OpenDate { get; set; }
        public DateTimeOffset? CloseDate { get; set; }
        public DateTimeOffset? AllotmentDate { get; set; }
        public DateTimeOffset? ListingDate { get; set; }

        public string? RegistrarName { get; set; }
        public string? RegistrarUrl { get; set; }
        public bool IsAllotmentOut { get; set; }

        public DateTimeOffset CreatedAt { get; set; } = DateTimeOffset.UtcNow;
        public DateTimeOffset UpdatedAt { get; set; } = DateTimeOffset.UtcNow;
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
}
