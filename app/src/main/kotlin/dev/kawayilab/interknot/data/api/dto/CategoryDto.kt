package dev.kawayilab.interknot.data.api.dto

import dev.kawayilab.interknot.model.Category
import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val name: String? = null,
    val slug: String? = null
)

fun CategoryDto.toDomain() = Category(
    name = name,
    slug = slug
)
