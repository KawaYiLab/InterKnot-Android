package dev.kawayilab.interknot.data.api

import dev.kawayilab.interknot.data.api.dto.ArticleDetailDto
import dev.kawayilab.interknot.data.api.dto.ArticleListItemDto
import dev.kawayilab.interknot.data.api.dto.PagedListDto
import dev.kawayilab.interknot.data.api.dto.SingleDto
import dev.kawayilab.interknot.data.api.dto.toDomain
import dev.kawayilab.interknot.model.Article
import dev.kawayilab.interknot.model.ArticlePage
import dev.kawayilab.interknot.model.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject

class InterknotApiImpl @Inject constructor(
    private val client: HttpClient
) : InterknotApi {

    override suspend fun login(username: String, password: String): String {
        return "demo-token"
    }

    override suspend fun getCurrentUser(): User {
        return User(id = "1", username = "demo", email = null)
    }

    override suspend fun getArticles(
        start: Int,
        limit: Int,
        feed: String,
        category: String?
    ): Result<ArticlePage> = runCatching {
        val response: PagedListDto<ArticleListItemDto> = client.get("articles/list") {
            parameter("start", start)
            parameter("limit", limit)
            parameter("feed", feed)
            if (!category.isNullOrBlank()) {
                parameter("category", category)
            }
        }.body()

        val items = response.data.map { it.toDomain() }
        val pagination = response.meta.pagination
        ArticlePage(
            items = items,
            start = pagination.start,
            limit = pagination.limit,
            total = pagination.total,
            hasMore = items.isNotEmpty() && (start + items.size) < pagination.total
        )
    }

    override suspend fun getArticle(documentId: String): Result<Article> = runCatching {
        val response: SingleDto<ArticleDetailDto> =
            client.get("articles/detail/$documentId").body()
        response.data.toDomain()
    }
}
