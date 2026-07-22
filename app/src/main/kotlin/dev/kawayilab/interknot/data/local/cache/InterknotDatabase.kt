package dev.kawayilab.interknot.data.local.cache

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        CachedArticle::class,
        CachedSearch::class,
        CachedProfile::class,
        CachedMessage::class
    ],
    version = 1,
    exportSchema = false
)
abstract class InterknotDatabase : RoomDatabase() {
    abstract fun cachedArticleDao(): CachedArticleDao
    abstract fun cachedSearchDao(): CachedSearchDao
    abstract fun cachedProfileDao(): CachedProfileDao
    abstract fun cachedMessageDao(): CachedMessageDao
}
