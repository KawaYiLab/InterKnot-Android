package dev.kawayilab.interknot.data.api.dto

import dev.kawayilab.interknot.model.BenefitValues
import dev.kawayilab.interknot.model.Benefits
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class BenefitsDto(
    val level: Int = 0,
    val maxLevel: Int = 0,
    val benefits: JsonObject? = null,
    val nextLevel: Int? = null,
    val nextBenefits: JsonObject? = null
) {
    fun toDomain() = Benefits(
        level = level,
        maxLevel = maxLevel,
        benefits = benefits.toBenefitValues(),
        nextLevel = nextLevel,
        nextBenefits = nextBenefits.toBenefitValues()
    )

    private fun JsonObject?.toBenefitValues(): BenefitValues {
        if (this == null) return BenefitValues()
        fun int(key: String) = this[key]?.toString()?.toIntOrNull()
        return BenefitValues(
            articleMaxBody = int("articleMaxBody"),
            commentMaxBody = int("commentMaxBody"),
            articleMaxImages = int("articleMaxImages"),
            dmMaxImages = int("dmMaxImages"),
            pinSlots = int("pinSlots")
        )
    }
}
