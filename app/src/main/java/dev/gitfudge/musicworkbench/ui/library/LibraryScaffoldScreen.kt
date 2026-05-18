package dev.gitfudge.musicworkbench.ui.library

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gitfudge.musicworkbench.R
import dev.gitfudge.musicworkbench.data.db.TrackEntity
import dev.gitfudge.musicworkbench.data.scan.ScanResult
import dev.gitfudge.musicworkbench.domain.LibraryFilter
import dev.gitfudge.musicworkbench.domain.LibrarySort
import dev.gitfudge.musicworkbench.domain.displayArtist
import dev.gitfudge.musicworkbench.domain.displayTitle
import dev.gitfudge.musicworkbench.ui.common.AlbumArtPreviewDialog
import dev.gitfudge.musicworkbench.ui.components.AppFilterChip
import dev.gitfudge.musicworkbench.ui.components.AppTopBar
import dev.gitfudge.musicworkbench.ui.components.EmptyState
import dev.gitfudge.musicworkbench.ui.components.ScopeItem
import dev.gitfudge.musicworkbench.ui.components.ScopeToolbar
import dev.gitfudge.musicworkbench.ui.components.PrimaryButton
import dev.gitfudge.musicworkbench.ui.theme.LocalMotion
import dev.gitfudge.musicworkbench.ui.theme.LocalSpacing
import dev.gitfudge.musicworkbench.ui.theme.MusicWorkbenchTheme
import dev.gitfudge.musicworkbench.ui.theme.ThemeMode

@Composable
fun LibraryScaffoldScreen(
    folderLabel: String,
    onChangeFolder: () -> Unit,
    onTrackClick: (documentUri: String) -> Unit,
    onAlbumClick: (albumKey: String) -> Unit = {},
    onUnfiledClick: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val scanState by viewModel.scanState.collectAsStateWithLifecycle()
    val libraryLoadState by viewModel.libraryLoadState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    when (val loadState = libraryLoadState) {
        is LibraryLoadState.FirstImport -> LibraryImportingContent(
            folderLabel = folderLabel,
            onChangeFolder = onChangeFolder,
            onOpenSettings = onOpenSettings,
            progress = loadState,
            scanState = scanState,
            snackbarHostState = snackbarHostState,
        )

        LibraryLoadState.Ready -> {
            ReadyLibraryScaffold(
                folderLabel = folderLabel,
                onChangeFolder = onChangeFolder,
                onTrackClick = onTrackClick,
                onAlbumClick = onAlbumClick,
                onUnfiledClick = onUnfiledClick,
                onOpenSettings = onOpenSettings,
                viewModel = viewModel,
                scanState = scanState,
                snackbarHostState = snackbarHostState,
            )
        }
    }
}

