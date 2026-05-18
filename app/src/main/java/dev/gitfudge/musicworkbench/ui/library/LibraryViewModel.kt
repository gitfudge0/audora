package dev.gitfudge.musicworkbench.ui.library

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gitfudge.musicworkbench.data.art.CoverArtCandidate
import dev.gitfudge.musicworkbench.data.art.CoverArtRepository
import dev.gitfudge.musicworkbench.data.db.AlbumRow
import dev.gitfudge.musicworkbench.data.db.TrackDao
import dev.gitfudge.musicworkbench.data.db.TrackEntity
import dev.gitfudge.musicworkbench.data.lyrics.LrcWriter
import dev.gitfudge.musicworkbench.data.lyrics.LrclibRepository
import dev.gitfudge.musicworkbench.data.scan.MediaScanner
import dev.gitfudge.musicworkbench.data.scan.ScanResult
import dev.gitfudge.musicworkbench.data.settings.SettingsRepository
import dev.gitfudge.musicworkbench.data.tags.BulkTagApplier
import dev.gitfudge.musicworkbench.data.tags.TagWriter
import dev.gitfudge.musicworkbench.domain.BulkTagEdits
import dev.gitfudge.musicworkbench.domain.AlbumArtStatus
import dev.gitfudge.musicworkbench.domain.AlbumLyricsStatus
import dev.gitfudge.musicworkbench.domain.AlbumSummary
import dev.gitfudge.musicworkbench.domain.AlbumTagStatus
import dev.gitfudge.musicworkbench.domain.duplicateSuspectKeys
import dev.gitfudge.musicworkbench.domain.LibraryFilter
import dev.gitfudge.musicworkbench.domain.LibrarySort
import dev.gitfudge.musicworkbench.domain.LibraryTab
import dev.gitfudge.musicworkbench.domain.displayTitle
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

// ── Scan ──────────────────────────────────────────────────────────────────────

sealed interface ScanState {
    data object Idle : ScanState
    data class Running(val count: Int, val label: String) : ScanState
    data class Done(val result: ScanResult) : ScanState
    data class Failed(val cause: String) : ScanState
}

sealed interface LibraryLoadState {
    data object Ready : LibraryLoadState
    data class FirstImport(
        val scannedCount: Int,
        val indexedCount: Int,
        val currentLabel: String,
    ) : LibraryLoadState
}

sealed interface ArtEnrichmentState {
    data object Idle : ArtEnrichmentState
    data class Running(val remaining: Int) : ArtEnrichmentState
}

// ── Batch lyrics fetch ────────────────────────────────────────────────────────

sealed interface BatchFetchState {
    data object Idle : BatchFetchState
    data class Running(val done: Int, val total: Int, val inFlight: Int) : BatchFetchState
    data class Done(val saved: Int, val noMatch: Int, val failed: Int) : BatchFetchState
}

// ── Bulk tag edit ─────────────────────────────────────────────────────────────

sealed interface BulkEditState {
    data object Idle : BulkEditState
    data class Running(val done: Int, val total: Int) : BulkEditState
    data class Done(val ok: Int, val failed: Int) : BulkEditState
}

// ── Per-track download entries (shown in the downloads sheet) ─────────────────

enum class DownloadStatus { PENDING, DOWNLOADING, SAVED, NO_MATCH, FAILED }

data class DownloadEntry(
    val documentUri: String,
    val title: String,
    val status: DownloadStatus,
)

// ── Batch art per-album picker ────────────────────────────────────────────────

data class AlbumPickerState(
    val albumName: String,
    val artistName: String,
    val trackCount: Int,
    val candidates: List<CoverArtCandidate>,
    val albumIndex: Int,    // 1-based for display
    val totalAlbums: Int,
)

