package com.example.oneononebaseball.data

import com.example.oneononebaseball.network.PlayerStats

data class Matchup(
    val position: Int,
    val firstPlayer: PlayerStats,
    val secondPlayer: PlayerStats
)
