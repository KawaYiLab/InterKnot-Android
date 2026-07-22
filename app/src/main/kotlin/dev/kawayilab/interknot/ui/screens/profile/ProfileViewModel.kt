package dev.kawayilab.interknot.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.kawayilab.interknot.data.repository.InterknotRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: InterknotRepository
) : ViewModel() {

    private val _denny = MutableStateFlow(0)
    val denny: StateFlow<Int> = _denny.asStateFlow()

    private val _dennyGiven = MutableStateFlow(0)
    val dennyGiven: StateFlow<Int> = _dennyGiven.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadBalance() {
        if (repository.user.value == null) return
        viewModelScope.launch {
            _isLoading.value = true
            repository.getDennyBalance()
                .onSuccess { balance ->
                    _denny.value = balance.denny
                    _dennyGiven.value = balance.dennyGiven
                    _error.value = null
                }
                .onFailure { _error.value = it.message ?: "余额加载失败" }
            _isLoading.value = false
        }
    }

    fun logout(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.logout()
            _denny.value = 0
            _dennyGiven.value = 0
            onComplete()
        }
    }
}
