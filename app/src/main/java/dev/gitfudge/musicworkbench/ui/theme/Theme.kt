package dev.gitfudge.musicworkbench.ui.theme

import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * User-selectable theme preference. `System` follows the OS; Light/Dark
 * pin the app regardless.
 */
enum class ThemeMode { System, Light, Dark }

private fun Palette.toColorScheme(dark: Boolean) = if (dark) {
    darkColorScheme(
        primary = accent,                 // ink/bone monochrome fill
        onPrimary = onAccent,
        primaryContainer = sodiumWash,    // selected / state tint
        onPrimaryContainer = onAccentContainer,
        secondary = sodium,               // state accent
        onSecondary = onAccent,
        secondaryContainer = sodiumWash,
        onSecondaryContainer = onAccentContainer,
        tertiary = sodium,
        onTertiary = onAccent,
        background = background,
        onBackground = onSurface,
        surface = background,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        surfaceContainerLowest = background,
        surfaceContainerLow = surface,
        surfaceContainer = surface,
        surfaceContainerHigh = surfaceVariant,
        surfaceContainerHighest = pressed,
        outline = outline,
        outlineVariant = outlineSubtle,
        error = statusMissing,
        onError = onStatusMissing,
        scrim = Color(0x61111111),
    )
} else {
    lightColorScheme(
        primary = accent,
        onPrimary = onAccent,
        primaryContainer = sodiumWash,
        onPrimaryContainer = onAccentContainer,
        secondary = sodium,
        onSecondary = onAccent,
        secondaryContainer = sodiumWash,
        onSecondaryContainer = onAccentContainer,
        tertiary = sodium,
        onTertiary = onAccent,
        background = background,
        onBackground = onSurface,
        surface = background,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        surfaceContainerLowest = background,
        surfaceContainerLow = background,
        surfaceContainer = surface,
        surfaceContainerHigh = surfaceVariant,
        surfaceContainerHighest = pressed,
        outline = outline,
        outlineVariant = outlineSubtle,
        error = statusMissing,
        onError = onStatusMissing,
        scrim = Color(0x61111111),
    )
}

@Composable
fun MusicWorkbenchTheme(
    themeMode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themeMode) {
        ThemeMode.System -> systemDark
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val palette = if (useDark) DarkPalette else LightPalette

    val context = LocalContext.current
    val reducedMotion = remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f
    }

    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalShapeScale provides AppShapeScale(),
        LocalStatusColors provides palette.toStatusColors(),
        LocalAccentColors provides palette.toAccentColors(),
        LocalMotion provides Motion(reducedMotion = reducedMotion),
    ) {
        MaterialTheme(
            colorScheme = palette.toColorScheme(useDark),
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
