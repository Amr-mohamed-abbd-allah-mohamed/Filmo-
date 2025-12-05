package com.example.filmo.uii

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(navController: NavController) {
    val gold = Color(0xFFD3C0AF)
    val darkRed = Color(0xFF6E0A01)
    val darkerRed = Color(0xFF4A0500)

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar(
        containerColor = darkerRed,
        contentColor = gold
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = if (currentRoute == "home") gold else Color.LightGray
                )
            },
            label = {
                Text(
                    "Home",
                    color = if (currentRoute == "home") gold else Color.LightGray
                )
            },
            selected = currentRoute == "home",
            onClick = {
                navController.navigate("home") {
                    popUpTo("home") { inclusive = false }
                }
            }
        )

        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = if (currentRoute == "search") gold else Color.LightGray
                )
            },
            label = {
                Text(
                    "Search",
                    color = if (currentRoute == "search") gold else Color.LightGray
                )
            },
            selected = currentRoute == "search",
            onClick = {
                navController.navigate("search") {
                    popUpTo("home") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )

        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Favourites",
                    tint = if (currentRoute == "favourites") gold else Color.LightGray
                )
            },
            label = {
                Text(
                    "Favourites",
                    color = if (currentRoute == "favourites") gold else Color.LightGray
                )
            },
            selected = currentRoute == "favourites",
            onClick = {
                navController.navigate("favourites") {
                    popUpTo("home") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )


        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = if (currentRoute == "profile") gold else Color.LightGray
                )
            },
            label = {
                Text(
                    "Profile",
                    color = if (currentRoute == "profile") gold else Color.LightGray
                )
            },
            selected = currentRoute == "profile",
            onClick = {
                navController.navigate("profile") {
                    popUpTo("home") { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
}

