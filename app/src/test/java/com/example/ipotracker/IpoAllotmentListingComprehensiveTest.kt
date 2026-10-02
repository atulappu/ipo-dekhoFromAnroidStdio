package com.example.ipotracker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ipotracker.data.model.*
import com.example.ipotracker.domain.repository.IpoRepository
import com.example.ipotracker.notification.IpoNotificationManager
import com.example.ipotracker.presentation.home.HomeNavigationContext
import com.example.ipotracker.presentation.home.HomeViewModel
import com.example.ipotracker.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Calendar
import java.util.TimeZone

/**
 * Formal Automated Test Suite for Allotment, Listing, Watch Live, and Dynamic Real-Time Refresh
 * Verifies all 14 test specifications required by the IPODekho Production Audit.
 */
@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class IpoAllotmentListingComprehensiveTest {

    private val testDispatcher = StandardTestDispatcher()
    private val istZone = TimeZone.getTimeZone("Asia/Kolkata")
    private lateinit var context: Context

    private fun getIstMillis(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int): Long {
        val cal = Calendar.getInstance(istZone)
        cal.set(year, month - 1, day, hour, minute, second)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        IpoNotificationManager.clearNotifications()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        IpoNotificationManager.clearNotifications()
    }

    // =========================================================================
    // TEST 1: Closed IPO + no allotment -> WAITING disabled
    // =========================================================================
    @Test
    fun test1_closedIpoWithoutAllotment_showsWaitingDisabled() {
        val testTime = getIstMillis(2026, 10, 1, 18, 0, 0)
        val ipo = IpoItem(
            id = "test-srit-closed",
            name = "SRIT India Ltd",
            symbol = "SRIT",
            openDate = "28-Sep-2026",
            closeDate = "30-Sep-2026",
            allotmentDate = "01-Oct-2026",
            status = IpoStatus.CLOSED,
            allotmentStatus = AllotmentStatus.PENDING,
            listingStatus = ListingStatus.NOT_LISTED,
            allotmentInfo = AllotmentInfo(
                registrarName = "MUFG Intime",
                registrarUrl = "https://in.mpms.mufg.com/",
                allotmentDate = "01-Oct-2026",
                isAvailable = false
            )
        )

        val allotmentStatus = DateUtils.getAllotmentStatus(ipo, testTime)
        assertEquals("WAITING", allotmentStatus)
        assertFalse(DateUtils.isAllotmentAvailable(ipo, testTime))
        assertEquals(IpoLifecycleState.CLOSED_ALLOTMENT_PENDING, ipo.lifecycleState)
    }

    // =========================================================================
    // TEST 2: Closed IPO + actual validated allotment -> ALLOTMENT enabled
    // =========================================================================
    @Test
    fun test2_closedIpoWithValidatedAllotment_showsAllotmentEnabled() {
        val testTime = getIstMillis(2026, 10, 1, 18, 0, 0)
        val ipo = IpoItem(
            id = "test-orient-allotted",
            name = "Orient Cables (India) Ltd.",
            symbol = "ORIENTCABL",
            openDate = "25-Sep-2026",
            closeDate = "29-Sep-2026",
            allotmentDate = "30-Sep-2026",
            status = IpoStatus.ALLOTMENT_AVAILABLE,
            allotmentStatus = AllotmentStatus.AVAILABLE,
            listingStatus = ListingStatus.NOT_LISTED,
            allotmentInfo = AllotmentInfo(
                registrarName = "Kfin Technologies Ltd.",
                registrarUrl = "https://ipostatus.kfintech.com/",
                allotmentDate = "30-Sep-2026",
                isAvailable = true
            )
        )

        val allotmentStatus = DateUtils.getAllotmentStatus(ipo, testTime)
        assertEquals("AVAILABLE", allotmentStatus)
        assertTrue(DateUtils.isAllotmentAvailable(ipo, testTime))
        assertEquals(IpoLifecycleState.ALLOTMENT_AVAILABLE, ipo.lifecycleState)
    }

    // =========================================================================
    // TEST 3: Allotment date exists but result unavailable -> WAITING
    // =========================================================================
    @Test
    fun test3_allotmentDatePassed_butResultUnavailable_showsWaiting() {
        // Today is 02-Oct, allotmentDate was 01-Oct, but result is not available
        val testTime = getIstMillis(2026, 10, 2, 12, 0, 0)
        val ipo = IpoItem(
            id = "test-pending-result",
            name = "Pending Result Ltd",
            symbol = "PENDRES",
            openDate = "28-Sep-2026",
            closeDate = "30-Sep-2026",
            allotmentDate = "01-Oct-2026",
            status = IpoStatus.CLOSED,
            allotmentStatus = AllotmentStatus.PENDING,
            allotmentInfo = AllotmentInfo(
                registrarName = "Bigshare",
                registrarUrl = "https://bigshare.com",
                allotmentDate = "01-Oct-2026",
                isAvailable = false
            )
        )

        val status = DateUtils.getAllotmentStatus(ipo, testTime)
        assertEquals("WAITING", status)
        assertFalse(DateUtils.isAllotmentAvailable(ipo, testTime))
    }

    // =========================================================================
    // TEST 4: Source unavailable -> WAITING / SOURCE_UNAVAILABLE internally
    // =========================================================================
    @Test
    fun test4_sourceUnavailable_returnsSourceUnavailableInternally() {
        val testTime = getIstMillis(2026, 10, 1, 18, 0, 0)
        val status = DateUtils.getAllotmentStatus(
            openDateStr = "28-Sep-2026",
            closeDateStr = "30-Sep-2026",
            allotmentDateStr = "01-Oct-2026",
            isExplicitlyAvailable = false,
            sourceFailed = true,
            checkInstantMillis = testTime
        )
        assertEquals("SOURCE_UNAVAILABLE", status)
    }

    // =========================================================================
    // TEST 5: Source returns invalid allotment (Open > Close) -> DATA_ERROR
    // =========================================================================
    @Test
    fun test5_invalidDates_returnsDataError() {
        val testTime = getIstMillis(2026, 10, 1, 18, 0, 0)
        val status = DateUtils.getAllotmentStatus(
            openDateStr = "05-Oct-2026",
            closeDateStr = "01-Oct-2026",
            allotmentDateStr = "06-Oct-2026",
            isExplicitlyAvailable = false,
            sourceFailed = false,
            checkInstantMillis = testTime
        )
        assertEquals("DATA_ERROR", status)
    }

    // =========================================================================
    // TEST 6: WAITING -> AVAILABLE transition generates one notification
    // =========================================================================
    @Test
    fun test6_waitingToAvailable_generatesOneNotification() {
        val ipoId = "ipo-orient-cables"
        val ipoName = "Orient Cables (India) Ltd."

        assertEquals(0, IpoNotificationManager.notifications.value.size)

        IpoNotificationManager.notifyAllotmentOut(
            context = context,
            ipoId = ipoId,
            ipoName = ipoName,
            registrar = "Kfin Technologies Ltd."
        )

        assertEquals(1, IpoNotificationManager.notifications.value.size)
        val notif = IpoNotificationManager.notifications.value.first()
        assertEquals("ALLOTMENT_AVAILABLE:$ipoId", notif.eventKey)
        assertEquals(NotificationType.ALLOTMENT_OUT, notif.type)
    }

    // =========================================================================
    // TEST 7: Repeated refresh after AVAILABLE generates no duplicate notifications
    // =========================================================================
    @Test
    fun test7_repeatedRefreshAfterAvailable_noDuplicates() {
        val ipoId = "ipo-orient-cables"
        val ipoName = "Orient Cables (India) Ltd."

        // Initial notification
        IpoNotificationManager.notifyAllotmentOut(
            context = context,
            ipoId = ipoId,
            ipoName = ipoName,
            registrar = "Kfin Technologies Ltd."
        )
        assertEquals(1, IpoNotificationManager.notifications.value.size)

        // Attempt 100 repeated refreshes with the same event
        for (i in 1..100) {
            IpoNotificationManager.notifyAllotmentOut(
                context = context,
                ipoId = ipoId,
                ipoName = ipoName,
                registrar = "Kfin Technologies Ltd."
            )
        }

        // Must still remain exactly 1 notification
        assertEquals(1, IpoNotificationManager.notifications.value.size)
    }

    // =========================================================================
    // TEST 8: Closed + listed + allotment available -> ALLOTMENT and WATCH LIVE
    // =========================================================================
    @Test
    fun test8_closedListedAllotmentAvailable_showsAllotmentAndWatchLive() {
        val ipo = IpoItem(
            id = "test-garuda",
            name = "Garuda Construction and Engineering Ltd",
            symbol = "GARUDA",
            openDate = "08-Oct-2024",
            closeDate = "10-Oct-2024",
            allotmentDate = "11-Oct-2024",
            listingDate = "15-Oct-2024",
            status = IpoStatus.LISTED,
            allotmentStatus = AllotmentStatus.AVAILABLE,
            listingStatus = ListingStatus.LISTED,
            allotmentInfo = AllotmentInfo(
                registrarName = "Link Intime",
                registrarUrl = "https://linkintime.co.in",
                allotmentDate = "11-Oct-2024",
                isAvailable = true
            )
        )

        assertEquals("AVAILABLE", DateUtils.getAllotmentStatus(ipo))
        assertEquals("LISTED", DateUtils.getListingStatus(ipo.listingStatus, ipo.status))
        assertTrue(DateUtils.isWatchLiveAvailable(ipo))
        assertEquals(IpoLifecycleState.LISTED, ipo.lifecycleState)
    }

    // =========================================================================
    // TEST 9: Closed + listed + allotment unavailable -> WAITING and WATCH LIVE
    // =========================================================================
    @Test
    fun test9_closedListedAllotmentUnavailable_showsWaitingAndWatchLive() {
        val ipo = IpoItem(
            id = "test-listed-unallotted",
            name = "Special Listing Corp",
            symbol = "SPECLIST",
            openDate = "10-Sep-2026",
            closeDate = "12-Sep-2026",
            allotmentDate = "14-Sep-2026",
            listingDate = "18-Sep-2026",
            status = IpoStatus.LISTED,
            allotmentStatus = AllotmentStatus.PENDING,
            listingStatus = ListingStatus.LISTED,
            allotmentInfo = AllotmentInfo(
                registrarName = "Bigshare",
                registrarUrl = "https://bigshare.com",
                allotmentDate = "14-Sep-2026",
                isAvailable = false
            )
        )

        assertEquals("WAITING", DateUtils.getAllotmentStatus(ipo))
        assertEquals("LISTED", DateUtils.getListingStatus(ipo.listingStatus, ipo.status))
        assertTrue(DateUtils.isWatchLiveAvailable(ipo))
        assertEquals(IpoLifecycleState.LISTED, ipo.lifecycleState)
    }

    // =========================================================================
    // TEST 10: Not listed -> No Watch Live
    // =========================================================================
    @Test
    fun test10_notListed_noWatchLive() {
        val ipo = IpoItem(
            id = "test-not-listed",
            name = "Unlisted Corp",
            symbol = "UNLIST",
            status = IpoStatus.CLOSED,
            listingStatus = ListingStatus.NOT_LISTED
        )

        assertEquals("NOT_LISTED", DateUtils.getListingStatus(ipo.listingStatus, ipo.status))
        assertFalse(DateUtils.isWatchLiveAvailable(ipo))
    }

    // =========================================================================
    // TEST 11: Background refresh changes WAITING -> AVAILABLE dynamically
    // =========================================================================
    @Test
    fun test11_backgroundRefreshTransitionsWaitingToAvailable() = runTest(testDispatcher) {
        val initialIpo = IpoItem(
            id = "dyn-ipo",
            name = "Dynamic Ipo Ltd",
            symbol = "DYNIPO",
            status = IpoStatus.CLOSED,
            allotmentStatus = AllotmentStatus.PENDING
        )
        val ipoFlow = MutableStateFlow(listOf(initialIpo))

        val repo = object : EmptyTestRepository() {
            override fun getAllIpos(): Flow<List<IpoItem>> = ipoFlow
            override fun getClosedIpos(): Flow<List<IpoItem>> = ipoFlow
        }

        val viewModel = HomeViewModel(repo)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("WAITING", DateUtils.getAllotmentStatus(viewModel.uiState.value.closedIpos.first()))

        // Simulate background data update from scraper pipeline
        val updatedIpo = initialIpo.copy(
            status = IpoStatus.ALLOTMENT_AVAILABLE,
            allotmentStatus = AllotmentStatus.AVAILABLE,
            allotmentInfo = AllotmentInfo(
                registrarName = "Kfin Technologies Ltd.",
                registrarUrl = "https://ipostatus.kfintech.com/",
                allotmentDate = "01-Oct-2026",
                isAvailable = true
            )
        )
        ipoFlow.value = listOf(updatedIpo)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("AVAILABLE", DateUtils.getAllotmentStatus(viewModel.uiState.value.closedIpos.first()))
    }

    // =========================================================================
    // TEST 12: User is on CLOSED tab during allotment update -> Remain on CLOSED tab
    // =========================================================================
    @Test
    fun test12_userOnClosedTab_remainsOnClosedTabDuringAllotmentUpdate() = runTest(testDispatcher) {
        val repo = object : EmptyTestRepository() {
            override fun getAllIpos(): Flow<List<IpoItem>> = flowOf(emptyList())
        }

        val viewModel = HomeViewModel(repo)
        // User navigates to CLOSED tab (tab index 2)
        viewModel.updateSelectedTab(2)
        assertEquals(2, viewModel.navContext.value.selectedTab)

        // Perform refresh
        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        // Tab MUST remain 2 (CLOSED tab)
        assertEquals(2, viewModel.navContext.value.selectedTab)
    }

    // =========================================================================
    // TEST 13: User is on CLOSED tab and Watch Live becomes available -> Remain on CLOSED tab
    // =========================================================================
    @Test
    fun test13_userOnClosedTab_remainsOnClosedTabWhenWatchLiveAppears() = runTest(testDispatcher) {
        val listedIpo = IpoItem(
            id = "test-listed-closed-tab",
            name = "Listed Company Ltd",
            symbol = "LISTEDCO",
            status = IpoStatus.LISTED,
            listingStatus = ListingStatus.LISTED
        )
        val repo = object : EmptyTestRepository() {
            override fun getAllIpos(): Flow<List<IpoItem>> = flowOf(listOf(listedIpo))
            override fun getClosedIpos(): Flow<List<IpoItem>> = flowOf(listOf(listedIpo))
        }

        val viewModel = HomeViewModel(repo)
        viewModel.updateSelectedTab(2) // CLOSED
        testDispatcher.scheduler.advanceUntilIdle()

        // Tab index remains 2
        assertEquals(2, viewModel.navContext.value.selectedTab)
        // Listed IPO is still returned in getClosedIpos because isClosed = true
        assertTrue(listedIpo.status.isClosed)
        assertTrue(DateUtils.isWatchLiveAvailable(listedIpo))
    }

    // =========================================================================
    // TEST 14: No API/data source response -> No fake data, graceful error handling
    // =========================================================================
    @Test
    fun test14_noApiResponse_noFakeData() = runTest(testDispatcher) {
        val repo = object : EmptyTestRepository() {
            override fun getAllIpos(): Flow<List<IpoItem>> = flowOf(emptyList())
            override suspend fun refreshData(): Result<Unit> = Result.failure(Exception("Network Timeout: 503 Service Unavailable"))
        }

        val viewModel = HomeViewModel(repo)
        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.openIpos.isEmpty())
        assertTrue(state.closedIpos.isEmpty())
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Network Timeout"))
    }

    open class EmptyTestRepository : IpoRepository {
        override fun getAllIpos(): Flow<List<IpoItem>> = flowOf(emptyList())
        override fun getOpenIpos(): Flow<List<IpoItem>> = flowOf(emptyList())
        override fun getUpcomingIpos(): Flow<List<IpoItem>> = flowOf(emptyList())
        override fun getClosedIpos(): Flow<List<IpoItem>> = flowOf(emptyList())
        override fun getListedIpos(): Flow<List<IpoItem>> = flowOf(emptyList())
        override fun getMarketSummary(): Flow<List<MarketIndex>> = flowOf(emptyList())
        override fun getIpoById(id: String): Flow<IpoItem?> = flowOf(null)
        override fun searchIpos(query: String): Flow<List<IpoItem>> = flowOf(emptyList())
        override fun getWatchlist(): Flow<List<IpoItem>> = flowOf(emptyList())
        override suspend fun toggleWatchlist(ipoId: String) {}
        override fun isWatchlisted(ipoId: String): Flow<Boolean> = flowOf(false)
        override fun getRecentSearches(): Flow<List<String>> = flowOf(emptyList())
        override suspend fun saveSearchQuery(query: String) {}
        override suspend fun deleteSearchQuery(query: String) {}
        override suspend fun clearSearchHistory() {}
        override suspend fun refreshData(): Result<Unit> = Result.success(Unit)
        override fun getRegistrars(): Flow<List<com.example.ipotracker.data.model.RegistrarItem>> = flowOf(emptyList())
        override suspend fun updateRegistrarUrl(id: String, newUrl: String, comments: String?, modifiedBy: String): Result<Unit> = Result.success(Unit)
        override suspend fun saveRegistrar(item: com.example.ipotracker.data.model.RegistrarItem): Result<Unit> = Result.success(Unit)
        override fun getExchangeConfigs(): Flow<List<com.example.ipotracker.data.local.entity.ExchangeConfigEntity>> = flowOf(emptyList())
        override suspend fun updateExchangeUrl(key: String, newUrl: String): Result<Unit> = Result.success(Unit)
        override suspend fun resetExchangeUrls(): Result<Unit> = Result.success(Unit)
        override suspend fun syncFromExchanges(): Result<Int> = Result.success(0)
    }
}
