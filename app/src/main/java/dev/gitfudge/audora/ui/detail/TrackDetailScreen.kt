package dev.gitfudge.audora.ui.detail

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gitfudge.audora.data.art.CoverArtCandidate
import dev.gitfudge.audora.data.db.TrackEntity
import dev.gitfudge.audora.data.lyrics.LrclibResult
import dev.gitfudge.audora.domain.LyricsStatus
import dev.gitfudge.audora.domain.displayTitle
import dev.gitfudge.audora.ui.common.AlbumArtPreviewDialog
import dev.gitfudge.audora.domain.lyricsStatus
import dev.gitfudge.audora.ui.components.AppPanel
import dev.gitfudge.audora.ui.components.AppTextField
import dev.gitfudge.audora.ui.components.ArtTile
import dev.gitfudge.audora.ui.components.ArtTileHero
import dev.gitfudge.audora.ui.components.ArtTileSize
import dev.gitfudge.audora.ui.components.GhostButton
import dev.gitfudge.audora.ui.components.Hairline
import dev.gitfudge.audora.ui.components.OutlineButton
import dev.gitfudge.audora.ui.components.PrimaryButton
import dev.gitfudge.audora.ui.components.SecondaryButton
import dev.gitfudge.audora.ui.library.previewTrack
import dev.gitfudge.audora.ui.theme.AppTextStyles
import dev.gitfudge.audora.ui.theme.LocalMotion
import dev.gitfudge.audora.ui.theme.LocalShapeScale
import dev.gitfudge.audora.ui.theme.LocalSpacing
import dev.gitfudge.audora.ui.theme.LocalStatusColors
import dev.gitfudge.audora.ui.theme.AudoraTheme
import java.io.File

