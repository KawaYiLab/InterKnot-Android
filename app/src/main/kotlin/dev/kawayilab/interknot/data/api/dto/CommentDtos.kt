package dev.kawayilab.interknot.data.api.dto

import dev.kawayilab.interknot.model.Author
import dev.kawayilab.interknot.model.Comment
import dev.kawayilab.interknot.model.CommentPage
import dev.kawayilab.interknot.model.ImageMeta
import kotlinx.serialization.Serializable

@Serializable
data class CommentAuthorDto(
    val id: Int? = null,
    val documentId: String? = null,
    val name: String? = null,
    val login: String? = null,
    val avatar: ImageMetaDto? = null,
    val level: Int? = null,
    val isAiAgent: Boolean? = null
)

@Serializable
data class CommentImageDto(
    val url: String,
    val width: Int? = null,
    val height: Int? = null,
    val nsfwStatus: String? = null
)

@Serializable
data class CommentDto(
    val documentId: String,
    val content: String? = null,
    val images: List<CommentImageDto>? = null,
    val liked: Boolean? = null,
    val likesCount: Int? = null,
    val createdAt: String? = null,
    val author: CommentAuthorDto? = null,
    val article: ArticleRefDto? = null,
    val replies: List<CommentDto>? = null,
    val isPinned: Boolean? = null,
    val pinnedAt: String? = null,
    val floor: Int? = null
)

@Serializable
data class CommentListResponseDto(
    val data: List<CommentDto> = emptyList(),
    val meta: CommentListMetaDto = CommentListMetaDto(),
    val pinned: CommentDto? = null
)

@Serializable
data class CommentListMetaDto(
    val pagination: PaginationDto = PaginationDto()
)

@Serializable
data class LikeResultDto(
    val liked: Boolean = false,
    val likesCount: Int = 0
)

private fun CommentAuthorDto.toDomain() = Author(
    documentId = documentId,
    username = login,
    name = name ?: login,
    avatarUrl = avatar?.url,
    avatarWidth = avatar?.width,
    avatarHeight = avatar?.height,
    level = level
)

private fun CommentImageDto.toDomain() = ImageMeta(
    url = url,
    width = width,
    height = height,
    nsfwStatus = nsfwStatus
)

private fun CommentDto.toDomain(): Comment = Comment(
    documentId = documentId,
    content = content ?: "",
    images = images?.map { it.toDomain() } ?: emptyList(),
    liked = liked == true,
    likesCount = likesCount ?: 0,
    createdAt = createdAt,
    author = author?.toDomain(),
    article = article?.toDomain(),
    replies = replies?.map { it.toDomain() } ?: emptyList(),
    isPinned = isPinned == true,
    pinnedAt = pinnedAt,
    floor = floor
)

fun CommentListResponseDto.toDomain(start: Int): CommentPage {
    val dataItems = data.map { it.toDomain() }
    val pinnedItem = pinned?.toDomain()
    val pagination = meta.pagination
    return CommentPage(
        items = dataItems,
        pinned = pinnedItem,
        start = pagination.start,
        limit = pagination.limit,
        total = pagination.total,
        hasMore = dataItems.isNotEmpty() && (start + dataItems.size) < pagination.total
    )
}

fun LikeResultDto.toDomain() = dev.kawayilab.interknot.model.LikeResult(
    liked = liked,
    likesCount = likesCount
)
