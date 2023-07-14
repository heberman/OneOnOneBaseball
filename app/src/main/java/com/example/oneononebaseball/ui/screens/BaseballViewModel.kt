package com.example.oneononebaseball.ui.screens

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.oneononebaseball.BaseballApplication
import com.example.oneononebaseball.data.BaseballDataRepository
import com.example.oneononebaseball.data.FantasyGame
import com.example.oneononebaseball.data.FantasyPlayer
import com.example.oneononebaseball.data.Matchup
import com.example.oneononebaseball.network.DailyBoxscoreResponseData
import com.example.oneononebaseball.network.FantasyPlayerData
import com.example.oneononebaseball.network.FantasyPlayerStats
import com.example.oneononebaseball.network.Game
import com.example.oneononebaseball.network.HittingStats
import com.example.oneononebaseball.network.PitchingStats
import com.example.oneononebaseball.network.PlayerStats
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.util.Calendar
import kotlin.math.ceil

sealed interface BaseballUiState {
    data class Success(val dailyBoxscoreData: DailyBoxscoreResponseData) : BaseballUiState
    object Error : BaseballUiState
    object Loading : BaseballUiState
}

sealed interface GameUiState {
    data class Success(val gameBoxscoreData: Game) : GameUiState
    object Error : GameUiState
    object Loading : GameUiState
}

sealed interface NewGameUiState {
    data class Success(val matchups: List<Matchup>?) : NewGameUiState
    object Error : NewGameUiState
    object Loading : NewGameUiState
}

sealed interface GameHistoryUiState {
    data class Success(val fantasyGames: List<Pair<String, FantasyGame>>) : GameHistoryUiState
    object Error : GameHistoryUiState
    object Loading : GameHistoryUiState
}

sealed interface PlayerDetialsUiState {
    data class Success(val playerData: PlayerStats) : PlayerDetialsUiState
    object Error : PlayerDetialsUiState
    object Loading : PlayerDetialsUiState
}

class BaseballViewModel(private val baseballDataRepository: BaseballDataRepository) : ViewModel() {
    /** The mutable State that stores the status of the most recent request */
    var baseballUiState: BaseballUiState by mutableStateOf(BaseballUiState.Loading)
        private set

    var gameUiState: GameUiState by mutableStateOf(GameUiState.Loading)
        private set

    var newGameUiState: NewGameUiState by mutableStateOf(NewGameUiState.Loading)
        private set

    var gameHistoryUiState: GameHistoryUiState by mutableStateOf(GameHistoryUiState.Loading)
        private set

    var playerDetailsUiState: PlayerDetialsUiState by mutableStateOf(PlayerDetialsUiState.Loading)
        private set

    var fantasyGame: FantasyGame by mutableStateOf(FantasyGame())

    private val _gameId = MutableStateFlow("")
    private val gameId: StateFlow<String> = _gameId.asStateFlow()

    private val _playerId = MutableStateFlow("")
    private val playerId: StateFlow<String> = _playerId.asStateFlow()

    private val _currDay = MutableStateFlow(Calendar.getInstance())
    private val currDay: StateFlow<Calendar> = _currDay.asStateFlow()

    init {
        getDailyBoxscoreData()
    }

    fun getDailyBoxscoreData(day: Calendar = currDay.value) {
        viewModelScope.launch {
            baseballUiState = BaseballUiState.Loading
            baseballUiState = try {
                BaseballUiState.Success(baseballDataRepository.getDailyBoxscoreData(day))
            } catch (e: IOException) {
                BaseballUiState.Error
            }
        }
    }

    fun getGameBoxscoreData() {
        viewModelScope.launch {
            gameUiState = GameUiState.Loading
            gameUiState = try {
                GameUiState.Success(baseballDataRepository.getGameBoxscoreData(gameId.value))
            } catch (e: IOException) {
                GameUiState.Error
            }
        }
    }

    fun getPlayerData() {
        viewModelScope.launch {
            playerDetailsUiState = PlayerDetialsUiState.Loading
            playerDetailsUiState = try {
                PlayerDetialsUiState.Success(baseballDataRepository.getPlayerDetailsData(playerId.value).player)
            } catch (e: IOException) {
                PlayerDetialsUiState.Error
            }
        }
    }