@Composable
fun TrackDetailScreen(
    onBack: () -> Unit,
    viewModel: TrackDetailViewModel = hiltViewModel(),
) {
    val track by viewModel.track.collectAsStateWithLifecycle()
    val form by viewModel.form.collectAsStateWithLifecycle()
    val isDirty by viewModel.isDirty.collectAsStateWithLifecycle()
    val changes by viewModel.changes.collectAsStateWithLifecycle()
    val tagSaveState by viewModel.tagSaveState.collectAsStateWithLifecycle()
    val lyricsState by viewModel.lyricsState.collectAsStateWithLifecycle()
    val lyricsSaveState by viewModel.lyricsSaveState.collectAsStateWithLifecycle()
    val pendingArtUri by viewModel.pendingArtUri.collectAsStateWithLifecycle()
    val artSaveState by viewModel.artSaveState.collectAsStateWithLifecycle()
    val artFetchState by viewModel.artFetchState.collectAsStateWithLifecycle()
    val manualLyrics by viewModel.manualLyrics.collectAsStateWithLifecycle()

    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(tagSaveState) {
        when (val s = tagSaveState) {
            is DetailSaveState.Done -> {
                viewModel.dismissTagSave()
                val result = snackbar.showSnackbar(
                    message = "Tags written",
                    actionLabel = "Undo",
                    duration = SnackbarDuration.Short,
                )
                if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                    viewModel.undoTags()
                    snackbar.showSnackbar("Reverted", duration = SnackbarDuration.Short)
                }
            }
            is DetailSaveState.Failed -> { snackbar.showSnackbar(s.message, duration = SnackbarDuration.Long); viewModel.dismissTagSave() }
            else -> {}
        }
    }
    LaunchedEffect(lyricsSaveState) {
        when (val s = lyricsSaveState) {
            is DetailSaveState.Done -> { snackbar.showSnackbar("Lyrics saved", duration = SnackbarDuration.Short); viewModel.dismissLyricsSave() }
            is DetailSaveState.Failed -> { snackbar.showSnackbar(s.message, duration = SnackbarDuration.Long); viewModel.dismissLyricsSave() }
            else -> {}
        }
    }
    LaunchedEffect(artSaveState) {
        when (val s = artSaveState) {
            is DetailSaveState.Done -> { snackbar.showSnackbar("Art saved", duration = SnackbarDuration.Short); viewModel.dismissArtSave() }
            is DetailSaveState.Failed -> { snackbar.showSnackbar(s.message, duration = SnackbarDuration.Long); viewModel.dismissArtSave() }
            else -> {}
        }
    }

    val pickImage = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) viewModel.setPendingArt(uri)
    }

    TrackDetailContent(
        track = track,
        form = form,
        isDirty = isDirty,
        changes = changes,
        savingTags = tagSaveState is DetailSaveState.Saving,
        lyricsState = lyricsState,
        savingLyrics = lyricsSaveState is DetailSaveState.Saving,
        pendingArtUri = pendingArtUri,
        savingArt = artSaveState is DetailSaveState.Saving,
        snackbarHostState = snackbar,
        onBack = onBack,
        onSaveTags = viewModel::saveTags,
        onSetTitle = viewModel::setTitle,
        onSetArtist = viewModel::setArtist,
        onSetAlbum = viewModel::setAlbum,
        onSetAlbumArtist = viewModel::setAlbumArtist,
        onSetTrackNumber = viewModel::setTrackNumber,
        onSetDiscNumber = viewModel::setDiscNumber,
        onSetYear = viewModel::setYear,
        onSetGenre = viewModel::setGenre,
        onSetComposer = viewModel::setComposer,
        onSetComment = viewModel::setComment,
        onSetCompilation = viewModel::setCompilation,
        onFetchLyrics = viewModel::fetchLyrics,
        onSaveLyrics = viewModel::saveLyrics,
        onDismissLyrics = viewModel::dismissLyrics,
        manualLyrics = manualLyrics,
        onStartEditLyrics = viewModel::startEditLyrics,
        onManualLyricsChange = viewModel::setManualLyrics,
        onCancelEditLyrics = viewModel::cancelEditLyrics,
        onSaveManualLyrics = viewModel::saveManualLyrics,
        onPickArt = { pickImage.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly)) },
        onSaveArt = viewModel::saveArt,
        onClearPendingArt = viewModel::clearPendingArt,
        artFetchState = artFetchState,
        onFetchArt = viewModel::fetchArt,
        onSelectCandidate = viewModel::selectCandidate,
        onDismissFetchArt = viewModel::dismissFetchArt,
        onApplyPreview = viewModel::applyPreviewedArt,
        onBackToCandidates = viewModel::backToCandidates,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrackDetailContent(
    track: TrackEntity?,
    form: TagFormState,
    isDirty: Boolean,
    changes: List<FieldChange>,
    savingTags: Boolean,
    lyricsState: LyricsState,
    savingLyrics: Boolean,
    pendingArtUri: android.net.Uri?,
    savingArt: Boolean,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onSaveTags: () -> Unit,
    onSetTitle: (String) -> Unit,
    onSetArtist: (String) -> Unit,
    onSetAlbum: (String) -> Unit,
    onSetAlbumArtist: (String) -> Unit,
    onSetTrackNumber: (String) -> Unit,
    onSetDiscNumber: (String) -> Unit,
    onSetYear: (String) -> Unit,
    onSetGenre: (String) -> Unit,
    onSetComposer: (String) -> Unit,
    onSetComment: (String) -> Unit,
    onSetCompilation: (Boolean) -> Unit,
    onFetchLyrics: () -> Unit,
    onSaveLyrics: () -> Unit,
    onDismissLyrics: () -> Unit,
    manualLyrics: String?,
    onStartEditLyrics: () -> Unit,
    onManualLyricsChange: (String) -> Unit,
    onCancelEditLyrics: () -> Unit,
    onSaveManualLyrics: () -> Unit,
    onPickArt: () -> Unit,
    onSaveArt: () -> Unit,
    onClearPendingArt: () -> Unit,
    artFetchState: ArtFetchState,
    onFetchArt: () -> Unit,
    onSelectCandidate: (CoverArtCandidate) -> Unit,
    onDismissFetchArt: () -> Unit,
    onApplyPreview: () -> Unit,
    onBackToCandidates: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme
    val motion = LocalMotion.current

    Scaffold(
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        if (track == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), Alignment.Center) {
                CircularProgressIndicator(color = colors.primary)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            TrackHero(
                track = track,
                isDirty = isDirty,
                savingTags = savingTags,
                onBack = onBack,
                onSaveTags = onSaveTags,
            )

            Column(
                modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.md),
                verticalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                TrackStatusSummary(track = track, isDirty = isDirty, changeCount = changes.size)

                TrackQuickActions(
                    isDirty = isDirty,
                    savingTags = savingTags,
                    onReviewChanges = onSaveTags,
                    onPickArt = onPickArt,
                    onStartEditLyrics = onStartEditLyrics,
                )

                // ── Core tags ────────────────────────────────────────────────
                SectionLabel("Core tags", trailing = if (changes.isNotEmpty()) "${changes.size} dirty" else null)
                TagField("Title", form.title, onSetTitle, capitalization = KeyboardCapitalization.Words)
                TagField("Artist", form.artist, onSetArtist, capitalization = KeyboardCapitalization.Words)
                TagField("Genre", form.genre, onSetGenre)

                // ── Album tags ───────────────────────────────────────────────
                Spacer(Modifier.height(spacing.sm))
                SectionLabel("Album")
                TagField("Album", form.album, onSetAlbum, capitalization = KeyboardCapitalization.Words)
                TagField("Album artist", form.albumArtist, onSetAlbumArtist, capitalization = KeyboardCapitalization.Words)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                    TagField("Track #", form.trackNumber, onSetTrackNumber, KeyboardType.Number, modifier = Modifier.weight(1f))
                    TagField("Disc #", form.discNumber, onSetDiscNumber, KeyboardType.Number, modifier = Modifier.weight(1f))
                }
                TagField("Year", form.year, onSetYear, KeyboardType.Number)

                // ── More tags ────────────────────────────────────────────────
                Spacer(Modifier.height(spacing.sm))
                SectionLabel("More")
                TagField("Composer", form.composer, onSetComposer, capitalization = KeyboardCapitalization.Words)
                TagField("Comment", form.comment, onSetComment)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSetCompilation(!form.compilation) }
                        .padding(vertical = spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        "Part of a compilation",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    androidx.compose.material3.Switch(
                        checked = form.compilation,
                        onCheckedChange = onSetCompilation,
                    )
                }

                // ── Lyrics ───────────────────────────────────────────────────
                Spacer(Modifier.height(spacing.sm))
                SectionLabel("Lyrics")
                LyricsSection(
                    track = track,
                    lyricsState = lyricsState,
                    saving = savingLyrics,
                    onFetch = onFetchLyrics,
                    onSave = onSaveLyrics,
                    onDismiss = onDismissLyrics,
                    manualLyrics = manualLyrics,
                    onStartEdit = onStartEditLyrics,
                    onManualChange = onManualLyricsChange,
                    onCancelEdit = onCancelEditLyrics,
                    onSaveManual = onSaveManualLyrics,
                )

                // ── Art ──────────────────────────────────────────────────────
                Spacer(Modifier.height(spacing.sm))
                SectionLabel("Cover art")
                ArtSection(
                    track = track,
                    pendingArtUri = pendingArtUri,
                    savingArt = savingArt,
                    artFetchState = artFetchState,
                    onPickArt = onPickArt,
                    onSaveArt = onSaveArt,
                    onClearPendingArt = onClearPendingArt,
                    onFetchArt = onFetchArt,
                    onSelectCandidate = onSelectCandidate,
                    onDismissFetchArt = onDismissFetchArt,
                    onApplyPreview = onApplyPreview,
                    onBackToCandidates = onBackToCandidates,
                )

                // DESIGN.md motion: tween-only, 150-250ms ease-out. Default spring would bounce.
                AnimatedVisibility(
                    isDirty,
                    enter = expandVertically(animationSpec = motion.spec(motion.standard)),
                    exit = shrinkVertically(animationSpec = motion.spec(motion.fast)),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                        Spacer(Modifier.height(spacing.sm))
                        SectionLabel("Pending changes")
                        ChangesCard(changes, saving = savingTags, onWrite = onSaveTags)
                    }
                }

                Spacer(Modifier.height(spacing.xl))
            }
        }
    }
}

