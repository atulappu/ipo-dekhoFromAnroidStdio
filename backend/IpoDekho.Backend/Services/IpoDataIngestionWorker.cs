using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.DependencyInjection;

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
            _logger.LogInformation("IpoDataIngestionWorker started. Running automated cycle every {Interval} minutes...", _checkInterval.TotalMinutes);

            while (!stoppingToken.IsCancellationRequested)
            {
                try
                {
                    using var scope = _serviceProvider.CreateScope();
                    var scraper = scope.ServiceProvider.GetRequiredService<IIpoScraperService>();
                    await scraper.SyncIpoDataAsync();
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "Error occurred during IPO data ingestion cycle.");
                }

                await Task.Delay(_checkInterval, stoppingToken);
            }
        }
    }
}
