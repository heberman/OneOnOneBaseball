package com.example.oneononebaseball.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oneononebaseball.network.Game
import com.example.oneononebaseball.network.GameDetails
import com.example.oneononebaseball.network.Inning
import com.example.oneononebaseball.network.LineupEntry
import com.example.oneononebaseball.network.PlayerStats
import com.example.oneononebaseball.network.TeamStats

@Composable
fun GameDetailsScreen(
    gameUiState: GameUiState,
    retryAction: () -> Unit,
    onPlayerClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (gameUiState) {
        is GameUiState.Loading -> LoadingScreen(modifier = modifier)
        is GameUiState.Success -> CompleteGameDetails(gameUiState.gameBoxscoreData, onPlayerClicked, modifier)
        else -> ErrorScreen(retryAction = retryAction, modifier = modifier)
    }
}

@Composable
fun CompleteGameDetails(
    game: Game,
    onPlayerClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val gameDetails = game.game
    if (gameDetails.status == "closed" || gameDetails.status == "inprogress")
        GameBegunDetails(gameDetails = gameDetails, onPlayerClicked = onPlayerClicked)
    else
        ScheduledGameDetails(gameDetails = gameDetails, onPlayerClicked = onPlayerClicked)
}

@Composable
fun ScheduledGameDetails(
    gameDetails: GameDetails,
    onPlayerClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
        ){
            Text(
                text = "(${gameDetails.away.win}, ${gameDetails.away.loss})",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = "${gameDetails.away.abbr} - ${gameDetails.home.abbr}",
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
            )
            Text(
                text = "(${gameDetails.home.win}, ${gameDetails.home.loss})",
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Text(
            text = convertDate(gameDetails.scheduled),
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.padding(8.dp))
        Text(
            text = "Probables",
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)

        )
        Spacer(modifier = Modifier.padding(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            if (gameDetails.away.probable_pitcher != null)
                ProbablePitcherStats(
                    playerStats = gameDetails.away.probable_pitcher,
                    onPlayerClicked = onPlayerClicked
                )
            if (gameDetails.home.probable_pitcher != null)
                ProbablePitcherStats(
                    playerStats = gameDetails.home.probable_pitcher,
                    onPlayerClicked = onPlayerClicked
                )
        }
    }
}

@Composable
fun ProbablePitcherStats(
    playerStats: PlayerStats,
    onPlayerClicked: (String) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 16.dp)
    ){
        ClickableText(
            text = AnnotatedString("P: ${playerStats.first_name} ${playerStats.last_name}"),
            onClick = { onPlayerClicked(playerStats.id) },
            style = TextStyle(fontSize = 16.sp)
        )
        Text(
            text = "${playerStats.win} - ${playerStats.loss}, ${playerStats.era} ERA"
        )
    }
}

@Composable
fun GameBegunDetails(
    gameDetails: GameDetails,
    onPlayerClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        GameHeader(gameDetails = gameDetails)
        if (gameDetails.status == "closed")
            PitchingResults(gameDetails = gameDetails, onPlayerClicked = onPlayerClicked)
        else
            CurrentGameState(gameDetails = gameDetails, onPlayerClicked = onPlayerClicked)
        Spacer(modifier = Modifier.padding(8.dp))
        Lineups(gameDetails = gameDetails, onPlayerClicked = onPlayerClicked)
    }
}

@Composable
fun GameHeader(
    gameDetails: GameDetails
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
        ){
            Text(
                text = "(${gameDetails.away.win}, ${gameDetails.away.loss})",
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = "${gameDetails.away.abbr}  ${gameDetails.away.runs} - ${gameDetails.home.runs}  ${gameDetails.home.abbr}",
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
            )
            Text(
                text = "(${gameDetails.home.win}, ${gameDetails.home.loss})",
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        if (gameDetails.status == "closed") {
            Text(
                text = "Final / ${gameDetails.final?.inning}",
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
            )
        } else {
            val inning = when (gameDetails.outcome?.current_inning_half) {
                "T" -> "Top"
                "B" -> "Bottom"
                else -> throw IllegalArgumentException("Invalid half inning")
            }
            Text(
                text = "$inning ${gameDetails.outcome.current_inning}",
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
            )
        }
        InningBoxScore(gameDetails = gameDetails)
    }
}

