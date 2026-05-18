package dev.gitfudge.musicworkbench.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.gitfudge.musicworkbench.ui.components.AppPanel
import dev.gitfudge.musicworkbench.ui.components.AppTopBar
import dev.gitfudge.musicworkbench.ui.components.Hairline
import dev.gitfudge.musicworkbench.ui.theme.LocalSpacing

private data class Attribution(val name: String, val detail: String)

private val DataSources = listOf(
    Attribution("LRCLIB", "Synced & plain lyrics. Public, community-run lyrics database (CC0). lrclib.net"),
    Attribution("MusicBrainz", "Release metadata for cover-art matching. Data under CC0 / CC-BY-NC-SA. musicbrainz.org"),
    Attribution("Cover Art Archive", "Album cover images, a joint project of MusicBrainz and the Internet Archive. coverartarchive.org"),
)

private val OpenSource = listOf(
    Attribution("jaudiotagger", "Audio tag reading/writing — LGPL-2.1"),
    Attribution("Jetpack Compose & Material 3", "UI toolkit — Apache-2.0"),
    Attribution("Hilt / Dagger", "Dependency injection — Apache-2.0"),
    Attribution("OkHttp & Retrofit", "HTTP & API client — Apache-2.0"),
    Attribution("kotlinx.serialization", "JSON parsing — Apache-2.0"),
    Attribution("Coil", "Image loading — Apache-2.0"),
    Attribution("AndroidX Room", "Local metadata cache — Apache-2.0"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(onBack: () -> Unit) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colors.background,
        topBar = {
            AppTopBar(
                title = "Licenses & sources",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.lg, vertical = spacing.md),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            SectionLabel("Data sources")
            Text(
                "Metadata and media are fetched from these services. Their data " +
                    "is used under each project's terms; Music Workbench is not " +
                    "affiliated with them.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
            AttributionPanel(DataSources)

            SectionLabel("Open source")
            AttributionPanel(OpenSource)

            Text(
                "Lyrics and cover art are downloaded into your own files. " +
                    "Rights to that content remain with their respective owners.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(top = spacing.sm),
            )
        }
    }
}

@Composable
private fun AttributionPanel(items: List<Attribution>) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme
    AppPanel(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.sm)) {
            items.forEachIndexed { i, a ->
                Column(modifier = Modifier.padding(vertical = spacing.sm)) {
                    Text(
                        a.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurface,
                    )
                    Text(
                        a.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                if (i < items.lastIndex) Hairline()
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
