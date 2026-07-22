package dev.kawayilab.interknot.model

data class MeProfile(
    val user: User,
    val author: Author
)

data class PinnedArticlesResponse(
    val pinned: List<String>?,
    val candidates: List<ArticleRef>,
    val max: Int
)

data class ArticleRef(
    val documentId: String,
    val title: String? = null,
    val coverUrl: String? = null,
    val updatedAt: String? = null
)

data class NameUpdateResult(val success: Boolean, val name: String?)
data class BioUpdateResult(val success: Boolean, val bio: String?)
data class VisibilityUpdateResult(val success: Boolean, val profileHidden: Boolean)
data class PinnedUpdateResult(val pinned: List<String>?)

data class Avatar(
    val documentId: String? = null,
    val name: String? = null,
    val description: String? = null,
    val type: String? = null,
    val image: ImageMeta? = null,
    val isCustom: Boolean = false
)

data class BusinessCard(
    val documentId: String? = null,
    val name: String? = null,
    val description: String? = null,
    val story: String? = null,
    val type: String? = null,
    val image: ImageMeta? = null
)
