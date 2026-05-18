package dev.gitfudge.musicworkbench.ui.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.gitfudge.musicworkbench.R
import dev.gitfudge.musicworkbench.ui.components.GhostButton
import dev.gitfudge.musicworkbench.ui.components.PrimaryButton
import dev.gitfudge.musicworkbench.ui.theme.LocalMotion
import dev.gitfudge.musicworkbench.ui.theme.LocalSpacing
import dev.gitfudge.musicworkbench.ui.theme.MusicWorkbenchTheme
import dev.gitfudge.musicworkbench.ui.theme.ThemeMode
import kotlinx.coroutines.launch

private data class WalkthroughPage(
    val icon: ImageVector,
    val titleRes: Int,
    val bodyRes: Int,
)

private val pages = listOf(
    WalkthroughPage(Icons.Rounded.LibraryMusic, R.string.walkthrough_p1_title, R.string.walkthrough_p1_body),
    WalkthroughPage(Icons.Rounded.FilterAlt, R.string.walkthrough_p2_title, R.string.walkthrough_p2_body),
    WalkthroughPage(Icons.Rounded.DoneAll, R.string.walkthrough_p3_title, R.string.walkthrough_p3_body),
    WalkthroughPage(Icons.Rounded.Lock, R.string.walkthrough_p4_title, R.string.walkthrough_p4_body),
)

/**
 * First-run feature tour. Swipeable pages; "Skip" on any page and "Get started"
 * on the last both call [onFinish], which leads into the folder picker.
 */
@Composable
fun WalkthroughScreen(onFinish: () -> Unit) {
    val spacing = LocalSpacing.current
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == pages.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = spacing.xl, vertical = spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) { page ->
            PageContent(pages[page])
        }

        Spacer(Modifier.height(spacing.xl))

        PageDots(count = pages.size, selected = pagerState.currentPage)

        Spacer(Modifier.height(spacing.xl))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GhostButton(onClick = onFinish) {
                Text(
                    text = stringResource(R.string.walkthrough_skip),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            PrimaryButton(
                onClick = {
                    if (isLast) {
                        onFinish()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
            ) {
                Text(
                    text = stringResource(
                        if (isLast) R.string.walkthrough_get_started else R.string.walkthrough_next,
                    ),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun PageContent(page: WalkthroughPage) {
    val spacing = LocalSpacing.current
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = CircleShape,
            color = colors.surfaceVariant,
            modifier = Modifier.size(88.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    tint = colors.onSurface,
                    modifier = Modifier.size(36.dp),
                )
            }
        }

        Spacer(Modifier.height(spacing.xl))

        Text(
            text = stringResource(page.titleRes),
            style = MaterialTheme.typography.titleLarge,
            color = colors.onBackground,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(spacing.md))

        Text(
            text = stringResource(page.bodyRes),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 320.dp),
        )
    }
}

@Composable
private fun PageDots(count: Int, selected: Int) {
    val colors = MaterialTheme.colorScheme
    val motion = LocalMotion.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { i ->
            val active = i == selected
            val width by animateDpAsState(
                targetValue = if (active) 20.dp else 6.dp,
                animationSpec = motion.spec(motion.standard),
                label = "dotWidth",
            )
            val color by animateColorAsState(
                targetValue = if (active) colors.primary else colors.outlineVariant,
                animationSpec = motion.spec(motion.standard),
                label = "dotColor",
            )
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}

@Preview(name = "Walkthrough · Light")
@Composable
private fun WalkthroughLightPreview() {
    MusicWorkbenchTheme(themeMode = ThemeMode.Light) {
        WalkthroughScreen(onFinish = {})
    }
}

@Preview(name = "Walkthrough · Dark")
@Composable
private fun WalkthroughDarkPreview() {
    MusicWorkbenchTheme(themeMode = ThemeMode.Dark) {
        WalkthroughScreen(onFinish = {})
    }
}
