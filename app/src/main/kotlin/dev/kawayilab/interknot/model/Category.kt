package dev.kawayilab.interknot.model

data class Category(
    val documentId: String? = null,
    val name: String? = null,
    val slug: String? = null,
    val order: Int? = null,
    val adminOnly: Boolean = false
)
