package dev.gitfudge.musicworkbench.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.gitfudge.musicworkbench.domain.AlbumSummary
import dev.gitfudge.musicworkbench.ui.components.AlphabetScroller
import dev.gitfudge.musicworkbench.ui.components.EmptyState
import dev.gitfudge.musicworkbench.ui.components.ListRow
import dev.gitfudge.musicworkbench.ui.components.PrimaryButton
import dev.gitfudge.musicworkbench.ui.components.sectionLetterOf
import dev.gitfudge.musicworkbench.ui.theme.LocalShapeScale
import dev.gitfudge.musicworkbench.ui.theme.LocalSpacing

/**
 * Albums tab body. Renders the unfiled bucket as a pinned ListRow at the top,
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
    selectionMode: Boolean = false,
    selectedAlbumKeys: Set<String> = emptySet(),
    onEnterAlbumSelection: (albumKey: String) -> Unit = {},
    onToggleAlbumSelection: (albumKey: String) -> Unit = {},
) {
    if (albums.isEmpty() && unfiledCount == 0) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyState(
                icon = Icons.Rounded.Album,
                title = "No albums yet",
                body = "Point Music Workbench at a folder of audio files to start.",
                action = {
                    PrimaryButton(onClick = onChangeFolder) { Text("Choose folder") }
                },
            )
        }
        return
    }

    val colors = MaterialTheme.colorScheme
    val spacing = LocalSpacing.current
    val listState = rememberLazyListState()

    // Albums render after the optional pinned "Unfiled" row, so every album's
    // lazy index is shifted by that offset. Record the first index per letter.
    val unfiledOffset = if (unfiledCount > 0) 1 else 0
    val letterIndex = remember(albums, unfiledOffset) {
        buildMap {
            albums.forEachIndexed { i, summary ->
                putIfAbsent(sectionLetterOf(summary.albumLabel), i + unfiledOffset)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = spacing.sm,
            bottom = spacing.lg,
            end = spacing.xl + spacing.sm,
        ),
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
                onClick = {
                    if (selectionMode) onToggleAlbumSelection(summary.albumKey)
                    else onAlbumClick(summary.albumKey)
                },
                onLongClick = {
                    if (!selectionMode) onEnterAlbumSelection(summary.albumKey)
                },
                selected = summary.albumKey in selectedAlbumKeys,
                selectionMode = selectionMode,
            )
            HorizontalDivider(
                color = colors.outlineVariant,
                modifier = Modifier.padding(start = spacing.lg + 64.dp + spacing.md),
            )
        }
    }

        if (albums.size >= 12) {
            AlphabetScroller(
                listState = listState,
                indexForLetter = { letterIndex[it] },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun UnfiledRow(count: Int, onClick: () -> Unit) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme
    val tileShape = LocalShapeScale.current.sm
    ListRow(
        onClick = onClick,
        leading = {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(tileShape)
                    .background(colors.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Inbox,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(28.dp),
                )
            }
        },
        headline = {
            Text(
                text = "Unfiled",
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface,
            )
        },
        supporting = {
            Text(
                text = "$count track${if (count == 1) "" else "s"} with no album",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        },
        contentPaddingHorizontal = spacing.lg,
        contentPaddingVertical = spacing.sm,
    )
}
