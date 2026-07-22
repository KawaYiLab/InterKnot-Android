package dev.kawayilab.interknot.model

import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: String,
    val title: String,
    val content: String,
    val author: User? = null
)
