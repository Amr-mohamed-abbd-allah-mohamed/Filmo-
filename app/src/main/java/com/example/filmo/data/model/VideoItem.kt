package com.example.filmo.data.model

import com.google.gson.annotations.SerializedName

data class VideoItem(
    @SerializedName("key") val key: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("site") val site: String?
)