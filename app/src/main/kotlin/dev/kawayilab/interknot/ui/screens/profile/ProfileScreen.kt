@file:OptIn(ExperimentalMaterial3Api::class)
package dev.kawayilab.interknot.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.kawayilab.interknot.model.User

@Composable
fun ProfileScreen(
    user: User?,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val denny by viewModel.denny.collectAsStateWithLifecycle()
    val dennyGiven by viewModel.dennyGiven.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    LaunchedEffect(user) {
        if (user != null) viewModel.loadBalance()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBar(
                title = { Text("我的") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (user != null) {
                Text(
                    text = user.name ?: user.username ?: "用户",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "邮箱：${user.email ?: "-"}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "等级：Lv.${user.level ?: 1}  经验：${user.exp ?: 0}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "丁尼余额：$denny  已投出：$dennyGiven",
                    style = MaterialTheme.typography.bodyLarge
                )

                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }

                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Button(
                    onClick = { viewModel.loadBalance() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("刷新余额")
                }

                Button(
                    onClick = { viewModel.logout(onComplete = onLogout) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("退出登录")
                }
            } else {
                Text(
                    text = "未登录",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}
