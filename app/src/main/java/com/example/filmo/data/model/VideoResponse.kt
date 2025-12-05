package com.example.filmo.data.model

import com.google.gson.annotations.SerializedName

data class VideoResponse(
    @SerializedName("results") val results: List<VideoItem>
)