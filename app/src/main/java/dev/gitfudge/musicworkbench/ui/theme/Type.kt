package dev.gitfudge.musicworkbench.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * DESIGN.md spec is Inter. Swap point: drop an Inter variable font into
 * res/font and set AppFontFamily to it. System sans is the offline-safe
 * fallback so the build never depends on a network font provider.
 */
val AppFontFamily = FontFamily.Default

private val tightLineHeight = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private fun style(
    size: Int,
    lineHeight: Int,
    weight: FontWeight,
    tracking: Double = 0.0,
) = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = tracking.em,
    lineHeightStyle = tightLineHeight,
)

/**
 * Maps the DESIGN.md scale onto the Material 3 roles actually used in this
 * app. Hierarchy is carried by weight contrast, not just size.
 */
val AppTypography = Typography(
    titleLarge = style(22, 28, FontWeight.SemiBold, -0.005),   // titleScreen
    titleMedium = style(17, 24, FontWeight.SemiBold),          // titleSection
    bodyLarge = style(15, 20, FontWeight.Medium),              // bodyStrong (track title)
    bodyMedium = style(15, 20, FontWeight.Normal),             // body (field values)
    labelLarge = style(15, 20, FontWeight.SemiBold),           // buttons
    labelMedium = style(13, 18, FontWeight.Medium, 0.005),     // labels, chips
    labelSmall = style(12, 16, FontWeight.Medium, 0.01),       // meta line, counts
    bodySmall = style(13, 18, FontWeight.Normal),              // secondary prose
)
