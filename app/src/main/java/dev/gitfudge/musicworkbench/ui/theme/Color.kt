package dev.gitfudge.musicworkbench.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * sRGB values are the DESIGN.md OKLCH tokens converted for Compose. Source of
 * truth is the OKLCH column in DESIGN.md; re-derive there if the hue shifts.
 */
internal object Palette {
    val Background = Color(0xFF14161A)
    val Surface = Color(0xFF1B1E23)
    val SurfaceVariant = Color(0xFF22252B)
    val SurfaceElevated = Color(0xFF292D34)
    val Outline = Color(0xFF3A3E47)
    val OutlineSubtle = Color(0xFF282B31)

    val OnSurface = Color(0xFFECEEF2)
    val OnSurfaceVariant = Color(0xFFA8ADB6)
    val OnSurfaceFaint = Color(0xFF7E838C)

    val Accent = Color(0xFFE0A35B)
    val AccentContainer = Color(0xFF6F4D29)
    val OnAccent = Color(0xFF2A2117)
    val OnAccentContainer = Color(0xFFF0D8B8)

    val StatusOk = Color(0xFF76B793)
    val StatusWarn = Color(0xFFE0BC60)
    val StatusMissing = Color(0xFFD86B57)
    val OnStatusMissing = Color(0xFF2A1714)
}

/**
 * Status vocabulary lives outside Material's ColorScheme because "ok / warn /
 * missing" is product semantics, not a Material role. Always paired with an
 * icon + label at the call site (see PRODUCT.md accessibility).
 */
@Immutable
data class StatusColors(
    val ok: Color,
    val warn: Color,
    val missing: Color,
    val onMissing: Color,
)

val DefaultStatusColors = StatusColors(
    ok = Palette.StatusOk,
    warn = Palette.StatusWarn,
    missing = Palette.StatusMissing,
    onMissing = Palette.OnStatusMissing,
)

val LocalStatusColors = staticCompositionLocalOf { DefaultStatusColors }