@Composable
fun InningBoxScore(gameDetails: GameDetails) {
    Column(Modifier.padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(end = 16.dp)
            ){
                Text(
                    text = ""
                )
                Text(
                    text = gameDetails.away.abbr,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = gameDetails.home.abbr,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )
            }
            if (gameDetails.away.scoring != null && gameDetails.home.scoring != null) {
                var i = 0
                while (i < 9.coerceAtLeast(gameDetails.away.scoring.size)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = (i + 1).toString()
                        )
                        if (i < gameDetails.away.scoring.size)
                            Text(text = gameDetails.away.scoring[i].runs)
                        else
                            Text(text = "X")
                        if (i < gameDetails.home.scoring.size)
                            Text(text = gameDetails.home.scoring[i].runs)
                        else
                            Text(text = "X")
                    }
                    i++
                }
            }
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(start = 8.dp, end = 4.dp)
            ){
                Text(
                    text = "R"
                )
                Text(
                    text = gameDetails.away.runs.toString(),
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = gameDetails.home.runs.toString(),
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )
            }
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 4.dp)
            ){
                Text(
                    text = "H"
                )
                Text(
                    text = gameDetails.away.hits.toString(),
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = gameDetails.home.hits.toString(),
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )
            }
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 4.dp)
            ){
                Text(
                    text = "E"
                )
                Text(
                    text = gameDetails.away.errors.toString(),
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = gameDetails.home.errors.toString(),
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun CurrentGameState(
    gameDetails: GameDetails,
    onPlayerClicked: (String) -> Unit
) {
    val outcome = gameDetails.outcome

    if (outcome?.count != null && outcome.hitter != null && outcome.pitcher != null) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding((4.dp))
        ) {
            Text(
                text = "${outcome.count.balls}-${outcome.count.strikes},   ${outcome.count.outs} outs"
            )
            if (outcome.runners != null) {
                Row {
                    Text(text = "Runners on: ")
                    outcome.runners.forEach { player ->
                        if (player.ending_base != null && player.ending_base < 4)
                            Text(text = "${player.ending_base}")
                    }
                }
            }
            ClickableText(
                text = AnnotatedString("AB: ${outcome.hitter.first_name} ${outcome.hitter.last_name}"),
                onClick = { onPlayerClicked(outcome.hitter.id) },
                style = TextStyle(fontSize = 16.sp)
            )
            ClickableText(
                text = AnnotatedString("P: ${outcome.pitcher.first_name} ${outcome.pitcher.last_name}"),
                onClick = { onPlayerClicked(outcome.pitcher.id) },
                style = TextStyle(fontSize = 16.sp)
            )
        }
    }
}

@Composable
fun PitchingResults(
    gameDetails: GameDetails,
    onPlayerClicked: (String) -> Unit
) {
    val pitchingResults = gameDetails.pitching

    if (pitchingResults != null) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            ClickableText(
                text = AnnotatedString("Win: ${pitchingResults.win.first_name} ${pitchingResults.win.last_name}"),
                onClick = { onPlayerClicked(pitchingResults.win.id) },
                style = TextStyle(fontSize = 16.sp)
            )
            ClickableText(
                text = AnnotatedString("Loss: ${pitchingResults.loss.first_name} ${pitchingResults.loss.last_name}"),
                onClick = { onPlayerClicked(pitchingResults.loss.id) },
                style = TextStyle(fontSize = 16.sp)
            )
            if (pitchingResults.save != null) {
                ClickableText(
                    text = AnnotatedString("Save: ${pitchingResults.save.first_name} ${pitchingResults.save.last_name}"),
                    onClick = { onPlayerClicked(pitchingResults.save.id) },
                    style = TextStyle(fontSize = 16.sp)
                )
            }
        }
    }
}