    fun getNewGameData() {
        viewModelScope.launch {
            newGameUiState = NewGameUiState.Loading
            newGameUiState = try {
                NewGameUiState.Success(baseballDataRepository.getNewGameData())
            } catch (e: IOException) {
                NewGameUiState.Error
            }
        }
    }

    fun getFantasyScore(fantasyPair: Pair<String, FantasyGame>) {
        viewModelScope.launch {
            gameHistoryUiState = GameHistoryUiState.Loading
            gameHistoryUiState = try {
                val docId = fantasyPair.first
                val fantasyGame = fantasyPair.second
                Log.d("getFantasyScore", "Getting fantasy data...")
                val fantasyPlayerData =
                    baseballDataRepository.getPlayerFantasyData(fantasyGame.timeCreated)
                Log.d("getFantasyScore", "Successfully fetched fantasy data")
                val playerScores = calculateFantasyScores(fantasyPlayerData, fantasyGame.playerMatchups)
                val randomScores = calculateFantasyScores(fantasyPlayerData, fantasyGame.randomMatchups)
                val playerFinalScore = calculateTotalScore(playerScores)
                val randomFinalScore = calculateTotalScore(randomScores)
                val newFantasyGame = FantasyGame(
                    playerMatchups = playerScores,
                    randomMatchups = randomScores,
                    timeCreated = fantasyGame.timeCreated,
                    playerFinalScore = playerFinalScore,
                    randomFinalScore = randomFinalScore
                )
                Log.d("getFantasyScore", "Updating fantasy game...")
                updateDataInFirestore(docId, newFantasyGame)
                Log.d("getFantasyScore", "Game updated")
                GameHistoryUiState.Success(getDataFromFireStore())
            } catch (e: IOException) {
                GameHistoryUiState.Error
            }
        }
    }

    fun getGameHistoryData() {
        viewModelScope.launch {
            gameHistoryUiState = try {
                GameHistoryUiState.Success(getDataFromFireStore())
            } catch (e: IOException) {
                GameHistoryUiState.Error
            }
        }
    }

    fun addData(fantasyGame: FantasyGame) {
        viewModelScope.launch {
            addDataToFireStore(fantasyGame)
        }
    }

    private suspend fun getDataFromFireStore(): List<Pair<String, FantasyGame>> {
        val db = FirebaseFirestore.getInstance()
        val result = mutableListOf<Pair<String, FantasyGame>>()

        try {
            val querySnapshot = db.collection("fantasy").get().await()
            for (documentSnapshot in querySnapshot) {
                val documentId = documentSnapshot.id
                val fantasyGame = documentSnapshot.toObject(FantasyGame::class.java)
                result.add(Pair(documentId, fantasyGame))
            }
        } catch (e: FirebaseFirestoreException) {
            Log.d("error", "getDataFromFirestore: $e")
        }

        return result
    }

