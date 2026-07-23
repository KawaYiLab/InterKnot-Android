package dev.kawayilab.interknot.data.local.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CachedArticleDao {
    @Query("SELECT * FROM cached_articles WHERE documentId = :documentId")
    suspend fun get(documentId: String): CachedArticle?

    @Query("SELECT * FROM cached_articles ORDER BY cachedAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<CachedArticle>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(article: CachedArticle)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(articles: List<CachedArticle>)

    @Query("DELETE FROM cached_articles WHERE documentId = :documentId")
    suspend fun delete(documentId: String)

    @Query("DELETE FROM cached_articles")
    suspend fun clear()
}
