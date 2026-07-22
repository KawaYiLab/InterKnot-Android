package dev.kawayilab.interknot.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.kawayilab.interknot.ui.components.navigation.InterknotBottomNav
import dev.kawayilab.interknot.ui.screens.create.CreateScreen
import dev.kawayilab.interknot.ui.screens.home.HomeScreen
import dev.kawayilab.interknot.ui.screens.knock.KnockScreen
import dev.kawayilab.interknot.ui.screens.level.LevelScreen
import dev.kawayilab.interknot.ui.screens.login.LoginScreen
import dev.kawayilab.interknot.ui.screens.post.PostDetailScreen
import dev.kawayilab.interknot.ui.screens.profile.ProfileScreen

@Composable
fun InterknotNavHost(
    modifier: Modifier = Modifier,
    backStack: InterknotBackStack = remember { InterknotBackStack() }
) {
    val currentTopLevel = backStack.currentTopLevel

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(left = 0.dp, top = 0.dp, right = 0.dp, bottom = 0.dp),
        bottomBar = {
            if (backStack.isTopLevel) {
                InterknotBottomNav(
                    currentRoute = currentTopLevel,
                    onNavigate = { backStack.navigate(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavDisplay(
                backStack = backStack.backStack,
                modifier = Modifier.fillMaxSize(),
                onBack = { backStack.goBack() },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                entryProvider = entryProvider {
                    entry<Home> {
                        HomeScreen(onPostClick = { id -> backStack.navigate(PostDetail(id)) })
                    }
                    entry<Knock> { KnockScreen() }
                    entry<Create> { CreateScreen(onNavigateBack = dropUnlessResumed { backStack.goBack() }) }
                    entry<Level> { LevelScreen() }
                    entry<Profile> {
                        ProfileScreen(onLogout = dropUnlessResumed { backStack.logout() })
                    }
                    entry<PostDetail> { key ->
                        PostDetailScreen(
                            postId = key.postId,
                            onNavigateBack = dropUnlessResumed { backStack.goBack() }
                        )
                    }
                    entry<Login> {
                        LoginScreen(onLoginSuccess = dropUnlessResumed { backStack.login() })
                    }
                }
            )
        }
    }
}
