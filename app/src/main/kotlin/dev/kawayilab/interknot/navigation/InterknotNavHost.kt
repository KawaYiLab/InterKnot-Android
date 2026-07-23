package dev.kawayilab.interknot.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.kawayilab.interknot.data.repository.InterknotRepository
import kotlinx.coroutines.launch
import dev.kawayilab.interknot.ui.components.navigation.InterknotBottomNav
import dev.kawayilab.interknot.ui.screens.create.CreateScreen
import dev.kawayilab.interknot.ui.screens.dm.DmDetailScreen
import dev.kawayilab.interknot.ui.screens.dm.DmListScreen
import dev.kawayilab.interknot.ui.screens.exam.ExamScreen
import dev.kawayilab.interknot.ui.screens.explore.ExploreScreen
import dev.kawayilab.interknot.ui.screens.home.HomeScreen
import dev.kawayilab.interknot.ui.screens.knock.KnockScreen
import dev.kawayilab.interknot.ui.screens.level.LevelScreen
import dev.kawayilab.interknot.ui.screens.login.LoginScreen
import dev.kawayilab.interknot.ui.screens.post.PostDetailScreen
import dev.kawayilab.interknot.ui.screens.profile.ProfileScreen
import dev.kawayilab.interknot.ui.screens.search.SearchScreen
import dev.kawayilab.interknot.ui.screens.settings.SettingsScreen

@Composable
fun InterknotNavHost(
    repository: InterknotRepository,
    modifier: Modifier = Modifier,
    backStack: InterknotBackStack = remember { InterknotBackStack() }
) {
    val user by repository.user.collectAsStateWithLifecycle()
    val unreadCount by repository.unreadNotificationCount.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    LaunchedEffect(user) {
        if (user != null) {
            backStack.login()
        } else {
            backStack.logout()
        }
    }

    val currentTopLevel = backStack.currentTopLevel

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(left = 0.dp, top = 0.dp, right = 0.dp, bottom = 0.dp),
        bottomBar = {
            if (backStack.isTopLevel) {
                InterknotBottomNav(
                    currentRoute = currentTopLevel,
                    onNavigate = { backStack.navigate(it) },
                    unreadCount = unreadCount
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
                    entry<Create> {
                        CreateScreen(
                            onNavigateBack = { backStack.goBack() },
                            onNavigateToExam = { backStack.navigate(Exam) }
                        )
                    }
                    entry<Exam> { ExamScreen(onNavigateBack = { backStack.goBack() }) }
                    entry<Level> { LevelScreen() }
                    entry<Explore> {
                        ExploreScreen(
                            onSearchClick = { backStack.navigate(Search()) },
                            onCategoryClick = { category ->
                                backStack.navigate(Search(category = category?.slug))
                            },
                            onPostClick = { id -> backStack.navigate(PostDetail(id)) }
                        )
                    }
                    entry<Profile> {
                        ProfileScreen(
                            documentId = null,
                            onLogout = {
                                scope.launch { repository.logout() }
                            },
                            onNavigateToPost = { id -> backStack.navigate(PostDetail(id)) },
                            onNavigateToLevel = { backStack.navigate(Level) },
                            onNavigateToDm = { userId, name ->
                                backStack.navigate(DmDetail(targetUserId = userId, targetName = name))
                            },
                            onNavigateToDmList = { backStack.navigate(DmList) },
                            onNavigateToSettings = { backStack.navigate(Settings) }
                        )
                    }
                    entry<ProfileDetail> { key ->
                        ProfileScreen(
                            documentId = key.documentId,
                            onLogout = { },
                            onNavigateToPost = { id -> backStack.navigate(PostDetail(id)) },
                            onNavigateToLevel = { backStack.navigate(Level) },
                            onNavigateToDm = { userId, name ->
                                backStack.navigate(DmDetail(targetUserId = userId, targetName = name))
                            },
                            onNavigateToDmList = { },
                            onNavigateToSettings = { }
                        )
                    }
                    entry<Settings> {
                        SettingsScreen(onNavigateBack = { backStack.goBack() })
                    }
                    entry<PostDetail> { key ->
                        PostDetailScreen(
                            postId = key.postId,
                            onNavigateBack = { backStack.goBack() },
                            onAuthorClick = { authorDocumentId ->
                                backStack.navigate(ProfileDetail(authorDocumentId))
                            }
                        )
                    }
                    entry<Search> { key ->
                        SearchScreen(
                            initialQuery = key.query,
                            initialCategory = key.category,
                            onBack = { backStack.goBack() },
                            onPostClick = { id -> backStack.navigate(PostDetail(id)) }
                        )
                    }
                    entry<DmList> {
                        DmListScreen(
                            onNavigateBack = { backStack.goBack() },
                            onConversationClick = { conversation ->
                                backStack.navigate(DmDetail(conversation.documentId))
                            }
                        )
                    }
                    entry<DmDetail> { key ->
                        DmDetailScreen(
                            conversationId = key.conversationId,
                            targetUserId = key.targetUserId,
                            targetName = key.targetName,
                            onNavigateBack = { backStack.goBack() }
                        )
                    }
                    entry<Login> {
                        LoginScreen(onLoginSuccess = { backStack.login() })
                    }
                }
            )
        }
    }
}
