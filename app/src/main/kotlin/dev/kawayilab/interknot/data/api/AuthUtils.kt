package dev.kawayilab.interknot.data.api

internal fun shouldAttachToken(path: String, method: String): Boolean {
    val normalizedPath = path.substringBefore("?")

    // Public auth endpoints (login/register/etc. do not need a token).
    // renew and mihoyo routes are the auth exceptions that require one.
    if (normalizedPath.startsWith("/api/auth/") &&
        !normalizedPath.startsWith("/api/auth/renew") &&
        !normalizedPath.startsWith("/api/auth/mihoyo/")
    ) {
        return false
    }

    // Attach the token to everything else, including optional-auth GET endpoints
    // (articles/list, comments/list, profiles/:id, etc.) so the backend can
    // personalize likes/read/follow/block state when the user is logged in.
    return true
}
