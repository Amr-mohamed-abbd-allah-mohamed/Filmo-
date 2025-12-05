package com.example.filmo.uii

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth


@Composable
fun AppNavHost(
    isDark: Boolean,
    onToggleTheme: () -> Unit
) {
    val navController = rememberNavController()
    val moviesViewModel: MoviesViewModel = viewModel()

    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") {
            SplashScreen(navController)
        }

        composable("signup") {
            SignUpScreen(navController)
        }

        composable("login") {
            LoginScreen(navController)
        }

        composable("home") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                HomeScreen(
                    navController = navController,
                    viewModel = moviesViewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }


        composable("details/{movieId}") { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId")?.toInt() ?: 0
            DetailsScreen(navController, movieId)
        }

        composable("favourites") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                FavouritesScreen(
                    navController = navController,
                    viewModel = moviesViewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }

        composable("search") {
            Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                SearchScreen(
                    navController = navController,
                    viewModel = moviesViewModel,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }

        composable("profile") {
            androidx.compose.material3.Scaffold(
                bottomBar = { BottomNavigationBar(navController) }
            ) { paddingValues ->
                ProfileScreen(
                    navController = navController,
                    isDark = isDark,
                    onToggleTheme = onToggleTheme,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}
