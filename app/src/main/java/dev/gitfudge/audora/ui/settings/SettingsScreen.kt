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
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import dev.gitfudge.audora.ui.MainViewModel
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
    onOpenLicenses: () -> Unit = {},
    onReplayWalkthrough: () -> Unit = {},
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    SettingsContent(
        themeMode = themeMode,
        onThemeModeChange = viewModel::setThemeMode,
        onBack = onBack,
        onOpenLicenses = onOpenLicenses,
        onReplayWalkthrough = onReplayWalkthrough,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onBack: () -> Unit,
    onOpenLicenses: () -> Unit = {},
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

/** Tappable settings row: chevron for in-app navigation, open-in-new for links that leave the app. */
@Composable
private fun SettingsNavRow(
    label: String,
    onClick: () -> Unit,
    supporting: String? = null,
    external: Boolean = false,
    rowContentDescription: String? = null,
) {
    ListRow(
        modifier = if (rowContentDescription != null) {
            Modifier.semantics { contentDescription = rowContentDescription }
        } else {
            Modifier
        },
        onClick = onClick,
        headline = { Text(label, style = MaterialTheme.typography.bodyMedium) },
        supporting = supporting?.let {
            { Text(it, style = AppTextStyles.mono) }
        },
        trailing = {
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
        SettingsContent(themeMode = ThemeMode.System, onThemeModeChange = {}, onBack = {})
    }
}

@Preview(name = "Settings · Dark")
@Composable
private fun SettingsDarkPreview() {
    AudoraTheme(themeMode = ThemeMode.Dark) {
        SettingsContent(themeMode = ThemeMode.Dark, onThemeModeChange = {}, onBack = {})
    }
}
