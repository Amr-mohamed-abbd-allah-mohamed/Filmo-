package com.example.filmo.uii

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.filmo.data.model.Movie

import com.example.filmo.data.remote.RetrofitInstance
import kotlinx.coroutines.launch

@Composable
fun DetailsScreen(navController: NavController, movieId: Int) {
    var movie by remember { mutableStateOf<Movie?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(movieId) {
        coroutineScope.launch {
            try {
                val response = RetrofitInstance.api.getMovieDetails(movieId)
                movie = response
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    val gold = Color(0xFFD3C0AF)
    val darkRed = Color(0xFF6E0A01)
    val darkBackground = Color(0xFF1A0907)

    movie?.let { m ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(darkBackground)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                val imageUrl = "https://image.tmdb.org/t/p/w500${m.posterPath}"
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = m.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, darkBackground.copy(alpha = 0.95f)),
                                startY = 200f
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = m.title ?: "No Title",
                color = gold,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                fontSize = 26.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "⭐ ${m.voteAverage ?: "?"}/10",
                    color = gold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "⏱️ ${m.runtime ?: "?"} min",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "📅 ${m.releaseDate ?: "N/A"}",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = m.overview ?: "No Overview available",
                color = Color(0xFFECE0D1),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
                textAlign = TextAlign.Justify
            )

            Spacer(modifier = Modifier.height(28.dp))

            Divider(
                color = darkRed.copy(alpha = 0.6f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = "Cast",
                color = gold,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow {
                m.credits?.cast?.take(15)?.forEach { actor ->
                    item {
                        val actorImage = if (actor.profilePath != null)
                            "https://image.tmdb.org/t/p/w200${actor.profilePath}"
                        else
                            "https://cdn-icons-png.flaticon.com/512/1077/1077114.png"

                        Column(
                            modifier = Modifier
                                .width(100.dp)
                                .padding(end = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(actorImage),
                                contentDescription = actor.name,
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(Color.DarkGray),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = actor.name ?: "",
                                color = gold,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = darkRed
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Back", color = gold, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

}
