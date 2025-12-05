package com.example.filmo.uii

import android.R.color.white
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
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
    var trailerKey by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // -------- GET MOVIE DETAILS --------
    LaunchedEffect(movieId) {
        coroutineScope.launch {
            try {
                val details = RetrofitInstance.api.getMovieDetails(movieId)
                movie = details
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // -------- GET MOVIE TRAILER --------
    LaunchedEffect(movieId) {
        coroutineScope.launch {
            try {
                val videoResponse = RetrofitInstance.api.getMovieVideos(movieId)
                val trailer = videoResponse.results.firstOrNull {
                    it.type == "Trailer" && it.site == "YouTube"
                }
                trailerKey = trailer?.key
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
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ---------- HEADER IMAGE ----------
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

                // ⬅⬅⬅ BACK BUTTON HERE
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

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

            // ---------- TITLE ----------
            Text(
                text = m.title ?: "No Title",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                fontSize = 26.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ---------- STATS ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "⭐ ${String.format("%.1f", m.voteAverage ?: 0.0)}/10", color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.width(12.dp))
                Text("⏱️ ${m.runtime} min", color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Text("📅 ${m.releaseDate}", color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ---------- OVERVIEW ----------
            Text(
                text = m.overview ?: "",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
                textAlign = TextAlign.Justify
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ---------- TRAILER BUTTON ----------
            trailerKey?.let { key ->
                Button(
                    onClick = {
                        val url = "https://www.youtube.com/watch?v=$key"
                        val context = navController.context
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse(url)
                        )
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = darkRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("▶ Watch Trailer", color = gold, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // ---------- CAST ----------
            Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))

            Text(
                text = "Cast",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
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
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))



            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}