package com.example.ipotracker.utils

import android.util.Log
import com.example.ipotracker.data.model.IpoItem
import com.example.ipotracker.data.model.IpoStatus
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Centralized Date & Timezone Utility.
 * Enforces Indian Standard Time (IST / Asia/Kolkata) across all IPO lifecycle status calculations.
 */
object DateUtils {

    private const val TAG = "DateUtils"

    // Authoritative TimeZone: Indian Standard Time (IST / UTC+05:30)
    val IST_TIME_ZONE: TimeZone = TimeZone.getTimeZone("Asia/Kolkata")

    private val supportedDatePatterns = listOf(
        "yyyy-MM-dd",
        "dd-MMM-yyyy",
        "dd MMM yyyy",
        "d-MMM-yyyy",
        "d MMM yyyy",
        "dd/MM/yyyy",
        "d/M/yyyy",
        "dd-MM-yyyy",
        "d-M-yyyy",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ssZ"
    )

    private val supportedDatePatternsWithoutYear = listOf(
        "dd-MMM",
        "d-MMM",
        "dd MMM",
        "d MMM",
        "dd/MM",
        "d/M",
        "dd-MM",
        "d-M"
    )

    private val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).apply {
        timeZone = IST_TIME_ZONE
    }

    private val timestampFormat = SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.ENGLISH).apply {
        timeZone = IST_TIME_ZONE
    }

    private val parseFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).apply {
        timeZone = IST_TIME_ZONE
    }

    fun formatDisplayDate(dateStr: String): String {
        return try {
            val cal = parseToIstCalendar(dateStr) ?: return dateStr
            displayFormat.format(cal.time)
        } catch (e: Exception) {
            dateStr
        }
    }

    /**
     * Formats date range as "28 Sep - 30 Sep 2026"
     */
    fun formatIpoDateRange(openDateStr: String, closeDateStr: String): String {
        return try {
            val d1 = parseToIstCalendar(openDateStr)
            val d2 = parseToIstCalendar(closeDateStr)
            if (d1 != null && d2 != null) {
                val f = SimpleDateFormat("dd MMM", Locale.ENGLISH).apply { timeZone = IST_TIME_ZONE }
                val y = SimpleDateFormat("yyyy", Locale.ENGLISH).apply { timeZone = IST_TIME_ZONE }
                "${f.format(d1.time)} - ${f.format(d2.time)} ${y.format(d2.time)}"
            } else {
                "${formatDisplayDate(openDateStr)} - ${formatDisplayDate(closeDateStr)}"
            }
        } catch (e: Exception) {
            "${formatDisplayDate(openDateStr)} - ${formatDisplayDate(closeDateStr)}"
        }
    }

    fun formatLastUpdated(timestamp: Long = System.currentTimeMillis()): String {
        return timestampFormat.format(Date(timestamp))
    }

    fun formatDisplayDateTime(timestamp: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss 'IST'", Locale.ENGLISH)
        sdf.timeZone = IST_TIME_ZONE
        return sdf.format(Date(timestamp))
    }

    /**
     * Parses a date string into an IST Calendar object across multiple standard formats.
     * Sanitizes inputs by stripping HTML and extracting the clean date token.
     */
    fun parseToIstCalendar(dateStr: String?, defaultYear: Int = 2026): Calendar? {
        if (dateStr.isNullOrBlank() || dateStr == "-" || dateStr == "--") return null
        // Strip HTML tags and isolate date pattern
        val noHtml = dateStr.replace(Regex("<[^>]*>"), " ").trim()
        val dateMatch = Regex("\\d{1,4}[-/][0-9a-zA-Z]{1,4}(?:[-/]\\d{2,4})?(?:T\\d{2}:\\d{2}:\\d{2}(?:[+-]\\d{2}:\\d{2})?)?").find(noHtml)
        val clean = dateMatch?.value ?: noHtml.split(Regex("\\s+")).firstOrNull() ?: noHtml

        // 1. Try patterns with year
        for (pattern in supportedDatePatterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.ENGLISH).apply {
                    timeZone = IST_TIME_ZONE
                    isLenient = false
                }
                val parsed = sdf.parse(clean)
                if (parsed != null) {
                    val cal = Calendar.getInstance(IST_TIME_ZONE)
                    cal.time = parsed
                    return cal
                }
            } catch (_: Exception) {}
        }

        // 2. Try patterns without year (assign defaultYear, e.g. 2026)
        for (pattern in supportedDatePatternsWithoutYear) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.ENGLISH).apply {
                    timeZone = IST_TIME_ZONE
                    isLenient = false
                }
                val parsed = sdf.parse(clean)
                if (parsed != null) {
                    val cal = Calendar.getInstance(IST_TIME_ZONE)
                    cal.time = parsed
                    cal.set(Calendar.YEAR, defaultYear)
                    return cal
                }
            } catch (_: Exception) {}
        }

        return null
    }

    /**
     * Calculates countdown string from closing date in IST.
     */
    fun getCountdownString(closingDateStr: String): String {
        return try {
            val closeCal = parseToIstCalendar(closingDateStr) ?: return "Check schedule"
            // Default market close time: 17:30 IST on close date
            closeCal.set(Calendar.HOUR_OF_DAY, 17)
            closeCal.set(Calendar.MINUTE, 30)
            closeCal.set(Calendar.SECOND, 0)
            closeCal.set(Calendar.MILLISECOND, 0)

            val diff = closeCal.timeInMillis - System.currentTimeMillis()
            if (diff <= 0) {
                return "Closed"
            }

            val days = TimeUnit.MILLISECONDS.toDays(diff)
            val hours = TimeUnit.MILLISECONDS.toHours(diff) % 24
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60

            when {
                days > 0 -> "$days Days $hours Hours"
                hours > 0 -> "$hours Hours $minutes Mins"
                else -> "$minutes Mins"
            }
        } catch (e: Exception) {
            "Open"
        }
    }

    /**
     * Centralized IPO Status Calculator.
     * Enforces the business rules using Indian Standard Time (IST):
     *
     * 1. OPEN Condition:
     *    CurrentDateTime >= OpenDate 00:00:00 IST AND CurrentDateTime < CloseDate 17:30:00 IST
     *
     * 2. CLOSED Condition:
     *    CurrentDateTime >= CloseDate 17:30:00 IST
     *
     * 3. UPCOMING Condition:
     *    CurrentDateTime < OpenDate 00:00:00 IST
     *
     * 4. Missing Dates:
     *    Returns "DATA_ERROR" / "NOT_AVAILABLE" without guessing or fabricating dates.
     */
    fun getIPOStatus(
        openDateStr: String?,
        closeDateStr: String?,
        checkInstantMillis: Long = System.currentTimeMillis()
    ): String {
        return calculateIpoStatus(openDateStr, closeDateStr, checkInstantMillis).name
    }

    fun getIPOStatus(
        ipo: IpoItem,
        checkInstantMillis: Long = System.currentTimeMillis()
    ): String {
        if (isWatchLiveAvailable(ipo)) return "LISTED"
        val allot = getAllotmentStatus(ipo, checkInstantMillis)
        if (allot == "AVAILABLE") return "ALLOTMENT_AVAILABLE"
        val base = calculateIpoStatus(ipo.openDate, ipo.closeDate, checkInstantMillis)
        return when (base) {
            IpoStatus.OPEN -> "OPEN"
            IpoStatus.UPCOMING -> "UPCOMING"
            IpoStatus.CLOSED -> "CLOSED"
            IpoStatus.NOT_AVAILABLE -> "NOT_AVAILABLE"
            IpoStatus.DATA_ERROR -> "DATA_ERROR"
            else -> ipo.status.name
        }
    }

    /**
     * Core status calculation returning type-safe IpoStatus enum.
     */
    fun calculateIpoStatus(
        openDateStr: String?,
        closeDateStr: String?,
        checkInstantMillis: Long = System.currentTimeMillis()
    ): IpoStatus {
        if (openDateStr.isNullOrBlank() || closeDateStr.isNullOrBlank() ||
            openDateStr == "-" || closeDateStr == "--" || closeDateStr == "-") {
            try { Log.w(TAG, "Validation Warning: Missing openDate or closeDate. Marking as NOT_AVAILABLE.") } catch (_: Throwable) {}
            return IpoStatus.NOT_AVAILABLE
        }

        val openCal = parseToIstCalendar(openDateStr)
        val closeCal = parseToIstCalendar(closeDateStr)

        if (openCal == null || closeCal == null) {
            try { Log.w(TAG, "Validation Error: Could not parse dates: open='$openDateStr', close='$closeDateStr'") } catch (_: Throwable) {}
            return IpoStatus.DATA_ERROR
        }

        // Set Open Bound: OpenDate 00:00:00.000 IST
        openCal.set(Calendar.HOUR_OF_DAY, 0)
        openCal.set(Calendar.MINUTE, 0)
        openCal.set(Calendar.SECOND, 0)
        openCal.set(Calendar.MILLISECOND, 0)
        val openMillis = openCal.timeInMillis

        // Set Close Bound: CloseDate 17:30:00.000 IST
        closeCal.set(Calendar.HOUR_OF_DAY, 17)
        closeCal.set(Calendar.MINUTE, 30)
        closeCal.set(Calendar.SECOND, 0)
        closeCal.set(Calendar.MILLISECOND, 0)
        val closeMillis = closeCal.timeInMillis

        return when {
            checkInstantMillis < openMillis -> IpoStatus.UPCOMING
            checkInstantMillis < closeMillis -> IpoStatus.OPEN
            else -> IpoStatus.CLOSED
        }
    }

    /**
     * Checks if a given date string is in the future in IST.
     */
    fun isDateInFuture(dateStr: String?, checkInstantMillis: Long = System.currentTimeMillis()): Boolean {
        if (dateStr.isNullOrBlank()) return false
        val cal = parseToIstCalendar(dateStr) ?: return false
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis > checkInstantMillis
    }

    /**
     * Checks if a given date string is in the past in IST.
     */
    fun isDateInPast(dateStr: String?, checkInstantMillis: Long = System.currentTimeMillis()): Boolean {
        if (dateStr.isNullOrBlank()) return false
        val cal = parseToIstCalendar(dateStr) ?: return false
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis < checkInstantMillis
    }

    /**
     * Effective status calculation that accounts for post-close stages (Allotment & Listing)
     * while strictly maintaining the 17:30:00 IST open/closed threshold.
     * Prevents historical or closed IPOs from ever appearing as UPCOMING.
     */
    /**
     * Calculates the effective domain status of an IPO dynamically.
     * Business rules:
     * - A validated LISTED status from configured source is strictly LISTED.
     * - A Close Date in the past cannot be UPCOMING or OPEN.
     * - For closed IPOs, ONLY validated allotment results can transition to ALLOTMENT_AVAILABLE.
     *   Never infer allotment availability from allotment date alone.
     */
    fun calculateEffectiveStatus(
        openDateStr: String?,
        closeDateStr: String?,
        allotmentDateStr: String? = null,
        listingDateStr: String? = null,
        fallbackStatus: IpoStatus = IpoStatus.UPCOMING,
        isAllotmentAvailable: Boolean = false,
        isListed: Boolean = false,
        checkInstantMillis: Long = System.currentTimeMillis()
    ): IpoStatus {
        // Rule 1: An already verified listed IPO from data source is LISTED
        if (isListed || fallbackStatus == IpoStatus.LISTED) {
            return IpoStatus.LISTED
        }

        // Rule 2: If Close Date is in the past, it CANNOT be UPCOMING or OPEN
        if (!closeDateStr.isNullOrBlank()) {
            val closeCal = parseToIstCalendar(closeDateStr)
            if (closeCal != null) {
                closeCal.set(Calendar.HOUR_OF_DAY, 17)
                closeCal.set(Calendar.MINUTE, 30)
                closeCal.set(Calendar.SECOND, 0)
                closeCal.set(Calendar.MILLISECOND, 0)
                if (checkInstantMillis >= closeCal.timeInMillis) {
                    if (isAllotmentAvailable || fallbackStatus == IpoStatus.ALLOTMENT_AVAILABLE) {
                        return IpoStatus.ALLOTMENT_AVAILABLE
                    }
                    return IpoStatus.CLOSED
                }
            }
        }

        val baseStatus = calculateIpoStatus(openDateStr, closeDateStr, checkInstantMillis)

        if (baseStatus == IpoStatus.NOT_AVAILABLE || baseStatus == IpoStatus.DATA_ERROR) {
            return if (fallbackStatus == IpoStatus.UPCOMING) IpoStatus.NOT_AVAILABLE else fallbackStatus
        }

        return baseStatus
    }

    /**
     * Centralized Allotment Availability Rule:
     * Possible values:
     * - NOT_AVAILABLE: IPO is not yet closed (UPCOMING or OPEN)
     * - WAITING: IPO is closed, but allotment is not yet available/declared
     * - AVAILABLE: IPO is closed, and validated allotment record exists from configured source
     * - DATA_ERROR: Invalid dates (e.g. OpenDate > CloseDate)
     * - SOURCE_UNAVAILABLE: Configured source was unreachable or returned an error
     *
     * CRITICAL: Never infer AVAILABLE from allotment date alone!
     */
    fun getAllotmentStatus(
        openDateStr: String?,
        closeDateStr: String?,
        allotmentDateStr: String? = null,
        isExplicitlyAvailable: Boolean = false,
        sourceFailed: Boolean = false,
        checkInstantMillis: Long = System.currentTimeMillis()
    ): String {
        if (sourceFailed) return "SOURCE_UNAVAILABLE"

        val openCal = parseToIstCalendar(openDateStr)
        val closeCal = parseToIstCalendar(closeDateStr)

        if (openCal != null && closeCal != null && openCal.timeInMillis > closeCal.timeInMillis) {
            return "DATA_ERROR"
        }

        val baseStatus = calculateIpoStatus(openDateStr, closeDateStr, checkInstantMillis)
        if (baseStatus == IpoStatus.UPCOMING || baseStatus == IpoStatus.OPEN || baseStatus == IpoStatus.NOT_AVAILABLE) {
            return "NOT_AVAILABLE"
        }
        if (baseStatus == IpoStatus.DATA_ERROR) {
            return "DATA_ERROR"
        }

        // Only return AVAILABLE if actual validated allotment result exists!
        if (isExplicitlyAvailable) {
            return "AVAILABLE"
        }

        return "WAITING"
    }

    fun getAllotmentStatus(
        ipo: IpoItem,
        checkInstantMillis: Long = System.currentTimeMillis()
    ): String {
        val isExplicitlyAvailable = ipo.allotmentStatus == com.example.ipotracker.data.model.AllotmentStatus.AVAILABLE ||
                ipo.status == IpoStatus.ALLOTMENT_AVAILABLE ||
                ipo.allotmentInfo?.isAvailable == true

        val sourceFailed = ipo.sourceId.equals("SOURCE_UNAVAILABLE", ignoreCase = true) || 
                ipo.sourceUrl.equals("SOURCE_UNAVAILABLE", ignoreCase = true)

        return getAllotmentStatus(
            openDateStr = ipo.openDate,
            closeDateStr = ipo.closeDate,
            allotmentDateStr = ipo.allotmentDate.ifBlank { ipo.allotmentInfo?.allotmentDate },
            isExplicitlyAvailable = isExplicitlyAvailable,
            sourceFailed = sourceFailed,
            checkInstantMillis = checkInstantMillis
        )
    }

    fun isAllotmentAvailable(
        ipo: IpoItem,
        checkInstantMillis: Long = System.currentTimeMillis()
    ): Boolean {
        return getAllotmentStatus(ipo, checkInstantMillis) == "AVAILABLE"
    }

    fun getListingStatus(
        listingStatus: com.example.ipotracker.data.model.ListingStatus,
        status: IpoStatus = IpoStatus.UPCOMING
    ): String {
        if (listingStatus == com.example.ipotracker.data.model.ListingStatus.LISTED || status == IpoStatus.LISTED) {
            return "LISTED"
        }
        return "NOT_LISTED"
    }

    fun isWatchLiveAvailable(
        listingStatus: com.example.ipotracker.data.model.ListingStatus,
        status: IpoStatus = IpoStatus.UPCOMING
    ): Boolean {
        return getListingStatus(listingStatus, status) == "LISTED"
    }

    fun isWatchLiveAvailable(ipo: IpoItem): Boolean {
        return isWatchLiveAvailable(ipo.listingStatus, ipo.status)
    }
}
