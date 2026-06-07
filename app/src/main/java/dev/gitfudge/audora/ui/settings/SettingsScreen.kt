package dev.gitfudge.audora.ui.settings

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gitfudge.audora.BuildConfig
import dev.gitfudge.audora.R
import dev.gitfudge.audora.data.releases.AppRelease
import dev.gitfudge.audora.ui.MainViewModel
import dev.gitfudge.audora.ui.ManualUpdateCheckResult
import dev.gitfudge.audora.ui.UpdateStatus
import dev.gitfudge.audora.ui.components.AppPanel
import dev.gitfudge.audora.ui.components.AppTopBar
import dev.gitfudge.audora.ui.components.Hairline
import dev.gitfudge.audora.ui.components.ListRow
import dev.gitfudge.audora.ui.components.PillTab
import dev.gitfudge.audora.ui.components.PillTabs
import dev.gitfudge.audora.ui.theme.AppTextStyles
import dev.gitfudge.audora.ui.theme.AudoraTheme
import dev.gitfudge.audora.ui.theme.LocalShapeScale
import dev.gitfudge.audora.ui.theme.LocalSpacing
import dev.gitfudge.audora.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: MainViewModel,
    folderLabel: String,
    onChangeFolder: () -> Unit,
    onOpenLicenses: () -> Unit = {},
    onOpenChangelog: () -> Unit = {},
    onReplayWalkthrough: () -> Unit = {},
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val autoSyncLyrics by viewModel.autoSyncLyrics.collectAsStateWithLifecycle()
    val includeEarlierFailedLyrics by viewModel.includeEarlierFailedLyrics.collectAsStateWithLifecycle()
    val releasesState by viewModel.releasesUiState.collectAsStateWithLifecycle()
    SettingsContent(
        themeMode = themeMode,
        autoSyncLyrics = autoSyncLyrics,
        includeEarlierFailedLyrics = includeEarlierFailedLyrics,
        updateRelease = releasesState.updateRelease,
        isCheckingForUpdate = releasesState.isLoading,
        updateStatus = releasesState.updateStatus,
        manualCheckResult = releasesState.manualCheckResult,
        onThemeModeChange = viewModel::setThemeMode,
        onAutoSyncLyricsChange = viewModel::setAutoSyncLyrics,
        onIncludeEarlierFailedLyricsChange = viewModel::setIncludeEarlierFailedLyrics,
        onCheckForUpdate = viewModel::checkForUpdate,
        onDismissManualCheckResult = viewModel::dismissManualCheckResult,
        onInstallUpdate = viewModel::downloadAndInstallUpdate,
        onBack = onBack,
        folderLabel = folderLabel,
        onChangeFolder = onChangeFolder,
        onOpenLicenses = onOpenLicenses,
        onOpenChangelog = onOpenChangelog,
        onReplayWalkthrough = onReplayWalkthrough,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    themeMode: ThemeMode,
    autoSyncLyrics: Boolean,
    includeEarlierFailedLyrics: Boolean,
    updateRelease: AppRelease?,
    isCheckingForUpdate: Boolean,
    updateStatus: UpdateStatus,
    manualCheckResult: ManualUpdateCheckResult?,
    onThemeModeChange: (ThemeMode) -> Unit,
    onAutoSyncLyricsChange: (Boolean) -> Unit,
    onIncludeEarlierFailedLyricsChange: (Boolean) -> Unit,
    onCheckForUpdate: () -> Unit,
    onDismissManualCheckResult: () -> Unit,
    onInstallUpdate: () -> Unit,
    onBack: () -> Unit,
    folderLabel: String,
    onChangeFolder: () -> Unit,
    onOpenLicenses: () -> Unit = {},
    onOpenChangelog: () -> Unit = {},
    onReplayWalkthrough: () -> Unit = {},
) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current

    fun open(url: String) {
        context.startActivity(
            android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse(url),
            ),
        )
    }

    manualCheckResult?.let { result ->
        AlertDialog(
            onDismissRequest = onDismissManualCheckResult,
            title = {
                Text(
                    when (result) {
                        ManualUpdateCheckResult.UpToDate -> "You're up to date"
                        is ManualUpdateCheckResult.Failed -> "Update check failed"
                    },
                )
            },
            text = {
                Text(
                    when (result) {
                        ManualUpdateCheckResult.UpToDate ->
                            "Audora ${BuildConfig.VERSION_NAME} is the latest version available."
                        is ManualUpdateCheckResult.Failed -> result.message
                    },
                )
            },
            confirmButton = {
                TextButton(onClick = onDismissManualCheckResult) {
                    Text("OK")
                }
            },
        )
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            AppTopBar(
                title = "Settings",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.lg, vertical = spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.lg),
        ) {
            // ── Library ───────────────────────────────────────────────────
            SectionLabel("Library")
            AppPanel(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingsNavRow(
                        label = stringResource(R.string.library_change_folder),
                        supporting = folderLabel,
                        onClick = onChangeFolder,
                    )
                }
            }

            // ── Appearance ────────────────────────────────────────────────
            SectionLabel("Appearance")
            AppPanel(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.onSurface,
                    )
                    Text(
                        text = "Follow your device, or pin a mode.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        PillTabs(
                            tabs = listOf(
                                PillTab("System"),
                                PillTab("Light"),
                                PillTab("Dark"),
                            ),
                            selectedIndex = when (themeMode) {
                                ThemeMode.System -> 0
                                ThemeMode.Light -> 1
                                ThemeMode.Dark -> 2
                            },
                            onSelected = { idx ->
                                onThemeModeChange(
                                    when (idx) {
                                        1 -> ThemeMode.Light
                                        2 -> ThemeMode.Dark
                                        else -> ThemeMode.System
                                    },
                                )
                            },
                        )
                    }
                }
            }

            // ── Lyrics ───────────────────────────────────────────────────
            SectionLabel("Lyrics sync")
            AppPanel(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingsSwitchRow(
                        label = "Enable autosync of lyrics",
                        supporting = "After a scan, fetch lyrics for newly found tracks that do not already have LRC files.",
                        checked = autoSyncLyrics,
                        onCheckedChange = onAutoSyncLyricsChange,
                    )
                    Hairline()
                    SettingsSwitchRow(
                        label = "Include earlier failed ones",
                        supporting = "Retry tracks that were checked before but had no match or failed to save.",
                        checked = includeEarlierFailedLyrics,
                        enabled = autoSyncLyrics,
                        onCheckedChange = onIncludeEarlierFailedLyricsChange,
                    )
                }
            }

            // ── Help & feedback ───────────────────────────────────────────
            SectionLabel("Help & feedback")
            AppPanel(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingsNavRow(
                        label = stringResource(R.string.walkthrough_replay),
                        onClick = onReplayWalkthrough,
                    )
                    Hairline()
                    SettingsNavRow(
                        label = "Report a bug or request a feature",
                        external = true,
                        rowContentDescription = "Opens GitHub Issues in browser",
                        onClick = { open("https://github.com/gitfudge0/audora/issues/new/choose") },
                    )
                    Text(
                        text = "An open feedback page for non-devs is in the works.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(spacing.lg),
                    )
                }
            }

            // ── About ─────────────────────────────────────────────────────
            SectionLabel("About")
            AppPanel(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SettingsInfoRow("Version", BuildConfig.VERSION_NAME)
                    Hairline()
                    SettingsInfoRow("Build", BuildConfig.VERSION_CODE.toString())
                    Hairline()
                    SettingsNavRow(
                        label = if (isCheckingForUpdate) "Checking for update" else "Check for update",
                        supporting = updateRelease?.let { "Audora ${it.version} is available" }
                            ?: "GitHub releases",
                        enabled = !isCheckingForUpdate,
                        onClick = onCheckForUpdate,
                        trailing = {
                            if (isCheckingForUpdate) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        },
                    )
                    updateRelease?.let { release ->
                        Hairline()
                        SettingsNavRow(
                            label = if (updateStatus == UpdateStatus.Downloading) {
                                "Downloading update"
                            } else {
                                "Update available"
                            },
                            supporting = "Audora ${release.version}",
                            enabled = updateStatus != UpdateStatus.Downloading,
                            onClick = onInstallUpdate,
                            trailing = {
                                if (updateStatus == UpdateStatus.Downloading) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp))
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.Download,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            },
                        )
                    }
                    if (updateStatus is UpdateStatus.Error) {
                        Text(
                            text = updateStatus.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.error,
                            modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.sm),
                        )
                    } else if (updateStatus is UpdateStatus.ReadyToInstall) {
                        Text(
                            text = "Installer opened. If Android asked for permission, enable it and tap Update again.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.sm),
                        )
                    }
                    Hairline()
                    SettingsNavRow(
                        label = "Changelog",
                        supporting = "GitHub releases",
                        onClick = onOpenChangelog,
                    )
                    Hairline()
                    SettingsNavRow(
                        label = "Licenses & sources",
                        supporting = "LRCLIB · MusicBrainz",
                        onClick = onOpenLicenses,
                    )
                    Hairline()
                    SettingsNavRow(
                        label = "Developer",
                        supporting = "gitfudge.dev",
                        external = true,
                        rowContentDescription = "Opens gitfudge.dev in browser",
                        onClick = { open("https://gitfudge.dev") },
                    )
                    Column(modifier = Modifier.padding(spacing.lg)) {
                        // Buy Me a Coffee — their recommended branded button:
                        // #FFDD00 background, dark label, coffee glyph.
                        Surface(
                            onClick = { open("https://buymeacoffee.com/gitfudge") },
                            shape = LocalShapeScale.current.sm,
                            color = Color(0xFFFFDD00),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    contentDescription = "Buy me a coffee, opens in browser"
                                },
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = spacing.lg,
                                    vertical = spacing.md,
                                ),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocalCafe,
                                    contentDescription = null,
                                    tint = Color(0xFF000000),
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(Modifier.size(spacing.sm))
                                Text(
                                    text = "Buy me a coffee",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color(0xFF000000),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    label: String,
    supporting: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    ListRow(
        onClick = if (enabled) ({ onCheckedChange(!checked) }) else null,
        headline = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        supporting = {
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailing = {
            Switch(
                checked = checked,
                enabled = enabled,
                onCheckedChange = onCheckedChange,
            )
        },
    )
}

/** Tappable settings row: chevron for in-app navigation, open-in-new for links that leave the app. */
@Composable
private fun SettingsNavRow(
    label: String,
    onClick: () -> Unit,
    supporting: String? = null,
    external: Boolean = false,
    rowContentDescription: String? = null,
    enabled: Boolean = true,
    trailing: @Composable (() -> Unit)? = null,
) {
    ListRow(
        modifier = if (rowContentDescription != null) {
            Modifier.semantics { contentDescription = rowContentDescription }
        } else {
            Modifier
        },
        onClick = if (enabled) onClick else null,
        headline = { Text(label, style = MaterialTheme.typography.bodyMedium) },
        supporting = supporting?.let {
            { Text(it, style = AppTextStyles.mono) }
        },
        trailing = trailing ?: {
            Icon(
                imageVector = if (external) {
                    Icons.AutoMirrored.Rounded.OpenInNew
                } else {
                    Icons.Rounded.ChevronRight
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        },
    )
}

/** Static settings row: label on the left, monospaced value on the right. */
@Composable
private fun SettingsInfoRow(label: String, value: String) {
    ListRow(
        headline = { Text(label, style = MaterialTheme.typography.bodyMedium) },
        trailing = {
            Text(
                text = value,
                style = AppTextStyles.mono,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "Settings · Light")
@Composable
private fun SettingsLightPreview() {
    AudoraTheme(themeMode = ThemeMode.Light) {
        SettingsContent(
            themeMode = ThemeMode.System,
            autoSyncLyrics = true,
            includeEarlierFailedLyrics = false,
            updateRelease = null,
            isCheckingForUpdate = false,
            updateStatus = UpdateStatus.Idle,
            manualCheckResult = null,
            onThemeModeChange = {},
            onAutoSyncLyricsChange = {},
            onIncludeEarlierFailedLyricsChange = {},
            onCheckForUpdate = {},
            onDismissManualCheckResult = {},
            onInstallUpdate = {},
            onBack = {},
            folderLabel = "Music",
            onChangeFolder = {},
        )
    }
}

@Preview(name = "Settings · Dark")
@Composable
private fun SettingsDarkPreview() {
    AudoraTheme(themeMode = ThemeMode.Dark) {
        SettingsContent(
            themeMode = ThemeMode.Dark,
            autoSyncLyrics = true,
            includeEarlierFailedLyrics = true,
            updateRelease = null,
            isCheckingForUpdate = false,
            updateStatus = UpdateStatus.Idle,
            manualCheckResult = null,
            onThemeModeChange = {},
            onAutoSyncLyricsChange = {},
            onIncludeEarlierFailedLyricsChange = {},
            onCheckForUpdate = {},
            onDismissManualCheckResult = {},
            onInstallUpdate = {},
            onBack = {},
            folderLabel = "Music",
            onChangeFolder = {},
        )
    }
}
