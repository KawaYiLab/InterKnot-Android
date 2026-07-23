package dev.kawayilab.interknot.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun InterknotImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    nsfwStatus: String? = null,
    placeholderColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant,
    errorColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.errorContainer
) {
    val context = LocalContext.current
    val isNsfw = nsfwStatus != null && nsfwStatus != "approved" && nsfwStatus.isNotBlank()
    val blurModifier = if (isNsfw) Modifier.blur(24.dp) else Modifier

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(model)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier
            .background(placeholderColor)
            .then(blurModifier),
        placeholder = ColorPainter(placeholderColor),
        error = ColorPainter(errorColor),
        fallback = ColorPainter(placeholderColor),
        contentScale = contentScale
    )
}
