using IpoDekho.Backend.Data;

namespace IpoDekho.Backend.Services
{
    /// <summary>
    /// Centralized IPO Status Calculator using Indian Standard Time (IST / Asia/Kolkata).
    /// Enforces business rules:
    /// - OPEN condition: CurrentDateTime >= OpenDate 00:00:00 IST AND CurrentDateTime < CloseDate 17:30:00 IST
    /// - CLOSED condition: CurrentDateTime >= CloseDate 17:30:00 IST
    /// - UPCOMING condition: CurrentDateTime < OpenDate 00:00:00 IST
    /// </summary>
    public static class IpoStatusCalculator
    {
        public static readonly TimeZoneInfo IstZone = ResolveIstTimeZone();

        private static TimeZoneInfo ResolveIstTimeZone()
        {
            try
            {
                return TimeZoneInfo.FindSystemTimeZoneById("Asia/Kolkata");
            }
            catch
            {
                try
                {
                    return TimeZoneInfo.FindSystemTimeZoneById("India Standard Time");
                }
                catch
                {
                    return TimeZoneInfo.CreateCustomTimeZone("IST", TimeSpan.FromMinutes(330), "India Standard Time", "IST");
                }
            }
        }

        public static string GetIPOStatus(IpoRecord ipo, DateTimeOffset? checkTime = null)
        {
            return CalculateStatus(ipo.OpenDate, ipo.CloseDate, checkTime);
        }

        public static string CalculateStatus(DateTimeOffset? openDate, DateTimeOffset? closeDate, DateTimeOffset? checkTime = null)
        {
            if (!openDate.HasValue || !closeDate.HasValue)
            {
                return "NOT_AVAILABLE";
            }

            var now = checkTime ?? DateTimeOffset.UtcNow;
            var nowIst = TimeZoneInfo.ConvertTime(now, IstZone);

            var openDateIst = TimeZoneInfo.ConvertTime(openDate.Value, IstZone).Date;
            var closeDateIst = TimeZoneInfo.ConvertTime(closeDate.Value, IstZone).Date;

            // 00:00:00 IST on Open Date
            var openBound = new DateTimeOffset(openDateIst.Year, openDateIst.Month, openDateIst.Day, 0, 0, 0, IstZone.BaseUtcOffset);

            // 17:30:00 IST on Close Date
            var closeBound = new DateTimeOffset(closeDateIst.Year, closeDateIst.Month, closeDateIst.Day, 17, 30, 0, IstZone.BaseUtcOffset);

            if (nowIst < openBound)
            {
                return "UPCOMING";
            }
            else if (nowIst < closeBound)
            {
                return "OPEN";
            }
            else
            {
                return "CLOSED";
            }
        }
    }
}
