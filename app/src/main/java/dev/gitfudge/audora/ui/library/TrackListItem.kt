package dev.gitfudge.audora.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.gitfudge.audora.data.db.TrackEntity
import dev.gitfudge.audora.domain.artStatus
import dev.gitfudge.audora.domain.displayArtist
import dev.gitfudge.audora.domain.displayTitle
import dev.gitfudge.audora.domain.lyricsStatus
import dev.gitfudge.audora.domain.tagStatus
import dev.gitfudge.audora.ui.common.ArtStatusChip
import dev.gitfudge.audora.ui.common.LyricsStatusChip
import dev.gitfudge.audora.ui.common.TagStatusChip
import dev.gitfudge.audora.ui.components.ArtTile
import dev.gitfudge.audora.ui.components.ArtTileSize
import dev.gitfudge.audora.ui.components.ListRow
import dev.gitfudge.audora.ui.theme.AudoraTheme
import dev.gitfudge.audora.ui.theme.ThemeMode
import java.io.File

@Composable
fun TrackListItem(
    track: TrackEntity,
    lowResThresholdPx: Int,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    selectionMode: Boolean = false,
    isDownloadingLyrics: Boolean = false,
    isDownloadingArt: Boolean = false,
    onLongClick: (String) -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    // DESIGN.md: selected row = full primaryContainer fill + checkbox (no leading bar).
    val titleColor = if (selected) colors.onPrimaryContainer else colors.onSurface
    val supportingColor = if (selected) {
        colors.onPrimaryContainer.copy(alpha = 0.78f)
    } else {
        colors.onSurfaceVariant
    }

    ListRow(
        modifier = modifier,
        onClick = { onClick(track.documentUri) },
        onLongClick = { onLongClick(track.documentUri) },
        selected = selected,
        leading = {
            if (selectionMode) {
                Checkbox(checked = selected, onCheckedChange = null)
            } else {
                ArtTile(
                    model = track.thumbnailPath?.let { File(it) },
                    contentDescription = null,
                    size = ArtTileSize.Sm,
                )
            }
        },
        headline = {
            Text(
                text = track.displayTitle(),
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supporting = {
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
        trailing = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = colors.surfaceContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.outline),
                ) {
                    Text(
                        text = track.format.take(4).uppercase(),
                        style = dev.gitfudge.audora.ui.theme.AppTextStyles.monoSmall
                            .copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
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
    composer = null,
    comment = null,
    compilation = false,
    albumKey = "${(artist ?: "").lowercase()}|${(album ?: "").lowercase()}",
    albumLabel = album ?: "",
    hasEmbeddedArt = hasArt,
    artWidth = if (hasArt) 1000 else null,
    artHeight = if (hasArt) 1000 else null,
    thumbnailPath = null,
    artScanPending = false,
    hasSidecarLrc = hasSynced,
    sidecarLrcSynced = hasSynced,
    coreTagsComplete = coreTagsComplete,
    artistUnknown = artistUnknown,
    scannedAt = 0L,
)

@Preview(name = "TrackListItem · Light")
@Composable
private fun TrackListItemLightPreview() = AudoraTheme(themeMode = ThemeMode.Light) {
    TrackListItem(track = previewTrack(), lowResThresholdPx = 600, onClick = {})
}

@Preview(name = "TrackListItem · Dark")
@Composable
private fun TrackListItemDarkPreview() = AudoraTheme(themeMode = ThemeMode.Dark) {
    TrackListItem(track = previewTrack(), lowResThresholdPx = 600, onClick = {})
}

@Preview(name = "TrackListItem · Selected")
@Composable
private fun TrackListItemSelectedPreview() = AudoraTheme(themeMode = ThemeMode.Dark) {
    TrackListItem(
        track = previewTrack(),
        lowResThresholdPx = 600,
        onClick = {},
        selected = true,
        selectionMode = true,
    )
}

@Preview(name = "TrackListItem · Warning")
@Composable
private fun TrackListItemWarningPreview() = AudoraTheme(themeMode = ThemeMode.Dark) {
    TrackListItem(
        track = previewTrack(coreTagsComplete = false, artistUnknown = true),
        lowResThresholdPx = 600,
        onClick = {},
    )
}
