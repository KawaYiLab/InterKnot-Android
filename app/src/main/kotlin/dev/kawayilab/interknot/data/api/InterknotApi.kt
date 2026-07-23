package dev.kawayilab.interknot.data.api

import dev.kawayilab.interknot.model.Article
import dev.kawayilab.interknot.model.ArticlePage
import dev.kawayilab.interknot.model.AuthResult
import dev.kawayilab.interknot.model.Author
import dev.kawayilab.interknot.model.BioUpdateResult
import dev.kawayilab.interknot.model.Category
import dev.kawayilab.interknot.model.CommentPage
import dev.kawayilab.interknot.model.DennyBalance
import dev.kawayilab.interknot.model.DennyGiveResult
import dev.kawayilab.interknot.model.FavoriteRecord
import dev.kawayilab.interknot.model.FavoriteResult
import dev.kawayilab.interknot.model.FileInfo
import dev.kawayilab.interknot.model.FollowResult
import dev.kawayilab.interknot.model.KnockConversation
import dev.kawayilab.interknot.model.KnockMessagePage
import dev.kawayilab.interknot.model.LikeRecord
import dev.kawayilab.interknot.model.LikeResult
import dev.kawayilab.interknot.model.NameUpdateResult
import dev.kawayilab.interknot.model.NotificationPage
import dev.kawayilab.interknot.model.PinnedArticlesResponse
import dev.kawayilab.interknot.model.PinnedUpdateResult
import dev.kawayilab.interknot.model.Profile
import dev.kawayilab.interknot.model.SearchSuggestion
import dev.kawayilab.interknot.model.SignedUploadResult
import dev.kawayilab.interknot.model.TripleResult
import dev.kawayilab.interknot.model.UploadedFile
import dev.kawayilab.interknot.model.User
import dev.kawayilab.interknot.model.VisibilityUpdateResult

interface InterknotApi {
    // Auth
    suspend fun login(identifier: String, password: String): Result<AuthResult>
    suspend fun register(email: String, code: String, password: String): Result<AuthResult>
    suspend fun sendRegisterCode(email: String): Result<Pair<Boolean, Int>>
    suspend fun sendResetCode(email: String): Result<Pair<Boolean, Int>>
    suspend fun resetPassword(email: String, code: String, password: String): Result<Boolean>
    suspend fun renewToken(): Result<String>
    suspend fun getCurrentUser(): Result<User>

    // Articles
    suspend fun getArticles(
        start: Int,
        limit: Int,
        feed: String = "recommend",
        category: String? = null
    ): Result<ArticlePage>

    suspend fun getArticle(documentId: String): Result<Article>
    suspend fun getMyDrafts(start: Int, limit: Int): Result<ArticlePage>
    suspend fun getMyPublished(start: Int, limit: Int): Result<ArticlePage>
    suspend fun createArticleDraft(
        title: String,
        text: String,
        authorDocumentId: String,
        category: String? = null,
        isAnonymous: Boolean = false
    ): Result<String>

    suspend fun updateArticle(
        documentId: String,
        title: String,
        text: String,
        category: String? = null,
        isAnonymous: Boolean = false
    ): Result<Article>

    suspend fun publishArticle(documentId: String): Result<Unit>
    suspend fun unpublishArticle(documentId: String, discardDraft: Boolean = false): Result<Unit>
    suspend fun discardDraft(documentId: String): Result<Unit>
    suspend fun deleteArticle(documentId: String): Result<Unit>
    suspend fun recordArticleView(documentId: String): Result<Unit>
    suspend fun markArticlesRead(articleDocumentIds: List<String>): Result<Unit>
    suspend fun tripleArticle(articleId: String): Result<TripleResult>

    // Search & categories
    suspend fun searchArticles(
        query: String,
        start: Int,
        limit: Int,
        category: String? = null
    ): Result<ArticlePage>

    suspend fun suggestArticles(
        query: String,
        category: String? = null,
        limit: Int = 8
    ): Result<List<SearchSuggestion>>

    suspend fun getCategories(): Result<List<Category>>

    // Comments
    suspend fun getComments(articleDocumentId: String, start: Int, limit: Int): Result<CommentPage>
    suspend fun addComment(
        articleDocumentId: String,
        content: String,
        authorDocumentId: String,
        parentDocumentId: String? = null,
        isAnonymous: Boolean = false
    ): Result<Unit>

    suspend fun deleteComment(documentId: String): Result<Unit>
    suspend fun pinComment(documentId: String): Result<Unit>
    suspend fun unpinComment(documentId: String): Result<Unit>

    // Social
    suspend fun toggleLike(targetType: String, targetId: String): Result<LikeResult>
    suspend fun checkLike(targetType: String, targetIds: List<String>): Result<Map<String, Boolean>>
    suspend fun getMyLikes(
        targetType: String? = null,
        start: Int,
        limit: Int
    ): Result<List<LikeRecord>>

    suspend fun toggleFavorite(articleDocumentId: String): Result<FavoriteResult>
    suspend fun checkFavorite(targetIds: List<String>): Result<Map<String, Boolean>>
    suspend fun getMyFavorites(start: Int, limit: Int): Result<List<FavoriteRecord>>

    suspend fun toggleFollow(authorDocumentId: String): Result<FollowResult>
    suspend fun checkFollow(authorDocumentIds: List<String>): Result<Map<String, Boolean>>
    suspend fun getFollowing(start: Int, limit: Int): Result<List<Author>>

    // Profiles
    suspend fun getProfile(documentId: String): Result<Profile>
    suspend fun getProfileArticles(documentId: String, start: Int, limit: Int): Result<ArticlePage>
    suspend fun getProfileComments(documentId: String, start: Int, limit: Int): Result<CommentPage>
    suspend fun searchAuthors(query: String, limit: Int = 8): Result<List<Author>>

    // Me
    suspend fun updateName(name: String): Result<NameUpdateResult>
    suspend fun updateBio(bio: String): Result<BioUpdateResult>
    suspend fun updateVisibility(profileHidden: Boolean): Result<VisibilityUpdateResult>
    suspend fun getPinnedArticles(limit: Int = 50): Result<PinnedArticlesResponse>
    suspend fun updatePinnedArticles(pinned: List<String>?): Result<PinnedUpdateResult>

    // Notifications / Knock
    suspend fun getNotifications(
        start: Int,
        limit: Int,
        isRead: Boolean? = null
    ): Result<NotificationPage>

    suspend fun markNotificationRead(documentId: String): Result<Boolean>
    suspend fun markAllNotificationsRead(): Result<Int>
    suspend fun getKnockConversations(): Result<List<KnockConversation>>
    suspend fun getKnockMessages(
        conversationId: String,
        cursor: String? = null,
        limit: Int = 50
    ): Result<KnockMessagePage>

    suspend fun markConversationRead(conversationId: String): Result<Int>
    suspend fun getUnreadNotificationCount(): Result<Int>

    // Denny
    suspend fun getDennyBalance(): Result<DennyBalance>
    suspend fun giveDenny(articleId: String, message: String? = null): Result<DennyGiveResult>

    // Direct upload
    suspend fun signUpload(
        filename: String,
        mimeType: String,
        size: Int,
        contentHash: String? = null,
        width: Int? = null,
        height: Int? = null,
        fileInfo: FileInfo? = null
    ): Result<SignedUploadResult>

    suspend fun completeUpload(
        uploadToken: String,
        width: Int? = null,
        height: Int? = null
    ): Result<UploadedFile>
}
