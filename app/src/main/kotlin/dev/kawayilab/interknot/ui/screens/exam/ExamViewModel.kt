package dev.kawayilab.interknot.ui.screens.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.kawayilab.interknot.data.repository.InterknotRepository
import dev.kawayilab.interknot.model.ExamQuestion
import dev.kawayilab.interknot.model.ExamReview
import dev.kawayilab.interknot.model.ExamStartResult
import dev.kawayilab.interknot.model.ExamStatus
import dev.kawayilab.interknot.model.ExamSubmitResult
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ExamViewModel @Inject constructor(
    private val repository: InterknotRepository
) : ViewModel() {

    private val _status = MutableStateFlow<ExamStatus?>(null)
    val status: StateFlow<ExamStatus?> = _status.asStateFlow()

    private val _questions = MutableStateFlow<List<ExamQuestion>>(emptyList())
    val questions: StateFlow<List<ExamQuestion>> = _questions.asStateFlow()

    private val _answers = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val answers: StateFlow<Map<String, List<String>>> = _answers.asStateFlow()

    private val _attempt = MutableStateFlow<ExamStartResult?>(null)
    val attempt: StateFlow<ExamStartResult?> = _attempt.asStateFlow()

    private val _submitResult = MutableStateFlow<ExamSubmitResult?>(null)
    val submitResult: StateFlow<ExamSubmitResult?> = _submitResult.asStateFlow()

    private val _review = MutableStateFlow<ExamReview?>(null)
    val review: StateFlow<ExamReview?> = _review.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadStatus()
    }

    fun loadStatus() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getExamStatus()
                .onSuccess { _status.value = it }
                .onFailure { _error.value = it.message ?: "加载考试状态失败" }
            _isLoading.value = false
        }
    }

    fun startExam() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.startExam()
                .onSuccess { result ->
                    _attempt.value = result
                    _questions.value = result.questions
                    _answers.value = emptyMap()
                    _submitResult.value = null
                    _review.value = null
                }
                .onFailure { _error.value = it.message ?: "开始考试失败" }
            _isLoading.value = false
        }
    }

    fun toggleAnswer(questionId: String, key: String, isMultiple: Boolean = false) {
        _answers.value = _answers.value.toMutableMap().apply {
            val current = this[questionId] ?: emptyList()
            val updated = if (isMultiple) {
                if (current.contains(key)) current - key else current + key
            } else {
                if (current.contains(key)) emptyList() else listOf(key)
            }
            this[questionId] = updated
        }
    }

    fun submit() {
        val attemptId = _attempt.value?.attemptId ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.submitExam(attemptId, _answers.value)
                .onSuccess { _submitResult.value = it }
                .onFailure { _error.value = it.message ?: "提交失败" }
            _isLoading.value = false
        }
    }

    fun loadReview() {
        val attemptId = _submitResult.value?.attemptId ?: _attempt.value?.attemptId ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getExamReview(attemptId)
                .onSuccess { _review.value = it }
                .onFailure { _error.value = it.message ?: "加载解析失败" }
            _isLoading.value = false
        }
    }

    fun reset() {
        _attempt.value = null
        _questions.value = emptyList()
        _answers.value = emptyMap()
        _submitResult.value = null
        _review.value = null
        _error.value = null
    }

    fun dismissError() { _error.value = null }
}
