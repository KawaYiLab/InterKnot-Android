package dev.kawayilab.interknot.model

data class Article(
    val documentId: String,
    val title: String,
    val text: String? = null,
    val coverUrl: String? = null,
    val coverWidth: Int? = null,
    val coverHeight: Int? = null,
    val coverImages: List<ImageMeta> = emptyList(),
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
    val author: Author? = null,
    val category: Category? = null
)

data class ArticlePage(
    val items: List<Article>,
    val start: Int,
    val limit: Int,
    val total: Int,
    val hasMore: Boolean
)
