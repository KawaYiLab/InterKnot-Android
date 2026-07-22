package dev.kawayilab.interknot.ui.screens.home

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
class HomeViewModel @Inject constructor(
    private val repository: InterknotRepository
) : ViewModel() {

    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isLoadingCategories = MutableStateFlow(false)
    val isLoadingCategories: StateFlow<Boolean> = _isLoadingCategories.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _feed = MutableStateFlow("recommend")
    val feed: StateFlow<String> = _feed.asStateFlow()

    private val _categorySlug = MutableStateFlow<String?>(null)
    val categorySlug: StateFlow<String?> = _categorySlug.asStateFlow()

    private var start = 0
    private val limit = 20
    private var loadJob: Job? = null

    init {
        loadCategories()
        loadMore()
    }

    fun setFeed(value: String) {
        if (_feed.value == value) return
        _feed.value = value
        refresh()
    }

    fun setCategorySlug(slug: String?) {
        if (_categorySlug.value == slug) return
        _categorySlug.value = slug
        refresh()
    }

    fun setQuery(value: String) {
        if (_query.value == value) return
        _query.value = value
        refresh()
    }

    fun loadMore() {
        if (_isLoading.value || _isRefreshing.value || !_hasMore.value) return
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val currentQuery = _query.value.trim()
            val currentCategory = _categorySlug.value?.takeIf { it.isNotBlank() }
            val result = if (currentQuery.isNotEmpty()) {
                repository.searchArticles(currentQuery, start, limit, currentCategory)
            } else {
                repository.getArticles(start, limit, _feed.value, currentCategory)
            }
            result
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
        _isRefreshing.value = false
        start = 0
        _articles.value = emptyList()
        _hasMore.value = true
        _error.value = null
        loadMore()
    }

    /**
     * Pull-to-refresh: fetches fresh data from page 0 but keeps
     * existing articles visible during the refresh for a smooth UX.
     */
    fun pullRefresh() {
        if (_isRefreshing.value) return
        loadJob?.cancel()
        _isRefreshing.value = true
        _error.value = null
        val savedStart = start
        start = 0
        _hasMore.value = true
        loadJob = viewModelScope.launch {
            try {
                val currentQuery = _query.value.trim()
                val currentCategory = _categorySlug.value?.takeIf { it.isNotBlank() }
                val result = if (currentQuery.isNotEmpty()) {
                    repository.searchArticles(currentQuery, start, limit, currentCategory)
                } else {
                    repository.getArticles(start, limit, _feed.value, currentCategory)
                }
                result
                    .onSuccess { page ->
                        _articles.value = page.items
                        start = page.items.size
                        _hasMore.value = page.hasMore
                    }
                    .onFailure {
                        // Revert start on failure so old pagination state is preserved
                        start = savedStart
                        _error.value = it.message ?: "刷新失败"
                    }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _isLoadingCategories.value = true
            repository.getCategories()
                .onSuccess { _categories.value = it.sortedBy { c -> c.order ?: Int.MAX_VALUE } }
                .onFailure { /* ignore category load failures, fallback to static list in UI */ }
            _isLoadingCategories.value = false
        }
    }
}
