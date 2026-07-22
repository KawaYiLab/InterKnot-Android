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
    val isAnonymous: Boolean = false,
    val author: AuthorDto? = null,
    val category: CategoryDto? = null,
    val coverNsfwStatus: String? = null
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
    isAnonymous = isAnonymous,
    author = author?.toDomain(),
    category = category?.toDomain()
)
