package dev.kawayilab.interknot.data.api

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.encodedPath

internal fun HttpRequestBuilder.appendTokenIfNeeded() {
    val token = TokenManager.token ?: return
    val path = url.encodedPath
    if (shouldAttachToken(path, method.value)) {
        headers.append(HttpHeaders.Authorization, "Bearer $token")
    }
}
