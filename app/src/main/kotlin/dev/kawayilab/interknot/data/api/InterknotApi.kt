package dev.kawayilab.interknot.data.api

import dev.kawayilab.interknot.model.Article
import dev.kawayilab.interknot.model.ArticlePage
import dev.kawayilab.interknot.model.AuthResult
import dev.kawayilab.interknot.model.Category
import dev.kawayilab.interknot.model.CommentPage
import dev.kawayilab.interknot.model.KnockConversation
import dev.kawayilab.interknot.model.KnockNotification
import dev.kawayilab.interknot.model.LikeResult
import dev.kawayilab.interknot.model.User

interface InterknotApi {
    suspend fun login(identifier: String, password: String): Result<AuthResult>
    suspend fun register(email: String, code: String, password: String): Result<AuthResult>
    suspend fun sendRegisterCode(email: String): Result<Pair<Boolean, Int>>
    suspend fun getCurrentUser(): Result<User>
    suspend fun getArticles(
        start: Int,
        limit: Int,
        feed: String = "recommend",
        category: String? = null
    ): Result<ArticlePage>
    suspend fun getArticle(documentId: String): Result<Article>
    suspend fun getComments(articleDocumentId: String, start: Int, limit: Int): Result<CommentPage>
    suspend fun addComment(
        articleDocumentId: String,
        content: String,
        authorDocumentId: String,
        parentDocumentId: String? = null,
        isAnonymous: Boolean = false
    ): Result<Unit>
    suspend fun toggleLike(targetType: String, targetId: String): Result<LikeResult>
    suspend fun createArticleDraft(
        title: String,
        text: String,
        authorDocumentId: String,
        category: String? = null,
        isAnonymous: Boolean = false
    ): Result<String>
    suspend fun publishArticle(documentId: String): Result<Unit>
    suspend fun searchArticles(
        query: String,
        start: Int,
        limit: Int,
        category: String? = null
    ): Result<ArticlePage>
    suspend fun getCategories(): Result<List<Category>>
    suspend fun suggestArticles(query: String, category: String? = null, limit: Int = 8): Result<List<SearchSuggestion>>
    suspend fun getKnockConversations(): Result<List<KnockConversation>>
    suspend fun getKnockMessages(conversationId: String, cursor: String? = null, limit: Int = 50): Result<KnockMessagePage>
    suspend fun markConversationRead(conversationId: String): Result<Int>
    suspend fun getUnreadNotificationCount(): Result<Int>
    suspend fun getDennyBalance(): Result<DennyBalance>
    suspend fun giveDenny(articleId: String, message: String? = null): Result<DennyGiveResult>
}

data class DennyBalance(
    val denny: Int,
    val dennyGiven: Int,
    val recentLogs: List<DennyLog> = emptyList()
)

data class DennyLog(
    val action: String?,
    val amount: Int?,
    val balance: Int?,
    val description: String?,
    val createdAt: String?
)

data class DennyGiveResult(
    val success: Boolean,
    val message: String?,
    val newBalance: Int?,
    val articleDennyCount: Int?
)

data class KnockMessagePage(
    val items: List<KnockNotification>,
    val nextCursor: String? = null,
    val hasMore: Boolean = false
)

data class SearchSuggestion(
    val documentId: String,
    val title: String,
    val titleHighlighted: String? = null,
    val excerpt: String? = null,
    val authorName: String? = null,
    val categoryName: String? = null,
    val categorySlug: String? = null,
    val isAnonymous: Boolean = false
)
