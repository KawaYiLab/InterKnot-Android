package dev.kawayilab.interknot.data.api.dto

import dev.kawayilab.interknot.model.Category
import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val documentId: String? = null,
    val name: String? = null,
    val slug: String? = null,
    val order: Int? = null,
    val adminOnly: Boolean? = null
)

fun CategoryDto.toDomain() = Category(
    documentId = documentId,
    name = name,
    slug = slug,
    order = order,
    adminOnly = adminOnly == true
)
