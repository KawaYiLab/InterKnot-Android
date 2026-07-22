package dev.kawayilab.interknot.model

data class AuthResult(
    val token: String,
    val user: User
)
