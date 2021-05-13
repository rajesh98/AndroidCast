package com.example.videoimagecast.jsonDataClass


import com.google.gson.annotations.SerializedName

data class Component(
    @SerializedName("height")
    val height: Float,
    @SerializedName("id")
    val id: String,
    @SerializedName("param")
    val `param`: Param,
    @SerializedName("resource")
    val resource: String,
    @SerializedName("type")
    var type: String,
    @SerializedName("width")
    val width: Float,
    @SerializedName("x")
    val x: Float,
    @SerializedName("y")
    val y: Float
)