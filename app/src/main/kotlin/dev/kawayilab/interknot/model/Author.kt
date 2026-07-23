package dev.kawayilab.interknot.model

import kotlinx.serialization.Serializable

@Serializable
data class Author(
    val documentId: String? = null,
    val username: String? = null,
    val name: String? = null,
    val email: String? = null,
    val avatarUrl: String? = null,
    val avatarWidth: Int? = null,
    val avatarHeight: Int? = null,
    val level: Int? = null,
    val exp: Int? = null,
    val isAdmin: Boolean = false,
    val examPassed: Boolean? = null,
    val bio: String? = null,
    val isFollowing: Boolean = false,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val isSelf: Boolean = false,
    val isBlockedByMe: Boolean = false,
    val hasBlockedMe: Boolean = false,
    val isHidden: Boolean = false,
    val profileHidden: Boolean = false,
    val isAiAgent: Boolean = false,
    val equippedCard: BusinessCard? = null,
    val equippedAvatar: Avatar? = null
)
