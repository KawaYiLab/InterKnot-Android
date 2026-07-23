package dev.kawayilab.interknot.data.api.dto

import dev.kawayilab.interknot.model.DailyExp
import dev.kawayilab.interknot.model.DailyExpSource
import dev.kawayilab.interknot.model.DailyExpSources
import dev.kawayilab.interknot.model.ExpInfo
import kotlinx.serialization.Serializable

@Serializable
data class ExpInfoDto(
    val exp: Int = 0,
    val level: Int = 1,
    val lastCheckInDate: String? = null,
    val consecutiveCheckInDays: Int = 0,
    val canCheckIn: Boolean = false,
    val nextEligibleAt: String? = null
)

@Serializable
data class DailyExpSourceDto(
    val done: Boolean = false,
    val exp: Int = 0
)

@Serializable
data class DailyExpSourcesDto(
    val checkIn: DailyExpSourceDto = DailyExpSourceDto(),
    val createArticle: DailyExpSourceDto = DailyExpSourceDto(),
    val createComment: DailyExpSourceDto = DailyExpSourceDto(),
    val likeGive: DailyExpSourceDto = DailyExpSourceDto()
)

@Serializable
data class DailyExpDto(
    val todaySelfGained: Int = 0,
    val todaySelfCap: Int = 0,
    val sources: DailyExpSourcesDto = DailyExpSourcesDto()
)

fun DailyExpSourceDto.toDomain() = DailyExpSource(done = done, exp = exp)
fun DailyExpSourcesDto.toDomain() = DailyExpSources(
    checkIn = checkIn.toDomain(),
    createArticle = createArticle.toDomain(),
    createComment = createComment.toDomain(),
    likeGive = likeGive.toDomain()
)

fun ExpInfoDto.toDomain() = ExpInfo(
    exp = exp,
    level = level,
    lastCheckInDate = lastCheckInDate,
    consecutiveCheckInDays = consecutiveCheckInDays,
    canCheckIn = canCheckIn,
    nextEligibleAt = nextEligibleAt
)

fun DailyExpDto.toDomain() = DailyExp(
    todaySelfGained = todaySelfGained,
    todaySelfCap = todaySelfCap,
    sources = sources.toDomain()
)
