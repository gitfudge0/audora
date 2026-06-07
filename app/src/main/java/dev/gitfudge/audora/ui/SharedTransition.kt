@file:OptIn(ExperimentalSharedTransitionApi::class)

package dev.gitfudge.audora.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

/**
 * The two scopes a shared-element transition needs, exposed as composition
 * locals so feature composables can opt into the morph without every
 * intermediate signature having to forward them.
 *
 * - [LocalSharedTransitionScope] is provided once, around the whole NavHost,
 *   by the enclosing `SharedTransitionLayout`.
 * - [LocalNavAnimatedVisibilityScope] is provided per destination (the
 *   `AnimatedContentScope` each `composable {}` lambda runs in), so the
 *   element knows which enter/exit transition drives it.
 *
 * Both are null when no shared-transition host is present (e.g. previews),
 * in which case [albumArtSharedElement] is a no-op.
 */
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/**
 * Marks a composable as the album-cover shared element. The list-side tile and
 * the detail-side hero use the same [albumKey], so the container morphs (bounds
 * + image) from one to the other across the navigation transition instead of
 * the detail content popping in after the slide settles.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.albumArtSharedElement(albumKey: String): Modifier {
    val sharedScope = LocalSharedTransitionScope.current ?: return this
    val visibilityScope = LocalNavAnimatedVisibilityScope.current ?: return this
    return with(sharedScope) {
        this@albumArtSharedElement.sharedElement(
            rememberSharedContentState(key = "albumArt/$albumKey"),
            animatedVisibilityScope = visibilityScope,
        )
    }
}
