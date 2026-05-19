package dev.gitfudge.audora.ui.detail

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gitfudge.audora.data.db.TrackDao
import dev.gitfudge.audora.data.db.TrackEntity
import dev.gitfudge.audora.data.art.CoverArtCandidate
import dev.gitfudge.audora.data.art.CoverArtRepository
import dev.gitfudge.audora.data.lyrics.LrcWriter
import dev.gitfudge.audora.data.lyrics.LrclibRepository
import dev.gitfudge.audora.data.lyrics.LrclibResult
import dev.gitfudge.audora.data.settings.SettingsRepository
import dev.gitfudge.audora.data.tags.TagEdits
import dev.gitfudge.audora.data.tags.TagWriter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Tag form ─────────────────────────────────────────────────────────────────

data class TagFormState(
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val albumArtist: String = "",
    val trackNumber: String = "",
    val discNumber: String = "",
    val year: String = "",
    val genre: String = "",
    val composer: String = "",
    val comment: String = "",
    val compilation: Boolean = false,
) {
    fun toTagEdits() = TagEdits(
        title, artist, album, albumArtist, trackNumber, discNumber, year, genre,
        composer, comment, compilation,
    )
}

data class FieldChange(val label: String, val old: String, val new: String)

// ── Shared save state ─────────────────────────────────────────────────────────

sealed interface DetailSaveState {
    data object Idle : DetailSaveState
    data object Saving : DetailSaveState
    data object Done : DetailSaveState
    data class Failed(val message: String) : DetailSaveState
}

// ── Lyrics ────────────────────────────────────────────────────────────────────

sealed interface LyricsState {
    data object Idle : LyricsState
    data object Fetching : LyricsState
    data class Preview(val result: LrclibResult, val isSynced: Boolean) : LyricsState
    data object NoResult : LyricsState
    data class Error(val message: String) : LyricsState
}

// ── Art fetch (web) ───────────────────────────────────────────────────────────

