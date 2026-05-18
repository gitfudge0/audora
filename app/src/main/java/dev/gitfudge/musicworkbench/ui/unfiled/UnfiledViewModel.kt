package dev.gitfudge.musicworkbench.ui.unfiled

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gitfudge.musicworkbench.data.db.TrackDao
import dev.gitfudge.musicworkbench.data.db.TrackEntity
import dev.gitfudge.musicworkbench.data.settings.SettingsRepository
import dev.gitfudge.musicworkbench.data.tags.BulkTagApplier
import dev.gitfudge.musicworkbench.domain.BulkTagEdits
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backing VM for the "Unfiled" screen — a flat list of tracks that have no
 * album tag (or whose album resolves to empty). Surfaced via the pinned
 * Unfiled row on the Albums tab. Supports multi-select + bulk tag editing so
 * the user can file a batch of orphans under an album in one go.
 */
@HiltViewModel
class UnfiledViewModel @Inject constructor(
    settings: SettingsRepository,
    private val trackDao: TrackDao,
    private val bulkTagApplier: BulkTagApplier,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val tracks: StateFlow<List<TrackEntity>> = settings.settings
        .flatMapLatest { s ->
            val uri = s.musicTreeUri ?: return@flatMapLatest flowOf(emptyList())
            trackDao.observeUnfiledTracks(uri)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val lowResThresholdPx: StateFlow<Int> = settings.settings
        .map { it.lowResThresholdPx }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 600)

    // ── Selection ─────────────────────────────────────────────────────────────

    private val _selectedUris = MutableStateFlow(emptySet<String>())
    val selectedUris: StateFlow<Set<String>> = _selectedUris.asStateFlow()

    val isSelecting: StateFlow<Boolean> = _selectedUris
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun enterSelectionWith(uri: String) { _selectedUris.value = setOf(uri) }
    fun toggleSelection(uri: String) {
        _selectedUris.update { if (uri in it) it - uri else it + uri }
    }
    fun clearSelection() { _selectedUris.value = emptySet() }

    // ── Bulk tag edit ─────────────────────────────────────────────────────────

    private val _bulkEditorOpen = MutableStateFlow(false)
    val bulkEditorOpen: StateFlow<Boolean> = _bulkEditorOpen.asStateFlow()

    private val _bulkEditorTracks = MutableStateFlow<List<TrackEntity>>(emptyList())
    val bulkEditorTracks: StateFlow<List<TrackEntity>> = _bulkEditorTracks.asStateFlow()

    private val _bulkEditState = MutableStateFlow<BulkEditState>(BulkEditState.Idle)
    val bulkEditState: StateFlow<BulkEditState> = _bulkEditState.asStateFlow()

    fun openBulkEditor() {
        val selected = _selectedUris.value
        if (selected.isEmpty()) return
        viewModelScope.launch {
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

    sealed interface BulkEditState {
        data object Idle : BulkEditState
        data class Running(val done: Int, val total: Int) : BulkEditState
        data class Done(val ok: Int, val failed: Int) : BulkEditState
    }
}
