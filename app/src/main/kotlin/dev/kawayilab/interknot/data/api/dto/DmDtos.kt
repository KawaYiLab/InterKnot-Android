package dev.kawayilab.interknot.data.api.dto

import dev.kawayilab.interknot.model.DmConversation
import dev.kawayilab.interknot.model.DmConversationDetail
import dev.kawayilab.interknot.model.DmLastMessage
import dev.kawayilab.interknot.model.DmMember
import dev.kawayilab.interknot.model.DmMessage
import dev.kawayilab.interknot.model.DmMessagePage
import dev.kawayilab.interknot.model.DmPeer
import dev.kawayilab.interknot.model.DmReplyTo
import dev.kawayilab.interknot.model.DmSelfMember
import dev.kawayilab.interknot.model.DmSender
import dev.kawayilab.interknot.model.DmSocketTicket
import kotlinx.serialization.Serializable

@Serializable
data class DmConversationDto(
    val documentId: String,
    val kind: String = "direct",
    val title: String? = null,
    val avatar: String? = null,
    val peer: DmPeerDto? = null,
    val memberCount: Int = 0,
    val lastMessageAt: String? = null,
    val lastMessage: DmLastMessageDto? = null,
    val unreadCount: Int = 0,
    val self: DmSelfMemberDto? = null
)

@Serializable
data class DmPeerDto(
    val userId: Int? = null,
    val authorDocumentId: String? = null,
    val name: String? = null,
    val avatar: String? = null,
    val level: Int? = null,
    val isAiAgent: Boolean = false
)

@Serializable
data class DmLastMessageDto(
    val documentId: String? = null,
    val content: String? = null,
    val createdAt: String? = null,
    val kind: String? = null,
    val senderUserId: Int? = null
)

@Serializable
data class DmSelfMemberDto(
    val role: String? = null,
    val muted: Boolean = false,
    val pinned: Boolean = false,
    val lastReadAt: String? = null
)

@Serializable
data class DmConversationDetailDto(
    val documentId: String,
    val kind: String = "direct",
    val title: String? = null,
    val avatar: String? = null,
    val ownerUserId: Int? = null,
    val lastMessageAt: String? = null,
    val self: DmSelfMemberDto? = null,
    val members: List<DmMemberDto> = emptyList()
)

@Serializable
data class DmMemberDto(
    val userId: Int? = null,
    val authorDocumentId: String? = null,
    val name: String? = null,
    val avatar: String? = null,
    val level: Int? = null,
    val role: String? = null,
    val isSelf: Boolean = false
)

@Serializable
data class DmMessageDto(
    val documentId: String,
    val kind: String = "text",
    val content: String? = null,
    val createdAt: String? = null,
    val editedAt: String? = null,
    val deletedAt: String? = null,
    val sender: DmSenderDto? = null,
    val replyTo: DmReplyToDto? = null
)

@Serializable
data class DmSenderDto(
    val userId: Int? = null,
    val authorDocumentId: String? = null,
    val name: String? = null,
    val avatar: String? = null,
    val level: Int? = null,
    val isAiAgent: Boolean = false
)

@Serializable
data class DmReplyToDto(
    val documentId: String? = null,
    val content: String? = null,
    val senderUserId: Int? = null
)

@Serializable
data class DmMessagePageDto(
    val data: List<DmMessageDto> = emptyList(),
    val meta: DmMessageMetaDto = DmMessageMetaDto()
)

@Serializable
data class DmMessageMetaDto(
    val hasMore: Boolean = false,
    val nextCursor: String? = null
)

@Serializable
data class DmSocketTicketDto(
    val ticket: String,
    val ttlSec: Int = 30
)

@Serializable
data class DmDirectResultDto(
    val data: DmConversationDto? = null,
    val isNew: Boolean = false
)

@Serializable
data class DmOperationResultDto(
    val documentId: String? = null,
    val content: String? = null,
    val deletedAt: String? = null,
    val editedAt: String? = null,
    val lastReadAt: String? = null,
    val ok: Boolean? = null
)

fun DmPeerDto.toDomain() = DmPeer(
    userId = userId,
    authorDocumentId = authorDocumentId,
    name = name,
    avatar = avatar,
    level = level,
    isAiAgent = isAiAgent
)

fun DmLastMessageDto.toDomain() = DmLastMessage(
    documentId = documentId,
    content = content,
    createdAt = createdAt,
    kind = kind,
    senderUserId = senderUserId
)

fun DmSelfMemberDto.toDomain() = DmSelfMember(
    role = role,
    muted = muted,
    pinned = pinned,
    lastReadAt = lastReadAt
)

fun DmConversationDto.toDomain() = DmConversation(
    documentId = documentId,
    kind = kind,
    title = title,
    avatar = avatar,
    peer = peer?.toDomain(),
    memberCount = memberCount,
    lastMessageAt = lastMessageAt,
    lastMessage = lastMessage?.toDomain(),
    unreadCount = unreadCount,
    self = self?.toDomain()
)

fun DmMemberDto.toDomain() = DmMember(
    userId = userId,
    authorDocumentId = authorDocumentId,
    name = name,
    avatar = avatar,
    level = level,
    role = role,
    isSelf = isSelf
)

fun DmConversationDetailDto.toDomain() = DmConversationDetail(
    documentId = documentId,
    kind = kind,
    title = title,
    avatar = avatar,
    ownerUserId = ownerUserId,
    lastMessageAt = lastMessageAt,
    self = self?.toDomain(),
    members = members.map { it.toDomain() }
)

fun DmSenderDto.toDomain() = DmSender(
    userId = userId,
    authorDocumentId = authorDocumentId,
    name = name,
    avatar = avatar,
    level = level,
    isAiAgent = isAiAgent
)

fun DmReplyToDto.toDomain() = DmReplyTo(
    documentId = documentId,
    content = content,
    senderUserId = senderUserId
)

fun DmMessageDto.toDomain(selfUserId: Int? = null) = DmMessage(
    documentId = documentId,
    kind = kind,
    content = content,
    createdAt = createdAt,
    editedAt = editedAt,
    deletedAt = deletedAt,
    sender = sender?.toDomain(),
    replyTo = replyTo?.toDomain(),
    isFromSelf = sender?.userId != null && selfUserId != null && sender.userId == selfUserId
)

fun DmMessagePageDto.toDomain(selfUserId: Int? = null) = DmMessagePage(
    items = data.map { it.toDomain(selfUserId) },
    nextCursor = meta.nextCursor,
    hasMore = meta.hasMore
)

fun DmSocketTicketDto.toDomain() = DmSocketTicket(
    ticket = ticket,
    ttlSec = ttlSec
)
