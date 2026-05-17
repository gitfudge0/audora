package dev.gitfudge.musicworkbench.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import dev.gitfudge.musicworkbench.ui.theme.LocalSpacing

/**
 * Full-width preview dialog used by both the detail screen (manual single-track fetch)
 * and the library screen (per-album batch fetch). Shows the downloaded artwork as a
 * large square (fillMaxWidth, aspectRatio 1:1) with Back / Apply actions.
 */
@Composable
fun AlbumArtPreviewDialog(
    bytes: ByteArray,
    title: String,
    artist: String,
    saving: Boolean,
    onApply: () -> Unit,
    onBack: () -> Unit,
    subtitle: String? = null,
) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme

    Dialog(
        onDismissRequest = onBack,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            color = colors.surface,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.md),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(spacing.md),
                modifier = Modifier.padding(spacing.md),
            ) {
                Text(
                    "Preview album art",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                )
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = colors.surfaceContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                ) {
                    AsyncImage(
                        model = bytes,
                        contentDescription = "Album art preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                Text(
                    "$title — $artist",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.md, Alignment.End),
                ) {
                    TextButton(onClick = onBack, enabled = !saving) {
                        Text("Back")
                    }
                    Button(onClick = onApply, enabled = !saving) {
                        if (saving) {
                            CircularProgressIndicator(
                                Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = colors.onPrimary,
                            )
                        } else {
                            Text("Apply")
                        }
                    }
                }
            }
        }
    }
}
