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

        public static string GetListingStatus(IpoRecord ipo)
        {
            if (ipo.Status == "LISTED" || (ipo.ListingPrice.HasValue && ipo.ListingPrice.Value > 0) || (ipo.CurrentMarketPrice.HasValue && ipo.CurrentMarketPrice.Value > 0))
            {
                return "LISTED";
            }
            return "NOT_LISTED";
        }

        public static bool GetWatchLiveAvailability(IpoRecord ipo)
        {
            return GetListingStatus(ipo) == "LISTED";
        }

        public static string GetIPOStatus(IpoRecord ipo, DateTimeOffset? checkTime = null)
        {
            if (GetListingStatus(ipo) == "LISTED")
            {
                return "LISTED";
            }

            var baseStatus = CalculateStatus(ipo.OpenDate, ipo.CloseDate, checkTime);
            if (baseStatus == "CLOSED")
            {
                var allotment = GetAllotmentStatus(ipo, checkTime);
                if (allotment == "AVAILABLE")
                {
                    return "ALLOTMENT_AVAILABLE";
                }
            }

            return baseStatus;
        }

        /// <summary>
        /// Centralized Allotment Availability Rule:
        /// Possible values:
        /// - NOT_AVAILABLE: IPO is not yet closed (UPCOMING or OPEN)
        /// - WAITING: IPO is closed, but allotment is not yet available/declared
        /// - AVAILABLE: IPO is closed, and validated allotment record or declaration exists
        /// - DATA_ERROR: Invalid dates (e.g. OpenDate > CloseDate)
        /// - SOURCE_UNAVAILABLE: Configured source was unreachable or returned an error
        ///
        /// CRITICAL: Never infer AVAILABLE from allotment date alone!
        /// </summary>
        public static string GetAllotmentStatus(IpoRecord ipo, DateTimeOffset? checkTime = null)
        {
            if (ipo.OpenDate.HasValue && ipo.CloseDate.HasValue && ipo.OpenDate.Value > ipo.CloseDate.Value)
            {
                return "DATA_ERROR";
            }

            var baseStatus = CalculateStatus(ipo.OpenDate, ipo.CloseDate, checkTime);
            if (baseStatus == "UPCOMING" || baseStatus == "OPEN" || baseStatus == "NOT_AVAILABLE")
            {
                return "NOT_AVAILABLE";
            }

            if (ipo.ValidationStatus == "SOURCE_UNAVAILABLE")
            {
                return "SOURCE_UNAVAILABLE";
            }

            // If explicitly marked allotment out in SQL from verified source
            if (ipo.IsAllotmentOut || ipo.AllotmentStatus == "AVAILABLE")
            {
                return "AVAILABLE";
            }

            // CRITICAL: Never infer allotment availability from AllotmentDate alone!
            return "WAITING";
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
