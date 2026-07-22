package dev.kawayilab.interknot.data.api.dto

import dev.kawayilab.interknot.model.DennyBalance
import dev.kawayilab.interknot.model.DennyGiveResult
import dev.kawayilab.interknot.model.DennyLog
import kotlinx.serialization.Serializable

@Serializable
data class DennyBalanceDto(
    val denny: Int = 0,
    val dennyGiven: Int = 0,
    val recentLogs: List<DennyLogDto> = emptyList()
)

@Serializable
data class DennyLogDto(
    val action: String? = null,
    val amount: Int? = null,
    val balance: Int? = null,
    val description: String? = null,
    val createdAt: String? = null
)

@Serializable
data class DennyGiveResponseDto(
    val success: Boolean = false,
    val message: String? = null,
    val newBalance: Int? = null,
    val articleDennyCount: Int? = null
)

fun DennyLogDto.toDomain() = DennyLog(
    action = action,
    amount = amount,
    balance = balance,
    description = description,
    createdAt = createdAt
)

fun DennyBalanceDto.toDomain() = DennyBalance(
    denny = denny,
    dennyGiven = dennyGiven,
    recentLogs = recentLogs.map { it.toDomain() }
)

fun DennyGiveResponseDto.toDomain() = DennyGiveResult(
    success = success,
    message = message,
    newBalance = newBalance,
    articleDennyCount = articleDennyCount
)
