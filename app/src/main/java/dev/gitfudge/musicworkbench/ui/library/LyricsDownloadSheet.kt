package dev.gitfudge.musicworkbench.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.gitfudge.musicworkbench.R
import dev.gitfudge.musicworkbench.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsDownloadSheet(
    entries: List<DownloadEntry>,
    batchFetchState: BatchFetchState,
    onDismiss: () -> Unit,
    onClear: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        confirmValueChange = { it != SheetValue.Hidden },
    )
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme

    val isRunning = batchFetchState is BatchFetchState.Running
    val done = (batchFetchState as? BatchFetchState.Running)?.done ?: entries.count { it.status != DownloadStatus.PENDING && it.status != DownloadStatus.DOWNLOADING }
    val total = entries.size
    val progress = if (total > 0) done.toFloat() / total.toFloat() else 0f

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = spacing.lg),
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.lg, vertical = spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.downloads_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f),
                )
                if (!isRunning && entries.isNotEmpty()) {
                    TextButton(onClick = onClear) {
                        Text(stringResource(R.string.downloads_clear))
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.downloads_dismiss))
                }
            }

            // Overall progress bar (visible while running)
            if (isRunning || entries.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.lg),
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = colors.primary,
                        trackColor = colors.surfaceContainer,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (isRunning) {
                            stringResource(R.string.downloads_progress, done, total)
                        } else {
                            val saved = entries.count { it.status == DownloadStatus.SAVED }
                            val noMatch = entries.count { it.status == DownloadStatus.NO_MATCH }
                            val failed = entries.count { it.status == DownloadStatus.FAILED }
                            stringResource(R.string.downloads_summary, saved, noMatch, failed)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(spacing.md))
                HorizontalDivider(color = colors.outlineVariant)
            }

            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = spacing.xl),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.downloads_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn {
                    items(entries, key = { it.documentUri }) { entry ->
                        DownloadEntryRow(entry)
                        HorizontalDivider(
                            color = colors.outlineVariant,
                            modifier = Modifier.padding(start = spacing.lg + 32.dp + spacing.md),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadEntryRow(entry: DownloadEntry) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.lg, vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        // Status icon
        Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
            when (entry.status) {
                DownloadStatus.PENDING -> Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
                DownloadStatus.DOWNLOADING -> CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = colors.primary,
                )
                DownloadStatus.SAVED -> Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp),
                )
                DownloadStatus.NO_MATCH -> Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
                DownloadStatus.FAILED -> Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = colors.error,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        // Title + status label
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.title,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val statusLabel = when (entry.status) {
                DownloadStatus.PENDING -> stringResource(R.string.download_status_pending)
                DownloadStatus.DOWNLOADING -> stringResource(R.string.download_status_downloading)
                DownloadStatus.SAVED -> stringResource(R.string.download_status_saved)
                DownloadStatus.NO_MATCH -> stringResource(R.string.download_status_no_match)
                DownloadStatus.FAILED -> stringResource(R.string.download_status_failed)
            }
            Text(
                text = statusLabel,
                style = MaterialTheme.typography.labelSmall,
                color = when (entry.status) {
                    DownloadStatus.FAILED -> colors.error
                    DownloadStatus.SAVED -> colors.primary
                    else -> colors.onSurfaceVariant
                },
            )
        }

        Spacer(Modifier.width(spacing.sm))
    }
}
