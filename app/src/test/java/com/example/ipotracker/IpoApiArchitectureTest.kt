package com.example.ipotracker

import com.example.ipotracker.data.model.IpoCategory
import com.example.ipotracker.data.model.IpoStatus
import com.example.ipotracker.data.remote.DataSourceType
import com.example.ipotracker.data.remote.IpoConfig
import com.example.ipotracker.data.remote.dto.IpoDto
import com.example.ipotracker.data.remote.dto.MarketIndexDto
import com.example.ipotracker.data.remote.dto.SubscriptionDetailsDto
import com.example.ipotracker.data.remote.dto.SubscriptionRowDto
import com.example.ipotracker.data.remote.mapper.IpoDtoMapper
import org.junit.Assert.*
import org.junit.Test

class IpoApiArchitectureTest {

    @Test
    fun testDefaultDataSourceIsMock() {
        // MockDataSource must remain the default development source
        assertEquals(DataSourceType.MOCK, IpoConfig.DATA_SOURCE)
        assertTrue(IpoConfig.isMockMode)
    }

    @Test
    fun testSwitchingDataSourceToApi() {
        val original = IpoConfig.DATA_SOURCE
        try {
            IpoConfig.DATA_SOURCE = DataSourceType.API
            assertEquals(DataSourceType.API, IpoConfig.DATA_SOURCE)
            assertFalse(IpoConfig.isMockMode)
        } finally {
            IpoConfig.DATA_SOURCE = original
        }
    }

    @Test
    fun testIpoDtoMapperFullMapping() {
        val dto = IpoDto(
            id = "test_solar_ipo",
            name = "Test Solar Energy Ltd",
            symbol = "TESTSOLAR",
            category = "MAINBOARD",
            status = "OPEN",
            priceBandMinCamel = 1850.0,
            priceBandMaxCamel = 1920.0,
            lotSizeCamel = 7,
            minInvestmentCamel = 13440.0,
            issueSizeCrCamel = 14500.0,
            freshIssueCrCamel = 10000.0,
            ofsCrCamel = 4500.0,
            openDateCamel = "2026-09-25",
            closeDateCamel = "2026-09-28",
            allotmentDateCamel = "2026-09-29",
            listingDateCamel = "2026-10-01",
            currentGmpCamel = 480.0,
            estimatedListingPriceCamel = 2400.0,
            estimatedGainPercentCamel = 25.0,
            lastGmpUpdatedCamel = "26 Sep, 17:00",
            currentSubscriptionTimesCamel = 6.39,
            qibTimesCamel = 8.24,
            niiTimesCamel = 5.42,
            retailTimesCamel = 4.18
        )

        val domain = IpoDtoMapper.mapIpoDtoToDomain(dto)

        assertEquals("test_solar_ipo", domain.id)
        assertEquals("Test Solar Energy Ltd", domain.name)
        assertEquals("TESTSOLAR", domain.symbol)
        assertEquals(IpoCategory.MAINBOARD, domain.category)
        assertEquals(IpoStatus.OPEN, domain.status)
        assertEquals(1850.0, domain.priceBandMin, 0.001)
        assertEquals(1920.0, domain.priceBandMax, 0.001)
        assertEquals(7, domain.lotSize)
        assertEquals(13440.0, domain.minInvestment, 0.001)
        assertEquals(14500.0, domain.issueSizeCr, 0.001)
        assertEquals(480.0, domain.currentGmp, 0.001)
        assertEquals(2400.0, domain.estimatedListingPrice, 0.001)
        assertEquals(25.0, domain.estimatedGainPercent, 0.001)
        assertEquals(6.39, domain.currentSubscriptionTimes, 0.001)
        assertFalse(domain.isDemoData)
    }

    @Test
    fun testIpoDtoMapperSmeCategoryAndNullFallbacks() {
        val dto = IpoDto(
            id = null,
            name = "SME Fresh Organics Ltd",
            symbol = null,
            category = "SME",
            status = "UPCOMING",
            priceBandMinCamel = null,
            priceBandMaxCamel = 120.0,
            lotSizeCamel = 1000,
            currentGmpCamel = null
        )

        val domain = IpoDtoMapper.mapIpoDtoToDomain(dto)

        assertEquals(IpoCategory.SME, domain.category)
        assertEquals(IpoStatus.UPCOMING, domain.status)
        assertEquals(120.0, domain.priceBandMin, 0.001)
        assertEquals(120.0, domain.priceBandMax, 0.001)
        assertEquals(1000, domain.lotSize)
        assertEquals(120000.0, domain.minInvestment, 0.001)
        assertEquals(0.0, domain.currentGmp, 0.001)
    }

    @Test
    fun testMarketIndexDtoMapper() {
        val dto = MarketIndexDto(
            name = "NIFTY 50",
            value = "25,790.95",
            change = "+112.40",
            percentChangeCamel = 0.44,
            isPositiveCamel = true
        )

        val domain = IpoDtoMapper.mapMarketIndexDtoToDomain(dto)

        assertEquals("NIFTY 50", domain.name)
        assertEquals("25,790.95", domain.value)
        assertEquals("+112.40", domain.change)
        assertEquals(0.44, domain.percentChange, 0.001)
        assertTrue(domain.isPositive)
    }

    @Test
    fun testSubscriptionDetailsDtoMapper() {
        val dto = SubscriptionDetailsDto(
            overallTimesCamel = 6.39,
            qibTimesCamel = 8.24,
            niiTimesCamel = 5.42,
            retailTimesCamel = 4.18,
            categoryRowsCamel = listOf(
                SubscriptionRowDto(category = "QIB", offeredSharesCamel = 5000000L, appliedSharesCamel = 41200000L, times = 8.24),
                SubscriptionRowDto(category = "Retail", offeredSharesCamel = 8000000L, appliedSharesCamel = 33440000L, times = 4.18)
            ),
            lastUpdatedCamel = "26-Sep-2026 17:05:26"
        )

        val domain = IpoDtoMapper.mapSubscriptionDetailsToDomain(dto)

        assertEquals(6.39, domain.overallTimes, 0.001)
        assertEquals(2, domain.categoryRows.size)
        assertEquals("QIB", domain.categoryRows[0].category)
        assertEquals(8.24, domain.categoryRows[0].times, 0.001)
    }
}
