package dev.gitfudge.musicworkbench.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.gitfudge.musicworkbench.ui.theme.MusicWorkbenchTheme
import dev.gitfudge.musicworkbench.ui.theme.ThemeMode

/**
 * Generic list row. Leading / headline / supporting / trailing slots; min
 * height 64dp; selected = full primaryContainer fill (no leading bar — ever).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListRow(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    selected: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
    headline: @Composable () -> Unit,
    supporting: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    contentPaddingHorizontal: Dp = 16.dp,
    contentPaddingVertical: Dp = 12.dp,
) {
    val bg = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val clickMod = when {
        onClick != null && onLongClick != null -> Modifier.combinedClickable(
            role = Role.Button,
            onClick = onClick,
            onLongClick = onLongClick,
        )
        onClick != null -> Modifier.clickable(role = Role.Button, onClick = onClick)
        else -> Modifier
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp)
            .background(bg)
            .then(clickMod)
            .padding(horizontal = contentPaddingHorizontal, vertical = contentPaddingVertical),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (leading != null) leading()
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                headline()
            }
            if (supporting != null) {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    supporting()
                }
            }
        }
        if (trailing != null) Box { trailing() }
    }
}

@Composable
private fun ListRowPreviewContent() {
    Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        ListRow(
            onClick = {},
            headline = { Text("Let It Happen", style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            supporting = { Text("Tame Impala · Currents · 2015", style = MaterialTheme.typography.labelSmall) },
        )
        ListRow(
            onClick = {},
            selected = true,
            headline = { Text("The Less I Know The Better", style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            supporting = { Text("Tame Impala · Currents · 2015", style = MaterialTheme.typography.labelSmall) },
        )
    }
}

@Preview(name = "ListRow · Light") @Composable
private fun ListRowLightPreview() = MusicWorkbenchTheme(themeMode = ThemeMode.Light) { ListRowPreviewContent() }

@Preview(name = "ListRow · Dark") @Composable
private fun ListRowDarkPreview() = MusicWorkbenchTheme(themeMode = ThemeMode.Dark) { ListRowPreviewContent() }
