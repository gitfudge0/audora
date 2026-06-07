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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import dev.gitfudge.audora.ui.library.DownloadStatus
import dev.gitfudge.audora.ui.theme.LocalSpacing
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AlbumDetailScreen(
    onBack: () -> Unit,
    onTrackClick: (String) -> Unit,
    deferHeavyContent: Boolean = false,
    viewModel: AlbumDetailViewModel = hiltViewModel(),
) {
    val albumState by viewModel.state.collectAsStateWithLifecycle()
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
        // While the enter slide is still running, render only the (cheap) top bar
        // + a neutral body so the animation stays at frame rate. The track list
        // composes once the transition settles — matching how it feels with
        // animations disabled.
        if (deferHeavyContent) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner),
            )
            return@Scaffold
        }

        val state = when (val s = albumState) {
            AlbumDetailState.Loading -> {
                // Neutral placeholder during the first DB round-trip. Rendering a
                // blank (matching the slide-in background) instead of an empty-state
                // message avoids a layout swap mid-navigation-animation.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(inner),
                )
                return@Scaffold
            }
            AlbumDetailState.NotFound -> {
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
            is AlbumDetailState.Loaded -> s.ui
        }

        val tracks = state.tracks
        // Disc grouping is a groupBy + sort; hoisting it out of the composition
        // body keeps it from re-running on every recomposition during the
        // navigation slide-in (which jitters the entering screen).
        val grouped: List<Pair<Int?, List<TrackEntity>>> = remember(tracks) {
            val multiDisc = tracks.mapNotNull { it.discNumber }.distinct().size > 1
            if (multiDisc) {
                tracks.groupBy { it.discNumber ?: 0 }
                    .toSortedMap()
                    .map { (disc, items) -> (disc.takeIf { it != 0 }) to items }
            } else {
                listOf(null to tracks)
            }
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
                        onClick = onTrackClick,
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
            AlbumArtFlowState.Searching -> ArtBatchSheet(
                state = af,
                onDismiss = { viewModel.dismissArtFlow() },
            )
            is AlbumArtFlowState.NoMatch -> ArtBatchSheet(
                state = af,
                onDismiss = { viewModel.dismissArtFlow() },
            )
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
            is AlbumArtFlowState.Writing -> ArtBatchSheet(
                state = af,
                onDismiss = { viewModel.dismissArtFlow() },
            )
            is AlbumArtFlowState.Done -> ArtBatchSheet(
                state = af,
                onDismiss = { viewModel.dismissArtFlow() },
            )
        }

        if (lyricsBatch !is LyricsBatchPhase.Idle) {
            LyricsBatchSheet(
                phase = lyricsBatch,
                onToggleReplace = { viewModel.setReplaceExisting(it) },
                onConfirmOptions = { viewModel.confirmLyricsOptions() },
                onToggleReviewItem = { viewModel.toggleReviewItem(it) },
                onCommitReview = { viewModel.commitLyricsReview() },
                onDismiss = { viewModel.dismissLyricsBatch() },
            )
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
        val heroModel = remember(coverThumbnailPath) { coverThumbnailPath?.let { File(it) } }
        ArtTileHero(
            model = heroModel,
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
    onClick: (documentUri: String) -> Unit,
) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme
    ListRow(
        onClick = { onClick(track.documentUri) },
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

/**
 * Single bottom sheet that drives the whole album lyrics batch flow —
 * options → fetching → review → saving → done — instead of a chain of
 * popups. Mirrors the downloads sheet: persistent surface, in-sheet
 * progress, dismissible only when no work is in flight.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LyricsBatchSheet(
    phase: LyricsBatchPhase,
    onToggleReplace: (Boolean) -> Unit,
    onConfirmOptions: () -> Unit,
    onToggleReviewItem: (String) -> Unit,
    onCommitReview: () -> Unit,
    onDismiss: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme

    // While fetching/saving the flow must not be swiped or tapped away.
    val inFlight = phase is LyricsBatchPhase.Fetching || phase is LyricsBatchPhase.Writing
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden || !inFlight },
    )

    AppBottomSheet(
        onDismissRequest = { if (!inFlight) onDismiss() },
        sheetState = sheetState,
        title = "Lyrics",
        footer = {
            when (phase) {
                is LyricsBatchPhase.Options -> {
                    val pending = if (phase.replaceExisting) phase.total else phase.total - phase.withExisting
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                    ) {
                        GhostButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                        PrimaryButton(
                            onClick = onConfirmOptions,
                            enabled = pending > 0,
                            modifier = Modifier.weight(1f),
                        ) { Text("Fetch $pending") }
                    }
                }
                is LyricsBatchPhase.Review -> {
                    val acceptedCount = phase.items.count { it.accept }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                    ) {
                        GhostButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                        PrimaryButton(
                            onClick = onCommitReview,
                            enabled = acceptedCount > 0,
                            modifier = Modifier.weight(1f),
                        ) { Text("Save $acceptedCount") }
                    }
                }
                is LyricsBatchPhase.Done -> {
                    PrimaryButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Done") }
                }
                else -> Unit
            }
        },
    ) {
        when (phase) {
            is LyricsBatchPhase.Options -> {
                val pending =
                    if (phase.replaceExisting) phase.total else phase.total - phase.withExisting
                Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    Text(
                        text = buildString {
                            append("${phase.total} ${if (phase.total == 1) "track" else "tracks"} in this album.")
                            if (phase.withExisting > 0) {
                                append(" ${phase.withExisting} already ${if (phase.withExisting == 1) "has" else "have"} lyrics.")
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurface,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = phase.withExisting > 0) {
                                onToggleReplace(!phase.replaceExisting)
                            }
                            .padding(vertical = spacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                    ) {
                        Checkbox(
                            checked = phase.replaceExisting,
                            onCheckedChange = onToggleReplace,
                            enabled = phase.withExisting > 0,
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Replace existing lyrics",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (phase.withExisting > 0) colors.onSurface else colors.onSurfaceVariant,
                            )
                            Text(
                                text = if (phase.withExisting > 0) {
                                    "Re-download and overwrite for the ${phase.withExisting} ${if (phase.withExisting == 1) "track" else "tracks"} that already have lyrics."
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
            }

            is LyricsBatchPhase.Fetching ->
                BatchItemList(
                    "Fetching lyrics", phase.items, Icons.Rounded.Lyrics,
                    activeLabel = "Fetching…", doneLabel = "Lyrics found",
                )

            is LyricsBatchPhase.Writing ->
                BatchItemList(
                    "Saving lyrics", phase.items, Icons.Rounded.Lyrics,
                    activeLabel = "Saving…", doneLabel = "Lyrics saved",
                )

            is LyricsBatchPhase.Review -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.7f),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    val summary = buildString {
                        append("Found ${phase.items.size}")
                        if (phase.noMatchCount > 0) append(" · No match ${phase.noMatchCount}")
                        if (phase.failedCount > 0) append(" · Failed ${phase.failedCount}")
                    }
                    Text(summary, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = true),
                        verticalArrangement = Arrangement.spacedBy(spacing.xs),
                    ) {
                        items(phase.items, key = { it.documentUri }) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleReviewItem(item.documentUri) }
                                    .padding(vertical = spacing.xs),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                            ) {
                                Checkbox(
                                    checked = item.accept,
                                    onCheckedChange = { onToggleReviewItem(item.documentUri) },
                                )
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

            is LyricsBatchPhase.Done -> {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    Text(
                        text = "Saved ${phase.saved} · Skipped ${phase.skipped} · No match ${phase.noMatch}" +
                            if (phase.failed > 0) " · Failed ${phase.failed}" else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurface,
                    )
                }
            }

            LyricsBatchPhase.Idle -> Unit
        }
    }
}

@Composable
private fun BatchItemList(
    label: String,
    items: List<BatchItem>,
    pendingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    activeLabel: String,
    doneLabel: String,
) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme
    val finished = items.count {
        it.status == DownloadStatus.SAVED ||
            it.status == DownloadStatus.NO_MATCH ||
            it.status == DownloadStatus.FAILED
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.7f),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Text(
            text = "$label… $finished/${items.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface,
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            items(items, key = { it.documentUri }) { item ->
                BatchItemRow(item, pendingIcon, activeLabel, doneLabel)
            }
        }
    }
}

@Composable
private fun BatchItemRow(
    item: BatchItem,
    pendingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    activeLabel: String,
    doneLabel: String,
) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme
    val sc = dev.gitfudge.audora.ui.theme.LocalStatusColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        androidx.compose.material3.Surface(
            shape = dev.gitfudge.audora.ui.theme.LocalShapeScale.current.sm,
            color = colors.surfaceContainer,
            modifier = Modifier.size(32.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                when (item.status) {
                    DownloadStatus.PENDING -> Icon(
                        imageVector = pendingIcon,
                        contentDescription = null,
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                    DownloadStatus.DOWNLOADING -> CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = colors.secondary,
                    )
                    DownloadStatus.SAVED -> Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = sc.ok,
                        modifier = Modifier.size(18.dp),
                    )
                    DownloadStatus.NO_MATCH -> Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = sc.warn,
                        modifier = Modifier.size(18.dp),
                    )
                    DownloadStatus.FAILED -> Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = sc.missing,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val (label, tint) = when (item.status) {
                DownloadStatus.PENDING -> "Queued" to colors.onSurfaceVariant
                DownloadStatus.DOWNLOADING -> activeLabel to colors.onSurfaceVariant
                DownloadStatus.SAVED -> doneLabel to sc.ok
                DownloadStatus.NO_MATCH -> "No match" to sc.warn
                DownloadStatus.FAILED -> "Failed" to sc.missing
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = tint,
            )
        }
    }
}

/**
 * Single bottom sheet for the album cover-art write flow — per-track
 * progress while embedding, then a result summary — matching the lyrics
 * batch sheet. Non-dismissible while writing is in flight.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArtBatchSheet(
    state: AlbumArtFlowState,
    onDismiss: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme

    val inFlight = state is AlbumArtFlowState.Writing
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden || !inFlight },
    )

    AppBottomSheet(
        onDismissRequest = { if (!inFlight) onDismiss() },
        sheetState = sheetState,
        title = "Cover art",
        footer = {
            when (state) {
                is AlbumArtFlowState.Done ->
                    PrimaryButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Done") }
                is AlbumArtFlowState.NoMatch ->
                    PrimaryButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("OK") }
                else -> Unit
            }
        },
    ) {
        when (state) {
            AlbumArtFlowState.Searching -> Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = spacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = colors.secondary,
                )
                Text(
                    text = "Searching for cover art…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface,
                )
            }
            is AlbumArtFlowState.NoMatch -> Text(
                text = state.message,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface,
            )
            is AlbumArtFlowState.Writing ->
                BatchItemList(
                    label = "Saving cover art",
                    items = state.items,
                    pendingIcon = Icons.Rounded.Image,
                    activeLabel = "Saving…",
                    doneLabel = "Cover saved",
                )
            is AlbumArtFlowState.Done -> Text(
                text = "Saved ${state.saved}" +
                    if (state.failed > 0) " · Failed ${state.failed}" else "",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface,
            )
            else -> Unit
        }
    }
}
