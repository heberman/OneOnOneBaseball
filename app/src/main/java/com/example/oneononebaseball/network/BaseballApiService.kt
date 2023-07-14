package com.example.oneononebaseball.network

import retrofit2.http.GET
import retrofit2.http.Path

private const val API_KEY = "28hj6azejf9qehrmxwepr4f3"

interface BaseballApiService {
    @GET("games/{year}/{month}/{day}/boxscore.json?api_key=$API_KEY")
    suspend fun getDailyBoxscoreData(
        @Path("year") year: String,
        @Path("month") month: String,
        @Path("day") day: String
    ): DailyBoxscoreResponseData

    @GET("games/{year}/{month}/{day}/summary.json?api_key=$API_KEY")
    suspend fun getDailySummaryData(
        @Path("year") year: String,
        @Path("month") month: String,
        @Path("day") day: String
    ): DailySummary

    @GET("games/{year}/{month}/{day}/summary.json?api_key=$API_KEY")
    suspend fun getFantasySummaryData(
        @Path("year") year: String,
        @Path("month") month: String,
        @Path("day") day: String
    ): FantasyPlayerData

    @GET("games/{gameId}/summary.json?api_key=$API_KEY")
    suspend fun getGameBoxscoreData(
        @Path("gameId") gameId: String
    ) : Game

    @GET("players/{playerId}/profile.json?api_key=$API_KEY")
    suspend fun getPlayerData(
        @Path("playerId") playerId: String
    ) : Player
}