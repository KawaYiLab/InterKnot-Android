@file:OptIn(ExperimentalMaterial3Api::class)
package dev.kawayilab.interknot.ui.screens.post

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import dev.kawayilab.interknot.model.Article
import dev.kawayilab.interknot.model.Author
import dev.kawayilab.interknot.model.Comment
import dev.kawayilab.interknot.presentation.components.MarkdownText
import dev.kawayilab.interknot.ui.components.common.ErrorState
import dev.kawayilab.interknot.ui.theme.LocalInterknotColors
import dev.kawayilab.interknot.ui.theme.Motion
import dev.kawayilab.interknot.ui.theme.Spacing
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun PostDetailScreen(
    postId: String,
    onNavigateBack: () -> Unit,
    onAuthorClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ArticleDetailViewModel = hiltViewModel()
) {
    val article by viewModel.article.collectAsStateWithLifecycle()
    val comments by viewModel.comments.collectAsStateWithLifecycle()
    val pinnedComment by viewModel.pinnedComment.collectAsStateWithLifecycle()
    val replyTo by viewModel.replyTo.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isLoadingComments by viewModel.isLoadingComments.collectAsStateWithLifecycle()
    val hasMoreComments by viewModel.hasMoreComments.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    var comment by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var reportOpen by remember { mutableStateOf(false) }
    var blockOpen by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }

    LaunchedEffect(postId) {
        viewModel.load(postId)
    }

    LaunchedEffect(error) {
        error?.let { message ->
            scope.launch { snackbarHostState.showSnackbar(message) }
            viewModel.dismissError()
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .distinctUntilChanged()
            .collect { lastVisible ->
                if (hasMoreComments && !isLoadingComments && lastVisible >= comments.size - 2) {
                    viewModel.loadMoreComments()
                }
            }
    }

    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.statusBars,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            article?.let {
                PostDetailTopBar(
                    article = it,
                    onBack = onNavigateBack,
                    onAuthorClick = { authorDocumentId ->
                        if (authorDocumentId.isNotBlank()) onAuthorClick(authorDocumentId)
                    },
                    onFollow = { viewModel.toggleFollowAuthor() },
                    onReport = { reportOpen = true },
                    onBlock = { blockOpen = true },
                    menuOpen = menuOpen,
                    onMenuOpenChange = { menuOpen = it }
                )
            } ?: TopAppBar(
                title = { Text("帖子详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            ArticleActionsBar(
                comment = comment,
                onCommentChange = { comment = it },
                onSend = {
                    viewModel.sendComment(comment) { result ->
                        result.onSuccess { comment = "" }
                    }
                },
                replyTo = replyTo,
                onCancelReply = { viewModel.setReplyTo(null) },
                article = article,
                onLike = { viewModel.toggleLikeArticle() },
                onFavorite = { viewModel.toggleFavoriteArticle() },
                onTriple = { viewModel.tripleArticle() },
                onGiveDenny = { viewModel.giveDenny { } }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Spacing.lg,
                    top = Spacing.lg,
                    end = Spacing.lg,
                    bottom = navBottom + Spacing.lg
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg)
            ) {
                when {
                    isLoading && article == null -> {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(top = 120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    error != null && article == null -> {
                        item {
                            ErrorState(
                                message = error ?: "加载失败",
                                onRetry = { viewModel.load(postId, forceRefresh = true) }
                            )
                        }
                    }

                    article != null -> {
                        item { ArticleDetailContent(article = article!!) }

                        pinnedComment?.let { pinned ->
                            item { PinnedBadge() }
                            item {
                                CommentItem(
                                    comment = pinned,
                                    onLike = { viewModel.toggleLikeComment(pinned) {} },
                                    onReply = { viewModel.setReplyTo(pinned) }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(Spacing.sm)) }
                        }

                        if (comments.isNotEmpty() || isLoadingComments || pinnedComment == null) {
                            item {
                                Text(
                                    text = "评论 ${article!!.commentsCount}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(top = Spacing.sm)
                                )
                            }
                        }

                        items(comments, key = { it.documentId }) { commentItem ->
                            CommentItem(
                                comment = commentItem,
                                onLike = { viewModel.toggleLikeComment(commentItem) {} },
                                onReply = { viewModel.setReplyTo(commentItem) }
                            )
                        }

                        if (isLoadingComments) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(Spacing.md),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (reportOpen) {
        ReportDialog(
            onDismiss = { reportOpen = false },
            onConfirm = { reason, detail ->
                viewModel.reportArticle(reason, detail) { result ->
                    result.onSuccess { scope.launch { snackbarHostState.showSnackbar("举报已提交") } }
                    reportOpen = false
                }
            }
        )
    }

    if (blockOpen) {
        val authorName = article?.author?.name ?: "该作者"
        val isBlocked = article?.author?.isBlockedByMe == true
        AlertDialog(
            onDismissRequest = { blockOpen = false },
            title = { Text(if (isBlocked) "取消拉黑" else "拉黑作者") },
            text = { Text(if (isBlocked) "确定取消拉黑 $authorName 吗？" else "确定拉黑 $authorName 吗？拉黑后将双向取消关注并隐藏其内容。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.blockAuthor { result ->
                            result.onSuccess {
                                scope.launch {
                                    snackbarHostState.showSnackbar(if (isBlocked) "已取消拉黑" else "已拉黑")
                                }
                            }
                            blockOpen = false
                        }
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { blockOpen = false }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun PostDetailTopBar(
    article: Article,
    onBack: () -> Unit,
    onAuthorClick: (String) -> Unit,
    onFollow: () -> Unit,
    onReport: () -> Unit,
    onBlock: () -> Unit,
    menuOpen: Boolean,
    onMenuOpenChange: (Boolean) -> Unit
) {
    TopAppBar(
        title = { AuthorHeader(article = article, onAuthorClick = onAuthorClick, onFollow = onFollow) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回"
                )
            }
        },
        actions = {
            Box {
                IconButton(onClick = { onMenuOpenChange(true) }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "更多",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { onMenuOpenChange(false) }
                ) {
                    DropdownMenuItem(
                        text = { Text("举报") },
                        leadingIcon = { Icon(Icons.Outlined.Flag, contentDescription = null) },
                        onClick = {
                            onMenuOpenChange(false)
                            onReport()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (article.author?.isBlockedByMe == true) "取消拉黑" else "拉黑") },
                        leadingIcon = { Icon(Icons.Outlined.Block, contentDescription = null) },
                        onClick = {
                            onMenuOpenChange(false)
                            onBlock()
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
private fun AuthorHeader(
    article: Article,
    onAuthorClick: (String) -> Unit,
    onFollow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val author = article.author
    val isOwner = article.isOwner
    val isBlocked = author?.isBlockedByMe == true
    val isFollowing = author?.isFollowing == true

    Row(
        modifier = modifier.clickable(
            enabled = author?.documentId != null,
            onClick = { author?.documentId?.let(onAuthorClick) }
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            val avatarUrl = author?.avatarUrl
            if (avatarUrl != null) {
                SubcomposeAsyncImage(
                    model = avatarUrl,
                    contentDescription = author?.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (val state = painter.state) {
                        is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
                        else -> DefaultAvatarIcon(20)
                    }
                }
            } else {
                DefaultAvatarIcon(20)
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = author?.name ?: if (article.isAnonymous) "匿名" else "未知",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(
                        text = "Lv.${author?.level ?: 1}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = Spacing.xs, vertical = 1.dp)
                    )
                }
                Text(
                    text = formatTime(article.publishedAt ?: article.createdAt),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        if (!isOwner && author?.documentId != null) {
            if (isBlocked) {
                OutlinedButton(
                    onClick = onFollow,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = Spacing.md),
                    enabled = false
                ) {
                    Text("已拉黑", style = MaterialTheme.typography.labelMedium)
                }
            } else if (isFollowing) {
                OutlinedButton(
                    onClick = onFollow,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = Spacing.md)
                ) {
                    Text("已关注", style = MaterialTheme.typography.labelMedium)
                }
            } else {
                Button(
                    onClick = onFollow,
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = Spacing.md)
                ) {
                    Text("关注", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun ArticleDetailContent(article: Article) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        CoverPager(article = article)

        val category = article.category?.name?.takeIf { it.isNotBlank() }
        Text(
            text = if (category != null) "[$category] ${article.title}" else article.title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )

        MarkdownText(
            text = article.text ?: "",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            StatText("${formatCount(article.views)} 浏览")
            StatText("${formatCount(article.likesCount)} 点赞")
            StatText("${formatCount(article.commentsCount)} 评论")
        }
    }
}

@Composable
private fun StatText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun CoverPager(article: Article) {
    val images = remember(article) {
        article.coverImages.map { it.url }.ifEmpty {
            article.coverUrl?.let { listOf(it) } ?: emptyList()
        }
    }
    if (images.isEmpty()) return

    val firstImage = article.coverImages.firstOrNull()
    val aspectRatio = if (firstImage?.width != null && firstImage.height != null && firstImage.height > 0) {
        firstImage.width.toFloat() / firstImage.height.toFloat()
    } else if (article.coverWidth != null && article.coverHeight != null && article.coverHeight > 0) {
        article.coverWidth.toFloat() / article.coverHeight.toFloat()
    } else {
        0.75f
    }

    if (images.size == 1) {
        CoverImage(images[0], article.title, aspectRatio, article.coverNsfwStatus)
    } else {
        val pagerState = rememberPagerState(pageCount = { images.size })
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                CoverImage(images[page], article.title, aspectRatio, article.coverNsfwStatus)
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(Spacing.sm),
                shape = MaterialTheme.shapes.extraSmall,
                color = Color.Black.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${images.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun CoverImage(url: String?, contentDescription: String?, aspectRatio: Float, nsfwStatus: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        AsyncImage(
            model = url,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        if (nsfwStatus != null && nsfwStatus != "safe") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (nsfwStatus == "error") "图片审核中" else "敏感内容",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ArticleActionsBar(
    comment: String,
    onCommentChange: (String) -> Unit,
    onSend: () -> Unit,
    replyTo: Comment?,
    onCancelReply: () -> Unit,
    article: Article?,
    onLike: () -> Unit,
    onFavorite: () -> Unit,
    onTriple: () -> Unit,
    onGiveDenny: () -> Unit
) {
    BottomAppBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        windowInsets = WindowInsets.navigationBars
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (replyTo != null) {
                Surface(
                    modifier = Modifier.padding(PaddingValues(start = Spacing.sm, end = Spacing.sm, bottom = Spacing.xs)),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 2.dp)
                    ) {
                        Text(
                            text = "回复 @${replyTo.author?.name ?: "匿名"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onCancelReply,
                            modifier = Modifier.size(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "取消回复",
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            TextField(
                value = comment,
                onValueChange = onCommentChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge,
                placeholder = {
                    Text(
                        text = if (replyTo != null) "回复一下..." else "评论一下...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                trailingIcon = {
                    IconButton(onClick = onSend) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "发送",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            modifier = Modifier.padding(start = Spacing.sm)
        ) {
            ActionButton(
                icon = if (article?.liked == true) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                label = "点赞",
                count = article?.likesCount,
                onClick = onLike,
                active = article?.liked == true
            )
            ActionButton(
                icon = if (article?.favorited == true) Icons.Filled.Star else Icons.Outlined.StarBorder,
                label = "收藏",
                count = article?.favoritesCount,
                onClick = onFavorite,
                active = article?.favorited == true
            )
            IconButton(onClick = onTriple) {
                Icon(
                    imageVector = Icons.Outlined.ThumbUp,
                    contentDescription = "三连",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
            Surface(
                modifier = Modifier.clip(MaterialTheme.shapes.extraSmall),
                color = if (article?.hasGivenDenny == true) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
            ) {
                IconButton(onClick = onGiveDenny) {
                    Text(
                        text = "丁尼 ${article?.dennyCount ?: 0}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (article?.hasGivenDenny == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    count: Int?,
    onClick: () -> Unit,
    active: Boolean
) {
    IconButton(onClick = onClick) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            if (count != null && count > 0) {
                Text(
                    text = formatCount(count),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PinnedBadge() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        Icon(
            imageVector = Icons.Filled.PushPin,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = "置顶",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CommentItem(
    comment: Comment,
    onLike: () -> Unit,
    onReply: () -> Unit,
    modifier: Modifier = Modifier
) {
    val likeScale by animateFloatAsState(
        targetValue = if (comment.liked) 1.15f else 1f,
        animationSpec = Motion.bounceSpec(),
        label = "commentLike"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                val avatarUrl = comment.author?.avatarUrl
                if (avatarUrl != null) {
                    SubcomposeAsyncImage(
                        model = avatarUrl,
                        contentDescription = comment.author?.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        when (val state = painter.state) {
                            is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
                            else -> DefaultAvatarIcon(16)
                        }
                    }
                } else {
                    DefaultAvatarIcon(16)
                }
            }
            Spacer(modifier = Modifier.width(Spacing.sm))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Text(
                        text = comment.author?.name ?: "匿名",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold
                    )
                    comment.floor?.let { floor ->
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ) {
                            Text(
                                text = "#$floor",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = Spacing.xs, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = formatTime(comment.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                IconButton(onClick = onReply, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Reply,
                        contentDescription = "回复",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onLike, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (comment.liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "点赞",
                        tint = if (comment.liked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp).scale(likeScale)
                    )
                }
                if (comment.likesCount > 0) {
                    Text(
                        text = formatCount(comment.likesCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Text(
            text = comment.content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 40.dp)
        )

        if (comment.replies.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .padding(start = 40.dp)
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                Column(
                    modifier = Modifier
                        .padding(start = Spacing.sm)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    comment.replies.forEach { reply ->
                        ReplyItem(reply = reply)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReplyItem(reply: Comment, modifier: Modifier = Modifier) {
    val likeScale by animateFloatAsState(
        targetValue = if (reply.liked) 1.15f else 1f,
        animationSpec = Motion.bounceSpec(),
        label = "replyLike"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = reply.author?.name ?: "匿名",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(Spacing.sm))
            Text(
                text = formatTime(reply.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Icon(
                    imageVector = if (reply.liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "点赞",
                    tint = if (reply.liked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp).scale(likeScale)
                )
                if (reply.likesCount > 0) {
                    Text(
                        text = formatCount(reply.likesCount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Text(
            text = reply.content,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun ReportDialog(
    onDismiss: () -> Unit,
    onConfirm: (reason: String, detail: String?) -> Unit
) {
    val reasons = remember {
        listOf(
            "spam" to "垃圾内容",
            "abuse" to "辱骂攻击",
            "porn" to "色情低俗",
            "illegal" to "违法违规",
            "privacy" to "侵犯隐私",
            "misinfo" to "虚假信息",
            "plagiarism" to "抄袭盗用",
            "other" to "其他"
        )
    }
    var selected by remember { mutableStateOf(reasons[0].first) }
    var detail by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("举报帖子") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .padding(vertical = Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                reasons.forEach { (key, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selected = key }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selected == key,
                            onClick = { selected = key }
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = Spacing.sm)
                        )
                    }
                }
                if (selected == "other") {
                    OutlinedTextField(
                        value = detail,
                        onValueChange = { detail = it },
                        label = { Text("补充说明") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selected, detail.takeIf { it.isNotBlank() }) },
                enabled = selected.isNotBlank() && (selected != "other" || detail.isNotBlank())
            ) {
                Text("提交")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
private fun DefaultAvatarIcon(size: Int) {
    Icon(
        imageVector = Icons.Filled.Person,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.size(size.dp)
    )
}

private fun formatTime(iso: String?): String {
    return iso?.take(10) ?: ""
}

private fun formatCount(count: Int): String {
    return when {
        count >= 10_000 -> "${count / 10_000}w"
        count >= 1_000 -> "${count / 1_000}k"
        else -> count.toString()
    }
}
