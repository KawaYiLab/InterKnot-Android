package dev.kawayilab.interknot.data.repository

import dev.kawayilab.interknot.data.api.InterknotApi
import dev.kawayilab.interknot.data.api.TokenManager
import dev.kawayilab.interknot.data.local.UserPreferences
import dev.kawayilab.interknot.model.Article
import dev.kawayilab.interknot.model.ArticlePage
import dev.kawayilab.interknot.model.AuthResult
import dev.kawayilab.interknot.model.CommentPage
import dev.kawayilab.interknot.model.LikeResult
import dev.kawayilab.interknot.model.User
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class InterknotRepository @Inject constructor(
    private val api: InterknotApi,
    private val preferences: UserPreferences
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

    suspend fun publishArticle(
        title: String,
        text: String,
        category: String? = null,
        isAnonymous: Boolean = false
    ): Result<String> {
        val authorId = user.value?.authorDocumentId ?: return Result.failure(IllegalStateException("未登录"))
        return api.createArticleDraft(title, text, authorId, category, isAnonymous).onSuccess { documentId ->
            api.publishArticle(documentId).getOrThrow()
        }
    }
}
