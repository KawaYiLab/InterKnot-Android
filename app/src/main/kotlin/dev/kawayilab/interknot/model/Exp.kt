package dev.kawayilab.interknot.model

import kotlinx.serialization.Serializable

@Serializable
data class ExpInfo(
    val exp: Int = 0,
    val level: Int = 1,
    val lastCheckInDate: String? = null,
    val consecutiveCheckInDays: Int = 0,
    val canCheckIn: Boolean = false,
    val nextEligibleAt: String? = null
)

@Serializable
data class DailyExp(
    val todaySelfGained: Int = 0,
    val todaySelfCap: Int = 0,
    val sources: DailyExpSources = DailyExpSources()
)

@Serializable
data class DailyExpSources(
    val checkIn: DailyExpSource = DailyExpSource(),
    val createArticle: DailyExpSource = DailyExpSource(),
    val createComment: DailyExpSource = DailyExpSource(),
    val likeGive: DailyExpSource = DailyExpSource()
)

@Serializable
data class DailyExpSource(
    val done: Boolean = false,
    val exp: Int = 0
)
