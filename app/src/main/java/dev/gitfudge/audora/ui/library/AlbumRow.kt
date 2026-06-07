package dev.gitfudge.audora.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import dev.gitfudge.audora.domain.AlbumSummary
import dev.gitfudge.audora.ui.common.AlbumArtStatusChip
import dev.gitfudge.audora.ui.common.AlbumLyricsStatusChip
import dev.gitfudge.audora.ui.common.AlbumTagStatusChip
import dev.gitfudge.audora.ui.components.ArtTile
import dev.gitfudge.audora.ui.components.ArtTileSize
import dev.gitfudge.audora.ui.components.ListRow
import dev.gitfudge.audora.ui.albumArtSharedElement
import dev.gitfudge.audora.ui.theme.LocalSpacing
import java.io.File

/**
 * One album row in the Albums tab. 64dp ArtTile + headline + supporting +
 * status chips, per DESIGN.md "list rows w/ 64dp art tile, not a grid".
 */
@Composable
fun AlbumRow(
    summary: AlbumSummary,
    onClick: (albumKey: String) -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    selectionMode: Boolean = false,
    onLongClick: (albumKey: String) -> Unit = {},
) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme

    ListRow(
        modifier = modifier,
        onClick = { onClick(summary.albumKey) },
        onLongClick = { onLongClick(summary.albumKey) },
        selected = selected,
        leading = {
            val artModel = remember(summary.coverThumbnailPath) {
                summary.coverThumbnailPath?.let { File(it) }
            }
            ArtTile(
                model = artModel,
                contentDescription = null,
                size = ArtTileSize.Md,
                modifier = Modifier.albumArtSharedElement(summary.albumKey),
            )
        },
        trailing = if (selectionMode) {
            {
                Checkbox(checked = selected, onCheckedChange = null)
            }
        } else null,
        headline = {
            Text(
                text = summary.albumLabel,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supporting = {
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
        },
        contentPaddingHorizontal = spacing.lg,
        contentPaddingVertical = spacing.sm,
    )
}
