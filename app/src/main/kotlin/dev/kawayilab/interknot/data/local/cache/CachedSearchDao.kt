package dev.kawayilab.interknot.data.local.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CachedSearchDao {
    @Query("SELECT * FROM cached_searches WHERE query = :query AND category = :category")
    suspend fun get(query: String, category: String = ""): CachedSearch?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(search: CachedSearch)

    @Query("DELETE FROM cached_searches WHERE cachedAt < :before")
    suspend fun deleteOlderThan(before: Long)

    @Query("DELETE FROM cached_searches")
    suspend fun clear()
}
