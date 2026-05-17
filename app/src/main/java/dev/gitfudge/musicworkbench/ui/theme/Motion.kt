package dev.gitfudge.musicworkbench.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * DESIGN.md motion: 150-250ms, ease-out, state changes only. No bounce, no
 * elastic, no layout-property animation. Reduced-motion collapses to a fast
 * fade (see [Motion.reducedMotion]).
 */
@Immutable
data class Motion(
    val fast: Int = 150,
    val standard: Int = 200,
    val deliberate: Int = 250,
    val easeOut: Easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f),
    val emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f),
    val reducedMotion: Boolean = false,
) {
    fun <T> spec(durationMillis: Int = standard) =
        tween<T>(durationMillis = if (reducedMotion) fast else durationMillis, easing = easeOut)
}

val LocalMotion = staticCompositionLocalOf { Motion() }
