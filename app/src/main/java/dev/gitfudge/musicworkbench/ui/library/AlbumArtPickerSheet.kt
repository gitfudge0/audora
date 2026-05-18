package dev.gitfudge.musicworkbench.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.gitfudge.musicworkbench.R
import dev.gitfudge.musicworkbench.data.art.CoverArtCandidate
import dev.gitfudge.musicworkbench.ui.components.AppBottomSheet
import dev.gitfudge.musicworkbench.ui.components.SecondaryButton
import dev.gitfudge.musicworkbench.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumArtPickerSheet(
    state: AlbumPickerState,
    onPick: (CoverArtCandidate) -> Unit,
    onSkip: () -> Unit,
    downloadingCandidate: Boolean = false,
) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme

    AppBottomSheet(
        onDismissRequest = onSkip,
        title = stringResource(R.string.art_picker_title, state.albumName),
        contentPadding = PaddingValues(horizontal = spacing.lg, vertical = spacing.md),
        footer = {
            Row(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                horizontalArrangement = Arrangement.End,
            ) {
                SecondaryButton(onClick = onSkip) {
                    Text(stringResource(R.string.art_picker_skip))
                }
            }
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
            Text(
                text = stringResource(
                    R.string.art_picker_subtitle,
                    state.albumIndex,
                    state.totalAlbums,
                    state.trackCount,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
            Text(
                text = state.artistName.ifBlank { "—" },
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                contentPadding = PaddingValues(vertical = spacing.sm),
            ) {
                items(state.candidates, key = { it.mbid }) { candidate ->
                    CandidateThumbnail(
                        candidate = candidate,
                        enabled = !downloadingCandidate,
                        onClick = { onPick(candidate) },
                    )
                }
            }
            if (downloadingCandidate) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = colors.primary)
                    Text(
                        "Downloading…",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun CandidateThumbnail(
    candidate: CoverArtCandidate,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .size(96.dp)
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        Box {
            AsyncImage(
                model = candidate.thumbnailUrl,
                contentDescription = "${candidate.title} by ${candidate.artist}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
