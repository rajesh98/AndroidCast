package com.example.videoimagecast.jsonDataClass


import com.google.gson.annotations.SerializedName

data class jsonDataClass(
    @SerializedName("resolution")
    val resolution: String,
    @SerializedName("screenTimer")
    val screenTimer: Long,
    @SerializedName("screens")
    val screens: List<Screen>,
    @SerializedName("version")
    val version: String
)