package com.example.ipotracker

import com.example.ipotracker.data.model.IpoStatus
import com.example.ipotracker.utils.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

/**
 * Automated Unit Tests for IPODekho Open/Closed Tab Dynamic Status Logic.
 *
 * Verifies business rule:
 * Open Date: 01-Sep-2026
 * Close Date: 03-Sep-2026
 * Timezone: Asia/Kolkata (Indian Standard Time - IST)
 *
 * Required test cases:
 * 1. 31-Aug-2026 23:59:59 IST → UPCOMING
 * 2. 01-Sep-2026 00:00:00 IST → OPEN
 * 3. 02-Sep-2026 12:00:00 IST → OPEN
 * 4. 03-Sep-2026 17:29:59 IST → OPEN
 * 5. 03-Sep-2026 17:30:00 IST → CLOSED
 * 6. 03-Sep-2026 18:00:00 IST → CLOSED
 * 7. 04-Sep-2026 00:00:00 IST → CLOSED
 */
class IpoOpenClosedStatusTest {

    private val istZone = TimeZone.getTimeZone("Asia/Kolkata")
    private val openDateStr = "2026-09-01"
    private val closeDateStr = "2026-09-03"

    private fun getIstMillis(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int): Long {
        val cal = Calendar.getInstance(istZone)
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        cal.set(Calendar.DAY_OF_MONTH, day)
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, second)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    @Test
    fun test1_31Aug2026_23_59_59_IST_is_UPCOMING() {
        val time = getIstMillis(2026, 8, 31, 23, 59, 59)
        val status = DateUtils.calculateIpoStatus(openDateStr, closeDateStr, time)
        assertEquals(IpoStatus.UPCOMING, status)
        assertEquals("UPCOMING", DateUtils.getIPOStatus(openDateStr, closeDateStr, time))
    }

    @Test
    fun test2_01Sep2026_00_00_00_IST_is_OPEN() {
        val time = getIstMillis(2026, 9, 1, 0, 0, 0)
        val status = DateUtils.calculateIpoStatus(openDateStr, closeDateStr, time)
        assertEquals(IpoStatus.OPEN, status)
        assertEquals("OPEN", DateUtils.getIPOStatus(openDateStr, closeDateStr, time))
    }

    @Test
    fun test3_02Sep2026_12_00_00_IST_is_OPEN() {
        val time = getIstMillis(2026, 9, 2, 12, 0, 0)
        val status = DateUtils.calculateIpoStatus(openDateStr, closeDateStr, time)
        assertEquals(IpoStatus.OPEN, status)
        assertEquals("OPEN", DateUtils.getIPOStatus(openDateStr, closeDateStr, time))
    }

    @Test
    fun test4_03Sep2026_17_29_59_IST_is_OPEN() {
        val time = getIstMillis(2026, 9, 3, 17, 29, 59)
        val status = DateUtils.calculateIpoStatus(openDateStr, closeDateStr, time)
        assertEquals(IpoStatus.OPEN, status)
        assertEquals("OPEN", DateUtils.getIPOStatus(openDateStr, closeDateStr, time))
    }

    @Test
    fun test5_03Sep2026_17_30_00_IST_is_CLOSED() {
        val time = getIstMillis(2026, 9, 3, 17, 30, 0)
        val status = DateUtils.calculateIpoStatus(openDateStr, closeDateStr, time)
        assertEquals(IpoStatus.CLOSED, status)
        assertEquals("CLOSED", DateUtils.getIPOStatus(openDateStr, closeDateStr, time))
    }

    @Test
    fun test6_03Sep2026_18_00_00_IST_is_CLOSED() {
        val time = getIstMillis(2026, 9, 3, 18, 0, 0)
        val status = DateUtils.calculateIpoStatus(openDateStr, closeDateStr, time)
        assertEquals(IpoStatus.CLOSED, status)
        assertEquals("CLOSED", DateUtils.getIPOStatus(openDateStr, closeDateStr, time))
    }

    @Test
    fun test7_04Sep2026_00_00_00_IST_is_CLOSED() {
        val time = getIstMillis(2026, 9, 4, 0, 0, 0)
        val status = DateUtils.calculateIpoStatus(openDateStr, closeDateStr, time)
        assertEquals(IpoStatus.CLOSED, status)
        assertEquals("CLOSED", DateUtils.getIPOStatus(openDateStr, closeDateStr, time))
    }

    @Test
    fun testEdgeCaseE_missing_or_blank_dates_returns_NOT_AVAILABLE() {
        val time = getIstMillis(2026, 9, 2, 12, 0, 0)
        assertEquals(IpoStatus.NOT_AVAILABLE, DateUtils.calculateIpoStatus(null, "2026-09-03", time))
        assertEquals(IpoStatus.NOT_AVAILABLE, DateUtils.calculateIpoStatus("2026-09-01", null, time))
        assertEquals(IpoStatus.NOT_AVAILABLE, DateUtils.calculateIpoStatus("", "", time))
        assertEquals(IpoStatus.NOT_AVAILABLE, DateUtils.calculateIpoStatus("-", "-", time))
    }

    @Test
    fun testAlternativeDateFormat_ddMmmYyyy() {
        // Exchange standard format: "01-Sep-2026" to "03-Sep-2026"
        val altOpen = "01-Sep-2026"
        val altClose = "03-Sep-2026"

        val openTime = getIstMillis(2026, 9, 1, 10, 0, 0)
        assertEquals(IpoStatus.OPEN, DateUtils.calculateIpoStatus(altOpen, altClose, openTime))

        val closeTime = getIstMillis(2026, 9, 3, 17, 30, 0)
        assertEquals(IpoStatus.CLOSED, DateUtils.calculateIpoStatus(altOpen, altClose, closeTime))
    }
}
