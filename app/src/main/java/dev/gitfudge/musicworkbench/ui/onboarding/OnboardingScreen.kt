package dev.gitfudge.musicworkbench.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.gitfudge.musicworkbench.R
import dev.gitfudge.musicworkbench.ui.theme.LocalSpacing
import dev.gitfudge.musicworkbench.ui.theme.MusicWorkbenchTheme

@Composable
fun OnboardingScreen(onPickFolder: () -> Unit) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme
    // Matches the Android adaptive icon mask used by the launcher (~22dp on a
    // 96dp tile), not `shapes.extraLarge` (24dp), so the in-app logo matches
    // the launcher icon shape exactly.
    val launcherIconShape = RoundedCornerShape(22.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = launcherIconShape,
            color = colors.surfaceContainer,
            modifier = Modifier.size(96.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = stringResource(R.string.cd_app_logo),
                    modifier = Modifier
                        .size(96.dp)
                        .clip(launcherIconShape),
                    tint = Color.Unspecified,
                )
            }
        }

        Spacer(Modifier.height(spacing.xl))

        Text(
            text = stringResource(R.string.onboarding_title),
            style = MaterialTheme.typography.titleLarge,
            color = colors.onBackground,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(spacing.md))

        Text(
            text = stringResource(R.string.onboarding_body),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 320.dp),
        )

        Spacer(Modifier.height(spacing.xl))

        // Button defaults already use `colors.primary` / `colors.onPrimary` from the
        // theme; no explicit colors block needed.
        Button(
            onClick = onPickFolder,
            contentPadding = PaddingValues(
                horizontal = spacing.xl,
                vertical = spacing.md,
            ),
        ) {
            Icon(
                imageVector = Icons.Rounded.FolderOpen,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.size(spacing.sm))
            Text(
                text = stringResource(R.string.onboarding_pick_folder),
                style = MaterialTheme.typography.labelLarge,
            )
        }

        Spacer(Modifier.height(spacing.xl))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            modifier = Modifier.widthIn(max = 320.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = stringResource(R.string.onboarding_permission_note),
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@Preview(name = "Onboarding", showBackground = true, backgroundColor = 0xFF14161A)
@Composable
private fun OnboardingPreview() {
    MusicWorkbenchTheme {
        OnboardingScreen(onPickFolder = {})
    }
}