sealed interface ArtFetchState {
    data object Idle : ArtFetchState
    data object Fetching : ArtFetchState
    data class Found(val candidates: List<CoverArtCandidate>) : ArtFetchState
    data object Downloading : ArtFetchState
    data class Previewing(
        val candidate: CoverArtCandidate,
        val bytes: ByteArray,
        val originalCandidates: List<CoverArtCandidate>,
    ) : ArtFetchState {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Previewing) return false
            return candidate == other.candidate &&
                bytes.contentEquals(other.bytes) &&
                originalCandidates == other.originalCandidates
        }
        override fun hashCode(): Int {
            var r = candidate.hashCode()
            r = 31 * r + bytes.contentHashCode()
            r = 31 * r + originalCandidates.hashCode()
            return r
        }
    }
    data object NotFound : ArtFetchState
    data class Error(val message: String) : ArtFetchState
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class TrackDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val trackDao: TrackDao,
    private val tagWriter: TagWriter,
    private val lrclibRepo: LrclibRepository,
    private val lrcWriter: LrcWriter,
    private val lrcReader: dev.gitfudge.audora.data.lyrics.LrcReader,
    private val settings: SettingsRepository,
    private val coverArtRepo: CoverArtRepository,
) : ViewModel() {

    val documentUri: String = checkNotNull(savedStateHandle["uri"])

    val track: StateFlow<TrackEntity?> = trackDao.observeTrack(documentUri)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // ── Tags ──────────────────────────────────────────────────────────────────

    private val _original = MutableStateFlow(TagFormState())
    private val _form = MutableStateFlow(TagFormState())
    val form: StateFlow<TagFormState> = _form.asStateFlow()

    private val _tagSaveState = MutableStateFlow<DetailSaveState>(DetailSaveState.Idle)
    val tagSaveState: StateFlow<DetailSaveState> = _tagSaveState.asStateFlow()

    val isDirty: StateFlow<Boolean> = combine(_form, _original) { f, o -> f != o }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val changes: StateFlow<List<FieldChange>> = combine(_form, _original) { f, o ->
        buildList {
            fun check(label: String, old: String, new: String) {
                if (old.trim() != new.trim()) add(FieldChange(label, old, new))
            }
            check("Title", o.title, f.title)
            check("Artist", o.artist, f.artist)
            check("Album", o.album, f.album)
            check("Album artist", o.albumArtist, f.albumArtist)
            check("Track #", o.trackNumber, f.trackNumber)
            check("Disc #", o.discNumber, f.discNumber)
            check("Year", o.year, f.year)
            check("Genre", o.genre, f.genre)
            check("Composer", o.composer, f.composer)
            check("Comment", o.comment, f.comment)
            if (o.compilation != f.compilation) {
                add(FieldChange("Compilation", o.compilation.toString(), f.compilation.toString()))
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var initialized = false

    init {
        viewModelScope.launch {
            track.filterNotNull().first().let { t ->
                if (!initialized) {
                    val fs = t.toFormState()
                    _original.value = fs
                    _form.value = fs
                    initialized = true
                }
            }
        }
    }

    fun setTitle(v: String) { _form.update { it.copy(title = v) } }
    fun setArtist(v: String) { _form.update { it.copy(artist = v) } }
    fun setAlbum(v: String) { _form.update { it.copy(album = v) } }
    fun setAlbumArtist(v: String) { _form.update { it.copy(albumArtist = v) } }
    fun setTrackNumber(v: String) { _form.update { it.copy(trackNumber = v) } }
    fun setDiscNumber(v: String) { _form.update { it.copy(discNumber = v) } }
    fun setYear(v: String) { _form.update { it.copy(year = v) } }
    fun setGenre(v: String) { _form.update { it.copy(genre = v) } }
    fun setComposer(v: String) { _form.update { it.copy(composer = v) } }
    fun setComment(v: String) { _form.update { it.copy(comment = v) } }
    fun setCompilation(v: Boolean) { _form.update { it.copy(compilation = v) } }

    /** Entity + form as they were immediately before the last tag write, for undo. */
    private var preTagWriteEntity: TrackEntity? = null
    private var preTagWriteForm: TagFormState? = null
    private val _canUndoTags = MutableStateFlow(false)
    val canUndoTags: StateFlow<Boolean> = _canUndoTags.asStateFlow()

    fun saveTags() {
        if (_tagSaveState.value is DetailSaveState.Saving) return
        val t = track.value ?: return
        val form = _form.value
        viewModelScope.launch {
            _tagSaveState.value = DetailSaveState.Saving
            runCatching {
                val newMod = tagWriter.write(
                    documentUri.toUri(), t.displayName, form.toTagEdits(),
                    expectedLastModified = t.lastModified,
                )
                preTagWriteEntity = t
                preTagWriteForm = _original.value
                trackDao.upsertAll(listOf(buildUpdatedEntity(t, form, newMod)))
                _original.value = form
            }.fold(
                onSuccess = {
                    _canUndoTags.value = true
                    _tagSaveState.value = DetailSaveState.Done
                },
                onFailure = { _tagSaveState.value = DetailSaveState.Failed(it.message ?: "Save failed") },
            )
        }
    }

    /** The 30s undo: restore original file bytes and revert the DB row. */
    fun undoTags() {
        val prev = preTagWriteEntity ?: return
        viewModelScope.launch {
            runCatching {
                val restoredMod = tagWriter.restoreLastWrite(documentUri.toUri())
                trackDao.upsertAll(listOf(
                    prev.copy(
                        lastModified = restoredMod ?: prev.lastModified,
                        scannedAt = System.currentTimeMillis(),
                    ),
                ))
                preTagWriteForm?.let { f -> _original.value = f; _form.value = f }
            }
            _canUndoTags.value = false
            preTagWriteEntity = null
        }
    }

    fun dismissTagSave() { _tagSaveState.value = DetailSaveState.Idle }

    // ── Lyrics ────────────────────────────────────────────────────────────────

    private val _lyricsState = MutableStateFlow<LyricsState>(LyricsState.Idle)
    val lyricsState: StateFlow<LyricsState> = _lyricsState.asStateFlow()

    private val _lyricsSaveState = MutableStateFlow<DetailSaveState>(DetailSaveState.Idle)
    val lyricsSaveState: StateFlow<DetailSaveState> = _lyricsSaveState.asStateFlow()

    fun fetchLyrics() {
        if (_lyricsState.value is LyricsState.Fetching) return
        val t = track.value ?: return
        viewModelScope.launch {
            _lyricsState.value = LyricsState.Fetching
            runCatching {
                lrclibRepo.fetch(
                    title = t.title ?: t.displayName.substringBeforeLast('.'),
                    artist = t.artist ?: "",
                    album = t.album,
                    durationMs = t.durationMs,
                )
            }.fold(
                onSuccess = { result ->
                    _lyricsState.value = when {
                        result == null -> LyricsState.NoResult
                        result.instrumental -> LyricsState.NoResult
                        else -> LyricsState.Preview(
                            result = result,
                            isSynced = !result.syncedLyrics.isNullOrBlank(),
                        )
                    }
                },
                onFailure = { _lyricsState.value = LyricsState.Error(it.message ?: "Fetch failed") },
            )
        }
    }

    fun saveLyrics() {
        val state = _lyricsState.value as? LyricsState.Preview ?: return
        if (_lyricsSaveState.value is DetailSaveState.Saving) return
        val t = track.value ?: return
        val lyricsText = if (state.isSynced) state.result.syncedLyrics!! else state.result.plainLyrics ?: ""
        viewModelScope.launch {
            _lyricsSaveState.value = DetailSaveState.Saving
            runCatching {
                val treeUri = t.treeUri.toUri()
                val docUri = documentUri.toUri()

                lrcWriter.write(treeUri, docUri, t.displayName, lyricsText)

                val s = settings.settings.first()
                var newMod = t.lastModified
                if (s.embedLyricsInTags) {
                    newMod = tagWriter.writeLyrics(
                        docUri, t.displayName, lyricsText,
                        expectedLastModified = t.lastModified,
                    )
                }

                trackDao.upsertAll(listOf(
                    t.copy(
                        hasSidecarLrc = true,
                        sidecarLrcSynced = state.isSynced,
                        lastModified = newMod,
                        scannedAt = System.currentTimeMillis(),
                    ),
                ))
                _lyricsState.value = LyricsState.Idle
            }.fold(
                onSuccess = { _lyricsSaveState.value = DetailSaveState.Done },
                onFailure = { _lyricsSaveState.value = DetailSaveState.Failed(it.message ?: "Save failed") },
            )
        }
    }

    fun dismissLyrics() { _lyricsState.value = LyricsState.Idle }
    fun dismissLyricsSave() { _lyricsSaveState.value = DetailSaveState.Idle }

    // ── Manual lyrics editing ─────────────────────────────────────────────────

    /** null = not editing; non-null = the editable buffer. */
    private val _manualLyrics = MutableStateFlow<String?>(null)
    val manualLyrics: StateFlow<String?> = _manualLyrics.asStateFlow()

    fun startEditLyrics() {
        val t = track.value ?: return
        viewModelScope.launch {
            val existing = runCatching {
                lrcReader.read(t.treeUri.toUri(), documentUri.toUri(), t.displayName)
            }.getOrNull().orEmpty()
            _manualLyrics.value = existing
        }
    }

    fun setManualLyrics(text: String) { _manualLyrics.value = text }
    fun cancelEditLyrics() { _manualLyrics.value = null }

    fun saveManualLyrics() {
        val text = _manualLyrics.value ?: return
        if (_lyricsSaveState.value is DetailSaveState.Saving) return
        val t = track.value ?: return
        viewModelScope.launch {
            _lyricsSaveState.value = DetailSaveState.Saving
            runCatching {
                val docUri = documentUri.toUri()
                val synced = lrcReader.isSynced(text)
                lrcWriter.write(t.treeUri.toUri(), docUri, t.displayName, text)
                val s = settings.settings.first()
                var newMod = t.lastModified
                if (s.embedLyricsInTags) {
                    newMod = tagWriter.writeLyrics(
                        docUri, t.displayName, text,
                        expectedLastModified = t.lastModified,
                    )
                }
                trackDao.upsertAll(listOf(
                    t.copy(
                        hasSidecarLrc = text.isNotBlank(),
                        sidecarLrcSynced = synced,
                        lastModified = newMod,
                        scannedAt = System.currentTimeMillis(),
                    ),
                ))
                _manualLyrics.value = null
            }.fold(
                onSuccess = { _lyricsSaveState.value = DetailSaveState.Done },
                onFailure = { _lyricsSaveState.value = DetailSaveState.Failed(it.message ?: "Save failed") },
            )
        }
    }

    // ── Art ───────────────────────────────────────────────────────────────────

    private val _pendingArtUri = MutableStateFlow<Uri?>(null)
    val pendingArtUri: StateFlow<Uri?> = _pendingArtUri.asStateFlow()

    private val _artSaveState = MutableStateFlow<DetailSaveState>(DetailSaveState.Idle)
    val artSaveState: StateFlow<DetailSaveState> = _artSaveState.asStateFlow()

    private val _artFetchState = MutableStateFlow<ArtFetchState>(ArtFetchState.Idle)
    val artFetchState: StateFlow<ArtFetchState> = _artFetchState.asStateFlow()

    fun setPendingArt(uri: Uri) { _pendingArtUri.value = uri }
    fun clearPendingArt() { _pendingArtUri.value = null }

    fun saveArt() {
        val artUri = _pendingArtUri.value ?: return
        if (_artSaveState.value is DetailSaveState.Saving) return
        val t = track.value ?: return
        viewModelScope.launch {
            _artSaveState.value = DetailSaveState.Saving
            runCatching {
                val result = tagWriter.writeArt(
                    documentUri.toUri(), t.displayName, artUri,
                    expectedLastModified = t.lastModified,
                )
                trackDao.upsertAll(listOf(
                    t.copy(
                        hasEmbeddedArt = true,
                        artWidth = result.artWidth,
                        artHeight = result.artHeight,
                        thumbnailPath = result.thumbnailPath,
                        artScanPending = false,
                        lastModified = result.lastModified,
                        scannedAt = System.currentTimeMillis(),
                    ),
                ))
                _pendingArtUri.value = null
            }.fold(
                onSuccess = { _artSaveState.value = DetailSaveState.Done },
                onFailure = { _artSaveState.value = DetailSaveState.Failed(it.message ?: "Save failed") },
            )
        }
    }

    fun dismissArtSave() { _artSaveState.value = DetailSaveState.Idle }

    fun fetchArt() {
        if (_artFetchState.value is ArtFetchState.Fetching) return
        val t = track.value ?: return
        val album = t.album ?: t.displayName.substringBeforeLast('.')
        val artist = t.artist ?: ""
        viewModelScope.launch {
            _artFetchState.value = ArtFetchState.Fetching
            runCatching {
                coverArtRepo.searchCandidates(album, artist)
            }.fold(
                onSuccess = { candidates ->
                    _artFetchState.value = if (candidates.isEmpty()) ArtFetchState.NotFound
                    else ArtFetchState.Found(candidates)
                },
                onFailure = { _artFetchState.value = ArtFetchState.Error(it.message ?: "Fetch failed") },
            )
        }
    }

    fun selectCandidate(candidate: CoverArtCandidate) {
        val current = _artFetchState.value
        if (current is ArtFetchState.Downloading) return
        val originals = when (current) {
            is ArtFetchState.Found -> current.candidates
            is ArtFetchState.Previewing -> current.originalCandidates
            else -> return
        }
        viewModelScope.launch {
            _artFetchState.value = ArtFetchState.Downloading
            runCatching {
                coverArtRepo.downloadFullRes(candidate)
            }.fold(
                onSuccess = { bytes ->
                    _artFetchState.value = ArtFetchState.Previewing(candidate, bytes, originals)
                },
                onFailure = {
                    _artFetchState.value = ArtFetchState.Error(it.message ?: "Download failed")
                },
            )
        }
    }

    fun backToCandidates() {
        val current = _artFetchState.value as? ArtFetchState.Previewing ?: return
        _artFetchState.value = ArtFetchState.Found(current.originalCandidates)
    }

    fun applyPreviewedArt() {
        val preview = _artFetchState.value as? ArtFetchState.Previewing ?: return
        val t = track.value ?: return
        viewModelScope.launch {
            _artSaveState.value = DetailSaveState.Saving
            runCatching {
                val result = tagWriter.writeArtFromBytes(
                    documentUri.toUri(), t.displayName, preview.bytes,
                    expectedLastModified = t.lastModified,
                )
                trackDao.upsertAll(listOf(
                    t.copy(
                        hasEmbeddedArt = true,
                        artWidth = result.artWidth,
                        artHeight = result.artHeight,
                        thumbnailPath = result.thumbnailPath,
                        artScanPending = false,
                        lastModified = result.lastModified,
                        scannedAt = System.currentTimeMillis(),
                    ),
                ))
            }.fold(
                onSuccess = {
                    _artFetchState.value = ArtFetchState.Idle
                    _artSaveState.value = DetailSaveState.Done
                },
                onFailure = {
                    _artSaveState.value = DetailSaveState.Failed(it.message ?: "Save failed")
                },
            )
        }
    }

    fun dismissFetchArt() { _artFetchState.value = ArtFetchState.Idle }

    // ── Shared helpers ────────────────────────────────────────────────────────

    private fun buildUpdatedEntity(t: TrackEntity, form: TagFormState, newMod: Long): TrackEntity {
        val title = form.title.trim().ifEmpty { null }
        val artist = form.artist.trim().ifEmpty { null }
        val album = form.album.trim().ifEmpty { null }
        val albumArtist = form.albumArtist.trim().ifEmpty { null }
        val artistUnknown = artist == null ||
            artist.lowercase() in setOf("unknown", "unknown artist", "<unknown>")
        val groupArtist = (albumArtist ?: artist ?: "").lowercase()
        val groupAlbum = (album ?: t.parentPath.substringAfterLast('/')).lowercase()
        return t.copy(
            title = title,
            artist = artist,
            album = album,
            albumArtist = albumArtist,
            trackNumber = form.trackNumber.trim().takeWhile { it.isDigit() }.toIntOrNull(),
            discNumber = form.discNumber.trim().takeWhile { it.isDigit() }.toIntOrNull(),
            year = form.year.trim().ifEmpty { null },
            genre = form.genre.trim().ifEmpty { null },
            composer = form.composer.trim().ifEmpty { null },
            comment = form.comment.trim().ifEmpty { null },
            compilation = form.compilation,
            albumKey = "$groupArtist$groupAlbum",
            albumLabel = album ?: t.parentPath.substringAfterLast('/').ifEmpty { "Unknown album" },
            artistUnknown = artistUnknown,
            coreTagsComplete = !title.isNullOrBlank() && !artist.isNullOrBlank() && !album.isNullOrBlank(),
            lastModified = newMod,
            scannedAt = System.currentTimeMillis(),
        )
    }
}

private fun TrackEntity.toFormState() = TagFormState(
    title = title ?: "",
    artist = artist ?: "",
    album = album ?: "",
    albumArtist = albumArtist ?: "",
    trackNumber = trackNumber?.toString() ?: "",
    discNumber = discNumber?.toString() ?: "",
    year = year ?: "",
    genre = genre ?: "",
    composer = composer ?: "",
    comment = comment ?: "",
    compilation = compilation,
)
