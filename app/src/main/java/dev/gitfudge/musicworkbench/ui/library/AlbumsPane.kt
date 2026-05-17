package dev.gitfudge.musicworkbench.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.gitfudge.musicworkbench.domain.AlbumSummary
import dev.gitfudge.musicworkbench.ui.theme.LocalSpacing

/**
 * Albums tab body. Renders the unfiled bucket as a pinned row at the top,
 * followed by every album row. Empty state guides toward picking a folder.
 */
@Composable
fun AlbumsPane(
    albums: List<AlbumSummary>,
    unfiledCount: Int,
    onAlbumClick: (albumKey: String) -> Unit,
    onUnfiledClick: () -> Unit,
    onChangeFolder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (albums.isEmpty() && unfiledCount == 0) {
        AlbumsEmptyState(onChangeFolder = onChangeFolder, modifier = modifier.fillMaxSize())
        return
    }

    val colors = MaterialTheme.colorScheme
    val spacing = LocalSpacing.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = spacing.sm, bottom = spacing.lg),
    ) {
        if (unfiledCount > 0) {
            item(key = "unfiled") {
                UnfiledRow(count = unfiledCount, onClick = onUnfiledClick)
                HorizontalDivider(
                    color = colors.outlineVariant,
                    modifier = Modifier.padding(start = spacing.lg + 64.dp + spacing.md),
                )
            }
        }
        items(albums, key = { it.albumKey }) { summary ->
            AlbumRow(
                summary = summary,
                onClick = { onAlbumClick(summary.albumKey) },
            )
            HorizontalDivider(
                color = colors.outlineVariant,
                modifier = Modifier.padding(start = spacing.lg + 64.dp + spacing.md),
            )
        }
    }
}

@Composable
private fun UnfiledRow(count: Int, onClick: () -> Unit) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.lg, vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = colors.surfaceContainerHigh,
            modifier = Modifier.size(64.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Inbox,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Unfiled",
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface,
            )
            Text(
                text = "$count track${if (count == 1) "" else "s"} with no album",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AlbumsEmptyState(onChangeFolder: () -> Unit, modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier.padding(horizontal = spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Album,
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.height(spacing.lg))
        Text(
            text = "No albums yet",
            style = MaterialTheme.typography.titleMedium,
            color = colors.onBackground,
        )
        Spacer(Modifier.height(spacing.sm))
        Text(
            text = "Point Music Workbench at a folder of audio files to start.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 340.dp),
        )
        Spacer(Modifier.height(spacing.lg))
        Button(onClick = onChangeFolder) { Text("Choose folder") }
    }
}
