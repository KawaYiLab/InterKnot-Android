package dev.kawayilab.interknot.model

data class Benefits(
    val level: Int = 0,
    val maxLevel: Int = 0,
    val benefits: BenefitValues = BenefitValues(),
    val nextLevel: Int? = null,
    val nextBenefits: BenefitValues? = null
)

data class BenefitValues(
    val articleMaxBody: Int? = null,
    val commentMaxBody: Int? = null,
    val articleMaxImages: Int? = null,
    val dmMaxImages: Int? = null,
    val pinSlots: Int? = null
)