// ── Hero and summary ──────────────────────────────────────────────────────────

@Composable
private fun TrackHero(
    track: TrackEntity,
    isDirty: Boolean,
    savingTags: Boolean,
    onBack: () -> Unit,
    onSaveTags: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val heroModel = remember(track.thumbnailPath) { track.thumbnailPath?.let { File(it) } }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
    ) {
        ArtTileHero(
            model = heroModel,
            contentDescription = "Album art",
            shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp),
            modifier = Modifier.fillMaxSize(),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.58f),
                        1f to Color.Transparent,
                    ),
                ),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = spacing.sm, vertical = spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeroIconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                )
            }
            HeroIconButton(
                onClick = onSaveTags,
                enabled = isDirty && !savingTags,
                light = true,
            ) {
                if (savingTags) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.Black,
                    )
                } else {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = "Save tags",
                        tint = Color.Black,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color.Transparent,
                            0.36f to Color.Black.copy(alpha = 0.46f),
                            1f to Color.Black.copy(alpha = 0.92f),
                        ),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = spacing.lg, vertical = spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            Text(
                text = "TRACK · ${(track.artist ?: "Unknown artist").uppercase()}",
                style = AppTextStyles.eyebrow,
                color = Color.White.copy(alpha = 0.76f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = track.displayTitle(),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = buildString {
                    append(track.album?.takeIf { it.isNotBlank() } ?: track.albumLabel)
                    track.trackNumber?.let { append(" · track %02d".format(it)) }
                    track.year?.let { append(" · $it") }
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.76f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            FileMetaStrip(track, color = Color.White.copy(alpha = 0.76f))
        }
    }
}

