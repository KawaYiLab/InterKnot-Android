package dev.kawayilab.interknot.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val documentId: String? = null,
    val name: String? = null,
    val slug: String? = null,
    val order: Int? = null,
    val adminOnly: Boolean = false
)
