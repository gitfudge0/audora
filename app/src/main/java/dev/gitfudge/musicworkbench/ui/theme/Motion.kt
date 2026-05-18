package dev.gitfudge.musicworkbench.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * mw-tokens.css motion: 120ms state change (chip/button), 200ms default
 * transition, 320ms sheet/route. M3 standard easing for almost everything;
 * emphasized for sheet entrances. No bounce, no spring, no scale-up.
 * Reduced-motion collapses everything to a fast fade.
 */
@Immutable
data class Motion(
    val fast: Int = 120,
    val standard: Int = 200,
    val deliberate: Int = 320,
    val easeOut: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f),       // M3 standard
    val emphasized: Easing = CubicBezierEasing(0.3f, 0f, 0.2f, 1f),  // sheet entrance
    val reducedMotion: Boolean = false,
) {
    fun <T> spec(durationMillis: Int = standard) =
        tween<T>(durationMillis = if (reducedMotion) fast else durationMillis, easing = easeOut)
}

val LocalMotion = staticCompositionLocalOf { Motion() }
