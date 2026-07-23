@file:OptIn(ExperimentalMaterial3Api::class)
package dev.kawayilab.interknot.ui.screens.exam

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.kawayilab.interknot.model.ExamConfig
import dev.kawayilab.interknot.model.ExamOption
import dev.kawayilab.interknot.model.ExamQuestion
import dev.kawayilab.interknot.model.ExamReview
import dev.kawayilab.interknot.model.ExamStatus
import dev.kawayilab.interknot.model.ExamSubmitResult

@Composable
fun ExamScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExamViewModel = hiltViewModel()
) {
    val status by viewModel.status.collectAsStateWithLifecycle()
    val questions by viewModel.questions.collectAsStateWithLifecycle()
    val answers by viewModel.answers.collectAsStateWithLifecycle()
    val attempt by viewModel.attempt.collectAsStateWithLifecycle()
    val submitResult by viewModel.submitResult.collectAsStateWithLifecycle()
    val review by viewModel.review.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = { Text("入站考试") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val reviewValue = review
            val submitResultValue = submitResult
            when {
                isLoading && status == null -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
                error != null && status == null -> ErrorState(
                    message = error ?: "加载失败",
                    onRetry = { viewModel.loadStatus() },
                    modifier = Modifier.align(Alignment.Center)
                )
                reviewValue != null -> ExamReviewView(
                    review = reviewValue,
                    onBack = { viewModel.reset(); viewModel.loadStatus() },
                    modifier = Modifier.fillMaxSize()
                )
                submitResultValue != null -> ExamResultView(
                    result = submitResultValue,
                    onReview = { viewModel.loadReview() },
                    onBack = onNavigateBack,
                    modifier = Modifier.fillMaxSize()
                )
                attempt != null && questions.isNotEmpty() -> ExamQuestionView(
                    questions = questions,
                    answers = answers,
                    isLoading = isLoading,
                    onToggle = { qid, key, multi -> viewModel.toggleAnswer(qid, key, multi) },
                    onSubmit = { viewModel.submit() },
                    modifier = Modifier.fillMaxSize()
                )
                else -> ExamIntro(
                    status = status,
                    isLoading = isLoading,
                    onStart = { viewModel.startExam() },
                    onRefresh = { viewModel.loadStatus() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun ExamIntro(
    status: ExamStatus?,
    isLoading: Boolean,
    onStart: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "入站考试",
            style = MaterialTheme.typography.headlineSmall
        )

        val passed = status?.passed == true
        if (passed) {
            Text(
                text = "已通过考试",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )
            status?.passedAt?.let {
                Text("通过时间: $it", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            val cooldown = status?.cooldownRemaining ?: 0
            val config = status?.config
            if (config != null) {
                ExamConfigInfo(config)
            }
            if (cooldown > 0) {
                Text(
                    text = "冷却倒计时: ${cooldown} 秒",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onRefresh, enabled = !isLoading) {
                Text("刷新")
            }
            if (!passed) {
                Button(onClick = onStart, enabled = !isLoading) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("开始考试")
                    }
                }
            }
        }
    }
}

@Composable
private fun ExamConfigInfo(config: ExamConfig) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("题目数量: ${config.questionCount}")
        Text("及格分数线: ${config.passScorePercent}%")
        if (config.timeLimitSeconds > 0) {
            Text("限时: ${config.timeLimitSeconds / 60} 分钟")
        }
        if (config.rewardExp > 0 || config.rewardDenny > 0) {
            Text("奖励: ${config.rewardExp} EXP / ${config.rewardDenny} Denny")
        }
    }
}

@Composable
private fun ExamQuestionView(
    questions: List<ExamQuestion>,
    answers: Map<String, List<String>>,
    isLoading: Boolean,
    onToggle: (String, String, Boolean) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(questions, key = { it.id }) { question ->
                val isMultiple = question.type == "multiple"
                QuestionCard(
                    question = question,
                    selectedKeys = answers[question.id] ?: emptyList(),
                    isMultiple = isMultiple,
                    onToggle = { key -> onToggle(question.id, key, isMultiple) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("提交")
            }
        }
    }
}

@Composable
private fun QuestionCard(
    question: ExamQuestion,
    selectedKeys: List<String>,
    isMultiple: Boolean,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = question.question,
            style = MaterialTheme.typography.titleSmall
        )
        question.options.forEach { option ->
            OptionRow(
                option = option,
                selected = selectedKeys.contains(option.key),
                isMultiple = isMultiple,
                onToggle = { onToggle(option.key) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun OptionRow(
    option: ExamOption,
    selected: Boolean,
    isMultiple: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .selectable(
                selected = selected,
                onClick = onToggle,
                role = if (isMultiple) Role.Checkbox else Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isMultiple) {
            Checkbox(checked = selected, onCheckedChange = null)
        } else {
            RadioButton(selected = selected, onClick = null)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(option.text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ExamResultView(
    result: ExamSubmitResult,
    onReview: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (result.passed) "恭喜通过" else "未通过",
            style = MaterialTheme.typography.headlineSmall,
            color = if (result.passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
        Text("得分: ${result.score} / ${result.total}")
        Text("百分比: ${result.percent}%")
        if (result.cooldownRemaining > 0) {
            Text(
                text = "冷却: ${result.cooldownRemaining} 秒",
                color = MaterialTheme.colorScheme.error
            )
        }
        if (result.rewardDenny != null && result.rewardDenny > 0) {
            Text("获得 ${result.rewardDenny} Denny")
        }
        if (result.rewardExp != null && result.rewardExp > 0) {
            Text("获得 ${result.rewardExp} EXP")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onBack) { Text("返回") }
            Button(onClick = onReview) { Text("查看解析") }
        }
    }
}

@Composable
private fun ExamReviewView(
    review: ExamReview,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (review.passed) "考试通过" else "考试未通过",
            style = MaterialTheme.typography.headlineSmall
        )
        Text("得分: ${review.score} / ${review.total} (${review.percent}%)")

        Spacer(modifier = Modifier.height(8.dp))

        review.questions.forEach { question ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(question.question, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = "你的答案: ${question.selectedKeys.joinToString(", ") { it }}",
                    color = if (question.isCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Text(
                    text = "正确答案: ${question.correctKeys.joinToString(", ") { it }}",
                    color = MaterialTheme.colorScheme.primary
                )
                question.explanation?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("返回") }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(message, color = MaterialTheme.colorScheme.error)
        Button(onClick = onRetry) { Text("重试") }
    }
}
