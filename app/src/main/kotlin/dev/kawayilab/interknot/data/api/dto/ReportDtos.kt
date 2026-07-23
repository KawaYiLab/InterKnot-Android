package dev.kawayilab.interknot.data.api.dto

import dev.kawayilab.interknot.model.ReportResult
import kotlinx.serialization.Serializable

@Serializable
data class ReportResponseDto(
    val documentId: String? = null,
    val targetType: String? = null,
    val reason: String? = null,
    val reportStatus: String? = null,
    val createdAt: String? = null
)

fun ReportResponseDto.toDomain() = ReportResult(
    documentId = documentId,
    targetType = targetType,
    reason = reason,
    reportStatus = reportStatus,
    createdAt = createdAt
)
