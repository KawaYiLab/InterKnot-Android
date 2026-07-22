package dev.kawayilab.interknot.data.repository

import dev.kawayilab.interknot.data.api.InterknotApi
import dev.kawayilab.interknot.data.local.UserPreferences
import dev.kawayilab.interknot.model.Post
import dev.kawayilab.interknot.model.User
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class InterknotRepository @Inject constructor(
    private val api: InterknotApi,
    private val preferences: UserPreferences
) {
    val token: Flow<String?> = preferences.token

    suspend fun login(username: String, password: String) {
        val token = api.login(username, password)
        preferences.saveToken(token)
    }

    suspend fun logout() {
        preferences.clearToken()
    }

    suspend fun getCurrentUser(): User = api.getCurrentUser()

    suspend fun getPosts(): List<Post> = api.getPosts()

    suspend fun getPost(id: String): Post = api.getPost(id)
}
