package dev.kawayilab.interknot.data.api

import dev.kawayilab.interknot.data.api.dto.ArticleDetailDto
import dev.kawayilab.interknot.data.api.dto.ArticleListItemDto
import dev.kawayilab.interknot.data.api.dto.AuthResponseDto
import dev.kawayilab.interknot.data.api.dto.CategoryDto
import dev.kawayilab.interknot.data.api.dto.CodeResultDto
import dev.kawayilab.interknot.data.api.dto.CommentListResponseDto
import dev.kawayilab.interknot.data.api.dto.DataListDto
import dev.kawayilab.interknot.data.api.dto.LikeResultDto
import dev.kawayilab.interknot.data.api.dto.PagedListDto
import dev.kawayilab.interknot.data.api.dto.SearchSuggestionDto
import dev.kawayilab.interknot.data.api.dto.SingleDto
import dev.kawayilab.interknot.data.api.dto.UserDto
import dev.kawayilab.interknot.data.api.dto.toDomain
import dev.kawayilab.interknot.model.Article
import dev.kawayilab.interknot.model.ArticlePage
import dev.kawayilab.interknot.model.AuthResult
import dev.kawayilab.interknot.model.CommentPage
import dev.kawayilab.interknot.model.LikeResult
import dev.kawayilab.interknot.model.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class InterknotApiImpl @Inject constructor(
    private val client: HttpClient
) : InterknotApi {

    override suspend fun login(identifier: String, password: String): Result<AuthResult> = runCatching {
        val response: AuthResponseDto = client.authPost("auth/local") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("identifier" to identifier, "password" to password))
        }
        TokenManager.token = response.jwt
        response.toDomain()
    }

    override suspend fun register(email: String, code: String, password: String): Result<AuthResult> = runCatching {
        val response: AuthResponseDto = client.authPost("auth/register-with-code") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("email" to email, "code" to code, "password" to password))
        }
        TokenManager.token = response.jwt
        response.toDomain()
    }

    override suspend fun sendRegisterCode(email: String): Result<Pair<Boolean, Int>> = runCatching {
        val response: CodeResultDto = client.authPost("auth/send-register-code") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("email" to email))
        }
        (response.sent == true) to (response.cooldown ?: response.expiresIn ?: 60)
    }

    override suspend fun getCurrentUser(): Result<User> = runCatching {
        val response: UserDto = client.authGet("me/profile")
        response.toDomain()
    }

    override suspend fun getArticles(
        start: Int,
        limit: Int,
        feed: String,
        category: String?
    ): Result<ArticlePage> = runCatching {
        val response: PagedListDto<ArticleListItemDto> = client.authGet("articles/list") {
            parameter("start", start)
            parameter("limit", limit)
            parameter("feed", feed)
            if (!category.isNullOrBlank()) {
                parameter("category", category)
            }
        }

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
        val response: SingleDto<ArticleDetailDto> = client.authGet("articles/detail/$documentId")
        response.data.toDomain()
    }

    override suspend fun getComments(
        articleDocumentId: String,
        start: Int,
        limit: Int
    ): Result<CommentPage> = runCatching {
        val response: CommentListResponseDto = client.authGet("comments/list") {
            parameter("article", articleDocumentId)
            parameter("start", start)
            parameter("limit", limit)
        }
        response.toDomain(start)
    }

    override suspend fun addComment(
        articleDocumentId: String,
        content: String,
        authorDocumentId: String,
        parentDocumentId: String?,
        isAnonymous: Boolean
    ): Result<Unit> = runCatching {
        client.authPost<Unit>("comments") {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "data" to buildMap {
                        put("article", articleDocumentId)
                        put("content", content)
                        put("author", authorDocumentId)
                        if (!parentDocumentId.isNullOrBlank()) put("parent", parentDocumentId)
                        if (isAnonymous) put("isAnonymous", true)
                    }
                )
            )
        }
        Unit
    }

    override suspend fun toggleLike(targetType: String, targetId: String): Result<LikeResult> = runCatching {
        val response: LikeResultDto = client.authPost("likes/toggle") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("targetType" to targetType, "targetId" to targetId))
        }
        response.toDomain()
    }

    override suspend fun createArticleDraft(
        title: String,
        text: String,
        authorDocumentId: String,
        category: String?,
        isAnonymous: Boolean
    ): Result<String> = runCatching {
        val response: SingleDto<ArticleDetailDto> = client.authPost("articles") {
            parameter("status", "draft")
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "data" to buildMap {
                        put("title", title)
                        put("text", text)
                        put("author", mapOf("connect" to listOf(mapOf("documentId" to authorDocumentId))))
                        if (!category.isNullOrBlank()) put("category", category)
                        if (isAnonymous) put("isAnonymous", true)
                    }
                )
            )
        }
        response.data.documentId
    }

    override suspend fun publishArticle(documentId: String): Result<Unit> = runCatching {
        client.authPost<Unit>("articles/$documentId/publish") {
            contentType(ContentType.Application.Json)
            setBody(mapOf<String, String>())
        }
        Unit
    }

    override suspend fun searchArticles(
        query: String,
        start: Int,
        limit: Int,
        category: String?
    ): Result<ArticlePage> = runCatching {
        val response: PagedListDto<ArticleListItemDto> = client.authGet("articles/search") {
            parameter("q", query.trim())
            parameter("start", start)
            parameter("limit", limit)
            if (!category.isNullOrBlank()) parameter("category", category)
        }

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

    override suspend fun getCategories(): Result<List<dev.kawayilab.interknot.model.Category>> = runCatching {
        val response: DataListDto<CategoryDto> = client.authGet("categories/list")
        response.data.map { it.toDomain() }
    }

    override suspend fun suggestArticles(
        query: String,
        category: String?,
        limit: Int
    ): Result<List<SearchSuggestion>> = runCatching {
        val response: DataListDto<SearchSuggestionDto> = client.authGet("articles/suggest") {
            parameter("q", query.trim())
            parameter("limit", limit)
            if (!category.isNullOrBlank()) parameter("category", category)
        }
        response.data.map { it.toDomain() }
    }
}

private suspend inline fun <reified T> HttpClient.authGet(
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {}
): T = get(urlString) {
    appendTokenIfNeeded()
    block()
}.body()

private suspend inline fun <reified T> HttpClient.authPost(
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {}
): T = post(urlString) {
    appendTokenIfNeeded()
    block()
}.body()
