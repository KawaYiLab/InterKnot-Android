package dev.kawayilab.interknot.data.api.dto

import dev.kawayilab.interknot.model.SignedUploadResult
import dev.kawayilab.interknot.model.UploadedFile
import kotlinx.serialization.Serializable

@Serializable
data class SignedUploadResultDto(
    val uploadUrl: String? = null,
    val uploadToken: String? = null,
    val method: String = "PUT",
    val objectKey: String? = null,
    val publicUrl: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val expiresAt: String? = null,
    val existing: UploadedFileDto? = null
)

@Serializable
data class UploadedFileDto(
    val id: Int? = null,
    val documentId: String? = null,
    val name: String? = null,
    val alternativeText: String? = null,
    val caption: String? = null,
    val url: String? = null,
    val mime: String? = null,
    val size: Int? = null,
    val width: Int? = null,
    val height: Int? = null,
    val nsfwStatus: String? = null,
    val nsfwScores: Map<String, Float>? = null
)

fun UploadedFileDto.toDomain() = UploadedFile(
    id = id,
    documentId = documentId,
    name = name,
    alternativeText = alternativeText,
    caption = caption,
    url = url,
    mime = mime,
    size = size,
    width = width,
    height = height,
    nsfwStatus = nsfwStatus,
    nsfwScores = nsfwScores
)

fun SignedUploadResultDto.toDomain() = SignedUploadResult(
    uploadUrl = uploadUrl,
    uploadToken = uploadToken,
    method = method,
    objectKey = objectKey,
    publicUrl = publicUrl,
    headers = headers,
    expiresAt = expiresAt,
    existing = existing?.toDomain()
)