@Composable
private fun HeroIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    light: Boolean = false,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.background(
            color = if (light) Color.White.copy(alpha = if (enabled) 0.94f else 0.58f) else Color.Black.copy(alpha = 0.32f),
            shape = androidx.compose.foundation.shape.CircleShape,
        ),
    ) {
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TrackStatusSummary(track: TrackEntity, isDirty: Boolean, changeCount: Int) {
    val spacing = LocalSpacing.current
    val sc = LocalStatusColors.current
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.xs),
        verticalArrangement = Arrangement.spacedBy(spacing.xs),
    ) {
        val lyricsLabel = when (track.lyricsStatus()) {
            LyricsStatus.NONE -> "No lyrics"
            LyricsStatus.SIDECAR_PLAIN -> "Plain lyrics"
            LyricsStatus.SIDECAR_SYNCED -> "Synced lyrics"
        }
        val lyricsColor = when (track.lyricsStatus()) {
            LyricsStatus.NONE -> sc.missing
            LyricsStatus.SIDECAR_PLAIN -> sc.warn
            LyricsStatus.SIDECAR_SYNCED -> sc.ok
        }
        SummaryChip(label = lyricsLabel, color = lyricsColor)
        if (isDirty) {
            SummaryChip(label = "$changeCount tag edit${if (changeCount == 1) "" else "s"}", color = sc.warn)
        }
        val artLabel = if (track.artWidth != null && track.artHeight != null) {
            "Art ${track.artWidth}px"
        } else if (track.hasEmbeddedArt) {
            "Art embedded"
        } else {
            "No art"
        }
        SummaryChip(label = artLabel, color = if (track.hasEmbeddedArt) sc.ok else sc.missing)
    }
}

@Composable
private fun SummaryChip(label: String, color: Color) {
    Surface(
        shape = LocalShapeScale.current.pill,
        color = color.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.26f)),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TrackQuickActions(
    isDirty: Boolean,
    savingTags: Boolean,
    onReviewChanges: () -> Unit,
    onPickArt: () -> Unit,
    onStartEditLyrics: () -> Unit,
) {
    val spacing = LocalSpacing.current
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        PrimaryButton(onClick = onReviewChanges, enabled = isDirty && !savingTags) {
            if (savingTags) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Rounded.Check, null, Modifier.size(18.dp))
            }
            Spacer(Modifier.size(spacing.xs))
            Text("Write file")
        }
        SecondaryButton(onClick = onPickArt) {
            Icon(Icons.Rounded.Image, null, Modifier.size(18.dp))
            Spacer(Modifier.size(spacing.xs))
            Text("Replace art")
        }
        GhostButton(onClick = onStartEditLyrics) {
            Icon(Icons.Rounded.Edit, null, Modifier.size(18.dp))
            Spacer(Modifier.size(spacing.xs))
            Text("Edit lyrics")
        }
    }
}

// ── Art section ───────────────────────────────────────────────────────────────

