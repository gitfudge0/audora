package dev.gitfudge.audora.ui.album

import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gitfudge.audora.data.art.CoverArtCandidate
import dev.gitfudge.audora.data.art.CoverArtRepository
import dev.gitfudge.audora.data.db.TrackDao
import dev.gitfudge.audora.data.db.TrackEntity
import dev.gitfudge.audora.data.lyrics.LrcWriter
import dev.gitfudge.audora.data.lyrics.LrclibRepository
import dev.gitfudge.audora.data.settings.SettingsRepository
import dev.gitfudge.audora.data.tags.BulkTagApplier
import dev.gitfudge.audora.data.tags.FileSignature
import dev.gitfudge.audora.data.tags.TagWriter
import dev.gitfudge.audora.domain.AlbumArtStatus
import dev.gitfudge.audora.domain.AlbumLyricsStatus
import dev.gitfudge.audora.domain.AlbumTagStatus
import dev.gitfudge.audora.domain.BulkTagField
import dev.gitfudge.audora.ui.library.DownloadStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Album Detail screen state. Holds the album key (route arg) and exposes
 * the live track list + a derived summary used by the hero.
 */
data class AlbumDetailUi(
    val albumKey: String,
    val albumLabel: String,
    val artistLabel: String,
    val year: String?,
    val trackCount: Int,
    val coverThumbnailPath: String?,
    val artStatus: AlbumArtStatus,
    val lyricsStatus: AlbumLyricsStatus,
    val tagStatus: AlbumTagStatus,
    val mixedArtist: Boolean,
    val tracks: List<TrackEntity>,
    val lowResThresholdPx: Int,
)

// ── Hero art ─────────────────────────────────────────────────────────────────

sealed interface AlbumArtFlowState {
    data object Idle : AlbumArtFlowState
    data object Searching : AlbumArtFlowState
    data class Picker(val candidates: List<CoverArtCandidate>) : AlbumArtFlowState
    data class Preview(val candidate: CoverArtCandidate, val bytes: ByteArray) : AlbumArtFlowState
    data class Writing(val items: List<BatchItem>) : AlbumArtFlowState
    data class Done(val saved: Int, val failed: Int) : AlbumArtFlowState
    data class NoMatch(val message: String) : AlbumArtFlowState
}

// ── Lyrics batch ─────────────────────────────────────────────────────────────

data class LyricsReviewItem(
    val documentUri: String,
    val title: String,
    val artist: String,
    val previewText: String,
    val fullText: String,
    val isSynced: Boolean,
    val accept: Boolean,
)

/** A track in a per-item batch (lyrics or art), with its live status. */
data class BatchItem(
    val documentUri: String,
    val title: String,
    val status: DownloadStatus,
)

