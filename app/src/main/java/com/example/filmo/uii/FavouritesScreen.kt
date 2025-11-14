    package com.example.filmo.uii
    import androidx.compose.foundation.background
    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.lazy.items
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.Delete
    import androidx.compose.material.icons.filled.Favorite
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.layout.ContentScale
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.text.style.TextOverflow
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import androidx.navigation.NavController
    import coil.compose.rememberAsyncImagePainter
    import com.example.filmo.data.model.Movie
    @Composable
    fun FavouritesScreen(
        navController: NavController,
        viewModel: MoviesViewModel,
        modifier: Modifier = Modifier
    ) {
        val favouriteMovies by viewModel.favouriteMovies.collectAsState()
        val gold = Color(0xFFD3C0AF)
        val darkRed = Color(0xFF6E0A01)
        val darkBackground = Color(0xFF1A0907)

        LaunchedEffect(Unit) { viewModel.loadFavourites() }

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(darkBackground)
        ) {

            Text(
                text = "My List",
                color = gold,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            if (favouriteMovies.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "No favourites",
                        tint = gold,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No favourite movies yet",
                        color = gold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add movies from Home or Details screen",
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favouriteMovies) { movie ->
                        FavouriteMovieItem(
                            movie = movie,
                            onMovieClick = { navController.navigate("details/${movie.id}") },
                            onRemoveClick = { viewModel.removeFromFavourites(movie) },
                            gold = gold,
                            darkRed = darkRed
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun FavouriteMovieItem(
        movie: Movie,
        onMovieClick: () -> Unit,
        onRemoveClick: () -> Unit,
        gold: Color,
        darkRed: Color
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onMovieClick() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = darkRed.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Poster
                Card(
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(80.dp)
                ) {
                    val imageUrl = movie.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
                        ?: "https://via.placeholder.com/500x750/333333/FFFFFF?text=No+Image"

                    androidx.compose.foundation.Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = movie.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = movie.title ?: "No Title",
                        color = gold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⭐", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${movie.voteAverage ?: "?"}/10", color = Color.LightGray, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📅", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = movie.releaseDate ?: "N/A", color = Color.LightGray, fontSize = 12.sp)
                    }
                }

                IconButton(onClick = onRemoveClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove from favourites",
                        tint = gold
                    )
                }
            }
        }
    }