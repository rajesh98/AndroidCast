package com.example.videoimagecast.jsonDataClass


import com.google.gson.annotations.SerializedName

data class Screen(
    @SerializedName("components")
    val components: List<Component>,
    @SerializedName("id")
    val id: String,
    @SerializedName("backgroundColor")
    val backgroundColor: String
)