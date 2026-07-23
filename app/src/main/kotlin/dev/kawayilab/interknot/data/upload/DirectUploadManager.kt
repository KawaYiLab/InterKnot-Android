package dev.kawayilab.interknot.data.upload

import dev.kawayilab.interknot.data.api.InterknotApi
import dev.kawayilab.interknot.model.FileInfo
import dev.kawayilab.interknot.model.UploadedFile
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.headers
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DirectUploadManager @Inject constructor(
    private val api: InterknotApi
) {
    private val uploadClient = HttpClient(OkHttp)

    suspend fun upload(
        bytes: ByteArray,
        filename: String,
        mimeType: String,
        width: Int? = null,
        height: Int? = null,
        fileInfo: FileInfo? = null
    ): Result<UploadedFile> = runCatching {
        val size = bytes.size
        val contentHash = sha256(bytes)
        val signResult = api.signUpload(
            filename = filename,
            mimeType = mimeType,
            size = size,
            contentHash = contentHash,
            width = width,
            height = height,
            fileInfo = fileInfo
        ).getOrThrow()

        signResult.existing?.let { return@runCatching it }

        val uploadUrl = signResult.uploadUrl ?: error("Missing uploadUrl")
        val uploadToken = signResult.uploadToken ?: error("Missing uploadToken")

        val response = uploadClient.put(uploadUrl) {
            headers {
                signResult.headers.forEach { (key, value) ->
                    if (key.equals("Content-Type", ignoreCase = true)) {
                        contentType(ContentType.parse(value))
                    } else {
                        append(key, value)
                    }
                }
            }
            setBody(ByteArrayContent(bytes))
        }

        if (!response.status.isSuccess()) {
            error("S3 upload failed: ${response.status}")
        }

        api.completeUpload(uploadToken, width, height).getOrThrow()
    }

    private fun sha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(bytes).joinToString("") { "%02x".format(it) }
    }
}
