package dev.kawayilab.interknot.ui.screens.level

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.kawayilab.interknot.data.repository.InterknotRepository
import dev.kawayilab.interknot.model.Benefits
import dev.kawayilab.interknot.model.CheckInResult
import dev.kawayilab.interknot.model.CheckInStatus
import dev.kawayilab.interknot.model.DailyExp
import dev.kawayilab.interknot.model.ExpInfo
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LevelViewModel @Inject constructor(
    private val repository: InterknotRepository
) : ViewModel() {

    private val _expInfo = MutableStateFlow<ExpInfo?>(null)
    val expInfo: StateFlow<ExpInfo?> = _expInfo.asStateFlow()

    private val _dailyExp = MutableStateFlow<DailyExp?>(null)
    val dailyExp: StateFlow<DailyExp?> = _dailyExp.asStateFlow()

    private val _checkInStatus = MutableStateFlow<CheckInStatus?>(null)
    val checkInStatus: StateFlow<CheckInStatus?> = _checkInStatus.asStateFlow()

    private val _benefits = MutableStateFlow<Benefits?>(null)
    val benefits: StateFlow<Benefits?> = _benefits.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isCheckingIn = MutableStateFlow(false)
    val isCheckingIn: StateFlow<Boolean> = _isCheckingIn.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _checkInResult = MutableStateFlow<CheckInResult?>(null)
    val checkInResult: StateFlow<CheckInResult?> = _checkInResult.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            listOf(
                repository.getMyExp(),
                repository.getDailyExp(),
                repository.getCheckInStatus(),
                repository.getBenefits()
            ).forEachIndexed { index, result ->
                result.onSuccess {
                    when (index) {
                        0 -> _expInfo.value = it as ExpInfo
                        1 -> _dailyExp.value = it as DailyExp
                        2 -> _checkInStatus.value = it as CheckInStatus
                        3 -> _benefits.value = it as Benefits
                    }
                }.onFailure { _error.value = it.message ?: "加载失败" }
            }
            _isLoading.value = false
        }
    }

    fun checkIn() {
        viewModelScope.launch {
            _isCheckingIn.value = true
            _error.value = null
            repository.checkIn()
                .onSuccess {
                    _checkInResult.value = it
                    load()
                }
                .onFailure { _error.value = it.message ?: "签到失败" }
            _isCheckingIn.value = false
        }
    }

    fun consumeCheckInResult() {
        _checkInResult.value = null
    }
}