@Composable
private fun ArtSection(
    track: TrackEntity,
    pendingArtUri: android.net.Uri?,
    savingArt: Boolean,
    artFetchState: ArtFetchState,
    onPickArt: () -> Unit,
    onSaveArt: () -> Unit,
    onClearPendingArt: () -> Unit,
    onFetchArt: () -> Unit,
    onSelectCandidate: (CoverArtCandidate) -> Unit,
    onDismissFetchArt: () -> Unit,
    onApplyPreview: () -> Unit,
    onBackToCandidates: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme

    val currentThumb = track.thumbnailPath?.let { File(it) }

    // Current art thumbnail — always shown when available
    if (currentThumb != null || track.hasEmbeddedArt) {
        ArtTileHero(
            model = currentThumb,
            contentDescription = "Album art",
            modifier = Modifier.fillMaxWidth().height(200.dp),
        )
    }

    // Action area: fetch UI replaces gallery controls when non-Idle
    when (artFetchState) {
        is ArtFetchState.Idle -> {
            if (pendingArtUri != null) {
                ArtTileHero(
                    model = pendingArtUri,
                    contentDescription = "New art preview",
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                    if (savingArt) {
                        CircularProgressIndicator(Modifier.size(24.dp).align(Alignment.CenterVertically), strokeWidth = 2.dp)
                    } else {
                        PrimaryButton(onClick = onSaveArt, modifier = Modifier.weight(1f)) {
                            Text("Save new art")
                        }
                        OutlineButton(onClick = onClearPendingArt, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                    OutlineButton(onClick = onPickArt, modifier = Modifier.weight(1f)) {
                        Text(
                            "From gallery",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    SecondaryButton(onClick = onFetchArt, modifier = Modifier.weight(1f)) {
                        Text(
                            "From web",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        is ArtFetchState.Fetching -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
                modifier = Modifier.padding(vertical = spacing.sm),
            ) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = colors.primary)
                Text("Searching…", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }

        is ArtFetchState.Found -> {
            CandidateRow(
                candidates = artFetchState.candidates,
                onSelect = onSelectCandidate,
                onDismiss = onDismissFetchArt,
            )
        }

        is ArtFetchState.Downloading -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
                modifier = Modifier.padding(vertical = spacing.sm),
            ) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = colors.primary)
                Text("Downloading…", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }

        is ArtFetchState.Previewing -> {
            // Keep the candidate row visible behind the dialog
            CandidateRow(
                candidates = artFetchState.originalCandidates,
                onSelect = onSelectCandidate,
                onDismiss = onDismissFetchArt,
            )
            AlbumArtPreviewDialog(
                bytes = artFetchState.bytes,
                title = artFetchState.candidate.title,
                artist = artFetchState.candidate.artist,
                saving = savingArt,
                onApply = onApplyPreview,
                onBack = onBackToCandidates,
            )
        }

        is ArtFetchState.NotFound -> {
            Text(
                "No art found.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
            GhostButton(onClick = onDismissFetchArt, modifier = Modifier.fillMaxWidth()) {
                Text("Dismiss")
            }
        }

        is ArtFetchState.Error -> {
            Text(
                artFetchState.message,
                style = MaterialTheme.typography.bodySmall,
                color = colors.error,
            )
            GhostButton(onClick = onDismissFetchArt, modifier = Modifier.fillMaxWidth()) {
                Text("Dismiss")
            }
        }
    }
}

// ── Candidate row ─────────────────────────────────────────────────────────────

@Composable
private fun CandidateRow(
    candidates: List<CoverArtCandidate>,
    onSelect: (CoverArtCandidate) -> Unit,
    onDismiss: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        Text(
            "Tap to apply",
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            items(candidates, key = { it.mbid }) { candidate ->
                ArtTile(
                    model = candidate.thumbnailUrl,
                    contentDescription = "${candidate.title} by ${candidate.artist}",
                    size = ArtTileSize.Lg,
                    modifier = Modifier.clickable { onSelect(candidate) },
                )
            }
        }
        GhostButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel")
        }
    }
}

// ── Lyrics section ────────────────────────────────────────────────────────────

@Composable
private fun LyricsSection(
    track: TrackEntity,
    lyricsState: LyricsState,
    saving: Boolean,
    onFetch: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    manualLyrics: String?,
    onStartEdit: () -> Unit,
    onManualChange: (String) -> Unit,
    onCancelEdit: () -> Unit,
    onSaveManual: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme
    val sc = LocalStatusColors.current

    // Manual edit takes over the section when active.
    if (manualLyrics != null) {
        androidx.compose.material3.OutlinedTextField(
            value = manualLyrics,
            onValueChange = onManualChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            placeholder = { Text("Paste or type lyrics. Use [mm:ss.xx] line prefixes for synced.") },
            minLines = 6,
            maxLines = 16,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.surfaceVariant,
                unfocusedContainerColor = colors.surfaceVariant,
                focusedBorderColor = colors.secondary,
                unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
            ),
        )
        Spacer(Modifier.height(spacing.sm))
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            if (saving) {
                CircularProgressIndicator(
                    Modifier.size(24.dp).align(Alignment.CenterVertically),
                    strokeWidth = 2.dp,
                    color = colors.secondary,
                )
            } else {
                PrimaryButton(onClick = onSaveManual) {
                    Icon(Icons.Rounded.Check, null, Modifier.size(18.dp))
                    Spacer(Modifier.size(spacing.xs))
                    Text("Save lyrics")
                }
                GhostButton(onClick = onCancelEdit) { Text("Cancel") }
            }
        }
        return
    }

    val currentStatus = track.lyricsStatus()
    val statusText = when (currentStatus) {
        LyricsStatus.NONE -> "No lyrics file"
        LyricsStatus.SIDECAR_PLAIN -> "Plain .lrc sidecar"
        LyricsStatus.SIDECAR_SYNCED -> "Synced .lrc sidecar"
    }
    val statusColor = when (currentStatus) {
        LyricsStatus.NONE -> colors.onSurfaceVariant
        LyricsStatus.SIDECAR_PLAIN -> sc.warn
        LyricsStatus.SIDECAR_SYNCED -> sc.ok
    }

    Text(statusText, style = MaterialTheme.typography.bodySmall, color = statusColor)

    when (lyricsState) {
        is LyricsState.Idle, is LyricsState.NoResult, is LyricsState.Error -> {
            if (lyricsState is LyricsState.NoResult) {
                Text(
                    "No match found on LRCLIB.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
            if (lyricsState is LyricsState.Error) {
                Text(
                    (lyricsState as LyricsState.Error).message,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.error,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                SecondaryButton(onClick = onFetch, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.MusicNote, null, Modifier.size(18.dp))
                    Spacer(Modifier.size(spacing.xs))
                    Text("Fetch from LRCLIB")
                }
                GhostButton(onClick = onStartEdit) {
                    Icon(Icons.Rounded.Edit, null, Modifier.size(18.dp))
                    Spacer(Modifier.size(spacing.xs))
                    Text("Edit")
                }
            }
        }

        is LyricsState.Fetching -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
                modifier = Modifier.padding(vertical = spacing.sm),
            ) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = colors.primary)
                Text("Searching LRCLIB…", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }

        is LyricsState.Preview -> {
            LyricsPreviewCard(
                result = lyricsState.result,
                isSynced = lyricsState.isSynced,
                saving = saving,
                onSave = onSave,
                onDismiss = onDismiss,
            )
        }
    }
}

@Composable
private fun LyricsPreviewCard(
    result: LrclibResult,
    isSynced: Boolean,
    saving: Boolean,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme
    val sc = LocalStatusColors.current

    val previewText = (if (isSynced) result.syncedLyrics else result.plainLyrics) ?: ""
    val previewLines = previewText.lines().take(6).joinToString("\n")

    AppPanel(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = if (isSynced) "Synced lyrics" else "Plain lyrics",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSynced) sc.ok else sc.warn,
                )
                Text(
                    text = result.trackName,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(start = spacing.md),
                )
            }
            Hairline()
            Text(
                text = previewLines,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = colors.onSurface,
            )
            if (previewText.lines().size > 6) {
                Text("…", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                if (saving) {
                    CircularProgressIndicator(
                        Modifier.size(24.dp).align(Alignment.CenterVertically),
                        strokeWidth = 2.dp,
                    )
                } else {
                    PrimaryButton(onClick = onSave, modifier = Modifier.weight(1f)) { Text("Save lyrics") }
                    GhostButton(onClick = onDismiss) { Text("Dismiss") }
                }
            }
        }
    }
}

