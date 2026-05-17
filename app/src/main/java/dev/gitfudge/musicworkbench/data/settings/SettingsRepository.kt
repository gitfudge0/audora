package dev.gitfudge.musicworkbench.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** User-tunable knobs and the persisted music folder. */
data class Settings(
    val musicTreeUri: String?,
    val embedLyricsInTags: Boolean,
    val lowResThresholdPx: Int,
)

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val MusicTreeUri = stringPreferencesKey("music_tree_uri")
        val EmbedLyrics = booleanPreferencesKey("embed_lyrics_in_tags")
        val LowResThreshold = intPreferencesKey("low_res_threshold_px")
    }

    val settings: Flow<Settings> = dataStore.data.map { prefs ->
        Settings(
            musicTreeUri = prefs[Keys.MusicTreeUri],
            embedLyricsInTags = prefs[Keys.EmbedLyrics] ?: true,
            lowResThresholdPx = prefs[Keys.LowResThreshold] ?: DEFAULT_LOW_RES_PX,
        )
    }

    suspend fun setMusicTreeUri(uri: String) {
        dataStore.edit { it[Keys.MusicTreeUri] = uri }
    }

    suspend fun clearMusicTreeUri() {
        dataStore.edit { it.remove(Keys.MusicTreeUri) }
    }

    suspend fun setEmbedLyricsInTags(enabled: Boolean) {
        dataStore.edit { it[Keys.EmbedLyrics] = enabled }
    }

    suspend fun setLowResThresholdPx(px: Int) {
        dataStore.edit { it[Keys.LowResThreshold] = px }
    }

    companion object {
        const val DEFAULT_LOW_RES_PX = 600
    }
}
