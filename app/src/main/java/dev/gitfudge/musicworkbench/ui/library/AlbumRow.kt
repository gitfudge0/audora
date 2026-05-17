package dev.gitfudge.musicworkbench.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.gitfudge.musicworkbench.domain.AlbumSummary
import dev.gitfudge.musicworkbench.ui.common.AlbumArtStatusChip
import dev.gitfudge.musicworkbench.ui.common.AlbumLyricsStatusChip
import dev.gitfudge.musicworkbench.ui.common.AlbumTagStatusChip
import dev.gitfudge.musicworkbench.ui.theme.LocalSpacing
import java.io.File

/**
 * One album row in the Albums tab. 64dp art tile + two-line text + status chips,
 * per DESIGN.md "list rows w/ 64dp art tile, not a grid".
 */
@Composable
fun AlbumRow(
    summary: AlbumSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.lg, vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        AlbumTile(summary.coverThumbnailPath)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(spacing.hairline + 1.dp),
        ) {
            Text(
                text = summary.albumLabel,
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = buildString {
                    append(summary.artistLabel)
                    append(" · ")
                    append(summary.trackCount)
                    append(if (summary.trackCount == 1) " track" else " tracks")
                    summary.year?.let { append(" · $it") }
                },
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                modifier = Modifier.padding(top = spacing.xs),
            ) {
                AlbumArtStatusChip(summary.artStatus)
                AlbumLyricsStatusChip(summary.lyricsStatus)
                AlbumTagStatusChip(summary.tagStatus)
            }
        }
    }
}

@Composable
private fun AlbumTile(thumbnailPath: String?) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.small,
        color = colors.surfaceContainer,
        modifier = Modifier.size(64.dp),
    ) {
        val file = thumbnailPath?.let { File(it) }
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
                    imageVector = Icons.Rounded.Album,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}
