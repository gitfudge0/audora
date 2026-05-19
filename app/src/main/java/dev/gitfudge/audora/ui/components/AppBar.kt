package dev.gitfudge.audora.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.gitfudge.audora.ui.theme.AudoraTheme
import dev.gitfudge.audora.ui.theme.ThemeMode

/**
 * App top bar. background color (not surface), no shadow; titleScreen style;
 * hairline divider on scroll handled by caller via [AppBarDivider].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        navigationIcon = navigationIcon ?: {},
        actions = {
            if (actions != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(end = 4.dp),
                ) { actions() }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        modifier = modifier,
    )
}

@Composable
fun AppBarDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppBarPreviewContent() {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
    ) {
        AppTopBar(title = "Library")
        AppBarDivider()
    }
}

@Preview(name = "AppBar · Light") @Composable
private fun AppBarLightPreview() = AudoraTheme(themeMode = ThemeMode.Light) { AppBarPreviewContent() }

@Preview(name = "AppBar · Dark") @Composable
private fun AppBarDarkPreview() = AudoraTheme(themeMode = ThemeMode.Dark) { AppBarPreviewContent() }
