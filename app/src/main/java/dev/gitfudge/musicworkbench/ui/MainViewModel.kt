package dev.gitfudge.musicworkbench.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gitfudge.musicworkbench.data.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RootUiState {
    data object Loading : RootUiState
    data object Onboarding : RootUiState
    data class Library(val folderLabel: String) : RootUiState
}

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<RootUiState> = settings.settings
        .map { current ->
            val uri = current.musicTreeUri
            if (uri.isNullOrBlank()) {
                RootUiState.Onboarding
            } else {
                RootUiState.Library(folderLabel = resolveFolderLabel(uri))
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RootUiState.Loading,
        )

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

    private fun resolveFolderLabel(treeUri: String): String =
        runCatching {
            DocumentFile.fromTreeUri(context, Uri.parse(treeUri))?.name
        }.getOrNull()?.takeIf { it.isNotBlank() }
            ?: Uri.parse(treeUri).lastPathSegment?.substringAfterLast('/')
            ?: treeUri
}
