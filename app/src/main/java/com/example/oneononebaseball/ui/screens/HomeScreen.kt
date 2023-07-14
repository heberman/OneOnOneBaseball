package com.example.oneononebaseball.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oneononebaseball.R
import com.example.oneononebaseball.network.DailyBoxscoreResponseData
import com.example.oneononebaseball.network.Game
import com.example.oneononebaseball.network.GameDetails
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeScreen(
    baseballUiState: BaseballUiState,
    retryAction: () -> Unit,
    onGameDetailsClicked: (String) -> Unit,
    onPrevDayClicked: () -> Unit,
    onNextDayClicked: () -> Unit,
    onNewGameClicked: () -> Unit,
    onGameHistoryClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (baseballUiState) {
        is BaseballUiState.Loading -> LoadingScreen(modifier = modifier)
        is BaseballUiState.Success ->
            GamesColumn(
                baseballUiState.dailyBoxscoreData,
                onGameDetailsClicked,
                onPrevDayClicked,
                onNextDayClicked,
                onNewGameClicked,
                onGameHistoryClicked,
                modifier)
        else -> ErrorScreen(retryAction = retryAction, modifier = modifier)
    }
}

@Composable
fun GamesColumn(
    dailyBoxscoreData: DailyBoxscoreResponseData,
    onGameDetailsClicked: (String) -> Unit,
    onPrevDayClicked: () -> Unit,
    onNextDayClicked: () -> Unit,
    onNewGameClicked: () -> Unit,
    onGameHistoryClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currDay by rememberSaveable { mutableStateOf(Calendar.getInstance()) }
    val sortedGames = sortGamesByScheduledTime(dailyBoxscoreData.league.games)

    Column {
        Box(Modifier.weight(1f)) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp)
            ) {
                items(sortedGames) { game ->
                    GameCard(
                        modifier = modifier.padding(8.dp),
                        gameDetails = game.game,
                        onGameDetailsClicked = onGameDetailsClicked
                    )
                }
            }
        }
        BottomAppBar (
            modifier = Modifier.height(172.dp),
            containerColor = Color(106,90,205),
            contentColor = Color.LightGray
        ){
            Column(Modifier.padding(8.dp)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    OutlinedButton(
                        onClick = {
                            currDay.add(Calendar.DAY_OF_YEAR, -1)
                            onPrevDayClicked()
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.LightGray
                        ),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = "Prev"
                        )
                    }
                    Text(
                        text = "${currDay.get(Calendar.MONTH) + 1}/${currDay.get(Calendar.DAY_OF_MONTH)}",
                        style = TextStyle(fontSize = 20.sp)
                    )
                    OutlinedButton(
                        onClick = {
                            currDay.add(Calendar.DAY_OF_YEAR, 1)
                            onNextDayClicked()
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.LightGray
                        ),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = "Next"
                        )
                    }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    OutlinedButton(
                        onClick = onNewGameClicked,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.LightGray
                        ),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.new_game)
                        )
                    }
                    OutlinedButton(
                        onClick = onGameHistoryClicked,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.LightGray
                        ),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.game_history)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameCard(
    modifier: Modifier = Modifier,
    gameDetails: GameDetails,
    onGameDetailsClicked: (String) -> Unit
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            when (gameDetails.status) {
                "inprogress" -> InProgressGameCard(gameDetails = gameDetails)
                "closed" -> FinalGameCard(gameDetails = gameDetails)
                else -> ScheduledGameCard(gameDetails = gameDetails)
            }
            OutlinedButton(
                onClick = { onGameDetailsClicked(gameDetails.id) },
                shape = RoundedCornerShape(8.dp),
                modifier = modifier
            ) {
                Text(
                    text = stringResource(R.string.game_details)
                )
            }
        }
    }
}

@Composable
fun InProgressGameCard(
    gameDetails: GameDetails
) {
    val inning = when (gameDetails.outcome?.current_inning_half) {
        "T" -> "Top"
        "B" -> "Bottom"
        else -> throw IllegalArgumentException("Invalid half inning")
    }
    Text(
        text = "${gameDetails.away.abbr}  ${gameDetails.away.runs} - ${gameDetails.home.runs}  ${gameDetails.home.abbr}",
        style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
    )
    Text(
        text = "$inning ${gameDetails.outcome.current_inning}",
        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
    )
}

@Composable
fun ScheduledGameCard(
    gameDetails: GameDetails
) {
    Text(
        text = "${gameDetails.away.abbr} - ${gameDetails.home.abbr}",
        style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
    )
    Text(
        text = convertDate(gameDetails.scheduled),
        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
    )
}

@Composable
fun FinalGameCard(
    gameDetails: GameDetails
) {
    Text(
        text = "${gameDetails.away.abbr}  ${gameDetails.away.runs} - ${gameDetails.home.runs}  ${gameDetails.home.abbr}",
        style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
    )
    Text(
        text = "Final / ${gameDetails.final?.inning}",
        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
    )
}

fun convertDate(dateString: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
    val outputFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    val date = inputFormat.parse(dateString)
    return outputFormat.format(date)
}

fun sortGamesByScheduledTime(games: List<Game>): List<Game> {
    return games.sortedBy { game ->
        // Convert the scheduled string to a comparable value
        game.game.scheduled
    }
}