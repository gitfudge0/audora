package dev.gitfudge.audora.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import dev.gitfudge.audora.R
import dev.gitfudge.audora.ui.components.AppBottomSheet
import dev.gitfudge.audora.ui.theme.LocalSpacing

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

    AppBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        title = stringResource(R.string.downloads_title),
        titleTrailing = {
            if (!isRunning && entries.isNotEmpty()) {
                TextButton(onClick = onClear) {
                    Text(stringResource(R.string.downloads_clear))
                }
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.downloads_dismiss))
            }
        },
        contentPadding = PaddingValues(0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = spacing.lg),
        ) {
            // Overall progress bar (visible while running)
            if (isRunning || entries.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.lg, vertical = spacing.sm),
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = colors.secondary,
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
        // Status icon in a 32dp rounded container (prototype: surfaceContainer
        // tile, semantic-colored glyph).
        val sc = dev.gitfudge.audora.ui.theme.LocalStatusColors.current
        androidx.compose.material3.Surface(
            shape = dev.gitfudge.audora.ui.theme.LocalShapeScale.current.sm,
            color = colors.surfaceContainer,
            modifier = Modifier.size(32.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                when (entry.status) {
                    DownloadStatus.PENDING -> Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                    DownloadStatus.DOWNLOADING -> CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = colors.secondary,
                    )
                    DownloadStatus.SAVED -> Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = sc.ok,
                        modifier = Modifier.size(18.dp),
                    )
                    DownloadStatus.NO_MATCH -> Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = sc.warn,
                        modifier = Modifier.size(18.dp),
                    )
                    DownloadStatus.FAILED -> Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = sc.missing,
                        modifier = Modifier.size(18.dp),
                    )
                }
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
                    DownloadStatus.FAILED -> sc.missing
                    DownloadStatus.SAVED -> sc.ok
                    DownloadStatus.NO_MATCH -> sc.warn
                    else -> colors.onSurfaceVariant
                },
            )
        }

        Spacer(Modifier.width(spacing.sm))
    }
}
