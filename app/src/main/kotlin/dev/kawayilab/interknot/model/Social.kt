package dev.kawayilab.interknot.model

data class FavoriteResult(
    val favorited: Boolean,
    val favoritesCount: Int
)

data class FollowResult(
    val following: Boolean,
    val followersCount: Int
)

data class TripleResult(
    val liked: Boolean = true,
    val likesCount: Int = 0,
    val favorited: Boolean = true,
    val favoritesCount: Int = 0,
    val coinGiven: Boolean = false,
    val coinReason: String? = null,
    val dennyCount: Int = 0,
    val newBalance: Int? = null
)

data class LikeRecord(
    val documentId: String? = null,
    val createdAt: String? = null,
    val targetType: String? = null,
    val article: ArticleRef? = null,
    val comment: CommentRef? = null
)

data class FavoriteRecord(
    val documentId: String? = null,
    val createdAt: String? = null,
    val article: ArticleRef? = null
)

data class CommentRef(
    val documentId: String,
    val content: String? = null,
    val createdAt: String? = null,
    val likesCount: Int = 0,
    val article: ArticleRef? = null
)
