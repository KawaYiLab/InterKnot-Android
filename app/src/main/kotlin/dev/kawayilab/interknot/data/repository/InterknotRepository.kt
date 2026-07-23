package dev.kawayilab.interknot.data.repository

import dev.kawayilab.interknot.data.api.InterknotApi
import dev.kawayilab.interknot.data.api.TokenManager
import dev.kawayilab.interknot.data.local.UserPreferences
import dev.kawayilab.interknot.data.local.cache.CachedArticle
import dev.kawayilab.interknot.data.local.cache.CachedArticleDao
import dev.kawayilab.interknot.data.local.cache.CachedSearch
import dev.kawayilab.interknot.data.local.cache.CachedSearchDao
import dev.kawayilab.interknot.model.Article
import dev.kawayilab.interknot.model.ArticlePage
import dev.kawayilab.interknot.model.AuthResult
import dev.kawayilab.interknot.model.Author
import dev.kawayilab.interknot.model.Avatar
import dev.kawayilab.interknot.model.Category
import dev.kawayilab.interknot.model.CommentPage
import dev.kawayilab.interknot.model.DennyBalance
import dev.kawayilab.interknot.model.DennyGiveResult
import dev.kawayilab.interknot.model.DmConversation
import dev.kawayilab.interknot.model.DmConversationDetail
import dev.kawayilab.interknot.model.DmMessage
import dev.kawayilab.interknot.model.DmMessagePage
import dev.kawayilab.interknot.model.DmSocketTicket
import dev.kawayilab.interknot.model.NotificationPage
import dev.kawayilab.interknot.model.Benefits
import dev.kawayilab.interknot.model.BioUpdateResult
import dev.kawayilab.interknot.model.ExamReview
import dev.kawayilab.interknot.model.NameUpdateResult
import dev.kawayilab.interknot.model.PinnedArticlesResponse
import dev.kawayilab.interknot.model.PinnedUpdateResult
import dev.kawayilab.interknot.model.VisibilityUpdateResult
import dev.kawayilab.interknot.model.ExamStartResult
import dev.kawayilab.interknot.model.ExamStatus
import dev.kawayilab.interknot.model.ExamSubmitResult
import dev.kawayilab.interknot.model.FavoriteResult
import dev.kawayilab.interknot.model.FollowResult
import dev.kawayilab.interknot.model.ImageMeta
import dev.kawayilab.interknot.model.KnockConversation
import dev.kawayilab.interknot.model.KnockNotification
import dev.kawayilab.interknot.model.LikeResult
import dev.kawayilab.interknot.model.ReportResult
import dev.kawayilab.interknot.model.SearchSuggestion
import dev.kawayilab.interknot.model.TripleResult
import dev.kawayilab.interknot.model.User
import dev.kawayilab.interknot.model.BlockResult
import dev.kawayilab.interknot.model.BusinessCard
import dev.kawayilab.interknot.model.CheckInResult
import dev.kawayilab.interknot.model.CheckInStatus
import dev.kawayilab.interknot.model.DailyExp
import dev.kawayilab.interknot.model.ExpInfo
import dev.kawayilab.interknot.model.Profile
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer

