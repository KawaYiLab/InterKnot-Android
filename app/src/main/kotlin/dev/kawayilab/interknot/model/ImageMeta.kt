package dev.kawayilab.interknot.model

data class ImageMeta(
    val url: String,
    val width: Int? = null,
    val height: Int? = null,
    val nsfwStatus: String? = null
)
