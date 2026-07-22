package dev.kawayilab.interknot.data.api

import dev.kawayilab.interknot.model.Post
import dev.kawayilab.interknot.model.User
import io.ktor.client.HttpClient
import javax.inject.Inject

class InterknotApiImpl @Inject constructor(
    private val client: HttpClient
) : InterknotApi {

    override suspend fun login(username: String, password: String): String {
        // TODO: replace with real API call
        return "demo-token"
    }

    override suspend fun getCurrentUser(): User {
        // TODO: replace with real API call
        return User(id = "1", username = "demo", email = null)
    }

    override suspend fun getPosts(): List<Post> {
        // TODO: replace with real API call
        return (1..10).map { Post(id = it.toString(), title = "Post $it", content = "") }
    }

    override suspend fun getPost(id: String): Post {
        // TODO: replace with real API call
        return Post(id = id, title = "Post $id", content = "Details for post $id")
    }
}
