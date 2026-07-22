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
