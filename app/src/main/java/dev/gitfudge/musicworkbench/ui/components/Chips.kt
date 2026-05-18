package dev.gitfudge.musicworkbench.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.gitfudge.musicworkbench.ui.theme.LocalShapeScale
import dev.gitfudge.musicworkbench.ui.theme.MusicWorkbenchTheme
import dev.gitfudge.musicworkbench.ui.theme.ThemeMode

/**
 * Chip family: filter chips (toggle), count chips (display), and a generic
 * status chip surface. Domain status chips (Art/Lyrics/Tag) live in
 * ui/common/StatusChip.kt and use the same visual vocabulary.
 */

@Composable
fun AppFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    val shape = LocalShapeScale.current.pill
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = 36.dp)
            .clickable(role = Role.Button, onClick = onClick),
        shape = shape,
        color = bg,
        contentColor = fg,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val icon = leadingIcon ?: if (selected) Icons.Rounded.Check else null
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun CountChip(
    count: Int,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    Surface(
        modifier = modifier.defaultMinSize(minHeight = 24.dp, minWidth = 28.dp),
        shape = LocalShapeScale.current.pill,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

/**
 * Generic chip surface — caller supplies icon + label + color. Status chip
 * convenience wrappers in `ui/common/StatusChip.kt` build on this contract.
 */
@Composable
fun TintedChip(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = LocalShapeScale.current.pill,
        color = color.copy(alpha = 0.16f),
        contentColor = color,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = color)
        }
    }
}

@Composable
private fun ChipsPreviewContent() {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppFilterChip(selected = true, onClick = {}, label = "Albums")
            AppFilterChip(selected = false, onClick = {}, label = "Tracks")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CountChip(count = 12)
            CountChip(count = 348)
        }
    }
}

@Preview(name = "Chips · Light") @Composable
private fun ChipsLightPreview() = MusicWorkbenchTheme(themeMode = ThemeMode.Light) { ChipsPreviewContent() }

@Preview(name = "Chips · Dark") @Composable
private fun ChipsDarkPreview() = MusicWorkbenchTheme(themeMode = ThemeMode.Dark) { ChipsPreviewContent() }
