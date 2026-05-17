package dev.gitfudge.musicworkbench.ui.album

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gitfudge.musicworkbench.data.db.TrackDao
import dev.gitfudge.musicworkbench.data.db.TrackEntity
import dev.gitfudge.musicworkbench.data.settings.SettingsRepository
import dev.gitfudge.musicworkbench.domain.AlbumArtStatus
import dev.gitfudge.musicworkbench.domain.AlbumLyricsStatus
import dev.gitfudge.musicworkbench.domain.AlbumTagStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val settings: SettingsRepository,
    private val trackDao: TrackDao,
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
