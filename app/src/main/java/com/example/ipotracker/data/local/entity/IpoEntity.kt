package com.example.ipotracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ipotracker.data.model.AllotmentInfo
import com.example.ipotracker.data.model.IpoCategory
import com.example.ipotracker.data.model.IpoItem
import com.example.ipotracker.data.model.IpoStatus

@Entity(tableName = "ipo_entries")
data class IpoEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val symbol: String,
    val exchange: String = "BSE, NSE",
    val category: String = "MAINBOARD",
    val status: String = "UPCOMING",
    val priceBandMin: Double = 0.0,
    val priceBandMax: Double = 0.0,
    val lotSize: Int = 1,
    val minInvestment: Double = 0.0,
    val issueSizeCr: Double = 0.0,
    val freshIssueCr: Double = 0.0,
    val ofsCr: Double = 0.0,
    val openDate: String = "",
    val closeDate: String = "",
    val allotmentDate: String = "",
    val listingDate: String = "",
    val currentGmp: Double = 0.0,
    val estimatedListingPrice: Double = 0.0,
    val estimatedGainPercent: Double = 0.0,
    val lastGmpUpdated: String = "",
    val currentSubscriptionTimes: Double = 0.0,
    val qibTimes: Double = 0.0,
    val niiTimes: Double = 0.0,
    val retailTimes: Double = 0.0,
    val listingPrice: Double? = null,
    val listingGainPercent: Double? = null,
    val currentMarketPrice: Double? = null,
    val currentReturnPercent: Double? = null,
    val description: String = "",
    val sector: String = "",
    val registrarName: String = "",
    val registrarUrl: String = "",
    val officialExchangeUrl: String = "",
    val lastSyncedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): IpoItem {
        val cat = try {
            IpoCategory.valueOf(category.uppercase())
        } catch (_: Exception) {
            IpoCategory.MAINBOARD
        }

        val stat = try {
            IpoStatus.valueOf(status.uppercase())
        } catch (_: Exception) {
            IpoStatus.UPCOMING
        }

        return IpoItem(
            id = id,
            name = name,
            symbol = symbol,
            category = cat,
            status = stat,
            priceBandMin = priceBandMin,
            priceBandMax = priceBandMax,
            lotSize = lotSize,
            minInvestment = if (minInvestment > 0) minInvestment else (priceBandMax * lotSize),
            issueSizeCr = issueSizeCr,
            freshIssueCr = freshIssueCr,
            ofsCr = ofsCr,
            openDate = openDate,
            closeDate = closeDate,
            allotmentDate = allotmentDate,
            listingDate = listingDate,
            currentGmp = currentGmp,
            estimatedListingPrice = if (estimatedListingPrice > 0) estimatedListingPrice else (priceBandMax + currentGmp),
            estimatedGainPercent = if (estimatedGainPercent != 0.0) estimatedGainPercent else (if (priceBandMax > 0) (currentGmp / priceBandMax) * 100 else 0.0),
            lastGmpUpdated = lastGmpUpdated,
            currentSubscriptionTimes = currentSubscriptionTimes,
            qibTimes = qibTimes,
            niiTimes = niiTimes,
            retailTimes = retailTimes,
            listingPrice = listingPrice,
            listingGainPercent = listingGainPercent,
            currentMarketPrice = currentMarketPrice,
            currentReturnPercent = currentReturnPercent,
            description = description,
            sector = sector,
            listingExchanges = exchange,
            isDemoData = false,
            allotmentInfo = if (registrarName.isNotEmpty() || registrarUrl.isNotEmpty()) {
                AllotmentInfo(
                    registrarName = registrarName,
                    registrarUrl = registrarUrl,
                    allotmentDate = allotmentDate
                )
            } else null
        )
    }

    companion object {
        fun fromDomain(domain: IpoItem): IpoEntity = IpoEntity(
            id = domain.id,
            name = domain.name,
            symbol = domain.symbol,
            exchange = domain.listingExchanges,
            category = domain.category.name,
            status = domain.status.name,
            priceBandMin = domain.priceBandMin,
            priceBandMax = domain.priceBandMax,
            lotSize = domain.lotSize,
            minInvestment = domain.minInvestment,
            issueSizeCr = domain.issueSizeCr,
            freshIssueCr = domain.freshIssueCr,
            ofsCr = domain.ofsCr,
            openDate = domain.openDate,
            closeDate = domain.closeDate,
            allotmentDate = domain.allotmentDate,
            listingDate = domain.listingDate,
            currentGmp = domain.currentGmp,
            estimatedListingPrice = domain.estimatedListingPrice,
            estimatedGainPercent = domain.estimatedGainPercent,
            lastGmpUpdated = domain.lastGmpUpdated,
            currentSubscriptionTimes = domain.currentSubscriptionTimes,
            qibTimes = domain.qibTimes,
            niiTimes = domain.niiTimes,
            retailTimes = domain.retailTimes,
            listingPrice = domain.listingPrice,
            listingGainPercent = domain.listingGainPercent,
            currentMarketPrice = domain.currentMarketPrice,
            currentReturnPercent = domain.currentReturnPercent,
            description = domain.description,
            sector = domain.sector,
            registrarName = domain.allotmentInfo?.registrarName ?: "",
            registrarUrl = domain.allotmentInfo?.registrarUrl ?: "",
            officialExchangeUrl = if (domain.listingExchanges.contains("BSE")) {
                "https://www.bseindia.com/markets/publicissues/ipoissues.aspx?id=1&type=pso"
            } else {
                "https://www.nseindia.com/market-data/all-upcoming-issues-ipo"
            },
            lastSyncedAt = System.currentTimeMillis()
        )
    }
}
