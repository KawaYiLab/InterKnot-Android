package dev.kawayilab.interknot.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import dev.kawayilab.interknot.ui.screens.explore.ExploreScreen
import dev.kawayilab.interknot.ui.screens.home.HomeScreen
import dev.kawayilab.interknot.ui.screens.login.LoginScreen
import dev.kawayilab.interknot.ui.screens.post.PostDetailScreen
import dev.kawayilab.interknot.ui.screens.profile.ProfileScreen

private fun InterknotRoute.icon(): ImageVector = when (this) {
    is Home -> Icons.Default.Home
    is Explore -> Icons.Default.Search
    is Profile -> Icons.Default.Person
    else -> Icons.Default.Home
}

private fun InterknotRoute.label(): String = when (this) {
    is Home -> "首页"
    is Explore -> "探索"
    is Profile -> "我的"
    else -> ""
}

@Composable
fun InterknotNavHost(
    modifier: Modifier = Modifier,
    backStack: InterknotBackStack = remember { InterknotBackStack() }
) {
    val navContent: @Composable () -> Unit = {
        NavDisplay(
            backStack = backStack.backStack,
            modifier = modifier,
            onBack = { backStack.goBack() },
            entryProvider = entryProvider {
                entry<Home> {
                    HomeScreen(
                        onPostClick = { id -> backStack.navigate(PostDetail(id)) }
                    )
                }
                entry<Explore> {
                    ExploreScreen(
                        onPostClick = { id -> backStack.navigate(PostDetail(id)) }
                    )
                }
                entry<Profile> {
                    ProfileScreen(
                        onLogout = dropUnlessResumed { backStack.logout() }
                    )
                }
                entry<PostDetail> { key ->
                    PostDetailScreen(
                        postId = key.postId,
                        onNavigateBack = dropUnlessResumed { backStack.goBack() }
                    )
                }
                entry<Login> {
                    LoginScreen(
                        onLoginSuccess = dropUnlessResumed { backStack.login() }
                    )
                }
            }
        )
    }

    if (backStack.isTopLevel) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                backStack.topLevelRoutes.forEach { route ->
                    val selected = backStack.currentTopLevel == route
                    item(
                        selected = selected,
                        onClick = { backStack.navigate(route) },
                        icon = {
                            Icon(
                                imageVector = route.icon(),
                                contentDescription = route.label()
                            )
                        },
                        label = { Text(route.label()) }
                    )
                }
            }
        ) {
            navContent()
        }
    } else {
        navContent()
    }
}
