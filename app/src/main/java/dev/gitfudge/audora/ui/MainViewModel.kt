package dev.gitfudge.audora.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.documentfile.provider.DocumentFile
import androidx.compose.runtime.Immutable
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.gitfudge.audora.BuildConfig
import dev.gitfudge.audora.data.releases.AppRelease
import dev.gitfudge.audora.data.releases.ReleasesRepository
import dev.gitfudge.audora.data.settings.SettingsRepository
import dev.gitfudge.audora.ui.theme.ThemeMode
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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

@Immutable
data class ReleasesUiState(
    val releases: ImmutableList<AppRelease> = persistentListOf(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val whatsNewRelease: AppRelease? = null,
    val updateRelease: AppRelease? = null,
    val updateStatus: UpdateStatus = UpdateStatus.Idle,
    val manualCheckResult: ManualUpdateCheckResult? = null,
)

@Immutable
sealed interface UpdateStatus {
    data object Idle : UpdateStatus
    data object Downloading : UpdateStatus
    data class ReadyToInstall(val release: AppRelease) : UpdateStatus
    data class Error(val message: String) : UpdateStatus
}

@Immutable
sealed interface ManualUpdateCheckResult {
    data object UpToDate : ManualUpdateCheckResult
    data class Failed(val message: String) : ManualUpdateCheckResult
}

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsRepository,
    private val releasesRepository: ReleasesRepository,
) : ViewModel() {
    private val _releasesUiState = MutableStateFlow(ReleasesUiState(isLoading = true))
    val releasesUiState: StateFlow<ReleasesUiState> = _releasesUiState

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

    val autoSyncLyrics: StateFlow<Boolean> = settings.settings
        .map { it.autoSyncLyrics }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    val includeEarlierFailedLyrics: StateFlow<Boolean> = settings.settings
        .map { it.includeEarlierFailedLyrics }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )

    init {
        refreshReleases()
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settings.setThemeMode(mode) }
    }

    fun setAutoSyncLyrics(enabled: Boolean) {
        viewModelScope.launch { settings.setAutoSyncLyrics(enabled) }
    }

    fun setIncludeEarlierFailedLyrics(enabled: Boolean) {
        viewModelScope.launch { settings.setIncludeEarlierFailedLyrics(enabled) }
    }

    fun setWalkthroughSeen(seen: Boolean) {
        viewModelScope.launch { settings.setHasSeenWalkthrough(seen) }
    }

    fun refreshReleases() {
        refreshReleases(showManualResult = false)
    }

    fun checkForUpdate() {
        refreshReleases(showManualResult = true)
    }

    private fun refreshReleases(showManualResult: Boolean) {
        viewModelScope.launch {
            _releasesUiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    manualCheckResult = null,
                )
            }
            releasesRepository.releases()
                .onSuccess { releases ->
                    val currentRelease = releases.firstOrNull {
                        it.version == BuildConfig.VERSION_NAME
                    }
                    val currentSettings = settings.settings.first()
                    val isFirstRun = !currentSettings.hasSeenWalkthrough &&
                        currentSettings.lastSeenReleaseVersion == null
                    if (isFirstRun) {
                        settings.setLastSeenReleaseVersion(BuildConfig.VERSION_NAME)
                    }
                    val updateRelease = releases.firstOrNull { release ->
                        release.apk != null &&
                            isVersionNewer(release.version, BuildConfig.VERSION_NAME)
                    }
                    _releasesUiState.value = ReleasesUiState(
                        releases = releases.toImmutableList(),
                        isLoading = false,
                        updateRelease = updateRelease,
                        whatsNewRelease = currentRelease
                            ?.takeIf {
                                !isFirstRun &&
                                    currentSettings.lastSeenReleaseVersion != BuildConfig.VERSION_NAME
                            },
                        manualCheckResult = if (showManualResult && updateRelease == null) {
                            ManualUpdateCheckResult.UpToDate
                        } else {
                            null
                        },
                    )
                }
                .onFailure { error ->
                    val message = error.message ?: "Could not load GitHub releases."
                    _releasesUiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = message,
                            manualCheckResult = if (showManualResult) {
                                ManualUpdateCheckResult.Failed(message)
                            } else {
                                null
                            },
                        )
                    }
                }
        }
    }

    fun downloadAndInstallUpdate() {
        val release = _releasesUiState.value.updateRelease ?: return
        if (_releasesUiState.value.updateStatus == UpdateStatus.Downloading) return

        viewModelScope.launch {
            _releasesUiState.update { it.copy(updateStatus = UpdateStatus.Downloading) }
            releasesRepository.downloadApk(release)
                .onSuccess { apkFile ->
                    if (
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                        !context.packageManager.canRequestPackageInstalls()
                    ) {
                        _releasesUiState.update {
                            it.copy(updateStatus = UpdateStatus.ReadyToInstall(release))
                        }
                        context.startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                Uri.parse("package:${context.packageName}"),
                            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                        return@launch
                    }

                    val apkUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        apkFile,
                    )
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(apkUri, "application/vnd.android.package-archive")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        },
                    )
                    _releasesUiState.update {
                        it.copy(updateStatus = UpdateStatus.ReadyToInstall(release))
                    }
                }
                .onFailure { error ->
                    _releasesUiState.update {
                        it.copy(
                            updateStatus = UpdateStatus.Error(
                                error.message ?: "Could not download the update.",
                            ),
                        )
                    }
                }
        }
    }

    fun dismissWhatsNew() {
        viewModelScope.launch {
            settings.setLastSeenReleaseVersion(BuildConfig.VERSION_NAME)
            _releasesUiState.update { it.copy(whatsNewRelease = null) }
        }
    }

    fun dismissManualCheckResult() {
        _releasesUiState.update { it.copy(manualCheckResult = null) }
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

private fun isVersionNewer(candidate: String, current: String): Boolean {
    val candidateParts = candidate.versionParts()
    val currentParts = current.versionParts()
    val max = maxOf(candidateParts.size, currentParts.size)
    for (index in 0 until max) {
        val candidatePart = candidateParts.getOrElse(index) { 0 }
        val currentPart = currentParts.getOrElse(index) { 0 }
        if (candidatePart != currentPart) return candidatePart > currentPart
    }
    return false
}

private fun String.versionParts(): List<Int> =
    trim()
        .removePrefix("v")
        .substringBefore('-')
        .split('.')
        .map { part -> part.takeWhile { it.isDigit() }.toIntOrNull() ?: 0 }
