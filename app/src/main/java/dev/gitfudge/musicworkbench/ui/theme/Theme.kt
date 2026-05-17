package dev.gitfudge.musicworkbench.ui.theme

import android.provider.Settings
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColors = darkColorScheme(
    primary = Palette.Accent,
    onPrimary = Palette.OnAccent,
    primaryContainer = Palette.AccentContainer,
    onPrimaryContainer = Palette.OnAccentContainer,
    secondary = Palette.OnSurfaceVariant,
    onSecondary = Palette.Background,
    tertiary = Palette.StatusOk,
    onTertiary = Palette.Background,
    background = Palette.Background,
    onBackground = Palette.OnSurface,
    surface = Palette.Surface,
    onSurface = Palette.OnSurface,
    surfaceVariant = Palette.SurfaceVariant,
    onSurfaceVariant = Palette.OnSurfaceVariant,
    surfaceContainerLowest = Palette.Background,
    surfaceContainerLow = Palette.Surface,
    surfaceContainer = Palette.SurfaceVariant,
    surfaceContainerHigh = Palette.SurfaceElevated,
    surfaceContainerHighest = Palette.SurfaceElevated,
    outline = Palette.Outline,
    outlineVariant = Palette.OutlineSubtle,
    error = Palette.StatusMissing,
    onError = Palette.OnStatusMissing,
    scrim = Palette.Background,
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun MusicWorkbenchTheme(content: @Composable () -> Unit) {
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
        LocalStatusColors provides DefaultStatusColors,
        LocalMotion provides Motion(reducedMotion = reducedMotion),
    ) {
        MaterialTheme(
            colorScheme = DarkColors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
