package dev.gitfudge.audora.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import dev.gitfudge.audora.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** User-tunable knobs and the persisted music folders. */
data class Settings(
    // ponytail: unordered Set is fine for a union view; upgrade to an ordered List<String> if the Settings UI ever needs stable folder order.
    val musicTreeUris: Set<String>,
    /**
     * The tree URIs that have already been fully scanned at least once. Used to
     * decide whether a scan should run automatically: a newly-added folder
     * auto-scans once; every later launch of an already-scanned folder relies
     * on the persisted DB and a manual rescan.
     */
    val scannedTreeUris: Set<String>,
    val lowResThresholdPx: Int,
    val themeMode: ThemeMode,
    val autoSyncLyrics: Boolean,
    val includeEarlierFailedLyrics: Boolean,
    /** False until the first-run walkthrough is finished or skipped. */
    val hasSeenWalkthrough: Boolean,
    /** App version whose GitHub release notes have already been acknowledged. */
    val lastSeenReleaseVersion: String?,
)

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val MusicTreeUris = stringSetPreferencesKey("music_tree_uris")
        val ScannedTreeUris = stringSetPreferencesKey("scanned_tree_uris")
        // Legacy single-folder keys — read only, to migrate on first access.
        val LegacyMusicTreeUri = stringPreferencesKey("music_tree_uri")
        val LegacyLastScannedTreeUri = stringPreferencesKey("last_scanned_tree_uri")
        val LowResThreshold = intPreferencesKey("low_res_threshold_px")
        val ThemeMode = stringPreferencesKey("theme_mode")
        val AutoSyncLyrics = booleanPreferencesKey("auto_sync_lyrics")
        val IncludeEarlierFailedLyrics = booleanPreferencesKey("include_earlier_failed_lyrics")
        val HasSeenWalkthrough = booleanPreferencesKey("has_seen_walkthrough")
        val LastSeenReleaseVersion = stringPreferencesKey("last_seen_release_version")
    }

    val settings: Flow<Settings> = dataStore.data.map { prefs ->
        Settings(
            // One-time read migration: seed the new set from the old single URI.
            musicTreeUris = prefs.musicTreeUris(),
            scannedTreeUris = prefs.scannedTreeUris(),
            lowResThresholdPx = prefs[Keys.LowResThreshold] ?: DEFAULT_LOW_RES_PX,
            themeMode = prefs[Keys.ThemeMode]?.toThemeModeOrNull() ?: ThemeMode.System,
            autoSyncLyrics = prefs[Keys.AutoSyncLyrics] ?: true,
            includeEarlierFailedLyrics = prefs[Keys.IncludeEarlierFailedLyrics] ?: false,
            hasSeenWalkthrough = prefs[Keys.HasSeenWalkthrough] ?: false,
            lastSeenReleaseVersion = prefs[Keys.LastSeenReleaseVersion],
        )
    }

    suspend fun addMusicTreeUri(uri: String) {
        dataStore.edit { it[Keys.MusicTreeUris] = it.musicTreeUris() + uri }
    }

    suspend fun removeMusicTreeUri(uri: String) {
        dataStore.edit { it[Keys.MusicTreeUris] = it.musicTreeUris() - uri }
    }

    suspend fun markTreeScanned(uri: String) {
        dataStore.edit { it[Keys.ScannedTreeUris] = it.scannedTreeUris() + uri }
    }

    suspend fun unmarkTreeScanned(uri: String) {
        dataStore.edit { it[Keys.ScannedTreeUris] = it.scannedTreeUris() - uri }
    }

    private fun Preferences.musicTreeUris(): Set<String> =
        this[Keys.MusicTreeUris]
            ?: this[Keys.LegacyMusicTreeUri]?.let { setOf(it) }
            ?: emptySet()

    private fun Preferences.scannedTreeUris(): Set<String> =
        this[Keys.ScannedTreeUris]
            ?: this[Keys.LegacyLastScannedTreeUri]?.let { setOf(it) }
            ?: emptySet()

    suspend fun setLowResThresholdPx(px: Int) {
        dataStore.edit { it[Keys.LowResThreshold] = px }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.ThemeMode] = mode.name }
    }

    suspend fun setAutoSyncLyrics(enabled: Boolean) {
        dataStore.edit { it[Keys.AutoSyncLyrics] = enabled }
    }

    suspend fun setIncludeEarlierFailedLyrics(enabled: Boolean) {
        dataStore.edit { it[Keys.IncludeEarlierFailedLyrics] = enabled }
    }

    suspend fun setHasSeenWalkthrough(seen: Boolean) {
        dataStore.edit { it[Keys.HasSeenWalkthrough] = seen }
    }

    suspend fun setLastSeenReleaseVersion(version: String) {
        dataStore.edit { it[Keys.LastSeenReleaseVersion] = version }
    }

    companion object {
        const val DEFAULT_LOW_RES_PX = 600
    }
}

private fun String.toThemeModeOrNull(): ThemeMode? =
    runCatching { ThemeMode.valueOf(this) }.getOrNull()
