package com.example.oneononebaseball.data
import com.google.firebase.Timestamp


data class FantasyGame(
    val playerMatchups: List<FantasyPlayer> = listOf(),
    val randomMatchups: List<FantasyPlayer> = listOf(),
    val timeCreated: Timestamp = Timestamp.now(),
    val playerFinalScore: Int? = null,
    val randomFinalScore: Int? = null
)

data class FantasyPlayer(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val position: Int = 0,
    val score: Int = 0
)
