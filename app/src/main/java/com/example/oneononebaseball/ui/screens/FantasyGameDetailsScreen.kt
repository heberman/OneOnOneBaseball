package com.example.oneononebaseball.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oneononebaseball.data.FantasyGame
import com.example.oneononebaseball.data.FantasyPlayer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun FantasyGameDetailsScreen(
    fantasyGame: FantasyGame,
    onPlayerClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedUserTeam by rememberSaveable { mutableStateOf(true) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        val calendar = Calendar.getInstance()
        calendar.time = fantasyGame.timeCreated.toDate()
        val dateFormat = SimpleDateFormat("MMMM d, yyyy h:mm a", Locale.US)
        val formattedDateTime = dateFormat.format(calendar.time)
        Text(
            text = "Created: $formattedDateTime",
            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(modifier = Modifier.padding(horizontal = 8.dp)) {
            ClickableText(
                text = AnnotatedString("Your Players"),
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                onClick = { selectedUserTeam = true },
                modifier = Modifier
                    .alpha(if (!selectedUserTeam) 0.5f else 1f)
                    .padding(horizontal = 8.dp)
            )
            ClickableText(
                text = AnnotatedString("CPU Players"),
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                onClick = { selectedUserTeam = false },
                modifier = Modifier
                    .alpha(if (selectedUserTeam) 0.5f else 1f)
                    .padding(horizontal = 8.dp)
            )
        }
        val currPlayers = if (selectedUserTeam) fantasyGame.playerMatchups else fantasyGame.randomMatchups
        val currFinalScore = if (selectedUserTeam) fantasyGame.playerFinalScore else fantasyGame.randomFinalScore
        Row(
            Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PositionsColumn(fantasyGame = fantasyGame)
            PlayerNamesColumn(
                players = currPlayers,
                finalScore = currFinalScore,
                onPlayerClicked = onPlayerClicked
            )
            if (currFinalScore != null) {
                PlayerScoresColumn(
                    players = currPlayers,
                    total = currFinalScore
                )
            }
        }
    }
}

@Composable
fun PositionsColumn(
    fantasyGame: FantasyGame
) {
    Column {
        for (i in 1 until 11) {
            Text(
                text = getPositionAbbr(i),
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(4.dp)
            )
        }
        if (fantasyGame.playerFinalScore != null) {
            Text(
                text = "Totals",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

@Composable
fun PlayerNamesColumn(
    players: List<FantasyPlayer>,
    finalScore: Int?,
    onPlayerClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column {
        players.forEach { player ->
            ClickableText(
                text = AnnotatedString("${player.firstName} ${player.lastName}"),
                onClick = { onPlayerClicked(player.id) },
                style = TextStyle(fontSize = 16.sp),
                modifier = Modifier.padding(4.dp)
            )
        }
        if (finalScore != null) {
            Text(
                text = "",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

@Composable
fun PlayerScoresColumn(
    players: List<FantasyPlayer>,
    total: Int,
    modifier: Modifier = Modifier
) {
    Column {
        players.forEach { player ->
            Text(
                text = AnnotatedString(player.score.toString()),
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(4.dp)
            )
        }
        Text(
            text = total.toString(),
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(4.dp)
        )
    }
}