/** Shown after the user picks a candidate in [AlbumPickerState] — full-size preview before commit. */
data class AlbumPreviewState(
    val albumName: String,
    val artistName: String,
    val candidate: CoverArtCandidate,
    val bytes: ByteArray,
    val trackCount: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AlbumPreviewState) return false
        return albumName == other.albumName &&
            artistName == other.artistName &&
            candidate == other.candidate &&
            bytes.contentEquals(other.bytes) &&
            trackCount == other.trackCount
    }
    override fun hashCode(): Int {
        var r = albumName.hashCode()
        r = 31 * r + artistName.hashCode()
        r = 31 * r + candidate.hashCode()
        r = 31 * r + bytes.contentHashCode()
        r = 31 * r + trackCount
        return r
    }
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class LibraryViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val trackDao: TrackDao,
    private val scanner: MediaScanner,
    private val lrclibRepo: LrclibRepository,
    private val lrcWriter: LrcWriter,
    private val tagWriter: TagWriter,
    private val bulkTagApplier: BulkTagApplier,
    private val coverArtRepository: CoverArtRepository,
) : ViewModel() {
    private companion object {
        const val SEARCH_DEBOUNCE_MS = 180L

        /**
         * A scan writes many batches in quick succession; each one re-runs the
         * track/album queries. Sampling coalesces that burst so Compose diffs a
         * multi-thousand-row list a few times a second instead of per batch.
         * Interactive filter/sort/search changes are still bounded by this.
         */
        const val LIST_SAMPLE_MS = 300L

        val BATCH_FETCH_PARALLELISM = Runtime.getRuntime().availableProcessors().coerceIn(2, 4)
    }

    private data class LibrarySourceSettings(
        val musicTreeUri: String?,
        val lowResThresholdPx: Int,
    )

    private val librarySourceSettings = settings.settings
        .map { LibrarySourceSettings(it.musicTreeUri, it.lowResThresholdPx) }
        .distinctUntilChanged()

    private val musicTreeUriFlow = settings.settings
        .map { it.musicTreeUri }
        .distinctUntilChanged()

    // ── Scan ──────────────────────────────────────────────────────────────────

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()
    private val _firstImportActive = MutableStateFlow(false)
    private var artEnrichmentJob: Job? = null

    val libraryLoadState: StateFlow<LibraryLoadState> = combine(
        _scanState,
        _firstImportActive,
        musicTreeUriFlow.flatMapLatest { uri ->
            if (uri == null) return@flatMapLatest flowOf(0)
            trackDao.observeCount(uri)
        },
    ) { scanState, firstImportActive, indexedCount ->
        if (firstImportActive && scanState is ScanState.Running) {
            LibraryLoadState.FirstImport(
                scannedCount = scanState.count,
                indexedCount = indexedCount,
                currentLabel = scanState.label,
            )
        } else {
            LibraryLoadState.Ready
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryLoadState.Ready)

    val artEnrichmentState: StateFlow<ArtEnrichmentState> = musicTreeUriFlow
        .flatMapLatest { uri ->
            if (uri == null) return@flatMapLatest flowOf(0)
            trackDao.observePendingArtCount(uri)
        }
        .map { remaining ->
            if (remaining > 0) ArtEnrichmentState.Running(remaining) else ArtEnrichmentState.Idle
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ArtEnrichmentState.Idle)

    // ── Filter / sort ─────────────────────────────────────────────────────────

    private val _filter = MutableStateFlow(LibraryFilter.ALL)
    val filter: StateFlow<LibraryFilter> = _filter.asStateFlow()

    private val _sort = MutableStateFlow(LibrarySort.ALBUM)
    val sort: StateFlow<LibrarySort> = _sort.asStateFlow()

    /** Free-text search across title / artist / album. Empty = no filter. */
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    fun setQuery(q: String) { _query.value = q }

    // ── View mode ─────────────────────────────────────────────────────────────

    private val _tab = MutableStateFlow(LibraryTab.ALBUMS)
    val tab: StateFlow<LibraryTab> = _tab.asStateFlow()

    fun setTab(t: LibraryTab) { _tab.value = t; clearSelection() }

    private val debouncedQuery = _query
        .map { it.trim() }
        .debounce(SEARCH_DEBOUNCE_MS)
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val lowResThresholdPx: StateFlow<Int> = settings.settings
        .map { it.lowResThresholdPx }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsRepository.DEFAULT_LOW_RES_PX)

    private val trackListQuery = combine(
        _tab,
        librarySourceSettings,
        _filter,
        _sort,
        debouncedQuery,
    ) { tab, source, filter, sort, query ->
        if (tab != LibraryTab.TRACKS) return@combine null
        val uri = source.musicTreeUri ?: return@combine null
        buildQuery(uri, filter, sort, source.lowResThresholdPx, query)
    }

    private val trackCountQuery = combine(
        librarySourceSettings,
        _filter,
        debouncedQuery,
    ) { source, filter, query ->
        val uri = source.musicTreeUri ?: return@combine null
        buildCountQuery(uri, filter, source.lowResThresholdPx, query)
    }

    val tracks: StateFlow<List<TrackEntity>> = trackListQuery
        .flatMapLatest { query -> if (query == null) flowOf(emptyList()) else trackDao.observeFiltered(query) }
        .sample(LIST_SAMPLE_MS)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val trackCount: StateFlow<Int> = trackCountQuery
        .flatMapLatest { query -> if (query == null) flowOf(0) else trackDao.observeFilteredCount(query) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /**
     * One row per album for the Albums tab. The same Filter/Sort controls as
     * the Tracks tab apply here, evaluated against the album rollup statuses
     * client-side (the album list is small and already in memory).
     */
    val albums: StateFlow<List<AlbumSummary>> = combine(
        librarySourceSettings.flatMapLatest { source ->
            val uri = source.musicTreeUri ?: return@flatMapLatest flowOf(emptyList<AlbumRow>())
            trackDao.observeAlbumRows(uri, source.lowResThresholdPx)
        }.sample(LIST_SAMPLE_MS).map { rows -> rows.map { it.toSummary() } },
        _filter,
        _sort,
        debouncedQuery,
    ) { list, filter, sort, q ->
        val needle = q.trim().lowercase()
        // DUPLICATES is cross-album (it needs to see the whole list to find
        // colliding titles), so it can't be expressed as a per-row predicate.
        val suspects = if (filter == LibraryFilter.DUPLICATES) duplicateSuspectKeys(list) else null
        list.asSequence()
            .filter { if (suspects != null) it.albumKey in suspects else it.matchesFilter(filter) }
            .filter {
                needle.isEmpty() ||
                    it.albumLabel.lowercase().contains(needle) ||
                    it.artistLabel.lowercase().contains(needle)
            }
            .sortedWith(albumComparator(sort))
            .toList()
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Count of unfiled tracks (no album tag). Drives the Unfiled bucket row. */
    val unfiledCount: StateFlow<Int> = musicTreeUriFlow
        .flatMapLatest { uri ->
            if (uri == null) return@flatMapLatest flowOf(0)
            trackDao.observeUnfiledCount(uri)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    // ── Selection ─────────────────────────────────────────────────────────────

    private val _selectedUris = MutableStateFlow(emptySet<String>())
    val selectedUris: StateFlow<Set<String>> = _selectedUris.asStateFlow()

    val isSelecting: StateFlow<Boolean> = _selectedUris
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** Albums whose tracks are currently selected (Albums-tab affordance). */
    private val _selectedAlbumKeys = MutableStateFlow(emptySet<String>())
    val selectedAlbumKeys: StateFlow<Set<String>> = _selectedAlbumKeys.asStateFlow()

    fun enterSelectionWith(uri: String) {
        _selectedUris.value = setOf(uri)
        _selectedAlbumKeys.value = emptySet()
    }
    fun toggleSelection(uri: String) { _selectedUris.update { if (uri in it) it - uri else it + uri } }
    fun clearSelection() {
        _selectedUris.value = emptySet()
        _selectedAlbumKeys.value = emptySet()
    }

    /** Resolve an album's track URIs and add/remove them as one unit. */
    private suspend fun albumUris(albumKey: String): List<String> {
        val uri = settings.settings.first().musicTreeUri ?: return emptyList()
        return trackDao.documentUrisForAlbums(uri, listOf(albumKey))
    }

    fun enterSelectionWithAlbum(albumKey: String) {
        viewModelScope.launch {
            val uris = albumUris(albumKey)
            if (uris.isEmpty()) return@launch
            _selectedUris.value = uris.toSet()
            _selectedAlbumKeys.value = setOf(albumKey)
        }
    }

    fun toggleAlbumSelection(albumKey: String) {
        viewModelScope.launch {
            val uris = albumUris(albumKey)
            if (uris.isEmpty()) return@launch
            if (albumKey in _selectedAlbumKeys.value) {
                _selectedUris.update { it - uris.toSet() }
                _selectedAlbumKeys.update { it - albumKey }
            } else {
                _selectedUris.update { it + uris.toSet() }
                _selectedAlbumKeys.update { it + albumKey }
            }
        }
    }

    // ── Bulk tag edit ─────────────────────────────────────────────────────────

    private val _bulkEditorOpen = MutableStateFlow(false)
    val bulkEditorOpen: StateFlow<Boolean> = _bulkEditorOpen.asStateFlow()

    private val _bulkEditorTracks = MutableStateFlow<List<TrackEntity>>(emptyList())
    val bulkEditorTracks: StateFlow<List<TrackEntity>> = _bulkEditorTracks.asStateFlow()

    private val _bulkEditState = MutableStateFlow<BulkEditState>(BulkEditState.Idle)
    val bulkEditState: StateFlow<BulkEditState> = _bulkEditState.asStateFlow()

    /** True when the editor was opened to merge duplicate albums (combine flow). */
    private val _combineMode = MutableStateFlow(false)
    val combineMode: StateFlow<Boolean> = _combineMode.asStateFlow()

    fun openBulkEditor() {
        val selected = _selectedUris.value
        if (selected.isEmpty()) return
        viewModelScope.launch {
            _combineMode.value = false
            _bulkEditorTracks.value = trackDao.getByDocumentUris(selected.toList())
            _bulkEditorOpen.value = true
        }
    }

    /**
     * Combine the multi-selected albums: load every track across them so the
     * editor can show why they were split and pre-fill a canonical Album /
     * Album artist that, once written, collapses them to one [albumKey].
     */
    fun openCombineEditor() {
        val selected = _selectedUris.value
        if (selected.isEmpty() || _selectedAlbumKeys.value.size < 2) return
        viewModelScope.launch {
            _combineMode.value = true
            _bulkEditorTracks.value = trackDao.getByDocumentUris(selected.toList())
            _bulkEditorOpen.value = true
        }
    }

    fun dismissBulkEditor() { _bulkEditorOpen.value = false }

    fun applyBulkEdits(edits: BulkTagEdits) {
        if (_bulkEditState.value is BulkEditState.Running || edits.isEmpty()) return
        val tracks = _bulkEditorTracks.value
        if (tracks.isEmpty()) return
        viewModelScope.launch {
            _bulkEditState.value = BulkEditState.Running(0, tracks.size)
            val result = bulkTagApplier.apply(tracks, edits) { done, total ->
                _bulkEditState.value = BulkEditState.Running(done, total)
            }
            _bulkEditState.value = BulkEditState.Done(result.ok, result.failed)
            _bulkEditorOpen.value = false
            clearSelection()
        }
    }

    fun dismissBulkEditResult() { _bulkEditState.value = BulkEditState.Idle }

    // ── Batch lyrics fetch ────────────────────────────────────────────────────

    private val _batchFetchState = MutableStateFlow<BatchFetchState>(BatchFetchState.Idle)
    val batchFetchState: StateFlow<BatchFetchState> = _batchFetchState.asStateFlow()

    /** URIs currently being fetched — used to show a loading indicator per list item. */
    private val _downloadingUris = MutableStateFlow(emptySet<String>())
    val downloadingUris: StateFlow<Set<String>> = _downloadingUris.asStateFlow()

    /** Ordered log of every download attempt in the current or last batch. */
    private val _downloadEntries = MutableStateFlow<List<DownloadEntry>>(emptyList())
    val downloadEntries: StateFlow<List<DownloadEntry>> = _downloadEntries.asStateFlow()

    fun batchFetchLyrics() {
        if (_batchFetchState.value is BatchFetchState.Running) return
        val selectedSet = _selectedUris.value
        if (selectedSet.isEmpty()) return

        viewModelScope.launch {
            val queue = trackDao.getByDocumentUris(selectedSet.toList())
            if (queue.isEmpty()) return@launch
            val s = settings.settings.first()
            val total = queue.size
            val saved = AtomicInteger(0)
            val noMatch = AtomicInteger(0)
            val failed = AtomicInteger(0)
            val done = AtomicInteger(0)
            val workerDispatcher = Dispatchers.IO.limitedParallelism(BATCH_FETCH_PARALLELISM)

            // Seed the entries list as all-pending and mark all URIs as downloading
            _downloadEntries.value = queue.map { DownloadEntry(it.documentUri, it.displayTitle(), DownloadStatus.PENDING) }
            _batchFetchState.value = BatchFetchState.Running(0, total, total)

            fun updateRunningState() {
                _batchFetchState.value = BatchFetchState.Running(
                    done = done.get(),
                    total = total,
                    inFlight = _downloadingUris.value.size,
                )
            }

            queue.map { track ->
                async(workerDispatcher) {
                    _downloadingUris.update { it + track.documentUri }
                    _downloadEntries.update { entries ->
                        entries.map { if (it.documentUri == track.documentUri) it.copy(status = DownloadStatus.DOWNLOADING) else it }
                    }
                    updateRunningState()

                    runCatching {
                        val result = lrclibRepo.fetch(
                            title = track.title ?: track.displayName.substringBeforeLast('.'),
                            artist = track.artist ?: "",
                            album = track.album,
                            durationMs = track.durationMs,
                        )
                        if (result == null || result.instrumental ||
                            (result.syncedLyrics.isNullOrBlank() && result.plainLyrics.isNullOrBlank())
                        ) {
                            noMatch.incrementAndGet()
                            _downloadEntries.update { entries ->
                                entries.map { if (it.documentUri == track.documentUri) it.copy(status = DownloadStatus.NO_MATCH) else it }
                            }
                            return@runCatching
                        }
                        val lyricsText = result.syncedLyrics ?: result.plainLyrics!!
                        val isSynced = !result.syncedLyrics.isNullOrBlank()
                        val treeUri = track.treeUri.toUri()
                        val docUri = track.documentUri.toUri()

                        lrcWriter.write(treeUri, docUri, track.displayName, lyricsText)
                        var newMod = track.lastModified
                        if (s.embedLyricsInTags) {
                            newMod = tagWriter.writeLyrics(
                                docUri, track.displayName, lyricsText,
                                expectedLastModified = track.lastModified,
                            )
                        }
                        trackDao.upsertAll(listOf(
                            track.copy(
                                hasSidecarLrc = true,
                                sidecarLrcSynced = isSynced,
                                lastModified = newMod,
                                scannedAt = System.currentTimeMillis(),
                            ),
                        ))
                        saved.incrementAndGet()
                        _downloadEntries.update { entries ->
                            entries.map { if (it.documentUri == track.documentUri) it.copy(status = DownloadStatus.SAVED) else it }
                        }
                    }.onFailure {
                        failed.incrementAndGet()
                        _downloadEntries.update { entries ->
                            entries.map { if (it.documentUri == track.documentUri) it.copy(status = DownloadStatus.FAILED) else it }
                        }
                    }

                    _downloadingUris.update { it - track.documentUri }
                    done.incrementAndGet()
                    updateRunningState()
                }
            }.awaitAll()

            _batchFetchState.value = BatchFetchState.Done(saved.get(), noMatch.get(), failed.get())
            clearSelection()
        }
    }

    fun dismissBatchFetch() { _batchFetchState.value = BatchFetchState.Idle }
    fun clearDownloadEntries() { _downloadEntries.value = emptyList() }

    // ── Batch art fetch ───────────────────────────────────────────────────────

    private val _batchArtFetchState = MutableStateFlow<BatchFetchState>(BatchFetchState.Idle)
    val batchArtFetchState: StateFlow<BatchFetchState> = _batchArtFetchState.asStateFlow()

    private val _downloadingArtUris = MutableStateFlow(emptySet<String>())
    val downloadingArtUris: StateFlow<Set<String>> = _downloadingArtUris.asStateFlow()

    private val _artDownloadEntries = MutableStateFlow<List<DownloadEntry>>(emptyList())
    val artDownloadEntries: StateFlow<List<DownloadEntry>> = _artDownloadEntries.asStateFlow()

    /** When non-null, the UI shows a bottom-sheet picker for this album. */
    private val _albumPicker = MutableStateFlow<AlbumPickerState?>(null)
    val albumPicker: StateFlow<AlbumPickerState?> = _albumPicker.asStateFlow()

    /** When non-null, the UI shows a full-size preview dialog over the picker. */
    private val _albumPreview = MutableStateFlow<AlbumPreviewState?>(null)
    val albumPreview: StateFlow<AlbumPreviewState?> = _albumPreview.asStateFlow()

    /** True while we are downloading the chosen image to show in the preview dialog. */
    private val _previewDownloading = MutableStateFlow(false)
    val previewDownloading: StateFlow<Boolean> = _previewDownloading.asStateFlow()

    /** Resolved when the user confirms (or null = skip) for the current album. */
    private var pickerDeferred: CompletableDeferred<Pair<CoverArtCandidate, ByteArray>?>? = null

    /** User tapped a candidate — download bytes, then emit preview. */
    fun pickAlbumArt(candidate: CoverArtCandidate) {
        if (_previewDownloading.value) return
        val current = _albumPicker.value ?: return
        viewModelScope.launch {
            _previewDownloading.value = true
            val bytesResult = runCatching { coverArtRepository.downloadFullRes(candidate) }
            _previewDownloading.value = false
            bytesResult.onSuccess { bytes ->
                _albumPreview.value = AlbumPreviewState(
                    albumName = current.albumName,
                    artistName = current.artistName,
                    candidate = candidate,
                    bytes = bytes,
                    trackCount = current.trackCount,
                )
            }
            // On failure we just stay on the picker; user can try a different candidate.
        }
    }

    /** User confirmed the previewed candidate — resume the batch loop. */
    fun confirmAlbumArt() {
        val preview = _albumPreview.value ?: return
        _albumPreview.value = null
        pickerDeferred?.complete(preview.candidate to preview.bytes)
    }

    /** User wants to go back to the candidate list — dismiss preview, keep picker. */
    fun cancelAlbumPreview() {
        _albumPreview.value = null
    }

    /** User skipped this album entirely. */
    fun skipAlbumArt() {
        _albumPreview.value = null
        pickerDeferred?.complete(null)
    }

    fun batchFetchArt() {
        if (_batchArtFetchState.value is BatchFetchState.Running) return
        val selectedSet = _selectedUris.value
        if (selectedSet.isEmpty()) return

        // Group by (album, artist). Tracks with blank album fall into a "no-album" bucket
        // that we auto-mark as NO_MATCH (we can't search without an album).
        viewModelScope.launch {
            val queue = trackDao.getByDocumentUris(selectedSet.toList())
            if (queue.isEmpty()) return@launch
            val grouped = queue.groupBy { (it.album ?: "").trim() to (it.artist ?: "").trim() }
            val albumGroups = grouped.entries
                .filter { (key, _) -> key.first.isNotBlank() }
                .map { (key, tracks) -> AlbumGroup(album = key.first, artist = key.second, tracks = tracks) }
            val orphanTracks = grouped.entries
                .filter { (key, _) -> key.first.isBlank() }
                .flatMap { it.value }

            val total = queue.size
            val saved = AtomicInteger(0)
            val noMatch = AtomicInteger(0)
            val failed = AtomicInteger(0)
            var done = 0

            _artDownloadEntries.value = queue.map {
                DownloadEntry(it.documentUri, it.displayTitle(), DownloadStatus.PENDING)
            }
            _batchArtFetchState.value = BatchFetchState.Running(0, total, 0)

            // Orphans: mark NO_MATCH immediately
            orphanTracks.forEach { track ->
                noMatch.incrementAndGet()
                _artDownloadEntries.update { entries ->
                    entries.map {
                        if (it.documentUri == track.documentUri) it.copy(status = DownloadStatus.NO_MATCH) else it
                    }
                }
                done++
            }
            _batchArtFetchState.value = BatchFetchState.Running(done, total, 0)

            albumGroups.forEachIndexed { albumIndex, group ->
                val groupUris = group.tracks.map { it.documentUri }.toSet()

                // Search candidates for this album
                val candidates = runCatching {
                    coverArtRepository.searchCandidates(album = group.album, artist = group.artist)
                }.getOrElse { emptyList() }

                if (candidates.isEmpty()) {
                    group.tracks.forEach { track ->
                        noMatch.incrementAndGet()
                        _artDownloadEntries.update { entries ->
                            entries.map {
                                if (it.documentUri == track.documentUri) it.copy(status = DownloadStatus.NO_MATCH) else it
                            }
                        }
                        done++
                    }
                    _batchArtFetchState.value = BatchFetchState.Running(done, total, 0)
                    if (albumIndex < albumGroups.size - 1) delay(1100)
                    return@forEachIndexed
                }

                // Ask the user to pick (and preview)
                val deferred = CompletableDeferred<Pair<CoverArtCandidate, ByteArray>?>()
                pickerDeferred = deferred
                _albumPicker.value = AlbumPickerState(
                    albumName = group.album,
                    artistName = group.artist,
                    trackCount = group.tracks.size,
                    candidates = candidates,
                    albumIndex = albumIndex + 1,
                    totalAlbums = albumGroups.size,
                )
                val choice = deferred.await()
                _albumPicker.value = null
                pickerDeferred = null

                if (choice == null) {
                    // Skipped
                    group.tracks.forEach { track ->
                        noMatch.incrementAndGet()
                        _artDownloadEntries.update { entries ->
                            entries.map {
                                if (it.documentUri == track.documentUri) it.copy(status = DownloadStatus.NO_MATCH) else it
                            }
                        }
                        done++
                    }
                    _batchArtFetchState.value = BatchFetchState.Running(done, total, 0)
                    if (albumIndex < albumGroups.size - 1) delay(1100)
                    return@forEachIndexed
                }

                val (_, imageBytes) = choice

                // Mark as downloading (bytes already in hand; this is the write phase)
                _downloadingArtUris.update { it + groupUris }
                _artDownloadEntries.update { entries ->
                    entries.map {
                        if (it.documentUri in groupUris) it.copy(status = DownloadStatus.DOWNLOADING) else it
                    }
                }
                _batchArtFetchState.value = BatchFetchState.Running(done, total, group.tracks.size)

                // Apply to every track in the group
                group.tracks.forEach { track ->
                    runCatching {
                        val docUri = track.documentUri.toUri()
                        val result = tagWriter.writeArtFromBytes(
                            docUri, track.displayName, imageBytes,
                            expectedLastModified = track.lastModified,
                        )
                        trackDao.upsertAll(listOf(
                            track.copy(
                                hasEmbeddedArt = true,
                                artWidth = result.artWidth,
                                artHeight = result.artHeight,
                                thumbnailPath = result.thumbnailPath,
                                artScanPending = false,
                                lastModified = result.lastModified,
                                scannedAt = System.currentTimeMillis(),
                            ),
                        ))
                        saved.incrementAndGet()
                        _artDownloadEntries.update { entries ->
                            entries.map {
                                if (it.documentUri == track.documentUri) it.copy(status = DownloadStatus.SAVED) else it
                            }
                        }
                    }.onFailure {
                        failed.incrementAndGet()
                        _artDownloadEntries.update { entries ->
                            entries.map {
                                if (it.documentUri == track.documentUri) it.copy(status = DownloadStatus.FAILED) else it
                            }
                        }
                    }
                    done++
                    _batchArtFetchState.value = BatchFetchState.Running(done, total, group.tracks.size)
                }

                _downloadingArtUris.update { it - groupUris }
                _batchArtFetchState.value = BatchFetchState.Running(done, total, 0)
                if (albumIndex < albumGroups.size - 1) delay(1100)
            }

            _batchArtFetchState.value = BatchFetchState.Done(saved.get(), noMatch.get(), failed.get())
            clearSelection()
        }
    }

    private data class AlbumGroup(
        val album: String,
        val artist: String,
        val tracks: List<TrackEntity>,
    )

    fun dismissBatchArtFetch() { _batchArtFetchState.value = BatchFetchState.Idle }
    fun clearArtDownloadEntries() { _artDownloadEntries.value = emptyList() }

    // ── Misc actions ──────────────────────────────────────────────────────────

    fun setFilter(f: LibraryFilter) { _filter.value = f; clearSelection() }
    fun setSort(s: LibrarySort) { _sort.value = s }

    fun rescan() {
        viewModelScope.launch {
            artEnrichmentJob?.cancel()
            val s = settings.settings.first()
            val uri = s.musicTreeUri ?: return@launch
            doScan(uri)
        }
    }

    init {
        // Auto-scan only the first time a folder is seen (onboarding) or when
        // the watch path changes. An already-scanned folder relies on the
        // persisted DB on subsequent launches; the user triggers a rescan
        // manually via [rescan].
        viewModelScope.launch {
            musicTreeUriFlow
                .filter { it != null }
                .collect { uri ->
                    if (uri != settings.settings.first().lastScannedTreeUri) {
                        doScan(uri!!)
                    }
                }
        }
    }

    private suspend fun doScan(treeUri: String) {
        if (_scanState.value is ScanState.Running) return
        artEnrichmentJob?.cancel()
        _firstImportActive.value = trackDao.observeCount(treeUri).first() == 0
        _scanState.value = ScanState.Running(0, "")
        runCatching {
            scanner.scan(treeUri) { count, label ->
                _scanState.value = ScanState.Running(count, label)
            }
        }.fold(
            onSuccess = {
                _firstImportActive.value = false
                settings.setLastScannedTreeUri(treeUri)
                _scanState.value = ScanState.Done(it)
                artEnrichmentJob = viewModelScope.launch {
                    scanner.enrichPendingArtwork(treeUri)
                }
            },
            onFailure = {
                _firstImportActive.value = false
                _scanState.value = ScanState.Failed(it.message ?: "Scan failed")
            },
        )
    }

    private fun buildQuery(
        treeUri: String,
        filter: LibraryFilter,
        sort: LibrarySort,
        lowResPx: Int,
        query: String,
    ): SupportSQLiteQuery {
        val args = mutableListOf<Any>()
        val where = buildTrackWhereClause(treeUri, filter, lowResPx, query, args)
        val orderBy = when (sort) {
            LibrarySort.ALBUM -> "albumLabel ASC, discNumber ASC, trackNumber ASC, displayName ASC"
            LibrarySort.TITLE -> "COALESCE(title, displayName) ASC"
            LibrarySort.ARTIST -> "COALESCE(artist, '') ASC, albumLabel ASC, trackNumber ASC"
            LibrarySort.RECENTLY_MODIFIED -> "lastModified DESC"
        }
        return SimpleSQLiteQuery("SELECT * FROM tracks WHERE $where ORDER BY $orderBy", args.toTypedArray())
    }

    private fun buildCountQuery(
        treeUri: String,
        filter: LibraryFilter,
        lowResPx: Int,
        query: String,
    ): SupportSQLiteQuery {
        val args = mutableListOf<Any>()
        val where = buildTrackWhereClause(treeUri, filter, lowResPx, query, args)
        return SimpleSQLiteQuery("SELECT COUNT(*) FROM tracks WHERE $where", args.toTypedArray())
    }

    private fun buildTrackWhereClause(
        treeUri: String,
        filter: LibraryFilter,
        lowResPx: Int,
        query: String,
        args: MutableList<Any>,
    ): String {
        args += treeUri
        return buildString {
            append("treeUri = ?")
            when (filter) {
                LibraryFilter.ALL -> {}
                LibraryFilter.MISSING_ART -> append(" AND artScanPending = 0 AND hasEmbeddedArt = 0")
                LibraryFilter.LOW_RES_ART -> {
                    append(" AND artScanPending = 0 AND hasEmbeddedArt = 1 AND artWidth IS NOT NULL AND artHeight IS NOT NULL AND MAX(artWidth, artHeight) < ?")
                    args += lowResPx
                }
                LibraryFilter.NO_LYRICS -> append(" AND hasSidecarLrc = 0")
                LibraryFilter.INCOMPLETE_TAGS -> append(" AND coreTagsComplete = 0")
                LibraryFilter.UNKNOWN_ARTIST -> append(" AND artistUnknown = 1")
                // Albums-only refinement; on the Tracks tab it behaves like ALL.
                LibraryFilter.DUPLICATES -> {}
            }
            if (query.isNotBlank()) {
                append(
                    " AND (" +
                        "COALESCE(title, displayName) LIKE ? COLLATE NOCASE OR " +
                        "COALESCE(artist, '') LIKE ? COLLATE NOCASE OR " +
                        "COALESCE(album, '') LIKE ? COLLATE NOCASE" +
                        ")",
                )
                val needle = "%$query%"
                args += needle
                args += needle
                args += needle
            }
        }
    }
}

/**
 * Album-level equivalent of the Tracks filter predicate: an album matches a
 * "needs work" filter when any of its tracks would (i.e. the rollup status is
 * anything other than fully clean).
 */
private fun AlbumSummary.matchesFilter(filter: LibraryFilter): Boolean = when (filter) {
    LibraryFilter.ALL -> true
    LibraryFilter.MISSING_ART ->
        artStatus == AlbumArtStatus.ALL_MISSING || artStatus == AlbumArtStatus.PARTIAL
    LibraryFilter.LOW_RES_ART -> artStatus == AlbumArtStatus.LOW_RES
    LibraryFilter.NO_LYRICS ->
        lyricsStatus == AlbumLyricsStatus.NONE || lyricsStatus == AlbumLyricsStatus.PARTIAL
    LibraryFilter.INCOMPLETE_TAGS -> tagStatus != AlbumTagStatus.ALL_OK
    LibraryFilter.UNKNOWN_ARTIST -> tagStatus == AlbumTagStatus.ALL_BAD
    // Handled in the albums flow with whole-list context; never reached here.
    LibraryFilter.DUPLICATES -> true
}

/**
 * Album sort. TITLE has no album analogue and RECENTLY_MODIFIED has no
 * per-album timestamp in the rollup, so both fall back to album label.
 */
private fun albumComparator(sort: LibrarySort): Comparator<AlbumSummary> {
    val byLabel = compareBy<AlbumSummary> { it.albumLabel.lowercase() }
    return when (sort) {
        LibrarySort.ALBUM, LibrarySort.TITLE, LibrarySort.RECENTLY_MODIFIED -> byLabel
        LibrarySort.ARTIST -> compareBy<AlbumSummary> { it.artistLabel.lowercase() }.then(byLabel)
    }
}

private fun AlbumRow.toSummary(): AlbumSummary {
    val artStatus = when {
        pendingArt > 0 -> AlbumArtStatus.PENDING
        withArt == 0 -> AlbumArtStatus.ALL_MISSING
        withArt < trackCount -> AlbumArtStatus.PARTIAL
        lowResArt > 0 -> AlbumArtStatus.LOW_RES
        else -> AlbumArtStatus.ALL_OK
    }
    val lyricsStatus = when {
        withLyrics == 0 -> AlbumLyricsStatus.NONE
        withLyrics < trackCount -> AlbumLyricsStatus.PARTIAL
        syncedLyrics == trackCount -> AlbumLyricsStatus.ALL_SYNCED
        else -> AlbumLyricsStatus.ALL_PRESENT
    }
    val tagStatus = when {
        okTags == trackCount -> AlbumTagStatus.ALL_OK
        okTags == 0 -> AlbumTagStatus.ALL_BAD
        else -> AlbumTagStatus.PARTIAL
    }
    val mixed = distinctArtistCount > 1
    return AlbumSummary(
        albumKey = albumKey,
        albumLabel = albumLabel,
        artistLabel = if (mixed) "Various Artists" else firstArtist.ifBlank { "Unknown artist" },
        trackCount = trackCount,
        year = year,
        coverThumbnailPath = coverThumbnailPath,
        artStatus = artStatus,
        lyricsStatus = lyricsStatus,
        tagStatus = tagStatus,
        mixedArtist = mixed,
    )
}