@Composable
fun Lineups(
    gameDetails: GameDetails,
    onPlayerClicked: (String) -> Unit
) {
    var selectedAwayTeam by rememberSaveable { mutableStateOf(true) }
    val awayPlayerMap = gameDetails.away.players?.let { gameDetails.away.lineup?.let { it1 ->
        createPlayerMap(it,
            it1
        )
    } }
    val homePlayerMap = gameDetails.home.players?.let { gameDetails.home.lineup?.let { it1 ->
        createPlayerMap(it,
            it1
        )
    } }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 8.dp)) {
            ClickableText(
                text = AnnotatedString(gameDetails.away.name),
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                onClick = { selectedAwayTeam = true },
                modifier = Modifier
                    .alpha(if (!selectedAwayTeam) 0.5f else 1f)
                    .padding(horizontal = 8.dp)
            )
            ClickableText(
                text = AnnotatedString(gameDetails.home.name),
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                onClick = { selectedAwayTeam = false },
                modifier = Modifier
                    .alpha(if (selectedAwayTeam) 0.5f else 1f)
                    .padding(horizontal = 8.dp)
            )
        }
        if (selectedAwayTeam) {
            if (awayPlayerMap != null && gameDetails.away.lineup != null) {
                Lineup(
                    teamStats = gameDetails.away,
                    playerMap = awayPlayerMap,
                    lineup = gameDetails.away.lineup,
                    onPlayerClicked = onPlayerClicked
                )
            }
        } else {
            if (homePlayerMap != null && gameDetails.home.lineup != null) {
                Lineup(
                    teamStats = gameDetails.home,
                    playerMap = homePlayerMap,
                    lineup = gameDetails.home.lineup,
                    onPlayerClicked = onPlayerClicked
                )
            }
        }
    }
}

