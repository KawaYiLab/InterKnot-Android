package dev.kawayilab.interknot.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

/**
 * InterKnot motion system — physics-based spring animations
 * inspired by Apple UIKit's default spring characteristics.
 *
 * Use these specs instead of tween/linear animations for any
 * interactive feedback (press, expand, toggle, etc.).
 */
object Motion {

    /** Card / button press feedback — fast, slight overshoot. */
    fun <T : Any> pressSpec(): SpringSpec<T> = spring(
        dampingRatio = 0.65f,
        stiffness = Spring.StiffnessMedium
    )

    /** Expand / collapse — smooth, gentle settling. */
    fun <T : Any> expandSpec(): SpringSpec<T> = spring(
        dampingRatio = 0.8f,
        stiffness = Spring.StiffnessLow
    )

    /** Toggle / snap — quick, no overshoot. */
    fun <T : Any> snapSpec(): SpringSpec<T> = spring(
        dampingRatio = 1.0f,
        stiffness = Spring.StiffnessMedium
    )

    /** Bounce-in — playful overshoot for badges, pop-ups. */
    fun <T : Any> bounceSpec(): SpringSpec<T> = spring(
        dampingRatio = 0.55f,
        stiffness = Spring.StiffnessMediumLow
    )

    // ── Page transition durations (non-spring, for NavTransitions) ──
    const val PAGE_ENTER_MS = 300
    const val PAGE_EXIT_MS = 200
    const val TAB_SWITCH_MS = 200
}
