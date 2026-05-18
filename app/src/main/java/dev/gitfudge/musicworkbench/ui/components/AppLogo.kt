package dev.gitfudge.musicworkbench.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import dev.gitfudge.musicworkbench.R

/**
 * The "MW" monogram tile. Matches the Android adaptive-icon mask (~22dp on a
 * 96dp tile) so the in-app logo reads as the same shape as the launcher icon.
 */
@Composable
fun AppLogo(modifier: Modifier = Modifier, size: Dp = 96.dp) {
    val colors = MaterialTheme.colorScheme
    val cd = stringResource(R.string.cd_app_logo)
    Surface(
        shape = RoundedCornerShape(size * 22f / 96f),
        color = colors.onSurface,
        modifier = modifier
            .size(size)
            .semantics { contentDescription = cd },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "MW",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.025).em,
                ),
                color = colors.background,
            )
        }
    }
}
