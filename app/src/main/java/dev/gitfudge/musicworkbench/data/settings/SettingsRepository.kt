package dev.gitfudge.musicworkbench.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.gitfudge.musicworkbench.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** User-tunable knobs and the persisted music folder. */
data class Settings(
    val musicTreeUri: String?,
    /**
     * The tree URI that has already been fully scanned at least once. Used to
     * decide whether a scan should run automatically: the first scan (during
     * onboarding) and a watch-path change auto-scan; every later launch of an
     * already-scanned folder relies on the persisted DB and a manual rescan.
     */
    val lastScannedTreeUri: String?,
    val embedLyricsInTags: Boolean,
    val lowResThresholdPx: Int,
    val themeMode: ThemeMode,
    /** False until the first-run walkthrough is finished or skipped. */
    val hasSeenWalkthrough: Boolean,
)

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val MusicTreeUri = stringPreferencesKey("music_tree_uri")
        val LastScannedTreeUri = stringPreferencesKey("last_scanned_tree_uri")
        val EmbedLyrics = booleanPreferencesKey("embed_lyrics_in_tags")
        val LowResThreshold = intPreferencesKey("low_res_threshold_px")
        val ThemeMode = stringPreferencesKey("theme_mode")
        val HasSeenWalkthrough = booleanPreferencesKey("has_seen_walkthrough")
    }

    val settings: Flow<Settings> = dataStore.data.map { prefs ->
        Settings(
            musicTreeUri = prefs[Keys.MusicTreeUri],
            lastScannedTreeUri = prefs[Keys.LastScannedTreeUri],
            embedLyricsInTags = prefs[Keys.EmbedLyrics] ?: true,
            lowResThresholdPx = prefs[Keys.LowResThreshold] ?: DEFAULT_LOW_RES_PX,
            themeMode = prefs[Keys.ThemeMode]?.toThemeModeOrNull() ?: ThemeMode.System,
            hasSeenWalkthrough = prefs[Keys.HasSeenWalkthrough] ?: false,
        )
    }

    suspend fun setMusicTreeUri(uri: String) {
        dataStore.edit { it[Keys.MusicTreeUri] = uri }
    }

    suspend fun clearMusicTreeUri() {
        dataStore.edit { it.remove(Keys.MusicTreeUri) }
    }

    suspend fun setLastScannedTreeUri(uri: String) {
        dataStore.edit { it[Keys.LastScannedTreeUri] = uri }
    }

    suspend fun setEmbedLyricsInTags(enabled: Boolean) {
        dataStore.edit { it[Keys.EmbedLyrics] = enabled }
    }

    suspend fun setLowResThresholdPx(px: Int) {
        dataStore.edit { it[Keys.LowResThreshold] = px }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.ThemeMode] = mode.name }
    }

    suspend fun setHasSeenWalkthrough(seen: Boolean) {
        dataStore.edit { it[Keys.HasSeenWalkthrough] = seen }
    }

    companion object {
        const val DEFAULT_LOW_RES_PX = 600
    }
}

private fun String.toThemeModeOrNull(): ThemeMode? =
    runCatching { ThemeMode.valueOf(this) }.getOrNull()
