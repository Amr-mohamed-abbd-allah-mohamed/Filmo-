package com.example.filmo.data.model

import com.google.gson.annotations.SerializedName


data class MovieResponse(
    val results: List<Movie>

)
data class MovieVideosResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("results")
    val results: List<Video>
)