package dev.gitfudge.musicworkbench.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.gitfudge.musicworkbench.ui.theme.AppTextStyles
import dev.gitfudge.musicworkbench.ui.theme.MusicWorkbenchTheme
import dev.gitfudge.musicworkbench.ui.theme.ThemeMode

/**
 * Hero numerics for the library: a single readout (label + value + caption)
 * and a grouped row of readouts separated by hairline dividers.
 *
 * Use `large = false` for compact readouts (album-row counts, header chips);
 * `large = true` for the home stats hero.
 */
@Composable
fun MetricReadout(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
    large: Boolean = true,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = if (large) AppTextStyles.numericHero else AppTextStyles.numericLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (caption != null) {
            Text(
                text = caption,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceFaintCompat(),
            )
        }
    }
}

@Composable
fun MetricGroup(
    modifier: Modifier = Modifier,
    metrics: List<MetricItem>,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top,
    ) {
        metrics.forEachIndexed { index, item ->
            MetricReadout(
                label = item.label,
                value = item.value,
                caption = item.caption,
                large = item.large,
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
            )
            if (index != metrics.lastIndex) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.outlineVariant),
                )
            }
        }
    }
}

data class MetricItem(
    val label: String,
    val value: String,
    val caption: String? = null,
    val large: Boolean = true,
)

/**
 * Convenience: `MaterialTheme.colorScheme` doesn't have an "onSurfaceFaint"
 * role; surface the tonal step we want via the outlineVariant-adjacent
 * channel. Kept inline so call sites don't reach into Palette directly.
 */
@Composable
private fun androidx.compose.material3.ColorScheme.onSurfaceFaintCompat() =
    this.onSurfaceVariant.copy(alpha = 0.7f)

@Composable
private fun MetricsPreviewContent() {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        MetricGroup(metrics = listOf(
            MetricItem("Tracks", "1,284", "across 132 albums"),
            MetricItem("With art", "92%", "1,182 tracks"),
            MetricItem("With lyrics", "37%", "476 tracks"),
        ))
        MetricReadout(label = "Bitrate", value = "320 kbps", large = false)
    }
}

@Preview(name = "Metrics · Light") @Composable
private fun MetricsLightPreview() = MusicWorkbenchTheme(themeMode = ThemeMode.Light) { MetricsPreviewContent() }

@Preview(name = "Metrics · Dark") @Composable
private fun MetricsDarkPreview() = MusicWorkbenchTheme(themeMode = ThemeMode.Dark) { MetricsPreviewContent() }
