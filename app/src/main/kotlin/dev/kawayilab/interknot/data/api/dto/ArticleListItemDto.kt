package dev.kawayilab.interknot.data.api.dto

import dev.kawayilab.interknot.model.Article
import dev.kawayilab.interknot.model.ImageMeta
import kotlinx.serialization.Serializable

@Serializable
data class ArticleListItemDto(
    val documentId: String,
    val title: String,
    val cover: String? = null,
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

fun ArticleListItemDto.toDomain() = Article(
    documentId = documentId,
    title = title,
    coverUrl = cover,
    coverWidth = coverWidth,
    coverHeight = coverHeight,
    coverImages = if (cover != null) listOf(
        ImageMeta(
            url = cover,
            width = coverWidth,
            height = coverHeight,
            nsfwStatus = coverNsfwStatus
        )
    ) else emptyList(),
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
