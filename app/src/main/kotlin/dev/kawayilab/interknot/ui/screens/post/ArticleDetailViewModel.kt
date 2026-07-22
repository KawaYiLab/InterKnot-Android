package dev.kawayilab.interknot.ui.screens.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.kawayilab.interknot.data.repository.InterknotRepository
import dev.kawayilab.interknot.model.Article
import dev.kawayilab.interknot.model.Comment
import dev.kawayilab.interknot.model.LikeResult
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
    private val repository: InterknotRepository
) : ViewModel() {

    private val _article = MutableStateFlow<Article?>(null)
    val article: StateFlow<Article?> = _article.asStateFlow()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingComments = MutableStateFlow(false)
    val isLoadingComments: StateFlow<Boolean> = _isLoadingComments.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentDocumentId: String? = null
    private var commentsTotal = 0
    private var commentsHasMore = false
    private val pageSize = 20

    fun load(documentId: String, forceRefresh: Boolean = false) {
        if (!forceRefresh && currentDocumentId == documentId && _article.value != null) return
        currentDocumentId = documentId
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getArticle(documentId)
                .onSuccess { _article.value = it }
                .onFailure { _error.value = it.message ?: "加载失败" }
            _isLoading.value = false
        }
        loadComments(documentId, reset = true)
    }

    fun loadComments(documentId: String, reset: Boolean = false) {
        val start = if (reset) 0 else _comments.value.size
        if (!reset && _isLoadingComments.value) return
        viewModelScope.launch {
            _isLoadingComments.value = true
            repository.getComments(documentId, start, pageSize)
                .onSuccess { page ->
                    commentsTotal = page.total
                    commentsHasMore = page.hasMore
                    _comments.value = if (reset) page.items else _comments.value + page.items
                }
                .onFailure { /* silently fail for comments, detail page still shows article */ }
            _isLoadingComments.value = false
        }
    }

    fun sendComment(content: String, parentDocumentId: String? = null, onResult: (Result<Unit>) -> Unit) {
        val postId = currentDocumentId ?: return
        viewModelScope.launch {
            repository.addComment(postId, content.trim(), parentDocumentId)
                .onSuccess {
                    loadComments(postId, reset = true)
                    onResult(Result.success(Unit))
                }
                .onFailure { onResult(Result.failure(it)) }
        }
    }

    fun toggleLikeArticle(onResult: (Result<LikeResult>) -> Unit) {
        val documentId = currentDocumentId ?: return
        viewModelScope.launch {
            repository.toggleLike("article", documentId)
                .onSuccess { result ->
                    _article.value = _article.value?.copy(
                        liked = result.liked,
                        likesCount = result.likesCount
                    )
                    onResult(Result.success(result))
                }
                .onFailure { onResult(Result.failure(it)) }
        }
    }

    fun toggleLikeComment(comment: Comment, onResult: (Result<LikeResult>) -> Unit) {
        viewModelScope.launch {
            repository.toggleLike("comment", comment.documentId)
                .onSuccess { result ->
                    updateCommentLike(comment.documentId, result.liked, result.likesCount)
                    onResult(Result.success(result))
                }
                .onFailure { onResult(Result.failure(it)) }
        }
    }

    private fun updateCommentLike(documentId: String, liked: Boolean, likesCount: Int) {
        _comments.value = _comments.value.map { c ->
            if (c.documentId == documentId) {
                c.copy(liked = liked, likesCount = likesCount)
            } else {
                c.copy(replies = c.replies.map { r ->
                    if (r.documentId == documentId) r.copy(liked = liked, likesCount = likesCount) else r
                })
            }
        }
    }
}
