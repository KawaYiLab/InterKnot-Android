package dev.kawayilab.interknot.ui.components.post

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.kawayilab.interknot.model.Article
import dev.kawayilab.interknot.ui.theme.Background
import dev.kawayilab.interknot.ui.theme.CardInner
import dev.kawayilab.interknot.ui.theme.CardOuter
import dev.kawayilab.interknot.ui.theme.CoverPlaceholder
import dev.kawayilab.interknot.ui.theme.Divider
import dev.kawayilab.interknot.ui.theme.TitleRead
import dev.kawayilab.interknot.ui.theme.TitleUnread

private val OuterShape = RoundedCornerShape(24.dp, 24.dp, 0.dp, 24.dp)
private val InnerShape = RoundedCornerShape(20.dp, 20.dp, 0.dp, 20.dp)
private val CoverShape = RoundedCornerShape(20.dp, 20.dp, 0.dp, 0.dp)

@Composable
fun PostCard(
    article: Article,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scaleValue = if (isPressed) 0.96f else 1f

    Box(
        modifier = modifier
            .scale(scaleValue)
            .clip(OuterShape)
            .background(CardOuter)
            .padding(4.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.TopStart
    ) {
        Column(
            modifier = Modifier
                .clip(InnerShape)
                .background(CardInner)
        ) {
            // Cover with views overlay
            val aspectRatio = if (article.coverWidth != null && article.coverHeight != null && article.coverHeight > 0) {
                article.coverWidth.toFloat() / article.coverHeight.toFloat()
            } else {
                1.576f
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CoverPlaceholder)
                    .clip(CoverShape)
            ) {
                AsyncImage(
                    model = article.coverUrl,
                    contentDescription = article.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(aspectRatio)
                        .background(CoverPlaceholder)
                )

                Row(
                    modifier = Modifier
                        .padding(horizontal = 11.dp, vertical = 10.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                        .align(Alignment.TopStart),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Visibility,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = formatCount(article.views),
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Author row overlapping cover
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .offset(y = (-28).dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(Background, CircleShape)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(CardInner)
                ) {
                    AsyncImage(
                        model = article.author?.avatarUrl,
                        contentDescription = article.author?.name ?: "默认头像",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = article.author?.name ?: if (article.isAnonymous) "匿名" else "未知",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Divider)
                    )
                }
            }

            // Title
            val titleColor = if (article.isRead) TitleRead else TitleUnread
            Text(
                text = buildTitle(article),
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = titleColor
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 12.dp)
            )
        }
    }
}

private fun buildTitle(article: Article): String {
    val category = article.category?.name?.takeIf { it.isNotBlank() }
    return if (category != null) "[ $category ] ${article.title}" else article.title
}

private fun formatCount(count: Int): String {
    return when {
        count >= 10_000 -> "${count / 10_000}w"
        count >= 1_000 -> "${count / 1_000}k"
        else -> count.toString()
    }
}
