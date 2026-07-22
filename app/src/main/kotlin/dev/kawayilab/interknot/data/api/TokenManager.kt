package dev.kawayilab.interknot.data.api

import java.util.concurrent.atomic.AtomicReference

internal object TokenManager {
    private val tokenRef = AtomicReference<String?>(null)

    var token: String?
        get() = tokenRef.get()
        set(value) = tokenRef.set(value)
}
