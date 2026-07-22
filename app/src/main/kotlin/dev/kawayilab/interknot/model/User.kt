package dev.kawayilab.interknot.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String? = null,
    val documentId: String? = null,
    val authorDocumentId: String? = null,
    val username: String? = null,
    val email: String? = null,
    val name: String? = null,
    val avatarUrl: String? = null,
    val level: Int? = null,
    val exp: Int? = null,
    val isAdmin: Boolean = false,
    val examPassed: Boolean? = null,
    val examPassedAt: String? = null,
    val profileHidden: Boolean = false,
    val consecutiveCheckInDays: Int = 0,
    val lastCheckInDate: String? = null
)
