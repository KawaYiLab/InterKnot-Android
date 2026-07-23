package dev.kawayilab.interknot.data.api.dto

import dev.kawayilab.interknot.model.CheckInResult
import dev.kawayilab.interknot.model.CheckInStatus
import kotlinx.serialization.Serializable

@Serializable
data class CheckInStatusDto(
    val canCheckIn: Boolean = false,
    val totalDays: Int = 0,
    val consecutiveDays: Int = 0,
    val rank: Int = 0,
    val checkInDay: String? = null,
    val nextEligibleAt: String? = null,
    val currentDenny: Int = 0
)

@Serializable
data class CheckInResultDto(
    val message: String,
    val reward: Int = 0,
    val consecutiveDays: Int = 0,
    val totalDays: Int = 0,
    val rank: Int = 0,
    val currentExp: Int? = null,
    val currentLevel: Int? = null,
    val currentDenny: Int = 0,
    val dennyAdded: Int = 0,
    val dennyCapped: Boolean = false,
    val nextEligibleAt: String? = null
)

fun CheckInStatusDto.toDomain() = CheckInStatus(
    canCheckIn = canCheckIn,
    totalDays = totalDays,
    consecutiveDays = consecutiveDays,
    rank = rank,
    checkInDay = checkInDay,
    nextEligibleAt = nextEligibleAt,
    currentDenny = currentDenny
)

fun CheckInResultDto.toDomain() = CheckInResult(
    message = message,
    reward = reward,
    consecutiveDays = consecutiveDays,
    totalDays = totalDays,
    rank = rank,
    currentExp = currentExp,
    currentLevel = currentLevel,
    currentDenny = currentDenny,
    dennyAdded = dennyAdded,
    dennyCapped = dennyCapped,
    nextEligibleAt = nextEligibleAt
)
