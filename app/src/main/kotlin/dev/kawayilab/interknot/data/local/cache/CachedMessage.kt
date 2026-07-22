package dev.kawayilab.interknot.data.local.cache

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_messages")
data class CachedMessage(
    @PrimaryKey val id: String,
    val conversationId: String? = null,
    val type: String? = null,
    val payloadJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
