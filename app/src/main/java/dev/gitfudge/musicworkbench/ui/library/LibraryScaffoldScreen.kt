package dev.gitfudge.musicworkbench.ui.library

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gitfudge.musicworkbench.R
import dev.gitfudge.musicworkbench.data.db.TrackEntity
import dev.gitfudge.musicworkbench.data.scan.ScanResult
import dev.gitfudge.musicworkbench.ui.common.AlbumArtPreviewDialog
import dev.gitfudge.musicworkbench.domain.LibraryFilter
import dev.gitfudge.musicworkbench.domain.LibrarySort
import dev.gitfudge.musicworkbench.ui.theme.LocalSpacing
import dev.gitfudge.musicworkbench.ui.theme.MusicWorkbenchTheme

@Composable
fun LibraryScaffoldScreen(
    folderLabel: String,
    onChangeFolder: () -> Unit,
    onTrackClick: (documentUri: String) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val scanState by viewModel.scanState.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val sort by viewModel.sort.collectAsStateWithLifecycle()
    val tracks by viewModel.tracks.collectAsStateWithLifecycle()
    val lowResPx by viewModel.lowResThresholdPx.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val groupedTracks by viewModel.groupedTracks.collectAsStateWithLifecycle()
    val selectedUris by viewModel.selectedUris.collectAsStateWithLifecycle()
    val isSelecting by viewModel.isSelecting.collectAsStateWithLifecycle()
    val batchFetchState by viewModel.batchFetchState.collectAsStateWithLifecycle()
    val downloadingUris by viewModel.downloadingUris.collectAsStateWithLifecycle()
    val downloadEntries by viewModel.downloadEntries.collectAsStateWithLifecycle()
    val batchArtFetchState by viewModel.batchArtFetchState.collectAsStateWithLifecycle()
    val downloadingArtUris by viewModel.downloadingArtUris.collectAsStateWithLifecycle()
    val artDownloadEntries by viewModel.artDownloadEntries.collectAsStateWithLifecycle()
    val albumPicker by viewModel.albumPicker.collectAsStateWithLifecycle()
    val albumPreview by viewModel.albumPreview.collectAsStateWithLifecycle()
    val previewDownloading by viewModel.previewDownloading.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(batchFetchState) {
        if (batchFetchState is BatchFetchState.Done) {
            val s = batchFetchState as BatchFetchState.Done
            snackbarHostState.showSnackbar(
                "Saved ${s.saved} · No match ${s.noMatch} · Failed ${s.failed}",
            )
            viewModel.dismissBatchFetch()
        }
    }

    LaunchedEffect(batchArtFetchState) {
        if (batchArtFetchState is BatchFetchState.Done) {
            val s = batchArtFetchState as BatchFetchState.Done
            snackbarHostState.showSnackbar(
                "Art saved ${s.saved} · No match ${s.noMatch} · Failed ${s.failed}",
            )
            viewModel.dismissBatchArtFetch()
        }
    }

    LibraryScaffoldContent(
        folderLabel = folderLabel,
        onChangeFolder = onChangeFolder,
        onTrackClick = onTrackClick,
        scanState = scanState,
        filter = filter,
        sort = sort,
        tracks = tracks,
        lowResThresholdPx = lowResPx,
        onFilterChange = { viewModel.setFilter(it) },
        onSortChange = { viewModel.setSort(it) },
        onRescan = { viewModel.rescan() },
        viewMode = viewMode,
        groupedTracks = groupedTracks,
        selectedUris = selectedUris,
        isSelecting = isSelecting,
        batchFetchState = batchFetchState,
        downloadingUris = downloadingUris,
        downloadEntries = downloadEntries,
        batchArtFetchState = batchArtFetchState,
        downloadingArtUris = downloadingArtUris,
        artDownloadEntries = artDownloadEntries,
        onToggleViewMode = { viewModel.toggleViewMode() },
        onEnterSelection = { viewModel.enterSelectionWith(it) },
        onToggleSelection = { viewModel.toggleSelection(it) },
        onClearSelection = { viewModel.clearSelection() },
        onFetchLyrics = { viewModel.batchFetchLyrics() },
        onFetchArt = { viewModel.batchFetchArt() },
        onClearDownloads = { viewModel.clearDownloadEntries() },
        onClearArtDownloads = { viewModel.clearArtDownloadEntries() },
        snackbarHostState = snackbarHostState,
    )

    albumPicker?.let { picker ->
        AlbumArtPickerSheet(
            state = picker,
            onPick = { viewModel.pickAlbumArt(it) },
            onSkip = { viewModel.skipAlbumArt() },
            downloadingCandidate = previewDownloading,
        )
    }

    albumPreview?.let { preview ->
        AlbumArtPreviewDialog(
            bytes = preview.bytes,
            title = preview.candidate.title,
            artist = preview.candidate.artist,
            saving = false,
            onApply = { viewModel.confirmAlbumArt() },
            onBack = { viewModel.cancelAlbumPreview() },
            subtitle = "${preview.albumName} · ${preview.trackCount} track${if (preview.trackCount == 1) "" else "s"}",
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun LibraryScaffoldContent(
    folderLabel: String,
    onChangeFolder: () -> Unit,
    onTrackClick: (documentUri: String) -> Unit,
    scanState: ScanState,
    filter: LibraryFilter,
    sort: LibrarySort,
    tracks: List<TrackEntity>,
    lowResThresholdPx: Int,
    onFilterChange: (LibraryFilter) -> Unit,
    onSortChange: (LibrarySort) -> Unit,
    onRescan: () -> Unit,
    viewMode: LibraryViewMode,
    groupedTracks: List<Pair<String, List<TrackEntity>>>,
    selectedUris: Set<String>,
    isSelecting: Boolean,
    batchFetchState: BatchFetchState,
    downloadingUris: Set<String>,
    downloadEntries: List<DownloadEntry>,
    batchArtFetchState: BatchFetchState,
    downloadingArtUris: Set<String>,
    artDownloadEntries: List<DownloadEntry>,
    onToggleViewMode: () -> Unit,
    onEnterSelection: (String) -> Unit,
    onToggleSelection: (String) -> Unit,
    onClearSelection: () -> Unit,
    onFetchLyrics: () -> Unit,
    onFetchArt: () -> Unit,
    onClearDownloads: () -> Unit,
    onClearArtDownloads: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme

    var showDownloadSheet by remember { mutableStateOf(false) }
    var showArtDownloadSheet by remember { mutableStateOf(false) }
    val isDownloading = batchFetchState is BatchFetchState.Running
    val isArtDownloading = batchArtFetchState is BatchFetchState.Running

    fun clickFor(track: TrackEntity): () -> Unit = {
        if (isSelecting) onToggleSelection(track.documentUri) else onTrackClick(track.documentUri)
    }
    fun longClickFor(track: TrackEntity): () -> Unit = {
        if (!isSelecting) onEnterSelection(track.documentUri)
    }

    if (showDownloadSheet) {
        LyricsDownloadSheet(
            entries = downloadEntries,
            batchFetchState = batchFetchState,
            onDismiss = { showDownloadSheet = false },
            onClear = { onClearDownloads(); showDownloadSheet = false },
        )
    }

    if (showArtDownloadSheet) {
        ArtDownloadSheet(
            entries = artDownloadEntries,
            batchFetchState = batchArtFetchState,
            onDismiss = { showArtDownloadSheet = false },
            onClear = { onClearArtDownloads(); showArtDownloadSheet = false },
        )
    }

    Scaffold(
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.library_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                actions = {
                    // Lyrics downloads button
                    if (downloadEntries.isNotEmpty() || isDownloading) {
                        IconButton(onClick = { showDownloadSheet = true }) {
                            BadgedBox(
                                badge = {
                                    if (isDownloading) {
                                        Badge(containerColor = colors.primary)
                                    }
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Download,
                                    contentDescription = stringResource(R.string.downloads_title),
                                    tint = if (isDownloading) colors.primary else colors.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    // Art downloads button
                    if (artDownloadEntries.isNotEmpty() || isArtDownloading) {
                        IconButton(onClick = { showArtDownloadSheet = true }) {
                            BadgedBox(
                                badge = {
                                    if (isArtDownloading) {
                                        Badge(containerColor = colors.primary)
                                    }
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Image,
                                    contentDescription = stringResource(R.string.art_downloads_title),
                                    tint = if (isArtDownloading) colors.primary else colors.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    IconButton(onClick = onToggleViewMode) {
                        Icon(
                            imageVector = if (viewMode == LibraryViewMode.FLAT) Icons.Rounded.FolderOpen else Icons.AutoMirrored.Rounded.ViewList,
                            contentDescription = stringResource(
                                if (viewMode == LibraryViewMode.FLAT) R.string.library_view_tree else R.string.library_view_flat,
                            ),
                            tint = colors.onSurfaceVariant,
                        )
                    }
                    if (scanState !is ScanState.Running) {
                        IconButton(onClick = onRescan) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = stringResource(R.string.library_rescan),
                                tint = colors.onSurfaceVariant,
                            )
                        }
                    }
                    IconButton(onClick = onChangeFolder) {
                        Icon(
                            imageVector = Icons.Rounded.SwapHoriz,
                            contentDescription = stringResource(R.string.library_change_folder),
                            tint = colors.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.onBackground,
                ),
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = isSelecting,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
            ) {
                SelectionBar(
                    selectedCount = selectedUris.size,
                    batchFetchState = batchFetchState,
                    batchArtFetchState = batchArtFetchState,
                    onFetchLyrics = onFetchLyrics,
                    onFetchArt = onFetchArt,
                    onClearSelection = onClearSelection,
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            FolderStrip(
                folderLabel = folderLabel,
                modifier = Modifier.padding(horizontal = spacing.lg),
            )

            ScanProgressRow(
                scanState = scanState,
                modifier = Modifier.padding(horizontal = spacing.lg),
            )

            FilterSortBar(
                filter = filter,
                sort = sort,
                onFilterChange = onFilterChange,
                onSortChange = onSortChange,
            )

            HorizontalDivider(color = colors.outlineVariant)

            when {
                tracks.isEmpty() && scanState is ScanState.Running -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = colors.primary,
                            modifier = Modifier.size(40.dp),
                        )
                    }
                }

                tracks.isEmpty() -> EmptyState(
                    isFiltered = filter != LibraryFilter.ALL,
                    modifier = Modifier.fillMaxSize(),
                )

                viewMode == LibraryViewMode.TREE -> LazyColumn(Modifier.fillMaxSize()) {
                    groupedTracks.forEach { (path, groupTracks) ->
                        stickyHeader(key = "header_$path") {
                            FolderHeader(path)
                        }
                        items(groupTracks, key = { it.documentUri }) { track ->
                            TrackListItem(
                                track = track,
                                lowResThresholdPx = lowResThresholdPx,
                                onClick = clickFor(track),
                                onLongClick = longClickFor(track),
                                selected = track.documentUri in selectedUris,
                                selectionMode = isSelecting,
                                isDownloadingLyrics = track.documentUri in downloadingUris,
                                isDownloadingArt = track.documentUri in downloadingArtUris,
                            )
                            HorizontalDivider(
                                color = colors.outlineVariant,
                                modifier = Modifier.padding(start = spacing.lg + 48.dp + spacing.md),
                            )
                        }
                    }
                }

                else -> LazyColumn(Modifier.fillMaxSize()) {
                    items(tracks, key = { it.documentUri }) { track ->
                        TrackListItem(
                            track = track,
                            lowResThresholdPx = lowResThresholdPx,
                            onClick = clickFor(track),
                            onLongClick = longClickFor(track),
                            selected = track.documentUri in selectedUris,
                            selectionMode = isSelecting,
                            isDownloadingLyrics = track.documentUri in downloadingUris,
                            isDownloadingArt = track.documentUri in downloadingArtUris,
                        )
                        HorizontalDivider(
                            color = colors.outlineVariant,
                            modifier = Modifier.padding(start = spacing.lg + 48.dp + spacing.md),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderHeader(path: String) {
    val colors = MaterialTheme.colorScheme
    val spacing = LocalSpacing.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainer)
            .padding(horizontal = spacing.lg, vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Icon(
            imageVector = Icons.Rounded.Folder,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = path.substringAfterLast('/').ifEmpty { path },
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurface,
        )
    }
}

@Composable
private fun SelectionBar(
    selectedCount: Int,
    batchFetchState: BatchFetchState,
    batchArtFetchState: BatchFetchState,
    onFetchLyrics: () -> Unit,
    onFetchArt: () -> Unit,
    onClearSelection: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val spacing = LocalSpacing.current
    Surface(
        tonalElevation = 3.dp,
        color = colors.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = spacing.lg, vertical = spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            // Top row: count + clear
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.selection_count, selectedCount),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onClearSelection) {
                    Icon(
                        imageVector = Icons.Rounded.Clear,
                        contentDescription = stringResource(R.string.selection_clear),
                        tint = colors.onSurfaceVariant,
                    )
                }
            }
            // Bottom row: actions or progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                when {
                    batchFetchState is BatchFetchState.Running -> {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text(
                            text = "Lyrics ${batchFetchState.done}/${batchFetchState.total}",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurfaceVariant,
                        )
                    }
                    batchArtFetchState is BatchFetchState.Running -> {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text(
                            text = "Art ${batchArtFetchState.done}/${batchArtFetchState.total}",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurfaceVariant,
                        )
                    }
                    else -> {
                        Button(
                            onClick = onFetchLyrics,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(stringResource(R.string.selection_fetch_lyrics))
                        }
                        Button(
                            onClick = onFetchArt,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(stringResource(R.string.selection_fetch_art))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderStrip(folderLabel: String, modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceContainer,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            Icon(
                imageVector = Icons.Rounded.Folder,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(20.dp),
            )
            Column {
                Text(
                    text = stringResource(R.string.library_folder_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                )
                Text(
                    text = folderLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurface,
                )
            }
        }
    }
}

@Composable
private fun ScanProgressRow(scanState: ScanState, modifier: Modifier = Modifier) {
    when (scanState) {
        is ScanState.Idle -> {}

        is ScanState.Running -> Column(modifier.padding(vertical = 6.dp)) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainer,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (scanState.count == 0) {
                    stringResource(R.string.scan_running)
                } else {
                    stringResource(R.string.scan_running_count, scanState.count, scanState.label)
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        is ScanState.Done -> Text(
            text = stringResource(
                R.string.scan_done,
                scanState.result.scanned,
                scanState.result.upserted,
                scanState.result.removed,
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(vertical = 6.dp),
        )

        is ScanState.Failed -> Text(
            text = stringResource(R.string.scan_failed, scanState.cause),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
            modifier = modifier.padding(vertical = 6.dp),
        )
    }
}

@Composable
private fun FilterSortBar(
    filter: LibraryFilter,
    sort: LibrarySort,
    onFilterChange: (LibraryFilter) -> Unit,
    onSortChange: (LibrarySort) -> Unit,
) {
    val spacing = LocalSpacing.current
    var showSortMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = spacing.lg, vertical = spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LibraryFilter.entries.forEach { f ->
            FilterChip(
                selected = filter == f,
                onClick = { onFilterChange(f) },
                label = { Text(stringResource(f.labelRes())) },
            )
        }

        Box {
            AssistChip(
                onClick = { showSortMenu = true },
                label = { Text(stringResource(sort.labelRes())) },
                trailingIcon = {
                    Icon(Icons.Rounded.ArrowDropDown, null, Modifier.size(18.dp))
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            )
            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false },
            ) {
                LibrarySort.entries.forEach { s ->
                    DropdownMenuItem(
                        text = { Text(stringResource(s.labelRes())) },
                        onClick = { onSortChange(s); showSortMenu = false },
                        leadingIcon = if (s == sort) ({
                            Icon(
                                Icons.Rounded.FilterList,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        }) else null,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(isFiltered: Boolean, modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier.padding(horizontal = spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = if (isFiltered) Icons.Rounded.FilterList else Icons.Rounded.GraphicEq,
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.height(spacing.lg))
        Text(
            text = stringResource(R.string.library_empty_title),
            style = MaterialTheme.typography.titleMedium,
            color = colors.onBackground,
        )
        Spacer(Modifier.height(spacing.sm))
        Text(
            text = stringResource(
                if (isFiltered) R.string.library_filter_empty_body else R.string.library_empty_body,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 340.dp),
        )
    }
}

@StringRes
private fun LibraryFilter.labelRes(): Int = when (this) {
    LibraryFilter.ALL -> R.string.filter_all
    LibraryFilter.MISSING_ART -> R.string.filter_missing_art
    LibraryFilter.LOW_RES_ART -> R.string.filter_low_res_art
    LibraryFilter.NO_LYRICS -> R.string.filter_no_lyrics
    LibraryFilter.INCOMPLETE_TAGS -> R.string.filter_incomplete_tags
    LibraryFilter.UNKNOWN_ARTIST -> R.string.filter_unknown_artist
}

@StringRes
private fun LibrarySort.labelRes(): Int = when (this) {
    LibrarySort.ALBUM -> R.string.sort_album
    LibrarySort.TITLE -> R.string.sort_title
    LibrarySort.ARTIST -> R.string.sort_artist
    LibrarySort.RECENTLY_MODIFIED -> R.string.sort_recently_modified
}

// ── Previews ────────────────────────────────────────────────────────────────

@Preview(name = "Library · scan pending", showBackground = true, backgroundColor = 0xFF14161A)
@Composable
private fun LibraryScanPendingPreview() {
    MusicWorkbenchTheme {
        LibraryScaffoldContent(
            folderLabel = "Qobuz Downloads",
            onChangeFolder = {},
            onTrackClick = {},
            scanState = ScanState.Running(42, "nils_frahm_all_melody.flac"),
            filter = LibraryFilter.ALL,
            sort = LibrarySort.ALBUM,
            tracks = emptyList(),
            lowResThresholdPx = 600,
            onFilterChange = {},
            onSortChange = {},
            onRescan = {},
            viewMode = LibraryViewMode.FLAT,
            groupedTracks = emptyList(),
            selectedUris = emptySet(),
            isSelecting = false,
            batchFetchState = BatchFetchState.Idle,
            downloadingUris = emptySet(),
            downloadEntries = emptyList(),
            batchArtFetchState = BatchFetchState.Idle,
            downloadingArtUris = emptySet(),
            artDownloadEntries = emptyList(),
            onToggleViewMode = {},
            onEnterSelection = {},
            onToggleSelection = {},
            onClearSelection = {},
            onFetchLyrics = {},
            onFetchArt = {},
            onClearDownloads = {},
            onClearArtDownloads = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview(name = "Library · track list", showBackground = true, backgroundColor = 0xFF14161A)
@Composable
private fun LibraryTrackListPreview() {
    MusicWorkbenchTheme {
        LibraryScaffoldContent(
            folderLabel = "Qobuz Downloads",
            onChangeFolder = {},
            onTrackClick = {},
            scanState = ScanState.Done(ScanResult(scanned = 248, upserted = 3, removed = 0, failed = 0)),
            filter = LibraryFilter.ALL,
            sort = LibrarySort.ALBUM,
            tracks = listOf(
                previewTrack("Song Without End", "Nils Frahm", "All Melody", "FLAC"),
                previewTrack("Says", "Nils Frahm", "All Melody", "FLAC", hasSynced = false),
                previewTrack("track_07", null, null, "MP3", hasArt = false, hasSynced = false),
                previewTrack("Opus 23", "Dustin O'Halloran", "Lumiere", "FLAC", hasSynced = true),
            ),
            lowResThresholdPx = 600,
            onFilterChange = {},
            onSortChange = {},
            onRescan = {},
            viewMode = LibraryViewMode.FLAT,
            groupedTracks = emptyList(),
            selectedUris = emptySet(),
            isSelecting = false,
            batchFetchState = BatchFetchState.Idle,
            downloadingUris = emptySet(),
            downloadEntries = emptyList(),
            batchArtFetchState = BatchFetchState.Idle,
            downloadingArtUris = emptySet(),
            artDownloadEntries = emptyList(),
            onToggleViewMode = {},
            onEnterSelection = {},
            onToggleSelection = {},
            onClearSelection = {},
            onFetchLyrics = {},
            onFetchArt = {},
            onClearDownloads = {},
            onClearArtDownloads = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview(name = "Library · selection mode", showBackground = true, backgroundColor = 0xFF14161A)
@Composable
private fun LibrarySelectionPreview() {
    val tracks = listOf(
        previewTrack("Song Without End", "Nils Frahm", "All Melody", "FLAC"),
        previewTrack("Says", "Nils Frahm", "All Melody", "FLAC", hasSynced = false),
    )
    MusicWorkbenchTheme {
        LibraryScaffoldContent(
            folderLabel = "Qobuz Downloads",
            onChangeFolder = {},
            onTrackClick = {},
            scanState = ScanState.Done(ScanResult(scanned = 248, upserted = 0, removed = 0, failed = 0)),
            filter = LibraryFilter.ALL,
            sort = LibrarySort.ALBUM,
            tracks = tracks,
            lowResThresholdPx = 600,
            onFilterChange = {},
            onSortChange = {},
            onRescan = {},
            viewMode = LibraryViewMode.FLAT,
            groupedTracks = emptyList(),
            selectedUris = setOf(tracks[0].documentUri),
            isSelecting = true,
            batchFetchState = BatchFetchState.Idle,
            downloadingUris = emptySet(),
            downloadEntries = emptyList(),
            batchArtFetchState = BatchFetchState.Idle,
            downloadingArtUris = emptySet(),
            artDownloadEntries = emptyList(),
            onToggleViewMode = {},
            onEnterSelection = {},
            onToggleSelection = {},
            onClearSelection = {},
            onFetchLyrics = {},
            onFetchArt = {},
            onClearDownloads = {},
            onClearArtDownloads = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview(name = "Library · empty (filtered)", showBackground = true, backgroundColor = 0xFF14161A)
@Composable
private fun LibraryFilteredEmptyPreview() {
    MusicWorkbenchTheme {
        LibraryScaffoldContent(
            folderLabel = "Qobuz Downloads",
            onChangeFolder = {},
            onTrackClick = {},
            scanState = ScanState.Done(ScanResult(scanned = 248, upserted = 0, removed = 0, failed = 0)),
            filter = LibraryFilter.MISSING_ART,
            sort = LibrarySort.ALBUM,
            tracks = emptyList(),
            lowResThresholdPx = 600,
            onFilterChange = {},
            onSortChange = {},
            onRescan = {},
            viewMode = LibraryViewMode.FLAT,
            groupedTracks = emptyList(),
            selectedUris = emptySet(),
            isSelecting = false,
            batchFetchState = BatchFetchState.Idle,
            downloadingUris = emptySet(),
            downloadEntries = emptyList(),
            batchArtFetchState = BatchFetchState.Idle,
            downloadingArtUris = emptySet(),
            artDownloadEntries = emptyList(),
            onToggleViewMode = {},
            onEnterSelection = {},
            onToggleSelection = {},
            onClearSelection = {},
            onFetchLyrics = {},
            onFetchArt = {},
            onClearDownloads = {},
            onClearArtDownloads = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
