package dev.kawayilab.interknot.data.api

import dev.kawayilab.interknot.data.api.dto.ArticleDetailDto
import dev.kawayilab.interknot.data.api.dto.ArticleListItemDto
import dev.kawayilab.interknot.data.api.dto.AuthResponseDto
import dev.kawayilab.interknot.data.api.dto.CodeResultDto
import dev.kawayilab.interknot.data.api.dto.PagedListDto
import dev.kawayilab.interknot.data.api.dto.SingleDto
import dev.kawayilab.interknot.data.api.dto.UserDto
import dev.kawayilab.interknot.data.api.dto.toDomain
import dev.kawayilab.interknot.model.Article
import dev.kawayilab.interknot.model.ArticlePage
import dev.kawayilab.interknot.model.AuthResult
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
