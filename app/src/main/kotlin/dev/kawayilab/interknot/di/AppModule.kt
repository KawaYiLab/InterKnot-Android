package dev.kawayilab.interknot.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.kawayilab.interknot.BuildConfig
import dev.kawayilab.interknot.data.api.InterknotApi
import dev.kawayilab.interknot.data.api.InterknotApiImpl
import dev.kawayilab.interknot.data.local.cache.CachedArticleDao
import dev.kawayilab.interknot.data.local.cache.CachedMessageDao
import dev.kawayilab.interknot.data.local.cache.CachedProfileDao
import dev.kawayilab.interknot.data.local.cache.CachedSearchDao
import dev.kawayilab.interknot.data.local.cache.InterknotDatabase
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import android.util.Log
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideHttpClient(json: Json): HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(json)
        }
        install(WebSockets)
        if (BuildConfig.DEBUG) {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("Ktor", message)
                    }
                }
                level = LogLevel.ALL
            }
        }
        expectSuccess = true
        HttpResponseValidator {
            handleResponseExceptionWithRequest { exception, _ ->
                val clientException = exception as? ClientRequestException ?: return@handleResponseExceptionWithRequest
                val response = clientException.response
                val bodyText = runCatching { response.bodyAsText() }.getOrNull() ?: ""
                val message = runCatching {
                    (json.parseToJsonElement(bodyText) as? JsonObject)
                        ?.get("error")?.jsonObject
                        ?.get("message")?.jsonPrimitive?.contentOrNull
                }.getOrNull()
                throw Exception(message ?: response.status.description)
            }
        }
        defaultRequest {
            val base = Url(BuildConfig.API_BASE_URL)
            url {
                protocol = base.protocol
                host = base.host
                port = base.port
                encodedPath = base.encodedPath
            }
        }
    }

    @Provides
    @Singleton
    fun provideInterknotApi(httpClient: HttpClient): InterknotApi = InterknotApiImpl(httpClient)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): InterknotDatabase =
        Room.databaseBuilder(context, InterknotDatabase::class.java, "interknot.db").build()

    @Provides
    fun provideCachedArticleDao(database: InterknotDatabase): CachedArticleDao =
        database.cachedArticleDao()

    @Provides
    fun provideCachedSearchDao(database: InterknotDatabase): CachedSearchDao =
        database.cachedSearchDao()

    @Provides
    fun provideCachedProfileDao(database: InterknotDatabase): CachedProfileDao =
        database.cachedProfileDao()

    @Provides
    fun provideCachedMessageDao(database: InterknotDatabase): CachedMessageDao =
        database.cachedMessageDao()
}
