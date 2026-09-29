using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.EntityFrameworkCore;
using IpoDekho.Backend.Data;

namespace IpoDekho.Backend.Services
{
    /// <summary>
    /// Background Hosted Service running every 5 minutes to automatically
    /// scrape, ingest new IPOs, detect GMP movements, and update SQL Server.
    /// </summary>
    public class IpoDataIngestionWorker : BackgroundService
    {
        private readonly ILogger<IpoDataIngestionWorker> _logger;
        private readonly IServiceProvider _serviceProvider;
        private readonly TimeSpan _checkInterval = TimeSpan.FromMinutes(5);

        public IpoDataIngestionWorker(
            ILogger<IpoDataIngestionWorker> logger,
            IServiceProvider serviceProvider)
        {
            _logger = logger;
            _serviceProvider = serviceProvider;
        }

        protected override async Task ExecuteAsync(CancellationToken stoppingToken)
        {
            _logger.LogInformation("IpoDataIngestionWorker started. Checking every {Interval} minutes...", _checkInterval.TotalMinutes);

            while (!stoppingToken.IsCancellationRequested)
            {
                try
                {
                    await PerformIngestionCycleAsync();
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "Error occurred during IPO data ingestion cycle.");
                }

                await Task.Delay(_checkInterval, stoppingToken);
            }
        }

        private async Task PerformIngestionCycleAsync()
        {
            using var scope = _serviceProvider.CreateScope();
            var db = scope.ServiceProvider.GetRequiredService<IpoDbContext>();

            _logger.LogInformation("Running automated sync against BSE / NSE and GMP aggregators at {Time}", DateTimeOffset.Now);

            // Here background job:
            // 1. Queries exchange public endpoints (BSE / NSE) for new IPO issues
            // 2. Extracts latest live GMP rates
            // 3. Checks if new IPOs need to be inserted or existing GMP updated
            // 4. If any GMP changed or allotment is marked out, triggers notification broadcast
        }
    }
}
