package dev.gitfudge.audora.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.gitfudge.audora.ui.theme.LocalShapeScale
import dev.gitfudge.audora.ui.theme.AudoraTheme
import dev.gitfudge.audora.ui.theme.ThemeMode

/**
 * Text field. surfaceVariant background, no baseline; focus = 1dp accent
 * border via OutlinedTextField with custom container color.
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    supportingText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val shape = LocalShapeScale.current.md
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.clip(shape),
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        isError = isError,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        shape = shape,
        supportingText = supportingText?.let { { Text(it) } },
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            errorContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}

@Composable
private fun TextFieldsPreviewContent() {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppTextField(value = "Tame Impala", onValueChange = {}, label = "Artist")
        AppTextField(value = "", onValueChange = {}, label = "Album", placeholder = "Currents")
        AppTextField(
            value = "Mismatched",
            onValueChange = {},
            label = "Track",
            isError = true,
            supportingText = "Differs across selection",
        )
    }
}

@Preview(name = "TextFields · Light") @Composable
private fun TextFieldsLightPreview() = AudoraTheme(themeMode = ThemeMode.Light) { TextFieldsPreviewContent() }

@Preview(name = "TextFields · Dark") @Composable
private fun TextFieldsDarkPreview() = AudoraTheme(themeMode = ThemeMode.Dark) { TextFieldsPreviewContent() }
