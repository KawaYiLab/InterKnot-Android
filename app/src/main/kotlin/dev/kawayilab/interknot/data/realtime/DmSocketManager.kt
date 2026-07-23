package dev.kawayilab.interknot.data.realtime

import dev.kawayilab.interknot.BuildConfig
import dev.kawayilab.interknot.data.api.InterknotApi
import dev.kawayilab.interknot.model.DmEvent
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.http.encodedPath
import io.ktor.http.takeFrom
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
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
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Singleton
class DmSocketManager @Inject constructor(
    private val client: HttpClient,
    private val api: InterknotApi,
    private val json: Json
) {
    @Serializable
    private data class DmWsFrame(val type: String, val conversationId: String? = null)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null

    private val _events = MutableSharedFlow<DmEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<DmEvent> = _events.asSharedFlow()

    fun connect() {
        if (job?.isActive == true) return
        job = scope.launch { runLoop() }
    }

    fun disconnect() {
        job?.cancel()
        job = null
    }

    fun dispose() {
        disconnect()
        scope.cancel()
    }

    fun sendTyping(conversationId: String) {
        send(DmWsFrame(type = "typing", conversationId = conversationId))
    }

    fun sendPing() {
        send(DmWsFrame(type = "ping"))
    }

    private fun send(frame: DmWsFrame) {
        // Best-effort; the socket job owns the actual outgoing channel.
        // This is a simplified placeholder; real send requires session scope.
    }

    private suspend fun runLoop() {
        while (coroutineContext.isActive) {
            val ticket = api.getDmSocketTicket().getOrNull()?.ticket
            if (ticket == null) {
                delay(5_000)
                continue
            }
            try {
                val base = Url(BuildConfig.API_BASE_URL)
                val url = URLBuilder().takeFrom(base).apply {
                    protocol = URLProtocol.WS
                    encodedPath = "/dm/socket"
                    parameters.append("ticket", ticket)
                }.build()

                client.webSocket(request = { this.url.takeFrom(url) }) {
                    val pingJob = launch {
                        while (isActive) {
                            delay(20_000)
                            try {
                                outgoing.send(Frame.Text(json.encodeToString(DmWsFrame.serializer(), DmWsFrame("ping"))))
                            } catch (_: Throwable) {
                                break
                            }
                        }
                    }
                    try {
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                parseAndEmit(frame.readText())
                            }
                        }
                    } finally {
                        pingJob.cancel()
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Throwable) {
                // ignored; reconnect below
            }
            delay(3_000)
        }
    }

    private fun parseAndEmit(text: String) {
        try {
            val event = json.decodeFromString(DmEvent.serializer(), text)
            _events.tryEmit(event)
        } catch (_: Throwable) {
            // Ignore malformed frames.
        }
    }
}
