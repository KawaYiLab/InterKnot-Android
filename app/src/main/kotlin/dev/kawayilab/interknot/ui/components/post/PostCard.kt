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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
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
import dev.kawayilab.interknot.ui.theme.InterknotTheme

@Composable
fun PostCard(
    article: Article,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        label = "postCardPress"
    )

    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        interactionSource = interactionSource,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = modifier.scale(scale)
    ) {
        Column {
            CoverSection(article = article)
            PostMeta(article = article)
        }
    }
}

@Composable
private fun CoverSection(article: Article) {
    val aspectRatio = if (article.coverWidth != null && article.coverHeight != null && article.coverHeight > 0) {
        article.coverWidth.toFloat() / article.coverHeight.toFloat()
    } else {
        0.75f
    }

    val imageUrl = article.coverUrl ?: article.coverImages.firstOrNull()?.url

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        if (imageUrl != null) {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = article.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            ) {
                when (val state = painter.state) {
                    is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
                    else -> CoverPlaceholder()
                }
            }
        } else {
            CoverPlaceholder()
        }

        ViewChip(
            count = article.views,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
        )

        TitleOverlay(
            title = buildTitle(article),
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}

@Composable
private fun CoverPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Image,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(40.dp)
        )
    }
}

@Composable
private fun TitleOverlay(
    title: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 120.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                )
            )
            .padding(12.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ViewChip(
    count: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Visibility,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = formatCount(count),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PostMeta(article: Article) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
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
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.size(16.dp)
    )
}

@Composable
private fun LikeChip(count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.FavoriteBorder,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = formatCount(count),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
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
private fun PostCardPreview() {
    InterknotTheme {
        PostCard(
            article = Article(
                documentId = "1",
                title = "测试委托标题，最多显示两行",
                coverUrl = null,
                views = 1234,
                likesCount = 88,
                author = Author(name = "测试用户")
            ),
            onClick = {}
        )
    }
}
