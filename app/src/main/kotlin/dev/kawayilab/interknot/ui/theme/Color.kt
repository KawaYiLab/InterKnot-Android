package dev.kawayilab.interknot.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Primitive palette — only used to seed the semantic tokens below.
internal val Black = Color(0xFF0A0A0A)
internal val Ink = Color(0xFF111111)
internal val SurfaceDark = Color(0xFF1A1A1A)
internal val SurfaceHigh = Color(0xFF1E1E1E)
internal val SurfaceHighest = Color(0xFF222222)
internal val Bright = Color(0xFF2A2A2A)

internal val White = Color(0xFFE8E8E8)
internal val Silver = Color(0xFF9A9A9A)
internal val Muted = Color(0xFF808080)

internal val InterknotYellow = Color(0xFFBFFF09)
internal val InterknotYellowLight = Color(0xFFFDFF42)
internal val InterknotYellowDark = Color(0xFFDFE200)
internal val AccentCyan = Color(0xFF00E5FF)

internal val Error = Color(0xFFFF4D4F)
internal val TitleUnread = Color(0xFF2196F3)
internal val TitleRead = Color(0xFF9E9E9E)
internal val Link = Color(0xFF6F9CFF)
internal val OnlineGreen = Color(0xFF4ADE80)
internal val KnockBadge = Color(0xFFFF3838)

internal val Divider = Color(0xFF3A3A3A)
internal val Border = Color(0xFF2D2D2D)
internal val HeaderBorder = Color(0xFF313132)

/**
 * App-specific colors that don't fit into the standard Material 3 [ColorScheme].
 *
 * Use via [LocalInterknotColors.current] inside [InterknotTheme].
 */
@Immutable
data class InterknotExtendedColors(
    val titleRead: Color = TitleRead,
    val titleUnread: Color = TitleUnread,
    val link: Color = Link,
    val online: Color = OnlineGreen,
    val knockBadge: Color = KnockBadge
)

val LocalInterknotColors = staticCompositionLocalOf { InterknotExtendedColors() }
