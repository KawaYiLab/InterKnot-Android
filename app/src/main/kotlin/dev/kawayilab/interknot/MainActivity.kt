package dev.kawayilab.interknot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.kawayilab.interknot.data.api.TokenManager
import dev.kawayilab.interknot.data.local.UserPreferences
import dev.kawayilab.interknot.data.repository.InterknotRepository
import dev.kawayilab.interknot.navigation.InterknotNavHost
import dev.kawayilab.interknot.ui.theme.InterknotTheme
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var repository: InterknotRepository

    @Inject
    lateinit var preferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repository.token.collect { token ->
                TokenManager.token = token
            }
        }

        lifecycleScope.launch {
            repository.loadSessionFromStorage()
            repository.fetchCurrentUser()
        }

        enableEdgeToEdge()
        setContent {
            val themeMode by preferences.themeMode.collectAsStateWithLifecycle(UserPreferences.ThemeMode.SYSTEM)
            val darkTheme = when (themeMode) {
                UserPreferences.ThemeMode.LIGHT -> false
                UserPreferences.ThemeMode.DARK -> true
                UserPreferences.ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            InterknotTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    InterknotNavHost(repository = repository)
                }
            }
        }
    }
}
