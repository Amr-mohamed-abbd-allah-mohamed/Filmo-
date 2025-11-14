package com.example.filmo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.filmo.uii.AppNavHost
import com.example.filmo.uii.MoviesViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val moviesViewModel: MoviesViewModel = viewModel()

            // تحميل الـ favourites من Firestore أول ما يفتح التطبيق
            LaunchedEffect(Unit) {
                moviesViewModel.loadFavourites()
            }

            AppNavHost()
        }
    }

}
