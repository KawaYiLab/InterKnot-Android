package dev.kawayilab.interknot.ui.screens.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.kawayilab.interknot.data.repository.InterknotRepository
import dev.kawayilab.interknot.model.Article
import dev.kawayilab.interknot.model.Category
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val repository: InterknotRepository
) : ViewModel() {

    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private var start = 0
    private val limit = 20
    private var loadJob: Job? = null

    init {
        loadCategories()
        loadMore()
    }

    fun loadMore() {
        if (_isLoading.value || !_hasMore.value) return
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
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
        loadJob?.cancel()
        start = 0
        _articles.value = emptyList()
        _hasMore.value = true
        _error.value = null
        loadMore()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            repository.getCategories()
                .onSuccess { _categories.value = it.sortedBy { c -> c.order ?: Int.MAX_VALUE } }
                .onFailure { /* ignore */ }
        }
    }
}
