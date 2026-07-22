package dev.kawayilab.interknot.data.api.dto

import dev.kawayilab.interknot.model.ImageMeta
import dev.kawayilab.interknot.model.KnockArticleRef
import dev.kawayilab.interknot.model.KnockCommentRef
import dev.kawayilab.interknot.model.KnockConversation
import dev.kawayilab.interknot.model.KnockNotification
import dev.kawayilab.interknot.model.KnockSender
import dev.kawayilab.interknot.model.KnockSenderAuthor
import kotlinx.serialization.Serializable

@Serializable
data class KnockConversationDto(
    val id: String,
    val category: String,
    val peerKey: String,
    val peerName: String,
    val peerAvatar: String? = null,
    val unread: Int = 0,
    val lastPreview: String = "",
    val lastAt: String? = null,
    val lastType: String = ""
)

fun KnockConversationDto.toDomain() = KnockConversation(
    id = id,
    category = category,
    peerKey = peerKey,
    peerName = peerName,
    peerAvatar = peerAvatar,
    unread = unread,
    lastPreview = lastPreview,
    lastAt = lastAt,
    lastType = lastType
)

@Serializable
data class KnockNotificationDto(
    val documentId: String,
    val type: String,
    val rawType: String? = null,
    val isRead: Boolean = false,
    val createdAt: String? = null,
    val message: String? = null,
    val sender: KnockSenderDto? = null,
    val article: KnockArticleRefDto? = null,
    val comment: KnockCommentRefDto? = null
)

fun KnockNotificationDto.toDomain() = KnockNotification(
    documentId = documentId,
    type = type,
    rawType = rawType,
    isRead = isRead,
    createdAt = createdAt,
    message = message,
    sender = sender?.toDomain(),
    article = article?.toDomain(),
    comment = comment?.toDomain()
)

@Serializable
data class KnockSenderDto(
    val id: Int? = null,
    val username: String? = null,
    val level: Int? = null,
    val author: KnockSenderAuthorDto? = null
)

fun KnockSenderDto.toDomain() = KnockSender(
    id = id?.toString(),
    username = username,
    level = level,
    author = author?.toDomain()
)

@Serializable
data class KnockSenderAuthorDto(
    val documentId: String? = null,
    val name: String? = null,
    val avatar: ImageMetaDto? = null
)

fun KnockSenderAuthorDto.toDomain() = KnockSenderAuthor(
    documentId = documentId,
    name = name,
    avatarUrl = avatar?.url
)

@Serializable
data class KnockArticleRefDto(
    val documentId: String,
    val title: String,
    val coverAspectRatio: Double? = null
)

fun KnockArticleRefDto.toDomain() = KnockArticleRef(
    documentId = documentId,
    title = title,
    coverAspectRatio = coverAspectRatio
)

@Serializable
data class KnockCommentRefDto(
    val documentId: String,
    val content: String,
    val images: List<ImageMetaDto> = emptyList(),
    val isAnonymous: Boolean = false
)

fun KnockCommentRefDto.toDomain() = KnockCommentRef(
    documentId = documentId,
    content = content,
    images = images.map { it.toDomain() },
    isAnonymous = isAnonymous
)

@Serializable
data class MarkReadResultDto(
    val success: Boolean = false,
    val updated: Int? = null
)

@Serializable
data class UnreadCountDto(
    val count: Int = 0
)

@Serializable
data class KnockMessagePageDto(
    val data: List<KnockNotificationDto> = emptyList(),
    val meta: KnockMessageMetaDto = KnockMessageMetaDto()
)

@Serializable
data class KnockMessageMetaDto(
    val nextCursor: String? = null,
    val hasMore: Boolean = false
)

fun KnockMessagePageDto.toDomain() = dev.kawayilab.interknot.model.KnockMessagePage(
    items = data.map { it.toDomain() },
    nextCursor = meta.nextCursor,
    hasMore = meta.hasMore
)
