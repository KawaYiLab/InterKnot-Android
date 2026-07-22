package dev.kawayilab.interknot.data.api

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpHeaders

internal fun HttpRequestBuilder.appendTokenIfNeeded() {
    val token = TokenManager.token ?: return
    val path = url.build().encodedPath
    if (shouldAttachToken(path, method.value)) {
        headers.append(HttpHeaders.Authorization, "Bearer $token")
    }
}
