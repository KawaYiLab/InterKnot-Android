@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
package dev.kawayilab.interknot.ui.screens.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.kawayilab.interknot.ui.components.common.InterknotImage
import dev.kawayilab.interknot.model.Article
import dev.kawayilab.interknot.model.ArticleRef
import dev.kawayilab.interknot.model.Author
import dev.kawayilab.interknot.model.Avatar
import dev.kawayilab.interknot.model.BusinessCard
import dev.kawayilab.interknot.model.Comment
import dev.kawayilab.interknot.model.Profile
import dev.kawayilab.interknot.ui.components.common.EmptyState
import dev.kawayilab.interknot.ui.components.common.ErrorState
import dev.kawayilab.interknot.ui.components.post.PostCard
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ROOT)

@Composable
fun ProfileScreen(
    documentId: String?,
    onLogout: () -> Unit,
    onNavigateToPost: (String) -> Unit,
    onNavigateToLevel: () -> Unit,
    onNavigateToDm: (Int?, String?) -> Unit = { _, _ -> },
    onNavigateToDmList: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val articles by viewModel.articles.collectAsStateWithLifecycle()
    val comments by viewModel.comments.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val denny by viewModel.denny.collectAsStateWithLifecycle()
    val dennyGiven by viewModel.dennyGiven.collectAsStateWithLifecycle()
    val hasMoreArticles by viewModel.hasMoreArticles.collectAsStateWithLifecycle()
    val hasMoreComments by viewModel.hasMoreComments.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showEditDialog by remember { mutableStateOf(false) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    var showCardDialog by remember { mutableStateOf(false) }
    var showPinnedDialog by remember { mutableStateOf(false) }
    var showBlockedDialog by remember { mutableStateOf(false) }

    LaunchedEffect(documentId) {
        viewModel.loadProfile(documentId)
    }

    LaunchedEffect(error) {
        error?.let { scope.launch { snackbarHostState.showSnackbar(it) } }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (profile?.isSelf == true) "我的" else "主页") },
                actions = {
                    if (profile?.isSelf == true) {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "设置"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.loadProfile(documentId) },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (profile == null && !isLoading && error != null) {
                ErrorState(
                    message = error ?: "加载失败",
                    onRetry = { viewModel.loadProfile(documentId) },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                ProfileContent(
                    profile = profile,
                    articles = articles,
                    comments = comments,
                    isLoadingMore = isLoadingMore,
                    hasMoreArticles = hasMoreArticles,
                    hasMoreComments = hasMoreComments,
                    denny = denny,
                    dennyGiven = dennyGiven,
                    onPostClick = onNavigateToPost,
                    onLoadMoreArticles = { viewModel.loadArticles(documentId) },
                    onLoadMoreComments = { viewModel.loadComments(documentId) },
                    onFollowClick = { viewModel.toggleFollow() },
                    onEditClick = { showEditDialog = true },
                    onLevelClick = onNavigateToLevel,
                    onLogout = onLogout,
                    onAvatarClick = { showAvatarDialog = true },
                    onCardClick = { showCardDialog = true },
                    onPinnedClick = { showPinnedDialog = true },
                    onBlockedClick = { showBlockedDialog = true },
                    onDmClick = { profile?.author?.userId?.let { onNavigateToDm(it, profile?.author?.name ?: profile?.author?.username ?: "") } },
                    onDmListClick = onNavigateToDmList
                )
            }
        }
    }

    if (showEditDialog && profile?.isSelf == true) {
        EditProfileDialog(
            profile = profile?.author,
            onDismiss = { showEditDialog = false },
            onSave = { name, bio, hidden ->
                if (name != profile?.author?.name) viewModel.updateName(name)
                if (bio != profile?.author?.bio) viewModel.updateBio(bio)
                viewModel.updateVisibility(hidden)
                showEditDialog = false
            }
        )
    }

    if (showAvatarDialog) {
        AvatarPickerDialog(
            viewModel = viewModel,
            onDismiss = { showAvatarDialog = false }
        )
    }

    if (showCardDialog) {
        BusinessCardPickerDialog(
            viewModel = viewModel,
            onDismiss = { showCardDialog = false }
        )
    }

    if (showPinnedDialog) {
        PinnedArticlesDialog(
            viewModel = viewModel,
            onDismiss = { showPinnedDialog = false }
        )
    }

    if (showBlockedDialog) {
        BlockedAuthorsDialog(
            viewModel = viewModel,
            onDismiss = { showBlockedDialog = false }
        )
    }
}

