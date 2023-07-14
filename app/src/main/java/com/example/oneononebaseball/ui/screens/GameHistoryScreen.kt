package com.example.oneononebaseball.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oneononebaseball.data.FantasyGame
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun GameHistoryScreen(
    gameHistoryUiState: GameHistoryUiState,
    retryAction: () -> Unit,
    onGetScoreClicked: (Pair<String, FantasyGame>) -> Unit,
    onDetailsClicked: (FantasyGame) -> Unit,
    modifier: Modifier = Modifier
) {
    when (gameHistoryUiState) {
        is GameHistoryUiState.Loading -> LoadingScreen(modifier = modifier)
        is GameHistoryUiState.Success -> FantasyGamesColumn(fantasyGames = gameHistoryUiState.fantasyGames, onGetScoreClicked, onDetailsClicked)
        else -> ErrorScreen(retryAction = retryAction, modifier = modifier)
    }
}

@Composable
fun FantasyGamesColumn(
    fantasyGames: List<Pair<String, FantasyGame>>,
    onGetScoreClicked: (Pair<String, FantasyGame>) -> Unit,
    onDetailsClicked: (FantasyGame) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp)
    ) {
        items(fantasyGames) { game ->
            FantasyGameCard(
                fantasyGame = game,
                onGetScoreClicked = onGetScoreClicked,
                onDetailsClicked = onDetailsClicked
            )
        }
    }
}

@Composable
fun FantasyGameCard(
    fantasyGame: Pair<String, FantasyGame>,
    onGetScoreClicked: (Pair<String, FantasyGame>) -> Unit,
    onDetailsClicked: (FantasyGame) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier.padding(8.dp)
    ) {
        Column(
            modifier = modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "One on One Fantasy Game",
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(2.dp)
            )
            val calendar = Calendar.getInstance()
            calendar.time = fantasyGame.second.timeCreated.toDate()
            val dateFormat = SimpleDateFormat("MMMM d, yyyy h:mm a", Locale.US)
            val formattedDateTime = dateFormat.format(calendar.time)
            Text(
                text = "Created: $formattedDateTime",
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(4.dp)
            )
            val gameCompleted = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) - calendar.get(Calendar.DAY_OF_YEAR) > 1
            val scoreCalculated = fantasyGame.second.playerFinalScore != null && fantasyGame.second.randomFinalScore != null
            if (gameCompleted) {
                if (scoreCalculated) {
                    val result =
                        fantasyGame.second.playerFinalScore!! - fantasyGame.second.randomFinalScore!!
                    val resultStr = if (result > 0)
                        "W"
                    else if (result < 0)
                        "L"
                    else
                        "T"
                    Text(
                        text = "$resultStr ${fantasyGame.second.playerFinalScore}-${fantasyGame.second.randomFinalScore}",
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    OutlinedButton(
                        onClick = { onGetScoreClicked(fantasyGame) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = "Get Result"
                        )
                    }
                }
            } else {
                Text(
                    text = "In Progress...",
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(8.dp)
                )
            }
            if (!gameCompleted || scoreCalculated) {
                OutlinedButton(
                    onClick = { onDetailsClicked(fantasyGame.second) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Game Details"
                    )
                }
            }
        }
    }
}