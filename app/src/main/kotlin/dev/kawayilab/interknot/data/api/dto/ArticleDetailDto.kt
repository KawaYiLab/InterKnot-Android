package dev.kawayilab.interknot.data.api.dto

import dev.kawayilab.interknot.model.Article
import kotlinx.serialization.Serializable

@Serializable
data class ArticleDetailDto(
    val documentId: String,
    val title: String,
    val text: String? = null,
    val cover: List<ImageMetaDto>? = null,
    val views: Int = 0,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val dennyCount: Int = 0,
    val favoritesCount: Int = 0,
    val liked: Boolean = false,
    val favorited: Boolean = false,
    val hasGivenDenny: Boolean = false,
    val isRead: Boolean = false,
    val isAnonymous: Boolean = false,
    val isHidden: Boolean = false,
    val isOwner: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val editedAt: String? = null,
    val publishedAt: String? = null,
    val editorState: String? = null,
    val author: AuthorDetailDto? = null,
    val category: CategoryDto? = null
)

fun ArticleDetailDto.toDomain() = Article(
    documentId = documentId,
    title = title,
    text = text,
    coverUrl = cover?.firstOrNull()?.url,
    coverImages = cover?.map { it.toDomain() } ?: emptyList(),
    views = views,
    likesCount = likesCount,
    commentsCount = commentsCount,
    dennyCount = dennyCount,
    favoritesCount = favoritesCount,
    liked = liked,
    favorited = favorited,
    hasGivenDenny = hasGivenDenny,
    isRead = isRead,
    isAnonymous = isAnonymous,
    isHidden = isHidden,
    isOwner = isOwner,
    createdAt = createdAt,
    updatedAt = updatedAt,
    editedAt = editedAt,
    publishedAt = publishedAt,
    editorState = editorState,
    author = author?.toDomain(),
    category = category?.toDomain()
)
