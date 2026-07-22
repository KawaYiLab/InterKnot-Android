package dev.kawayilab.interknot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = InterknotYellow,
    onPrimary = Background,
    primaryContainer = InterknotYellow,
    onPrimaryContainer = Background,
    secondary = AccentCyan,
    onSecondary = Background,
    secondaryContainer = Surface,
    onSecondaryContainer = TextPrimary,
    tertiary = InterknotYellowLight,
    onTertiary = Background,
    tertiaryContainer = InterknotYellowDark,
    onTertiaryContainer = Background,
    error = Error,
    onError = Background,
    errorContainer = Color(0xFF3A1C1C),
    onErrorContainer = Color(0xFFFFCCCB),
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = CardInner,
    onSurfaceVariant = TextSecondary,
    surfaceDim = Background,
    surfaceBright = Color(0xFF2A2A2A),
    surfaceContainerLowest = Background,
    surfaceContainerLow = Color(0xFF111111),
    surfaceContainer = Surface,
    surfaceContainerHigh = Color(0xFF1E1E1E),
    surfaceContainerHighest = CardInner,
    outline = Divider,
    outlineVariant = Border,
    inverseSurface = TextPrimary,
    inverseOnSurface = Background,
    inversePrimary = InterknotYellowDark
)

@Composable
fun InterknotTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
