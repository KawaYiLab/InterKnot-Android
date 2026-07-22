package dev.kawayilab.interknot.data.api

import dev.kawayilab.interknot.model.Post
import dev.kawayilab.interknot.model.User

interface InterknotApi {
    suspend fun login(username: String, password: String): String
    suspend fun getCurrentUser(): User
    suspend fun getPosts(): List<Post>
    suspend fun getPost(id: String): Post
}
