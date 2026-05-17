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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import dev.gitfudge.musicworkbench.ui.common.AlbumArtPreviewDialog
import dev.gitfudge.musicworkbench.ui.common.AlbumArtStatusChip
import dev.gitfudge.musicworkbench.ui.common.AlbumLyricsStatusChip
import dev.gitfudge.musicworkbench.ui.common.AlbumTagStatusChip
import dev.gitfudge.musicworkbench.ui.library.AlbumArtPickerSheet
import dev.gitfudge.musicworkbench.ui.library.AlbumPickerState
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
    val tagEditorOpen by viewModel.tagEditorOpen.collectAsStateWithLifecycle()
    val tagWriteInFlight by viewModel.tagWriteInFlight.collectAsStateWithLifecycle()
    val artFlow by viewModel.artFlow.collectAsStateWithLifecycle()
    val lyricsBatch by viewModel.lyricsBatch.collectAsStateWithLifecycle()
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
                    onTapCover = { viewModel.startHeroArt() },
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
                    onGetArt = { viewModel.startHeroArt() },
                    onGetLyrics = { viewModel.startLyricsBatch() },
                    onEditTags = { viewModel.openTagEditor() },
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

        // ── Overlays ────────────────────────────────────────────────────────
        if (tagEditorOpen) {
            AlbumTagEditorSheet(
                initial = viewModel.computeInitial(state.tracks),
                onApply = { viewModel.applyTagEdits(it) },
                onDismiss = { viewModel.dismissTagEditor() },
            )
        }
        if (tagWriteInFlight) {
            ProgressDialog("Saving tags…")
        }

        when (val af = artFlow) {
            AlbumArtFlowState.Idle -> Unit
            AlbumArtFlowState.Searching -> ProgressDialog("Searching for cover art…")
            is AlbumArtFlowState.NoMatch -> InfoDialog(af.message) { viewModel.dismissArtFlow() }
            is AlbumArtFlowState.Picker -> AlbumArtPickerSheet(
                state = AlbumPickerState(
                    albumName = state.albumLabel,
                    artistName = state.artistLabel,
                    trackCount = state.trackCount,
                    candidates = af.candidates,
                    albumIndex = 1,
                    totalAlbums = 1,
                ),
                onPick = { viewModel.pickArtCandidate(it) },
                onSkip = { viewModel.dismissArtFlow() },
            )
            is AlbumArtFlowState.Preview -> AlbumArtPreviewDialog(
                bytes = af.bytes,
                title = state.albumLabel,
                artist = state.artistLabel,
                saving = false,
                onApply = { viewModel.confirmHeroArt() },
                onBack = { viewModel.backToPicker() },
                subtitle = "${state.trackCount} ${if (state.trackCount == 1) "track" else "tracks"}",
            )
            is AlbumArtFlowState.Writing -> ProgressDialog("Saving art… ${af.done}/${af.total}")
        }

        when (val lb = lyricsBatch) {
            LyricsBatchPhase.Idle -> Unit
            is LyricsBatchPhase.Fetching -> ProgressDialog("Fetching lyrics… ${lb.done}/${lb.total}")
            is LyricsBatchPhase.Writing -> ProgressDialog("Saving lyrics… ${lb.done}/${lb.total}")
            is LyricsBatchPhase.Review -> LyricsBatchReviewSheet(
                review = lb,
                onToggle = { viewModel.toggleReviewItem(it) },
                onCommit = { viewModel.commitLyricsReview() },
                onDismiss = { viewModel.dismissLyricsBatch() },
            )
            is LyricsBatchPhase.Done -> InfoDialog(
                "Saved ${lb.saved} · Skipped ${lb.skipped} · No match ${lb.noMatch}" +
                    if (lb.failed > 0) " · Failed ${lb.failed}" else "",
            ) { viewModel.dismissLyricsBatch() }
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

@Composable
private fun ProgressDialog(message: String) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {},
        title = { Text(message) },
        text = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        },
    )
}

@Composable
private fun InfoDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } },
        text = { Text(message) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LyricsBatchReviewSheet(
    review: LyricsBatchPhase.Review,
    onToggle: (String) -> Unit,
    onCommit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme
    val acceptedCount = review.items.count { it.accept }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = spacing.lg, vertical = spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Text(
                text = "Review lyrics",
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
            )
            val summary = buildString {
                append("Found ${review.items.size}")
                if (review.noMatchCount > 0) append(" · No match ${review.noMatchCount}")
                if (review.failedCount > 0) append(" · Failed ${review.failedCount}")
            }
            Text(summary, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                items(review.items, key = { it.documentUri }) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggle(item.documentUri) }
                            .padding(vertical = spacing.xs),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                    ) {
                        Checkbox(checked = item.accept, onCheckedChange = { onToggle(item.documentUri) })
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title + if (item.isSynced) "  (synced)" else "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = item.previewText,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                FilledTonalButton(
                    onClick = onCommit,
                    enabled = acceptedCount > 0,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Save $acceptedCount")
                }
            }
        }
    }
}
