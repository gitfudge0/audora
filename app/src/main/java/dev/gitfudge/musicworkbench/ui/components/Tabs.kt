package dev.gitfudge.musicworkbench.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.clip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.gitfudge.musicworkbench.ui.theme.LocalShapeScale
import dev.gitfudge.musicworkbench.ui.theme.MusicWorkbenchTheme
import dev.gitfudge.musicworkbench.ui.theme.ThemeMode

/**
 * Pill segmented tab strip — Albums | Tracks scope switch. State-only
 * motion: selected segment slides via Material's recomposition, no spring.
 */
data class PillTab(val label: String)

@Composable
fun PillTabs(
    tabs: List<PillTab>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = LocalShapeScale.current.pill,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            tabs.forEachIndexed { index, tab ->
                val selected = index == selectedIndex
                Surface(
                    onClick = { onSelected(index) },
                    shape = LocalShapeScale.current.pill,
                    color = if (selected) MaterialTheme.colorScheme.surface else androidx.compose.ui.graphics.Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ) {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

/**
 * Scope toolbar — left-aligned text + count, no container. The selected
 * scope is colored and weighted with a short underline accent; the other
 * is a quiet tappable label. Reads like a section header, not a control.
 */
data class ScopeItem(val label: String, val count: Int)

@Composable
fun ScopeToolbar(
    items: List<ScopeItem>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = androidx.compose.ui.Alignment.Bottom,
    ) {
        items.forEachIndexed { index, item ->
            val selected = index == selectedIndex
            val accent = MaterialTheme.colorScheme.primary
            val labelColor =
                if (selected) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .clip(LocalShapeScale.current.sm)
                    .clickable { onSelected(index) }
                    .padding(horizontal = 2.dp, vertical = 4.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.Start,
            ) {
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (selected)
                                androidx.compose.ui.text.font.FontWeight.SemiBold
                            else androidx.compose.ui.text.font.FontWeight.Normal,
                        ),
                        color = labelColor,
                    )
                    Text(
                        text = item.count.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .height(2.dp)
                        .width(if (selected) 20.dp else 0.dp)
                        .background(accent),
                )
            }
        }
    }
}

@Composable
private fun TabsPreviewContent() {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
    ) {
        PillTabs(
            tabs = listOf(PillTab("Albums"), PillTab("Tracks")),
            selectedIndex = 0,
            onSelected = {},
        )
    }
}

@Preview(name = "Tabs · Light") @Composable
private fun TabsLightPreview() = MusicWorkbenchTheme(themeMode = ThemeMode.Light) { TabsPreviewContent() }

@Preview(name = "Tabs · Dark") @Composable
private fun TabsDarkPreview() = MusicWorkbenchTheme(themeMode = ThemeMode.Dark) { TabsPreviewContent() }
