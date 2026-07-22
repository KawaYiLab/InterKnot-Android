package dev.kawayilab.interknot.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class PaginationDto(
    val start: Int = 0,
    val limit: Int = 0,
    val total: Int = 0,
    val pageCount: Int = 0
)

@Serializable
data class PagedListMetaDto(
    val pagination: PaginationDto = PaginationDto()
)

@Serializable
data class PagedListDto<T>(
    val data: List<T> = emptyList(),
    val meta: PagedListMetaDto = PagedListMetaDto()
)

@Serializable
data class SingleDto<T>(
    val data: T
)

@Serializable
data class DataListDto<T>(
    val data: List<T> = emptyList()
)

@Serializable
data class SearchSuggestionDto(
    val documentId: String,
    val title: String,
    val titleHighlighted: String? = null,
    val excerpt: String? = null,
    val authorName: String? = null,
    val categoryName: String? = null,
    val categorySlug: String? = null,
    val isAnonymous: Boolean? = null
)

fun SearchSuggestionDto.toDomain() = dev.kawayilab.interknot.data.api.SearchSuggestion(
    documentId = documentId,
    title = title,
    titleHighlighted = titleHighlighted,
    excerpt = excerpt,
    authorName = authorName,
    categoryName = categoryName,
    categorySlug = categorySlug,
    isAnonymous = isAnonymous == true
)
