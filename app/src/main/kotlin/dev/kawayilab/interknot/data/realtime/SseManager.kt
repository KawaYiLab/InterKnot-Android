package dev.kawayilab.interknot.data.realtime

import dev.kawayilab.interknot.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import okhttp3.Call
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

@Singleton
class SseManager @Inject constructor(
    private val json: Json
) {
    data class SseEvent(val type: String, val data: JsonElement)

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .writeTimeout(0, TimeUnit.SECONDS)
        .build()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null
    private var call: Call? = null
    private var retryDelayMs = 5_000L

    private val _events = MutableSharedFlow<SseEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<SseEvent> = _events.asSharedFlow()

    fun connect(token: String?) {
        if (token.isNullOrBlank()) return
        disconnect()
        job = scope.launch {
            while (isActive) {
                try {
                    listen(token)
                } catch (e: CancellationException) {
                    break
                } catch (_: Throwable) {
                    // ignored; retry below
                }
                delay(retryDelayMs)
            }
        }
    }

    fun disconnect() {
        job?.cancel()
        job = null
        call?.cancel()
        call = null
    }

    fun dispose() {
        disconnect()
        scope.cancel()
    }

    private fun listen(token: String) {
        val url = "${BuildConfig.API_BASE_URL}knock/stream?token=$token"
        val request = Request.Builder()
            .url(url)
            .header("Accept", "text/event-stream")
            .header("Cache-Control", "no-cache, no-transform")
            .get()
            .build()

        val localCall = client.newCall(request)
        call = localCall
        val response: Response = localCall.execute()
        if (!response.isSuccessful || response.body == null) {
            throw IllegalStateException("SSE connect failed: ${response.code}")
        }

        val source = response.body!!.source()
        var eventType = "message"
        val eventData = StringBuilder()
        while (job?.isActive != false) {
            val line = source.readUtf8Line() ?: break
            if (line.isEmpty()) {
                if (eventData.isNotEmpty()) {
                    emit(eventType, eventData.toString())
                    eventData.clear()
                }
                eventType = "message"
            } else if (line.startsWith("event:")) {
                eventType = line.substringAfter("event:").trim()
            } else if (line.startsWith("data:")) {
                if (eventData.isNotEmpty()) eventData.append("\n")
                eventData.append(line.substringAfter("data:").trim())
            } else if (line.startsWith("retry:")) {
                val value = line.substringAfter("retry:").trim().toLongOrNull()
                if (value != null && value > 0) retryDelayMs = value
            }
            // Lines starting with ":" are comments/heartbeats and ignored.
        }
    }

    private fun emit(type: String, data: String) {
        val element = try {
            json.parseToJsonElement(data)
        } catch (_: Throwable) {
            JsonNull
        }
        _events.tryEmit(SseEvent(type = type, data = element))
    }
}
