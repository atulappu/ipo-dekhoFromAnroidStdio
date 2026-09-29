using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace IpoDekho.Backend.Models.Entities;

[Table("GmpHistories")]
public class GmpHistoryEntity
{
    [Key]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public long Id { get; set; }

    [Required]
    [MaxLength(64)]
    public string IpoId { get; set; } = string.Empty;

    [Required]
    [MaxLength(32)]
    public string Date { get; set; } = string.Empty;

    [Column(TypeName = "decimal(18,2)")]
    public decimal GmpAmount { get; set; }

    [Column(TypeName = "decimal(18,2)")]
    public decimal? EstimatedPrice { get; set; }

    [Column(TypeName = "decimal(18,2)")]
    public decimal? EstimatedGainPercent { get; set; }

    [MaxLength(32)]
    public string? Rating { get; set; } = "Neutral";

    public DateTime RecordedAt { get; set; } = DateTime.UtcNow;

    [ForeignKey(nameof(IpoId))]
    public IpoEntity? Ipo { get; set; }
}

[Table("Subscriptions")]
public class SubscriptionEntity
{
    [Key]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public long Id { get; set; }

    [Required]
    [MaxLength(64)]
    public string IpoId { get; set; } = string.Empty;

    [Required]
    [MaxLength(32)]
    public string DayLabel { get; set; } = string.Empty;

    [Required]
    [MaxLength(32)]
    public string Date { get; set; } = string.Empty;

    [Column(TypeName = "decimal(18,2)")]
    public decimal QibTimes { get; set; }

    [Column(TypeName = "decimal(18,2)")]
    public decimal NiiTimes { get; set; }

    [Column(TypeName = "decimal(18,2)")]
    public decimal RetailTimes { get; set; }

    [Column(TypeName = "decimal(18,2)")]
    public decimal TotalTimes { get; set; }

    public DateTime RecordedAt { get; set; } = DateTime.UtcNow;

    [ForeignKey(nameof(IpoId))]
    public IpoEntity? Ipo { get; set; }
}

[Table("MarketIndices")]
public class MarketIndexEntity
{
    [Key]
    [MaxLength(32)]
    public string Symbol { get; set; } = string.Empty;

    [Required]
    [MaxLength(64)]
    public string Name { get; set; } = string.Empty;

    [Column(TypeName = "decimal(18,2)")]
    public decimal CurrentValue { get; set; }

    [Column(TypeName = "decimal(18,2)")]
    public decimal ChangeValue { get; set; }

    [Column(TypeName = "decimal(18,2)")]
    public decimal ChangePercent { get; set; }

    public DateTime LastUpdated { get; set; } = DateTime.UtcNow;
}
