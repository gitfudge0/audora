package dev.gitfudge.audora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.gitfudge.audora.ui.AppRoot
import dev.gitfudge.audora.ui.MainViewModel
import dev.gitfudge.audora.ui.RootUiState
import dev.gitfudge.audora.ui.theme.AudoraTheme
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var keepSplash = true
        splash.setKeepOnScreenCondition { keepSplash }
        lifecycleScope.launch {
            viewModel.uiState.takeWhile { it is RootUiState.Loading }.collect {}
            keepSplash = false
        }
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            AudoraTheme(themeMode = themeMode) {
                AppRoot(viewModel)
            }
        }
    }
}
