package dev.kawayilab.interknot.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ──────────────────────────────────────────────
// Primitive palette — seeds for the semantic tokens below.
// ──────────────────────────────────────────────

// Dark primitives (existing)
internal val Black = Color(0xFF0A0A0A)
internal val Ink = Color(0xFF111111)
internal val SurfaceDark = Color(0xFF1A1A1A)
internal val SurfaceHigh = Color(0xFF1E1E1E)
internal val SurfaceHighest = Color(0xFF222222)
internal val Bright = Color(0xFF2A2A2A)

internal val White = Color(0xFFE8E8E8)
internal val Silver = Color(0xFF9A9A9A)
internal val Muted = Color(0xFF808080)

// Light primitives (new)
internal val PureWhite = Color(0xFFFFFFFF)
internal val LightSurface = Color(0xFFF7F7F7)
internal val LightSurfaceLow = Color(0xFFFAFAFA)
internal val LightSurfaceHigh = Color(0xFFF0F0F0)
internal val LightSurfaceHighest = Color(0xFFEBEBEB)
internal val LightSurfaceDim = Color(0xFFE5E5E5)
internal val LightSurfaceBright = Color(0xFFFFFFFF)

internal val InkText = Color(0xFF1A1A1A)
internal val InkTextSecondary = Color(0xFF6B6B6B)
internal val LightOutline = Color(0xFFD0D0D0)
internal val LightOutlineVariant = Color(0xFFE8E8E8)

// Brand colors — shared across themes
internal val InterknotYellow = Color(0xFFBFFF09)
internal val InterknotYellowLight = Color(0xFFFDFF42)
internal val InterknotYellowDark = Color(0xFFDFE200)
internal val InterknotYellowToned = Color(0xFFA6D900) // light-mode primary, less harsh on white
internal val AccentCyan = Color(0xFF00E5FF)

// Semantic accent colors — per-theme variants
internal val Error = Color(0xFFFF4D4F)
internal val TitleUnreadDark = Color(0xFF2196F3)
internal val TitleUnreadLight = Color(0xFF007AFF) // Apple system blue
internal val TitleReadDark = Color(0xFF9E9E9E)
internal val TitleReadLight = Color(0xFF9E9E9E)
internal val LinkDark = Color(0xFF6F9CFF)
internal val LinkLight = Color(0xFF4A7AFF)
internal val OnlineGreenDark = Color(0xFF4ADE80)
internal val OnlineGreenLight = Color(0xFF34C759) // Apple system green
internal val KnockBadgeDark = Color(0xFFFF3838)
internal val KnockBadgeLight = Color(0xFFFF3B30) // Apple system red

internal val DividerDark = Color(0xFF3A3A3A)
internal val BorderDark = Color(0xFF2D2D2D)
internal val DividerLight = Color(0xFFE0E0E0)
internal val BorderLight = Color(0xFFEEEEEE)

// Shimmer colors
internal val ShimmerBaseDark = Color(0xFF2A2A2A)
internal val ShimmerHighlightDark = Color(0xFF3A3A3A)
internal val ShimmerBaseLight = Color(0xFFF0F0F0)
internal val ShimmerHighlightLight = Color(0xFFFAFAFA)

// Telegram-style message bubbles
internal val MessageIncomingDark = Color(0xFF1E2C3A)
internal val MessageOutgoingDark = Color(0xFF2B5278)
internal val MessageIncomingLight = Color(0xFFF2F2F2)
internal val MessageOutgoingLight = Color(0xFFD6EEFF)
internal val MessageTextDark = White
internal val MessageTextLight = InkText

// ──────────────────────────────────────────────
// Color schemes
// ──────────────────────────────────────────────

val LightColorScheme = lightColorScheme(
    primary = InterknotYellowToned,
    onPrimary = Black,
    primaryContainer = InterknotYellowToned,
    onPrimaryContainer = Black,
    secondary = AccentCyan,
    onSecondary = Black,
    secondaryContainer = LightSurfaceHigh,
    onSecondaryContainer = InkText,
    tertiary = InterknotYellowLight,
    onTertiary = Black,
    tertiaryContainer = InterknotYellowDark,
    onTertiaryContainer = Black,
    error = Error,
    onError = PureWhite,
    errorContainer = Color(0xFFFFE5E5),
    onErrorContainer = Color(0xFFB71C1C),
    background = PureWhite,
    onBackground = InkText,
    surface = LightSurface,
    onSurface = InkText,
    surfaceVariant = LightSurfaceHigh,
    onSurfaceVariant = InkTextSecondary,
    surfaceDim = LightSurfaceDim,
    surfaceBright = LightSurfaceBright,
    surfaceContainerLowest = PureWhite,
    surfaceContainerLow = LightSurfaceLow,
    surfaceContainer = LightSurface,
    surfaceContainerHigh = LightSurfaceHigh,
    surfaceContainerHighest = LightSurfaceHighest,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    inverseSurface = InkText,
    inverseOnSurface = PureWhite,
    inversePrimary = InterknotYellow
)

val DarkColorScheme = darkColorScheme(
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
    outline = DividerDark,
    outlineVariant = BorderDark,
    inverseSurface = White,
    inverseOnSurface = Black,
    inversePrimary = InterknotYellowDark
)

// ──────────────────────────────────────────────
// Extended colors — app-specific tokens that don't
// fit into the standard Material 3 ColorScheme.
//
// Use via [LocalInterknotColors.current] inside [InterknotTheme].
// ──────────────────────────────────────────────

@Immutable
data class InterknotExtendedColors(
    val titleRead: Color,
    val titleUnread: Color,
    val link: Color,
    val online: Color,
    val knockBadge: Color,
    val shimmerBase: Color,
    val shimmerHighlight: Color,
    val messageIncoming: Color,
    val messageOutgoing: Color,
    val messageText: Color
)

/** Dark-mode extended colors (also used as the CompositionLocal default). */
val ExtendedColorsDark = InterknotExtendedColors(
    titleRead = TitleReadDark,
    titleUnread = TitleUnreadDark,
    link = LinkDark,
    online = OnlineGreenDark,
    knockBadge = KnockBadgeDark,
    shimmerBase = ShimmerBaseDark,
    shimmerHighlight = ShimmerHighlightDark,
    messageIncoming = MessageIncomingDark,
    messageOutgoing = MessageOutgoingDark,
    messageText = MessageTextDark
)

/** Light-mode extended colors. */
val ExtendedColorsLight = InterknotExtendedColors(
    titleRead = TitleReadLight,
    titleUnread = TitleUnreadLight,
    link = LinkLight,
    online = OnlineGreenLight,
    knockBadge = KnockBadgeLight,
    shimmerBase = ShimmerBaseLight,
    shimmerHighlight = ShimmerHighlightLight,
    messageIncoming = MessageIncomingLight,
    messageOutgoing = MessageOutgoingLight,
    messageText = MessageTextLight
)

val LocalInterknotColors = staticCompositionLocalOf { ExtendedColorsDark }
