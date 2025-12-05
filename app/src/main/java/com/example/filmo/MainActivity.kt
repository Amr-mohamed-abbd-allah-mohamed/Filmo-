package com.example.filmo

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.filmo.ui.theme.MyAppTheme
import com.example.filmo.ui.theme.ThemeManager
import com.example.filmo.ui.theme.ThemeViewModel
import com.example.filmo.ui.theme.ThemeViewModelFactory
import com.example.filmo.uii.AppNavHost
import com.example.filmo.uii.MoviesViewModel

class MainActivity : ComponentActivity() {
    private lateinit var themeViewModelFactory: ThemeViewModelFactory
    private val themeViewModel: ThemeViewModel by viewModels { themeViewModelFactory }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val splashScreen = installSplashScreen()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setKeepOnScreenCondition { false } // مباشرة تقفلها
        }
        // انشئ ThemeManager و Factory
        val themeManager = ThemeManager(this)
        themeViewModelFactory = ThemeViewModelFactory(themeManager)
        setContent {
            val moviesViewModel: MoviesViewModel = viewModel()
            val isDark by themeViewModel.isDark.collectAsState()

            LaunchedEffect(Unit) {
                moviesViewModel.loadFavourites()
            }
            MyAppTheme(darkTheme = isDark) {
                AppNavHost(
                    isDark = isDark,
                    onToggleTheme = { themeViewModel.toggleTheme() }
                )
            }
        }
    }
}


