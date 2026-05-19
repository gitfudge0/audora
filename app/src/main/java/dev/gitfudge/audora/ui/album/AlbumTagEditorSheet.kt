package dev.gitfudge.audora.ui.album

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import dev.gitfudge.audora.ui.components.AppBottomSheet
import dev.gitfudge.audora.ui.components.AppTextField
import dev.gitfudge.audora.ui.components.GhostButton
import dev.gitfudge.audora.ui.components.PrimaryButton
import dev.gitfudge.audora.ui.theme.LocalSpacing

/**
 * Initial values for the album tag editor. A null value means the field is
 * "mixed" across tracks — we show "Mixed" as placeholder and only overwrite
 * when the user types something.
 */
data class AlbumTagEditorInitial(
    val album: String?,
    val albumArtist: String?,
    val year: String?,
    val genre: String?,
    val trackCount: Int,
)

/** What the user typed. Empty string = "leave as-is" when the original was mixed. */
data class AlbumTagEdits(
    val album: String?,
    val albumArtist: String?,
    val year: String?,
    val genre: String?,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumTagEditorSheet(
    initial: AlbumTagEditorInitial,
    onApply: (AlbumTagEdits) -> Unit,
    onDismiss: () -> Unit,
) {
    val spacing = LocalSpacing.current

    // Pre-fill with the shared value, or empty when mixed.
    var album by remember { mutableStateOf(initial.album.orEmpty()) }
    var albumArtist by remember { mutableStateOf(initial.albumArtist.orEmpty()) }
    var year by remember { mutableStateOf(initial.year.orEmpty()) }
    var genre by remember { mutableStateOf(initial.genre.orEmpty()) }

    AppBottomSheet(
        onDismissRequest = onDismiss,
        title = "Edit album tags",
        contentPadding = PaddingValues(horizontal = spacing.lg, vertical = spacing.md),
        footer = {
            Row(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                horizontalArrangement = Arrangement.End,
            ) {
                GhostButton(onClick = onDismiss) { Text("Cancel") }
                PrimaryButton(
                    onClick = {
                        onApply(
                            AlbumTagEdits(
                                album = album.takeIf { it.isNotBlank() || initial.album != null },
                                albumArtist = albumArtist.takeIf { it.isNotBlank() || initial.albumArtist != null },
                                year = year.takeIf { it.isNotBlank() || initial.year != null },
                                genre = genre.takeIf { it.isNotBlank() || initial.genre != null },
                            ),
                        )
                    },
                    modifier = Modifier.padding(start = spacing.sm),
                ) { Text("Apply") }
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            Text(
                text = "Applies to ${initial.trackCount} ${if (initial.trackCount == 1) "track" else "tracks"}. Leave blank to keep mixed values.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            EditorField(
                label = "Album",
                value = album,
                isMixed = initial.album == null,
                onChange = { album = it },
                capitalize = true,
            )
            EditorField(
                label = "Album artist",
                value = albumArtist,
                isMixed = initial.albumArtist == null,
                onChange = { albumArtist = it },
                capitalize = true,
            )
            EditorField(
                label = "Year",
                value = year,
                isMixed = initial.year == null,
                onChange = { year = it.filter(Char::isDigit).take(4) },
                capitalize = false,
            )
            EditorField(
                label = "Genre",
                value = genre,
                isMixed = initial.genre == null,
                onChange = { genre = it },
                capitalize = true,
            )
        }
    }
}

@Composable
private fun EditorField(
    label: String,
    value: String,
    isMixed: Boolean,
    onChange: (String) -> Unit,
    capitalize: Boolean,
) {
    AppTextField(
        value = value,
        onValueChange = onChange,
        label = label,
        placeholder = if (isMixed) "Mixed" else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = if (capitalize) KeyboardCapitalization.Words else KeyboardCapitalization.None,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}
