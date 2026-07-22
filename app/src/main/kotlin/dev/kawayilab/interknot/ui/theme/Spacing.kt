package dev.kawayilab.interknot.ui.theme

import androidx.compose.ui.unit.dp

/**
 * InterKnot spacing system — 4dp base unit grid.
 *
 * Every padding, margin, and gap in the app should reference
 * these tokens rather than hard-coded Dp values. This ensures
 * visual rhythm and makes global adjustments trivial.
 *
 *  xs   4dp  — icon-to-label gaps, tight internal spacing
 *  sm   8dp  — between elements inside a card
 *  md  12dp  — card content padding
 *  lg  16dp  — screen horizontal padding, section gaps
 *  xl  24dp  — large section separation
 *  xxl 32dp  — page-level spacing
 */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
}
