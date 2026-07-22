package dev.kawayilab.interknot.ui.components.common

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.kawayilab.interknot.ui.theme.InterknotTheme
import dev.kawayilab.interknot.ui.theme.LocalInterknotColors
import dev.kawayilab.interknot.ui.theme.Spacing

/**
 * A shimmering placeholder box used in skeleton loading states.
 * Uses an animated linear gradient sweep across the surface.
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    aspectRatio: Float? = null
) {
    val extendedColors = LocalInterknotColors.current
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerProgress"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            extendedColors.shimmerBase,
            extendedColors.shimmerHighlight,
            extendedColors.shimmerBase
        ),
        start = androidx.compose.ui.geometry.Offset(progress * 600f - 300f, 0f),
        end = androidx.compose.ui.geometry.Offset(progress * 600f, 600f)
    )

    val finalModifier = if (aspectRatio != null) {
        modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .clip(MaterialTheme.shapes.medium)
            .background(brush)
    } else {
        modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(brush)
    }
    Box(modifier = finalModifier)
}

/**
 * Skeleton placeholder matching the PostCard layout.
 * Shows a shimmer cover, title bars, and meta row.
 */
@Composable
fun PostCardSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clip(MaterialTheme.shapes.medium)
    ) {
        // Cover shimmer (3:4 ratio like a typical post)
        ShimmerBox(aspectRatio = 0.75f)

        // Title bars
        Column(
            modifier = Modifier.padding(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.9f).height(14.dp))
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.6f).height(14.dp))
        }

        // Meta row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.sm),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            ShimmerBox(modifier = Modifier.size(20.dp).clip(CircleShape))
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.3f).height(10.dp))
            Spacer(modifier = Modifier.weight(1f))
            ShimmerBox(modifier = Modifier.size(36.dp))
        }
    }
}

@Preview
@Composable
private fun PostCardSkeletonPreviewDark() {
    InterknotTheme(darkTheme = true) {
        PostCardSkeleton()
    }
}

@Preview
@Composable
private fun PostCardSkeletonPreviewLight() {
    InterknotTheme(darkTheme = false) {
        PostCardSkeleton()
    }
}
