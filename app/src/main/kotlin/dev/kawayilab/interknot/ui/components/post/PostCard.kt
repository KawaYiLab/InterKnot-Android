package dev.kawayilab.interknot.ui.components.post

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import dev.kawayilab.interknot.model.Article
import dev.kawayilab.interknot.model.Author
import dev.kawayilab.interknot.ui.components.common.ShimmerBox
import dev.kawayilab.interknot.ui.theme.InterknotTheme
import dev.kawayilab.interknot.ui.theme.LocalInterknotColors
import dev.kawayilab.interknot.ui.theme.Motion
import dev.kawayilab.interknot.ui.theme.Spacing

@Composable
fun PostCard(
    article: Article,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = Motion.pressSpec(),
        label = "postCardPress"
    )
    val extendedColors = LocalInterknotColors.current
    val titleColor = if (article.isRead) extendedColors.titleRead else extendedColors.titleUnread

    val imageUrl = article.coverUrl ?: article.coverImages.firstOrNull()?.url

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        interactionSource = interactionSource,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            pressedElevation = 2.dp,
            hoveredElevation = 2.dp
        ),
        modifier = modifier.scale(scale)
    ) {
        Column {
            if (imageUrl != null) {
                CoverSection(article = article, imageUrl = imageUrl)
            }

            Text(
                text = buildTitle(article),
                style = MaterialTheme.typography.titleSmall,
                color = titleColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(
                    start = Spacing.sm,
                    end = Spacing.sm,
                    top = if (imageUrl != null) Spacing.sm else Spacing.md
                )
            )

            PostMeta(article = article)
        }
    }
}

@Composable
private fun CoverSection(article: Article, imageUrl: String) {
    val aspectRatio = if (article.coverWidth != null && article.coverHeight != null && article.coverHeight > 0) {
        article.coverWidth.toFloat() / article.coverHeight.toFloat()
    } else {
        0.75f
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        SubcomposeAsyncImage(
            model = imageUrl,
            contentDescription = article.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        ) {
            when (val state = painter.state) {
                is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
                else -> CoverShimmer()
            }
        }

        if (article.coverNsfwStatus != null && article.coverNsfwStatus != "safe" && article.coverNsfwStatus != "approved") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (article.coverNsfwStatus == "error") "图片审核中" else "敏感内容",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        ViewsOverlay(views = article.views, modifier = Modifier.align(Alignment.TopStart))
    }
}

@Composable
private fun ViewsOverlay(views: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .padding(Spacing.sm)
            .background(Color.Black.copy(alpha = 0.45f), shape = MaterialTheme.shapes.extraSmall)
            .padding(horizontal = Spacing.xs, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Visibility,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = formatCount(views),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CoverShimmer() {
    ShimmerBox(modifier = Modifier.fillMaxSize())
}

@Composable
private fun PostMeta(article: Article) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) {
            val avatarUrl = article.author?.avatarUrl
            if (avatarUrl != null) {
                SubcomposeAsyncImage(
                    model = avatarUrl,
                    contentDescription = article.author?.name ?: "默认头像",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (val state = painter.state) {
                        is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
                        else -> DefaultAvatarIcon()
                    }
                }
            } else {
                DefaultAvatarIcon()
            }
        }

        Text(
            text = article.author?.name ?: if (article.isAnonymous) "匿名" else "未知",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        if (article.likesCount > 0) {
            LikeChip(count = article.likesCount)
        }
    }
}

@Composable
private fun DefaultAvatarIcon() {
    Icon(
        imageVector = Icons.Filled.Person,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.size(14.dp)
    )
}

@Composable
private fun LikeChip(count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        Icon(
            imageVector = Icons.Outlined.FavoriteBorder,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = formatCount(count),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun buildTitle(article: Article): String {
    val category = article.category?.name?.takeIf { it.isNotBlank() }
    return if (category != null) "[$category] ${article.title}" else article.title
}

private fun formatCount(count: Int): String {
    return when {
        count >= 10_000 -> "${count / 10_000}w"
        count >= 1_000 -> "${count / 1_000}k"
        else -> count.toString()
    }
}

@Preview
@Composable
private fun PostCardPreviewDark() {
    InterknotTheme(darkTheme = true) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PostCard(
                article = Article(
                    documentId = "1",
                    title = "测试委托标题，最多显示两行",
                    coverUrl = null,
                    coverWidth = 1080,
                    coverHeight = 1440,
                    views = 1234,
                    likesCount = 88,
                    author = Author(name = "测试用户")
                ),
                onClick = {}
            )
            PostCard(
                article = Article(
                    documentId = "2",
                    title = "纯文字委托标题",
                    coverUrl = null,
                    views = 567,
                    likesCount = 12,
                    author = Author(name = "另一个用户")
                ),
                onClick = {}
            )
            PostCard(
                article = Article(
                    documentId = "3",
                    title = "已读的委托标题",
                    coverUrl = null,
                    coverWidth = 1080,
                    coverHeight = 720,
                    views = 890,
                    likesCount = 3,
                    isRead = true,
                    author = Author(name = "作者")
                ),
                onClick = {}
            )
        }
    }
}

@Preview
@Composable
private fun PostCardPreviewLight() {
    InterknotTheme(darkTheme = false) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PostCard(
                article = Article(
                    documentId = "1",
                    title = "测试委托标题，最多显示两行",
                    coverUrl = null,
                    coverWidth = 1080,
                    coverHeight = 1440,
                    views = 1234,
                    likesCount = 88,
                    author = Author(name = "测试用户")
                ),
                onClick = {}
            )
            PostCard(
                article = Article(
                    documentId = "2",
                    title = "纯文字委托标题",
                    coverUrl = null,
                    views = 567,
                    likesCount = 12,
                    author = Author(name = "另一个用户")
                ),
                onClick = {}
            )
        }
    }
}
