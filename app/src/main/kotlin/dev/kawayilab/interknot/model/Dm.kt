package dev.kawayilab.interknot.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class DmConversation(
    val documentId: String,
    val kind: String = "direct",
    val title: String? = null,
    val avatar: String? = null,
    val peer: DmPeer? = null,
    val memberCount: Int = 0,
    val lastMessageAt: String? = null,
    val lastMessage: DmLastMessage? = null,
    val unreadCount: Int = 0,
    val self: DmSelfMember? = null
)

@Serializable
data class DmPeer(
    val userId: Int? = null,
    val authorDocumentId: String? = null,
    val name: String? = null,
    val avatar: String? = null,
    val level: Int? = null,
    val isAiAgent: Boolean = false
)

@Serializable
data class DmLastMessage(
    val documentId: String? = null,
    val content: String? = null,
    val createdAt: String? = null,
    val kind: String? = null,
    val senderUserId: Int? = null
)

@Serializable
data class DmSelfMember(
    val role: String? = null,
    val muted: Boolean = false,
    val pinned: Boolean = false,
    val lastReadAt: String? = null
)

@Serializable
data class DmConversationDetail(
    val documentId: String,
    val kind: String = "direct",
    val title: String? = null,
    val avatar: String? = null,
    val ownerUserId: Int? = null,
    val lastMessageAt: String? = null,
    val self: DmSelfMember? = null,
    val members: List<DmMember> = emptyList()
)

@Serializable
data class DmMember(
    val userId: Int? = null,
    val authorDocumentId: String? = null,
    val name: String? = null,
    val avatar: String? = null,
    val level: Int? = null,
    val role: String? = null,
    val isSelf: Boolean = false
)

@Serializable
data class DmMessage(
    val documentId: String,
    val kind: String = "text",
    val content: String? = null,
    val createdAt: String? = null,
    val editedAt: String? = null,
    val deletedAt: String? = null,
    val sender: DmSender? = null,
    val replyTo: DmReplyTo? = null,
    val isFromSelf: Boolean = false
)

@Serializable
data class DmSender(
    val userId: Int? = null,
    val authorDocumentId: String? = null,
    val name: String? = null,
    val avatar: String? = null,
    val level: Int? = null,
    val isAiAgent: Boolean = false
)

@Serializable
data class DmReplyTo(
    val documentId: String? = null,
    val content: String? = null,
    val senderUserId: Int? = null
)

@Serializable
data class DmMessagePage(
    val items: List<DmMessage> = emptyList(),
    val nextCursor: String? = null,
    val hasMore: Boolean = false
)

@Serializable
data class DmSocketTicket(
    val ticket: String,
    val ttlSec: Int = 30
)

@Serializable
data class DmEvent(
    val type: String,
    val conversationId: String? = null,
    val messageId: String? = null,
    val data: JsonElement? = null,
    val at: String? = null
)
