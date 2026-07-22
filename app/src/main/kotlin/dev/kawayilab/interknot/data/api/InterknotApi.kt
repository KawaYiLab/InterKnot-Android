package dev.kawayilab.interknot.data.api

import dev.kawayilab.interknot.model.Delegation
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterknotApi @Inject constructor(
    private val client: HttpClient
) {

    suspend fun getDelegations(page: Int = 1, pageSize: Int = 20): List<Delegation> {
        return client.get("/api/articles") {
            parameter("pagination[page]", page)
            parameter("pagination[pageSize]", pageSize)
            parameter("populate", "*")
        }.body()
    }

    suspend fun getDelegation(id: Int): Delegation {
        return client.get("/api/articles/$id").body()
    }
}
