package dev.kawayilab.interknot.data.local.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CachedProfileDao {
    @Query("SELECT * FROM cached_profiles WHERE documentId = :documentId")
    suspend fun get(documentId: String): CachedProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: CachedProfile)

    @Query("DELETE FROM cached_profiles WHERE documentId = :documentId")
    suspend fun delete(documentId: String)

    @Query("DELETE FROM cached_profiles")
    suspend fun clear()
}
