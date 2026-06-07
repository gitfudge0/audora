package dev.gitfudge.audora.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.gitfudge.audora.ui.components.PrimaryButton
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.gitfudge.audora.ui.album.AlbumDetailScreen
import dev.gitfudge.audora.ui.detail.TrackDetailScreen
import dev.gitfudge.audora.ui.library.LibraryScaffoldScreen
import dev.gitfudge.audora.ui.library.LibraryViewModel
import dev.gitfudge.audora.ui.onboarding.OnboardingScreen
import dev.gitfudge.audora.ui.onboarding.WalkthroughScreen
import dev.gitfudge.audora.ui.settings.SettingsScreen
import dev.gitfudge.audora.ui.theme.LocalMotion
import dev.gitfudge.audora.ui.theme.AudoraTheme
import dev.gitfudge.audora.ui.unfiled.UnfiledScreen

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppRoot(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val pickFolder = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { uri -> if (uri != null) viewModel.onFolderPicked(uri) }

    val motion = LocalMotion.current

    // Scoped to the Activity so LibraryViewModel survives detail ↔ library navigation.
    val libraryViewModel: LibraryViewModel = hiltViewModel()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        AnimatedContent(
            targetState = state,
            transitionSpec = {
                fadeIn(motion.spec(motion.standard)) togetherWith
                    fadeOut(motion.spec(motion.fast))
            },
            label = "root",
        ) { current ->
            when (current) {
                RootUiState.Loading -> Unit // splash screen covers this frame

                RootUiState.Walkthrough ->
                    WalkthroughScreen(onFinish = { viewModel.setWalkthroughSeen(true) })

                RootUiState.Onboarding ->
                    OnboardingScreen(onPickFolder = { pickFolder.launch(null) })

                is RootUiState.PermissionLost ->
                    PermissionLostScreen(
                        folderLabel = current.folderLabel,
                        onReselect = { pickFolder.launch(null) },
                    )

                is RootUiState.Library -> {
                    val navController = rememberNavController()
                    val routeDuration = if (motion.reducedMotion) motion.fast else motion.deliberate
                    SharedTransitionLayout {
                      CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                    NavHost(
                        navController = navController,
                        startDestination = "library",
                        enterTransition = {
                            if (motion.reducedMotion) {
                                fadeIn(motion.spec(routeDuration))
                            } else {
                                slideInHorizontally(motion.spec(routeDuration)) { it }
                            }
                        },
                        exitTransition = {
                            if (motion.reducedMotion) {
                                fadeOut(motion.spec(routeDuration))
                            } else {
                                slideOutHorizontally(motion.spec(routeDuration)) { -it / 3 }
                            }
                        },
                        popEnterTransition = {
                            if (motion.reducedMotion) {
                                fadeIn(motion.spec(routeDuration))
                            } else {
                                slideInHorizontally(motion.spec(routeDuration)) { -it / 3 }
                            }
                        },
                        popExitTransition = {
                            if (motion.reducedMotion) {
                                fadeOut(motion.spec(routeDuration))
                            } else {
                                slideOutHorizontally(motion.spec(routeDuration)) { it }
                            }
                        },
                    ) {
                        composable("library") {
                          CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
                            LibraryScaffoldScreen(
                                folderLabel = current.folderLabel,
                                onChangeFolder = { pickFolder.launch(null) },
                                onTrackClick = { docUri ->
                                    navController.navigate("detail/${Uri.encode(docUri)}")
                                },
                                onAlbumClick = { albumKey ->
                                    navController.navigate("album/${Uri.encode(albumKey)}")
                                },
                                onUnfiledClick = { navController.navigate("unfiled") },
                                onOpenSettings = { navController.navigate("settings") },
                                viewModel = libraryViewModel,
                            )
                          }
                        }
                        composable("settings") {
                            SettingsScreen(
                                onBack = { navController.popBackStack() },
                                viewModel = viewModel,
                                folderLabel = current.folderLabel,
                                onChangeFolder = { pickFolder.launch(null) },
                                onOpenLicenses = { navController.navigate("licenses") },
                                onReplayWalkthrough = { viewModel.setWalkthroughSeen(false) },
                            )
                        }
                        composable("licenses") {
                            dev.gitfudge.audora.ui.settings.LicensesScreen(
                                onBack = { navController.popBackStack() },
                            )
                        }
                        composable(
                            route = "detail/{uri}",
                            arguments = listOf(navArgument("uri") { type = NavType.StringType }),
                        ) {
                            TrackDetailScreen(onBack = { navController.popBackStack() })
                        }
                        composable(
                            route = "album/{albumKey}",
                            arguments = listOf(navArgument("albumKey") { type = NavType.StringType }),
                        ) { backStackEntry ->
                          CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
                            // Show a cheap skeleton while the enter slide is running
                            // so the animation never contends with a heavy first
                            // composition. Once currentState == targetState the
                            // transition is settled and the real content can render.
                            val transitionComplete =
                                transition.currentState == transition.targetState
                            // Nav decodes the arg back to the same album key the row
                            // holds (summary.albumKey), so the shared-element keys
                            // line up on both sides of the transition.
                            val albumKey =
                                backStackEntry.arguments?.getString("albumKey").orEmpty()
                            AlbumDetailScreen(
                                albumKey = albumKey,
                                onBack = { navController.popBackStack() },
                                onTrackClick = { docUri ->
                                    navController.navigate("detail/${Uri.encode(docUri)}")
                                },
                                transitionComplete = transitionComplete,
                            )
                          }
                        }
                        composable("unfiled") {
                            UnfiledScreen(
                                onBack = { navController.popBackStack() },
                                onTrackClick = { docUri ->
                                    navController.navigate("detail/${Uri.encode(docUri)}")
                                },
                            )
                        }
                    }
                      }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionLostScreen(folderLabel: String, onReselect: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.FolderOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(40.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Lost access to “$folderLabel”",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "The folder permission was revoked or the storage changed. " +
                "Re-select the folder to keep working — your files are untouched.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 320.dp),
        )
        Spacer(Modifier.height(24.dp))
        PrimaryButton(onClick = onReselect) { Text("Re-select folder") }
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Root · Library", showBackground = true)
@Composable
private fun AppRootLibraryPreview() {
    AudoraTheme {
        LibraryScaffoldScreen(folderLabel = "Music", onChangeFolder = {}, onTrackClick = {})
    }
}
