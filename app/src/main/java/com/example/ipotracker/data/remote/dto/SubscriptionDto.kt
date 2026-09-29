package com.example.ipotracker.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubscriptionRowDto(
    @Json(name = "category") val category: String? = null,
    @Json(name = "offeredShares") val offeredSharesCamel: Long? = null,
    @Json(name = "offered_shares") val offeredSharesSnake: Long? = null,
    @Json(name = "appliedShares") val appliedSharesCamel: Long? = null,
    @Json(name = "applied_shares") val appliedSharesSnake: Long? = null,
    @Json(name = "times") val times: Double? = null
) {
    val offeredShares: Long? get() = offeredSharesCamel ?: offeredSharesSnake
    val appliedShares: Long? get() = appliedSharesCamel ?: appliedSharesSnake
}

@JsonClass(generateAdapter = true)
data class SubscriptionDayProgressDto(
    @Json(name = "dayLabel") val dayLabelCamel: String? = null,
    @Json(name = "day_label") val dayLabelSnake: String? = null,
    @Json(name = "date") val date: String? = null,
    @Json(name = "overallTimes") val overallTimesCamel: Double? = null,
    @Json(name = "overall_times") val overallTimesSnake: Double? = null,
    @Json(name = "qibTimes") val qibTimesCamel: Double? = null,
    @Json(name = "qib_times") val qibTimesSnake: Double? = null,
    @Json(name = "niiTimes") val niiTimesCamel: Double? = null,
    @Json(name = "nii_times") val niiTimesSnake: Double? = null,
    @Json(name = "retailTimes") val retailTimesCamel: Double? = null,
    @Json(name = "retail_times") val retailTimesSnake: Double? = null,
    @Json(name = "employeeTimes") val employeeTimesCamel: Double? = null,
    @Json(name = "employee_times") val employeeTimesSnake: Double? = null,
    @Json(name = "otherTimes") val otherTimesCamel: Double? = null,
    @Json(name = "other_times") val otherTimesSnake: Double? = null
) {
    val dayLabel: String? get() = dayLabelCamel ?: dayLabelSnake
    val overallTimes: Double? get() = overallTimesCamel ?: overallTimesSnake
    val qibTimes: Double? get() = qibTimesCamel ?: qibTimesSnake
    val niiTimes: Double? get() = niiTimesCamel ?: niiTimesSnake
    val retailTimes: Double? get() = retailTimesCamel ?: retailTimesSnake
    val employeeTimes: Double? get() = employeeTimesCamel ?: employeeTimesSnake
    val otherTimes: Double? get() = otherTimesCamel ?: otherTimesSnake
}

@JsonClass(generateAdapter = true)
data class SubscriptionDetailsDto(
    @Json(name = "overallTimes") val overallTimesCamel: Double? = null,
    @Json(name = "overall_times") val overallTimesSnake: Double? = null,
    @Json(name = "qibTimes") val qibTimesCamel: Double? = null,
    @Json(name = "qib_times") val qibTimesSnake: Double? = null,
    @Json(name = "niiTimes") val niiTimesCamel: Double? = null,
    @Json(name = "nii_times") val niiTimesSnake: Double? = null,
    @Json(name = "retailTimes") val retailTimesCamel: Double? = null,
    @Json(name = "retail_times") val retailTimesSnake: Double? = null,
    @Json(name = "employeeTimes") val employeeTimesCamel: Double? = null,
    @Json(name = "employee_times") val employeeTimesSnake: Double? = null,
    @Json(name = "otherTimes") val otherTimesCamel: Double? = null,
    @Json(name = "other_times") val otherTimesSnake: Double? = null,
    @Json(name = "categoryRows") val categoryRowsCamel: List<SubscriptionRowDto>? = null,
    @Json(name = "category_rows") val categoryRowsSnake: List<SubscriptionRowDto>? = null,
    @Json(name = "dayProgress") val dayProgressCamel: List<SubscriptionDayProgressDto>? = null,
    @Json(name = "day_progress") val dayProgressSnake: List<SubscriptionDayProgressDto>? = null,
    @Json(name = "lastUpdated") val lastUpdatedCamel: String? = null,
    @Json(name = "last_updated") val lastUpdatedSnake: String? = null
) {
    val overallTimes: Double? get() = overallTimesCamel ?: overallTimesSnake
    val qibTimes: Double? get() = qibTimesCamel ?: qibTimesSnake
    val niiTimes: Double? get() = niiTimesCamel ?: niiTimesSnake
    val retailTimes: Double? get() = retailTimesCamel ?: retailTimesSnake
    val employeeTimes: Double? get() = employeeTimesCamel ?: employeeTimesSnake
    val otherTimes: Double? get() = otherTimesCamel ?: otherTimesSnake
    val categoryRows: List<SubscriptionRowDto>? get() = categoryRowsCamel ?: categoryRowsSnake
    val dayProgress: List<SubscriptionDayProgressDto>? get() = dayProgressCamel ?: dayProgressSnake
    val lastUpdated: String? get() = lastUpdatedCamel ?: lastUpdatedSnake
}
