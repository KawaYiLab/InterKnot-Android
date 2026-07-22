package dev.kawayilab.interknot.ui.screens.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.kawayilab.interknot.data.repository.InterknotRepository
import dev.kawayilab.interknot.model.Article
import dev.kawayilab.interknot.model.Comment
import dev.kawayilab.interknot.model.LikeResult
import javax.inject.Inject
import kotlinx.coroutines.Job
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

    private val _pinnedComment = MutableStateFlow<Comment?>(null)
    val pinnedComment: StateFlow<Comment?> = _pinnedComment.asStateFlow()

    private val _replyTo = MutableStateFlow<Comment?>(null)
    val replyTo: StateFlow<Comment?> = _replyTo.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingComments = MutableStateFlow(false)
    val isLoadingComments: StateFlow<Boolean> = _isLoadingComments.asStateFlow()

    private val _hasMoreComments = MutableStateFlow(true)
    val hasMoreComments: StateFlow<Boolean> = _hasMoreComments.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentDocumentId: String? = null
    private var commentsTotal = 0
    private var commentsHasMore = false
    private val pageSize = 20
    private var commentsJob: Job? = null

    fun load(documentId: String, forceRefresh: Boolean = false) {
        if (!forceRefresh && currentDocumentId == documentId && _article.value != null) {
            refreshSocialState()
            return
        }
        currentDocumentId = documentId
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getArticle(documentId)
                .onSuccess { article ->
                    _article.value = article
                    repository.recordArticleView(documentId)
                    refreshSocialState()
                }
                .onFailure { _error.value = it.message ?: "加载失败" }
            _isLoading.value = false
        }
        loadComments(documentId, reset = true)
    }

    private fun refreshSocialState() {
        val article = _article.value ?: return
        val authorId = article.author?.documentId ?: return
        val meId = repository.user.value?.authorDocumentId
        if (meId == null || meId == authorId) return

        viewModelScope.launch {
            repository.checkFollow(listOf(authorId))
                .onSuccess { result ->
                    updateAuthor { it.copy(isFollowing = result[authorId] ?: it.isFollowing) }
                }
            repository.checkBlock(listOf(authorId))
                .onSuccess { result ->
                    updateAuthor { it.copy(isBlockedByMe = result[authorId] ?: it.isBlockedByMe) }
                }
        }
    }

    fun loadComments(documentId: String, reset: Boolean = false) {
        val start = if (reset) 0 else _comments.value.size
        if (!reset && _isLoadingComments.value) return
        commentsJob?.cancel()
        commentsJob = viewModelScope.launch {
            _isLoadingComments.value = true
            repository.getComments(documentId, start, pageSize)
                .onSuccess { page ->
                    commentsTotal = page.total
                    commentsHasMore = page.hasMore
                    _hasMoreComments.value = page.hasMore
                    if (reset) {
                        _pinnedComment.value = page.pinned
                        _comments.value = page.items
                    } else {
                        _comments.value = _comments.value + page.items
                    }
                }
                .onFailure { _error.value = it.message ?: "评论加载失败" }
            _isLoadingComments.value = false
        }
    }

    fun loadMoreComments() {
        val documentId = currentDocumentId ?: return
        if (_isLoadingComments.value || !commentsHasMore) return
        loadComments(documentId, reset = false)
    }

    fun setReplyTo(comment: Comment?) {
        _replyTo.value = comment
    }

    fun sendComment(content: String, onResult: (Result<Unit>) -> Unit = {}) {
        if (content.isBlank()) return
        val postId = currentDocumentId ?: return
        val parentId = _replyTo.value?.documentId
        viewModelScope.launch {
            repository.addComment(postId, content.trim(), parentId)
                .onSuccess {
                    _replyTo.value = null
                    loadComments(postId, reset = true)
                    onResult(Result.success(Unit))
                }
                .onFailure {
                    _error.value = it.message ?: "评论失败"
                    onResult(Result.failure(it))
                }
        }
    }

    fun toggleLikeArticle(onResult: (Result<LikeResult>) -> Unit = {}) {
        val documentId = currentDocumentId ?: return
        viewModelScope.launch {
            repository.toggleLike("article", documentId)
                .onSuccess { result ->
                    updateArticleLike(result)
                    onResult(Result.success(result))
                }
                .onFailure { onResult(Result.failure(it)) }
        }
    }

    fun toggleFavoriteArticle(onResult: (Result<Unit>) -> Unit = {}) {
        val documentId = currentDocumentId ?: return
        viewModelScope.launch {
            repository.toggleFavorite(documentId)
                .onSuccess { result ->
                    _article.value = _article.value?.copy(
                        favorited = result.favorited,
                        favoritesCount = result.favoritesCount
                    )
                    onResult(Result.success(Unit))
                }
                .onFailure { onResult(Result.failure(it)) }
        }
    }

    fun tripleArticle(onResult: (Result<Unit>) -> Unit = {}) {
        val documentId = currentDocumentId ?: return
        viewModelScope.launch {
            repository.tripleArticle(documentId)
                .onSuccess { result ->
                    _article.value = _article.value?.copy(
                        liked = result.liked,
                        likesCount = result.likesCount,
                        favorited = result.favorited,
                        favoritesCount = result.favoritesCount,
                        hasGivenDenny = result.coinGiven || (_article.value?.hasGivenDenny ?: false),
                        dennyCount = result.dennyCount
                    )
                    onResult(Result.success(Unit))
                }
                .onFailure { onResult(Result.failure(it)) }
        }
    }

    fun toggleFollowAuthor(onResult: (Result<Unit>) -> Unit = {}) {
        val authorId = _article.value?.author?.documentId ?: return
        viewModelScope.launch {
            repository.toggleFollow(authorId)
                .onSuccess { result ->
                    updateAuthor { it.copy(isFollowing = result.following, followersCount = result.followersCount) }
                    onResult(Result.success(Unit))
                }
                .onFailure { onResult(Result.failure(it)) }
        }
    }

    fun blockAuthor(onResult: (Result<Unit>) -> Unit = {}) {
        val authorId = _article.value?.author?.documentId ?: return
        viewModelScope.launch {
            repository.toggleBlock(authorId)
                .onSuccess { result ->
                    updateAuthor {
                        it.copy(
                            isBlockedByMe = result.blocked,
                            isFollowing = if (result.blocked) false else it.isFollowing
                        )
                    }
                    onResult(Result.success(Unit))
                }
                .onFailure { onResult(Result.failure(it)) }
        }
    }

    fun reportArticle(reason: String, detail: String? = null, onResult: (Result<Unit>) -> Unit = {}) {
        val documentId = currentDocumentId ?: return
        viewModelScope.launch {
            repository.createReport("article", documentId, reason, detail)
                .onSuccess { onResult(Result.success(Unit)) }
                .onFailure { onResult(Result.failure(it)) }
        }
    }

    fun giveDenny(message: String? = null, onResult: (Result<Unit>) -> Unit = {}) {
        val documentId = currentDocumentId ?: return
        viewModelScope.launch {
            repository.giveDenny(documentId, message)
                .onSuccess { result ->
                    _article.value = _article.value?.copy(
                        dennyCount = result.articleDennyCount ?: (_article.value?.dennyCount?.plus(1) ?: 1),
                        hasGivenDenny = true
                    )
                    onResult(Result.success(Unit))
                }
                .onFailure { onResult(Result.failure(it)) }
        }
    }

    fun toggleLikeComment(comment: Comment, onResult: (Result<LikeResult>) -> Unit = {}) {
        viewModelScope.launch {
            repository.toggleLike("comment", comment.documentId)
                .onSuccess { result ->
                    updateCommentLike(comment.documentId, result.liked, result.likesCount)
                    onResult(Result.success(result))
                }
                .onFailure { onResult(Result.failure(it)) }
        }
    }

    private fun updateArticleLike(result: LikeResult) {
        _article.value = _article.value?.copy(
            liked = result.liked,
            likesCount = result.likesCount
        )
    }

    private fun updateAuthor(transform: (dev.kawayilab.interknot.model.Author) -> dev.kawayilab.interknot.model.Author) {
        _article.value = _article.value?.copy(author = _article.value?.author?.let(transform))
    }

    private fun updateCommentLike(documentId: String, liked: Boolean, likesCount: Int) {
        _pinnedComment.value?.let { pinned ->
            if (pinned.documentId == documentId) {
                _pinnedComment.value = pinned.copy(liked = liked, likesCount = likesCount)
            }
        }
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

    fun dismissError() {
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        commentsJob?.cancel()
    }
}
