package dev.kawayilab.interknot.data.api.dto

import dev.kawayilab.interknot.model.Article
import dev.kawayilab.interknot.model.ImageMeta
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class ArticleListItemDto(
    val documentId: String,
    val title: String,
    val cover: JsonElement? = null,
    val coverWidth: Int? = null,
    val coverHeight: Int? = null,
    val views: Int = 0,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val dennyCount: Int = 0,
    val favoritesCount: Int = 0,
    val liked: Boolean = false,
    val favorited: Boolean = false,
    val isRead: Boolean = false,
    val isAnonymous: Boolean = false,
    val author: AuthorDto? = null,
    val category: CategoryDto? = null,
    val coverNsfwStatus: String? = null,
    val hasPublishedVersion: Boolean? = null
)

@Serializable
data class ArticleDraftItemDto(
    val documentId: String,
    val title: String,
    val text: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

fun ArticleDraftItemDto.toDomain() = Article(
    documentId = documentId,
    title = title,
    text = text,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ArticleListItemDto.toDomain(): Article {
    val coverMeta = cover.toImageMeta(coverWidth, coverHeight, coverNsfwStatus)
    return Article(
        documentId = documentId,
        title = title,
        coverUrl = coverMeta?.url,
        coverWidth = coverMeta?.width,
        coverHeight = coverMeta?.height,
        coverNsfwStatus = coverMeta?.nsfwStatus,
        coverImages = listOfNotNull(coverMeta),
        views = views,
        likesCount = likesCount,
        commentsCount = commentsCount,
        dennyCount = dennyCount,
        favoritesCount = favoritesCount,
        liked = liked,
        favorited = favorited,
        isRead = isRead,
        isAnonymous = isAnonymous,
        hasPublishedVersion = hasPublishedVersion == true,
        author = author?.toDomain(),
        category = category?.toDomain()
    )
}

private fun JsonElement?.toImageMeta(
    fallbackWidth: Int? = null,
    fallbackHeight: Int? = null,
    fallbackNsfw: String? = null
): ImageMeta? {
    if (this == null) return null
    val rawUrl = when (this) {
        is JsonPrimitive -> if (this == JsonNull) null else content
        is JsonObject -> this["url"]?.jsonPrimitive?.content
        else -> null
    }
    val url = rawUrl?.takeIf { it.isNotBlank() } ?: return null
    val width = if (this is JsonObject) {
        this["width"]?.jsonPrimitive?.content?.toIntOrNull() ?: fallbackWidth
    } else fallbackWidth
    val height = if (this is JsonObject) {
        this["height"]?.jsonPrimitive?.content?.toIntOrNull() ?: fallbackHeight
    } else fallbackHeight
    val nsfwStatus = if (this is JsonObject) {
        this["nsfwStatus"]?.jsonPrimitive?.content ?: fallbackNsfw
    } else fallbackNsfw
    return ImageMeta(url = url, width = width, height = height, nsfwStatus = nsfwStatus)
}
