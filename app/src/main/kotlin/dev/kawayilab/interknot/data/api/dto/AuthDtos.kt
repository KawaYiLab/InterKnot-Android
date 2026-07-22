package dev.kawayilab.interknot.data.api.dto

import dev.kawayilab.interknot.model.AuthResult
import dev.kawayilab.interknot.model.User
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class AuthResponseDto(
    val jwt: String,
    val user: UserDto
)

@Serializable
data class UserAuthorDto(
    val documentId: String? = null,
    val name: String? = null,
    val avatar: JsonElement? = null
)

@Serializable
data class UserDto(
    val id: String? = null,
    val documentId: String? = null,
    val username: String? = null,
    val email: String? = null,
    val exp: Int? = null,
    val level: Int? = null,
    val isAdmin: Boolean? = null,
    val examPassed: Boolean? = null,
    val examPassedAt: String? = null,
    val profileHidden: Boolean? = null,
    val name: String? = null,
    val avatar: JsonElement? = null,
    val author: UserAuthorDto? = null
)

@Serializable
data class CodeResultDto(
    val email: String? = null,
    val sent: Boolean? = null,
    val expiresIn: Int? = null,
    val cooldown: Int? = null
)

private fun JsonElement?.extractImageUrl(): String? = when (this) {
    is JsonObject -> this["url"]?.jsonPrimitive?.content
    is JsonPrimitive -> if (this == JsonNull) null else content
    else -> null
}

fun UserDto.toDomain(): User {
    val authorName = author?.name
    val authorAvatar = author?.avatar.extractImageUrl()
    return User(
        id = id,
        documentId = documentId,
        authorDocumentId = author?.documentId,
        username = username,
        email = email,
        name = name ?: authorName ?: username,
        avatarUrl = avatar.extractImageUrl() ?: authorAvatar,
        level = level,
        exp = exp,
        isAdmin = isAdmin ?: false,
        examPassed = examPassed,
        examPassedAt = examPassedAt,
        profileHidden = profileHidden ?: false
    )
}

fun AuthResponseDto.toDomain() = AuthResult(token = jwt, user = user.toDomain())

@Serializable
data class ResetPasswordResultDto(val success: Boolean = false)

@Serializable
data class RenewTokenResponseDto(val jwt: String)

fun CodeResultDto.toDomain(): Pair<Boolean, Int> {
    val cooldownValue = cooldown ?: expiresIn ?: 60
    return true to cooldownValue
}
