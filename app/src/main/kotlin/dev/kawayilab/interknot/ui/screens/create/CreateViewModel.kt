package dev.kawayilab.interknot.ui.screens.create

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.kawayilab.interknot.data.repository.InterknotRepository
import dev.kawayilab.interknot.data.upload.DirectUploadManager
import dev.kawayilab.interknot.model.Benefits
import dev.kawayilab.interknot.model.Category
import dev.kawayilab.interknot.model.ExamStatus
import dev.kawayilab.interknot.model.UploadedFile
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CreateViewModel @Inject constructor(
    private val repository: InterknotRepository,
    private val uploadManager: DirectUploadManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _body = MutableStateFlow("")
    val body: StateFlow<String> = _body.asStateFlow()

    private val _selectedImages = MutableStateFlow<List<PendingImage>>(emptyList())
    val selectedImages: StateFlow<List<PendingImage>> = _selectedImages.asStateFlow()

    private val _category = MutableStateFlow<Category?>(null)
    val category: StateFlow<Category?> = _category.asStateFlow()

    private val _isAnonymous = MutableStateFlow(false)
    val isAnonymous: StateFlow<Boolean> = _isAnonymous.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _isPublishing = MutableStateFlow(false)
    val isPublishing: StateFlow<Boolean> = _isPublishing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _publishedId = MutableStateFlow<String?>(null)
    val publishedId: StateFlow<String?> = _publishedId.asStateFlow()

    private val _examStatus = MutableStateFlow<ExamStatus?>(null)
    val examStatus: StateFlow<ExamStatus?> = _examStatus.asStateFlow()

    private val _benefits = MutableStateFlow<Benefits?>(null)
    val benefits: StateFlow<Benefits?> = _benefits.asStateFlow()

    private var uploadJob: Job? = null

    init {
        loadCategories()
        loadExamAndBenefits()
    }

    fun setTitle(value: String) { _title.value = value }
    fun setBody(value: String) { _body.value = value }
    fun setCategory(value: Category?) { _category.value = value }
    fun setAnonymous(value: Boolean) { _isAnonymous.value = value }

    fun addImages(uris: List<Uri>) {
        val current = _selectedImages.value.toMutableList()
        uris.forEach { uri ->
            if (current.none { it.uri == uri }) {
                current.add(PendingImage(uri = uri))
            }
        }
        _selectedImages.value = current
    }

    fun removeImage(uri: Uri) {
        _selectedImages.value = _selectedImages.value.filter { it.uri != uri }
    }

    fun dismissError() { _error.value = null }

    fun publish(
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (_isPublishing.value) return
        uploadJob?.cancel()
        uploadJob = viewModelScope.launch {
            _error.value = null
            _publishedId.value = null
            _isPublishing.value = true

            val status = _examStatus.value
            if (status?.passed != true) {
                _error.value = "需要通过入站考试后才能发布"
                _isPublishing.value = false
                onError(_error.value ?: "未知错误")
                return@launch
            }

            val t = _title.value.trim()
            val b = _body.value.trim()
            if (t.isEmpty() || b.isEmpty()) {
                _error.value = "标题和正文不能为空"
                _isPublishing.value = false
                onError(_error.value ?: "未知错误")
                return@launch
            }

            val maxBody = _benefits.value?.benefits?.articleMaxBody ?: 1500
            if (b.length > maxBody) {
                _error.value = "正文超过当前等级上限 ${maxBody} 字"
                _isPublishing.value = false
                onError(_error.value ?: "未知错误")
                return@launch
            }

            val maxImages = _benefits.value?.benefits?.articleMaxImages ?: 9
            val pending = _selectedImages.value
            if (pending.size > maxImages) {
                _error.value = "图片超过当前等级上限 ${maxImages} 张"
                _isPublishing.value = false
                onError(_error.value ?: "未知错误")
                return@launch
            }

            val uploadedIds = mutableListOf<String>()
            if (pending.isNotEmpty()) {
                _selectedImages.value = pending.map { it.copy(isUploading = true) }
                pending.forEach { item ->
                    val file = uploadImage(item.uri)
                    if (file == null) {
                        _error.value = "图片上传失败"
                        _isPublishing.value = false
                        _selectedImages.value = pending.map { it.copy(isUploading = false) }
                        onError(_error.value ?: "未知错误")
                        return@launch
                    }
                    file.documentId?.let { uploadedIds.add(it) }
                }
                _selectedImages.value = pending.map { it.copy(isUploading = false) }
            }

            repository.publishArticle(
                title = t,
                text = b,
                category = _category.value?.slug,
                isAnonymous = _isAnonymous.value,
                coverDocumentIds = uploadedIds
            ).onSuccess { id ->
                _publishedId.value = id
                clearForm()
                onSuccess()
            }.onFailure { err ->
                _error.value = err.message ?: "发布失败"
                onError(_error.value ?: "未知错误")
            }
            _isPublishing.value = false
        }
    }

    private suspend fun uploadImage(uri: Uri): UploadedFile? {
        return runCatching {
            val resolver = context.contentResolver
            val originalBytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
            val compressed = compressImage(originalBytes, getFilename(uri) ?: "image.jpg")
            uploadManager.upload(compressed.bytes, compressed.filename, "image/jpeg", compressed.width, compressed.height).getOrThrow()
        }.getOrNull()
    }

    private data class CompressedImage(
        val bytes: ByteArray,
        val width: Int,
        val height: Int,
        val filename: String
    )

    private fun compressImage(
        bytes: ByteArray,
        originalFilename: String
    ): CompressedImage {
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

    private fun loadCategories() {
        viewModelScope.launch {
            repository.getCategories().onSuccess { _categories.value = it }
        }
    }

    private fun loadExamAndBenefits() {
        viewModelScope.launch {
            repository.getExamStatus().onSuccess { _examStatus.value = it }
        }
        viewModelScope.launch {
            repository.getBenefits().onSuccess { _benefits.value = it }
        }
    }

    private fun clearForm() {
        _title.value = ""
        _body.value = ""
        _selectedImages.value = emptyList()
        _category.value = null
        _isAnonymous.value = false
    }

    data class PendingImage(
        val uri: Uri,
        val isUploading: Boolean = false
    )

    companion object {
        private const val MAX_IMAGE_DIMENSION = 1920
        private const val JPEG_QUALITY = 85
    }
}
