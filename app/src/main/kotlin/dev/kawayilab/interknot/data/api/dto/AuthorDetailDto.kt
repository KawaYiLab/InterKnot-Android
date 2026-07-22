package dev.kawayilab.interknot.data.api.dto

import dev.kawayilab.interknot.model.Author
import kotlinx.serialization.Serializable

@Serializable
data class AuthorDetailDto(
    val documentId: String? = null,
    val username: String? = null,
    val name: String? = null,
    val email: String? = null,
    val avatar: ImageMetaDto? = null,
    val level: Int? = null,
    val exp: Int? = null,
    val isAdmin: Boolean? = null,
    val examPassed: Boolean? = null
)

fun AuthorDetailDto.toDomain() = Author(
    documentId = documentId,
    username = username,
    name = name ?: username,
    email = email,
    avatarUrl = avatar?.url,
    avatarWidth = avatar?.width,
    avatarHeight = avatar?.height,
    level = level,
    exp = exp,
    isAdmin = isAdmin ?: false,
    examPassed = examPassed
)
