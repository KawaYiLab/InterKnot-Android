package dev.kawayilab.interknot.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Delegation(
    val id: Int,
    val title: String,
    val content: String,
    val author: User? = null,
    @SerialName("cover_url")
    val coverUrl: String? = null,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
)
