package dev.gitfudge.musicworkbench.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.gitfudge.musicworkbench.ui.album.AlbumDetailScreen
import dev.gitfudge.musicworkbench.ui.detail.TrackDetailScreen
import dev.gitfudge.musicworkbench.ui.library.LibraryScaffoldScreen
import dev.gitfudge.musicworkbench.ui.library.LibraryViewModel
import dev.gitfudge.musicworkbench.ui.onboarding.OnboardingScreen
import dev.gitfudge.musicworkbench.ui.theme.LocalMotion
import dev.gitfudge.musicworkbench.ui.theme.MusicWorkbenchTheme

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

                RootUiState.Onboarding ->
                    OnboardingScreen(onPickFolder = { pickFolder.launch(null) })

                is RootUiState.Library -> {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "library") {
                        composable("library") {
                            LibraryScaffoldScreen(
                                folderLabel = current.folderLabel,
                                onChangeFolder = { pickFolder.launch(null) },
                                onTrackClick = { docUri ->
                                    navController.navigate("detail/${Uri.encode(docUri)}")
                                },
                                onAlbumClick = { albumKey ->
                                    navController.navigate("album/${Uri.encode(albumKey)}")
                                },
                                viewModel = libraryViewModel,
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
                        ) {
                            AlbumDetailScreen(
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

@androidx.compose.ui.tooling.preview.Preview(name = "Root · Library", showBackground = true)
@Composable
private fun AppRootLibraryPreview() {
    MusicWorkbenchTheme {
        LibraryScaffoldScreen(folderLabel = "Music", onChangeFolder = {}, onTrackClick = {})
    }
}
