package com.example.filmo.uii

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.filmo.R
import com.example.filmo.data.model.Movie
import com.example.filmo.data.remote.RetrofitInstance
import com.example.filmo.ui.theme.BackGround
import com.example.filmo.ui.theme.Maroon
import com.example.filmo.ui.theme.MyAppTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: MoviesViewModel,
    modifier: Modifier = Modifier
) {
    var popularMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var topRatedMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var upcomingMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var nowPlayingMovies by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var latestMovie by remember { mutableStateOf<Movie?>(null) }
    var isDark by remember { mutableStateOf(false) }

    val favouriteMovies by viewModel.favouriteMovies.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFavourites()
        try {
            popularMovies = RetrofitInstance.api.getPopularMovies().results
            topRatedMovies = RetrofitInstance.api.getTopRatedMovies().results
            upcomingMovies = RetrofitInstance.api.getUpcomingMovies().results
            nowPlayingMovies = RetrofitInstance.api.getNowPlayingMovies().results
            latestMovie = RetrofitInstance.api.getLatestMovie()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val gold = Color(0xFFD3C0AF)
    val darkRed = Color(0xFF6E0A01)
    val darkBackground = Color(0xFF1A0907)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            )
            {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    {
                        Image(
                            painter = painterResource(id = R.drawable.filmo_logo),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .size(140.dp)
                                .align(Alignment.TopStart)
                        )
                        Text(
                            text = "Filmo",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            fontFamily = FontFamily(Font(R.font.blanka)),
                            modifier = Modifier
                                .align(Alignment.Center)
                        )

                    }

                }
            }
        }

        item {
            MovieRow(
                "🔥 Popular Movies",
                popularMovies,
                navController,
                viewModel,
                favouriteMovies,
                MaterialTheme.colorScheme.primary,
                darkRed
            )
        }
        item {
            MovieRow(
                "⭐ Top Rated",
                topRatedMovies,
                navController,
                viewModel,
                favouriteMovies,
                MaterialTheme.colorScheme.primary,
                darkRed
            )
        }
        item {
            MovieRow(
                "🎞️ Upcoming",
                upcomingMovies,
                navController,
                viewModel,
                favouriteMovies,
                MaterialTheme.colorScheme.primary,
                darkRed
            )
        }
        item {
            MovieRow(
                "🎥 Now Playing",
                nowPlayingMovies,
                navController,
                viewModel,
                favouriteMovies,
                MaterialTheme.colorScheme.primary,
                darkRed
            )
        }

        latestMovie?.let { movie ->
            item {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "🆕 Latest Movie",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    MovieThumbnail(movie, darkRed, MaterialTheme.colorScheme.primary, viewModel, favouriteMovies) {
                        navController.navigate("details/${movie.id}")
                    }
                }
            }
        }
    }
}

@Composable
fun MovieRow(
    title: String,
    movies: List<Movie>,
    navController: NavController,
    viewModel: MoviesViewModel,
    favouriteMovies: List<Movie>,
    primary: Color,
    darkRed: Color
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            items(movies) { movie ->
                MovieThumbnail(movie, darkRed, MaterialTheme.colorScheme.primary, viewModel, favouriteMovies) {
                    navController.navigate("details/${movie.id}")
                }
            }
        }
    }
}

@Composable
fun MovieThumbnail(
    movie: Movie,
    gold: Color,
    primary: Color,
    viewModel: MoviesViewModel,
    favouriteMovies: List<Movie>,
    onClick: () -> Unit
) {
    val isFavourite = favouriteMovies.any { it.id == movie.id }

    Box(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Card(
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Box {
                    Image(
                        painter = rememberAsyncImagePainter("https://image.tmdb.org/t/p/w500${movie.posterPath}"),
                        contentDescription = movie.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    IconButton(
                        onClick = {
                            if (isFavourite) viewModel.removeFromFavourites(movie)
                            else viewModel.addToFavourites(movie)
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavourite) "Remove from favourites" else "Add to favourites",
                            tint = if (isFavourite) Color.Red else gold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = movie.title ?: "No Title",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun ThemeSwitch(
    isDark: Boolean,
    onToggle: () -> Unit
) {
    // استخدم rememberUpdatedState عشان يحافظ على onToggle بعد recomposition
    val currentToggle by rememberUpdatedState(onToggle)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isDark) "Dark Mode" else "Light Mode",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Switch(
            checked = isDark,
            onCheckedChange = { currentToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.secondary
            )
        )
    }
}

