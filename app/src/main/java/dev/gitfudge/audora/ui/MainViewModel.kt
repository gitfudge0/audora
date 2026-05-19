package dev.gitfudge.audora.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gitfudge.audora.data.settings.SettingsRepository
import dev.gitfudge.audora.ui.theme.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RootUiState {
    data object Loading : RootUiState

    /** First-run (or replayed) feature tour, shown before the folder picker. */
    data object Walkthrough : RootUiState
    data object Onboarding : RootUiState
    data class Library(val folderLabel: String) : RootUiState

    /**
     * A folder was chosen but the persisted SAF grant is gone (revoked, app
     * data cleared, SD card remount). We must not silently fail writes — ask
     * the user to re-grant.
     */
    data class PermissionLost(val folderLabel: String) : RootUiState
}

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<RootUiState> = settings.settings
        .map { current ->
            val uri = current.musicTreeUri
            when {
                !current.hasSeenWalkthrough -> RootUiState.Walkthrough
                uri.isNullOrBlank() -> RootUiState.Onboarding
                !hasValidAccess(uri) ->
                    RootUiState.PermissionLost(folderLabel = resolveFolderLabel(uri))
                else -> RootUiState.Library(folderLabel = resolveFolderLabel(uri))
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RootUiState.Loading,
        )

    val themeMode: StateFlow<ThemeMode> = settings.settings
        .map { it.themeMode }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeMode.System,
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settings.setThemeMode(mode) }
    }

    fun setWalkthroughSeen(seen: Boolean) {
        viewModelScope.launch { settings.setHasSeenWalkthrough(seen) }
    }

    fun onFolderPicked(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
            }
            settings.setMusicTreeUri(uri.toString())
        }
    }

    fun changeFolder() {
        viewModelScope.launch { settings.clearMusicTreeUri() }
    }

    /**
     * The persisted grant must still be held *and* the tree readable. We
     * check both: a permission can linger in the list while the volume is
     * gone, and vice versa.
     */
    private fun hasValidAccess(treeUri: String): Boolean = runCatching {
        val uri = Uri.parse(treeUri)
        val held = context.contentResolver.persistedUriPermissions.any {
            it.uri == uri && it.isReadPermission && it.isWritePermission
        }
        if (!held) return false
        DocumentFile.fromTreeUri(context, uri)?.canRead() == true
    }.getOrDefault(false)

    private fun resolveFolderLabel(treeUri: String): String =
        runCatching {
            DocumentFile.fromTreeUri(context, Uri.parse(treeUri))?.name
        }.getOrNull()?.takeIf { it.isNotBlank() }
            ?: Uri.parse(treeUri).lastPathSegment?.substringAfterLast('/')
            ?: treeUri
}
