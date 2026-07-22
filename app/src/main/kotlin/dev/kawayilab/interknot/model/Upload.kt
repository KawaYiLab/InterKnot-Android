package dev.kawayilab.interknot.model

data class SignedUploadResult(
    val uploadUrl: String? = null,
    val uploadToken: String? = null,
    val method: String = "PUT",
    val objectKey: String? = null,
    val publicUrl: String? = null,
    val headers: Map<String, String> = emptyMap(),
    val expiresAt: String? = null,
    val existing: UploadedFile? = null
)

data class UploadedFile(
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

data class FileInfo(
    val name: String? = null,
    val alternativeText: String? = null,
    val caption: String? = null
)
