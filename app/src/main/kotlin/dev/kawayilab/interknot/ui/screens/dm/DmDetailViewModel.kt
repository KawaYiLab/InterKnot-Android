package dev.kawayilab.interknot.ui.screens.dm

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.graphics.scale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.kawayilab.interknot.data.realtime.DmSocketManager
import dev.kawayilab.interknot.data.repository.InterknotRepository
import dev.kawayilab.interknot.data.upload.DirectUploadManager
import dev.kawayilab.interknot.model.DmConversationDetail
import dev.kawayilab.interknot.model.DmMessage
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class DmDetailViewModel @Inject constructor(
    private val repository: InterknotRepository,
    private val dmSocketManager: DmSocketManager,
    private val uploadManager: DirectUploadManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _conversation = MutableStateFlow<DmConversationDetail?>(null)
    val conversation: StateFlow<DmConversationDetail?> = _conversation.asStateFlow()

    private val _messages = MutableStateFlow<List<DmMessage>>(emptyList())
    val messages: StateFlow<List<DmMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _editingMessage = MutableStateFlow<DmMessage?>(null)
    val editingMessage: StateFlow<DmMessage?> = _editingMessage.asStateFlow()

    private var conversationId: String? = null
    private var nextCursor: String? = null
    private var hasMorePages = true
    private var selfUserId: Int? = null

    init {
        dmSocketManager.connect()
        viewModelScope.launch {
            dmSocketManager.events.collect { event ->
                if (event.conversationId != conversationId) return@collect
                when (event.type) {
                    "message.created" -> loadMessages(reset = true)
                    "message.edited" -> loadMessages(reset = true)
                    "message.deleted" -> loadMessages(reset = true)
                    "conversation.read" -> loadConversation()
                }
            }
        }
    }

    fun load(conversationId: String?, targetUserId: Int?, targetName: String?) {
        when {
            conversationId != null -> {
                this.conversationId = conversationId
                viewModelScope.launch {
                    _isLoading.value = true
                    loadConversationInternal()
                    loadMessagesInternal(reset = true)
                    _isLoading.value = false
                }
            }
            targetUserId != null && this.conversationId == null -> {
                viewModelScope.launch {
                    _isLoading.value = true
                    repository.createDirectConversation(targetUserId)
                        .onSuccess { (conv, _) ->
                            this@DmDetailViewModel.conversationId = conv.documentId
                            loadConversationInternal()
                            loadMessagesInternal(reset = true)
                        }
                        .onFailure { _error.value = it.message ?: "无法开始会话" }
                    _isLoading.value = false
                }
            }
            this.conversationId != null -> {
                viewModelScope.launch {
                    loadConversationInternal()
                    loadMessagesInternal(reset = true)
                }
            }
        }
    }

    fun loadConversation() {
        viewModelScope.launch { loadConversationInternal() }
    }

    private suspend fun loadConversationInternal() {
        val id = conversationId ?: return
        repository.getDmConversationDetail(id)
            .onSuccess {
                _conversation.value = it
                selfUserId = it.members.find { member -> member.isSelf }?.userId
            }
        repository.markDmConversationRead(id)
    }

    fun loadMessages(reset: Boolean = false) {
        viewModelScope.launch { loadMessagesInternal(reset) }
    }

    private suspend fun loadMessagesInternal(reset: Boolean = false) {
        val id = conversationId ?: return
        if (reset) {
            nextCursor = null
            hasMorePages = true
            _messages.value = emptyList()
        }
        if (!hasMorePages) return

        if (reset) _isLoading.value = true else _isLoadingMore.value = true
        repository.getDmMessages(id, cursor = nextCursor)
            .onSuccess { page ->
                val current = if (reset) emptyList() else _messages.value
                val mapped = page.items.map { it.copy(isFromSelf = it.sender?.userId == selfUserId) }
                // Server returns newest first; display oldest first.
                val combined = (mapped + current).distinctBy { it.documentId }
                _messages.value = combined.sortedBy { it.createdAt }
                nextCursor = page.nextCursor
                hasMorePages = page.hasMore
            }
            .onFailure { _error.value = it.message ?: "加载消息失败" }
        if (reset) _isLoading.value = false else _isLoadingMore.value = false
    }

    fun startEditing(message: DmMessage) {
        if (message.isFromSelf) _editingMessage.value = message
    }

    fun cancelEditing() {
        _editingMessage.value = null
    }

    fun sendText(content: String) {
        val editing = _editingMessage.value
        if (editing != null) {
            editMessage(editing.documentId, content)
        } else {
            sendMessage(content)
        }
    }

    private fun sendMessage(content: String) {
        val id = conversationId ?: return
        if (content.isBlank()) return
        viewModelScope.launch {
            _isSending.value = true
            repository.sendDmMessage(id, content.trim())
                .onSuccess { loadMessagesInternal(reset = true) }
                .onFailure { _error.value = it.message ?: "发送失败" }
            _isSending.value = false
        }
    }

    private fun editMessage(messageId: String, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            _isSending.value = true
            repository.editDmMessage(messageId, content.trim())
                .onSuccess {
                    _editingMessage.value = null
                    loadMessagesInternal(reset = true)
                }
                .onFailure { _error.value = it.message ?: "编辑失败" }
            _isSending.value = false
        }
    }

    fun withdrawMessage(messageId: String) {
        viewModelScope.launch {
            repository.withdrawDmMessage(messageId)
                .onSuccess { loadMessagesInternal(reset = true) }
                .onFailure { _error.value = it.message ?: "撤回失败" }
        }
    }

    fun sendImage(uri: Uri) {
        viewModelScope.launch {
            _isSending.value = true
            runCatching {
                val file = uploadImage(uri)
                if (file?.url == null) {
                    _error.value = "图片上传失败"
                    return@runCatching
                }
                val id = conversationId ?: return@runCatching
                repository.sendDmMessage(id, file.url, kind = "image")
                    .onSuccess { loadMessagesInternal(reset = true) }
                    .onFailure { _error.value = it.message ?: "发送失败" }
            }.onFailure { _error.value = it.message ?: "图片处理失败" }
            _isSending.value = false
        }
    }

    private suspend fun uploadImage(uri: Uri): dev.kawayilab.interknot.model.UploadedFile? {
        return runCatching {
            withContext(Dispatchers.IO) {
                val resolver = context.contentResolver
                val originalBytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: return@withContext null
                val compressed = compressImage(originalBytes, getFilename(uri) ?: "image.jpg")
                uploadManager.upload(compressed.bytes, compressed.filename, "image/jpeg", compressed.width, compressed.height).getOrThrow()
            }
        }.getOrNull()
    }

    private data class CompressedImage(
        val bytes: ByteArray,
        val width: Int,
        val height: Int,
        val filename: String
    )

    private fun compressImage(bytes: ByteArray, originalFilename: String): CompressedImage {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = false
            inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, MAX_IMAGE_DIMENSION)
        }
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            ?: throw IllegalStateException("无法解码图片")

        val scaled = if (bitmap.width > MAX_IMAGE_DIMENSION || bitmap.height > MAX_IMAGE_DIMENSION) {
            val ratio = MAX_IMAGE_DIMENSION.toFloat() / maxOf(bitmap.width, bitmap.height)
            val newWidth = (bitmap.width * ratio).toInt()
            val newHeight = (bitmap.height * ratio).toInt()
            bitmap.scale(newWidth, newHeight)
        } else {
            bitmap
        }

        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        val compressed = out.toByteArray()
        scaled.recycle()
        return CompressedImage(compressed, scaled.width, scaled.height, originalFilename.asJpg())
    }

    private fun calculateInSampleSize(width: Int, height: Int, maxDimension: Int): Int {
        var inSampleSize = 1
        while (width / inSampleSize > maxDimension || height / inSampleSize > maxDimension) {
            inSampleSize *= 2
        }
        return inSampleSize
    }

    private fun String.asJpg(): String {
        val dot = lastIndexOf('.')
        return if (dot > 0) substring(0, dot) + ".jpg" else "$this.jpg"
    }

    private fun getFilename(uri: Uri): String? {
        return runCatching {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                } else null
            }
        }.getOrNull()
    }

    override fun onCleared() {
        super.onCleared()
    }

    companion object {
        private const val MAX_IMAGE_DIMENSION = 1920
        private const val JPEG_QUALITY = 85
    }
}