sealed interface LyricsBatchPhase {
    data object Idle : LyricsBatchPhase
    data class Options(
        val total: Int,
        val withExisting: Int,
        val replaceExisting: Boolean,
    ) : LyricsBatchPhase
    data class Fetching(val items: List<BatchItem>) : LyricsBatchPhase
    data class Review(
        val items: List<LyricsReviewItem>,
        val noMatchCount: Int,
        val failedCount: Int,
    ) : LyricsBatchPhase
    data class Writing(val items: List<BatchItem>) : LyricsBatchPhase
    data class Done(val saved: Int, val skipped: Int, val noMatch: Int, val failed: Int) : LyricsBatchPhase
}

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val settings: SettingsRepository,
    private val trackDao: TrackDao,
    private val tagWriter: TagWriter,
    private val bulkTagApplier: BulkTagApplier,
    private val coverArtRepository: CoverArtRepository,
    private val lrclibRepo: LrclibRepository,
    private val lrcWriter: LrcWriter,
) : ViewModel() {

    val albumKey: String = checkNotNull(savedState["albumKey"]) {
        "AlbumDetail requires an albumKey route arg"
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val ui: StateFlow<AlbumDetailUi?> = settings.settings
        .flatMapLatest { s ->
            val uri = s.musicTreeUri
                ?: return@flatMapLatest flowOf(emptyList<TrackEntity>() to s.lowResThresholdPx)
            trackDao.observeTracksInAlbum(uri, albumKey)
                .map { tracks -> tracks to s.lowResThresholdPx }
        }
        .map { (tracks, lowResPx) ->
            if (tracks.isEmpty()) null else summarize(albumKey, tracks, lowResPx)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // ── Tag editor ───────────────────────────────────────────────────────────

    private val _tagEditorOpen = MutableStateFlow(false)
    val tagEditorOpen: StateFlow<Boolean> = _tagEditorOpen.asStateFlow()

    private val _tagWriteInFlight = MutableStateFlow(false)
    val tagWriteInFlight: StateFlow<Boolean> = _tagWriteInFlight.asStateFlow()

    fun openTagEditor() { _tagEditorOpen.value = true }
    fun dismissTagEditor() { _tagEditorOpen.value = false }

    fun computeInitial(tracks: List<TrackEntity>): AlbumTagEditorInitial {
        fun shared(selector: (TrackEntity) -> String?): String? {
            val values = tracks.map { selector(it).orEmpty().trim() }.distinct()
            return if (values.size == 1) values.first().ifBlank { "" } else null
        }
        return AlbumTagEditorInitial(
            album = shared { it.album },
            albumArtist = shared { it.albumArtist },
            year = shared { it.year },
            genre = shared { it.genre },
            trackCount = tracks.size,
        )
    }

    fun applyTagEdits(edits: AlbumTagEdits) {
        if (_tagWriteInFlight.value) return
        val tracks = ui.value?.tracks ?: return
        val bulkEdits = buildMap {
            edits.album?.let { put(BulkTagField.ALBUM, it) }
            edits.albumArtist?.let { put(BulkTagField.ALBUM_ARTIST, it) }
            edits.year?.let { put(BulkTagField.YEAR, it) }
            edits.genre?.let { put(BulkTagField.GENRE, it) }
        }
        viewModelScope.launch {
            _tagWriteInFlight.value = true
            runCatching { bulkTagApplier.apply(tracks, bulkEdits) }
            _tagWriteInFlight.value = false
            _tagEditorOpen.value = false
        }
    }

    // ── Hero art ─────────────────────────────────────────────────────────────

    private val _artFlow = MutableStateFlow<AlbumArtFlowState>(AlbumArtFlowState.Idle)
    val artFlow: StateFlow<AlbumArtFlowState> = _artFlow.asStateFlow()

    private val _artDownloading = MutableStateFlow(false)
    val artDownloading: StateFlow<Boolean> = _artDownloading.asStateFlow()

    fun startHeroArt() {
        val state = ui.value ?: return
        if (_artFlow.value !is AlbumArtFlowState.Idle) return
        viewModelScope.launch {
            _artFlow.value = AlbumArtFlowState.Searching
            val artistForSearch = state.tracks.firstNotNullOfOrNull {
                it.albumArtist?.takeIf(String::isNotBlank) ?: it.artist?.takeIf(String::isNotBlank)
            }.orEmpty()
            val candidates = runCatching {
                coverArtRepository.searchCandidates(album = state.albumLabel, artist = artistForSearch)
            }.getOrElse { emptyList() }
            _artFlow.value = if (candidates.isEmpty()) {
                AlbumArtFlowState.NoMatch("No cover art found for ${state.albumLabel}")
            } else {
                AlbumArtFlowState.Picker(candidates)
            }
        }
    }

    fun pickArtCandidate(candidate: CoverArtCandidate) {
        if (_artDownloading.value) return
        viewModelScope.launch {
            _artDownloading.value = true
            try {
                val bytes = runCatching { coverArtRepository.downloadFullRes(candidate) }.getOrNull()
                if (bytes == null) {
                    _artFlow.value = AlbumArtFlowState.NoMatch("Could not download that image")
                    return@launch
                }
                _artFlow.value = AlbumArtFlowState.Preview(candidate, bytes)
            } finally {
                _artDownloading.value = false
            }
        }
    }

    fun confirmHeroArt() {
        val preview = _artFlow.value as? AlbumArtFlowState.Preview ?: return
        val tracks = ui.value?.tracks ?: return
        viewModelScope.launch {
            val saved = java.util.concurrent.atomic.AtomicInteger(0)
            val failed = java.util.concurrent.atomic.AtomicInteger(0)
            _artFlow.value = AlbumArtFlowState.Writing(
                tracks.map { BatchItem(it.documentUri, it.title ?: it.displayName, DownloadStatus.PENDING) },
            )

            fun setStatus(uri: String, status: DownloadStatus) {
                _artFlow.update { current ->
                    (current as? AlbumArtFlowState.Writing)?.let { w ->
                        w.copy(items = w.items.map { if (it.documentUri == uri) it.copy(status = status) else it })
                    } ?: current
                }
            }

            kotlinx.coroutines.coroutineScope {
                tracks.map { track ->
                    async(kotlinx.coroutines.Dispatchers.IO) {
                        setStatus(track.documentUri, DownloadStatus.DOWNLOADING)
                        runCatching {
                            val res = tagWriter.writeArtFromBytes(
                                track.documentUri.toUri(), track.displayName, preview.bytes,
                                expected = FileSignature(track.lastModified, track.sizeBytes),
                            )
                            trackDao.upsertAll(listOf(
                                track.copy(
                                    hasEmbeddedArt = true,
                                    artWidth = res.artWidth,
                                    artHeight = res.artHeight,
                                    thumbnailPath = res.thumbnailPath,
                                    artScanPending = false,
                                    lastModified = res.lastModified,
                                    sizeBytes = if (res.sizeBytes > 0L) res.sizeBytes else track.sizeBytes,
                                    scannedAt = System.currentTimeMillis(),
                                ),
                            ))
                        }.onSuccess {
                            saved.incrementAndGet()
                            setStatus(track.documentUri, DownloadStatus.SAVED)
                        }.onFailure {
                            failed.incrementAndGet()
                            setStatus(track.documentUri, DownloadStatus.FAILED)
                        }
                    }
                }.awaitAll()
            }

            _artFlow.value = AlbumArtFlowState.Done(saved = saved.get(), failed = failed.get())
        }
    }

    fun backToPicker() {
        val preview = _artFlow.value as? AlbumArtFlowState.Preview ?: return
        // Re-derive picker; we kept candidates only on Picker state, so search again is acceptable,
        // but cheaper to just clear preview and let user start over from a fresh search.
        _artFlow.value = AlbumArtFlowState.Idle
        startHeroArt()
        // suppress unused-warning; preview kept for symmetry
        @Suppress("UNUSED_VARIABLE") val keep = preview
    }

    fun dismissArtFlow() { _artFlow.value = AlbumArtFlowState.Idle }

    // ── Lyrics batch ─────────────────────────────────────────────────────────

    private val _lyricsBatch = MutableStateFlow<LyricsBatchPhase>(LyricsBatchPhase.Idle)
    val lyricsBatch: StateFlow<LyricsBatchPhase> = _lyricsBatch.asStateFlow()

    fun startLyricsBatch() {
        val tracks = ui.value?.tracks ?: return
        if (_lyricsBatch.value !is LyricsBatchPhase.Idle) return
        val withExisting = tracks.count { it.hasSidecarLrc }
        _lyricsBatch.value = LyricsBatchPhase.Options(
            total = tracks.size,
            withExisting = withExisting,
            replaceExisting = false,
        )
    }

    fun setReplaceExisting(value: Boolean) {
        _lyricsBatch.update { current ->
            (current as? LyricsBatchPhase.Options)?.copy(replaceExisting = value) ?: current
        }
    }

    fun confirmLyricsOptions() {
        val opts = _lyricsBatch.value as? LyricsBatchPhase.Options ?: return
        val allTracks = ui.value?.tracks ?: return
        val targets = if (opts.replaceExisting) allTracks else allTracks.filter { !it.hasSidecarLrc }
        if (targets.isEmpty()) {
            _lyricsBatch.value = LyricsBatchPhase.Done(saved = 0, skipped = allTracks.size, noMatch = 0, failed = 0)
            return
        }
        viewModelScope.launch {
            val noMatch = java.util.concurrent.atomic.AtomicInteger(0)
            val failed = java.util.concurrent.atomic.AtomicInteger(0)
            val reviewItems = java.util.concurrent.ConcurrentLinkedQueue<LyricsReviewItem>()
            _lyricsBatch.value = LyricsBatchPhase.Fetching(
                targets.map {
                    BatchItem(it.documentUri, it.title ?: it.displayName, DownloadStatus.PENDING)
                },
            )

            fun setStatus(uri: String, status: DownloadStatus) {
                _lyricsBatch.update { current ->
                    (current as? LyricsBatchPhase.Fetching)?.let { f ->
                        f.copy(items = f.items.map { if (it.documentUri == uri) it.copy(status = status) else it })
                    } ?: current
                }
            }

            kotlinx.coroutines.coroutineScope {
                targets.map { track ->
                    async(kotlinx.coroutines.Dispatchers.IO) {
                        setStatus(track.documentUri, DownloadStatus.DOWNLOADING)
                        runCatching {
                            val result = lrclibRepo.fetch(
                                title = track.title ?: track.displayName.substringBeforeLast('.'),
                                artist = track.artist ?: "",
                                album = track.album,
                                durationMs = track.durationMs,
                            )
                            val text = result?.syncedLyrics?.takeIf { it.isNotBlank() }
                                ?: result?.plainLyrics?.takeIf { it.isNotBlank() }
                            if (result == null || result.instrumental || text == null) {
                                noMatch.incrementAndGet()
                                setStatus(track.documentUri, DownloadStatus.NO_MATCH)
                            } else {
                                reviewItems += LyricsReviewItem(
                                    documentUri = track.documentUri,
                                    title = track.title ?: track.displayName,
                                    artist = track.artist ?: "",
                                    previewText = text.lineSequence().take(3).joinToString("\n"),
                                    fullText = text,
                                    isSynced = !result.syncedLyrics.isNullOrBlank(),
                                    accept = true,
                                )
                                setStatus(track.documentUri, DownloadStatus.SAVED)
                            }
                        }.onFailure {
                            failed.incrementAndGet()
                            setStatus(track.documentUri, DownloadStatus.FAILED)
                        }
                    }
                }.awaitAll()
            }

            val items = reviewItems.toList()
            _lyricsBatch.value = if (items.isEmpty()) {
                LyricsBatchPhase.Done(saved = 0, skipped = 0, noMatch = noMatch.get(), failed = failed.get())
            } else {
                LyricsBatchPhase.Review(items, noMatchCount = noMatch.get(), failedCount = failed.get())
            }
        }
    }

    fun toggleReviewItem(uri: String) {
        _lyricsBatch.update { current ->
            (current as? LyricsBatchPhase.Review)?.let { r ->
                r.copy(items = r.items.map { if (it.documentUri == uri) it.copy(accept = !it.accept) else it })
            } ?: current
        }
    }

    fun commitLyricsReview() {
        val review = _lyricsBatch.value as? LyricsBatchPhase.Review ?: return
        val tracks = ui.value?.tracks ?: return
        val byUri = tracks.associateBy { it.documentUri }
        val accepted = review.items.filter { it.accept }
        val skipped = review.items.size - accepted.size
        viewModelScope.launch {
            val s = settings.settings.first()
            val saved = java.util.concurrent.atomic.AtomicInteger(0)
            val failed = java.util.concurrent.atomic.AtomicInteger(review.failedCount)
            _lyricsBatch.value = LyricsBatchPhase.Writing(
                accepted.map { BatchItem(it.documentUri, it.title, DownloadStatus.PENDING) },
            )

            fun setStatus(uri: String, status: DownloadStatus) {
                _lyricsBatch.update { current ->
                    (current as? LyricsBatchPhase.Writing)?.let { w ->
                        w.copy(items = w.items.map { if (it.documentUri == uri) it.copy(status = status) else it })
                    } ?: current
                }
            }

            kotlinx.coroutines.coroutineScope {
                accepted.map { item ->
                    async(kotlinx.coroutines.Dispatchers.IO) {
                        val track = byUri[item.documentUri]
                        if (track == null) {
                            failed.incrementAndGet()
                            setStatus(item.documentUri, DownloadStatus.FAILED)
                            return@async
                        }
                        setStatus(item.documentUri, DownloadStatus.DOWNLOADING)
                        runCatching {
                            val treeUri = track.treeUri.toUri()
                            val docUri = track.documentUri.toUri()
                            lrcWriter.write(treeUri, docUri, track.displayName, item.fullText)
                            var newMod = track.lastModified
                            var newSize = track.sizeBytes
                            if (s.embedLyricsInTags) {
                                val sig = tagWriter.writeLyrics(
                                    docUri, track.displayName, item.fullText,
                                    expected = FileSignature(track.lastModified, track.sizeBytes),
                                )
                                newMod = sig.lastModified
                                newSize = sig.sizeOr(track.sizeBytes)
                            }
                            trackDao.upsertAll(listOf(
                                track.copy(
                                    hasSidecarLrc = true,
                                    sidecarLrcSynced = item.isSynced,
                                    lastModified = newMod,
                                    sizeBytes = newSize,
                                    scannedAt = System.currentTimeMillis(),
                                ),
                            ))
                        }.onSuccess {
                            saved.incrementAndGet()
                            setStatus(item.documentUri, DownloadStatus.SAVED)
                        }.onFailure {
                            failed.incrementAndGet()
                            setStatus(item.documentUri, DownloadStatus.FAILED)
                        }
                    }
                }.awaitAll()
            }

            _lyricsBatch.value = LyricsBatchPhase.Done(
                saved = saved.get(),
                skipped = skipped,
                noMatch = review.noMatchCount,
                failed = failed.get(),
            )
        }
    }

    fun dismissLyricsBatch() { _lyricsBatch.value = LyricsBatchPhase.Idle }
}

private fun summarize(albumKey: String, tracks: List<TrackEntity>, lowResThresholdPx: Int): AlbumDetailUi {
    val distinctArtists = tracks
        .map { (it.albumArtist?.takeIf(String::isNotBlank) ?: it.artist ?: "").trim() }
        .filter { it.isNotEmpty() }
        .distinct()
    val mixed = distinctArtists.size > 1

    val pendingArt = tracks.count { it.artScanPending }
    val withArt = tracks.count { it.hasEmbeddedArt }
    val artStatus = when {
        pendingArt > 0 -> AlbumArtStatus.PENDING
        withArt == 0 -> AlbumArtStatus.ALL_MISSING
        withArt < tracks.size -> AlbumArtStatus.PARTIAL
        else -> AlbumArtStatus.ALL_OK
    }

    val withLyrics = tracks.count { it.hasSidecarLrc }
    val synced = tracks.count { it.hasSidecarLrc && it.sidecarLrcSynced }
    val lyricsStatus = when {
        withLyrics == 0 -> AlbumLyricsStatus.NONE
        withLyrics < tracks.size -> AlbumLyricsStatus.PARTIAL
        synced == tracks.size -> AlbumLyricsStatus.ALL_SYNCED
        else -> AlbumLyricsStatus.ALL_PRESENT
    }

    val okTags = tracks.count { it.coreTagsComplete && !it.artistUnknown }
    val tagStatus = when {
        okTags == tracks.size -> AlbumTagStatus.ALL_OK
        okTags == 0 -> AlbumTagStatus.ALL_BAD
        else -> AlbumTagStatus.PARTIAL
    }

    return AlbumDetailUi(
        albumKey = albumKey,
        albumLabel = tracks.firstNotNullOfOrNull { it.album?.takeIf(String::isNotBlank) } ?: tracks.first().albumLabel,
        artistLabel = if (mixed) "Various Artists" else (distinctArtists.firstOrNull() ?: "Unknown artist"),
        year = tracks.firstNotNullOfOrNull { it.year?.takeIf(String::isNotBlank) },
        trackCount = tracks.size,
        coverThumbnailPath = tracks.firstNotNullOfOrNull { it.thumbnailPath },
        artStatus = artStatus,
        lyricsStatus = lyricsStatus,
        tagStatus = tagStatus,
        mixedArtist = mixed,
        tracks = tracks,
        lowResThresholdPx = lowResThresholdPx,
    )
}
