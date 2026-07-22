package dev.kawayilab.interknot.data.local.cache

import androidx.room.Entity

@Entity(tableName = "cached_searches", primaryKeys = ["query", "category"])
data class CachedSearch(
    val query: String,
    val category: String = "",
    val resultsJson: String? = null,
    val total: Int = 0,
    val cachedAt: Long = System.currentTimeMillis()
)
