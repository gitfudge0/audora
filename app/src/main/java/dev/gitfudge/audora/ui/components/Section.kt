package dev.gitfudge.audora.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LibraryMusic
import dev.gitfudge.audora.ui.theme.AudoraTheme
import dev.gitfudge.audora.ui.theme.ThemeMode

/**
 * Section header. Title (titleSection) with optional trailing slot for an
 * action (e.g. a count chip or "See all"). Vertical rhythm: 20dp top, 12dp
 * bottom so groups breathe.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    paddingTop: Dp = 20.dp,
    paddingBottom: Dp = 12.dp,
    paddingHorizontal: Dp = 16.dp,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = paddingHorizontal, end = paddingHorizontal, top = paddingTop, bottom = paddingBottom),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) trailing()
    }
}

/**
 * Empty state. Icon over title over body over optional action.
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 340.dp),
        )
        if (action != null) {
            Box(modifier = Modifier.padding(top = 8.dp)) { action() }
        }
    }
}

@Composable
private fun SectionPreviewContent() {
    Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        SectionHeader(title = "Albums", trailing = { CountChip(count = 12) })
        SectionHeader(title = "Currents")
        EmptyState(
            icon = Icons.Rounded.LibraryMusic,
            title = "Nothing here yet",
            body = "Pick a folder to scan and we'll show you what's inside.",
        )
    }
}

@Preview(name = "Section · Light") @Composable
private fun SectionLightPreview() = AudoraTheme(themeMode = ThemeMode.Light) { SectionPreviewContent() }

@Preview(name = "Section · Dark") @Composable
private fun SectionDarkPreview() = AudoraTheme(themeMode = ThemeMode.Dark) { SectionPreviewContent() }
