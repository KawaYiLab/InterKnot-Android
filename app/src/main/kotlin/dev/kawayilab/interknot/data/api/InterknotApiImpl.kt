package dev.kawayilab.interknot.data.api

import dev.kawayilab.interknot.data.api.dto.ArticleDraftItemDto
import dev.kawayilab.interknot.data.api.dto.ArticleDetailDto
import dev.kawayilab.interknot.data.api.dto.ArticleListItemDto
import dev.kawayilab.interknot.data.api.dto.ArticleRefDto
import dev.kawayilab.interknot.data.api.dto.AuthResponseDto
import dev.kawayilab.interknot.data.api.dto.AuthorSearchItemDto
import dev.kawayilab.interknot.data.api.dto.BioUpdateResultDto
import dev.kawayilab.interknot.data.api.dto.BlockCheckResultDto
import dev.kawayilab.interknot.data.api.dto.BlockResultDto
import dev.kawayilab.interknot.data.api.dto.CategoryDto
import dev.kawayilab.interknot.data.api.dto.CodeResultDto
import dev.kawayilab.interknot.data.api.dto.CommentDto
import dev.kawayilab.interknot.data.api.dto.CommentListMetaDto
import dev.kawayilab.interknot.data.api.dto.CommentListResponseDto
import dev.kawayilab.interknot.data.api.dto.DataListDto
import dev.kawayilab.interknot.data.api.dto.BenefitsDto
import dev.kawayilab.interknot.data.api.dto.DennyBalanceDto
import dev.kawayilab.interknot.data.api.dto.ExamReviewDto
import dev.kawayilab.interknot.data.api.dto.ExamStartResultDto
import dev.kawayilab.interknot.data.api.dto.ExamStatusDto
import dev.kawayilab.interknot.data.api.dto.ExamSubmitResultDto
import dev.kawayilab.interknot.data.api.dto.DennyGiveResponseDto
import dev.kawayilab.interknot.data.api.dto.FavoriteCheckResultDto
import dev.kawayilab.interknot.data.api.dto.FavoriteListItemDto
import dev.kawayilab.interknot.data.api.dto.FavoriteResultDto
import dev.kawayilab.interknot.data.api.dto.FollowCheckResultDto
import dev.kawayilab.interknot.data.api.dto.FollowListItemDto
import dev.kawayilab.interknot.data.api.dto.FollowResultDto
import dev.kawayilab.interknot.data.api.dto.KnockConversationDto
import dev.kawayilab.interknot.data.api.dto.KnockMessagePageDto
import dev.kawayilab.interknot.data.api.dto.KnockNotificationDto
import dev.kawayilab.interknot.data.api.dto.LikeCheckResultDto
import dev.kawayilab.interknot.data.api.dto.LikeListItemDto
import dev.kawayilab.interknot.data.api.dto.LikeResultDto
import dev.kawayilab.interknot.data.api.dto.MarkAllReadResultDto
import dev.kawayilab.interknot.data.api.dto.MarkReadResultDto
import dev.kawayilab.interknot.data.api.dto.NameUpdateResultDto
import dev.kawayilab.interknot.data.api.dto.NotificationReadResultDto
import dev.kawayilab.interknot.data.api.dto.PagedListDto
import dev.kawayilab.interknot.data.api.dto.PinnedArticlesResponseDto
import dev.kawayilab.interknot.data.api.dto.PinnedUpdateResultDto
import dev.kawayilab.interknot.data.api.dto.ProfileDataDto
import dev.kawayilab.interknot.data.api.dto.ProfileStatsDto
import dev.kawayilab.interknot.data.api.dto.ResetPasswordResultDto
import dev.kawayilab.interknot.data.api.dto.RenewTokenResponseDto
import dev.kawayilab.interknot.data.api.dto.ReportResponseDto
import dev.kawayilab.interknot.data.api.dto.SearchSuggestionDto
import dev.kawayilab.interknot.data.api.dto.SignedUploadResultDto
import dev.kawayilab.interknot.data.api.dto.SingleDto
import dev.kawayilab.interknot.data.api.dto.TripleResultDto
import dev.kawayilab.interknot.data.api.dto.UnreadCountDto
import dev.kawayilab.interknot.data.api.dto.UploadedFileDto
import dev.kawayilab.interknot.data.api.dto.UserDto
import dev.kawayilab.interknot.data.api.dto.VisibilityUpdateResultDto
import dev.kawayilab.interknot.data.api.dto.toDomain
import dev.kawayilab.interknot.model.Article
import dev.kawayilab.interknot.model.ArticlePage
import dev.kawayilab.interknot.model.AuthResult
import dev.kawayilab.interknot.model.Author
import dev.kawayilab.interknot.model.BioUpdateResult
import dev.kawayilab.interknot.model.BlockResult
import dev.kawayilab.interknot.model.Category
import dev.kawayilab.interknot.model.CommentPage
import dev.kawayilab.interknot.model.DennyBalance
import dev.kawayilab.interknot.model.DennyGiveResult
import dev.kawayilab.interknot.model.ExamReview
import dev.kawayilab.interknot.model.ExamStartResult
import dev.kawayilab.interknot.model.ExamStatus
import dev.kawayilab.interknot.model.ExamSubmitResult
import dev.kawayilab.interknot.model.Benefits
import dev.kawayilab.interknot.model.FavoriteRecord
import dev.kawayilab.interknot.model.FavoriteResult
import dev.kawayilab.interknot.model.FileInfo
import dev.kawayilab.interknot.model.FollowResult
import dev.kawayilab.interknot.model.KnockConversation
import dev.kawayilab.interknot.model.KnockMessagePage
import dev.kawayilab.interknot.model.KnockNotification
import dev.kawayilab.interknot.model.LikeRecord
import dev.kawayilab.interknot.model.LikeResult
import dev.kawayilab.interknot.model.NameUpdateResult
import dev.kawayilab.interknot.model.NotificationPage
import dev.kawayilab.interknot.model.PinnedArticlesResponse
import dev.kawayilab.interknot.model.PinnedUpdateResult
import dev.kawayilab.interknot.model.Profile
import dev.kawayilab.interknot.model.ReportResult
import dev.kawayilab.interknot.model.SearchSuggestion
import dev.kawayilab.interknot.model.SignedUploadResult
import dev.kawayilab.interknot.model.TripleResult
import dev.kawayilab.interknot.model.UploadedFile
import dev.kawayilab.interknot.model.User
import dev.kawayilab.interknot.model.VisibilityUpdateResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject
import kotlinx.serialization.json.JsonObject

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
        response.toDomain()
    }

    override suspend fun sendResetCode(email: String): Result<Pair<Boolean, Int>> = runCatching {
        val response: CodeResultDto = client.authPost("auth/send-reset-code") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("email" to email))
        }
        response.toDomain()
    }

    override suspend fun resetPassword(email: String, code: String, password: String): Result<Boolean> = runCatching {
        val response: ResetPasswordResultDto = client.authPost("auth/reset-password") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("email" to email, "code" to code, "password" to password))
        }
        response.success
    }

    override suspend fun renewToken(): Result<String> = runCatching {
        val response: RenewTokenResponseDto = client.authPost("auth/renew") {
            contentType(ContentType.Application.Json)
            setBody(mapOf<String, String>())
        }
        response.jwt
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
            if (!category.isNullOrBlank()) parameter("category", category)
        }
        response.toArticlePage(start)
    }

    override suspend fun getArticle(documentId: String): Result<Article> = runCatching {
        val response: SingleDto<ArticleDetailDto> = client.authGet("articles/detail/$documentId")
        response.data.toDomain()
    }

    override suspend fun getMyDrafts(start: Int, limit: Int): Result<ArticlePage> = runCatching {
        val response: PagedListDto<ArticleDraftItemDto> = client.authGet("articles/my/drafts") {
            parameter("start", start)
            parameter("limit", limit)
        }
        ArticlePage(
            items = response.data.map { it.toDomain() },
            start = response.meta.pagination.start,
            limit = response.meta.pagination.limit,
            total = response.meta.pagination.total,
            hasMore = response.data.isNotEmpty() && (start + response.data.size) < response.meta.pagination.total
        )
    }

    override suspend fun getMyPublished(start: Int, limit: Int): Result<ArticlePage> = runCatching {
        val response: PagedListDto<ArticleListItemDto> = client.authGet("articles/my/published") {
            parameter("start", start)
            parameter("limit", limit)
        }
        response.toArticlePage(start)
    }

    override suspend fun createArticleDraft(
        title: String,
        text: String,
        authorDocumentId: String,
        category: String?,
        isAnonymous: Boolean,
        coverDocumentIds: List<String>?
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
                        if (!coverDocumentIds.isNullOrEmpty()) {
                            put("cover", coverDocumentIds.map { mapOf("documentId" to it) })
                        }
                    }
                )
            )
        }
        response.data.documentId
    }

    override suspend fun updateArticle(
        documentId: String,
        title: String,
        text: String,
        category: String?,
        isAnonymous: Boolean,
        coverDocumentIds: List<String>?
    ): Result<Article> = runCatching {
        val response: SingleDto<ArticleDetailDto> = client.authPut("articles/$documentId") {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "data" to buildMap {
                        put("title", title)
                        put("text", text)
                        if (!category.isNullOrBlank()) put("category", category)
                        if (isAnonymous) put("isAnonymous", true)
                        if (!coverDocumentIds.isNullOrEmpty()) {
                            put("cover", coverDocumentIds.map { mapOf("documentId" to it) })
                        }
                    }
                )
            )
        }
        response.data.toDomain()
    }

    override suspend fun publishArticle(documentId: String): Result<Unit> = runCatching {
        client.authPost<JsonObject>("articles/$documentId/publish") {
            contentType(ContentType.Application.Json)
            setBody(mapOf<String, String>())
        }
        Unit
    }

    override suspend fun unpublishArticle(documentId: String, discardDraft: Boolean): Result<Unit> = runCatching {
        client.authPost<JsonObject>("articles/$documentId/unpublish") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("discardDraft" to discardDraft))
        }
        Unit
    }

    override suspend fun discardDraft(documentId: String): Result<Unit> = runCatching {
        client.authPost<JsonObject>("articles/$documentId/discard-draft") {
            contentType(ContentType.Application.Json)
            setBody(mapOf<String, String>())
        }
        Unit
    }

    override suspend fun deleteArticle(documentId: String): Result<Unit> = runCatching {
        client.authDelete<JsonObject>("articles/$documentId")
        Unit
    }

    override suspend fun recordArticleView(documentId: String): Result<Unit> = runCatching {
        client.authPost<JsonObject>("articles/$documentId/view") {
            contentType(ContentType.Application.Json)
            setBody(mapOf<String, String>())
        }
        Unit
    }

    override suspend fun markArticlesRead(articleDocumentIds: List<String>): Result<Unit> = runCatching {
        client.authPost<JsonObject>("article-reads/batch") {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "articleDocumentIds" to articleDocumentIds,
                    "markAsRead" to true
                )
            )
        }
        Unit
    }

    override suspend fun tripleArticle(articleId: String): Result<TripleResult> = runCatching {
        val response: TripleResultDto = client.authPost("articles/triple") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("articleId" to articleId))
        }
        response.toDomain()
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

    override suspend fun deleteComment(documentId: String): Result<Unit> = runCatching {
        client.authDelete<JsonObject>("comments/$documentId")
        Unit
    }

    override suspend fun pinComment(documentId: String): Result<Unit> = runCatching {
        client.authPost<JsonObject>("comments/$documentId/pin") {
            contentType(ContentType.Application.Json)
            setBody(mapOf<String, String>())
        }
        Unit
    }

    override suspend fun unpinComment(documentId: String): Result<Unit> = runCatching {
        client.authPost<JsonObject>("comments/$documentId/unpin") {
            contentType(ContentType.Application.Json)
            setBody(mapOf<String, String>())
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

    override suspend fun checkLike(targetType: String, targetIds: List<String>): Result<Map<String, Boolean>> = runCatching {
        val response: LikeCheckResultDto = client.authGet("likes/check") {
            parameter("targetType", targetType)
            parameter("targetIds", targetIds.joinToString(","))
        }
        response.data
    }

    override suspend fun getMyLikes(
        targetType: String?,
        start: Int,
        limit: Int
    ): Result<List<LikeRecord>> = runCatching {
        val response: PagedListDto<LikeListItemDto> = client.authGet("likes/my-list") {
            parameter("start", start)
            parameter("limit", limit)
            if (!targetType.isNullOrBlank()) parameter("targetType", targetType)
        }
        response.data.map { it.toDomain() }
    }

    override suspend fun toggleFavorite(articleDocumentId: String): Result<FavoriteResult> = runCatching {
        val response: FavoriteResultDto = client.authPost("favorites/toggle") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("targetId" to articleDocumentId))
        }
        response.toDomain()
    }

    override suspend fun checkFavorite(targetIds: List<String>): Result<Map<String, Boolean>> = runCatching {
        val response: FavoriteCheckResultDto = client.authGet("favorites/check") {
            parameter("targetIds", targetIds.joinToString(","))
        }
        response.data
    }

    override suspend fun getMyFavorites(start: Int, limit: Int): Result<List<FavoriteRecord>> = runCatching {
        val response: PagedListDto<FavoriteListItemDto> = client.authGet("favorites/list") {
            parameter("start", start)
            parameter("limit", limit)
        }
        response.data.map { it.toDomain() }
    }

    override suspend fun toggleFollow(authorDocumentId: String): Result<FollowResult> = runCatching {
        val response: FollowResultDto = client.authPost("follows/toggle") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("authorDocumentId" to authorDocumentId))
        }
        response.toDomain()
    }

    override suspend fun checkFollow(authorDocumentIds: List<String>): Result<Map<String, Boolean>> = runCatching {
        val response: FollowCheckResultDto = client.authGet("follows/check") {
            parameter("authorIds", authorDocumentIds.joinToString(","))
        }
        response.data
    }

    override suspend fun getFollowing(start: Int, limit: Int): Result<List<Author>> = runCatching {
        val response: PagedListDto<FollowListItemDto> = client.authGet("follows/following") {
            parameter("start", start)
            parameter("limit", limit)
        }
        response.data.map { it.toDomain() }
    }

    override suspend fun createReport(
        targetType: String,
        targetId: String,
        reason: String,
        detail: String?
    ): Result<ReportResult> = runCatching {
        val response: SingleDto<ReportResponseDto> = client.authPost("reports") {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "targetType" to targetType,
                    "targetId" to targetId,
                    "reason" to reason,
                    "detail" to (detail ?: "")
                )
            )
        }
        response.data.toDomain()
    }

    override suspend fun toggleBlock(authorDocumentId: String): Result<BlockResult> = runCatching {
        val response: BlockResultDto = client.authPost("user-blocks/toggle") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("authorDocumentId" to authorDocumentId))
        }
        response.toDomain()
    }

    override suspend fun checkBlock(authorDocumentIds: List<String>): Result<Map<String, Boolean>> = runCatching {
        val response: BlockCheckResultDto = client.authGet("user-blocks/check") {
            parameter("authorIds", authorDocumentIds.joinToString(","))
        }
        response.data
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
        response.toArticlePage(start)
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

    override suspend fun getCategories(): Result<List<Category>> = runCatching {
        val response: DataListDto<CategoryDto> = client.authGet("categories/list")
        response.data.map { it.toDomain() }
    }

    override suspend fun getProfile(documentId: String): Result<Profile> = runCatching {
        val response: SingleDto<ProfileDataDto> = client.authGet("profiles/$documentId")
        response.data.toDomain()
    }

    override suspend fun getProfileArticles(documentId: String, start: Int, limit: Int): Result<ArticlePage> = runCatching {
        val response: PagedListDto<ArticleListItemDto> = client.authGet("profiles/$documentId/articles") {
            parameter("start", start)
            parameter("limit", limit)
        }
        response.toArticlePage(start)
    }

    override suspend fun getProfileComments(documentId: String, start: Int, limit: Int): Result<CommentPage> = runCatching {
        val response: PagedListDto<CommentDto> = client.authGet("profiles/$documentId/comments") {
            parameter("start", start)
            parameter("limit", limit)
        }
        response.toCommentPage(start)
    }

    override suspend fun searchAuthors(query: String, limit: Int): Result<List<Author>> = runCatching {
        val response: DataListDto<AuthorSearchItemDto> = client.authGet("authors/search") {
            parameter("q", query.trim())
            parameter("limit", limit)
        }
        response.data.map { it.toDomain() }
    }

    override suspend fun updateName(name: String): Result<NameUpdateResult> = runCatching {
        val response: NameUpdateResultDto = client.authPut("me/profile/name") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("name" to name))
        }
        response.toDomain()
    }

    override suspend fun updateBio(bio: String): Result<BioUpdateResult> = runCatching {
        val response: BioUpdateResultDto = client.authPut("me/profile/bio") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("bio" to bio))
        }
        response.toDomain()
    }

    override suspend fun updateVisibility(profileHidden: Boolean): Result<VisibilityUpdateResult> = runCatching {
        val response: VisibilityUpdateResultDto = client.authPut("me/profile/visibility") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("profileHidden" to profileHidden))
        }
        response.toDomain()
    }

    override suspend fun getPinnedArticles(limit: Int): Result<PinnedArticlesResponse> = runCatching {
        val response: PinnedArticlesResponseDto = client.authGet("me/profile/pinned-articles") {
            parameter("limit", limit)
        }
        response.toDomain()
    }

    override suspend fun updatePinnedArticles(pinned: List<String>?): Result<PinnedUpdateResult> = runCatching {
        val response: PinnedUpdateResultDto = client.authPut("me/profile/pinned-articles") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("pinned" to pinned))
        }
        response.toDomain()
    }

    override suspend fun getNotifications(
        start: Int,
        limit: Int,
        isRead: Boolean?
    ): Result<NotificationPage> = runCatching {
        val response: PagedListDto<KnockNotificationDto> = client.authGet("notifications/list") {
            parameter("start", start)
            parameter("limit", limit)
            if (isRead != null) parameter("isRead", isRead.toString())
        }
        response.toNotificationPage(start)
    }

    override suspend fun markNotificationRead(documentId: String): Result<Boolean> = runCatching {
        val response: NotificationReadResultDto = client.authPut("notifications/$documentId/mark-read") {
            contentType(ContentType.Application.Json)
            setBody(mapOf<String, String>())
        }
        response.success
    }

    override suspend fun markAllNotificationsRead(): Result<Int> = runCatching {
        val response: MarkAllReadResultDto = client.authPut("notifications/mark-all-read") {
            contentType(ContentType.Application.Json)
            setBody(mapOf<String, String>())
        }
        response.updated ?: 0
    }

    override suspend fun getKnockConversations(): Result<List<KnockConversation>> = runCatching {
        val response: DataListDto<KnockConversationDto> = client.authGet("knock/conversations")
        response.data.map { it.toDomain() }
    }

    override suspend fun getKnockMessages(
        conversationId: String,
        cursor: String?,
        limit: Int
    ): Result<KnockMessagePage> = runCatching {
        val response: KnockMessagePageDto = client.authGet("knock/conversations/$conversationId/messages") {
            parameter("limit", limit)
            if (!cursor.isNullOrBlank()) parameter("cursor", cursor)
        }
        response.toDomain()
    }

    override suspend fun markConversationRead(conversationId: String): Result<Int> = runCatching {
        val response: MarkReadResultDto = client.authPost("knock/conversations/$conversationId/mark-read") {
            contentType(ContentType.Application.Json)
            setBody(mapOf<String, String>())
        }
        response.updated ?: 0
    }

    override suspend fun getUnreadNotificationCount(): Result<Int> = runCatching {
        val response: UnreadCountDto = client.authGet("notifications/unread-count")
        response.count
    }

    override suspend fun getDennyBalance(): Result<DennyBalance> = runCatching {
        val response: DennyBalanceDto = client.authGet("user-denny")
        response.toDomain()
    }

    override suspend fun giveDenny(articleId: String, message: String?): Result<DennyGiveResult> = runCatching {
        val response: DennyGiveResponseDto = client.authPost("user-denny/give") {
            contentType(ContentType.Application.Json)
            setBody(buildMap {
                put("articleId", articleId)
                if (!message.isNullOrBlank()) put("message", message)
            })
        }
        response.toDomain()
    }

    override suspend fun getExamStatus(): Result<ExamStatus> = runCatching {
        val response: ExamStatusDto = client.authGet("exam/status")
        response.toDomain()
    }

    override suspend fun startExam(): Result<ExamStartResult> = runCatching {
        val response: ExamStartResultDto = client.authPost("exam/start") {
            contentType(ContentType.Application.Json)
            setBody(mapOf<String, String>())
        }
        response.toDomain()
    }

    override suspend fun submitExam(
        attemptId: String,
        answers: Map<String, List<String>>
    ): Result<ExamSubmitResult> = runCatching {
        val response: ExamSubmitResultDto = client.authPost("exam/submit") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("attemptId" to attemptId, "answers" to answers))
        }
        response.toDomain()
    }

    override suspend fun getExamReview(attemptId: String?): Result<ExamReview> = runCatching {
        val response: ExamReviewDto = client.authGet("exam/review") {
            if (!attemptId.isNullOrBlank()) parameter("attemptId", attemptId)
        }
        response.toDomain()
    }

    override suspend fun getBenefits(): Result<Benefits> = runCatching {
        val response: BenefitsDto = client.authGet("benefits/me")
        response.toDomain()
    }

    override suspend fun signUpload(
        filename: String,
        mimeType: String,
        size: Int,
        contentHash: String?,
        width: Int?,
        height: Int?,
        fileInfo: FileInfo?
    ): Result<SignedUploadResult> = runCatching {
        val response: SingleDto<SignedUploadResultDto> = client.authPost("direct-upload/sign") {
            contentType(ContentType.Application.Json)
            setBody(
                buildMap {
                    put("filename", filename)
                    put("mimeType", mimeType)
                    put("size", size)
                    if (!contentHash.isNullOrBlank()) put("contentHash", contentHash)
                    if (width != null) put("width", width)
                    if (height != null) put("height", height)
                    if (fileInfo != null) {
                        put(
                            "fileInfo",
                            mapOf(
                                "name" to fileInfo.name,
                                "alternativeText" to fileInfo.alternativeText,
                                "caption" to fileInfo.caption
                            )
                        )
                    }
                }
            )
        }
        response.data.toDomain()
    }

    override suspend fun completeUpload(
        uploadToken: String,
        width: Int?,
        height: Int?
    ): Result<UploadedFile> = runCatching {
        val response: SingleDto<UploadedFileDto> = client.authPost("direct-upload/complete") {
            contentType(ContentType.Application.Json)
            setBody(
                buildMap {
                    put("uploadToken", uploadToken)
                    if (width != null) put("width", width)
                    if (height != null) put("height", height)
                }
            )
        }
        response.data.toDomain()
    }
}

