package dev.kawayilab.interknot.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * InterKnot shape scale — slightly more generous than stock M3
 * to achieve an Apple-like continuous-corner feel.
 *
 *  extraSmall  6dp  — chips, snackbars
 *  small      10dp  — text fields, menus
 *  medium     14dp  — cards
 *  large      18dp  — bottom sheets, FABs
 *  extraLarge 28dp  — dialogs, full sheets (Apple sheet radius)
 */
val InterknotShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
