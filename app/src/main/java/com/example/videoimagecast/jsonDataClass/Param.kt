package com.example.videoimagecast.jsonDataClass


import com.google.gson.annotations.SerializedName

data class Param(
    @SerializedName("allcaps")
    val allcaps: Boolean,
    @SerializedName("back")
    val back: Boolean,
    @SerializedName("backtextcolor")
    val backtextcolor: String,
    @SerializedName("bold")
    val bold: Boolean,
    @SerializedName("color")
    val color: String,
    @SerializedName("fontsize")
    val fontsize: Int,
    @SerializedName("italic")
    val italic: Boolean,
    @SerializedName("loop")
    val loop: Boolean,
    @SerializedName("mute")
    val mute: Boolean,

    @SerializedName("slideTime")
    val slideTime: Long

)