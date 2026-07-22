package dev.kawayilab.interknot.data.api

internal fun shouldAttachToken(path: String, method: String): Boolean {
    val normalizedPath = path.substringBefore("?")
    val upperMethod = method.uppercase()

    // Public auth endpoints (except mihoyo binding/qr which need token)
    if (normalizedPath.startsWith("/api/auth/") &&
        !normalizedPath.startsWith("/api/auth/renew") &&
        !normalizedPath.startsWith("/api/auth/mihoyo/")
    ) {
        return false
    }

    if (upperMethod != "GET") return true

    return !(
        (normalizedPath.startsWith("/api/articles") &&
            !normalizedPath.contains("/my") &&
            !normalizedPath.contains("/publish") &&
            !normalizedPath.contains("/unpublish")) ||
            (normalizedPath.startsWith("/api/comments") && !normalizedPath.contains("/likes")) ||
            normalizedPath.startsWith("/api/authors") ||
            normalizedPath.startsWith("/api/profiles")
        )
}
