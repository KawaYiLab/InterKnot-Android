package dev.kawayilab.interknot.data.api.dto

import dev.kawayilab.interknot.model.BlockResult
import kotlinx.serialization.Serializable

@Serializable
data class BlockResultDto(
    val blocked: Boolean = false,
    val authorDocumentId: String? = null
)

fun BlockResultDto.toDomain() = BlockResult(
    blocked = blocked,
    authorDocumentId = authorDocumentId ?: ""
)

@Serializable
data class BlockCheckResultDto(val data: Map<String, Boolean> = emptyMap())