@Composable
private fun ReadyLibraryScaffold(
    folderLabel: String,
    onChangeFolder: () -> Unit,
    onTrackClick: (documentUri: String) -> Unit,
    onAlbumClick: (albumKey: String) -> Unit,
    onUnfiledClick: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: LibraryViewModel,
    scanState: ScanState,
    snackbarHostState: SnackbarHostState,
) {
    val artEnrichmentState by viewModel.artEnrichmentState.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val sort by viewModel.sort.collectAsStateWithLifecycle()
    val tab by viewModel.tab.collectAsStateWithLifecycle()
    val tracks by viewModel.tracks.collectAsStateWithLifecycle()
    val trackCount by viewModel.trackCount.collectAsStateWithLifecycle()
    val lowResPx by viewModel.lowResThresholdPx.collectAsStateWithLifecycle()
    val albums by viewModel.albums.collectAsStateWithLifecycle()
    val unfiledCount by viewModel.unfiledCount.collectAsStateWithLifecycle()
    val selectedUris by viewModel.selectedUris.collectAsStateWithLifecycle()
    val selectedAlbumKeys by viewModel.selectedAlbumKeys.collectAsStateWithLifecycle()
    val isSelecting by viewModel.isSelecting.collectAsStateWithLifecycle()
    val bulkEditorOpen by viewModel.bulkEditorOpen.collectAsStateWithLifecycle()
    val bulkEditorTracks by viewModel.bulkEditorTracks.collectAsStateWithLifecycle()
    val bulkEditState by viewModel.bulkEditState.collectAsStateWithLifecycle()
    val combineMode by viewModel.combineMode.collectAsStateWithLifecycle()
    val batchFetchState by viewModel.batchFetchState.collectAsStateWithLifecycle()
    val downloadingUris by viewModel.downloadingUris.collectAsStateWithLifecycle()
    val downloadEntries by viewModel.downloadEntries.collectAsStateWithLifecycle()
    val batchArtFetchState by viewModel.batchArtFetchState.collectAsStateWithLifecycle()
    val downloadingArtUris by viewModel.downloadingArtUris.collectAsStateWithLifecycle()
    val artDownloadEntries by viewModel.artDownloadEntries.collectAsStateWithLifecycle()
    val albumPicker by viewModel.albumPicker.collectAsStateWithLifecycle()
    val albumPreview by viewModel.albumPreview.collectAsStateWithLifecycle()
    val previewDownloading by viewModel.previewDownloading.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

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

    LaunchedEffect(bulkEditState) {
        if (bulkEditState is BulkEditState.Done) {
            val s = bulkEditState as BulkEditState.Done
            snackbarHostState.showSnackbar(
                "Updated ${s.ok} track${if (s.ok == 1) "" else "s"}" +
                    if (s.failed > 0) " · Failed ${s.failed}" else "",
            )
            viewModel.dismissBulkEditResult()
        }
    }

    LibraryScaffoldContent(
        folderLabel = folderLabel,
        onChangeFolder = onChangeFolder,
        onOpenSettings = onOpenSettings,
        onTrackClick = onTrackClick,
        scanState = scanState,
        artEnrichmentState = artEnrichmentState,
        filter = filter,
        sort = sort,
        tracks = tracks,
        trackCount = trackCount,
        lowResThresholdPx = lowResPx,
        onFilterChange = { viewModel.setFilter(it) },
        onSortChange = { viewModel.setSort(it) },
        onRescan = { viewModel.rescan() },
        tab = tab,
        albums = albums,
        unfiledCount = unfiledCount,
        onTabChange = { viewModel.setTab(it) },
        onAlbumClick = onAlbumClick,
        onUnfiledClick = onUnfiledClick,
        selectedUris = selectedUris,
        isSelecting = isSelecting,
        batchFetchState = batchFetchState,
        downloadingUris = downloadingUris,
        downloadEntries = downloadEntries,
        batchArtFetchState = batchArtFetchState,
        downloadingArtUris = downloadingArtUris,
        artDownloadEntries = artDownloadEntries,
        onEnterSelection = { viewModel.enterSelectionWith(it) },
        onToggleSelection = { viewModel.toggleSelection(it) },
        onClearSelection = { viewModel.clearSelection() },
        onFetchLyrics = { viewModel.batchFetchLyrics() },
        onFetchArt = { viewModel.batchFetchArt() },
        onClearDownloads = { viewModel.clearDownloadEntries() },
        onClearArtDownloads = { viewModel.clearArtDownloadEntries() },
        selectedAlbumKeys = selectedAlbumKeys,
        onEnterAlbumSelection = { viewModel.enterSelectionWithAlbum(it) },
        onToggleAlbumSelection = { viewModel.toggleAlbumSelection(it) },
        onEditTags = { viewModel.openBulkEditor() },
        onCombineAlbums = { viewModel.openCombineEditor() },
        snackbarHostState = snackbarHostState,
        query = query,
        onQueryChange = { viewModel.setQuery(it) },
    )

    if (bulkEditorOpen) {
        if (combineMode) {
            val plan = remember(bulkEditorTracks) {
                dev.gitfudge.musicworkbench.domain.buildCombinePlan(bulkEditorTracks)
            }
            dev.gitfudge.musicworkbench.ui.common.BulkTagEditorSheet(
                tracks = bulkEditorTracks,
                inFlight = bulkEditState is BulkEditState.Running,
                onApply = { viewModel.applyBulkEdits(it) },
                onDismiss = { viewModel.dismissBulkEditor() },
                title = "Combine albums",
                applyLabel = "Combine",
                prefill = buildMap {
                    if (plan.canonicalAlbum.isNotBlank()) {
                        put(dev.gitfudge.musicworkbench.domain.BulkTagField.ALBUM, plan.canonicalAlbum)
                    }
                    put(
                        dev.gitfudge.musicworkbench.domain.BulkTagField.ALBUM_ARTIST,
                        plan.canonicalAlbumArtist,
                    )
                },
                header = { CombineHeader(plan) },
            )
        } else {
            dev.gitfudge.musicworkbench.ui.common.BulkTagEditorSheet(
                tracks = bulkEditorTracks,
                inFlight = bulkEditState is BulkEditState.Running,
                onApply = { viewModel.applyBulkEdits(it) },
                onDismiss = { viewModel.dismissBulkEditor() },
            )
        }
    }

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

@Composable
private fun LibraryImportingContent(
    folderLabel: String,
    onChangeFolder: () -> Unit,
    onOpenSettings: () -> Unit,
    progress: LibraryLoadState.FirstImport,
    scanState: ScanState,
    snackbarHostState: SnackbarHostState,
) {
    val colors = MaterialTheme.colorScheme
    val spacing = LocalSpacing.current

    Scaffold(
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = stringResource(R.string.library_title),
                actions = {
                    IconButton(onClick = onChangeFolder) {
                        Icon(
                            imageVector = Icons.Rounded.SwapHoriz,
                            contentDescription = stringResource(R.string.library_change_folder),
                            tint = colors.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = colors.onSurfaceVariant,
                        )
                    }
                },
            )
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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = spacing.lg, vertical = spacing.md),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.Surface(
                    color = colors.surfaceContainerLow,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.xl),
                        verticalArrangement = Arrangement.spacedBy(spacing.md),
                    ) {
                        Text(
                            text = stringResource(R.string.library_importing_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onSurface,
                        )
                        Text(
                            text = stringResource(R.string.library_importing_body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                        )
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = colors.primary,
                            trackColor = colors.surfaceContainerHigh,
                        )
                        Text(
                            text = stringResource(
                                R.string.library_importing_scanned,
                                progress.scannedCount,
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.onSurface,
                        )
                        Text(
                            text = stringResource(
                                R.string.library_importing_indexed,
                                progress.indexedCount,
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                        )
                        if (progress.currentLabel.isNotBlank()) {
                            Text(
                                text = stringResource(
                                    R.string.library_importing_current_file,
                                    progress.currentLabel,
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        if (scanState is ScanState.Failed) {
                            Text(
                                text = stringResource(R.string.scan_failed, scanState.cause),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.error,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun LibraryScaffoldContent(
    folderLabel: String,
    onChangeFolder: () -> Unit,
    onOpenSettings: () -> Unit,
    onTrackClick: (documentUri: String) -> Unit,
    scanState: ScanState,
    artEnrichmentState: ArtEnrichmentState,
    filter: LibraryFilter,
    sort: LibrarySort,
    tracks: List<TrackEntity>,
    trackCount: Int,
    lowResThresholdPx: Int,
    onFilterChange: (LibraryFilter) -> Unit,
    onSortChange: (LibrarySort) -> Unit,
    onRescan: () -> Unit,
    tab: dev.gitfudge.musicworkbench.domain.LibraryTab,
    albums: List<dev.gitfudge.musicworkbench.domain.AlbumSummary>,
    unfiledCount: Int,
    onTabChange: (dev.gitfudge.musicworkbench.domain.LibraryTab) -> Unit,
    onAlbumClick: (albumKey: String) -> Unit,
    onUnfiledClick: () -> Unit,
    selectedUris: Set<String>,
    isSelecting: Boolean,
    batchFetchState: BatchFetchState,
    downloadingUris: Set<String>,
    downloadEntries: List<DownloadEntry>,
    batchArtFetchState: BatchFetchState,
    downloadingArtUris: Set<String>,
    artDownloadEntries: List<DownloadEntry>,
    onEnterSelection: (String) -> Unit,
    onToggleSelection: (String) -> Unit,
    onClearSelection: () -> Unit,
    onFetchLyrics: () -> Unit,
    onFetchArt: () -> Unit,
    onClearDownloads: () -> Unit,
    onClearArtDownloads: () -> Unit,
    selectedAlbumKeys: Set<String> = emptySet(),
    onEnterAlbumSelection: (String) -> Unit = {},
    onToggleAlbumSelection: (String) -> Unit = {},
    onEditTags: () -> Unit = {},
    onCombineAlbums: () -> Unit = {},
    snackbarHostState: SnackbarHostState,
    query: String = "",
    onQueryChange: (String) -> Unit = {},
) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme

    var showDownloadSheet by remember { mutableStateOf(false) }
    var showArtDownloadSheet by remember { mutableStateOf(false) }
    val isDownloading = batchFetchState is BatchFetchState.Running
    val isArtDownloading = batchArtFetchState is BatchFetchState.Running

    val onTrackRowClick = remember(isSelecting, onToggleSelection, onTrackClick) {
        { documentUri: String ->
            if (isSelecting) onToggleSelection(documentUri) else onTrackClick(documentUri)
        }
    }
    val onTrackRowLongClick = remember(isSelecting, onEnterSelection) {
        { documentUri: String ->
            if (!isSelecting) onEnterSelection(documentUri)
        }
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
            AppTopBar(
                title = stringResource(R.string.library_title),
                actions = {
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
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = colors.onSurfaceVariant,
                        )
                    }
                },
            )
        },
        bottomBar = {
            val motion = LocalMotion.current
            AnimatedVisibility(
                visible = isSelecting,
                enter = slideInVertically(animationSpec = tween(motion.standard, easing = motion.easeOut)) { it },
                exit = slideOutVertically(animationSpec = tween(motion.fast, easing = motion.easeOut)) { it },
            ) {
                SelectionBar(
                    selectedCount = selectedUris.size,
                    batchFetchState = batchFetchState,
                    batchArtFetchState = batchArtFetchState,
                    onFetchLyrics = onFetchLyrics,
                    onFetchArt = onFetchArt,
                    onEditTags = onEditTags,
                    onClearSelection = onClearSelection,
                    combineEnabled = tab == dev.gitfudge.musicworkbench.domain.LibraryTab.ALBUMS &&
                        selectedAlbumKeys.size >= 2,
                    selectedAlbumCount = selectedAlbumKeys.size,
                    onCombine = onCombineAlbums,
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
                artEnrichmentState = artEnrichmentState,
                modifier = Modifier.padding(horizontal = spacing.lg),
            )

            LibrarySearchField(
                query = query,
                onQueryChange = onQueryChange,
                modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.sm),
            )

            // Scope first (Albums vs Tracks), then refinement (filters/sort),
            // which applies to both scopes.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.lg, vertical = spacing.sm),
                contentAlignment = Alignment.CenterStart,
            ) {
                ScopeToolbar(
                    items = listOf(
                        ScopeItem("Albums", albums.size),
                        ScopeItem("Tracks", trackCount),
                    ),
                    selectedIndex = if (tab == dev.gitfudge.musicworkbench.domain.LibraryTab.ALBUMS) 0 else 1,
                    onSelected = { idx ->
                        onTabChange(
                            if (idx == 0) dev.gitfudge.musicworkbench.domain.LibraryTab.ALBUMS
                            else dev.gitfudge.musicworkbench.domain.LibraryTab.TRACKS,
                        )
                    },
                )
            }

            FilterSortBar(
                filter = filter,
                sort = sort,
                onFilterChange = onFilterChange,
                onSortChange = onSortChange,
                tab = tab,
            )
            HorizontalDivider(color = colors.outlineVariant)

            when {
                tab == dev.gitfudge.musicworkbench.domain.LibraryTab.TRACKS &&
                    tracks.isEmpty() && scanState is ScanState.Running -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = colors.primary,
                            modifier = Modifier.size(40.dp),
                        )
                    }
                }

                tab == dev.gitfudge.musicworkbench.domain.LibraryTab.ALBUMS -> AlbumsPane(
                    albums = albums,
                    unfiledCount = unfiledCount,
                    onAlbumClick = onAlbumClick,
                    onUnfiledClick = onUnfiledClick,
                    onChangeFolder = onChangeFolder,
                    modifier = Modifier.fillMaxSize(),
                    selectionMode = isSelecting,
                    selectedAlbumKeys = selectedAlbumKeys,
                    onEnterAlbumSelection = onEnterAlbumSelection,
                    onToggleAlbumSelection = onToggleAlbumSelection,
                )

                tracks.isEmpty() -> {
                    val filtered = filter != LibraryFilter.ALL
                    EmptyState(
                        icon = if (filtered) Icons.Rounded.FilterList else Icons.Rounded.GraphicEq,
                        title = stringResource(R.string.library_empty_title),
                        body = stringResource(
                            if (filtered) R.string.library_filter_empty_body else R.string.library_empty_body,
                        ),
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                else -> {
                    val trackListState = rememberLazyListState()
                    // Index key follows the active sort so jumps land in order.
                    // RECENTLY_MODIFIED has no alphabetical axis, so no rail.
                    val letterIndex = remember(tracks, sort) {
                        if (sort == LibrarySort.RECENTLY_MODIFIED) {
                            emptyMap()
                        } else {
                            buildMap {
                                tracks.forEachIndexed { i, t ->
                                    val label = when (sort) {
                                        LibrarySort.ARTIST -> t.displayArtist().orEmpty()
                                        LibrarySort.ALBUM -> t.albumLabel
                                        else -> t.displayTitle()
                                    }
                                    putIfAbsent(
                                        dev.gitfudge.musicworkbench.ui.components.sectionLetterOf(label),
                                        i,
                                    )
                                }
                            }
                        }
                    }
                    val railVisible = letterIndex.isNotEmpty() && tracks.size >= 12
                    Box(Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = trackListState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = if (railVisible) {
                                PaddingValues(end = spacing.xl + spacing.sm)
                            } else {
                                PaddingValues()
                            },
                        ) {
                            items(tracks, key = { it.documentUri }) { track ->
                                TrackListItem(
                                    track = track,
                                    lowResThresholdPx = lowResThresholdPx,
                                    onClick = onTrackRowClick,
                                    onLongClick = onTrackRowLongClick,
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
                        if (railVisible) {
                            dev.gitfudge.musicworkbench.ui.components.AlphabetScroller(
                                listState = trackListState,
                                indexForLetter = { letterIndex[it] },
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .fillMaxHeight(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectionBar(
    selectedCount: Int,
    batchFetchState: BatchFetchState,
    batchArtFetchState: BatchFetchState,
    onFetchLyrics: () -> Unit,
    onFetchArt: () -> Unit,
    onEditTags: () -> Unit,
    onClearSelection: () -> Unit,
    combineEnabled: Boolean = false,
    selectedAlbumCount: Int = 0,
    onCombine: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val spacing = LocalSpacing.current
    // DESIGN.md: batch action bar sits on `surfaceContainerHigh` with a 1px outline
    // hairline rather than a heavy shadow. No tonal elevation.
    androidx.compose.material3.Surface(
        color = colors.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(color = colors.outline, thickness = 1.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = spacing.lg, vertical = spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.xs),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.selection_count, selectedCount),
                        style = dev.gitfudge.musicworkbench.ui.theme.AppTextStyles.mono
                            .copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                        color = colors.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onClearSelection) {
                        Icon(
                            imageVector = Icons.Rounded.Clear,
                            contentDescription = stringResource(R.string.selection_clear),
                            tint = colors.secondary,
                        )
                    }
                }
                val batchRunning = batchFetchState is BatchFetchState.Running ||
                    batchArtFetchState is BatchFetchState.Running
                if (combineEnabled && !batchRunning) {
                    PrimaryButton(
                        onClick = onCombine,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.selection_combine, selectedAlbumCount))
                    }
                }
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
                            dev.gitfudge.musicworkbench.ui.components.GhostButton(
                                onClick = onEditTags,
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(stringResource(R.string.selection_edit_tags))
                            }
                            dev.gitfudge.musicworkbench.ui.components.GhostButton(
                                onClick = onFetchLyrics,
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(stringResource(R.string.selection_fetch_lyrics))
                            }
                            PrimaryButton(
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
}

@Composable
private fun CombineHeader(plan: dev.gitfudge.musicworkbench.domain.CombinePlan) {
    val colors = MaterialTheme.colorScheme
    val spacing = LocalSpacing.current
    Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
        Text(
            text = "Combining ${plan.groups.size} albums",
            style = MaterialTheme.typography.titleSmall,
            color = colors.onSurface,
        )
        Text(
            text = "These show up separately because:",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
        )
        plan.reasons.forEach { reason ->
            Text(
                text = "• $reason",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
        plan.groups.forEach { g ->
            Text(
                text = "— ${g.albumText} · ${g.albumArtist ?: g.artistSample ?: "no album artist"} · " +
                    "${g.trackCount} track${if (g.trackCount == 1) "" else "s"}",
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = "Pick the canonical Album and Album artist below — writing them to " +
                "every track collapses these into one album. Adjust or add fields as needed.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(top = spacing.xs),
        )
    }
}

@Composable
private fun LibrarySearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    androidx.compose.material3.OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium,
        placeholder = {
            Text("Search title, artist, album", style = MaterialTheme.typography.bodyMedium)
        },
        leadingIcon = {
            Icon(
                Icons.Rounded.Search,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Rounded.Clear, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                }
            }
        } else null,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.surfaceVariant,
            unfocusedContainerColor = colors.surfaceVariant,
            focusedBorderColor = colors.secondary,
            unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
        ),
    )
}

@Composable
private fun FolderStrip(folderLabel: String, modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme

    // Calm reminder of scope — a single inline strip, not a card. Eyebrow
    // label + mono path so it reads like equipment chrome.
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        val faint = colors.onSurfaceVariant.copy(alpha = 0.7f)
        Icon(
            imageVector = Icons.Rounded.Folder,
            contentDescription = null,
            tint = faint,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = stringResource(R.string.library_folder_label).uppercase(),
            style = dev.gitfudge.musicworkbench.ui.theme.AppTextStyles.eyebrow,
            color = faint,
        )
        Text(
            text = folderLabel,
            style = dev.gitfudge.musicworkbench.ui.theme.AppTextStyles.mono,
            color = colors.onSurfaceVariant,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ScanProgressRow(
    scanState: ScanState,
    artEnrichmentState: ArtEnrichmentState,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    when (scanState) {
        is ScanState.Idle -> {}

        is ScanState.Running -> Column(modifier.padding(vertical = spacing.xs)) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainer,
            )
            Spacer(Modifier.height(spacing.xs))
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

        is ScanState.Done -> when (artEnrichmentState) {
            ArtEnrichmentState.Idle -> {}
            is ArtEnrichmentState.Running -> Text(
                text = stringResource(
                    R.string.library_art_enrichment_running,
                    artEnrichmentState.remaining,
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = modifier.padding(vertical = spacing.xs),
            )
        }

        is ScanState.Failed -> Text(
            text = stringResource(R.string.scan_failed, scanState.cause),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
            modifier = modifier.padding(vertical = spacing.xs),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSortBar(
    filter: LibraryFilter,
    sort: LibrarySort,
    onFilterChange: (LibraryFilter) -> Unit,
    onSortChange: (LibrarySort) -> Unit,
    tab: dev.gitfudge.musicworkbench.domain.LibraryTab,
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
        // "Possible duplicates" is an album-level refinement; hide it on Tracks.
        LibraryFilter.entries.filter {
            it != LibraryFilter.DUPLICATES ||
                tab == dev.gitfudge.musicworkbench.domain.LibraryTab.ALBUMS
        }.forEach { f ->
            AppFilterChip(
                selected = filter == f,
                onClick = { onFilterChange(f) },
                label = stringResource(f.labelRes()),
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
                    labelColor = MaterialTheme.colorScheme.onSurface,
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

@StringRes
private fun LibraryFilter.labelRes(): Int = when (this) {
    LibraryFilter.ALL -> R.string.filter_all
    LibraryFilter.MISSING_ART -> R.string.filter_missing_art
    LibraryFilter.LOW_RES_ART -> R.string.filter_low_res_art
    LibraryFilter.NO_LYRICS -> R.string.filter_no_lyrics
    LibraryFilter.INCOMPLETE_TAGS -> R.string.filter_incomplete_tags
    LibraryFilter.UNKNOWN_ARTIST -> R.string.filter_unknown_artist
    LibraryFilter.DUPLICATES -> R.string.filter_duplicates
}

@StringRes
private fun LibrarySort.labelRes(): Int = when (this) {
    LibrarySort.ALBUM -> R.string.sort_album
    LibrarySort.TITLE -> R.string.sort_title
    LibrarySort.ARTIST -> R.string.sort_artist
    LibrarySort.RECENTLY_MODIFIED -> R.string.sort_recently_modified
}

// ── Previews ────────────────────────────────────────────────────────────────

@Composable
private fun previewScaffold(
    themeMode: ThemeMode,
    scanState: ScanState = ScanState.Done(ScanResult(scanned = 248, upserted = 3, removed = 0, failed = 0)),
    filter: LibraryFilter = LibraryFilter.ALL,
    tracks: List<TrackEntity> = previewTracks(),
    tab: dev.gitfudge.musicworkbench.domain.LibraryTab = dev.gitfudge.musicworkbench.domain.LibraryTab.TRACKS,
    isSelecting: Boolean = false,
    selectedUris: Set<String> = emptySet(),
) {
    MusicWorkbenchTheme(themeMode = themeMode) {
        LibraryScaffoldContent(
            folderLabel = "Qobuz Downloads",
            onChangeFolder = {},
            onOpenSettings = {},
            onTrackClick = {},
            scanState = scanState,
            artEnrichmentState = ArtEnrichmentState.Idle,
            filter = filter,
            sort = LibrarySort.ALBUM,
            tracks = tracks,
            trackCount = tracks.size,
            lowResThresholdPx = 600,
            onFilterChange = {},
            onSortChange = {},
            onRescan = {},
            tab = tab,
            albums = emptyList(),
            unfiledCount = 2,
            onTabChange = {},
            onAlbumClick = {},
            onUnfiledClick = {},
            selectedUris = selectedUris,
            isSelecting = isSelecting,
            batchFetchState = BatchFetchState.Idle,
            downloadingUris = emptySet(),
            downloadEntries = emptyList(),
            batchArtFetchState = BatchFetchState.Idle,
            downloadingArtUris = emptySet(),
            artDownloadEntries = emptyList(),
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

private fun previewTracks(): List<TrackEntity> = listOf(
    previewTrack("Song Without End", "Nils Frahm", "All Melody", "FLAC"),
    previewTrack("Says", "Nils Frahm", "All Melody", "FLAC", hasSynced = false),
    previewTrack("track_07", null, null, "MP3", hasArt = false, hasSynced = false),
    previewTrack("Opus 23", "Dustin O'Halloran", "Lumiere", "FLAC", hasSynced = true),
)

@Preview(name = "Library · Light") @Composable
private fun LibraryLightPreview() = previewScaffold(ThemeMode.Light)

@Preview(name = "Library · Dark") @Composable
private fun LibraryDarkPreview() = previewScaffold(ThemeMode.Dark)

@Preview(name = "Library · scan pending · Dark") @Composable
private fun LibraryScanPendingPreview() = previewScaffold(
    themeMode = ThemeMode.Dark,
    scanState = ScanState.Running(42, "nils_frahm_all_melody.flac"),
    tracks = emptyList(),
)

@Preview(name = "Library · selection · Light") @Composable
private fun LibrarySelectionPreview() {
    val tracks = previewTracks()
    previewScaffold(
        themeMode = ThemeMode.Light,
        tracks = tracks,
        isSelecting = true,
        selectedUris = setOf(tracks[0].documentUri),
    )
}

@Preview(name = "Library · empty (filtered) · Dark") @Composable
private fun LibraryFilteredEmptyPreview() = previewScaffold(
    themeMode = ThemeMode.Dark,
    filter = LibraryFilter.MISSING_ART,
    tracks = emptyList(),
)
