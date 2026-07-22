package dev.kawayilab.interknot.data.api.dto

import dev.kawayilab.interknot.model.Author
import kotlinx.serialization.Serializable

@Serializable
data class AuthorDto(
    val documentId: String? = null,
    val name: String? = null,
    val avatar: String? = null,
    val level: Int? = null
)

fun AuthorDto.toDomain() = Author(
    documentId = documentId,
    name = name,
    avatarUrl = avatar,
    level = level
)
