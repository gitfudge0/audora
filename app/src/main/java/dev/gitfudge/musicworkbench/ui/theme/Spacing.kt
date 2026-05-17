package dev.gitfudge.musicworkbench.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing is varied for rhythm (group headers breathe, rows stay dense), not
 * a single uniform pad. Touch targets stay >= 48dp regardless of these.
 */
@Immutable
data class Spacing(
    val hairline: Dp = 1.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val section: Dp = 48.dp,
    val minTouchTarget: Dp = 48.dp,
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }
