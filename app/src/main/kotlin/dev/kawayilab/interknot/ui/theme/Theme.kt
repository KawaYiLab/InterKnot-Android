package dev.kawayilab.interknot.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun InterknotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) ExtendedColorsDark else ExtendedColorsLight

    CompositionLocalProvider(LocalInterknotColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = InterknotTypography,
            shapes = InterknotShapes,
            content = content
        )
    }
}
