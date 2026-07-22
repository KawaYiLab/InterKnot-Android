package dev.kawayilab.interknot.data.local.cache

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_articles")
data class CachedArticle(
    @PrimaryKey val documentId: String,
    val title: String = "",
    val text: String? = null,
    val coverUrl: String? = null,
    val coverWidth: Int? = null,
    val coverHeight: Int? = null,
    val coverNsfwStatus: String? = null,
    val coverImagesJson: String? = null,
    val views: Int = 0,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val dennyCount: Int = 0,
    val favoritesCount: Int = 0,
    val liked: Boolean = false,
    val favorited: Boolean = false,
    val hasGivenDenny: Boolean = false,
    val isRead: Boolean = false,
    val isAnonymous: Boolean = false,
    val isHidden: Boolean = false,
    val isOwner: Boolean = false,
    val hasPublishedVersion: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val editedAt: String? = null,
    val publishedAt: String? = null,
    val authorJson: String? = null,
    val categoryJson: String? = null,
    val cachedAt: Long = System.currentTimeMillis()
)
