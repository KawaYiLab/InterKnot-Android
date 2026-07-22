package dev.kawayilab.interknot.model

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
    val examPassed: Boolean? = null
)