@Composable
fun Lineup(
    teamStats: TeamStats,
    playerMap: MutableMap<String, PlayerStats>,
    lineup: List<LineupEntry>,
    onPlayerClicked: (String) -> Unit
) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(
                modifier = Modifier.padding(4.dp)
            ) {
                Text(
                    text = "Batters",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                for (i in 1 until 10) {
                    val player = playerMap[lineup[i].id]
                    if (player != null) {
                        ClickableText(
                            text = AnnotatedString("${player.last_name} - ${getPositionAbbr(lineup[i].position)}"),
                            onClick = { onPlayerClicked(player.id) },
                            style = TextStyle(fontSize = 16.sp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
                Text(
                    text = "Totals",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(4.dp)
            ) {
                Text(
                    text = "H/AB",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                for (i in 1 until 10) {
                    val player = playerMap[lineup[i].id]
                    val playerAb = player?.statistics?.hitting?.overall?.ab
                    val playerHits = player?.statistics?.hitting?.overall?.onbase?.h
                    if (playerAb != null && playerHits != null)
                        Text(text = "${playerHits}/${playerAb}", modifier = Modifier.padding(vertical = 4.dp))
                }
                val totalAb = teamStats.statistics?.hitting?.overall?.ab
                val totalHits = teamStats.statistics?.hitting?.overall?.onbase?.h
                if (totalAb != null && totalHits != null)
                    Text(
                        text = "${totalHits}/${totalAb}",
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(4.dp)
            ) {
                Text(
                    text = "R",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                for (i in 1 until 10) {
                    val player = playerMap[lineup[i].id]
                    val playerRuns = player?.statistics?.hitting?.overall?.runs?.total
                    if (playerRuns != null)
                        Text(text = playerRuns.toString(), modifier = Modifier.padding(vertical = 4.dp))
                }
                val totalRuns = teamStats.statistics?.hitting?.overall?.runs?.total
                if (totalRuns != null)
                    Text(
                        text = totalRuns.toString(),
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(4.dp)
            ) {
                Text(
                    text = "HR",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                for (i in 1 until 10) {
                    val player = playerMap[lineup[i].id]
                    val playerHr = player?.statistics?.hitting?.overall?.onbase?.hr
                    if (playerHr != null)
                        Text(text = playerHr.toString(), modifier = Modifier.padding(vertical = 4.dp))
                }
                val totalHr = teamStats.statistics?.hitting?.overall?.onbase?.hr
                if (totalHr != null)
                    Text(
                        text = totalHr.toString(),
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(4.dp)
            ) {
                Text(
                    text = "TB",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                for (i in 1 until 10) {
                    val player = playerMap[lineup[i].id]
                    val playerTb = player?.statistics?.hitting?.overall?.onbase?.tb
                    if (playerTb != null)
                        Text(text = playerTb.toString(), modifier = Modifier.padding(vertical = 4.dp))
                }
                val totalTb = teamStats.statistics?.hitting?.overall?.onbase?.tb
                if (totalTb != null)
                    Text(
                        text = totalTb.toString(),
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(4.dp)
            ) {
                Text(
                    text = "RBI",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                for (i in 1 until 10) {
                    val player = playerMap[lineup[i].id]
                    val playerRbi = player?.statistics?.hitting?.overall?.rbi
                    if (playerRbi != null)
                        Text(text = playerRbi.toString(), modifier = Modifier.padding(vertical = 4.dp))
                }
                val totalRbi = teamStats.statistics?.hitting?.overall?.rbi
                if (totalRbi != null)
                    Text(
                        text = totalRbi.toString(),
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(4.dp)
            ) {
                Text(
                    text = "BB",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                for (i in 1 until 10) {
                    val player = playerMap[lineup[i].id]
                    val playerBb = player?.statistics?.hitting?.overall?.onbase?.bb
                    if (playerBb != null)
                        Text(text = playerBb.toString(), modifier = Modifier.padding(vertical = 4.dp))
                }
                val totalBb = teamStats.statistics?.hitting?.overall?.onbase?.bb
                if (totalBb != null)
                    Text(
                        text = totalBb.toString(),
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(4.dp)
            ) {
                Text(
                    text = "K",
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                for (i in 1 until 10) {
                    val player = playerMap[lineup[i].id]
                    val playerK = player?.statistics?.hitting?.overall?.outs?.ktotal
                    if (playerK != null)
                        Text(
                            text = playerK.toString(),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                }
                val totalK = teamStats.statistics?.hitting?.overall?.outs?.ktotal
                if (totalK != null)
                    Text(
                        text = totalK.toString(),
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
            }
        }
    }
}

fun createPlayerMap(
    players: List<PlayerStats>,
    lineup: List<LineupEntry>
): MutableMap<String, PlayerStats> {
    val playerMap: MutableMap<String, PlayerStats> = mutableMapOf()
    lineup.forEach { lineupEntry ->
        val playerId = lineupEntry.id
        players.forEach { player ->
            if (playerId == player.id) {
                playerMap[playerId] = player
            }
        }
    }
    return playerMap
}

fun getPositionAbbr(position: Int): String {
    return when (position) {
        1 ->  "P"
        2 -> "C"
        3 -> "1B"
        4 -> "2B"
        5 -> "3B"
        6 -> "SS"
        7 -> "LF"
        8 -> "CF"
        9 -> "RF"
        10 -> "DH"
        else -> "?"
    }
}

@Preview
@Composable
fun PreviewClosedGameDetails(){
    val fakeTeamScoring = listOf(
        Inning(1, 1, "10", "0", "0"),
        Inning(2, 2, "0", "0", "0"),
        Inning(3, 3, "0", "0", "0"),
        Inning(4, 4, "0", "0", "0"),
        Inning(5, 5, "0", "0", "0"),
        Inning(6, 6, "0", "0", "0"),
        Inning(7, 7, "0", "0", "0"),
        Inning(8, 8, "0", "0", "0"),
        Inning(9, 9, "0", "0", "0")
    )
    val fakeTeam1 = TeamStats(
        "Padres",
        "San Diego",
        "SD",
        "0",
        10,
        0,
        0,
        50,
        50,
        scoring = fakeTeamScoring
    )
    val fakeTeam2 = TeamStats(
        "Dodgers",
        "Los Angeles",
        "LA",
        "0",
        10,
        0,
        0,
        50,
        50,
        scoring = fakeTeamScoring
    )
    val fakeGameDetails = GameDetails(
        "0",
        "closed",
        "idk",
        "Dodgers",
        "Padres",
        home = fakeTeam1,
        away = fakeTeam2
    )
    val fakeGame = Game(fakeGameDetails)
    CompleteGameDetails(game = fakeGame, onPlayerClicked = {})
}