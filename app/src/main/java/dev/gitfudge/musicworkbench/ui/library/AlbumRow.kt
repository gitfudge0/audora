package dev.gitfudge.musicworkbench.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import dev.gitfudge.musicworkbench.domain.AlbumSummary
import dev.gitfudge.musicworkbench.ui.common.AlbumArtStatusChip
import dev.gitfudge.musicworkbench.ui.common.AlbumLyricsStatusChip
import dev.gitfudge.musicworkbench.ui.common.AlbumTagStatusChip
import dev.gitfudge.musicworkbench.ui.components.ArtTile
import dev.gitfudge.musicworkbench.ui.components.ArtTileSize
import dev.gitfudge.musicworkbench.ui.components.ListRow
import dev.gitfudge.musicworkbench.ui.theme.LocalSpacing
import java.io.File

/**
 * One album row in the Albums tab. 64dp ArtTile + headline + supporting +
 * status chips, per DESIGN.md "list rows w/ 64dp art tile, not a grid".
 */
@Composable
fun AlbumRow(
    summary: AlbumSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    selectionMode: Boolean = false,
    onLongClick: () -> Unit = {},
) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme

    ListRow(
        modifier = modifier,
        onClick = onClick,
        onLongClick = onLongClick,
        selected = selected,
        leading = {
            if (selectionMode) {
                Checkbox(checked = selected, onCheckedChange = null)
            } else {
                ArtTile(
                    model = summary.coverThumbnailPath?.let { File(it) },
                    contentDescription = null,
                    size = ArtTileSize.Md,
                )
            }
        },
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
