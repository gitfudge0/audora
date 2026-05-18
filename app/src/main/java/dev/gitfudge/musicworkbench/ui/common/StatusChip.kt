package dev.gitfudge.musicworkbench.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.ImageNotSupported
import androidx.compose.material.icons.rounded.LowPriority
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PersonOff
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.gitfudge.musicworkbench.domain.AlbumArtStatus
import dev.gitfudge.musicworkbench.domain.AlbumLyricsStatus
import dev.gitfudge.musicworkbench.domain.AlbumTagStatus
import dev.gitfudge.musicworkbench.domain.ArtStatus
import dev.gitfudge.musicworkbench.domain.LyricsStatus
import dev.gitfudge.musicworkbench.domain.TagStatus
import dev.gitfudge.musicworkbench.ui.theme.LocalStatusColors

/**
 * Status chip = icon + label + color. Replaces the 8dp colored dots so the
 * status vocabulary is legible without color perception and meets the
 * "Hierarchy: chips for status, not dots" rule from DESIGN.md.
 */
@Composable
fun StatusChip(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    accessibilityLabel: String = label,
    compact: Boolean = false,
) {
    // Hardware-style status tag: xs (4dp) corners, tinted wash background,
    // solid status fg. Never a pill — pills are for chips/buttons.
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f),
        contentColor = color,
        modifier = modifier.semantics { contentDescription = accessibilityLabel },
    ) {
        if (compact) {
            // Icon-only: minimal footprint so per-track headline/supporting text can breathe.
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp).size(12.dp),
            )
        } else {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
                    color = color,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}

// ── Per-track convenience chips ───────────────────────────────────────────────

@Composable
fun ArtStatusChip(status: ArtStatus, modifier: Modifier = Modifier) {
    val sc = LocalStatusColors.current
    when (status) {
        ArtStatus.PENDING -> StatusChip(Icons.Rounded.AccessTime, "Scanning art", sc.warn, modifier, compact = true)
        ArtStatus.OK -> StatusChip(Icons.Rounded.Check, "Art", sc.ok, modifier, compact = true)
        ArtStatus.LOW_RES -> StatusChip(Icons.Rounded.LowPriority, "Low-res art", sc.warn, modifier, compact = true)
        ArtStatus.NONE -> StatusChip(Icons.Rounded.ImageNotSupported, "No art", sc.missing, modifier, compact = true)
    }
}

@Composable
fun LyricsStatusChip(status: LyricsStatus, modifier: Modifier = Modifier) {
    val sc = LocalStatusColors.current
    val faint = MaterialTheme.colorScheme.onSurfaceVariant
    when (status) {
        LyricsStatus.SIDECAR_SYNCED -> StatusChip(Icons.Rounded.Lyrics, "Synced lyrics", sc.ok, modifier, compact = true)
        LyricsStatus.SIDECAR_PLAIN -> StatusChip(Icons.Rounded.Lyrics, "Plain lyrics", sc.warn, modifier, compact = true)
        LyricsStatus.NONE -> StatusChip(Icons.Rounded.Lyrics, "No lyrics", faint, modifier, compact = true)
    }
}

@Composable
fun TagStatusChip(status: TagStatus, modifier: Modifier = Modifier) {
    val sc = LocalStatusColors.current
    when (status) {
        TagStatus.OK -> StatusChip(Icons.Rounded.Tag, "Tags OK", sc.ok, modifier, compact = true)
        TagStatus.INCOMPLETE -> StatusChip(Icons.Rounded.ErrorOutline, "Tags incomplete", sc.warn, modifier, compact = true)
        TagStatus.UNKNOWN_ARTIST -> StatusChip(Icons.Rounded.PersonOff, "Unknown artist", sc.missing, modifier, compact = true)
    }
}

// ── Album-level chips ─────────────────────────────────────────────────────────

@Composable
fun AlbumArtStatusChip(status: AlbumArtStatus, modifier: Modifier = Modifier) {
    val sc = LocalStatusColors.current
    when (status) {
        AlbumArtStatus.PENDING -> StatusChip(Icons.Rounded.AccessTime, "Scanning art", sc.warn, modifier)
        AlbumArtStatus.ALL_OK -> StatusChip(Icons.Rounded.Check, "Art", sc.ok, modifier)
        AlbumArtStatus.LOW_RES -> StatusChip(Icons.Rounded.LowPriority, "Low-res art", sc.warn, modifier)
        AlbumArtStatus.PARTIAL -> StatusChip(Icons.Rounded.ImageNotSupported, "Some art missing", sc.warn, modifier)
        AlbumArtStatus.ALL_MISSING -> StatusChip(Icons.Rounded.ImageNotSupported, "No art", sc.missing, modifier)
    }
}

@Composable
fun AlbumLyricsStatusChip(status: AlbumLyricsStatus, modifier: Modifier = Modifier) {
    val sc = LocalStatusColors.current
    val faint = MaterialTheme.colorScheme.onSurfaceVariant
    when (status) {
        AlbumLyricsStatus.ALL_SYNCED -> StatusChip(Icons.Rounded.Lyrics, "All synced", sc.ok, modifier)
        AlbumLyricsStatus.ALL_PRESENT -> StatusChip(Icons.Rounded.Lyrics, "Lyrics", sc.warn, modifier)
        AlbumLyricsStatus.PARTIAL -> StatusChip(Icons.Rounded.AccessTime, "Some lyrics", sc.warn, modifier)
        AlbumLyricsStatus.NONE -> StatusChip(Icons.Rounded.Lyrics, "No lyrics", faint, modifier)
    }
}

@Composable
fun AlbumTagStatusChip(status: AlbumTagStatus, modifier: Modifier = Modifier) {
    val sc = LocalStatusColors.current
    when (status) {
        AlbumTagStatus.ALL_OK -> StatusChip(Icons.Rounded.Tag, "Tags", sc.ok, modifier)
        AlbumTagStatus.PARTIAL -> StatusChip(Icons.Rounded.ErrorOutline, "Some tags incomplete", sc.warn, modifier)
        AlbumTagStatus.ALL_BAD -> StatusChip(Icons.Rounded.ErrorOutline, "Tags incomplete", sc.missing, modifier)
    }
}

@Suppress("unused") // Reserved for empty-state placeholders in album rows.
@Composable
private fun PlaceholderIcon() {
    Icon(Icons.Rounded.MusicNote, null)
}
