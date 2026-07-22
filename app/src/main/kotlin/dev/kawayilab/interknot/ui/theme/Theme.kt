package dev.kawayilab.interknot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = InterknotYellow,
    onPrimary = Background,
    primaryContainer = InterknotYellowDark,
    onPrimaryContainer = Background,
    secondary = AccentCyan,
    onSecondary = Background,
    secondaryContainer = Surface,
    onSecondaryContainer = TextPrimary,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = CardInner,
    onSurfaceVariant = TextSecondary,
    error = Error,
    onError = TextPrimary,
    outline = Divider,
    surfaceTint = InterknotYellow
)

@Composable
fun InterknotTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