@Singleton
class InterknotRepository @Inject constructor(
    private val api: InterknotApi,
    private val preferences: UserPreferences,
    private val cachedArticleDao: CachedArticleDao,
    private val cachedSearchDao: CachedSearchDao,
    private val json: Json
) {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    val isLoggedIn: Flow<Boolean> = user.map { it != null }

    val token: Flow<String?> = preferences.token

    private val _unreadNotificationCount = MutableStateFlow(0)
    val unreadNotificationCount: StateFlow<Int> = _unreadNotificationCount.asStateFlow()

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
    ): Result<ArticlePage> {
        return api.getArticles(start, limit, feed, category)
            .onSuccess { page ->
                if (start == 0) {
                    cacheArticles(page.items)
                }
            }
            .recoverCatching {
                if (start == 0) {
                    val cached = cachedArticleDao.getRecent(limit)
                        .map { it.toArticle() }
                        .takeIf { it.isNotEmpty() }
                    cached?.let { ArticlePage(it, 0, limit, it.size, false) }
                        ?: throw it
                } else throw it
            }
    }

    suspend fun getArticle(documentId: String): Result<Article> {
        return api.getArticle(documentId)
            .onSuccess { cacheArticles(listOf(it)) }
            .recoverCatching {
                cachedArticleDao.get(documentId)?.toArticle()?.let { return@recoverCatching it }
                throw it
            }
    }

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

    suspend fun saveArticleDraft(
        title: String,
        text: String,
        category: String? = null,
        isAnonymous: Boolean = false,
        coverDocumentIds: List<String>? = null,
        existingDocumentId: String? = null
    ): Result<String> {
        val authorId = user.value?.authorDocumentId ?: return Result.failure(IllegalStateException("未登录"))
        return if (!existingDocumentId.isNullOrBlank()) {
            api.updateArticle(existingDocumentId, title, text, category, isAnonymous, coverDocumentIds)
                .map { it.documentId }
        } else {
            api.createArticleDraft(title, text, authorId, category, isAnonymous, coverDocumentIds)
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

    suspend fun addSearchHistory(query: String) = runCatching { preferences.addSearchHistory(query) }
    suspend fun clearSearchHistory() = runCatching { preferences.clearSearchHistory() }

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
        .onSuccess { _unreadNotificationCount.value = it }

    // Notifications
    suspend fun getNotifications(start: Int = 0, limit: Int = 20, isRead: Boolean? = null): Result<NotificationPage> =
        api.getNotifications(start, limit, isRead)

    suspend fun markNotificationRead(documentId: String): Result<Boolean> =
        api.markNotificationRead(documentId).onSuccess { getUnreadNotificationCount() }

    suspend fun markAllNotificationsRead(): Result<Int> =
        api.markAllNotificationsRead().onSuccess { _unreadNotificationCount.value = 0 }

    // DM
    suspend fun getDmConversations(): Result<List<DmConversation>> = api.getDmConversations()
    suspend fun getDmConversationDetail(documentId: String): Result<DmConversationDetail> = api.getDmConversationDetail(documentId)
    suspend fun createDirectConversation(targetUserId: Int): Result<Pair<DmConversation, Boolean>> =
        api.createDirectConversation(targetUserId)

    suspend fun getDmMessages(
        conversationId: String,
        cursor: String? = null,
        limit: Int = 50
    ): Result<DmMessagePage> = api.getDmMessages(conversationId, cursor, limit)

    suspend fun sendDmMessage(
        conversationId: String,
        content: String,
        kind: String = "text",
        replyTo: String? = null
    ): Result<DmMessage> = api.sendDmMessage(conversationId, content, kind, replyTo)

    suspend fun editDmMessage(messageId: String, content: String): Result<Boolean> = api.editDmMessage(messageId, content)
    suspend fun withdrawDmMessage(messageId: String): Result<Boolean> = api.withdrawDmMessage(messageId)
    suspend fun markDmConversationRead(conversationId: String): Result<Boolean> = api.markDmConversationRead(conversationId)
    suspend fun getDmSocketTicket(): Result<DmSocketTicket> = api.getDmSocketTicket()

    // Profile / Me
    suspend fun getProfile(documentId: String): Result<Profile> = api.getProfile(documentId)
    suspend fun getProfileArticles(documentId: String, start: Int = 0, limit: Int = 20): Result<ArticlePage> =
        api.getProfileArticles(documentId, start, limit)
    suspend fun getProfileComments(documentId: String, start: Int = 0, limit: Int = 20): Result<CommentPage> =
        api.getProfileComments(documentId, start, limit)

    suspend fun updateName(name: String): Result<NameUpdateResult> = api.updateName(name)
    suspend fun updateBio(bio: String): Result<BioUpdateResult> = api.updateBio(bio)
    suspend fun updateVisibility(profileHidden: Boolean): Result<VisibilityUpdateResult> = api.updateVisibility(profileHidden)
    suspend fun getPinnedArticles(limit: Int = 50): Result<PinnedArticlesResponse> = api.getPinnedArticles(limit)
    suspend fun updatePinnedArticles(pinned: List<String>?): Result<PinnedUpdateResult> = api.updatePinnedArticles(pinned)

    suspend fun getAvatars(): Result<Pair<List<Avatar>, String?>> = api.getAvatars()
    suspend fun equipAvatar(documentId: String?): Result<String?> = api.equipAvatar(documentId)
    suspend fun getBusinessCards(type: String? = null): Result<Pair<List<BusinessCard>, String?>> = api.getBusinessCards(type)
    suspend fun equipBusinessCard(documentId: String?): Result<String?> = api.equipBusinessCard(documentId)
    suspend fun getBlockedAuthors(start: Int = 0, limit: Int = 20): Result<List<Author>> = api.getBlockedAuthors(start, limit)

    // Check-in / Level
    suspend fun getCheckInStatus(): Result<CheckInStatus> = api.getCheckInStatus()
    suspend fun checkIn(): Result<CheckInResult> = api.checkIn()
    suspend fun getMyExp(): Result<ExpInfo> = api.getMyExp()
    suspend fun getDailyExp(): Result<DailyExp> = api.getDailyExp()

    suspend fun getDennyBalance(): Result<DennyBalance> = api.getDennyBalance()

    suspend fun giveDenny(articleId: String, message: String? = null): Result<DennyGiveResult> = api.giveDenny(articleId, message)

    private suspend fun cacheArticles(articles: List<Article>) {
        runCatching { cachedArticleDao.insertAll(articles.map { it.toCachedArticle() }) }
    }

    private fun Article.toCachedArticle(): CachedArticle = CachedArticle(
        documentId = documentId,
        title = title,
        text = text,
        coverUrl = coverUrl,
        coverWidth = coverWidth,
        coverHeight = coverHeight,
        coverNsfwStatus = coverNsfwStatus,
        coverImagesJson = coverImages.takeIf { it.isNotEmpty() }?.let { json.encodeToString(ListSerializer(ImageMeta.serializer()), it) },
        views = views,
        likesCount = likesCount,
        commentsCount = commentsCount,
        dennyCount = dennyCount,
        favoritesCount = favoritesCount,
        liked = liked,
        favorited = favorited,
        hasGivenDenny = hasGivenDenny,
        isRead = isRead,
        isAnonymous = isAnonymous,
        isHidden = isHidden,
        isOwner = isOwner,
        hasPublishedVersion = hasPublishedVersion,
        createdAt = createdAt,
        updatedAt = updatedAt,
        editedAt = editedAt,
        publishedAt = publishedAt,
        authorJson = author?.let { json.encodeToString(Author.serializer(), it) },
        categoryJson = category?.let { json.encodeToString(Category.serializer(), it) }
    )

    private fun CachedArticle.toArticle(): Article = Article(
        documentId = documentId,
        title = title,
        text = text,
        coverUrl = coverUrl,
        coverWidth = coverWidth,
        coverHeight = coverHeight,
        coverNsfwStatus = coverNsfwStatus,
        coverImages = coverImagesJson?.let {
            runCatching { json.decodeFromString(ListSerializer(ImageMeta.serializer()), it) }.getOrDefault(emptyList())
        } ?: emptyList(),
        views = views,
        likesCount = likesCount,
        commentsCount = commentsCount,
        dennyCount = dennyCount,
        favoritesCount = favoritesCount,
        liked = liked,
        favorited = favorited,
        hasGivenDenny = hasGivenDenny,
        isRead = isRead,
        isAnonymous = isAnonymous,
        isHidden = isHidden,
        isOwner = isOwner,
        hasPublishedVersion = hasPublishedVersion,
        createdAt = createdAt,
        updatedAt = updatedAt,
        editedAt = editedAt,
        publishedAt = publishedAt,
        author = authorJson?.let { runCatching { json.decodeFromString(Author.serializer(), it) }.getOrNull() },
        category = categoryJson?.let { runCatching { json.decodeFromString(Category.serializer(), it) }.getOrNull() }
    )
}
