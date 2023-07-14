package com.example.oneononebaseball.data

import android.util.Log
import com.example.oneononebaseball.network.BaseballApiService
import com.example.oneononebaseball.network.DailyBoxscoreResponseData
import com.example.oneononebaseball.network.FantasyPlayerData
import com.example.oneononebaseball.network.Game
import com.example.oneononebaseball.network.Player
import com.example.oneononebaseball.network.PlayerStats
import com.example.oneononebaseball.network.SimplifiedTeamDetails
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import java.util.Calendar

interface BaseballDataRepository {
    suspend fun getDailyBoxscoreData(calendar: Calendar): DailyBoxscoreResponseData
    suspend fun getGameBoxscoreData(gameId: String): Game
    suspend fun getPlayerDetailsData(playerId: String): Player
    suspend fun getNewGameData(): List<Matchup>?
    suspend fun getPlayerFantasyData(timeCreated: Timestamp): FantasyPlayerData
}

class NetworkBaseballDataRepository(
    private val baseballApiService: BaseballApiService
) : BaseballDataRepository {
    override suspend fun getDailyBoxscoreData(calendar: Calendar): DailyBoxscoreResponseData {
        val currentYear = calendar.get(Calendar.YEAR).toString()
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val formattedMonth = String.format("%02d", currentMonth)
        val formattedDay = String.format("%02d", currentDay)
        return baseballApiService.getDailyBoxscoreData(currentYear, formattedMonth, formattedDay)
    }

    override suspend fun getGameBoxscoreData(gameId: String) : Game {
        return baseballApiService.getGameBoxscoreData(gameId)
    }

    override suspend fun getPlayerDetailsData(playerId: String): Player {
        return baseballApiService.getPlayerData(playerId)
    }

    override suspend fun getPlayerFantasyData(timeCreated: Timestamp): FantasyPlayerData {
        val calendar = Calendar.getInstance()
        calendar.time = timeCreated.toDate()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val currentYear = calendar.get(Calendar.YEAR).toString()
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val formattedMonth = String.format("%02d", currentMonth)
        val formattedDay = String.format("%02d", currentDay)
        return baseballApiService.getFantasySummaryData(currentYear, formattedMonth, formattedDay)
    }

    override suspend fun getNewGameData(): List<Matchup>? {
        Log.d("getNewGameData", "Getting summary")
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val currentYear = calendar.get(Calendar.YEAR).toString()
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val formattedMonth = String.format("%02d", currentMonth)
        val formattedDay = String.format("%02d", currentDay)
        val currDayBoxscoreData = baseballApiService.getDailySummaryData(currentYear, formattedMonth, formattedDay)
        delay(1500)

        Log.d("getNewGameData", "Getting next day data")
        calendar.add(Calendar.DAY_OF_YEAR, 2)
        val newYear = calendar.get(Calendar.YEAR).toString()
        val newMonth = calendar.get(Calendar.MONTH) + 1
        val newDay = calendar.get(Calendar.DAY_OF_MONTH)
        val formattedNewMonth = String.format("%02d", newMonth)
        val formattedNewDay = String.format("%02d", newDay)
        val nextDayBoxscoreData = baseballApiService.getDailyBoxscoreData(newYear, formattedNewMonth, formattedNewDay)

        val currDayGames = currDayBoxscoreData.league.games
        val nextDayGames = nextDayBoxscoreData.league.games

        Log.d("getNewGameData", "Getting valid teams")
        // create list of teams that will play tomorrow that also played today
        val validTeams = mutableListOf<SimplifiedTeamDetails>()
        currDayGames.forEach {game ->
            val homeTeam = game.game.home_team
            nextDayGames.forEach {nextDayGame ->
                if (homeTeam == nextDayGame.game.home_team) {
                    val validTeam = game.game.home
                    validTeam.probable_pitcher = nextDayGame.game.home.probable_pitcher
                    validTeams.add(game.game.home)
                } else if (homeTeam == nextDayGame.game.away_team) {
                    val validTeam = game.game.home
                    validTeam.probable_pitcher = nextDayGame.game.away.probable_pitcher
                    validTeams.add(game.game.home)
                }
            }
            val awayTeam = game.game.away_team
            nextDayGames.forEach {nextDayGame ->
                if (awayTeam == nextDayGame.game.home_team) {
                    val validTeam = game.game.away
                    validTeam.probable_pitcher = nextDayGame.game.home.probable_pitcher
                    validTeams.add(game.game.away)
                } else if (awayTeam == nextDayGame.game.away_team) {
                    val validTeam = game.game.away
                    validTeam.probable_pitcher = nextDayGame.game.away.probable_pitcher
                    validTeams.add(game.game.away)
                }
            }
        }
        if (validTeams.size < 2)
            return null

        Log.d("getNewGameData", "Creating matchups")
        // create matchup for probable pitchers tomorrow

        // create matchups for each position
        val matchups = mutableListOf<Matchup>()
        for (i in 1 until 11) {
            var teams = chooseTwoRandomElements(validTeams)
            if (i == 1) {
                while (teams.first.probable_pitcher == null || teams.second.probable_pitcher == null)
                    teams = chooseTwoRandomElements(validTeams)
                matchups.add(Matchup(1, teams.first.probable_pitcher!!, teams.second.probable_pitcher!!))
            } else {
                while (teams.first.lineup == null || teams.first.roster == null
                    || teams.second.lineup == null || teams.second.roster == null)
                    teams = chooseTwoRandomElements(validTeams)
                val firstTeam = teams.first
                val secondTeam = teams.second
                var j = 0
                while (firstTeam.lineup!![j].position != i)
                    j++
                val firstPlayerId = firstTeam.lineup[j].id
                j = 0
                while (firstTeam.roster!![j].id != firstPlayerId)
                    j++
                val firstPlayer = firstTeam.roster[j]
                var k = 0
                while (secondTeam.lineup!![k].position != i)
                    k++
                val secondPlayerId = secondTeam.lineup[k].id
                k = 0
                while (secondTeam.roster!![k].id != secondPlayerId)
                    k++
                val secondPlayer = secondTeam.roster[k]
                matchups.add(Matchup(i, firstPlayer, secondPlayer))
            }
        }
        Log.d("getNewGameData", "Completed creating matchups")
        return matchups
    }
}

fun <T> chooseTwoRandomElements(list: List<T>): Pair<T, T> {
    val randomIndices = mutableListOf<Int>()

    while (randomIndices.size < 2) {
        val randomIndex = (0 until list.size).random()
        if (randomIndex !in randomIndices) {
            randomIndices.add(randomIndex)
        }
    }

    val firstElement = list[randomIndices[0]]
    val secondElement = list[randomIndices[1]]

    return Pair(firstElement, secondElement)
}