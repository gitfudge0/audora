package dev.gitfudge.audora.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.gitfudge.audora.R

@Composable
fun AppLogo(modifier: Modifier = Modifier, size: Dp = 96.dp) {
    val colors = MaterialTheme.colorScheme
    val cd = stringResource(R.string.cd_app_logo)
    Icon(
        painter = painterResource(R.drawable.ic_audora_mark),
        contentDescription = null,
        tint = colors.onSurface,
        modifier = modifier
            .size(size)
            .semantics { contentDescription = cd },
    )
}
