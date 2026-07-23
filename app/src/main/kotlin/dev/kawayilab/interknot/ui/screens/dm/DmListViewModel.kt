package dev.kawayilab.interknot.ui.screens.dm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.kawayilab.interknot.data.realtime.DmSocketManager
import dev.kawayilab.interknot.data.repository.InterknotRepository
import dev.kawayilab.interknot.model.DmConversation
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class DmListViewModel @Inject constructor(
    private val repository: InterknotRepository,
    private val dmSocketManager: DmSocketManager
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<DmConversation>>(emptyList())
    val conversations: StateFlow<List<DmConversation>> = _conversations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        dmSocketManager.connect()
        viewModelScope.launch {
            dmSocketManager.events.collect { event ->
                when (event.type) {
                    "message.created", "message.edited", "message.deleted",
                    "conversation.updated", "conversation.read" -> refresh()
                }
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getDmConversations()
                .onSuccess { _conversations.value = it }
                .onFailure { _error.value = it.message ?: "加载失败" }
            _isLoading.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Keep socket alive for background updates.
    }
}
