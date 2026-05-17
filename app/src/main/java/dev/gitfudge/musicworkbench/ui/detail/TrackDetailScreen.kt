package dev.gitfudge.musicworkbench.ui.detail

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import dev.gitfudge.musicworkbench.data.art.CoverArtCandidate
import dev.gitfudge.musicworkbench.data.db.TrackEntity
import dev.gitfudge.musicworkbench.data.lyrics.LrclibResult
import dev.gitfudge.musicworkbench.domain.LyricsStatus
import dev.gitfudge.musicworkbench.domain.displayTitle
import dev.gitfudge.musicworkbench.ui.common.AlbumArtPreviewDialog
import dev.gitfudge.musicworkbench.domain.lyricsStatus
import dev.gitfudge.musicworkbench.ui.library.previewTrack
import dev.gitfudge.musicworkbench.ui.theme.LocalSpacing
import dev.gitfudge.musicworkbench.ui.theme.LocalStatusColors
import dev.gitfudge.musicworkbench.ui.theme.MusicWorkbenchTheme
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

    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(tagSaveState) {
        when (val s = tagSaveState) {
            is DetailSaveState.Done -> { snackbar.showSnackbar("Tags saved", duration = SnackbarDuration.Short); viewModel.dismissTagSave() }
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
        onFetchLyrics = viewModel::fetchLyrics,
        onSaveLyrics = viewModel::saveLyrics,
        onDismissLyrics = viewModel::dismissLyrics,
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
    onFetchLyrics: () -> Unit,
    onSaveLyrics: () -> Unit,
    onDismissLyrics: () -> Unit,
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

    Scaffold(
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = track?.displayTitle() ?: "Track",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (savingTags) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(end = spacing.sm),
                            strokeWidth = 2.dp,
                            color = colors.primary,
                        )
                    } else {
                        IconButton(onClick = onSaveTags, enabled = isDirty) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = "Save tags",
                                tint = if (isDirty) colors.primary else colors.onSurfaceVariant,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.onBackground,
                ),
            )
        },
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.lg, vertical = spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            // ── Art ──────────────────────────────────────────────────────────
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

            // ── Tags ─────────────────────────────────────────────────────────
            SectionLabel("Identity")
            TagField("Title", form.title, onSetTitle, capitalization = KeyboardCapitalization.Words)
            TagField("Artist", form.artist, onSetArtist, capitalization = KeyboardCapitalization.Words)
            TagField("Album", form.album, onSetAlbum, capitalization = KeyboardCapitalization.Words)
            TagField("Album artist", form.albumArtist, onSetAlbumArtist, capitalization = KeyboardCapitalization.Words)

            Spacer(Modifier.height(spacing.sm))
            SectionLabel("Track info")

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                TagField("Track #", form.trackNumber, onSetTrackNumber, KeyboardType.Number, modifier = Modifier.weight(1f))
                TagField("Disc #", form.discNumber, onSetDiscNumber, KeyboardType.Number, modifier = Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                TagField("Year", form.year, onSetYear, KeyboardType.Number, modifier = Modifier.weight(1f))
                TagField("Genre", form.genre, onSetGenre, modifier = Modifier.weight(1f))
            }

            AnimatedVisibility(isDirty, enter = expandVertically(), exit = shrinkVertically()) {
                ChangesCard(changes)
            }

            // ── Lyrics ───────────────────────────────────────────────────────
            Spacer(Modifier.height(spacing.sm))
            SectionLabel("Lyrics")
            LyricsSection(
                track = track,
                lyricsState = lyricsState,
                saving = savingLyrics,
                onFetch = onFetchLyrics,
                onSave = onSaveLyrics,
                onDismiss = onDismissLyrics,
            )

            Spacer(Modifier.height(spacing.xl))
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
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = colors.surfaceContainer,
            modifier = Modifier.fillMaxWidth().height(200.dp),
        ) {
            AsyncImage(
                model = currentThumb,
                contentDescription = "Album art",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    // Action area: fetch UI replaces gallery controls when non-Idle
    when (artFetchState) {
        is ArtFetchState.Idle -> {
            if (pendingArtUri != null) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = colors.surfaceContainer,
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                ) {
                    AsyncImage(
                        model = pendingArtUri,
                        contentDescription = "New art preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                    if (savingArt) {
                        CircularProgressIndicator(Modifier.size(24.dp).align(Alignment.CenterVertically), strokeWidth = 2.dp)
                    } else {
                        Button(onClick = onSaveArt, modifier = Modifier.weight(1f)) {
                            Text("Save new art")
                        }
                        OutlinedButton(onClick = onClearPendingArt, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                    OutlinedButton(onClick = onPickArt, modifier = Modifier.weight(1f)) {
                        Text(
                            "From gallery",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    FilledTonalButton(onClick = onFetchArt, modifier = Modifier.weight(1f)) {
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
            TextButton(onClick = onDismissFetchArt, modifier = Modifier.fillMaxWidth()) {
                Text("Dismiss")
            }
        }

        is ArtFetchState.Error -> {
            Text(
                artFetchState.message,
                style = MaterialTheme.typography.bodySmall,
                color = colors.error,
            )
            TextButton(onClick = onDismissFetchArt, modifier = Modifier.fillMaxWidth()) {
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
                CandidateThumbnail(candidate = candidate, onClick = { onSelect(candidate) })
            }
        }
        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("Cancel")
        }
    }
}

@Composable
private fun CandidateThumbnail(candidate: CoverArtCandidate, onClick: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .size(80.dp)
            .clickable(onClick = onClick),
    ) {
        AsyncImage(
            model = candidate.thumbnailUrl,
            contentDescription = "${candidate.title} by ${candidate.artist}",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
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
) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme
    val sc = LocalStatusColors.current

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
            FilledTonalButton(onClick = onFetch, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Rounded.MusicNote, null, Modifier.size(18.dp).padding(end = 4.dp))
                Text("Fetch from LRCLIB")
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

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = colors.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
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
            HorizontalDivider(color = colors.outlineVariant)
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
                    Button(onClick = onSave, modifier = Modifier.weight(1f)) { Text("Save lyrics") }
                    TextButton(onClick = onDismiss) { Text("Dismiss") }
                }
            }
        }
    }
}

// ── Shared components ─────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
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
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            capitalization = capitalization,
            imeAction = ImeAction.Next,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun ChangesCard(changes: List<FieldChange>) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme
    val sc = LocalStatusColors.current

    Surface(shape = MaterialTheme.shapes.medium, color = colors.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(spacing.lg), verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
            Text(
                "${changes.size} change${if (changes.size != 1) "s" else ""}",
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant,
            )
            changes.forEach { change ->
                Column {
                    Text(change.label, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                    if (change.old.isNotBlank()) {
                        Text(
                            "− ${change.old}",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = sc.missing,
                        )
                    }
                    if (change.new.isNotBlank()) {
                        Text(
                            "+ ${change.new}",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = sc.ok,
                        )
                    }
                }
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "Detail · with lyrics preview", showBackground = true, backgroundColor = 0xFF14161A)
@Composable
private fun DetailLyricsPreviewPreview() {
    MusicWorkbenchTheme {
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
            onFetchLyrics = {}, onSaveLyrics = {}, onDismissLyrics = {},
            onPickArt = {}, onSaveArt = {}, onClearPendingArt = {},
            artFetchState = ArtFetchState.Idle,
            onFetchArt = {}, onSelectCandidate = {}, onDismissFetchArt = {},
            onApplyPreview = {}, onBackToCandidates = {},
        )
    }
}
