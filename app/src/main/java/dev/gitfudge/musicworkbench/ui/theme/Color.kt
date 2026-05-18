package dev.gitfudge.musicworkbench.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Music Workbench palette — bone-and-ink with a single sodium-lamp accent.
 * Source of truth is the handoff design system (mw-tokens.css). Chrome is pure
 * neutral white/black (never cool gray); sodium (#DF5A24) is the only chroma
 * and marks state only — selection, focus, write-pending. Color is otherwise
 * status-only.
 */
@Immutable
internal data class Palette(
    val background: Color,        // bg-1 / paper canvas
    val surface: Color,           // bg-2 / bone elevated card
    val surfaceVariant: Color,    // bg-3 / paper-2 recessed (inputs, table row)
    val surfaceElevated: Color,   // bg-2 / bone (sheets, panels)
    val pressed: Color,           // bg-4 / paper-3 pressed wash
    val outline: Color,           // line-1 hairline (default boundary)
    val outlineSubtle: Color,     // line-1 hairline (dividers — same in this system)
    val outlineStrong: Color,     // line-2 / fog stronger border
    val onSurface: Color,         // fg-1 ink primary text
    val onSurfaceVariant: Color,  // fg-3 graphite secondary text
    val onSurfaceFaint: Color,    // fg-4 stone metadata / captions
    val disabled: Color,          // fg-5 fog disabled / placeholder
    val accent: Color,            // ink — monochrome primary fill (fg-1)
    val accentContainer: Color,   // sodium-wash — selected / state tint
    val onAccent: Color,          // paper — text on ink fill
    val onAccentContainer: Color, // sodium-deep — text/icon on state tint
    val sodium: Color,            // state accent
    val sodiumDeep: Color,        // pressed accent / on-wash text
    val sodiumWash: Color,        // tinted state background
    val sodiumLine: Color,        // tinted state border
    val statusOk: Color,
    val onStatusOk: Color,
    val statusWarn: Color,
    val onStatusWarn: Color,
    val statusMissing: Color,
    val onStatusMissing: Color,
)

internal val LightPalette = Palette(
    background = Color(0xFFFFFFFF),       // mw-paper
    surface = Color(0xFFFAFAFA),          // mw-bone
    surfaceVariant = Color(0xFFF4F4F4),   // mw-paper-2
    surfaceElevated = Color(0xFFFAFAFA),  // mw-bone
    pressed = Color(0xFFEBEBEB),          // mw-paper-3
    outline = Color(0xFFE8E8E8),          // mw-line
    outlineSubtle = Color(0xFFE8E8E8),    // mw-line
    outlineStrong = Color(0xFFC0C0C0),    // mw-fog
    onSurface = Color(0xFF111111),        // mw-ink
    onSurfaceVariant = Color(0xFF4A4A4A), // mw-graphite
    onSurfaceFaint = Color(0xFF888888),   // mw-stone
    disabled = Color(0xFFC0C0C0),         // mw-fog
    accent = Color(0xFF111111),           // mw-ink
    accentContainer = Color(0xFFFBE6D9),  // mw-sodium-wash
    onAccent = Color(0xFFFFFFFF),         // mw-paper
    onAccentContainer = Color(0xFFB9461A),// mw-sodium-deep
    sodium = Color(0xFFDF5A24),           // mw-sodium
    sodiumDeep = Color(0xFFB9461A),       // mw-sodium-deep
    sodiumWash = Color(0xFFFBE6D9),       // mw-sodium-wash
    sodiumLine = Color(0xFFF2B997),       // mw-sodium-line
    statusOk = Color(0xFF4A6B3A),
    onStatusOk = Color(0xFFFFFFFF),
    statusWarn = Color(0xFFA56C16),
    onStatusWarn = Color(0xFFFFFFFF),
    statusMissing = Color(0xFFB43A28),
    onStatusMissing = Color(0xFFFFFFFF),
)

internal val DarkPalette = Palette(
    background = Color(0xFF000000),       // mw-graphite-bg (true black)
    surface = Color(0xFF111111),          // mw-graphite-2
    surfaceVariant = Color(0xFF1A1A1A),   // mw-graphite-3
    surfaceElevated = Color(0xFF111111),  // mw-graphite-2
    pressed = Color(0xFF252525),          // dark bg-4
    outline = Color(0xFF1E1E1E),          // dark line-1
    outlineSubtle = Color(0xFF1E1E1E),    // dark line-1
    outlineStrong = Color(0xFF2A2A2A),    // dark line-2
    onSurface = Color(0xFFF5F5F5),        // dark fg-1
    onSurfaceVariant = Color(0xFFA0A0A0), // dark fg-3
    onSurfaceFaint = Color(0xFF707070),   // dark fg-4
    disabled = Color(0xFF484848),         // dark fg-5
    accent = Color(0xFFF5F5F5),           // ink → bone in dark (primary fill)
    accentContainer = Color(0xFF2D1508),  // dark sodium-wash
    onAccent = Color(0xFF111111),         // mw-ink
    onAccentContainer = Color(0xFFDF5A24),// sodium reads on dark wash
    sodium = Color(0xFFDF5A24),           // mw-sodium (unchanged in dark)
    sodiumDeep = Color(0xFFE0703C),       // lighter pressed/text in dark
    sodiumWash = Color(0xFF2D1508),       // dark sodium-wash
    sodiumLine = Color(0xFF5A2810),       // dark sodium-line
    statusOk = Color(0xFF76B793),
    onStatusOk = Color(0xFF0F1A14),
    statusWarn = Color(0xFFE0BC60),
    onStatusWarn = Color(0xFF1F1A0D),
    statusMissing = Color(0xFFD86B57),
    onStatusMissing = Color(0xFF2A1714),
)

/**
 * Status vocabulary lives outside Material's ColorScheme because "ok / warn /
 * missing" is product semantics, not a Material role. Always paired with an
 * icon + label at the call site (see PRODUCT.md accessibility).
 */
@Immutable
data class StatusColors(
    val ok: Color,
    val onOk: Color,
    val warn: Color,
    val onWarn: Color,
    val missing: Color,
    val onMissing: Color,
)

internal fun Palette.toStatusColors() = StatusColors(
    ok = statusOk,
    onOk = onStatusOk,
    warn = statusWarn,
    onWarn = onStatusWarn,
    missing = statusMissing,
    onMissing = onStatusMissing,
)

/**
 * Sodium is state-only chroma (selection, focus, write-pending) and isn't a
 * Material role. Exposed as a CompositionLocal so any composable can reach it
 * without threading the Palette through.
 */
@Immutable
data class AccentColors(
    val sodium: Color,
    val sodiumDeep: Color,
    val sodiumWash: Color,
    val sodiumLine: Color,
)

internal fun Palette.toAccentColors() = AccentColors(
    sodium = sodium,
    sodiumDeep = sodiumDeep,
    sodiumWash = sodiumWash,
    sodiumLine = sodiumLine,
)

val LocalStatusColors = staticCompositionLocalOf { DarkPalette.toStatusColors() }
val LocalAccentColors = staticCompositionLocalOf { DarkPalette.toAccentColors() }
