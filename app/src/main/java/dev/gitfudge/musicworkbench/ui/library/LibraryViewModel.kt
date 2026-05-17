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
import dev.gitfudge.musicworkbench.data.tags.TagWriter
import dev.gitfudge.musicworkbench.domain.AlbumArtStatus
import dev.gitfudge.musicworkbench.domain.AlbumLyricsStatus
import dev.gitfudge.musicworkbench.domain.AlbumSummary
import dev.gitfudge.musicworkbench.domain.AlbumTagStatus
import dev.gitfudge.musicworkbench.domain.LibraryFilter
import dev.gitfudge.musicworkbench.domain.LibrarySort
import dev.gitfudge.musicworkbench.domain.LibraryTab
import dev.gitfudge.musicworkbench.domain.displayTitle
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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

// ── Batch lyrics fetch ────────────────────────────────────────────────────────

sealed interface BatchFetchState {
    data object Idle : BatchFetchState
    data class Running(val done: Int, val total: Int, val inFlight: Int) : BatchFetchState
    data class Done(val saved: Int, val noMatch: Int, val failed: Int) : BatchFetchState
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
class LibraryViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val trackDao: TrackDao,
    private val scanner: MediaScanner,
    private val lrclibRepo: LrclibRepository,
    private val lrcWriter: LrcWriter,
    private val tagWriter: TagWriter,
    private val coverArtRepository: CoverArtRepository,
) : ViewModel() {

    // ── Scan ──────────────────────────────────────────────────────────────────

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    // ── Filter / sort ─────────────────────────────────────────────────────────

    private val _filter = MutableStateFlow(LibraryFilter.ALL)
    val filter: StateFlow<LibraryFilter> = _filter.asStateFlow()

    private val _sort = MutableStateFlow(LibrarySort.ALBUM)
    val sort: StateFlow<LibrarySort> = _sort.asStateFlow()

    val lowResThresholdPx: StateFlow<Int> = settings.settings
        .map { it.lowResThresholdPx }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsRepository.DEFAULT_LOW_RES_PX)

    @OptIn(ExperimentalCoroutinesApi::class)
    val tracks: StateFlow<List<TrackEntity>> = combine(settings.settings, _filter, _sort) { s, f, sort ->
        val uri = s.musicTreeUri ?: return@combine null
        buildQuery(uri, f, sort, s.lowResThresholdPx)
    }
        .flatMapLatest { query -> if (query == null) flowOf(emptyList()) else trackDao.observeFiltered(query) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── View mode ─────────────────────────────────────────────────────────────

    private val _tab = MutableStateFlow(LibraryTab.ALBUMS)
    val tab: StateFlow<LibraryTab> = _tab.asStateFlow()

    fun setTab(t: LibraryTab) { _tab.value = t; _selectedUris.value = emptySet() }

    /** One row per album for the Albums tab. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val albums: StateFlow<List<AlbumSummary>> = settings.settings
        .flatMapLatest { s ->
            val uri = s.musicTreeUri ?: return@flatMapLatest flowOf(emptyList<AlbumRow>())
            trackDao.observeAlbumRows(uri, s.lowResThresholdPx)
        }
        .map { rows -> rows.map { it.toSummary() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Count of unfiled tracks (no album tag). Drives the Unfiled bucket row. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val unfiledCount: StateFlow<Int> = settings.settings
        .flatMapLatest { s ->
            val uri = s.musicTreeUri ?: return@flatMapLatest flowOf(0)
            trackDao.observeUnfiledCount(uri)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    // ── Selection ─────────────────────────────────────────────────────────────

    private val _selectedUris = MutableStateFlow(emptySet<String>())
    val selectedUris: StateFlow<Set<String>> = _selectedUris.asStateFlow()

    val isSelecting: StateFlow<Boolean> = _selectedUris
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun enterSelectionWith(uri: String) { _selectedUris.value = setOf(uri) }
    fun toggleSelection(uri: String) { _selectedUris.update { if (uri in it) it - uri else it + uri } }
    fun clearSelection() { _selectedUris.value = emptySet() }

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
        val queue = tracks.value.filter { it.documentUri in selectedSet }
        if (queue.isEmpty()) return

        viewModelScope.launch {
            val s = settings.settings.first()
            val total = queue.size
            val saved = AtomicInteger(0)
            val noMatch = AtomicInteger(0)
            val failed = AtomicInteger(0)
            val done = AtomicInteger(0)

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
                async {
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
                            newMod = tagWriter.writeLyrics(docUri, track.displayName, lyricsText)
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
            _selectedUris.value = emptySet()
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
        val queue = tracks.value.filter { it.documentUri in selectedSet }
        if (queue.isEmpty()) return

        // Group by (album, artist). Tracks with blank album fall into a "no-album" bucket
        // that we auto-mark as NO_MATCH (we can't search without an album).
        val grouped = queue.groupBy { (it.album ?: "").trim() to (it.artist ?: "").trim() }
        val albumGroups = grouped.entries
            .filter { (key, _) -> key.first.isNotBlank() }
            .map { (key, tracks) -> AlbumGroup(album = key.first, artist = key.second, tracks = tracks) }
        val orphanTracks = grouped.entries
            .filter { (key, _) -> key.first.isBlank() }
            .flatMap { it.value }

        viewModelScope.launch {
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
                        val result = tagWriter.writeArtFromBytes(docUri, track.displayName, imageBytes)
                        trackDao.upsertAll(listOf(
                            track.copy(
                                hasEmbeddedArt = true,
                                artWidth = result.artWidth,
                                artHeight = result.artHeight,
                                thumbnailPath = result.thumbnailPath,
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
            _selectedUris.value = emptySet()
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

    fun setFilter(f: LibraryFilter) { _filter.value = f; _selectedUris.value = emptySet() }
    fun setSort(s: LibrarySort) { _sort.value = s }

    fun rescan() {
        viewModelScope.launch {
            val s = settings.settings.first()
            val uri = s.musicTreeUri ?: return@launch
            doScan(uri, s.lowResThresholdPx)
        }
    }

    init {
        viewModelScope.launch {
            settings.settings
                .distinctUntilChanged { old, new -> old.musicTreeUri == new.musicTreeUri }
                .filter { it.musicTreeUri != null }
                .collect { s -> doScan(s.musicTreeUri!!, s.lowResThresholdPx) }
        }
    }

    private suspend fun doScan(treeUri: String, lowResPx: Int) {
        if (_scanState.value is ScanState.Running) return
        _scanState.value = ScanState.Running(0, "")
        runCatching {
            scanner.scan(treeUri, lowResPx) { count, label ->
                _scanState.value = ScanState.Running(count, label)
            }
        }.fold(
            onSuccess = { _scanState.value = ScanState.Done(it) },
            onFailure = { _scanState.value = ScanState.Failed(it.message ?: "Scan failed") },
        )
    }

    private fun buildQuery(
        treeUri: String,
        filter: LibraryFilter,
        sort: LibrarySort,
        lowResPx: Int,
    ): SupportSQLiteQuery {
        val args = mutableListOf<Any>(treeUri)
        val where = buildString {
            append("treeUri = ?")
            when (filter) {
                LibraryFilter.ALL -> {}
                LibraryFilter.MISSING_ART -> append(" AND hasEmbeddedArt = 0")
                LibraryFilter.LOW_RES_ART -> {
                    append(" AND hasEmbeddedArt = 1 AND artWidth IS NOT NULL AND artHeight IS NOT NULL AND MAX(artWidth, artHeight) < ?")
                    args += lowResPx
                }
                LibraryFilter.NO_LYRICS -> append(" AND hasSidecarLrc = 0")
                LibraryFilter.INCOMPLETE_TAGS -> append(" AND coreTagsComplete = 0")
                LibraryFilter.UNKNOWN_ARTIST -> append(" AND artistUnknown = 1")
            }
        }
        val orderBy = when (sort) {
            LibrarySort.ALBUM -> "albumLabel ASC, discNumber ASC, trackNumber ASC, displayName ASC"
            LibrarySort.TITLE -> "COALESCE(title, displayName) ASC"
            LibrarySort.ARTIST -> "COALESCE(artist, '') ASC, albumLabel ASC, trackNumber ASC"
            LibrarySort.RECENTLY_MODIFIED -> "lastModified DESC"
        }
        return SimpleSQLiteQuery("SELECT * FROM tracks WHERE $where ORDER BY $orderBy", args.toTypedArray())
    }
}

private fun AlbumRow.toSummary(): AlbumSummary {
    val artStatus = when {
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
