package dev.kawayilab.interknot.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.kawayilab.interknot.data.repository.InterknotRepository
import dev.kawayilab.interknot.model.Article
import dev.kawayilab.interknot.model.ArticlePage
import dev.kawayilab.interknot.model.Author
import dev.kawayilab.interknot.model.Avatar
import dev.kawayilab.interknot.model.BusinessCard
import dev.kawayilab.interknot.model.Comment
import dev.kawayilab.interknot.model.CommentPage
import dev.kawayilab.interknot.model.PinnedArticlesResponse
import dev.kawayilab.interknot.model.Profile
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: InterknotRepository
) : ViewModel() {

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile.asStateFlow()

    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles.asStateFlow()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _hasMoreArticles = MutableStateFlow(true)
    val hasMoreArticles: StateFlow<Boolean> = _hasMoreArticles.asStateFlow()

    private val _hasMoreComments = MutableStateFlow(true)
    val hasMoreComments: StateFlow<Boolean> = _hasMoreComments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingArticles = MutableStateFlow(false)
    private val _isLoadingComments = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = combine(_isLoadingArticles, _isLoadingComments) { a, c ->
        a || c
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _denny = MutableStateFlow(0)
    val denny: StateFlow<Int> = _denny.asStateFlow()

    private val _dennyGiven = MutableStateFlow(0)
    val dennyGiven: StateFlow<Int> = _dennyGiven.asStateFlow()

    private val _avatars = MutableStateFlow<List<Avatar>>(emptyList())
    val avatars: StateFlow<List<Avatar>> = _avatars.asStateFlow()

    private val _equippedAvatarId = MutableStateFlow<String?>(null)
    val equippedAvatarId: StateFlow<String?> = _equippedAvatarId.asStateFlow()

    private val _businessCards = MutableStateFlow<List<BusinessCard>>(emptyList())
    val businessCards: StateFlow<List<BusinessCard>> = _businessCards.asStateFlow()

    private val _equippedCardId = MutableStateFlow<String?>(null)
    val equippedCardId: StateFlow<String?> = _equippedCardId.asStateFlow()

    private val _pinnedArticles = MutableStateFlow<PinnedArticlesResponse?>(null)
    val pinnedArticles: StateFlow<PinnedArticlesResponse?> = _pinnedArticles.asStateFlow()

    private val _blockedAuthors = MutableStateFlow<List<Author>>(emptyList())
    val blockedAuthors: StateFlow<List<Author>> = _blockedAuthors.asStateFlow()

    private var blockedStart = 0
    private val blockedPageSize = 20
    private var _hasMoreBlocked = true
    private val _isLoadingBlocked = MutableStateFlow(false)

    private var articleStart = 0
    private var commentStart = 0
    private val pageSize = 20

    private fun profileDocumentId(documentId: String?): String? {
        return documentId ?: repository.user.value?.authorDocumentId
    }

    fun loadProfile(documentId: String?) {
        val id = profileDocumentId(documentId) ?: run {
            _error.value = "未登录"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getProfile(id)
                .onSuccess {
                    _profile.value = it
                    articleStart = 0
                    commentStart = 0
                    _articles.value = emptyList()
                    _comments.value = emptyList()
                    _hasMoreArticles.value = true
                    _hasMoreComments.value = true
                    loadArticles(documentId, refresh = true)
                    loadComments(documentId, refresh = true)
                    if (it.isSelf) loadBalance()
                }
                .onFailure { _error.value = it.message ?: "加载失败" }
            _isLoading.value = false
        }
    }

    fun loadArticles(documentId: String?, refresh: Boolean = false) {
        if (_isLoadingArticles.value) return
        val id = profileDocumentId(documentId) ?: return
        if (refresh) articleStart = 0
        if (articleStart != 0 && !_hasMoreArticles.value) return
        viewModelScope.launch {
            _isLoadingArticles.value = true
            repository.getProfileArticles(id, articleStart, pageSize)
                .onSuccess { page ->
                    _articles.value = if (refresh) page.items else _articles.value + page.items
                    articleStart += page.items.size
                    _hasMoreArticles.value = page.items.size >= pageSize || articleStart < page.total
                }
                .onFailure { _error.value = it.message ?: "加载帖子失败" }
            _isLoadingArticles.value = false
        }
    }

    fun loadComments(documentId: String?, refresh: Boolean = false) {
        if (_isLoadingComments.value) return
        val id = profileDocumentId(documentId) ?: return
        if (refresh) commentStart = 0
        if (commentStart != 0 && !_hasMoreComments.value) return
        viewModelScope.launch {
            _isLoadingComments.value = true
            repository.getProfileComments(id, commentStart, pageSize)
                .onSuccess { page ->
                    _comments.value = if (refresh) page.items else _comments.value + page.items
                    commentStart += page.items.size
                    _hasMoreComments.value = page.items.size >= pageSize || commentStart < page.total
                }
                .onFailure { _error.value = it.message ?: "加载评论失败" }
            _isLoadingComments.value = false
        }
    }

    fun toggleFollow() {
        val id = _profile.value?.author?.documentId ?: return
        viewModelScope.launch {
            repository.toggleFollow(id)
                .onSuccess { result ->
                    _profile.value = _profile.value?.copy(
                        author = _profile.value?.author?.copy(
                            isFollowing = result.following,
                            followersCount = result.followersCount
                        ) ?: return@launch
                    )
                }
                .onFailure { _error.value = it.message ?: "操作失败" }
        }
    }

    fun updateName(name: String, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            repository.updateName(name)
                .onSuccess {
                    _profile.value = _profile.value?.copy(
                        author = _profile.value?.author?.copy(name = it.name ?: name) ?: return@onSuccess)
                    repository.fetchCurrentUser()
                    onDone(true)
                }
                .onFailure { _error.value = it.message ?: "修改昵称失败"; onDone(false) }
        }
    }

    fun updateBio(bio: String, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            repository.updateBio(bio)
                .onSuccess { result ->
                    _profile.value = _profile.value?.copy(
                        author = _profile.value?.author?.copy(
                            bio = result.bio ?: bio
                        ) ?: return@onSuccess
                    )
                    onDone(true)
                }
                .onFailure { _error.value = it.message ?: "修改签名失败"; onDone(false) }
        }
    }

    fun updateVisibility(hidden: Boolean, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            repository.updateVisibility(hidden)
                .onSuccess { result ->
                    _profile.value = _profile.value?.copy(
                        author = _profile.value?.author?.copy(
                            isHidden = result.profileHidden
                        ) ?: return@onSuccess
                    )
                    onDone(true)
                }
                .onFailure { _error.value = it.message ?: "修改可见性失败"; onDone(false) }
        }
    }

    fun loadBalance() {
        viewModelScope.launch {
            repository.getDennyBalance()
                .onSuccess {
                    _denny.value = it.denny
                    _dennyGiven.value = it.dennyGiven
                }
        }
    }

    fun loadAvatars() {
        viewModelScope.launch {
            repository.getAvatars()
                .onSuccess { (list, equipped) ->
                    _avatars.value = list
                    _equippedAvatarId.value = equipped
                }
                .onFailure { _error.value = it.message ?: "加载头像失败" }
        }
    }

    fun equipAvatar(documentId: String?) {
        viewModelScope.launch {
            repository.equipAvatar(documentId)
                .onSuccess { _equippedAvatarId.value = it }
                .onFailure { _error.value = it.message ?: "装备头像失败" }
        }
    }

    fun loadBusinessCards() {
        viewModelScope.launch {
            repository.getBusinessCards()
                .onSuccess { (list, equipped) ->
                    _businessCards.value = list
                    _equippedCardId.value = equipped
                }
                .onFailure { _error.value = it.message ?: "加载名片失败" }
        }
    }

    fun equipBusinessCard(documentId: String?) {
        viewModelScope.launch {
            repository.equipBusinessCard(documentId)
                .onSuccess { _equippedCardId.value = it }
                .onFailure { _error.value = it.message ?: "装备名片失败" }
        }
    }

    fun loadPinnedArticles() {
        viewModelScope.launch {
            repository.getPinnedArticles()
                .onSuccess { _pinnedArticles.value = it }
                .onFailure { _error.value = it.message ?: "加载置顶失败" }
        }
    }

    fun updatePinnedArticles(pinned: List<String>?) {
        viewModelScope.launch {
            repository.updatePinnedArticles(pinned)
                .onSuccess { loadPinnedArticles() }
                .onFailure { _error.value = it.message ?: "更新置顶失败" }
        }
    }

    fun loadBlockedAuthors(refresh: Boolean = false) {
        if (_isLoadingBlocked.value) return
        if (refresh) blockedStart = 0
        if (blockedStart != 0 && !_hasMoreBlocked) return
        viewModelScope.launch {
            _isLoadingBlocked.value = true
            repository.getBlockedAuthors(blockedStart, blockedPageSize)
                .onSuccess { list ->
                    _blockedAuthors.value = if (refresh) list else _blockedAuthors.value + list
                    blockedStart += list.size
                    _hasMoreBlocked = list.size >= blockedPageSize
                }
                .onFailure { _error.value = it.message ?: "加载黑名单失败" }
            _isLoadingBlocked.value = false
        }
    }

    fun unblockAuthor(authorDocumentId: String) {
        viewModelScope.launch {
            repository.toggleBlock(authorDocumentId)
                .onSuccess {
                    _blockedAuthors.value = _blockedAuthors.value.filter { it.documentId != authorDocumentId }
                }
                .onFailure { _error.value = it.message ?: "取消拉黑失败" }
        }
    }

    fun logout(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.logout()
            _denny.value = 0
            _dennyGiven.value = 0
            onComplete()
        }
    }
}
