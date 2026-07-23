package dev.kawayilab.interknot.model

import kotlinx.serialization.Serializable

@Serializable
data class CheckInStatus(
    val canCheckIn: Boolean = false,
    val totalDays: Int = 0,
    val consecutiveDays: Int = 0,
    val rank: Int = 0,
    val checkInDay: String? = null,
    val nextEligibleAt: String? = null,
    val currentDenny: Int = 0
)

@Serializable
data class CheckInResult(
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