@Composable
private fun ProfileContent(
    profile: Profile?,
    articles: List<Article>,
    comments: List<Comment>,
    isLoadingMore: Boolean,
    hasMoreArticles: Boolean,
    hasMoreComments: Boolean,
    denny: Int,
    dennyGiven: Int,
    onPostClick: (String) -> Unit,
    onLoadMoreArticles: () -> Unit,
    onLoadMoreComments: () -> Unit,
    onFollowClick: () -> Unit,
    onEditClick: () -> Unit,
    onLevelClick: () -> Unit,
    onLogout: () -> Unit,
    onAvatarClick: () -> Unit,
    onCardClick: () -> Unit,
    onPinnedClick: () -> Unit,
    onBlockedClick: () -> Unit,
    onDmClick: () -> Unit,
    onDmListClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("帖子", "评论")
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val count = if (selectedTab == 0) articles.size else comments.size
            val hasMore = if (selectedTab == 0) hasMoreArticles else hasMoreComments
            val lastIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            hasMore && !isLoadingMore && lastIndex >= count - 1
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            if (selectedTab == 0) onLoadMoreArticles() else onLoadMoreComments()
        }
    }

    LaunchedEffect(selectedTab) {
        listState.scrollToItem(0)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            ProfileHeader(
                profile = profile,
                denny = denny,
                dennyGiven = dennyGiven,
                onFollowClick = onFollowClick,
                onEditClick = onEditClick,
                onLevelClick = onLevelClick,
                onLogout = onLogout,
                onAvatarClick = onAvatarClick,
                onCardClick = onCardClick,
                onPinnedClick = onPinnedClick,
                onBlockedClick = onBlockedClick,
                onDmClick = onDmClick,
                onDmListClick = onDmListClick
            )
        }

        stickyHeader {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
        }

        if (profile == null) {
            item { LoadingItem() }
        } else if (selectedTab == 0) {
            if (articles.isEmpty() && !isLoadingMore) {
                item { EmptyState(message = "还没有帖子", modifier = Modifier.fillMaxWidth()) }
            } else {
                itemsIndexed(articles, key = { _, article -> article.documentId }) { _, article ->
                    PostCard(
                        article = article,
                        onClick = { onPostClick(article.documentId) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                if (isLoadingMore) {
                    item { LoadingItem() }
                }
            }
        } else {
            if (comments.isEmpty() && !isLoadingMore) {
                item { EmptyState(message = "还没有评论", modifier = Modifier.fillMaxWidth()) }
            } else {
                itemsIndexed(comments, key = { _, comment -> comment.documentId }) { _, comment ->
                    CommentItem(
                        comment = comment,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                if (isLoadingMore) {
                    item { LoadingItem() }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    profile: Profile?,
    denny: Int,
    dennyGiven: Int,
    onFollowClick: () -> Unit,
    onEditClick: () -> Unit,
    onLevelClick: () -> Unit,
    onLogout: () -> Unit,
    onAvatarClick: () -> Unit,
    onCardClick: () -> Unit,
    onPinnedClick: () -> Unit,
    onBlockedClick: () -> Unit,
    onDmClick: () -> Unit,
    onDmListClick: () -> Unit
) {
    val author = profile?.author
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AvatarImage(author = author, modifier = Modifier.size(80.dp))

        Text(
            text = author?.name ?: author?.username ?: "用户",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "@${author?.username ?: ""} · Lv.${author?.level ?: 1}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ProfileStatsRow(profile)

        if (profile?.isSelf == true) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                OutlinedButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("编辑资料")
                }
                FilledTonalButton(onClick = onLevelClick) {
                    Text("等级 · $denny 丁尼")
                }
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TextButton(onClick = onAvatarClick) { Text("头像") }
                TextButton(onClick = onCardClick) { Text("名片") }
                TextButton(onClick = onPinnedClick) { Text("置顶") }
                TextButton(onClick = onBlockedClick) { Text("黑名单") }
                TextButton(onClick = onDmListClick) { Text("私信") }
                TextButton(onClick = onLogout) { Text("退出") }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                val isFollowing = author?.isFollowing == true
                Button(onClick = onFollowClick) {
                    Text(if (isFollowing) "已关注" else "关注")
                }
                OutlinedButton(onClick = onDmClick) { Text("私信") }
                if (profile?.isBlocked == true) {
                    OutlinedButton(onClick = onFollowClick) { Text("已拉黑") }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileStatsRow(profile: Profile?) {
    val stats = profile?.stats
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        StatChip(label = "帖子", value = stats?.articleCount ?: 0)
        StatChip(label = "评论", value = stats?.commentCount ?: 0)
        StatChip(label = "获赞", value = stats?.totalLikes ?: 0)
        StatChip(label = "粉丝", value = stats?.followersCount ?: 0)
        StatChip(label = "关注", value = stats?.followingCount ?: 0)
    }
}

@Composable
private fun StatChip(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AvatarImage(author: Author?, modifier: Modifier = Modifier) {
    val avatarUrl = author?.equippedAvatar?.image?.url ?: author?.avatarUrl
    Box(
        modifier = modifier
            .clip(CircleShape)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        if (avatarUrl != null) {
            InterknotImage(
                model = avatarUrl,
                contentDescription = "头像",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
private fun CommentItem(
    comment: Comment,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = formatTime(comment.createdAt),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = comment.content,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp)
        )
        if (comment.article != null) {
            Text(
                text = "来自：${comment.article.title ?: "帖子"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp).clickable { }
            )
        }
    }
}

@Composable
private fun LoadingItem() {
    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EditProfileDialog(
    profile: Author?,
    onDismiss: () -> Unit,
    onSave: (String, String, Boolean) -> Unit
) {
    var name by remember { mutableStateOf(profile?.name ?: "") }
    var bio by remember { mutableStateOf(profile?.bio ?: "") }
    var hidden by remember { mutableStateOf(profile?.profileHidden ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑资料") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("昵称") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("签名") },
                    maxLines = 3
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("隐藏主页", modifier = Modifier.weight(1f))
                    Switch(checked = hidden, onCheckedChange = { hidden = it })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, bio, hidden) }) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
private fun AvatarPickerDialog(
    viewModel: ProfileViewModel,
    onDismiss: () -> Unit
) {
    val avatars by viewModel.avatars.collectAsStateWithLifecycle()
    val equipped by viewModel.equippedAvatarId.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadAvatars() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择头像") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                items(avatars, key = { it.documentId ?: it.name ?: "" }) { avatar ->
                    val selected = equipped == avatar.documentId
                    SelectionListItem(
                        imageUrl = avatar.image?.url,
                        title = avatar.name ?: "头像",
                        selected = selected
                    ) {
                        viewModel.equipAvatar(avatar.documentId)
                        onDismiss()
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } }
    )
}

@Composable
private fun BusinessCardPickerDialog(
    viewModel: ProfileViewModel,
    onDismiss: () -> Unit
) {
    val cards by viewModel.businessCards.collectAsStateWithLifecycle()
    val equipped by viewModel.equippedCardId.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadBusinessCards() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择名片") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                items(cards, key = { it.documentId ?: it.name ?: "" }) { card ->
                    val selected = equipped == card.documentId
                    SelectionListItem(
                        imageUrl = card.image?.url,
                        title = card.name ?: "名片",
                        selected = selected
                    ) {
                        viewModel.equipBusinessCard(card.documentId)
                        onDismiss()
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } }
    )
}

@Composable
private fun SelectionListItem(
    imageUrl: String?,
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (imageUrl != null) {
            InterknotImage(
                model = imageUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(48.dp).clip(CircleShape)
            )
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
        RadioButton(selected = selected, onClick = onClick)
    }
}

@Composable
private fun PinnedArticlesDialog(
    viewModel: ProfileViewModel,
    onDismiss: () -> Unit
) {
    val response by viewModel.pinnedArticles.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadPinnedArticles() }

    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    LaunchedEffect(response) {
        response?.pinned?.let { selected = it.toSet() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("置顶委托") },
        text = {
            val candidates = response?.candidates ?: emptyList()
            val max = response?.max ?: 0
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                items(candidates, key = { it.documentId }) { article ->
                    val isSelected = selected.contains(article.documentId)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selected = if (isSelected) {
                                    selected - article.documentId
                                } else if (selected.size < max) {
                                    selected + article.documentId
                                } else {
                                    selected
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = isSelected, onCheckedChange = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = article.title ?: "未命名委托",
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    viewModel.updatePinnedArticles(selected.toList())
                    onDismiss()
                }
            ) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
private fun BlockedAuthorsDialog(
    viewModel: ProfileViewModel,
    onDismiss: () -> Unit
) {
    val authors by viewModel.blockedAuthors.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadBlockedAuthors(refresh = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("黑名单") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                if (authors.isEmpty()) {
                    item { EmptyState(message = "黑名单为空", modifier = Modifier.fillMaxWidth()) }
                } else {
                    items(authors, key = { it.documentId ?: "" }) { author ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AvatarImage(author = author, modifier = Modifier.size(36.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(author.name ?: author.username ?: "用户")
                            }
                            TextButton(
                                onClick = { author.documentId?.let { viewModel.unblockAuthor(it) } }
                            ) { Text("解除") }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } }
    )
}

private fun formatTime(iso: String?): String {
    return try {
        iso?.let {
            Instant.parse(it)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(dateFormatter)
        } ?: ""
    } catch (_: Exception) {
        iso ?: ""
    }
}
