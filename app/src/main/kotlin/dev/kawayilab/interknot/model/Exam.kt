package dev.kawayilab.interknot.model

data class ExamStatus(
    val passed: Boolean = false,
    val passedAt: String? = null,
    val cooldownRemaining: Int = 0,
    val activeAttempt: ExamAttemptInfo? = null,
    val config: ExamConfig? = null
)

data class ExamAttemptInfo(
    val attemptId: String,
    val startedAt: String?,
    val expiresAt: String?,
    val questionCount: Int = 0
)

data class ExamConfig(
    val questionCount: Int = 0,
    val passScorePercent: Int = 0,
    val timeLimitSeconds: Int = 0,
    val maxFailsBeforeCooldown: Int = 0,
    val failCooldownSeconds: Int = 0,
    val rewardDenny: Int = 0,
    val rewardExp: Int = 0
)

data class ExamStartResult(
    val attemptId: String,
    val resumed: Boolean = false,
    val startedAt: String? = null,
    val expiresAt: String? = null,
    val questions: List<ExamQuestion> = emptyList(),
    val config: ExamConfig? = null
)

data class ExamQuestion(
    val id: String,
    val question: String,
    val type: String,
    val options: List<ExamOption> = emptyList()
)

data class ExamOption(
    val key: String,
    val text: String
)

data class ExamAnswer(
    val questionId: String,
    val selectedKeys: List<String>
)

data class ExamSubmitResult(
    val passed: Boolean = false,
    val score: Int = 0,
    val total: Int = 0,
    val percent: Int = 0,
    val cooldownRemaining: Int = 0,
    val attemptId: String? = null,
    val rewardDenny: Int? = null,
    val rewardExp: Int? = null
)

data class ExamReview(
    val attemptId: String,
    val passed: Boolean,
    val score: Int,
    val total: Int,
    val percent: Int,
    val questions: List<ExamReviewQuestion> = emptyList()
)

data class ExamReviewQuestion(
    val id: String,
    val question: String,
    val type: String,
    val options: List<ExamOption> = emptyList(),
    val correctKeys: List<String> = emptyList(),
    val selectedKeys: List<String> = emptyList(),
    val isCorrect: Boolean = false,
    val weight: Int? = null,
    val score: Int? = null,
    val explanation: String? = null
)
