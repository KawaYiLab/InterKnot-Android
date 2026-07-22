package dev.kawayilab.interknot.data.api

import dev.kawayilab.interknot.model.Article
import dev.kawayilab.interknot.model.ArticlePage
import dev.kawayilab.interknot.model.AuthResult
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
}
