package dev.gitfudge.musicworkbench.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.gitfudge.musicworkbench.ui.theme.LocalShapeScale
import dev.gitfudge.musicworkbench.ui.theme.MusicWorkbenchTheme
import dev.gitfudge.musicworkbench.ui.theme.ThemeMode

/**
 * Skeletons. Tonal blocks only — no shimmer animation (state-only motion).
 */
@Composable
fun SkeletonBlock(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = LocalShapeScale.current.sm,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}

@Composable
fun SkeletonText(
    modifier: Modifier = Modifier,
    height: Dp = 14.dp,
    width: Dp = 120.dp,
) {
    SkeletonBlock(modifier = modifier.height(height).width(width))
}

@Composable
fun SkeletonTile(modifier: Modifier = Modifier, size: Dp = 56.dp) {
    SkeletonBlock(modifier = modifier.size(size), shape = LocalShapeScale.current.sm)
}

@Composable
fun SkeletonRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkeletonTile()
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            SkeletonText(width = 180.dp, height = 16.dp)
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(6.dp))
            SkeletonText(width = 120.dp, height = 12.dp)
        }
    }
}

@Composable
private fun SkeletonsPreviewContent() {
    Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        SkeletonRow()
        SkeletonRow()
        SkeletonRow()
    }
}

@Preview(name = "Skeletons · Light") @Composable
private fun SkeletonsLightPreview() = MusicWorkbenchTheme(themeMode = ThemeMode.Light) { SkeletonsPreviewContent() }

@Preview(name = "Skeletons · Dark") @Composable
private fun SkeletonsDarkPreview() = MusicWorkbenchTheme(themeMode = ThemeMode.Dark) { SkeletonsPreviewContent() }
