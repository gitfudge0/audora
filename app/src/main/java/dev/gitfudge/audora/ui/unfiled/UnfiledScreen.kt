package dev.gitfudge.audora.ui.unfiled

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gitfudge.audora.ui.common.BulkTagEditorSheet
import dev.gitfudge.audora.ui.components.AppTopBar
import dev.gitfudge.audora.ui.components.EmptyState
import dev.gitfudge.audora.ui.components.PrimaryButton
import dev.gitfudge.audora.ui.library.TrackListItem
import dev.gitfudge.audora.ui.theme.LocalSpacing

/**
 * Flat list of tracks with no album tag. Reached from the pinned Unfiled
 * row on the Albums tab. Supports multi-select + bulk tag edit so a batch of
 * orphans can be filed under an album at once.
 */
@Composable
fun UnfiledScreen(
    onBack: () -> Unit,
    onTrackClick: (String) -> Unit,
    viewModel: UnfiledViewModel = hiltViewModel(),
) {
    val tracks by viewModel.tracks.collectAsStateWithLifecycle()
    val lowResPx by viewModel.lowResThresholdPx.collectAsStateWithLifecycle()
    val selectedUris by viewModel.selectedUris.collectAsStateWithLifecycle()
    val isSelecting by viewModel.isSelecting.collectAsStateWithLifecycle()
    val bulkEditorOpen by viewModel.bulkEditorOpen.collectAsStateWithLifecycle()
    val bulkEditorTracks by viewModel.bulkEditorTracks.collectAsStateWithLifecycle()
    val bulkEditState by viewModel.bulkEditState.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(bulkEditState) {
        val s = bulkEditState
        if (s is UnfiledViewModel.BulkEditState.Done) {
            snackbarHostState.showSnackbar(
                "Updated ${s.ok} track${if (s.ok == 1) "" else "s"}" +
                    if (s.failed > 0) " · Failed ${s.failed}" else "",
            )
            viewModel.dismissBulkEditResult()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "Unfiled · ${tracks.size}",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        bottomBar = {
            if (isSelecting) {
                UnfiledSelectionBar(
                    selectedCount = selectedUris.size,
                    onEditTags = { viewModel.openBulkEditor() },
                    onClearSelection = { viewModel.clearSelection() },
                )
            }
        },
    ) { inner ->
        if (tracks.isEmpty()) {
            EmptyState(
                icon = Icons.Rounded.Inbox,
                title = "Nothing unfiled",
                body = "Every track has an album tag.",
                modifier = Modifier.fillMaxSize().padding(inner),
            )
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
                    onClick = {
                        if (isSelecting) viewModel.toggleSelection(track.documentUri)
                        else onTrackClick(track.documentUri)
                    },
                    onLongClick = {
                        if (!isSelecting) viewModel.enterSelectionWith(track.documentUri)
                    },
                    selected = track.documentUri in selectedUris,
                    selectionMode = isSelecting,
                )
            }
        }
    }

    if (bulkEditorOpen) {
        BulkTagEditorSheet(
            tracks = bulkEditorTracks,
            inFlight = bulkEditState is UnfiledViewModel.BulkEditState.Running,
            onApply = { viewModel.applyBulkEdits(it) },
            onDismiss = { viewModel.dismissBulkEditor() },
        )
    }
}

@Composable
private fun UnfiledSelectionBar(
    selectedCount: Int,
    onEditTags: () -> Unit,
    onClearSelection: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val spacing = LocalSpacing.current
    Surface(color = colors.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(color = colors.outline, thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = spacing.lg, vertical = spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                Text(
                    text = "$selectedCount selected",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onClearSelection) {
                    Icon(
                        Icons.Rounded.Clear,
                        contentDescription = "Clear selection",
                        tint = colors.secondary,
                    )
                }
                PrimaryButton(onClick = onEditTags) { Text("Edit tags") }
            }
        }
    }
}
