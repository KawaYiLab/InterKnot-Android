package dev.kawayilab.interknot.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = InterknotYellow,
    onPrimary = InterknotBlack,
    primaryContainer = InterknotYellow,
    onPrimaryContainer = InterknotBlack,
    secondary = InterknotPurple,
    onSecondary = InterknotWhite,
    background = InterknotWhite,
    onBackground = InterknotBlack,
    surface = InterknotWhite,
    onSurface = InterknotBlack,
)

private val DarkColorScheme = darkColorScheme(
    primary = InterknotYellow,
    onPrimary = InterknotBlack,
    primaryContainer = InterknotYellowDark,
    onPrimaryContainer = InterknotBlack,
    secondary = InterknotPurple,
    onSecondary = InterknotWhite,
    background = InterknotBlack,
    onBackground = InterknotWhite,
    surface = InterknotBlack,
    onSurface = InterknotWhite,
)

@Composable
fun InterknotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
