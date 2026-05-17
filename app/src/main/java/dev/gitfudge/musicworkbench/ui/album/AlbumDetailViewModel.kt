package dev.gitfudge.musicworkbench.ui.album

import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gitfudge.musicworkbench.data.art.CoverArtCandidate
import dev.gitfudge.musicworkbench.data.art.CoverArtRepository
import dev.gitfudge.musicworkbench.data.db.TrackDao
import dev.gitfudge.musicworkbench.data.db.TrackEntity
import dev.gitfudge.musicworkbench.data.lyrics.LrcWriter
import dev.gitfudge.musicworkbench.data.lyrics.LrclibRepository
import dev.gitfudge.musicworkbench.data.settings.SettingsRepository
import dev.gitfudge.musicworkbench.data.tags.TagEdits
import dev.gitfudge.musicworkbench.data.tags.TagWriter
import dev.gitfudge.musicworkbench.domain.AlbumArtStatus
import dev.gitfudge.musicworkbench.domain.AlbumLyricsStatus
import dev.gitfudge.musicworkbench.domain.AlbumTagStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
)

// ── Hero art ─────────────────────────────────────────────────────────────────

sealed interface AlbumArtFlowState {
    data object Idle : AlbumArtFlowState
    data object Searching : AlbumArtFlowState
    data class Picker(val candidates: List<CoverArtCandidate>) : AlbumArtFlowState
    data class Preview(val candidate: CoverArtCandidate, val bytes: ByteArray) : AlbumArtFlowState
    data class Writing(val done: Int, val total: Int) : AlbumArtFlowState
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

sealed interface LyricsBatchPhase {
    data object Idle : LyricsBatchPhase
    data class Fetching(val done: Int, val total: Int) : LyricsBatchPhase
    data class Review(
        val items: List<LyricsReviewItem>,
        val noMatchCount: Int,
        val failedCount: Int,
    ) : LyricsBatchPhase
    data class Writing(val done: Int, val total: Int) : LyricsBatchPhase
    data class Done(val saved: Int, val skipped: Int, val noMatch: Int, val failed: Int) : LyricsBatchPhase
}

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val settings: SettingsRepository,
    private val trackDao: TrackDao,
    private val tagWriter: TagWriter,
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
            val uri = s.musicTreeUri ?: return@flatMapLatest flowOf(emptyList())
            trackDao.observeTracksInAlbum(uri, albumKey)
        }
        .map { tracks -> if (tracks.isEmpty()) null else summarize(albumKey, tracks) }
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
        viewModelScope.launch {
            _tagWriteInFlight.value = true
            runCatching {
                tracks.forEach { track ->
                    val newAlbum = edits.album ?: track.album ?: ""
                    val newAlbumArtist = edits.albumArtist ?: track.albumArtist ?: ""
                    val newYear = edits.year ?: track.year ?: ""
                    val newGenre = edits.genre ?: track.genre ?: ""
                    val tagEdits = TagEdits(
                        title = track.title ?: "",
                        artist = track.artist ?: "",
                        album = newAlbum,
                        albumArtist = newAlbumArtist,
                        trackNumber = track.trackNumber?.toString() ?: "",
                        discNumber = track.discNumber?.toString() ?: "",
                        year = newYear,
                        genre = newGenre,
                    )
                    val newMod = tagWriter.write(track.documentUri.toUri(), track.displayName, tagEdits)
                    val resolvedAlbum = newAlbum.ifBlank { track.album }
                    val newKey = buildAlbumKey(
                        albumArtist = newAlbumArtist.ifBlank { track.albumArtist },
                        artist = track.artist,
                        album = resolvedAlbum,
                    )
                    trackDao.upsertAll(listOf(
                        track.copy(
                            album = resolvedAlbum,
                            albumArtist = newAlbumArtist.ifBlank { track.albumArtist },
                            year = newYear.ifBlank { track.year },
                            genre = newGenre.ifBlank { track.genre },
                            albumKey = newKey,
                            albumLabel = resolvedAlbum ?: track.albumLabel,
                            lastModified = newMod,
                            scannedAt = System.currentTimeMillis(),
                        ),
                    ))
                }
            }
            _tagWriteInFlight.value = false
            _tagEditorOpen.value = false
        }
    }

    // ── Hero art ─────────────────────────────────────────────────────────────

    private val _artFlow = MutableStateFlow<AlbumArtFlowState>(AlbumArtFlowState.Idle)
    val artFlow: StateFlow<AlbumArtFlowState> = _artFlow.asStateFlow()

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
        viewModelScope.launch {
            val bytes = runCatching { coverArtRepository.downloadFullRes(candidate) }.getOrNull()
            if (bytes == null) {
                _artFlow.value = AlbumArtFlowState.NoMatch("Could not download that image")
                return@launch
            }
            _artFlow.value = AlbumArtFlowState.Preview(candidate, bytes)
        }
    }

    fun confirmHeroArt() {
        val preview = _artFlow.value as? AlbumArtFlowState.Preview ?: return
        val tracks = ui.value?.tracks ?: return
        viewModelScope.launch {
            _artFlow.value = AlbumArtFlowState.Writing(0, tracks.size)
            var done = 0
            tracks.forEach { track ->
                runCatching {
                    val res = tagWriter.writeArtFromBytes(track.documentUri.toUri(), track.displayName, preview.bytes)
                    trackDao.upsertAll(listOf(
                        track.copy(
                            hasEmbeddedArt = true,
                            artWidth = res.artWidth,
                            artHeight = res.artHeight,
                            thumbnailPath = res.thumbnailPath,
                            lastModified = res.lastModified,
                            scannedAt = System.currentTimeMillis(),
                        ),
                    ))
                }
                done++
                _artFlow.value = AlbumArtFlowState.Writing(done, tracks.size)
            }
            _artFlow.value = AlbumArtFlowState.Idle
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
        viewModelScope.launch {
            val total = tracks.size
            var done = 0
            var noMatch = 0
            var failed = 0
            val reviewItems = mutableListOf<LyricsReviewItem>()
            _lyricsBatch.value = LyricsBatchPhase.Fetching(0, total)
            tracks.forEach { track ->
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
                        noMatch++
                    } else {
                        val isSynced = !result.syncedLyrics.isNullOrBlank()
                        reviewItems += LyricsReviewItem(
                            documentUri = track.documentUri,
                            title = track.title ?: track.displayName,
                            artist = track.artist ?: "",
                            previewText = text.lineSequence().take(3).joinToString("\n"),
                            fullText = text,
                            isSynced = isSynced,
                            accept = true,
                        )
                    }
                }.onFailure { failed++ }
                done++
                _lyricsBatch.value = LyricsBatchPhase.Fetching(done, total)
            }
            _lyricsBatch.value = if (reviewItems.isEmpty()) {
                LyricsBatchPhase.Done(saved = 0, skipped = 0, noMatch = noMatch, failed = failed)
            } else {
                LyricsBatchPhase.Review(reviewItems, noMatchCount = noMatch, failedCount = failed)
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
            var done = 0
            var saved = 0
            var failed = review.failedCount
            _lyricsBatch.value = LyricsBatchPhase.Writing(0, accepted.size)
            accepted.forEach { item ->
                val track = byUri[item.documentUri] ?: return@forEach
                runCatching {
                    val treeUri = track.treeUri.toUri()
                    val docUri = track.documentUri.toUri()
                    lrcWriter.write(treeUri, docUri, track.displayName, item.fullText)
                    var newMod = track.lastModified
                    if (s.embedLyricsInTags) {
                        newMod = tagWriter.writeLyrics(docUri, track.displayName, item.fullText)
                    }
                    trackDao.upsertAll(listOf(
                        track.copy(
                            hasSidecarLrc = true,
                            sidecarLrcSynced = item.isSynced,
                            lastModified = newMod,
                            scannedAt = System.currentTimeMillis(),
                        ),
                    ))
                    saved++
                }.onFailure { failed++ }
                done++
                _lyricsBatch.value = LyricsBatchPhase.Writing(done, accepted.size)
            }
            _lyricsBatch.value = LyricsBatchPhase.Done(
                saved = saved,
                skipped = skipped,
                noMatch = review.noMatchCount,
                failed = failed,
            )
        }
    }

    fun dismissLyricsBatch() { _lyricsBatch.value = LyricsBatchPhase.Idle }
}

private fun summarize(albumKey: String, tracks: List<TrackEntity>): AlbumDetailUi {
    val distinctArtists = tracks
        .map { (it.albumArtist?.takeIf(String::isNotBlank) ?: it.artist ?: "").trim() }
        .filter { it.isNotEmpty() }
        .distinct()
    val mixed = distinctArtists.size > 1

    val withArt = tracks.count { it.hasEmbeddedArt }
    val artStatus = when {
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
    )
}

/** Matches MediaScanner.kt: concatenated lowercase, no separator. */
private fun buildAlbumKey(albumArtist: String?, artist: String?, album: String?): String {
    val groupArtist = (albumArtist ?: artist ?: "").lowercase()
    val groupAlbum = album.orEmpty().lowercase()
    return "$groupArtist$groupAlbum"
}
