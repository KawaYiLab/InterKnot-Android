package dev.kawayilab.interknot.data.api.dto

import dev.kawayilab.interknot.model.ArticleRef
import dev.kawayilab.interknot.model.Author
import dev.kawayilab.interknot.model.CommentRef
import dev.kawayilab.interknot.model.FavoriteRecord
import dev.kawayilab.interknot.model.FavoriteResult
import dev.kawayilab.interknot.model.FollowResult
import dev.kawayilab.interknot.model.LikeRecord
import dev.kawayilab.interknot.model.TripleResult
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteResultDto(
    val favorited: Boolean = false,
    val favoritesCount: Int = 0
)

@Serializable
data class FollowResultDto(
    val following: Boolean = false,
    val followersCount: Int = 0
)

@Serializable
data class TripleResultDto(
    val liked: Boolean = true,
    val likesCount: Int = 0,
    val favorited: Boolean = true,
    val favoritesCount: Int = 0,
    val coinGiven: Boolean = false,
    val coinReason: String? = null,
    val dennyCount: Int = 0,
    val newBalance: Int? = null
)

@Serializable
data class LikeCheckResultDto(val data: Map<String, Boolean> = emptyMap())

@Serializable
data class FavoriteCheckResultDto(val data: Map<String, Boolean> = emptyMap())

@Serializable
data class FollowCheckResultDto(val data: Map<String, Boolean> = emptyMap())

@Serializable
data class ArticleRefDto(
    val documentId: String? = null,
    val title: String? = null,
    val cover: String? = null,
    val updatedAt: String? = null,
    val likesCount: Int? = null
)

@Serializable
data class CommentRefDto(
    val documentId: String? = null,
    val content: String? = null,
    val createdAt: String? = null,
    val likesCount: Int = 0,
    val article: ArticleRefDto? = null
)

@Serializable
data class LikeListItemDto(
    val documentId: String? = null,
    val createdAt: String? = null,
    val targetType: String? = null,
    val article: ArticleRefDto? = null,
    val comment: CommentRefDto? = null
)

@Serializable
data class FavoriteListItemDto(
    val documentId: String? = null,
    val createdAt: String? = null,
    val article: ArticleRefDto? = null
)

@Serializable
data class FollowListItemDto(
    val documentId: String? = null,
    val name: String? = null,
    val avatar: String? = null
)

fun FavoriteResultDto.toDomain() = FavoriteResult(favorited = favorited, favoritesCount = favoritesCount)
fun FollowResultDto.toDomain() = FollowResult(following = following, followersCount = followersCount)
fun TripleResultDto.toDomain() = TripleResult(
    liked = liked,
    likesCount = likesCount,
    favorited = favorited,
    favoritesCount = favoritesCount,
    coinGiven = coinGiven,
    coinReason = coinReason,
    dennyCount = dennyCount,
    newBalance = newBalance
)

fun ArticleRefDto.toDomain() = ArticleRef(
    documentId = documentId ?: "",
    title = title,
    coverUrl = cover,
    updatedAt = updatedAt
)

fun CommentRefDto.toDomain() = CommentRef(
    documentId = documentId ?: "",
    content = content,
    createdAt = createdAt,
    likesCount = likesCount,
    article = article?.toDomain()
)

fun LikeListItemDto.toDomain() = LikeRecord(
    documentId = documentId,
    createdAt = createdAt,
    targetType = targetType,
    article = article?.toDomain(),
    comment = comment?.toDomain()
)

fun FavoriteListItemDto.toDomain() = FavoriteRecord(
    documentId = documentId,
    createdAt = createdAt,
    article = article?.toDomain()
)

fun FollowListItemDto.toDomain() = Author(
    documentId = documentId,
    name = name,
    avatarUrl = avatar
)
