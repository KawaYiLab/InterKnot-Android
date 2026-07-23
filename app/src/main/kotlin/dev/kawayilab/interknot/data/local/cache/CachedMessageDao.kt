package dev.kawayilab.interknot.data.local.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CachedMessageDao {
    @Query("SELECT * FROM cached_messages WHERE conversationId = :conversationId ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getByConversation(conversationId: String, limit: Int): List<CachedMessage>

    @Query("SELECT * FROM cached_messages WHERE id = :id")
    suspend fun get(id: String): CachedMessage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: CachedMessage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<CachedMessage>)

    @Query("UPDATE cached_messages SET isRead = 1 WHERE conversationId = :conversationId")
    suspend fun markReadByConversation(conversationId: String)

    @Query("DELETE FROM cached_messages WHERE conversationId = :conversationId")
    suspend fun deleteByConversation(conversationId: String)

    @Query("DELETE FROM cached_messages")
    suspend fun clear()
}
