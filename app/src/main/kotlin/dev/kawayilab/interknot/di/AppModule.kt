package dev.kawayilab.interknot.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.kawayilab.interknot.BuildConfig
import dev.kawayilab.interknot.data.api.InterknotApi
import dev.kawayilab.interknot.data.api.InterknotApiImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import javax.inject.Singleton
import kotlinx.serialization.json.Json

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
}