    fun addDataToFireStore(fantasyGame: FantasyGame)
    {
        val db = FirebaseFirestore.getInstance()

        val gameToAdd = hashMapOf(
            "playerMatchups" to fantasyGame.playerMatchups.map { fantasyPlayer ->
                mapOf(
                    "id" to fantasyPlayer.id,
                    "firstName" to fantasyPlayer.firstName,
                    "lastName" to fantasyPlayer.lastName,
                    "position" to fantasyPlayer.position,
                    "score" to 0
                )
            },
            "randomMatchups" to fantasyGame.randomMatchups.map { fantasyPlayer ->
                mapOf(
                    "id" to fantasyPlayer.id,
                    "firstName" to fantasyPlayer.firstName,
                    "lastName" to fantasyPlayer.lastName,
                    "position" to fantasyPlayer.position,
                    "score" to 0
                )
            },
            "timeCreated" to fantasyGame.timeCreated,
            "playerFinalScore" to null,
            "randomFinalScore" to null
        )
        // Add a new document with a generated ID
        db.collection("fantasy")
            .add(gameToAdd)
            .addOnSuccessListener { documentReference ->
                Log.d("success", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("error", "Error adding document", e)
            }
    }

    fun updateDataInFirestore(documentId: String, updatedData: FantasyGame) {
        val db = FirebaseFirestore.getInstance()

        val gameToUpdate = hashMapOf(
            "playerMatchups" to updatedData.playerMatchups,
            "randomMatchups" to updatedData.randomMatchups,
            "timeCreated" to updatedData.timeCreated,
            "playerFinalScore" to updatedData.playerFinalScore,
            "randomFinalScore" to updatedData.randomFinalScore
        )

        db.collection("fantasy")
            .document(documentId)
            .update(gameToUpdate)
            .addOnSuccessListener {
                Log.d("success", "DocumentSnapshot updated with ID: $documentId")
            }
            .addOnFailureListener { e ->
                Log.w("error", "Error updating document", e)
            }
    }

    fun setGameId(id: String) {
        _gameId.update { id }
    }

    fun setPlayerId(id: String) {
        _playerId.update { id }
    }

    fun incCurrDay(n: Int) {
        _currDay.update {
            val calendar = currDay.value.clone() as Calendar
            calendar.add(Calendar.DAY_OF_YEAR, n)
            calendar
        }
    }

    private fun calculateFantasyScores(
        fantasyPlayerData: FantasyPlayerData,
        selections: List<FantasyPlayer>
    ): List<FantasyPlayer> {
        val games = fantasyPlayerData.league.games

        // first get list of FantasyPlayerStats that were selected
        val selectedPlayers = mutableListOf<FantasyPlayerStats>()
        selections.forEach { fantasyPlayer ->
            var playerFound = false
            games.forEach { game ->
                game.game.away.players?.forEach {player ->
                    if (player.id == fantasyPlayer.id) {
                        selectedPlayers.add(player)
                        playerFound = true
                    }
                }
                game.game.home.players?.forEach {player ->
                    if (player.id == fantasyPlayer.id) {
                        selectedPlayers.add(player)
                        playerFound = true
                    }
                }
            }
            if (!playerFound) {
                selectedPlayers.add(
                    FantasyPlayerStats(
                        fantasyPlayer.id,
                        fantasyPlayer.firstName,
                        fantasyPlayer.lastName
                    )
                )
            }
        }

        // now calculate each players score and add it to total
        val newPlayers = mutableListOf<FantasyPlayer>()
        for (i in 1 until selectedPlayers.size + 1) {
            val selectedPlayer = selectedPlayers[i-1]
            if (i == 1) {
                newPlayers.add(FantasyPlayer(
                    selectedPlayer.id,
                    selectedPlayer.first_name,
                    selectedPlayer.last_name,
                    i,
                    pitcherFantasyScore(selectedPlayer.statistics?.pitching?.overall)
                ))
            }
            else {
                newPlayers.add(FantasyPlayer(
                    selectedPlayer.id,
                    selectedPlayer.first_name,
                    selectedPlayer.last_name,
                    i,
                    hitterFantasyScore(selectedPlayer.statistics?.hitting?.overall)
                ))
            }
        }

        return newPlayers
    }

    private fun calculateTotalScore(players: List<FantasyPlayer>) : Int {
        var totalScore = 0
        players.forEach { player ->
            totalScore += player.score
        }
        return totalScore
    }

    private fun pitcherFantasyScore(pitchingStats: PitchingStats?): Int {
        if (pitchingStats == null)
            return 0
        var score = 0
        if (pitchingStats.runs.earned != null)
            score -= pitchingStats.runs.earned * 3
        score += ceil(pitchingStats.ip_2).toInt() * 3
        score -= pitchingStats.onbase.h
        score -= pitchingStats.onbase.bb
        score += pitchingStats.outs.ktotal
        score += pitchingStats.games.win * 4
        return score
    }

    private fun hitterFantasyScore(hittingStats: HittingStats?): Int {
        if (hittingStats == null)
            return 0
        var score = 0
        score += hittingStats.onbase.h * 2
        score += hittingStats.onbase.bb * 2
        score += hittingStats.rbi * 2
        score += hittingStats.runs.total * 2
        score += hittingStats.onbase.hr * 6
        score += hittingStats.steal.stolen * 4
        return score
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as BaseballApplication)
                val baseballDataRepository = application.container.baseballDataRepository
                BaseballViewModel(baseballDataRepository = baseballDataRepository)
            }
        }
    }

}