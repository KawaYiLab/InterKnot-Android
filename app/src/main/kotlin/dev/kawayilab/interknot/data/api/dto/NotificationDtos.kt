package dev.kawayilab.interknot.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotificationReadResultDto(
    val success: Boolean = false,
    val documentId: String? = null
)

@Serializable
data class MarkAllReadResultDto(
    val success: Boolean = false,
    val updated: Int? = null
)
