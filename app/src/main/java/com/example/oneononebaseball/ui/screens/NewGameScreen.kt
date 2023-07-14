package com.example.oneononebaseball.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oneononebaseball.data.FantasyPlayer
import com.example.oneononebaseball.data.Matchup

@Composable
fun NewGameScreen(
    newGameUiState: NewGameUiState,
    retryAction: () -> Unit,
    onPlayerClicked: (String) -> Unit,
    onSubmitButtonClicked: (List<FantasyPlayer>, List<FantasyPlayer>) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (newGameUiState) {
        is NewGameUiState.Loading -> LoadingScreen(modifier = modifier)
        is NewGameUiState.Success -> NewGameUi(newGameUiState.matchups, onPlayerClicked, onSubmitButtonClicked)
        else -> ErrorScreen(retryAction = retryAction, modifier = modifier)
    }
}

@Composable
fun NewGameUi(
    matchups: List<Matchup>?,
    onPlayerClicked: (String) -> Unit,
    onSubmitButtonClicked: (List<FantasyPlayer>, List<FantasyPlayer>) -> Unit
) {
    if (matchups != null) {
        val defaultSelections = mutableListOf<FantasyPlayer>()
        matchups.forEach { matchup ->
            defaultSelections.add(FantasyPlayer(
                matchup.firstPlayer.id,
                matchup.firstPlayer.first_name,
                matchup.firstPlayer.last_name,
                matchup.position
            ))
        }

        val selections = remember { mutableStateListOf(*defaultSelections.toTypedArray()) }

        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Choose a player for each position:",
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(10.dp)
            )
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(Modifier.padding(8.dp)) {
                    items(matchups) { matchup ->
                        MatchupCard(
                            matchup,
                            onPlayerClicked,
                            onCheckedChange = { checked ->
                                val index = matchups.indexOf(matchup)
                                if (checked) {
                                    selections[index] = FantasyPlayer(
                                        matchup.secondPlayer.id,
                                        matchup.secondPlayer.first_name,
                                        matchup.secondPlayer.last_name,
                                        matchup.position
                                    )
                                }
                                else {
                                    selections[index] = FantasyPlayer(
                                        matchup.firstPlayer.id,
                                        matchup.firstPlayer.first_name,
                                        matchup.firstPlayer.last_name,
                                        matchup.position
                                    )
                                }
                            }
                        )
                    }
                }
            }
            OutlinedButton(
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.LightGray
                ),
                onClick = {
                    val randomSelections = makeRandomSelections(matchups)
                    onSubmitButtonClicked(selections, randomSelections)
                },
                modifier = Modifier
                    .padding(16.dp)
                    .size(140.dp, 80.dp)
            ) {
                Text(
                    text = "Submit",
                    style = TextStyle(fontSize = 24.sp)
                )
            }
        }
    }
}

@Composable
fun MatchupCard(
    matchup: Matchup,
    onPlayerClicked: (String) -> Unit,
    onCheckedChange: (Boolean) -> Unit
) {
    var selection by remember { mutableStateOf(matchup.firstPlayer.id) }

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = getPositionAbbr(matchup.position),
            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        ClickableText(
            text = AnnotatedString("${matchup.firstPlayer.first_name} ${matchup.firstPlayer.last_name}"),
            onClick = { onPlayerClicked(matchup.firstPlayer.id) },
            style = TextStyle(fontSize = 16.sp)
        )
        Switch(
            checked = selection == matchup.secondPlayer.id,
            onCheckedChange = { checked ->
                selection = if (checked)
                    matchup.secondPlayer.id
                else
                    matchup.firstPlayer.id
                onCheckedChange(checked) },
            thumbContent = {
                Icon(
                    imageVector = if (selection == matchup.secondPlayer.id) Icons.Filled.ArrowForward else Icons.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                )
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(27, 161, 226),
                checkedIconColor = Color(27, 161, 226),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(27, 161, 226),
                uncheckedIconColor = Color(27, 161, 226),
                uncheckedBorderColor = Color.Transparent
            ),
            modifier = Modifier
                .padding(horizontal = 2.dp)
                .scale(0.8f, 0.8f)
        )
        ClickableText(
            text = AnnotatedString("${matchup.secondPlayer.first_name} ${matchup.secondPlayer.last_name}"),
            onClick = { onPlayerClicked(matchup.secondPlayer.id) },
            style = TextStyle(fontSize = 16.sp)
        )
    }
}

fun makeRandomSelections(matchups: List<Matchup>?): List<FantasyPlayer> {
    val randomPicks = mutableListOf<FantasyPlayer>()
    matchups?.forEach { matchup ->
        val randomIndex = (0 until 2).random()
        if (randomIndex == 0) {
            randomPicks.add(
                FantasyPlayer(
                    matchup.firstPlayer.id,
                    matchup.firstPlayer.first_name,
                    matchup.firstPlayer.last_name,
                    matchup.position
                )
            )
        }
        else {
            randomPicks.add(FantasyPlayer(
                matchup.secondPlayer.id,
                matchup.secondPlayer.first_name,
                matchup.secondPlayer.last_name,
                matchup.position
            ))
        }
    }
    return randomPicks
}