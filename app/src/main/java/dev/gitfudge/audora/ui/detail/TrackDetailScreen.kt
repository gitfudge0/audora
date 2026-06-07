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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DriveFileRenameOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Search
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
import dev.gitfudge.audora.data.art.MetadataCandidate
import dev.gitfudge.audora.data.db.TrackEntity
import dev.gitfudge.audora.domain.FilenameParse
import dev.gitfudge.audora.domain.NamingPattern
import dev.gitfudge.audora.domain.TokenMapping
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
    val suggestSource by viewModel.suggestSource.collectAsStateWithLifecycle()
    val namingPattern by viewModel.namingPattern.collectAsStateWithLifecycle()
    val filenameParse by viewModel.filenameParse.collectAsStateWithLifecycle()
    val webQuery by viewModel.webQuery.collectAsStateWithLifecycle()
    val metadataFetchState by viewModel.metadataFetchState.collectAsStateWithLifecycle()

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
        suggestSource = suggestSource,
        namingPattern = namingPattern,
        filenameParse = filenameParse,
        webQuery = webQuery,
        metadataFetchState = metadataFetchState,
        onOpenSuggest = viewModel::openSuggest,
        onCloseSuggest = viewModel::closeSuggest,
        onSetNamingPattern = viewModel::setNamingPattern,
        onApplyFilenameParse = viewModel::applyFilenameParse,
        onSetWebQuery = viewModel::setWebQuery,
        onFetchMetadata = viewModel::fetchMetadata,
        onSelectMetadataCandidate = viewModel::applyMetadataCandidate,
        onDismissMetadata = viewModel::dismissMetadata,
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
    suggestSource: SuggestSource,
    namingPattern: NamingPattern,
    filenameParse: FilenameParse?,
    webQuery: String,
    metadataFetchState: MetadataFetchState,
    onOpenSuggest: (SuggestSource) -> Unit,
    onCloseSuggest: () -> Unit,
    onSetNamingPattern: (NamingPattern) -> Unit,
    onApplyFilenameParse: () -> Unit,
    onSetWebQuery: (String) -> Unit,
    onFetchMetadata: () -> Unit,
    onSelectMetadataCandidate: (MetadataCandidate) -> Unit,
    onDismissMetadata: () -> Unit,
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

                FormatMismatchWarning(track)

                TrackQuickActions(
                    isDirty = isDirty,
                    savingTags = savingTags,
                    onReviewChanges = onSaveTags,
                    onPickArt = onPickArt,
                    onStartEditLyrics = onStartEditLyrics,
                )

                // ── Suggest tags (adaptive populator) ────────────────────────
                SuggestTagsSection(
                    track = track,
                    coreTagsSparse = form.title.isBlank() || form.artist.isBlank(),
                    source = suggestSource,
                    namingPattern = namingPattern,
                    filenameParse = filenameParse,
                    webQuery = webQuery,
                    metadataFetchState = metadataFetchState,
                    onOpenSuggest = onOpenSuggest,
                    onCloseSuggest = onCloseSuggest,
                    onSetNamingPattern = onSetNamingPattern,
                    onApplyFilenameParse = onApplyFilenameParse,
                    onSetWebQuery = onSetWebQuery,
                    onFetchMetadata = onFetchMetadata,
                    onSelectMetadataCandidate = onSelectMetadataCandidate,
                    onDismissMetadata = onDismissMetadata,
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

/**
 * Shown when the file's real container disagrees with its name extension
 * (detected at scan). Reassures that editing is still safe — the write path
 * uses the real format automatically.
 */
@Composable
private fun FormatMismatchWarning(track: TrackEntity) {
    val detected = track.detectedFormat ?: return
    val spacing = LocalSpacing.current
    val sc = LocalStatusColors.current
    val nameExt = track.displayName.substringAfterLast('.', "").uppercase().ifEmpty { "?" }

    Surface(
        shape = LocalShapeScale.current.md,
        color = sc.warn.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, sc.warn.copy(alpha = 0.26f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(spacing.md),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            Icon(
                Icons.Rounded.Warning,
                contentDescription = null,
                tint = sc.warn,
                modifier = Modifier.size(18.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                Text(
                    "Wrong file extension",
                    style = MaterialTheme.typography.labelLarge,
                    color = sc.warn,
                )
                Text(
                    "Named .${nameExt.lowercase()} but the audio is actually $detected. " +
                        "Editing tags, lyrics and art is still safe — Audora writes using the real format.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
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

// ── Suggest tags ──────────────────────────────────────────────────────────────

/**
 * Adaptive entry point + workspace for populating tag fields from the filename
 * or the web. Loud sodium-washed prompt banner when core tags are empty/sparse;
 * a quiet collapsed row when the track is already tagged. Both sources are pure
 * populators — they set the existing form fields, which dirties the standard
 * pending-changes diff. Nothing is written until the user presses "Write file".
 */
@Composable
private fun SuggestTagsSection(
    track: TrackEntity,
    coreTagsSparse: Boolean,
    source: SuggestSource,
    namingPattern: NamingPattern,
    filenameParse: FilenameParse?,
    webQuery: String,
    metadataFetchState: MetadataFetchState,
    onOpenSuggest: (SuggestSource) -> Unit,
    onCloseSuggest: () -> Unit,
    onSetNamingPattern: (NamingPattern) -> Unit,
    onApplyFilenameParse: () -> Unit,
    onSetWebQuery: (String) -> Unit,
    onFetchMetadata: () -> Unit,
    onSelectMetadataCandidate: (MetadataCandidate) -> Unit,
    onDismissMetadata: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme
    val open = source != SuggestSource.NONE
    val prompt = coreTagsSparse && !open

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = LocalShapeScale.current.lg,
        // Loud sodium wash only as a prompt; quiet otherwise.
        color = if (prompt) colors.primary.copy(alpha = 0.10f) else colors.surfaceVariant,
        contentColor = colors.onSurface,
        border = if (prompt) BorderStroke(1.dp, colors.primary.copy(alpha = 0.34f)) else null,
    ) {
        Column(Modifier.fillMaxWidth()) {
            when {
                // Collapsed, already-tagged → quiet entry row.
                !open && !coreTagsSparse -> SuggestQuietRow(onClick = { onOpenSuggest(SuggestSource.FILENAME) })
                // Collapsed, sparse → loud prompt banner.
                !open -> SuggestPromptHead(coreTagsSparse = coreTagsSparse)
                // Open → header showing the source toggle is handled below.
                else -> {}
            }

            if (open || coreTagsSparse) {
                SuggestSourceToggle(
                    source = if (open) source else SuggestSource.NONE,
                    onOpenSuggest = onOpenSuggest,
                    promptStyle = coreTagsSparse && !open,
                )
            }

            // ── Filename workspace ──────────────────────────────────────────
            AnimatedVisibility(source == SuggestSource.FILENAME && filenameParse != null) {
                if (filenameParse != null) {
                    FilenameWorkspace(
                        rawName = track.displayName,
                        parse = filenameParse,
                        selectedPattern = namingPattern,
                        onSelectPattern = onSetNamingPattern,
                        onApply = onApplyFilenameParse,
                    )
                }
            }

            // ── Web workspace ───────────────────────────────────────────────
            AnimatedVisibility(source == SuggestSource.WEB) {
                WebLookupWorkspace(
                    query = webQuery,
                    fetchState = metadataFetchState,
                    onQueryChange = onSetWebQuery,
                    onSearch = onFetchMetadata,
                    onSelect = onSelectMetadataCandidate,
                    onDismiss = onDismissMetadata,
                )
            }
        }
    }
}

@Composable
private fun SuggestPromptHead(coreTagsSparse: Boolean) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().padding(start = spacing.lg, top = spacing.lg, end = spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Box(
            Modifier
                .size(34.dp)
                .background(colors.primary, androidx.compose.foundation.shape.RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.AutoAwesome, null, tint = colors.onPrimary, modifier = Modifier.size(18.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
            Text(
                "This track has no tags",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurface,
            )
            Text(
                "Only the filename is set. Fill title, artist and more from the name, " +
                    "or look it up online. Nothing is written until you confirm.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SuggestQuietRow(onClick: () -> Unit) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(spacing.md),
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(30.dp)
                .background(colors.surfaceVariant, androidx.compose.foundation.shape.RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.AutoAwesome, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
        }
        Column(Modifier.weight(1f)) {
            Text("Suggest tags", style = MaterialTheme.typography.bodyLarge, color = colors.onSurface)
            Text(
                "Re-fill from filename or look up online",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = colors.outline)
    }
}

@Composable
private fun SuggestSourceToggle(
    source: SuggestSource,
    onOpenSuggest: (SuggestSource) -> Unit,
    promptStyle: Boolean,
) {
    val spacing = LocalSpacing.current
    Row(
        Modifier.fillMaxWidth().padding(spacing.md),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        SourceButton(
            label = "From filename",
            icon = Icons.Rounded.DriveFileRenameOutline,
            selected = source == SuggestSource.FILENAME,
            onClick = { onOpenSuggest(SuggestSource.FILENAME) },
            modifier = Modifier.weight(1f),
        )
        SourceButton(
            label = "From the web",
            icon = Icons.Rounded.Public,
            selected = source == SuggestSource.WEB,
            onClick = { onOpenSuggest(SuggestSource.WEB) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SourceButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val spacing = LocalSpacing.current
    Surface(
        onClick = onClick,
        shape = LocalShapeScale.current.md,
        color = if (selected) colors.onSurface else colors.surface,
        border = BorderStroke(1.dp, if (selected) colors.onSurface else colors.outline),
        modifier = modifier.height(46.dp),
    ) {
        Row(
            Modifier.fillMaxSize().padding(horizontal = spacing.md),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                null,
                modifier = Modifier.size(18.dp),
                tint = if (selected) colors.surface else colors.onSurfaceVariant,
            )
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) colors.surface else colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun FilenameWorkspace(
    rawName: String,
    parse: FilenameParse,
    selectedPattern: NamingPattern,
    onSelectPattern: (NamingPattern) -> Unit,
    onApply: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxWidth().padding(start = spacing.md, end = spacing.md, bottom = spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Hairline()
        // Raw filename, mono.
        Surface(
            shape = LocalShapeScale.current.sm,
            color = colors.surfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                rawName,
                style = AppTextStyles.monoSmall,
                color = colors.onSurface,
                modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.sm),
            )
        }

        Text("NAMING PATTERN", style = AppTextStyles.eyebrow, color = colors.onSurfaceVariant)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            items(NamingPattern.entries.toList(), key = { it.name }) { pattern ->
                PatternChip(
                    label = pattern.label,
                    selected = pattern == selectedPattern,
                    onClick = { onSelectPattern(pattern) },
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
            parse.mappings.forEach { TokenMapRow(it) }
        }

        val fieldCount = parse.fields.size
        PrimaryButton(
            onClick = onApply,
            enabled = fieldCount > 0,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Rounded.Check, null, Modifier.size(18.dp))
            Spacer(Modifier.size(spacing.xs))
            Text("Add $fieldCount field${if (fieldCount == 1) "" else "s"} to review")
        }
    }
}

@Composable
private fun PatternChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val sc = LocalStatusColors.current
    Surface(
        onClick = onClick,
        shape = LocalShapeScale.current.pill,
        color = if (selected) colors.primary.copy(alpha = 0.12f) else colors.surfaceVariant,
        border = BorderStroke(1.dp, if (selected) colors.primary.copy(alpha = 0.5f) else colors.outline),
    ) {
        Text(
            label,
            style = AppTextStyles.monoSmall,
            color = if (selected) colors.primary else colors.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            maxLines = 1,
        )
    }
}

@Composable
private fun TokenMapRow(mapping: TokenMapping) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme
    val ignored = mapping.fieldLabel == null
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = LocalShapeScale.current.sm,
            color = if (ignored) colors.surfaceVariant else colors.onSurface,
            modifier = Modifier.weight(0.45f, fill = false),
        ) {
            Text(
                mapping.token,
                style = AppTextStyles.monoSmall.copy(
                    textDecoration = if (ignored) {
                        androidx.compose.ui.text.style.TextDecoration.LineThrough
                    } else {
                        null
                    },
                ),
                color = if (ignored) colors.onSurfaceVariant else colors.surface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            )
        }
        Icon(
            Icons.AutoMirrored.Rounded.ArrowForward,
            null,
            tint = colors.outline,
            modifier = Modifier.size(15.dp),
        )
        if (ignored) {
            Text("Ignored", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${mapping.fieldLabel}", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                Text(
                    mapping.value.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun WebLookupWorkspace(
    query: String,
    fetchState: MetadataFetchState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSelect: (MetadataCandidate) -> Unit,
    onDismiss: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxWidth().padding(start = spacing.md, end = spacing.md, bottom = spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        Hairline()
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            AppTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = "Search MusicBrainz",
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.weight(1f),
            )
            SecondaryButton(onClick = onSearch, enabled = query.isNotBlank()) {
                Icon(Icons.Rounded.Search, null, Modifier.size(18.dp))
            }
        }

        when (fetchState) {
            is MetadataFetchState.Idle -> {}
            is MetadataFetchState.Fetching -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.md),
                ) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = colors.primary)
                    Text("Searching…", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
            is MetadataFetchState.Found -> {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    // Fixed-height, internally scrolling list so a full 10 results
                    // never push the rest of the screen off-screen.
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().height(296.dp),
                        verticalArrangement = Arrangement.spacedBy(spacing.sm),
                    ) {
                        itemsIndexed(
                            fetchState.candidates,
                            key = { i, c -> "$i-${c.recordingMbid}" },
                        ) { i, candidate ->
                            MetadataCandidateRow(candidate, isBest = i == 0, onClick = { onSelect(candidate) })
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(Icons.Rounded.Public, null, Modifier.size(15.dp), tint = colors.onSurfaceVariant)
                        Text(
                            "Metadata from MusicBrainz · tap a result to review",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
            }
            is MetadataFetchState.NotFound -> {
                Text("No match found on MusicBrainz.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                GhostButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Dismiss") }
            }
            is MetadataFetchState.Error -> {
                Text(fetchState.message, style = MaterialTheme.typography.bodySmall, color = colors.error)
                GhostButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Dismiss") }
            }
        }
    }
}

@Composable
private fun MetadataCandidateRow(
    candidate: MetadataCandidate,
    isBest: Boolean,
    onClick: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        shape = LocalShapeScale.current.md,
        color = colors.surface,
        border = BorderStroke(1.dp, if (isBest) colors.primary.copy(alpha = 0.38f) else colors.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ArtTile(
                model = candidate.thumbnailUrl,
                contentDescription = null,
                size = ArtTileSize.Md,
            )
            Column(Modifier.weight(1f)) {
                Text(
                    candidate.title.ifBlank { "Untitled" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    listOf(candidate.artist, candidate.album.ifBlank { "Single" }, candidate.year)
                        .filter { it.isNotBlank() }
                        .joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (isBest) {
                Text(
                    "BEST",
                    style = AppTextStyles.eyebrow,
                    color = colors.primary,
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
            suggestSource = SuggestSource.NONE,
            namingPattern = NamingPattern.TRACK_ARTIST_TITLE,
            filenameParse = null,
            webQuery = "",
            metadataFetchState = MetadataFetchState.Idle,
            onOpenSuggest = {}, onCloseSuggest = {}, onSetNamingPattern = {},
            onApplyFilenameParse = {}, onSetWebQuery = {}, onFetchMetadata = {},
            onSelectMetadataCandidate = {}, onDismissMetadata = {},
        )
    }
}

@Preview(name = "Detail · suggest from filename", showBackground = true)
@Composable
private fun DetailSuggestFilenamePreview() {
    AudoraTheme {
        TrackDetailContent(
            track = previewTrack("04 - Luna Rail - Night Bus.flac", null, null),
            form = TagFormState(),
            isDirty = false,
            changes = emptyList(),
            savingTags = false,
            lyricsState = LyricsState.Idle,
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
            suggestSource = SuggestSource.FILENAME,
            namingPattern = NamingPattern.TRACK_ARTIST_TITLE,
            filenameParse = dev.gitfudge.audora.domain.FilenameTags.parse(
                "04 - Luna Rail - Night Bus.flac",
                NamingPattern.TRACK_ARTIST_TITLE,
            ),
            webQuery = "",
            metadataFetchState = MetadataFetchState.Idle,
            onOpenSuggest = {}, onCloseSuggest = {}, onSetNamingPattern = {},
            onApplyFilenameParse = {}, onSetWebQuery = {}, onFetchMetadata = {},
            onSelectMetadataCandidate = {}, onDismissMetadata = {},
        )
    }
}
