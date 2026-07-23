package dev.kawayilab.interknot.model

data class Profile(
    val author: Author,
    val stats: ProfileStats,
    val isFollowing: Boolean = false,
    val isSelf: Boolean = false,
    val isBlocked: Boolean = false
)

data class ProfileStats(
    val articleCount: Int = 0,
    val commentCount: Int = 0,
    val totalViews: Int = 0,
    val totalComments: Int = 0,
    val totalLikes: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0
)
