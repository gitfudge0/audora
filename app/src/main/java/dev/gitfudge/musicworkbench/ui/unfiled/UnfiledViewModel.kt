package dev.gitfudge.musicworkbench.ui.unfiled

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gitfudge.musicworkbench.data.db.TrackDao
import dev.gitfudge.musicworkbench.data.db.TrackEntity
import dev.gitfudge.musicworkbench.data.settings.SettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Backing VM for the "Unfiled" screen — a flat list of tracks that have no
 * album tag (or whose album resolves to empty). Surfaced via the pinned
 * Unfiled row on the Albums tab.
 */
@HiltViewModel
class UnfiledViewModel @Inject constructor(
    settings: SettingsRepository,
    trackDao: TrackDao,
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
}