private fun PagedListDto<ArticleListItemDto>.toArticlePage(start: Int) = ArticlePage(
    items = data.map { it.toDomain() },
    start = meta.pagination.start,
    limit = meta.pagination.limit,
    total = meta.pagination.total,
    hasMore = data.isNotEmpty() && (start + data.size) < meta.pagination.total
)

private fun PagedListDto<CommentDto>.toCommentPage(start: Int): CommentPage {
    val dto = CommentListResponseDto(
        data = this.data,
        meta = CommentListMetaDto(pagination = this.meta.pagination)
    )
    return dto.toDomain(start)
}

private fun PagedListDto<KnockNotificationDto>.toNotificationPage(start: Int) = NotificationPage(
    items = data.map { it.toDomain() },
    start = meta.pagination.start,
    limit = meta.pagination.limit,
    total = meta.pagination.total,
    hasMore = data.isNotEmpty() && (start + data.size) < meta.pagination.total
)

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

private suspend inline fun <reified T> HttpClient.authPut(
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {}
): T = put(urlString) {
    appendTokenIfNeeded()
    block()
}.body()

private suspend inline fun <reified T> HttpClient.authDelete(
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {}
): T = delete(urlString) {
    appendTokenIfNeeded()
    block()
}.body()

private suspend inline fun <reified T> HttpClient.authPatch(
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {}
): T = patch(urlString) {
    appendTokenIfNeeded()
    block()
}.body()
