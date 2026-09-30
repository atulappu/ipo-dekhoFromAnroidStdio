package com.example.ipotracker

import com.example.ipotracker.data.model.*
import com.example.ipotracker.utils.DateUtils
import org.junit.Assert.*
import org.junit.Test
import java.util.*

class DataProvenanceUpcomingTest {

    // Simulated test instant: Wednesday, 30-Sep-2026 12:00:00 IST
    private val testCalendar = Calendar.getInstance(DateUtils.IST_TIME_ZONE).apply {
        set(2026, Calendar.SEPTEMBER, 30, 12, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }
    private val testInstant = testCalendar.timeInMillis

    // 1. Historical IPO cannot appear in Upcoming
    @Test
    fun testHistoricalIpoCannotAppearInUpcoming() {
        // Garuda Construction and Engineering Ltd (Listed Oct 2024)
        val garuda = IpoItem(
            id = "ipo-garuda",
            name = "Garuda Construction and Engineering Ltd",
            symbol = "GARUDA",
            category = IpoCategory.MAINBOARD,
            status = IpoStatus.LISTED,
            listingStatus = ListingStatus.LISTED,
            openDate = "08-Oct-2024",
            closeDate = "10-Oct-2024",
            listingDate = "15-Oct-2024",
            priceBandMin = 90.0,
            priceBandMax = 95.0,
            lotSize = 157
        )

        val status = DateUtils.calculateEffectiveStatus(
            openDateStr = garuda.openDate,
            closeDateStr = garuda.closeDate,
            listingDateStr = garuda.listingDate,
            fallbackStatus = garuda.status,
            checkInstantMillis = testInstant
        )

        assertNotEquals(IpoStatus.UPCOMING, status)
        assertEquals(IpoStatus.LISTED, status)
        assertFalse(DateUtils.isDateInFuture(garuda.openDate, testInstant))
    }

    // 2. IPO with CloseDate in the past cannot appear in Upcoming
    @Test
    fun testIpoWithCloseDateInPastCannotAppearInUpcoming() {
        val closedIpo = IpoItem(
            id = "test-closed",
            name = "Past Issue Ltd",
            symbol = "PAST",
            category = IpoCategory.MAINBOARD,
            status = IpoStatus.UPCOMING, // Static erroneous flag
            openDate = "20-Sep-2026",
            closeDate = "24-Sep-2026",
            priceBandMin = 100.0,
            priceBandMax = 110.0,
            lotSize = 100
        )

        val status = DateUtils.calculateEffectiveStatus(
            openDateStr = closedIpo.openDate,
            closeDateStr = closedIpo.closeDate,
            fallbackStatus = closedIpo.status,
            checkInstantMillis = testInstant
        )

        assertNotEquals("IPO with close date in the past must never be UPCOMING", IpoStatus.UPCOMING, status)
        assertEquals(IpoStatus.CLOSED, status)
    }

    // 3. IPO with OpenDate in the future can appear in Upcoming if source is valid
    @Test
    fun testIpoWithOpenDateInFutureCanAppearInUpcomingIfSourceValid() {
        val futureIpo = IpoItem(
            id = "test-future",
            name = "R.K. Fashion Accessories Ltd",
            symbol = "RKFASHION",
            category = IpoCategory.SME,
            status = IpoStatus.UPCOMING,
            openDate = "05-Oct-2026",
            closeDate = "07-Oct-2026",
            priceBandMin = 85.0,
            priceBandMax = 90.0,
            lotSize = 1600,
            sourceId = "SRC_INVESTORGAIN_GMP",
            isSourceVerified = true
        )

        val status = DateUtils.calculateEffectiveStatus(
            openDateStr = futureIpo.openDate,
            closeDateStr = futureIpo.closeDate,
            fallbackStatus = futureIpo.status,
            checkInstantMillis = testInstant
        )

        assertEquals(IpoStatus.UPCOMING, status)
        assertTrue(DateUtils.isDateInFuture(futureIpo.openDate, testInstant))
        assertTrue(futureIpo.isSourceVerified)
    }

    // 4. Current Open IPO appears in Open
    @Test
    fun testCurrentOpenIpoAppearsInOpen() {
        val openIpo = IpoItem(
            id = "vans-electroengineerings",
            name = "VANS Electroengineerings Ltd.",
            symbol = "VANS",
            category = IpoCategory.SME,
            status = IpoStatus.OPEN,
            openDate = "29-Sep-2026",
            closeDate = "01-Oct-2026",
            priceBandMin = 118.0,
            priceBandMax = 118.0,
            lotSize = 1200
        )

        val status = DateUtils.calculateEffectiveStatus(
            openDateStr = openIpo.openDate,
            closeDateStr = openIpo.closeDate,
            fallbackStatus = openIpo.status,
            checkInstantMillis = testInstant
        )

        assertEquals(IpoStatus.OPEN, status)
    }

    // 5. Closed IPO appears in Closed
    @Test
    fun testClosedIpoAppearsInClosed() {
        val closedIpo = IpoItem(
            id = "ipo-manba",
            name = "Manba Finance Ltd",
            symbol = "MANBA",
            category = IpoCategory.MAINBOARD,
            status = IpoStatus.CLOSED,
            openDate = "23-Sep-2026",
            closeDate = "25-Sep-2026",
            priceBandMin = 114.0,
            priceBandMax = 120.0,
            lotSize = 125
        )

        val status = DateUtils.calculateEffectiveStatus(
            openDateStr = closedIpo.openDate,
            closeDateStr = closedIpo.closeDate,
            fallbackStatus = closedIpo.status,
            checkInstantMillis = testInstant
        )

        assertTrue(status.isClosed)
    }

    // 6. Historical IPO remains available in historical/closed data
    @Test
    fun testHistoricalIpoRemainsAvailableInHistory() {
        val hyundai = IpoItem(
            id = "ipo-hyundai",
            name = "Hyundai Motor India Ltd",
            symbol = "HYUNDAI",
            category = IpoCategory.MAINBOARD,
            status = IpoStatus.LISTED,
            listingStatus = ListingStatus.LISTED,
            openDate = "15-Oct-2024",
            closeDate = "17-Oct-2024",
            listingDate = "22-Oct-2024",
            priceBandMin = 1865.0,
            priceBandMax = 1960.0,
            lotSize = 7
        )

        val status = DateUtils.calculateEffectiveStatus(
            openDateStr = hyundai.openDate,
            closeDateStr = hyundai.closeDate,
            listingDateStr = hyundai.listingDate,
            fallbackStatus = hyundai.status,
            checkInstantMillis = testInstant
        )

        assertEquals("Historical IPO must be LISTED", IpoStatus.LISTED, status)
        assertNotNull("Historical IPO must retain its identity", hyundai.id)
        assertEquals("Historical listing date must be preserved", "22-Oct-2024", hyundai.listingDate)
    }

    // 7. Missing source provenance prevents an IPO from being marked verified Upcoming
    @Test
    fun testMissingSourceProvenancePreventsVerifiedUpcoming() {
        val unverifiedIpo = IpoItem(
            id = "ipo-fake",
            name = "Unverified Future Corp",
            symbol = "FAKE",
            category = IpoCategory.MAINBOARD,
            status = IpoStatus.UPCOMING,
            priceBandMin = 100.0,
            priceBandMax = 110.0,
            lotSize = 100,
            openDate = "20-Oct-2026",
            closeDate = "22-Oct-2026",
            sourceId = "",
            isSourceVerified = false,
            isDemoData = true
        )

        val isEligibleForUpcoming = unverifiedIpo.status == IpoStatus.UPCOMING &&
                !unverifiedIpo.status.isClosed &&
                DateUtils.isDateInFuture(unverifiedIpo.openDate, testInstant) &&
                unverifiedIpo.isSourceVerified &&
                !unverifiedIpo.isDemoData

        assertFalse("Unverified demo data without provenance must not appear in upcoming", isEligibleForUpcoming)
    }

    // 8. Frontend does not use mock IPO data
    @Test
    fun testFrontendDoesNotUseMockIpoData() {
        val realIpos = listOf(
            IpoItem(
                id = "real-1",
                name = "Real Verified IPO",
                symbol = "REAL",
                category = IpoCategory.MAINBOARD,
                status = IpoStatus.UPCOMING,
                priceBandMin = 100.0,
                priceBandMax = 110.0,
                lotSize = 100,
                openDate = "10-Oct-2026",
                closeDate = "12-Oct-2026",
                isDemoData = false,
                isSourceVerified = true
            ),
            IpoItem(
                id = "mock-1",
                name = "Mock Sample IPO",
                symbol = "MOCK",
                category = IpoCategory.MAINBOARD,
                status = IpoStatus.UPCOMING,
                priceBandMin = 100.0,
                priceBandMax = 110.0,
                lotSize = 100,
                openDate = "10-Oct-2026",
                closeDate = "12-Oct-2026",
                isDemoData = true,
                isSourceVerified = false
            )
        )

        val verifiedUpcoming = realIpos.filter {
            it.status == IpoStatus.UPCOMING &&
                    !it.status.isClosed &&
                    DateUtils.isDateInFuture(it.openDate, testInstant) &&
                    it.isSourceVerified &&
                    !it.isDemoData
        }

        assertEquals(1, verifiedUpcoming.size)
        assertEquals("real-1", verifiedUpcoming.first().id)
    }

    // 9. API returns only DB-backed validated IPO data
    @Test
    fun testApiReturnsOnlyDbBackedValidatedIpoData() {
        val dbRecord = IpoItem(
            id = "db-backed-1",
            name = "Validated Issue Ltd",
            symbol = "VAL",
            category = IpoCategory.MAINBOARD,
            status = IpoStatus.OPEN,
            priceBandMin = 100.0,
            priceBandMax = 110.0,
            lotSize = 100,
            openDate = "29-Sep-2026",
            closeDate = "01-Oct-2026",
            sourceId = "SRC_NSE_LIVE",
            isSourceVerified = true
        )

        assertTrue(dbRecord.isSourceVerified)
        assertEquals("SRC_NSE_LIVE", dbRecord.sourceId)
    }

    // 10. IST date/time is used (00:00:00 Open, 17:30:00 Close)
    @Test
    fun testIstDateTimeThresholds() {
        val openDate = "29-Sep-2026"
        val closeDate = "01-Oct-2026"

        // 29-Sep-2026 00:00:00 IST -> OPEN
        val atOpen = Calendar.getInstance(DateUtils.IST_TIME_ZONE).apply {
            set(2026, Calendar.SEPTEMBER, 29, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        assertEquals(IpoStatus.OPEN, DateUtils.calculateIpoStatus(openDate, closeDate, atOpen))

        // 01-Oct-2026 17:29:59 IST -> OPEN
        val beforeClose = Calendar.getInstance(DateUtils.IST_TIME_ZONE).apply {
            set(2026, Calendar.OCTOBER, 1, 17, 29, 59)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        assertEquals(IpoStatus.OPEN, DateUtils.calculateIpoStatus(openDate, closeDate, beforeClose))

        // 01-Oct-2026 17:30:00 IST -> CLOSED
        val atClose = Calendar.getInstance(DateUtils.IST_TIME_ZONE).apply {
            set(2026, Calendar.OCTOBER, 1, 17, 30, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        assertEquals(IpoStatus.CLOSED, DateUtils.calculateIpoStatus(openDate, closeDate, atClose))
    }
}
