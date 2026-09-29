package com.example.ipotracker.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class IpoDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "symbol") val symbol: String? = null,
    @Json(name = "category") val category: String? = null,
    @Json(name = "status") val status: String? = null,

    // Both camelCase (ASP.NET Core default) and snake_case support
    @Json(name = "priceBandMin") val priceBandMinCamel: Double? = null,
    @Json(name = "price_band_min") val priceBandMinSnake: Double? = null,

    @Json(name = "priceBandMax") val priceBandMaxCamel: Double? = null,
    @Json(name = "price_band_max") val priceBandMaxSnake: Double? = null,

    @Json(name = "lotSize") val lotSizeCamel: Int? = null,
    @Json(name = "lot_size") val lotSizeSnake: Int? = null,

    @Json(name = "minInvestment") val minInvestmentCamel: Double? = null,
    @Json(name = "min_investment") val minInvestmentSnake: Double? = null,

    @Json(name = "issueSizeCr") val issueSizeCrCamel: Double? = null,
    @Json(name = "issue_size_cr") val issueSizeCrSnake: Double? = null,

    @Json(name = "freshIssueCr") val freshIssueCrCamel: Double? = null,
    @Json(name = "fresh_issue_cr") val freshIssueCrSnake: Double? = null,

    @Json(name = "ofsCr") val ofsCrCamel: Double? = null,
    @Json(name = "ofs_cr") val ofsCrSnake: Double? = null,

    @Json(name = "openDate") val openDateCamel: String? = null,
    @Json(name = "open_date") val openDateSnake: String? = null,

    @Json(name = "closeDate") val closeDateCamel: String? = null,
    @Json(name = "close_date") val closeDateSnake: String? = null,

    @Json(name = "allotmentDate") val allotmentDateCamel: String? = null,
    @Json(name = "allotment_date") val allotmentDateSnake: String? = null,

    @Json(name = "listingDate") val listingDateCamel: String? = null,
    @Json(name = "listing_date") val listingDateSnake: String? = null,

    @Json(name = "currentGmp") val currentGmpCamel: Double? = null,
    @Json(name = "current_gmp") val currentGmpSnake: Double? = null,

    @Json(name = "estimatedListingPrice") val estimatedListingPriceCamel: Double? = null,
    @Json(name = "estimated_listing_price") val estimatedListingPriceSnake: Double? = null,

    @Json(name = "estimatedGainPercent") val estimatedGainPercentCamel: Double? = null,
    @Json(name = "estimated_gain_percent") val estimatedGainPercentSnake: Double? = null,

    @Json(name = "lastGmpUpdated") val lastGmpUpdatedCamel: String? = null,
    @Json(name = "last_gmp_updated") val lastGmpUpdatedSnake: String? = null,

    @Json(name = "currentSubscriptionTimes") val currentSubscriptionTimesCamel: Double? = null,
    @Json(name = "current_subscription_times") val currentSubscriptionTimesSnake: Double? = null,

    @Json(name = "qibTimes") val qibTimesCamel: Double? = null,
    @Json(name = "qib_times") val qibTimesSnake: Double? = null,

    @Json(name = "niiTimes") val niiTimesCamel: Double? = null,
    @Json(name = "nii_times") val niiTimesSnake: Double? = null,

    @Json(name = "retailTimes") val retailTimesCamel: Double? = null,
    @Json(name = "retail_times") val retailTimesSnake: Double? = null,

    @Json(name = "listingPrice") val listingPriceCamel: Double? = null,
    @Json(name = "listing_price") val listingPriceSnake: Double? = null,

    @Json(name = "listingGainPercent") val listingGainPercentCamel: Double? = null,
    @Json(name = "listing_gain_percent") val listingGainPercentSnake: Double? = null,

    @Json(name = "currentMarketPrice") val currentMarketPriceCamel: Double? = null,
    @Json(name = "current_market_price") val currentMarketPriceSnake: Double? = null,

    @Json(name = "currentReturnPercent") val currentReturnPercentCamel: Double? = null,
    @Json(name = "current_return_percent") val currentReturnPercentSnake: Double? = null,

    @Json(name = "description") val description: String? = null,
    @Json(name = "sector") val sector: String? = null,
    @Json(name = "listingExchanges") val listingExchangesCamel: String? = null,
    @Json(name = "listing_exchanges") val listingExchangesSnake: String? = null,
    @Json(name = "faceValue") val faceValueCamel: Double? = null,
    @Json(name = "face_value") val faceValueSnake: Double? = null,
    @Json(name = "leadManagers") val leadManagersCamel: String? = null,
    @Json(name = "lead_managers") val leadManagersSnake: String? = null,
    @Json(name = "registrar") val registrar: String? = null,
    @Json(name = "promoterHoldingPre") val promoterHoldingPreCamel: Double? = null,
    @Json(name = "promoter_holding_pre") val promoterHoldingPreSnake: Double? = null,
    @Json(name = "promoterHoldingPost") val promoterHoldingPostCamel: Double? = null,
    @Json(name = "promoter_holding_post") val promoterHoldingPostSnake: Double? = null,
    @Json(name = "objectsOfIssue") val objectsOfIssueCamel: List<String>? = null,
    @Json(name = "objects_of_issue") val objectsOfIssueSnake: List<String>? = null,
    @Json(name = "subscriptionDetails") val subscriptionDetailsCamel: SubscriptionDetailsDto? = null,
    @Json(name = "subscription_details") val subscriptionDetailsSnake: SubscriptionDetailsDto? = null,
    @Json(name = "gmpHistory") val gmpHistoryCamel: List<GmpHistoryItemDto>? = null,
    @Json(name = "gmp_history") val gmpHistorySnake: List<GmpHistoryItemDto>? = null,
    @Json(name = "financials") val financials: List<FinancialYearDataDto>? = null,
    @Json(name = "importantDates") val importantDatesCamel: List<ImportantDateItemDto>? = null,
    @Json(name = "important_dates") val importantDatesSnake: List<ImportantDateItemDto>? = null,
    @Json(name = "allotmentInfo") val allotmentInfoCamel: AllotmentInfoDto? = null,
    @Json(name = "allotment_info") val allotmentInfoSnake: AllotmentInfoDto? = null,
    @Json(name = "analysisReport") val analysisReportCamel: AnalysisReportDto? = null,
    @Json(name = "analysis_report") val analysisReportSnake: AnalysisReportDto? = null
) {
    val priceBandMin: Double? get() = priceBandMinCamel ?: priceBandMinSnake
    val priceBandMax: Double? get() = priceBandMaxCamel ?: priceBandMaxSnake
    val lotSize: Int? get() = lotSizeCamel ?: lotSizeSnake
    val minInvestment: Double? get() = minInvestmentCamel ?: minInvestmentSnake
    val issueSizeCr: Double? get() = issueSizeCrCamel ?: issueSizeCrSnake
    val freshIssueCr: Double? get() = freshIssueCrCamel ?: freshIssueCrSnake
    val ofsCr: Double? get() = ofsCrCamel ?: ofsCrSnake
    val openDate: String? get() = openDateCamel ?: openDateSnake
    val closeDate: String? get() = closeDateCamel ?: closeDateSnake
    val allotmentDate: String? get() = allotmentDateCamel ?: allotmentDateSnake
    val listingDate: String? get() = listingDateCamel ?: listingDateSnake
    val currentGmp: Double? get() = currentGmpCamel ?: currentGmpSnake
    val estimatedListingPrice: Double? get() = estimatedListingPriceCamel ?: estimatedListingPriceSnake
    val estimatedGainPercent: Double? get() = estimatedGainPercentCamel ?: estimatedGainPercentSnake
    val lastGmpUpdated: String? get() = lastGmpUpdatedCamel ?: lastGmpUpdatedSnake
    val currentSubscriptionTimes: Double? get() = currentSubscriptionTimesCamel ?: currentSubscriptionTimesSnake
    val qibTimes: Double? get() = qibTimesCamel ?: qibTimesSnake
    val niiTimes: Double? get() = niiTimesCamel ?: niiTimesSnake
    val retailTimes: Double? get() = retailTimesCamel ?: retailTimesSnake
    val listingPrice: Double? get() = listingPriceCamel ?: listingPriceSnake
    val listingGainPercent: Double? get() = listingGainPercentCamel ?: listingGainPercentSnake
    val currentMarketPrice: Double? get() = currentMarketPriceCamel ?: currentMarketPriceSnake
    val currentReturnPercent: Double? get() = currentReturnPercentCamel ?: currentReturnPercentSnake
    val listingExchanges: String? get() = listingExchangesCamel ?: listingExchangesSnake
    val faceValue: Double? get() = faceValueCamel ?: faceValueSnake
    val leadManagers: String? get() = leadManagersCamel ?: leadManagersSnake
    val promoterHoldingPre: Double? get() = promoterHoldingPreCamel ?: promoterHoldingPreSnake
    val promoterHoldingPost: Double? get() = promoterHoldingPostCamel ?: promoterHoldingPostSnake
    val objectsOfIssue: List<String>? get() = objectsOfIssueCamel ?: objectsOfIssueSnake
    val subscriptionDetails: SubscriptionDetailsDto? get() = subscriptionDetailsCamel ?: subscriptionDetailsSnake
    val gmpHistory: List<GmpHistoryItemDto>? get() = gmpHistoryCamel ?: gmpHistorySnake
    val importantDates: List<ImportantDateItemDto>? get() = importantDatesCamel ?: importantDatesSnake
    val allotmentInfo: AllotmentInfoDto? get() = allotmentInfoCamel ?: allotmentInfoSnake
    val analysisReport: AnalysisReportDto? get() = analysisReportCamel ?: analysisReportSnake
}
