package com.example.filmo.data.model

import com.google.gson.annotations.SerializedName

data class Movie(
    @SerializedName("id")
    val id: Int,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("overview")
    val overview: String? = null,

    @SerializedName("poster_path")
    val posterPath: String? = null,

    @SerializedName("backdrop_path")
    val backdropPath: String? = null,

    @SerializedName("release_date")
    val releaseDate: String? = null,

    @SerializedName("vote_average")
    val voteAverage: Double? = null,

    @SerializedName("vote_count")
    val voteCount: Int? = null,

    @SerializedName("runtime")
    val runtime: Int? = null,

    @SerializedName("genres")
    val genres: List<Genre>? = null,
    val credits: Credits? = null
)

data class Genre(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String
)

data class Credits(
    @SerializedName("cast")
    val cast: List<Cast>? = null
)

data class Cast(
    @SerializedName("name")
    val name: String?,

    @SerializedName("character")
    val character: String?,

    @SerializedName("profile_path")
    val profilePath: String?
)

data class Video(
    val key: String?,
    val site: String?,
    val type: String?
)
