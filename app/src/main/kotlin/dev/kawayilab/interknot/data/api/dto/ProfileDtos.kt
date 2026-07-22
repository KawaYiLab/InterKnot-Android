package dev.kawayilab.interknot.data.api.dto

import dev.kawayilab.interknot.model.Author
import dev.kawayilab.interknot.model.Profile
import dev.kawayilab.interknot.model.ProfileStats
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class ProfileUserDto(
    val username: String? = null,
    val level: Int? = null,
    val exp: Int? = null
)

@Serializable
data class ProfileStatsDto(
    val articleCount: Int = 0,
    val commentCount: Int = 0,
    val totalViews: Int = 0,
    val totalComments: Int = 0,
    val totalLikes: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0
)

@Serializable
data class EquippedCardDto(
    val documentId: String? = null,
    val name: String? = null,
    val description: String? = null,
    val type: String? = null,
    val image: ImageMetaDto? = null
)

@Serializable
data class EquippedAvatarDto(
    val documentId: String? = null,
    val name: String? = null,
    val type: String? = null,
    val image: ImageMetaDto? = null
)

@Serializable
data class ProfileDataDto(
    val userId: Int? = null,
    val documentId: String? = null,
    val name: String? = null,
    val bio: List<JsonObject>? = null,
    val avatar: JsonElement? = null,
    val user: ProfileUserDto? = null,
    val equippedCard: EquippedCardDto? = null,
    val equippedAvatar: EquippedAvatarDto? = null,
    val isSelf: Boolean = false,
    val isHidden: Boolean = false,
    val profileHidden: Boolean = false,
    val isAiAgent: Boolean = false,
    val isBlockedByMe: Boolean = false,
    val hasBlockedMe: Boolean = false,
    val isFollowing: Boolean = false,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val stats: ProfileStatsDto? = null
)

@Serializable
data class AuthorSearchItemDto(
    val documentId: String,
    val name: String? = null,
    val username: String? = null,
    val level: Int? = null,
    val avatar: String? = null
)

private fun JsonElement?.extractImageUrl(): String? = when (this) {
    is JsonObject -> this["url"]?.jsonPrimitive?.content
    is JsonPrimitive -> if (this == JsonNull) null else content
    else -> null
}

private fun ProfileStatsDto?.toDomain() = this?.let {
    ProfileStats(
        articleCount = articleCount,
        commentCount = commentCount,
        totalViews = totalViews,
        totalComments = totalComments,
        totalLikes = totalLikes,
        followersCount = followersCount,
        followingCount = followingCount
    )
} ?: ProfileStats()

fun ProfileDataDto.toDomain() = Profile(
    author = Author(
        documentId = documentId,
        username = user?.username,
        name = name,
        avatarUrl = avatar.extractImageUrl(),
        avatarWidth = (avatar as? JsonObject)?.get("width")?.jsonPrimitive?.content?.toIntOrNull(),
        avatarHeight = (avatar as? JsonObject)?.get("height")?.jsonPrimitive?.content?.toIntOrNull(),
        level = user?.level,
        exp = user?.exp,
        bio = null,
        isFollowing = isFollowing,
        followersCount = followersCount,
        followingCount = followingCount,
        isSelf = isSelf,
        isBlockedByMe = isBlockedByMe,
        hasBlockedMe = hasBlockedMe,
        isHidden = isHidden,
        profileHidden = profileHidden,
        isAiAgent = isAiAgent,
        equippedCard = equippedCard?.toDomain(),
        equippedAvatar = equippedAvatar?.toDomain()
    ),
    stats = stats.toDomain(),
    isFollowing = isFollowing,
    isSelf = isSelf,
    isBlocked = isBlockedByMe || hasBlockedMe
)

fun EquippedCardDto.toDomain() = dev.kawayilab.interknot.model.BusinessCard(
    documentId = documentId,
    name = name,
    description = description,
    type = type,
    image = image?.toDomain()
)

fun EquippedAvatarDto.toDomain() = dev.kawayilab.interknot.model.Avatar(
    documentId = documentId,
    name = name,
    type = type,
    image = image?.toDomain()
)

fun AuthorSearchItemDto.toDomain() = Author(
    documentId = documentId,
    username = username,
    name = name,
    avatarUrl = avatar,
    level = level
)
