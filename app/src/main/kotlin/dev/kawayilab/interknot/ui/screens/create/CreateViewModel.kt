package dev.kawayilab.interknot.ui.screens.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.kawayilab.interknot.data.repository.InterknotRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CreateViewModel @Inject constructor(
    private val repository: InterknotRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _publishedId = MutableStateFlow<String?>(null)
    val publishedId: StateFlow<String?> = _publishedId.asStateFlow()

    fun publishArticle(
        title: String,
        text: String,
        category: String? = null,
        isAnonymous: Boolean = false,
        onResult: (Result<String>) -> Unit = {}
    ) {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _publishedId.value = null
            repository.publishArticle(title, text, category, isAnonymous)
                .onSuccess { id ->
                    _publishedId.value = id
                    onResult(Result.success(id))
                }
                .onFailure { err ->
                    _error.value = err.message ?: "发布失败"
                    onResult(Result.failure(err))
                }
            _isLoading.value = false
        }
    }

    fun dismissError() {
        _error.value = null
    }
}
