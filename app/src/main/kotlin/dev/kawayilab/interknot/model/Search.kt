package dev.kawayilab.interknot.model

data class SearchSuggestion(
    val documentId: String,
    val title: String,
    val titleHighlighted: String? = null,
    val excerpt: String? = null,
    val authorName: String? = null,
    val categoryName: String? = null,
    val categorySlug: String? = null,
    val isAnonymous: Boolean = false
)
