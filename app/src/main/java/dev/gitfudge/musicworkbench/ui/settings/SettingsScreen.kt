package dev.gitfudge.musicworkbench.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dev.gitfudge.musicworkbench.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gitfudge.musicworkbench.ui.MainViewModel
import dev.gitfudge.musicworkbench.ui.components.AppPanel
import dev.gitfudge.musicworkbench.ui.components.AppTopBar
import dev.gitfudge.musicworkbench.ui.components.Hairline
import dev.gitfudge.musicworkbench.ui.components.PillTab
import dev.gitfudge.musicworkbench.ui.components.PillTabs
import dev.gitfudge.musicworkbench.ui.theme.LocalSpacing
import dev.gitfudge.musicworkbench.ui.theme.MusicWorkbenchTheme
import dev.gitfudge.musicworkbench.ui.theme.ThemeMode

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
                .padding(horizontal = spacing.lg, vertical = spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
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

            SectionLabel("About")
            AppPanel(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.sm)) {
                    val rows = listOf(
                        "Version" to dev.gitfudge.musicworkbench.BuildConfig.VERSION_NAME,
                        "Build" to dev.gitfudge.musicworkbench.BuildConfig.VERSION_CODE.toString(),
                        "Source" to "LRCLIB · MusicBrainz",
                    )
                    rows.forEachIndexed { i, (label, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = spacing.sm),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurface,
                            )
                            Text(
                                text = value,
                                style = dev.gitfudge.musicworkbench.ui.theme.AppTextStyles.mono,
                                color = colors.onSurfaceVariant,
                            )
                        }
                        if (i < rows.lastIndex) Hairline()
                    }
                    Hairline()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenLicenses() }
                            .padding(vertical = spacing.sm),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Licenses & sources",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurface,
                        )
                        Icon(
                            Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Hairline()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onReplayWalkthrough() }
                            .padding(vertical = spacing.sm),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.walkthrough_replay),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurface,
                        )
                        Icon(
                            Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            SectionLabel("Developer")
            AppPanel(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.sm)) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    fun open(url: String) {
                        context.startActivity(
                            android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse(url),
                            ),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { open("https://gitfudge.dev") }
                            .padding(vertical = spacing.sm),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Developer",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurface,
                        )
                        Text(
                            text = "gitfudge.dev",
                            style = dev.gitfudge.musicworkbench.ui.theme.AppTextStyles.mono,
                            color = colors.secondary,
                        )
                    }
                    Hairline()
                    // Buy Me a Coffee — their recommended branded button:
                    // #FFDD00 background, dark label, coffee glyph.
                    androidx.compose.material3.Surface(
                        onClick = { open("https://buymeacoffee.com/gitfudge") },
                        shape = dev.gitfudge.musicworkbench.ui.theme.LocalShapeScale.current.sm,
                        color = androidx.compose.ui.graphics.Color(0xFFFFDD00),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = spacing.md)
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
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocalCafe,
                                contentDescription = null,
                                tint = androidx.compose.ui.graphics.Color(0xFF000000),
                                modifier = Modifier.size(20.dp),
                            )
                            androidx.compose.foundation.layout.Spacer(Modifier.size(spacing.sm))
                            Text(
                                text = "Buy me a coffee",
                                style = MaterialTheme.typography.labelLarge,
                                color = androidx.compose.ui.graphics.Color(0xFF000000),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Hairline()
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "Settings · Light")
@Composable
private fun SettingsLightPreview() {
    MusicWorkbenchTheme(themeMode = ThemeMode.Light) {
        SettingsContent(themeMode = ThemeMode.System, onThemeModeChange = {}, onBack = {})
    }
}

@Preview(name = "Settings · Dark")
@Composable
private fun SettingsDarkPreview() {
    MusicWorkbenchTheme(themeMode = ThemeMode.Dark) {
        SettingsContent(themeMode = ThemeMode.Dark, onThemeModeChange = {}, onBack = {})
    }
}
