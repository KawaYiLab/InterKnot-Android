package dev.kawayilab.interknot.data.repository

import dev.kawayilab.interknot.data.api.InterknotApi
import dev.kawayilab.interknot.data.api.TokenManager
import dev.kawayilab.interknot.data.local.UserPreferences
import dev.kawayilab.interknot.data.local.cache.CachedSearch
import dev.kawayilab.interknot.data.local.cache.CachedSearchDao
import dev.kawayilab.interknot.model.Article
import dev.kawayilab.interknot.model.ArticlePage
import dev.kawayilab.interknot.model.AuthResult
import dev.kawayilab.interknot.model.Category
import dev.kawayilab.interknot.model.CommentPage
import dev.kawayilab.interknot.model.DennyBalance
import dev.kawayilab.interknot.model.DennyGiveResult
import dev.kawayilab.interknot.model.Benefits
import dev.kawayilab.interknot.model.ExamReview
import dev.kawayilab.interknot.model.ExamStartResult
import dev.kawayilab.interknot.model.ExamStatus
import dev.kawayilab.interknot.model.ExamSubmitResult
import dev.kawayilab.interknot.model.FavoriteResult
import dev.kawayilab.interknot.model.FollowResult
import dev.kawayilab.interknot.model.KnockConversation
import dev.kawayilab.interknot.model.KnockNotification
import dev.kawayilab.interknot.model.LikeResult
import dev.kawayilab.interknot.model.ReportResult
import dev.kawayilab.interknot.model.SearchSuggestion
import dev.kawayilab.interknot.model.TripleResult
import dev.kawayilab.interknot.model.User
import dev.kawayilab.interknot.model.BlockResult
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class InterknotRepository @Inject constructor(
    private val api: InterknotApi,
    private val preferences: UserPreferences,
    private val cachedSearchDao: CachedSearchDao,
    private val json: Json
) {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    val isLoggedIn: Flow<Boolean> = user.map { it != null }

    val token: Flow<String?> = preferences.token

    init {
        // todo: load user from DataStore on app start? UserPreferences.user flow can be collected externally.
    }

    suspend fun loadSessionFromStorage(): User? {
        TokenManager.token = preferences.token.first()
        val storedUser = preferences.user.first()
        _user.value = storedUser
        return storedUser
    }

    suspend fun login(identifier: String, password: String): Result<AuthResult> {
        return api.login(identifier, password).onSuccess { result ->
            preferences.saveSession(result.token, result.user)
            _user.value = result.user
            refreshSelfUser()
        }
    }

    suspend fun register(email: String, code: String, password: String): Result<AuthResult> {
        return api.register(email, code, password).onSuccess { result ->
            preferences.saveSession(result.token, result.user)
            _user.value = result.user
            refreshSelfUser()
        }
    }

    private suspend fun refreshSelfUser() {
        api.getCurrentUser().onSuccess { user ->
            val token = TokenManager.token ?: preferences.token.first() ?: return
            preferences.saveSession(token, user)
            _user.value = user
        }
    }

    suspend fun sendRegisterCode(email: String): Result<Pair<Boolean, Int>> {
        return api.sendRegisterCode(email)
    }

    suspend fun fetchCurrentUser(): Result<User> {
        return api.getCurrentUser().onSuccess { user ->
            val token = TokenManager.token ?: preferences.token.first() ?: ""
            preferences.saveSession(token, user)
            _user.value = user
        }
    }

    suspend fun logout() {
        preferences.clearSession()
        _user.value = null
    }

    suspend fun getArticles(
        start: Int,
        limit: Int,
        feed: String = "recommend",
        category: String? = null
    ): Result<ArticlePage> = api.getArticles(start, limit, feed, category)

    suspend fun getArticle(documentId: String): Result<Article> = api.getArticle(documentId)

    suspend fun getComments(articleDocumentId: String, start: Int, limit: Int): Result<CommentPage> =
        api.getComments(articleDocumentId, start, limit)

    suspend fun addComment(
        articleDocumentId: String,
        content: String,
        parentDocumentId: String? = null,
        isAnonymous: Boolean = false
    ): Result<Unit> {
        val authorId = user.value?.authorDocumentId ?: return Result.failure(IllegalStateException("未登录"))
        return api.addComment(articleDocumentId, content, authorId, parentDocumentId, isAnonymous)
    }

    suspend fun toggleLike(targetType: String, targetId: String): Result<LikeResult> =
        api.toggleLike(targetType, targetId)

    suspend fun toggleFavorite(articleDocumentId: String): Result<FavoriteResult> =
        api.toggleFavorite(articleDocumentId)

    suspend fun tripleArticle(articleId: String): Result<TripleResult> =
        api.tripleArticle(articleId)

    suspend fun toggleFollow(authorDocumentId: String): Result<FollowResult> =
        api.toggleFollow(authorDocumentId)

    suspend fun checkFollow(authorDocumentIds: List<String>): Result<Map<String, Boolean>> =
        api.checkFollow(authorDocumentIds)

    suspend fun checkBlock(authorDocumentIds: List<String>): Result<Map<String, Boolean>> =
        api.checkBlock(authorDocumentIds)

    suspend fun recordArticleView(documentId: String): Result<Unit> =
        api.recordArticleView(documentId)

    suspend fun createReport(
        targetType: String,
        targetId: String,
        reason: String,
        detail: String? = null
    ): Result<ReportResult> = api.createReport(targetType, targetId, reason, detail)

    suspend fun toggleBlock(authorDocumentId: String): Result<BlockResult> =
        api.toggleBlock(authorDocumentId)

    suspend fun publishArticle(
        title: String,
        text: String,
        category: String? = null,
        isAnonymous: Boolean = false,
        coverDocumentIds: List<String>? = null
    ): Result<String> {
        val authorId = user.value?.authorDocumentId ?: return Result.failure(IllegalStateException("未登录"))
        return runCatching {
            val documentId = api.createArticleDraft(title, text, authorId, category, isAnonymous, coverDocumentIds).getOrThrow()
            api.publishArticle(documentId).getOrThrow()
            documentId
        }
    }

    suspend fun searchArticles(
        query: String,
        start: Int = 0,
        limit: Int = 20,
        category: String? = null
    ): Result<ArticlePage> {
        val cached = if (start == 0) getCachedSearch(query, category) else null
        return api.searchArticles(query, start, limit, category)
            .onSuccess { page ->
                if (start == 0 || page.items.isNotEmpty()) {
                    cacheSearch(query, category, page)
                }
            }
            .recoverCatching { cached ?: throw it }
    }

    suspend fun getCachedSearch(query: String, category: String? = null): ArticlePage? {
        return cachedSearchDao.get(query, category ?: "")?.let { cached ->
            runCatching { json.decodeFromString(ArticlePage.serializer(), cached.resultsJson ?: "") }.getOrNull()
        }
    }

    private suspend fun cacheSearch(query: String, category: String? = null, page: ArticlePage) {
        runCatching {
            cachedSearchDao.insert(
                CachedSearch(
                    query = query,
                    category = category ?: "",
                    resultsJson = json.encodeToString(ArticlePage.serializer(), page),
                    total = page.total,
                    cachedAt = System.currentTimeMillis()
                )
            )
        }
    }

    val searchHistory: Flow<List<String>> = preferences.searchHistory

    suspend fun addSearchHistory(query: String) = preferences.addSearchHistory(query)
    suspend fun clearSearchHistory() = preferences.clearSearchHistory()

    suspend fun getCategories(): Result<List<Category>> = api.getCategories()

    suspend fun getExamStatus(): Result<ExamStatus> = api.getExamStatus()
    suspend fun startExam(): Result<ExamStartResult> = api.startExam()
    suspend fun submitExam(attemptId: String, answers: Map<String, List<String>>): Result<ExamSubmitResult> = api.submitExam(attemptId, answers)
    suspend fun getExamReview(attemptId: String? = null): Result<ExamReview> = api.getExamReview(attemptId)
    suspend fun getBenefits(): Result<Benefits> = api.getBenefits()

    suspend fun suggestArticles(
        query: String,
        category: String? = null,
        limit: Int = 8
    ): Result<List<SearchSuggestion>> = api.suggestArticles(query, category, limit)

    suspend fun getKnockConversations(): Result<List<KnockConversation>> = api.getKnockConversations()

    suspend fun getKnockMessages(conversationId: String, cursor: String? = null, limit: Int = 50): Result<List<KnockNotification>> =
        api.getKnockMessages(conversationId, cursor, limit).map { it.items }

    suspend fun markConversationRead(conversationId: String): Result<Int> = api.markConversationRead(conversationId)

    suspend fun getUnreadNotificationCount(): Result<Int> = api.getUnreadNotificationCount()

    suspend fun getDennyBalance(): Result<DennyBalance> = api.getDennyBalance()

    suspend fun giveDenny(articleId: String, message: String? = null): Result<DennyGiveResult> = api.giveDenny(articleId, message)
}