// ── Shared components ─────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String, trailing: String? = null) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
        )
        if (trailing != null) {
            Text(
                text = trailing.uppercase(),
                style = AppTextStyles.monoSmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
    Hairline()
}

@Composable
private fun TagField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    modifier: Modifier = Modifier,
) {
    AppTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            capitalization = capitalization,
            imeAction = ImeAction.Next,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

/**
 * File metadata strip — `FLAC · 38.4 MB · 4:15 · 1600 × 1600`, mono, fog
 * separators. Hardware-style facts before the workspace sections.
 */
@Composable
private fun FileMetaStrip(
    track: TrackEntity,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val parts = buildList {
        add(track.format.uppercase())
        add(dev.gitfudge.audora.util.MediaFormat.size(track.sizeBytes))
        track.durationMs?.let { add(dev.gitfudge.audora.util.MediaFormat.duration(it)) }
        if (track.artWidth != null && track.artHeight != null) {
            add("${track.artWidth} × ${track.artHeight}")
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = LocalSpacing.current.xs),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        parts.forEachIndexed { i, part ->
            if (i > 0) {
                Text(
                    "·",
                    style = dev.gitfudge.audora.ui.theme.AppTextStyles.monoSmall,
                    color = color.copy(alpha = 0.58f),
                )
            }
            Text(
                part,
                style = dev.gitfudge.audora.ui.theme.AppTextStyles.monoSmall,
                color = color,
            )
        }
    }
}


@Composable
private fun ChangesCard(
    changes: List<FieldChange>,
    saving: Boolean,
    onWrite: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme

    AppPanel(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(spacing.lg), verticalArrangement = Arrangement.spacedBy(spacing.md)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                Icon(
                    Icons.Rounded.Edit,
                    contentDescription = null,
                    tint = colors.secondary,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    "${changes.size} change${if (changes.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurface,
                )
                Text(
                    "— write to confirm",
                    style = dev.gitfudge.audora.ui.theme.AppTextStyles.monoSmall,
                    color = colors.onSurfaceVariant,
                )
            }
            changes.forEach { change -> DiffRow(change) }
            if (saving) {
                CircularProgressIndicator(
                    Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = colors.secondary,
                )
            } else {
                PrimaryButton(onClick = onWrite, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Rounded.Check, null, Modifier.size(18.dp))
                    Spacer(Modifier.size(spacing.xs))
                    Text("Write file")
                }
            }
        }
    }
}

