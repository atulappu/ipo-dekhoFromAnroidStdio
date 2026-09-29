using Microsoft.EntityFrameworkCore;
using IpoDekho.Backend.Models.Entities;

namespace IpoDekho.Backend.Data;

public class AppDbContext : DbContext
{
    public AppDbContext(DbContextOptions<AppDbContext> options) : base(options)
    {
    }

    public DbSet<IpoEntity> Ipos => Set<IpoEntity>();
    public DbSet<GmpHistoryEntity> GmpHistories => Set<GmpHistoryEntity>();
    public DbSet<SubscriptionEntity> Subscriptions => Set<SubscriptionEntity>();
    public DbSet<MarketIndexEntity> MarketIndices => Set<MarketIndexEntity>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        modelBuilder.Entity<IpoEntity>(entity =>
        {
            entity.HasIndex(e => e.Status);
            entity.HasIndex(e => e.Category);
            entity.HasIndex(e => e.OpenDate);
            entity.HasIndex(e => e.Symbol);

            entity.HasMany(e => e.GmpHistories)
                  .WithOne(e => e.Ipo)
                  .HasForeignKey(e => e.IpoId)
                  .OnDelete(DeleteBehavior.Cascade);

            entity.HasMany(e => e.Subscriptions)
                  .WithOne(e => e.Ipo)
                  .HasForeignKey(e => e.IpoId)
                  .OnDelete(DeleteBehavior.Cascade);
        });

        modelBuilder.Entity<GmpHistoryEntity>(entity =>
        {
            entity.HasIndex(e => e.IpoId);
            entity.HasIndex(e => e.Date);
        });

        modelBuilder.Entity<SubscriptionEntity>(entity =>
        {
            entity.HasIndex(e => e.IpoId);
        });
    }
}
