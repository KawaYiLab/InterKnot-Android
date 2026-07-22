package dev.kawayilab.interknot.data.api.dto

import dev.kawayilab.interknot.model.ImageMeta
import kotlinx.serialization.Serializable

@Serializable
 data class ImageMetaDto(
    val url: String,
    val width: Int? = null,
    val height: Int? = null,
    val nsfwStatus: String? = null
)

fun ImageMetaDto.toDomain() = ImageMeta(
    url = url,
    width = width,
    height = height,
    nsfwStatus = nsfwStatus
)
