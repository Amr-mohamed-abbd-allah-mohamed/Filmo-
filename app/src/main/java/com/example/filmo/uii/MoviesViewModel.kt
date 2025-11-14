package com.example.filmo.uii
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.filmo.data.model.Movie
import com.google.firebase.auth.FirebaseAuth

class MoviesViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _favouriteMovies = MutableStateFlow<List<Movie>>(emptyList())
    val favouriteMovies: StateFlow<List<Movie>> = _favouriteMovies


    fun addToFavourites(movie: com.example.filmo.data.model.Movie) {
        val user = auth.currentUser ?: return
        val uid = user.uid
        viewModelScope.launch {
            try {
                val favouriteData = hashMapOf(
                    "movieId" to movie.id,
                    "title" to movie.title,
                    "posterPath" to movie.posterPath,
                    "voteAverage" to movie.voteAverage,
                    "releaseDate" to movie.releaseDate,
                    "addedAt" to System.currentTimeMillis()
                )

                db.collection("users")
                    .document(uid)
                    .collection("favourites")
                    .document(movie.id.toString())
                    .set(favouriteData)
                    .await()

                loadFavourites()
                println(" Added to Firestore for $uid: ${movie.title}")

            } catch (e: Exception) {
                e.printStackTrace()
                println("Error adding to Firestore: ${e.message}")
            }
        }
    }


    fun removeFromFavourites(movie: Movie) {
        val user = auth.currentUser ?: return
        val uid = user.uid
        viewModelScope.launch {
            try {
                db.collection("users")
                    .document(uid)
                    .collection("favourites")
                    .document(movie.id.toString())
                    .delete()
                    .await()

                loadFavourites()
                println("Removed from Firestore for $uid: ${movie.title}")

            } catch (e: Exception) {
                e.printStackTrace()
                println("Error removing from Firestore: ${e.message}")
            }
        }
    }


    fun loadFavourites() {
        val user = auth.currentUser ?: return
        val uid = user.uid
        viewModelScope.launch {
            try {
                val snapshot = db.collection("users")
                    .document(uid)
                    .collection("favourites")
                    .get()
                    .await()

                val favourites = snapshot.documents.mapNotNull { doc ->
                    val movieId = doc.getLong("movieId")?.toInt() ?: return@mapNotNull null
                    Movie(
                        id = movieId,
                        title = doc.getString("title"),
                        posterPath = doc.getString("posterPath"),
                        voteAverage = doc.getDouble("voteAverage"),
                        releaseDate = doc.getString("releaseDate")
                    )
                }

                _favouriteMovies.value = favourites
                println("Loaded ${favourites.size} favourites for $uid")

            } catch (e: Exception) {
                e.printStackTrace()
                println("Error loading from Firestore: ${e.message}")
            }
        }
    }

    fun isFavourite(movie: Movie): Boolean {
        return _favouriteMovies.value.any { it.id == movie.id }
    }
}