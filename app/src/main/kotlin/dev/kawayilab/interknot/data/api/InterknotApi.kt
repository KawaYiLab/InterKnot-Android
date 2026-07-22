package dev.kawayilab.interknot.data.api

import dev.kawayilab.interknot.model.Article
import dev.kawayilab.interknot.model.ArticlePage
import dev.kawayilab.interknot.model.AuthResult
import dev.kawayilab.interknot.model.Category
import dev.kawayilab.interknot.model.CommentPage
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
}

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
