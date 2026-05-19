package dev.gitfudge.audora.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import dev.gitfudge.audora.data.db.TrackEntity
import dev.gitfudge.audora.domain.BulkTagEdits
import dev.gitfudge.audora.domain.BulkTagField
import dev.gitfudge.audora.domain.BulkTagFieldKind
import dev.gitfudge.audora.domain.distinctValues
import dev.gitfudge.audora.ui.components.AppBottomSheet
import dev.gitfudge.audora.ui.components.AppFilterChip
import dev.gitfudge.audora.ui.components.AppTextField
import dev.gitfudge.audora.ui.components.GhostButton
import dev.gitfudge.audora.ui.components.PrimaryButton
import dev.gitfudge.audora.ui.theme.LocalSpacing

/**
 * Generic bulk tag editor. For each [fields] entry the user toggles the field
 * on, picks one of the distinct existing values (chips) or types a new one,
 * and that single value is applied to every selected track on Apply. Fields
 * left toggled off are untouched.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BulkTagEditorSheet(
    tracks: List<TrackEntity>,
    inFlight: Boolean,
    onApply: (BulkTagEdits) -> Unit,
    onDismiss: () -> Unit,
    fields: List<BulkTagField> = BulkTagField.ALL,
    title: String = "Edit tags",
    applyLabel: String = "Apply",
    /** Fields switched on with a starting value when the sheet opens. */
    prefill: Map<BulkTagField, String> = emptyMap(),
    /** Optional explanatory block rendered above the field list. */
    header: (@Composable () -> Unit)? = null,
) {
    val spacing = LocalSpacing.current
    val enabled = remember { mutableStateMapOf<BulkTagField, Boolean>() }
    val values = remember { mutableStateMapOf<BulkTagField, String>() }
    // Seed enabled/values once per distinct prefill so the combine flow opens
    // with the canonical guess already filled in and toggled on.
    remember(prefill) {
        prefill.forEach { (field, value) ->
            enabled[field] = true
            values[field] = value
        }
        prefill
    }
    val trackCount = tracks.size
    val anyEnabled = fields.any { enabled[it] == true }

    AppBottomSheet(
        onDismissRequest = onDismiss,
        title = title,
        contentPadding = PaddingValues(horizontal = spacing.lg, vertical = spacing.md),
        footer = {
            Row(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                horizontalArrangement = Arrangement.End,
            ) {
                GhostButton(onClick = onDismiss, enabled = !inFlight) { Text("Cancel") }
                PrimaryButton(
                    onClick = {
                        val edits = buildMap {
                            fields.forEach { field ->
                                if (enabled[field] != true) return@forEach
                                val raw = values[field].orEmpty()
                                when (field.kind) {
                                    BulkTagFieldKind.BOOLEAN ->
                                        put(field, if (raw == "false") "false" else "true")
                                    else ->
                                        if (raw.isNotBlank()) put(field, raw.trim())
                                }
                            }
                        }
                        if (edits.isNotEmpty()) onApply(edits)
                    },
                    enabled = !inFlight && anyEnabled,
                    modifier = Modifier.padding(start = spacing.sm),
                ) { Text(if (inFlight) "Applying…" else applyLabel) }
            }
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            if (header != null) {
                header()
            } else {
                Text(
                    text = "Bulk edit",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = "Changes apply to all $trackCount selected " +
                    "${if (trackCount == 1) "track" else "tracks"}. Turn on only the fields " +
                    "you want to overwrite — everything left off is untouched. For each field, " +
                    "tap one of the existing values or type a new one.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            fields.forEach { field ->
                val on = enabled[field] == true
                val distinct = remember(tracks, field) { distinctValues(tracks, field) }
                val summary = when {
                    field.kind == BulkTagFieldKind.BOOLEAN -> {
                        val yes = tracks.count { it.compilation }
                        "$yes of $trackCount marked yes"
                    }
                    distinct.isEmpty() -> "Currently empty on all tracks"
                    distinct.size == 1 -> "All tracks: ${distinct.first()}"
                    else -> "Mixed · ${distinct.size} different values"
                }
                Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = field.label,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = summary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = on,
                            onCheckedChange = { enabled[field] = it },
                            enabled = !inFlight,
                        )
                    }

                    if (!on) return@Column

                    when (field.kind) {
                        BulkTagFieldKind.BOOLEAN -> {
                            val v = values[field] ?: "true"
                            Text(
                                text = "Mark every selected track as:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                                AppFilterChip(
                                    selected = v != "false",
                                    onClick = { values[field] = "true" },
                                    label = "Yes",
                                )
                                AppFilterChip(
                                    selected = v == "false",
                                    onClick = { values[field] = "false" },
                                    label = "No",
                                )
                            }
                        }

                        else -> {
                            if (distinct.isNotEmpty()) {
                                Text(
                                    text = "Existing values (tap to reuse):",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                                    verticalArrangement = Arrangement.spacedBy(spacing.xs),
                                ) {
                                    distinct.forEach { candidate ->
                                        AppFilterChip(
                                            selected = values[field] == candidate,
                                            onClick = { values[field] = candidate },
                                            label = candidate,
                                        )
                                    }
                                }
                            }
                            AppTextField(
                                value = values[field].orEmpty(),
                                onValueChange = { input ->
                                    values[field] = if (field.kind == BulkTagFieldKind.NUMBER) {
                                        input.filter(Char::isDigit).take(4)
                                    } else {
                                        input
                                    }
                                },
                                label = "New ${field.label.lowercase()} for all tracks",
                                placeholder = "Type a value to apply to all $trackCount",
                                supportingText = "Leave blank to skip this field on Apply.",
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = if (field.kind == BulkTagFieldKind.TEXT) {
                                        KeyboardCapitalization.Words
                                    } else {
                                        KeyboardCapitalization.None
                                    },
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}
