package dev.kawayilab.interknot.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.kawayilab.interknot.data.repository.InterknotRepository
import dev.kawayilab.interknot.model.Article
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: InterknotRepository
) : ViewModel() {

    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private var start = 0
    private val limit = 20

    init {
        loadMore()
    }

    fun loadMore() {
        if (_isLoading.value || !_hasMore.value) return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getArticles(start, limit, "recommend", null)
                .onSuccess { page ->
                    _articles.value = _articles.value + page.items
                    start += page.items.size
                    _hasMore.value = page.hasMore
                }
                .onFailure { _error.value = it.message ?: "加载失败" }
            _isLoading.value = false
        }
    }

    fun refresh() {
        start = 0
        _articles.value = emptyList()
        _hasMore.value = true
        _error.value = null
        loadMore()
    }
}
