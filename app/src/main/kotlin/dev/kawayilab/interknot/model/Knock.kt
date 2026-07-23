package dev.kawayilab.interknot.model

data class KnockConversation(
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

data class KnockNotification(
    val documentId: String,
    val type: String,
    val rawType: String? = null,
    val isRead: Boolean = false,
    val createdAt: String? = null,
    val message: String? = null,
    val sender: KnockSender? = null,
    val article: KnockArticleRef? = null,
    val comment: KnockCommentRef? = null
)

data class KnockSender(
    val id: String? = null,
    val username: String? = null,
    val level: Int? = null,
    val author: KnockSenderAuthor? = null
)

data class KnockSenderAuthor(
    val documentId: String? = null,
    val name: String? = null,
    val avatarUrl: String? = null
)

data class KnockArticleRef(
    val documentId: String,
    val title: String,
    val coverAspectRatio: Double? = null
)

data class KnockCommentRef(
    val documentId: String,
    val content: String,
    val images: List<ImageMeta> = emptyList(),
    val isAnonymous: Boolean = false
)

data class KnockMessagePage(
    val items: List<KnockNotification>,
    val nextCursor: String? = null,
    val hasMore: Boolean = false
)

data class NotificationPage(
    val items: List<KnockNotification>,
    val start: Int = 0,
    val limit: Int = 0,
    val total: Int = 0,
    val hasMore: Boolean = false
)
