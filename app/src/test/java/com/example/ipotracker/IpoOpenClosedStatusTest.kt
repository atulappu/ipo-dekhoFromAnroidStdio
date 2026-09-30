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

    @Test
    fun testVansElectroengineerings_IssueTimeline_29Sep_to_01Oct() {
        val vansOpen = "29-Sep-2026"
        val vansClose = "01-Oct-2026"

        // 1. 29-Sep-2026 00:00:00 IST -> OPEN
        val timeDay1 = getIstMillis(2026, 9, 29, 0, 0, 0)
        assertEquals(IpoStatus.OPEN, DateUtils.calculateIpoStatus(vansOpen, vansClose, timeDay1))
        assertEquals("OPEN", DateUtils.getIPOStatus(vansOpen, vansClose, timeDay1))

        // 2. 30-Sep-2026 12:00:00 IST -> OPEN (Current active bidding)
        val timeDay2 = getIstMillis(2026, 9, 30, 12, 0, 0)
        assertEquals(IpoStatus.OPEN, DateUtils.calculateIpoStatus(vansOpen, vansClose, timeDay2))
        assertEquals("OPEN", DateUtils.getIPOStatus(vansOpen, vansClose, timeDay2))

        // 3. 01-Oct-2026 17:29:59 IST -> OPEN
        val timeJustBeforeClose = getIstMillis(2026, 10, 1, 17, 29, 59)
        assertEquals(IpoStatus.OPEN, DateUtils.calculateIpoStatus(vansOpen, vansClose, timeJustBeforeClose))
        assertEquals("OPEN", DateUtils.getIPOStatus(vansOpen, vansClose, timeJustBeforeClose))

        // 4. 01-Oct-2026 17:30:00 IST -> CLOSED
        val timeAtClose = getIstMillis(2026, 10, 1, 17, 30, 0)
        assertEquals(IpoStatus.CLOSED, DateUtils.calculateIpoStatus(vansOpen, vansClose, timeAtClose))
        assertEquals("CLOSED", DateUtils.getIPOStatus(vansOpen, vansClose, timeAtClose))

        // 5. 02-Oct-2026 00:00:00 IST -> CLOSED
        val timeAfterClose = getIstMillis(2026, 10, 2, 0, 0, 0)
        assertEquals(IpoStatus.CLOSED, DateUtils.calculateIpoStatus(vansOpen, vansClose, timeAfterClose))
        assertEquals("CLOSED", DateUtils.getIPOStatus(vansOpen, vansClose, timeAfterClose))
    }

    @Test
    fun testDateParsingAllRequiredFormats() {
        val testFormats = listOf(
            "29-Sep-2026",
            "29/09/2026",
            "29-09-2026",
            "2026-09-29",
            "2026-09-29T00:00:00",
            "2026-09-29T00:00:00+05:30",
            "29-Sep",
            "29-Sep   GMP: 90",
            "29-Sep<br><small>GMP: 90</small>"
        )

        for (fmt in testFormats) {
            val cal = DateUtils.parseToIstCalendar(fmt)
            org.junit.Assert.assertNotNull("Format '$fmt' must parse successfully", cal)
            cal?.let {
                assertEquals("Year must be 2026 for '$fmt'", 2026, it.get(Calendar.YEAR))
                assertEquals("Month must be September (8) for '$fmt'", Calendar.SEPTEMBER, it.get(Calendar.MONTH))
                assertEquals("Day must be 29 for '$fmt'", 29, it.get(Calendar.DAY_OF_MONTH))
            }
        }

        // Test 1-Oct format without year
        val octCal = DateUtils.parseToIstCalendar("1-Oct")
        org.junit.Assert.assertNotNull("1-Oct must parse successfully", octCal)
        octCal?.let {
            assertEquals(2026, it.get(Calendar.YEAR))
            assertEquals(Calendar.OCTOBER, it.get(Calendar.MONTH))
            assertEquals(1, it.get(Calendar.DAY_OF_MONTH))
        }
    }

    @Test
    fun testVansElectroengineeringsInVerifiedMasterList() {
        val vans = com.example.ipotracker.data.remote.MockIpoDataSource.ipoList.find {
            it.name.contains("VANS", ignoreCase = true) || it.id.contains("vans", ignoreCase = true)
        }
        org.junit.Assert.assertNotNull("VANS Electroengineerings must be present in verified master IPO list", vans)
        vans?.let {
            assertEquals(com.example.ipotracker.data.model.IpoCategory.SME, it.category)
            org.junit.Assert.assertTrue("Exchange must include BSE", it.listingExchanges.contains("BSE"))
            assertEquals("29-Sep-2026", it.openDate)
            assertEquals("01-Oct-2026", it.closeDate)

            // Current test time: 30-Sep-2026 12:00:00 IST -> must be OPEN
            val testTime = getIstMillis(2026, 9, 30, 12, 0, 0)
            val effectiveStatus = DateUtils.calculateEffectiveStatus(
                openDateStr = it.openDate,
                closeDateStr = it.closeDate,
                allotmentDateStr = it.allotmentDate,
                listingDateStr = it.listingDate,
                fallbackStatus = it.status,
                checkInstantMillis = testTime
            )
            assertEquals(IpoStatus.OPEN, effectiveStatus)
        }
    }
}
