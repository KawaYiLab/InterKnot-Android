package dev.kawayilab.interknot.data.api.dto

import dev.kawayilab.interknot.model.ArticleRef
import dev.kawayilab.interknot.model.BioUpdateResult
import dev.kawayilab.interknot.model.NameUpdateResult
import dev.kawayilab.interknot.model.PinnedArticlesResponse
import dev.kawayilab.interknot.model.PinnedUpdateResult
import dev.kawayilab.interknot.model.VisibilityUpdateResult
import kotlinx.serialization.Serializable

@Serializable
data class PinnedArticlesResponseDto(
    val pinned: List<String>? = null,
    val candidates: List<ArticleRefDto> = emptyList(),
    val max: Int = 6
)

@Serializable
data class PinnedUpdateResultDto(val pinned: List<String>? = null)

@Serializable
data class NameUpdateResultDto(val success: Boolean = false, val name: String? = null)

@Serializable
data class BioUpdateResultDto(val success: Boolean = false, val bio: String? = null)

@Serializable
data class VisibilityUpdateResultDto(
    val success: Boolean = false,
    val profileHidden: Boolean = false
)

fun PinnedArticlesResponseDto.toDomain() = PinnedArticlesResponse(
    pinned = pinned,
    candidates = candidates.map { it.toDomain() },
    max = max
)

fun PinnedUpdateResultDto.toDomain() = PinnedUpdateResult(pinned = pinned)
fun NameUpdateResultDto.toDomain() = NameUpdateResult(success = success, name = name)
fun BioUpdateResultDto.toDomain() = BioUpdateResult(success = success, bio = bio)
fun VisibilityUpdateResultDto.toDomain() = VisibilityUpdateResult(
    success = success,
    profileHidden = profileHidden
)
