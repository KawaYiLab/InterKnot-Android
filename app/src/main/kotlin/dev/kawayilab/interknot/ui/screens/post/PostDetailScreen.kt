@file:OptIn(ExperimentalMaterial3Api::class)
package dev.kawayilab.interknot.ui.screens.post

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import dev.kawayilab.interknot.model.Article
import dev.kawayilab.interknot.ui.theme.Background
import dev.kawayilab.interknot.ui.theme.Border
import dev.kawayilab.interknot.ui.theme.CardInner
import dev.kawayilab.interknot.ui.theme.HeaderBorder
import dev.kawayilab.interknot.ui.theme.InterknotYellow
import dev.kawayilab.interknot.ui.theme.TextMuted
import dev.kawayilab.interknot.ui.theme.TextPrimary
import dev.kawayilab.interknot.ui.theme.TextSecondary

@Composable
fun PostDetailScreen(
    postId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArticleDetailViewModel = hiltViewModel()
) {
    val article by viewModel.article.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    LaunchedEffect(postId) {
        viewModel.load(postId)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Background,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            article?.let { PostDetailTopBar(it, onNavigateBack) }
                ?: TopAppBar(
                    title = { Text("帖子详情") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
                )
        },
        bottomBar = { article?.let { ArticleActionsBar(it) } }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading && article == null -> {
                    CircularProgressIndicator(
                        color = InterknotYellow,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                error != null && article == null -> {
                    Text(
                        text = error ?: "加载失败",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                article != null -> {
                    ArticleDetailContent(article = article!!)
                }
            }
        }
    }
}

@Composable
private fun PostDetailTopBar(
    article: Article,
    onBack: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CardInner)
                        .padding(3.dp)
                        .clip(CircleShape)
                        .background(Border)
                ) {
                    AsyncImage(
                        model = article.author?.avatarUrl,
                        contentDescription = article.author?.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = article.author?.name ?: if (article.isAnonymous) "匿名" else "未知",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Lv.${article.author?.level ?: 1}",
                            color = InterknotYellow,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formatTime(article.publishedAt ?: article.createdAt),
                            color = TextMuted,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = TextPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Background,
            navigationIconContentColor = TextPrimary,
            titleContentColor = TextPrimary,
            actionIconContentColor = TextPrimary
        )
    )
}

@Composable
private fun ArticleDetailContent(
    article: Article,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CoverPager(article = article)
        }

        item {
            val category = article.category?.name?.takeIf { it.isNotBlank() }
            Text(
                text = if (category != null) "[ $category ] ${article.title}" else article.title,
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
        }

        item {
            Text(
                text = article.text ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun CoverPager(article: Article) {
    val images = remember(article) {
        article.coverImages.map { it.url }.ifEmpty {
            article.coverUrl?.let { listOf(it) } ?: emptyList()
        }
    }

    if (images.isEmpty()) return

    if (images.size == 1) {
        CoverImage(images[0], article.title)
    } else {
        val pagerState = rememberPagerState(pageCount = { images.size })
        Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                CoverImage(images[page], article.title)
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(images.size) { index ->
                    val isSelected = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (isSelected) 18.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) InterknotYellow else Color.White.copy(alpha = 0.4f))
                    )
                }
            }
        }
    }
}

@Composable
private fun CoverImage(url: String?, contentDescription: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(HeaderBorder)
            .padding(4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Background)
    ) {
        AsyncImage(
            model = url,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun ArticleActionsBar(article: Article) {
    val navPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    var comment by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .padding(bottom = navPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TextField(
                value = comment,
                onValueChange = { comment = it },
                modifier = Modifier.weight(1f).height(42.dp),
                singleLine = true,
                shape = RoundedCornerShape(999.dp),
                placeholder = {
                    Text(
                        "说点什么...",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Background,
                    unfocusedContainerColor = Background,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedPlaceholderColor = TextSecondary,
                    unfocusedPlaceholderColor = TextSecondary,
                    cursorColor = InterknotYellow
                )
            )

            ActionIconButton(
                icon = Icons.Default.ThumbUp,
                count = article.likesCount,
                isActive = article.liked,
                onClick = {}
            )
            ActionIconButton(
                icon = Icons.Default.Star,
                count = article.dennyCount,
                isActive = article.hasGivenDenny,
                tint = InterknotYellow,
                onClick = {}
            )
            ActionIconButton(
                icon = Icons.Default.FavoriteBorder,
                count = article.favoritesCount,
                isActive = article.favorited,
                onClick = {}
            )
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "更多",
                    tint = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun ActionIconButton(
    icon: ImageVector,
    count: Int,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    tint: Color = InterknotYellow,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) tint else TextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
        if (count > 0) {
            Text(
                text = formatCount(count),
                color = if (isActive) tint else TextSecondary,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 10_000 -> "${count / 10_000}w"
        count >= 1_000 -> "${count / 1_000}k"
        else -> count.toString()
    }
}

private fun formatTime(iso: String?): String {
    return iso?.take(10) ?: ""
}
