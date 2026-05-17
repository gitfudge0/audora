package dev.gitfudge.musicworkbench.ui.unfiled

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gitfudge.musicworkbench.ui.library.TrackListItem
import dev.gitfudge.musicworkbench.ui.theme.LocalSpacing

/**
 * Flat list of tracks with no album tag. Reached from the pinned Unfiled
 * row on the Albums tab.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnfiledScreen(
    onBack: () -> Unit,
    onTrackClick: (String) -> Unit,
    viewModel: UnfiledViewModel = hiltViewModel(),
) {
    val tracks by viewModel.tracks.collectAsStateWithLifecycle()
    val lowResPx by viewModel.lowResThresholdPx.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unfiled · ${tracks.size}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { inner ->
        if (tracks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(inner),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Nothing here — every track has an album.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner),
            contentPadding = PaddingValues(bottom = spacing.xl),
        ) {
            items(tracks, key = { it.documentUri }) { track ->
                TrackListItem(
                    track = track,
                    lowResThresholdPx = lowResPx,
                    onClick = { onTrackClick(track.documentUri) },
                )
            }
        }
    }
}
