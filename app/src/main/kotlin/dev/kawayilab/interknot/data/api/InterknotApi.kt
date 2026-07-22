package dev.kawayilab.interknot.data.api

import dev.kawayilab.interknot.model.Article
import dev.kawayilab.interknot.model.ArticlePage
import dev.kawayilab.interknot.model.User

interface InterknotApi {
    suspend fun login(username: String, password: String): String
    suspend fun getCurrentUser(): User
    suspend fun getArticles(
        start: Int,
        limit: Int,
        feed: String = "recommend",
        category: String? = null
    ): Result<ArticlePage>
    suspend fun getArticle(documentId: String): Result<Article>
}
