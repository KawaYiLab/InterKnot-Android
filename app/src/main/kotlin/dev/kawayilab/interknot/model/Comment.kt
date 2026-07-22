package dev.kawayilab.interknot.model

data class Comment(
    val documentId: String,
    val content: String,
    val images: List<ImageMeta> = emptyList(),
    val liked: Boolean = false,
    val likesCount: Int = 0,
    val createdAt: String? = null,
    val author: Author? = null,
    val replies: List<Comment> = emptyList(),
    val isPinned: Boolean = false,
    val pinnedAt: String? = null,
    val floor: Int? = null
)

data class CommentPage(
    val items: List<Comment>,
    val pinned: Comment? = null,
    val start: Int = 0,
    val limit: Int = 0,
    val total: Int = 0,
    val hasMore: Boolean = false
)
