package com.example.filmo.uii

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.filmo.data.model.Movie
import com.example.filmo.data.remote.RetrofitInstance
import com.example.filmo.ui.theme.BackGround
import com.example.filmo.ui.theme.LightGray
import com.example.filmo.ui.theme.Maroon
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: MoviesViewModel,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val favouriteMovies by viewModel.favouriteMovies.collectAsState()

    val gold = Color(0xFFD3C0AF)
    val darkRed = Color(0xFF6E0A01)
    val darkBackground = Color(0xFF1A0907)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // شريط البحث
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { query ->
                searchQuery = query
                if (query.length >= 2) {
                    coroutineScope.launch {
                        isLoading = true
                        try {
                            val response = RetrofitInstance.api.searchMovies(query)
                            searchResults = response.results
                        } catch (e: Exception) {
                            searchResults = emptyList()
                        }
                        isLoading = false
                    }
                } else {
                    searchResults = emptyList()
                }
            },
            label = { Text("Search movies...", color = LightGray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = gold) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.primary,
                unfocusedTextColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = darkRed.copy(alpha = 0.3f),
                unfocusedContainerColor = darkRed.copy(alpha = 0.3f),
                cursorColor = gold
            )
        )

        Spacer(modifier = Modifier.height(16.dp))


        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = gold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Searching...", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            searchResults.isNotEmpty() -> {
                Text(
                    "Found ${searchResults.size} results for '$searchQuery'",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(searchResults) { movie ->
                        MovieThumbnail(
                            movie = movie,
                            primary = MaterialTheme.colorScheme.primary,
                            gold = gold,
                            viewModel = viewModel,
                            favouriteMovies = favouriteMovies
                        ) {
                            navController.navigate("details/${movie.id}")
                        }
                    }
                }
            }
            searchQuery.length >= 2 -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No movies found for '$searchQuery'", color = gold)
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Type to search movies...", color = Color.LightGray)
                }
            }
        }
    }
}