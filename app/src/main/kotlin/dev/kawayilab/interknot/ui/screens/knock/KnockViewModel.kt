package dev.kawayilab.interknot.ui.screens.knock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.kawayilab.interknot.data.realtime.SseManager
import dev.kawayilab.interknot.data.repository.InterknotRepository
import dev.kawayilab.interknot.model.KnockConversation
import dev.kawayilab.interknot.model.KnockNotification
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class KnockViewModel @Inject constructor(
    private val repository: InterknotRepository,
    private val sseManager: SseManager
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<KnockConversation>>(emptyList())
    val conversations: StateFlow<List<KnockConversation>> = _conversations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val unreadCount: StateFlow<Int> = repository.unreadNotificationCount

    private val _selectedConversation = MutableStateFlow<KnockConversation?>(null)
    val selectedConversation: StateFlow<KnockConversation?> = _selectedConversation.asStateFlow()

    private val _messages = MutableStateFlow<List<KnockNotification>>(emptyList())
    val messages: StateFlow<List<KnockNotification>> = _messages.asStateFlow()

    private val _isLoadingMessages = MutableStateFlow(false)
    val isLoadingMessages: StateFlow<Boolean> = _isLoadingMessages.asStateFlow()

    private var tokenJob: Job? = null
    private var eventJob: Job? = null

    init {
        startPolling()
    }

    fun startPolling() {
        if (tokenJob?.isActive == true) return

        tokenJob = viewModelScope.launch {
            repository.token.collect { token ->
                sseManager.connect(token)
            }
        }

        eventJob = viewModelScope.launch {
            sseManager.events.collect { event ->
                when (event.type) {
                    "notification.created",
                    "notification.read",
                    "notification.read.bulk",
                    "message.created",
                    "message.deleted",
                    "message.edited",
                    "conversation.read",
                    "conversation.updated" -> {
                        refresh()
                        updateUnreadCount()
                    }
                }
            }
        }

        refresh()
        updateUnreadCount()
    }

    fun stopPolling() {
        tokenJob?.cancel()
        tokenJob = null
        eventJob?.cancel()
        eventJob = null
        sseManager.disconnect()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getKnockConversations()
                .onSuccess { _conversations.value = it }
                .onFailure { _error.value = it.message ?: "加载失败" }
            _isLoading.value = false
        }
    }

    private fun updateUnreadCount() {
        viewModelScope.launch {
            repository.getUnreadNotificationCount()
        }
    }

    fun selectConversation(conversation: KnockConversation?) {
        _selectedConversation.value = conversation
        conversation?.let { loadMessages(it.id) }
    }

    fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            _isLoadingMessages.value = true
            repository.getKnockMessages(conversationId)
                .onSuccess { _messages.value = it }
                .onFailure { _error.value = it.message ?: "加载消息失败" }
            _isLoadingMessages.value = false
        }
    }

    fun markConversationRead(conversationId: String) {
        viewModelScope.launch {
            repository.markConversationRead(conversationId)
                .onSuccess { refresh(); updateUnreadCount() }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}
