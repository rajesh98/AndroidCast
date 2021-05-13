package com.example.videoimagecast

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit


interface HeartBeatAPI {

    @GET("{deviceID}")
    fun checkHeartBeat(@Path("deviceID") deviceID:String?) : Call<ResponseBody>

    companion object {
        operator fun invoke(): HeartBeatAPI {
            var okHttpClient: OkHttpClient? = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()

            val gson = GsonBuilder()
                .setLenient()
                .create()

            return Retrofit.Builder()
                .baseUrl("http://15.207.86.115:8085/digisign/heartbeat/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
                .create(HeartBeatAPI::class.java)
        }
    }
}