@file:OptIn(ExperimentalMaterial3Api::class)
package dev.kawayilab.interknot.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.kawayilab.interknot.model.Category
import dev.kawayilab.interknot.ui.components.common.EmptyState
import dev.kawayilab.interknot.ui.components.common.ErrorState
import dev.kawayilab.interknot.ui.components.common.PostCardSkeleton
import dev.kawayilab.interknot.ui.components.post.PostCard
import dev.kawayilab.interknot.ui.theme.Spacing

@Composable
fun HomeScreen(
    onPostClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val articles by viewModel.articles.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val hasMore by viewModel.hasMore.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val feed by viewModel.feed.collectAsStateWithLifecycle()
    val categorySlug by viewModel.categorySlug.collectAsStateWithLifecycle()
    val gridState = rememberLazyStaggeredGridState()
    var searchActive by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    LaunchedEffect(gridState, hasMore, isLoading) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .collect { lastVisible ->
                if (hasMore && !isLoading && lastVisible >= articles.size - 6) {
                    viewModel.loadMore()
                }
            }
    }

    val feedLabels = remember { listOf("关注" to "following", "推荐" to "recommend") }
    val selectedFeedIndex = feedLabels.indexOfFirst { it.second == feed }.coerceAtLeast(1)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            HomeTopBar(
                selectedTab = selectedFeedIndex,
                onTabSelected = { viewModel.setFeed(feedLabels[it].second) },
                searchActive = searchActive,
                query = query,
                onQueryChange = { query = it },
                onSearchActiveChange = { searchActive = it },
                onSearch = { viewModel.setQuery(query.trim()) }
            )
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CategoryTabs(
                categories = categories,
                selectedSlug = categorySlug,
                onSelect = { slug -> viewModel.setCategorySlug(slug) }
            )

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.pullRefresh() },
                modifier = Modifier.fillMaxSize()
            ) {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Adaptive(170.dp),
                    state = gridState,
                    contentPadding = PaddingValues(
                        horizontal = Spacing.md,
                        vertical = Spacing.sm
                    ),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    verticalItemSpacing = Spacing.md,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isLoading && articles.isEmpty()) {
                        items(6) {
                            PostCardSkeleton()
                        }
                    } else {
                        items(articles, key = { it.documentId }) { article ->
                            PostCard(
                                article = article,
                                onClick = { onPostClick(article.documentId) }
                            )
                        }
                    }

                    if (isLoading && articles.isNotEmpty()) {
                        item {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Spacing.md),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                        }
                    }
                }

                if (!isLoading && articles.isEmpty() && error == null) {
                    EmptyState(
                        message = if (query.isNotBlank()) "未找到相关委托" else "还没有委托，下拉刷新试试",
                        actionLabel = "刷新",
                        onAction = { viewModel.pullRefresh() }
                    )
                }

                if (error != null && !isRefreshing && articles.isEmpty()) {
                    ErrorState(
                        message = error!!,
                        onRetry = { viewModel.pullRefresh() }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    searchActive: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit,
    onSearch: () -> Unit
) {
    val tabs = listOf("关注", "推荐")

    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        title = {
            if (searchActive) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("搜索委托") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                    trailingIcon = {
                        IconButton(onClick = {
                            onSearchActiveChange(false)
                            onQueryChange("")
                            onSearch()
                        }) {
                            Icon(Icons.Filled.Close, contentDescription = "关闭")
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(0.7f),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    tabs.forEachIndexed { index, title ->
                        val selected = selectedTab == index
                        CustomTab(
                            title = title,
                            selected = selected,
                            onClick = { onTabSelected(index) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        actions = {
            if (!searchActive) {
                IconButton(onClick = { onSearchActiveChange(true) }) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "搜索",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
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
private fun CustomTab(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.sm),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
            if (selected) {
                Box(
                    modifier = Modifier
                        .padding(top = Spacing.xs)
                        .width(28.dp)
                        .height(3.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(1.5.dp)
                        )
                )
            } else {
                Spacer(modifier = Modifier.height(7.dp))
            }
        }
    }
}

@Composable
private fun CategoryTabs(
    categories: List<Category>,
    selectedSlug: String?,
    onSelect: (String?) -> Unit
) {
    val chips = remember(categories) {
        listOf(Category(name = "全部", slug = null)) +
            categories.filter { !it.adminOnly && !it.name.isNullOrBlank() && !it.slug.isNullOrBlank() }
    }
    val selectedIndex = chips.indexOfFirst { it.slug == selectedSlug }.coerceAtLeast(0)

    androidx.compose.foundation.lazy.LazyRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(chips.size) { index ->
            val selected = index == selectedIndex
            FilterChip(
                selected = selected,
                onClick = { onSelect(chips[index].slug) },
                label = {
                    Text(
                        text = chips[index].name ?: "全部",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                shape = MaterialTheme.shapes.extraLarge,
                border = null,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}
