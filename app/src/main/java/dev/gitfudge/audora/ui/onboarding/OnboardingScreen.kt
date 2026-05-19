package dev.gitfudge.audora.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.gitfudge.audora.R
import dev.gitfudge.audora.ui.components.AppLogo
import dev.gitfudge.audora.ui.components.PrimaryButton
import dev.gitfudge.audora.ui.theme.LocalSpacing
import dev.gitfudge.audora.ui.theme.AudoraTheme
import dev.gitfudge.audora.ui.theme.ThemeMode

@Composable
fun OnboardingScreen(onPickFolder: () -> Unit) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AppLogo()

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

        PrimaryButton(onClick = onPickFolder) {
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

@Preview(name = "Onboarding · Light")
@Composable
private fun OnboardingLightPreview() {
    AudoraTheme(themeMode = ThemeMode.Light) {
        OnboardingScreen(onPickFolder = {})
    }
}

@Preview(name = "Onboarding · Dark")
@Composable
private fun OnboardingDarkPreview() {
    AudoraTheme(themeMode = ThemeMode.Dark) {
        OnboardingScreen(onPickFolder = {})
    }
}
