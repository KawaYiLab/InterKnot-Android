package dev.kawayilab.interknot.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.kawayilab.interknot.data.local.UserPreferences
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    preferences: UserPreferences
) : ViewModel() {

    val themeMode = preferences.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences.ThemeMode.SYSTEM)

    private val prefs = preferences

    fun setThemeMode(mode: UserPreferences.ThemeMode) {
        viewModelScope.launch { prefs.setThemeMode(mode) }
    }
}
