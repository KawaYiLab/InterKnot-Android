package dev.kawayilab.interknot.data.api.dto

import dev.kawayilab.interknot.model.ExamAttemptInfo
import dev.kawayilab.interknot.model.ExamConfig
import dev.kawayilab.interknot.model.ExamOption
import dev.kawayilab.interknot.model.ExamQuestion
import dev.kawayilab.interknot.model.ExamReview
import dev.kawayilab.interknot.model.ExamReviewQuestion
import dev.kawayilab.interknot.model.ExamStartResult
import dev.kawayilab.interknot.model.ExamStatus
import dev.kawayilab.interknot.model.ExamSubmitResult
import kotlinx.serialization.Serializable

@Serializable
data class ExamConfigDto(
    val questionCount: Int = 0,
    val passScorePercent: Int = 0,
    val timeLimitSeconds: Int = 0,
    val maxFailsBeforeCooldown: Int = 0,
    val failCooldownSeconds: Int = 0,
    val rewardDenny: Int = 0,
    val rewardExp: Int = 0
) {
    fun toDomain() = ExamConfig(
        questionCount,
        passScorePercent,
        timeLimitSeconds,
        maxFailsBeforeCooldown,
        failCooldownSeconds,
        rewardDenny,
        rewardExp
    )
}

@Serializable
data class ExamAttemptInfoDto(
    val attemptId: String? = null,
    val startedAt: String? = null,
    val expiresAt: String? = null,
    val questionCount: Int = 0
) {
    fun toDomain() = attemptId?.let {
        ExamAttemptInfo(
            attemptId = it,
            startedAt = startedAt,
            expiresAt = expiresAt,
            questionCount = questionCount
        )
    }
}

@Serializable
data class ExamStatusDto(
    val passed: Boolean = false,
    val passedAt: String? = null,
    val cooldownRemaining: Int = 0,
    val activeAttempt: ExamAttemptInfoDto? = null,
    val config: ExamConfigDto? = null
) {
    fun toDomain() = ExamStatus(
        passed = passed,
        passedAt = passedAt,
        cooldownRemaining = cooldownRemaining,
        activeAttempt = activeAttempt?.toDomain(),
        config = config?.toDomain()
    )
}

@Serializable
data class ExamOptionDto(
    val key: String = "",
    val text: String = ""
) {
    fun toDomain() = ExamOption(key, text)
}

@Serializable
data class ExamQuestionDto(
    val questionId: String? = null,
    val question: String = "",
    val type: String = "",
    val options: List<ExamOptionDto> = emptyList(),
    val weight: Int? = null
) {
    fun toDomain() = ExamQuestion(
        id = questionId ?: "",
        question = question,
        type = type,
        options = options.map { it.toDomain() }
    )
}

@Serializable
data class ExamStartResultDto(
    val attemptId: String? = null,
    val resumed: Boolean = false,
    val startedAt: String? = null,
    val expiresAt: String? = null,
    val questions: List<ExamQuestionDto> = emptyList(),
    val config: ExamConfigDto? = null
) {
    fun toDomain() = ExamStartResult(
        attemptId = attemptId ?: "",
        resumed = resumed,
        startedAt = startedAt,
        expiresAt = expiresAt,
        questions = questions.map { it.toDomain() },
        config = config?.toDomain()
    )
}

@Serializable
data class ExamRewardDto(
    val denny: Int? = null,
    val exp: Int? = null
)

@Serializable
data class ExamSubmitResultDto(
    val passed: Boolean = false,
    val score: Int? = null,
    val totalScore: Int? = null,
    val scorePercent: Int? = null,
    val correctCount: Int? = null,
    val questionCount: Int? = null,
    val passScorePercent: Int? = null,
    val cooldownRemaining: Int = 0,
    val reward: ExamRewardDto? = null
) {
    fun toDomain() = ExamSubmitResult(
        passed = passed,
        score = score ?: 0,
        total = totalScore ?: 0,
        percent = scorePercent ?: 0,
        cooldownRemaining = cooldownRemaining,
        rewardDenny = reward?.denny,
        rewardExp = reward?.exp
    )
}

@Serializable
data class ExamReviewQuestionDto(
    val questionId: String? = null,
    val question: String = "",
    val type: String = "",
    val options: List<ExamOptionDto> = emptyList(),
    val userAnswer: List<String> = emptyList(),
    val isCorrect: Boolean = false,
    val weight: Int? = null,
    val score: Int? = null,
    val explanation: String? = null
) {
    fun toDomain(): ExamReviewQuestion {
        return ExamReviewQuestion(
            id = questionId ?: "",
            question = question,
            type = type,
            options = options.map { it.toDomain() },
            selectedKeys = userAnswer,
            isCorrect = isCorrect,
            weight = weight,
            score = score,
            explanation = explanation
        )
    }
}

@Serializable
data class ExamReviewDto(
    val attemptId: String? = null,
    val passed: Boolean = false,
    val score: Int? = null,
    val totalScore: Int? = null,
    val scorePercent: Int? = null,
    val correctCount: Int? = null,
    val questionCount: Int? = null,
    val submittedAt: String? = null,
    val config: ExamConfigDto? = null,
    val questions: List<ExamReviewQuestionDto> = emptyList()
) {
    fun toDomain() = ExamReview(
        attemptId = attemptId ?: "",
        passed = passed,
        score = score ?: 0,
        total = totalScore ?: 0,
        percent = scorePercent ?: 0,
        questions = questions.map { it.toDomain() }
    )
}
