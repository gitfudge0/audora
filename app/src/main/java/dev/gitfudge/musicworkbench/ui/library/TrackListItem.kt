package dev.gitfudge.musicworkbench.ui.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.gitfudge.musicworkbench.data.db.TrackEntity
import dev.gitfudge.musicworkbench.domain.artStatus
import dev.gitfudge.musicworkbench.domain.displayArtist
import dev.gitfudge.musicworkbench.domain.displayTitle
import dev.gitfudge.musicworkbench.domain.lyricsStatus
import dev.gitfudge.musicworkbench.domain.tagStatus
import dev.gitfudge.musicworkbench.ui.common.ArtStatusChip
import dev.gitfudge.musicworkbench.ui.common.LyricsStatusChip
import dev.gitfudge.musicworkbench.ui.common.TagStatusChip
import dev.gitfudge.musicworkbench.ui.theme.MusicWorkbenchTheme
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrackListItem(
    track: TrackEntity,
    lowResThresholdPx: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    selectionMode: Boolean = false,
    isDownloadingLyrics: Boolean = false,
    isDownloadingArt: Boolean = false,
    onLongClick: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    // DESIGN.md: selected row = full accentContainer fill + checkbox (banned: leading bar).
    // primaryContainer is mapped to Palette.AccentContainer in the theme.
    val containerColor = if (selected) colors.primaryContainer else colors.surface
    val titleColor = if (selected) colors.onPrimaryContainer else colors.onSurface
    val supportingColor = if (selected) {
        colors.onPrimaryContainer.copy(alpha = 0.78f)
    } else {
        colors.onSurfaceVariant
    }

    ListItem(
        modifier = modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick,
        ),
        colors = ListItemDefaults.colors(containerColor = containerColor),
        leadingContent = {
            if (selectionMode) {
                Checkbox(checked = selected, onCheckedChange = null)
            } else {
                TrackThumbnail(track)
            }
        },
        headlineContent = {
            Text(
                text = track.displayTitle(),
                style = MaterialTheme.typography.titleSmall,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Text(
                text = buildString {
                    append(track.displayArtist().orEmpty().ifBlank { "Unknown artist" })
                    if (track.albumLabel.isNotBlank()) {
                        append(" · ")
                        append(track.albumLabel)
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = supportingColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = colors.surfaceContainerHigh,
                ) {
                    Text(
                        text = track.format.take(4),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (isDownloadingArt) {
                        ArtDownloadingIndicator()
                    } else {
                        ArtStatusChip(track.artStatus(lowResThresholdPx))
                    }
                    TagStatusChip(track.tagStatus())
                    if (isDownloadingLyrics) {
                        LyricsDownloadingIndicator()
                    } else {
                        LyricsStatusChip(track.lyricsStatus())
                    }
                }
            }
        },
    )
}

@Composable
private fun TrackThumbnail(track: TrackEntity) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.small,
        color = colors.surfaceContainer,
        modifier = Modifier.size(48.dp),
    ) {
        val file = track.thumbnailPath?.let { File(it) }
        if (file != null) {
            AsyncImage(
                model = file,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

@Composable
private fun LyricsDownloadingIndicator() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(16.dp)
            .semantics { contentDescription = "Downloading lyrics" },
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(12.dp),
            strokeWidth = 1.5.dp,
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )
    }
}

@Composable
private fun ArtDownloadingIndicator() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(16.dp)
            .semantics { contentDescription = "Downloading art" },
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(12.dp),
            strokeWidth = 1.5.dp,
            color = MaterialTheme.colorScheme.tertiary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

fun previewTrack(
    title: String = "Song Without End",
    artist: String? = "Nils Frahm",
    album: String? = "All Melody",
    format: String = "FLAC",
    coreTagsComplete: Boolean = true,
    artistUnknown: Boolean = false,
    hasArt: Boolean = true,
    hasSynced: Boolean = true,
): TrackEntity = TrackEntity(
    documentUri = "preview://$title",
    treeUri = "preview://tree",
    displayName = "$title.${format.lowercase()}",
    parentPath = "Music/${artist ?: "Unknown"}/${album ?: "Unknown"}",
    format = format,
    sizeBytes = 1_000_000,
    lastModified = 0L,
    durationMs = 1_234_567L,
    title = title,
    artist = artist,
    album = album,
    albumArtist = artist,
    trackNumber = 1,
    discNumber = 1,
    year = "2018",
    genre = "Ambient",
    albumKey = "${(artist ?: "").lowercase()}|${(album ?: "").lowercase()}",
    albumLabel = album ?: "",
    hasEmbeddedArt = hasArt,
    artWidth = if (hasArt) 1000 else null,
    artHeight = if (hasArt) 1000 else null,
    thumbnailPath = null,
    hasSidecarLrc = hasSynced,
    sidecarLrcSynced = hasSynced,
    coreTagsComplete = coreTagsComplete,
    artistUnknown = artistUnknown,
    scannedAt = 0L,
)

@Preview(name = "TrackListItem · full", showBackground = true, backgroundColor = 0xFF14161A)
@Composable
private fun TrackListItemPreview() {
    MusicWorkbenchTheme {
        TrackListItem(
            track = previewTrack(),
            lowResThresholdPx = 600,
            onClick = {},
        )
    }
}

@Preview(name = "TrackListItem · selected", showBackground = true, backgroundColor = 0xFF14161A)
@Composable
private fun TrackListItemSelectedPreview() {
    MusicWorkbenchTheme {
        TrackListItem(
            track = previewTrack(),
            lowResThresholdPx = 600,
            onClick = {},
            selected = true,
            selectionMode = true,
        )
    }
}

@Preview(name = "TrackListItem · warning", showBackground = true, backgroundColor = 0xFF14161A)
@Composable
private fun TrackListItemWarningPreview() {
    MusicWorkbenchTheme {
        TrackListItem(
            track = previewTrack(coreTagsComplete = false, artistUnknown = true),
            lowResThresholdPx = 600,
            onClick = {},
        )
    }
}
