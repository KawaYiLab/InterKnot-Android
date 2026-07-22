package dev.kawayilab.interknot.data.api.dto

import dev.kawayilab.interknot.model.Author
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class AuthorDto(
    val documentId: String? = null,
    val username: String? = null,
    val name: String? = null,
    val email: String? = null,
    val avatar: JsonElement? = null,
    val level: Int? = null,
    val exp: Int? = null,
    val isAdmin: Boolean? = null,
    val examPassed: Boolean? = null
)

fun AuthorDto.toDomain() = Author(
    documentId = documentId,
    username = username,
    name = name ?: username,
    email = email,
    avatarUrl = avatar.extractImageUrl(),
    avatarWidth = (avatar as? JsonObject)?.get("width")?.jsonPrimitive?.content?.toIntOrNull(),
    avatarHeight = (avatar as? JsonObject)?.get("height")?.jsonPrimitive?.content?.toIntOrNull(),
    level = level,
    exp = exp,
    isAdmin = isAdmin ?: false,
    examPassed = examPassed
)

private fun JsonElement?.extractImageUrl(): String? = when (this) {
    is JsonObject -> this["url"]?.jsonPrimitive?.content
    is JsonPrimitive -> if (this == JsonNull) null else content
    else -> null
}
