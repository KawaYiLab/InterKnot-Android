package dev.kawayilab.interknot.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = InterknotYellow,
    onPrimary = Black,
    primaryContainer = InterknotYellow,
    onPrimaryContainer = Black,
    secondary = AccentCyan,
    onSecondary = Black,
    secondaryContainer = SurfaceDark,
    onSecondaryContainer = White,
    tertiary = InterknotYellowLight,
    onTertiary = Black,
    tertiaryContainer = InterknotYellowDark,
    onTertiaryContainer = Black,
    error = Error,
    onError = Black,
    errorContainer = Color(0xFF3A1C1C),
    onErrorContainer = Color(0xFFFFCCCB),
    background = Black,
    onBackground = White,
    surface = SurfaceDark,
    onSurface = White,
    surfaceVariant = SurfaceHighest,
    onSurfaceVariant = Silver,
    surfaceDim = Black,
    surfaceBright = Bright,
    surfaceContainerLowest = Black,
    surfaceContainerLow = Ink,
    surfaceContainer = SurfaceDark,
    surfaceContainerHigh = SurfaceHigh,
    surfaceContainerHighest = SurfaceHighest,
    outline = Divider,
    outlineVariant = Border,
    inverseSurface = White,
    inverseOnSurface = Black,
    inversePrimary = InterknotYellowDark
)

private val ExtendedColors = InterknotExtendedColors()

@Composable
fun InterknotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> DarkColorScheme // Interknot is dark-only; we ignore the system light request.
    }

    CompositionLocalProvider(LocalInterknotColors provides ExtendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = InterknotTypography,
            shapes = InterknotShapes,
            content = content
        )
    }
}
