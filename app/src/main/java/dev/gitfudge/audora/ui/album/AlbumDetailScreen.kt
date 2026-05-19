package dev.gitfudge.audora.ui.album

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gitfudge.audora.data.db.TrackEntity
import dev.gitfudge.audora.domain.artStatus
import dev.gitfudge.audora.domain.lyricsStatus
import dev.gitfudge.audora.domain.tagStatus
import dev.gitfudge.audora.ui.common.AlbumArtPreviewDialog
import dev.gitfudge.audora.ui.common.AlbumArtStatusChip
import dev.gitfudge.audora.ui.common.AlbumLyricsStatusChip
import dev.gitfudge.audora.ui.common.AlbumTagStatusChip
import dev.gitfudge.audora.ui.common.ArtStatusChip
import dev.gitfudge.audora.ui.common.LyricsStatusChip
import dev.gitfudge.audora.ui.common.TagStatusChip
import dev.gitfudge.audora.ui.components.AppBottomSheet
import dev.gitfudge.audora.ui.components.AppTopBar
import dev.gitfudge.audora.ui.components.ArtTileHero
import dev.gitfudge.audora.ui.components.GhostButton
import dev.gitfudge.audora.ui.components.ListRow
import dev.gitfudge.audora.ui.components.PrimaryButton
import dev.gitfudge.audora.ui.components.SecondaryButton
import dev.gitfudge.audora.ui.library.AlbumArtPickerSheet
import dev.gitfudge.audora.ui.library.AlbumPickerState
import dev.gitfudge.audora.ui.theme.LocalSpacing
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    val artDownloading by viewModel.artDownloading.collectAsStateWithLifecycle()
    val lyricsBatch by viewModel.lyricsBatch.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colors.background,
        topBar = {
            AppTopBar(
                // Constant title; the album name lives in the hero, so duplicating it
                // here just creates two title areas competing for attention.
                title = "Album",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
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
                    color = colors.onSurfaceVariant,
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
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.lg, vertical = spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                    verticalArrangement = Arrangement.spacedBy(spacing.xs),
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
                            color = colors.onSurfaceVariant,
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
                        lowResThresholdPx = state.lowResThresholdPx,
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
                downloadingCandidate = artDownloading,
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
            is LyricsBatchPhase.Options -> LyricsOptionsDialog(
                options = lb,
                onToggleReplace = { viewModel.setReplaceExisting(it) },
                onConfirm = { viewModel.confirmLyricsOptions() },
                onDismiss = { viewModel.dismissLyricsBatch() },
            )
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

    // Vertical hero: the cover is the master, sized aggressively, square (no
    // radius enforced by ArtTileHero shape override), then the text block.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.lg, vertical = spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        ArtTileHero(
            model = coverThumbnailPath?.let { File(it) },
            contentDescription = "Album cover",
            shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
            modifier = Modifier
                .size(120.dp)
                .clickable(onClick = onTapCover),
        )
        Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
            Text(
                text = "ALBUM · ${artist.uppercase()}",
                style = dev.gitfudge.audora.ui.theme.AppTextStyles.eyebrow,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
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
                style = dev.gitfudge.audora.ui.theme.AppTextStyles.mono,
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
        ActionPill(Icons.Rounded.Image, "Art", onGetArt, Modifier.weight(1f))
        ActionPill(Icons.Rounded.Lyrics, "Lyrics", onGetLyrics, Modifier.weight(1f))
        ActionPill(Icons.Rounded.Edit, "Tags", onEditTags, Modifier.weight(1f))
    }
}

@Composable
private fun ActionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    SecondaryButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = spacing.sm, vertical = spacing.xs),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.size(spacing.xs))
        Text(
            text = label,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun AlbumTrackRow(
    track: TrackEntity,
    lowResThresholdPx: Int,
    onClick: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme
    ListRow(
        onClick = onClick,
        leading = {
            Text(
                text = track.trackNumber?.toString()?.padStart(2, '0') ?: "—",
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
                modifier = Modifier.size(width = 28.dp, height = 20.dp),
            )
        },
        headline = {
            Text(
                text = track.title?.takeIf(String::isNotBlank) ?: track.displayName,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supporting = track.artist?.takeIf(String::isNotBlank)?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        trailing = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    Text(
                        text = track.format.take(4).uppercase(),
                        style = dev.gitfudge.audora.ui.theme.AppTextStyles.monoSmall,
                        color = colors.onSurfaceVariant,
                    )
                    track.durationMs?.let { ms ->
                        val total = ms / 1000
                        Text(
                            text = "%d:%02d".format(total / 60, total % 60),
                            style = dev.gitfudge.audora.ui.theme.AppTextStyles.mono,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    ArtStatusChip(track.artStatus(lowResThresholdPx))
                    TagStatusChip(track.tagStatus())
                    LyricsStatusChip(track.lyricsStatus())
                }
            }
        },
        contentPaddingHorizontal = spacing.lg,
        contentPaddingVertical = spacing.sm,
    )
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

@Composable
private fun LyricsOptionsDialog(
    options: LyricsBatchPhase.Options,
    onToggleReplace: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme
    val pending = if (options.replaceExisting) options.total else options.total - options.withExisting

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Fetch lyrics") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                Text(
                    text = buildString {
                        append("${options.total} ${if (options.total == 1) "track" else "tracks"} in this album.")
                        if (options.withExisting > 0) {
                            append(" ${options.withExisting} already ${if (options.withExisting == 1) "has" else "have"} lyrics.")
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = options.withExisting > 0) {
                            onToggleReplace(!options.replaceExisting)
                        }
                        .padding(vertical = spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    Checkbox(
                        checked = options.replaceExisting,
                        onCheckedChange = onToggleReplace,
                        enabled = options.withExisting > 0,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Replace existing lyrics",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (options.withExisting > 0) colors.onSurface else colors.onSurfaceVariant,
                        )
                        Text(
                            text = if (options.withExisting > 0) {
                                "Re-download and overwrite for the ${options.withExisting} ${if (options.withExisting == 1) "track" else "tracks"} that already have lyrics."
                            } else {
                                "No tracks already have lyrics."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    text = "Will fetch for $pending ${if (pending == 1) "track" else "tracks"}.",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.primary,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = pending > 0) { Text("Fetch") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
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
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme
    val acceptedCount = review.items.count { it.accept }

    AppBottomSheet(
        onDismissRequest = onDismiss,
        title = "Review lyrics",
        footer = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                GhostButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                PrimaryButton(
                    onClick = onCommit,
                    enabled = acceptedCount > 0,
                    modifier = Modifier.weight(1f),
                ) { Text("Save $acceptedCount") }
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            val summary = buildString {
                append("Found ${review.items.size}")
                if (review.noMatchCount > 0) append(" · No match ${review.noMatchCount}")
                if (review.failedCount > 0) append(" · Failed ${review.failedCount}")
            }
            Text(summary, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true),
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
        }
    }
}
