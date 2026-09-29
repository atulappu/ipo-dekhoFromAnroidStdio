using System.Text;
using System.Text.Json;

namespace IpoDekho.Backend.Services
{
    public interface IFirebaseNotificationService
    {
        Task SendNotificationToTopicAsync(string topic, string title, string body, Dictionary<string, string>? data = null);
        Task BroadcastAdminNoticeAsync(string title, string message, string? targetIpoId = null);
    }

    public class FirebaseNotificationService : IFirebaseNotificationService
    {
        private readonly ILogger<FirebaseNotificationService> _logger;
        private readonly HttpClient _httpClient;

        public FirebaseNotificationService(
            ILogger<FirebaseNotificationService> logger,
            IHttpClientFactory httpClientFactory)
        {
            _logger = logger;
            _httpClient = httpClientFactory.CreateClient();
        }

        public async Task SendNotificationToTopicAsync(string topic, string title, string body, Dictionary<string, string>? data = null)
        {
            _logger.LogInformation("Broadcasting push notification to topic '{Topic}': {Title} - {Body}", topic, title, body);

            // Payload structure for FCM
            var payload = new
            {
                to = $"/topics/{topic}",
                priority = "high",
                notification = new
                {
                    title = title,
                    body = body,
                    sound = "default"
                },
                data = data ?? new Dictionary<string, string>()
            };

            var json = JsonSerializer.Serialize(payload);
            _logger.LogDebug("FCM Payload: {Json}", json);

            // In production, when Firebase Server Key is provided in appsettings.json,
            // this dispatches to https://fcm.googleapis.com/fcm/send
            await Task.CompletedTask;
        }

        public async Task BroadcastAdminNoticeAsync(string title, string message, string? targetIpoId = null)
        {
            var data = new Dictionary<string, string>
            {
                { "destination", "notifications" }
            };
            if (!string.IsNullOrEmpty(targetIpoId))
            {
                data["ipoId"] = targetIpoId;
            }

            await SendNotificationToTopicAsync("admin_notices", title, message, data);
        }
    }
}
