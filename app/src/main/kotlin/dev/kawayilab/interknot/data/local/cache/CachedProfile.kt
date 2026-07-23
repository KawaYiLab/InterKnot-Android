package dev.kawayilab.interknot.data.local.cache

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_profiles")
data class CachedProfile(
    @PrimaryKey val documentId: String,
    val profileJson: String,
    val cachedAt: Long = System.currentTimeMillis()
)
