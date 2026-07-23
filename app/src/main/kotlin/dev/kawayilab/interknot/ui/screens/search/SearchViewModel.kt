package dev.kawayilab.interknot.ui.screens.search

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.FlowPreview
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.kawayilab.interknot.data.repository.InterknotRepository
import dev.kawayilab.interknot.model.Article
import dev.kawayilab.interknot.model.SearchSuggestion
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: InterknotRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _suggestions = MutableStateFlow<List<SearchSuggestion>>(emptyList())
    val suggestions: StateFlow<List<SearchSuggestion>> = _suggestions.asStateFlow()

    private val _results = MutableStateFlow<List<Article>>(emptyList())
    val results: StateFlow<List<Article>> = _results.asStateFlow()

    private val _history = MutableStateFlow<List<String>>(emptyList())
    val history: StateFlow<List<String>> = _history.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private var start = 0
    private val limit = 20
    private var searchJob: Job? = null
    private var suggestJob: Job? = null

    init {
        observeQueryForSuggestions()
        observeHistory()
    }

    fun setInitial(query: String = "", category: String? = null) {
        _query.value = query
        _selectedCategory.value = category
        if (query.isNotBlank()) {
            search()
        }
    }

    fun setQuery(value: String) {
        _query.value = value
    }

    fun setCategory(slug: String?) {
        _selectedCategory.value = slug
    }

    fun search() {
        _results.value = emptyList()
        _suggestions.value = emptyList()
        _error.value = null
        _hasMore.value = true
        start = 0
        submitSearch()
    }

    fun loadMore() {
        if (_isLoadingMore.value || _isLoading.value || !_hasMore.value) return
        submitSearch()
    }

    private fun submitSearch() {
        searchJob?.cancel()
        val isFirstPage = start == 0
        searchJob = viewModelScope.launch {
            if (isFirstPage) _isLoading.value = true else _isLoadingMore.value = true
            _error.value = null
            val q = _query.value.trim()
            if (q.isEmpty()) {
                _isLoading.value = false
                _isLoadingMore.value = false
                return@launch
            }
            repository.addSearchHistory(q)
            repository.searchArticles(q, start, limit, _selectedCategory.value)
                .onSuccess { page ->
                    _results.value = _results.value + page.items
                    start += page.items.size
                    _hasMore.value = page.hasMore
                }
                .onFailure { _error.value = it.message ?: "搜索失败" }
            _isLoading.value = false
            _isLoadingMore.value = false
        }
    }

    private fun observeQueryForSuggestions() {
        _query
            .debounce(250)
            .distinctUntilChanged()
            .map { it.trim() }
            .onEach { q ->
                if (q.isEmpty() || q.length < 2) {
                    _suggestions.value = emptyList()
                    return@onEach
                }
                suggestJob?.cancel()
                suggestJob = viewModelScope.launch {
                    repository.suggestArticles(q, _selectedCategory.value, 8)
                        .onSuccess { _suggestions.value = it }
                        .onFailure { _suggestions.value = emptyList() }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeHistory() {
        viewModelScope.launch {
            repository.searchHistory.collect { _history.value = it }
        }
    }

    fun clearHistory() {
        viewModelScope.launch { repository.clearSearchHistory() }
    }

    fun addHistoryItem(query: String) {
        viewModelScope.launch { repository.addSearchHistory(query) }
    }
}
