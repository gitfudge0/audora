package dev.gitfudge.audora.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.gitfudge.audora.ui.theme.LocalShapeScale
import dev.gitfudge.audora.ui.theme.AudoraTheme
import dev.gitfudge.audora.ui.theme.ThemeMode

/**
 * Surfaces. Hairlines instead of shadows. Cards are bordered; Panels are
 * flat tonal blocks for grouped fields.
 */

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = LocalShapeScale.current.lg,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) { content() }
}

@Composable
fun AppPanel(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = LocalShapeScale.current.lg,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) { content() }
}

@Composable
fun Hairline(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

@Composable
fun InsetHairline(
    modifier: Modifier = Modifier,
    insetStart: androidx.compose.ui.unit.Dp = 16.dp,
    insetEnd: androidx.compose.ui.unit.Dp = 16.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = insetStart, end = insetEnd)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

@Composable
private fun SurfacesPreviewContent() {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Text("Card", modifier = Modifier.padding(16.dp))
        }
        AppPanel(modifier = Modifier.fillMaxWidth()) {
            Text("Panel", modifier = Modifier.padding(16.dp))
        }
        Hairline()
        InsetHairline()
    }
}

@Preview(name = "Surfaces · Light")
@Composable
private fun SurfacesLightPreview() {
    AudoraTheme(themeMode = ThemeMode.Light) { SurfacesPreviewContent() }
}

@Preview(name = "Surfaces · Dark")
@Composable
private fun SurfacesDarkPreview() {
    AudoraTheme(themeMode = ThemeMode.Dark) { SurfacesPreviewContent() }
}
