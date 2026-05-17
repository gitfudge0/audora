package dev.gitfudge.musicworkbench.ui.album

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import dev.gitfudge.musicworkbench.data.db.TrackEntity
import dev.gitfudge.musicworkbench.ui.common.AlbumArtStatusChip
import dev.gitfudge.musicworkbench.ui.common.AlbumLyricsStatusChip
import dev.gitfudge.musicworkbench.ui.common.AlbumTagStatusChip
import dev.gitfudge.musicworkbench.ui.theme.LocalSpacing
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    onBack: () -> Unit,
    onTrackClick: (String) -> Unit,
    viewModel: AlbumDetailViewModel = hiltViewModel(),
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = ui?.albumLabel ?: "Album",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { inner ->
        val state = ui
        if (state == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Album not found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@Scaffold
        }

        val tracks = state.tracks
        val multiDisc = tracks.mapNotNull { it.discNumber }.distinct().size > 1
        val grouped: List<Pair<Int?, List<TrackEntity>>> = if (multiDisc) {
            tracks.groupBy { it.discNumber ?: 0 }
                .toSortedMap()
                .map { (disc, items) -> (disc.takeIf { it != 0 }) to items }
        } else {
            listOf<Pair<Int?, List<TrackEntity>>>(null to tracks)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            contentPadding = PaddingValues(bottom = spacing.xl),
        ) {
            item("hero") {
                AlbumHero(
                    coverThumbnailPath = state.coverThumbnailPath,
                    title = state.albumLabel,
                    artist = state.artistLabel,
                    year = state.year,
                    trackCount = state.trackCount,
                    mixedArtist = state.mixedArtist,
                    onTapCover = { /* Phase 4: hero art editor */ },
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.lg, vertical = spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                ) {
                    AlbumArtStatusChip(state.artStatus)
                    AlbumLyricsStatusChip(state.lyricsStatus)
                    AlbumTagStatusChip(state.tagStatus)
                }
                AlbumActionRow(
                    onGetArt = { /* Phase 4 */ },
                    onGetLyrics = { /* Phase 4 */ },
                    onEditTags = { /* Phase 4 */ },
                )
                Spacer(Modifier.height(spacing.sm))
            }

            grouped.forEach { (disc, items) ->
                if (disc != null) {
                    item("disc-$disc") {
                        Text(
                            text = "Disc $disc",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(
                                horizontal = spacing.lg,
                                vertical = spacing.sm,
                            ),
                        )
                    }
                }
                items(items, key = { it.documentUri }) { track ->
                    AlbumTrackRow(
                        track = track,
                        onClick = { onTrackClick(track.documentUri) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumHero(
    coverThumbnailPath: String?,
    title: String,
    artist: String,
    year: String?,
    trackCount: Int,
    mixedArtist: Boolean,
    onTapCover: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.lg, vertical = spacing.md),
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = colors.surfaceContainer,
            modifier = Modifier
                .size(112.dp)
                .clickable(onClick = onTapCover),
        ) {
            val file = coverThumbnailPath?.let { File(it) }
            if (file != null) {
                AsyncImage(
                    model = file,
                    contentDescription = "Album cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Rounded.Album,
                        contentDescription = null,
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(40.dp),
                    )
                }
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = colors.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = artist,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = buildString {
                    append(trackCount)
                    append(if (trackCount == 1) " track" else " tracks")
                    year?.let { append(" · $it") }
                    if (mixedArtist) append(" · Mixed")
                },
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AlbumActionRow(
    onGetArt: () -> Unit,
    onGetLyrics: () -> Unit,
    onEditTags: () -> Unit,
) {
    val spacing = LocalSpacing.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.lg, vertical = spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        FilledTonalButton(onClick = onGetArt, modifier = Modifier.weight(1f)) {
            Icon(Icons.Rounded.Image, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(spacing.xs))
            Text("Art")
        }
        FilledTonalButton(onClick = onGetLyrics, modifier = Modifier.weight(1f)) {
            Icon(Icons.Rounded.Lyrics, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(spacing.xs))
            Text("Lyrics")
        }
        FilledTonalButton(onClick = onEditTags, modifier = Modifier.weight(1f)) {
            Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(spacing.xs))
            Text("Tags")
        }
    }
}

@Composable
private fun AlbumTrackRow(
    track: TrackEntity,
    onClick: () -> Unit,
) {
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
        Text(
            text = track.trackNumber?.toString()?.padStart(2, '0') ?: "—",
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant,
            modifier = Modifier.size(width = 28.dp, height = 20.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title?.takeIf(String::isNotBlank) ?: track.displayName,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val sub = track.artist?.takeIf(String::isNotBlank)
            if (sub != null) {
                Text(
                    text = sub,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Text(
            text = track.format.take(4),
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
        )
    }
}
