package dev.kawayilab.interknot.data.api.dto

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
