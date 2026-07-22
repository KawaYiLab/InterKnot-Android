package dev.kawayilab.interknot.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.kawayilab.interknot.data.repository.InterknotRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: InterknotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(identifier: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.login(identifier, password)
                .onSuccess {
                    _uiState.value = LoginUiState(isLoggedIn = true)
                }
                .onFailure { error ->
                    _uiState.value = LoginUiState(error = error.message ?: "登录失败")
                }
        }
    }

    fun register(email: String, code: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.register(email, code, password)
                .onSuccess {
                    _uiState.value = LoginUiState(isLoggedIn = true)
                }
                .onFailure { error ->
                    _uiState.value = LoginUiState(error = error.message ?: "注册失败")
                }
        }
    }

    fun sendRegisterCode(email: String) {
        viewModelScope.launch {
            repository.sendRegisterCode(email)
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
