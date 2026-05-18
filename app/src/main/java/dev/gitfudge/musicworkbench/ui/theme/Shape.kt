package dev.gitfudge.musicworkbench.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * Radii from mw-tokens.css: xs 4 (dense chips) · sm 8 (inputs, icon buttons,
 * inline thumbs) · md 12 (default card) · lg 16 (sheet) · xl 24 (hero / sheet
 * top) · pill (chips, capsule buttons). The biggest cover on a screen is never
 * rounded — that's enforced at the call site, not here.
 */
@Immutable
data class AppShapeScale(
    val xs: RoundedCornerShape = RoundedCornerShape(4.dp),
    val sm: RoundedCornerShape = RoundedCornerShape(8.dp),
    val md: RoundedCornerShape = RoundedCornerShape(12.dp),
    val lg: RoundedCornerShape = RoundedCornerShape(16.dp),
    val xl: RoundedCornerShape = RoundedCornerShape(24.dp),
    val pill: RoundedCornerShape = RoundedCornerShape(percent = 50),
)

val LocalShapeScale = staticCompositionLocalOf { AppShapeScale() }

internal val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)
