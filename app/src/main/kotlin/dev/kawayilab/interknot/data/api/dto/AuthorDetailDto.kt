package dev.kawayilab.interknot.data.api.dto

import dev.kawayilab.interknot.model.Author
import kotlinx.serialization.Serializable

@Serializable
data class AuthorDetailDto(
    val documentId: String? = null,
    val name: String? = null,
    val avatar: ImageMetaDto? = null,
    val level: Int? = null
)

fun AuthorDetailDto.toDomain() = Author(
    documentId = documentId,
    name = name,
    avatarUrl = avatar?.url,
    avatarWidth = avatar?.width,
    avatarHeight = avatar?.height,
    level = level
)
