package com.example.oneononebaseball.data

import com.example.oneononebaseball.network.BaseballApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

interface AppContainer {
    val baseballDataRepository: BaseballDataRepository
}

class DefaultAppContainer : AppContainer {
    private val BASE_URL =
        "https://api.sportradar.us/mlb/trial/v7/en/"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(BASE_URL)
        .build()

    private val retrofitService : BaseballApiService by lazy {
        retrofit.create(BaseballApiService::class.java)
    }

    override val baseballDataRepository: BaseballDataRepository by lazy {
        NetworkBaseballDataRepository(retrofitService)
    }
}