package dev.kawayilab.interknot.ui.screens.knock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.kawayilab.interknot.data.repository.InterknotRepository
import dev.kawayilab.interknot.model.KnockConversation
import dev.kawayilab.interknot.model.KnockNotification
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class KnockViewModel @Inject constructor(
    private val repository: InterknotRepository
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<KnockConversation>>(emptyList())
    val conversations: StateFlow<List<KnockConversation>> = _conversations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _selectedConversation = MutableStateFlow<KnockConversation?>(null)
    val selectedConversation: StateFlow<KnockConversation?> = _selectedConversation.asStateFlow()

    private val _messages = MutableStateFlow<List<KnockNotification>>(emptyList())
    val messages: StateFlow<List<KnockNotification>> = _messages.asStateFlow()

    private val _isLoadingMessages = MutableStateFlow(false)
    val isLoadingMessages: StateFlow<Boolean> = _isLoadingMessages.asStateFlow()

    private var pollingJob: Job? = null

    init {
        startPolling()
    }

    fun startPolling() {
        if (pollingJob?.isActive == true) return
        pollingJob = viewModelScope.launch {
            while (true) {
                refresh()
                repository.getUnreadNotificationCount().onSuccess { _unreadCount.value = it }
                delay(15_000)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
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
                .onSuccess { refresh() }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}
