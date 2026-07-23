package dev.kawayilab.interknot.data.local

import kotlinx.serialization.Serializable

@Serializable
data class CreateDraft(
    val title: String = "",
    val body: String = "",
    val categorySlug: String? = null,
    val isAnonymous: Boolean = false,
    val imageDocumentIds: List<String> = emptyList(),
    val remoteDraftId: String? = null
)
