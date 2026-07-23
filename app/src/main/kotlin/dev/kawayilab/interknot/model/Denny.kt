package dev.kawayilab.interknot.model

data class DennyBalance(
    val denny: Int,
    val dennyGiven: Int,
    val recentLogs: List<DennyLog> = emptyList()
)

data class DennyLog(
    val action: String? = null,
    val amount: Int? = null,
    val balance: Int? = null,
    val description: String? = null,
    val createdAt: String? = null
)

data class DennyGiveResult(
    val success: Boolean,
    val message: String? = null,
    val newBalance: Int? = null,
    val articleDennyCount: Int? = null
)
