package dev.kawayilab.interknot.model

import kotlinx.serialization.Serializable

@Serializable
data class ImageMeta(
    val url: String,
    val width: Int? = null,
    val height: Int? = null,
    val nsfwStatus: String? = null
)
