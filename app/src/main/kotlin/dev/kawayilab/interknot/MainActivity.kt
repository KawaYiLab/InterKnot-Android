package dev.kawayilab.interknot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.kawayilab.interknot.data.api.TokenManager
import dev.kawayilab.interknot.data.repository.InterknotRepository
import dev.kawayilab.interknot.navigation.InterknotNavHost
import dev.kawayilab.interknot.ui.theme.InterknotTheme
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var repository: InterknotRepository

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
            InterknotTheme {
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
