package dev.gitfudge.musicworkbench.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Handoff design system specifies Geist (sans) + Geist Mono (technical
 * numerics). No Geist variable font is bundled, so we fall back to the
 * platform sans/mono exactly as the design CSS does (`local("Geist"),
 * system-ui`). Swap point: drop GeistVariable.ttf / GeistMonoVariable.ttf into
 * res/font and point AppFontFamily / AppMonoFamily at them.
 */
val AppFontFamily = FontFamily.Default
val AppMonoFamily = FontFamily.Monospace

private val tightLineHeight = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

/** OpenType `tnum` — tabular figures so numerics align column-wise. */
private const val TABULAR = "tnum"

/** Geist body weight is 450; Compose has no 450 so Normal (400) is closest. */
private val Body = FontWeight.Normal
private val Caption = FontWeight.Medium // 500 — caption / mono
private val Heading = FontWeight.SemiBold // 600 — headings / eyebrow

private fun style(
    size: Int,
    lineHeight: Int,
    weight: FontWeight,
    tracking: Double = 0.0,
    family: FontFamily = AppFontFamily,
    tabular: Boolean = false,
) = TextStyle(
    fontFamily = family,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = tracking.em,
    lineHeightStyle = tightLineHeight,
    fontFeatureSettings = if (tabular) TABULAR else "",
)

/**
 * mw-tokens.css scale mapped onto the Material 3 roles this app actually
 * uses. Hierarchy is carried by weight contrast (Geist 450 vs 600), negative
 * tracking on headings, default on body.
 */
val AppTypography = Typography(
    // numericHero — HeroMetrics readout (mono, tabular)
    displaySmall = style(24, 28, Heading, -0.02, family = AppMonoFamily, tabular = true),
    // numericLarge — secondary big numerics
    headlineSmall = style(20, 24, Heading, -0.01, family = AppMonoFamily, tabular = true),
    // screen / hero title (t-h1 / display-2 scaled for phone)
    titleLarge = style(24, 28, Heading, -0.015),
    // app bar / sheet / panel title (t-h3)
    titleMedium = style(17, 22, Heading, -0.005),
    // list primary (album / track row title) — 600 @ 14
    bodyLarge = style(14, 19, Heading),
    // default body / field values (t-body)
    bodyMedium = style(14, 20, Body),
    // buttons (t-body, 600)
    labelLarge = style(14, 18, Heading),
    // chips / inline labels (t-body-sm, 500)
    labelMedium = style(13, 18, Caption, 0.005),
    // eyebrow / section label / meta (t-eyebrow) — uppercase set at call site
    labelSmall = style(11, 16, Heading, 0.10),
    // supporting prose under a headline (t-caption)
    bodySmall = style(12, 16, Body),
)

/**
 * Styles with no clean Material analog. Reach for these explicitly
 * (`AppTextStyles.mono` etc.) rather than inlining sizes.
 */
object AppTextStyles {
    val numericHero: TextStyle = AppTypography.displaySmall
    val numericLarge: TextStyle = AppTypography.headlineSmall
    val mono: TextStyle = style(13, 18, Caption, family = AppMonoFamily, tabular = true)
    val monoSmall: TextStyle = style(11, 16, Caption, family = AppMonoFamily, tabular = true)
    val eyebrow: TextStyle = style(11, 16, Heading, 0.12)
}