/** The honest diff: uppercase field label, struck-through old, → new. */
@Composable
private fun DiffRow(change: FieldChange) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme
    val sc = LocalStatusColors.current
    androidx.compose.material3.Surface(
        shape = LocalShapeScale.current.sm,
        color = colors.background,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier.padding(horizontal = spacing.md, vertical = spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            Text(
                change.label.uppercase(),
                style = dev.gitfudge.audora.ui.theme.AppTextStyles.eyebrow,
                color = colors.onSurfaceVariant,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                Text(
                    text = change.old.ifBlank { "—" },
                    style = AppTextStyles.mono.copy(
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough,
                    ),
                    color = sc.missing,
                )
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = "to",
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    text = change.new.ifBlank { "—" },
                    style = AppTextStyles.mono.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                    color = sc.ok,
                )
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "Detail · with lyrics preview", showBackground = true)
@Composable
private fun DetailLyricsPreviewPreview() {
    AudoraTheme {
        TrackDetailContent(
            track = previewTrack("Song Without End", "Nils Frahm", "All Melody"),
            form = TagFormState(title = "Song Without End", artist = "Nils Frahm", album = "All Melody"),
            isDirty = false,
            changes = emptyList(),
            savingTags = false,
            lyricsState = LyricsState.Preview(
                result = LrclibResult(
                    trackName = "Song Without End",
                    artistName = "Nils Frahm",
                    syncedLyrics = "[00:12.00] First line of lyrics\n[00:15.40] Second line here\n[00:20.00] Third line follows",
                ),
                isSynced = true,
            ),
            savingLyrics = false,
            pendingArtUri = null,
            savingArt = false,
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onSaveTags = {},
            onSetTitle = {}, onSetArtist = {}, onSetAlbum = {}, onSetAlbumArtist = {},
            onSetTrackNumber = {}, onSetDiscNumber = {}, onSetYear = {}, onSetGenre = {},
            onSetComposer = {}, onSetComment = {}, onSetCompilation = {},
            onFetchLyrics = {}, onSaveLyrics = {}, onDismissLyrics = {},
            manualLyrics = null,
            onStartEditLyrics = {}, onManualLyricsChange = {},
            onCancelEditLyrics = {}, onSaveManualLyrics = {},
            onPickArt = {}, onSaveArt = {}, onClearPendingArt = {},
            artFetchState = ArtFetchState.Idle,
            onFetchArt = {}, onSelectCandidate = {}, onDismissFetchArt = {},
            onApplyPreview = {}, onBackToCandidates = {},
        )
    }
}
