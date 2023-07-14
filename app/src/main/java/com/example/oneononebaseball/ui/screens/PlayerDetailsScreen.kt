package com.example.oneononebaseball.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oneononebaseball.network.HittingStats
import com.example.oneononebaseball.network.PitchingStats
import com.example.oneononebaseball.network.PlayerStats
import java.text.DecimalFormat
import java.util.Calendar

private val DECIMAL_FORMAT_3 = DecimalFormat(".000")
private val DECIMAL_FORMAT_2 = DecimalFormat("#0.00")


@Composable
fun PlayerDetailsScreen(
    playerDetialsUiState: PlayerDetialsUiState,
    retryAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (playerDetialsUiState) {
        is PlayerDetialsUiState.Loading -> LoadingScreen(modifier = modifier)
        is PlayerDetialsUiState.Success -> PlayerDetailsUi(playerDetialsUiState.playerData)
        else -> ErrorScreen(retryAction = retryAction, modifier = modifier)
    }
}

@Composable
fun PlayerDetailsUi(
    playerStats: PlayerStats,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.padding(8.dp)
    ) {
        if (playerStats.full_name != null) {
            val jersey_number = if (playerStats.jersey_number == null) "N/A" else playerStats.jersey_number.toString()
            Text(
                text = "${playerStats.full_name} | $jersey_number",
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(8.dp)
            )
        }
        if (playerStats.primary_position != null && playerStats.team != null) {
            Text(
                text = "${playerStats.primary_position}    ${playerStats.team.abbr}",
                style = TextStyle(fontSize = 18.sp),
                modifier = Modifier.padding(8.dp)
            )
        }
        if (playerStats.bat_hand != null && playerStats.throw_hand != null) {
            Text(
                text = "Throws/Bats: ${playerStats.throw_hand}/${playerStats.bat_hand}",
                style = TextStyle(fontSize = 18.sp),
                modifier = Modifier.padding(8.dp)
            )
        }
        if (playerStats.seasons != null) {
            var i = 0
            val calendar = Calendar.getInstance()
            while (i < playerStats.seasons.size &&
                (playerStats.seasons[i].type != "REG" || playerStats.seasons[i].year != calendar.get(
                    Calendar.YEAR
                ))
            )
                i++
            if (i < playerStats.seasons.size) {
                val currentSeasonStats = playerStats.seasons[i].totals.statistics
                if (playerStats.position != null) {
                    if (playerStats.position == "P") {
                        if (currentSeasonStats.pitching != null)
                            PitcherStatsUi(currentSeasonStats.pitching.overall)
                    } else {
                        if (currentSeasonStats.hitting != null)
                            HittingStatsUi(currentSeasonStats.hitting.overall)
                    }
                }
            }
        }
    }
}

@Composable
fun PitcherStatsUi(
    pitchingStats: PitchingStats,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = "W: ${pitchingStats.games.win}",
                modifier = Modifier.padding(4.dp)
            )
            Text(
                text = "ERA: ${pitchingStats.era}",
                modifier = Modifier.padding(4.dp)
            )
            Text(
                text = "K: ${pitchingStats.outs.ktotal}",
                modifier = Modifier.padding(4.dp)
            )
            Text(
                text = "K/BB: ${DECIMAL_FORMAT_2.format(pitchingStats.kbb)}",
                modifier = Modifier.padding(4.dp)
            )
        }
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = "L: ${pitchingStats.games.loss}",
                modifier = Modifier.padding(4.dp)
            )
            Text(
                text = "WHIP: ${pitchingStats.whip}",
                modifier = Modifier.padding(4.dp)
            )
            Text(
                text = "BB: ${pitchingStats.onbase.bb}",
                modifier = Modifier.padding(4.dp)
            )
            Text(
                text = "OBA: ${pitchingStats.oba}",
                modifier = Modifier.padding(4.dp)
            )
        }
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = "S: ${pitchingStats.games.save}",
                modifier = Modifier.padding(4.dp)
            )
            Text(
                text = "IP: ${pitchingStats.ip_2}",
                modifier = Modifier.padding(4.dp)
            )
            Text(
                text = "K/9: ${DECIMAL_FORMAT_2.format(pitchingStats.k9)}",
                modifier = Modifier.padding(4.dp)
            )
            Text(
                text = "SLG: ${pitchingStats.slg}",
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

@Composable
fun HittingStatsUi(
    hittingStats: HittingStats,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = "H: ${hittingStats.onbase.h}",
                modifier = Modifier.padding(4.dp)
            )
            Text(
                text = "RBI: ${hittingStats.rbi}",
                modifier = Modifier.padding(4.dp)
            )
            Text(
                text = "BB: ${hittingStats.onbase.bb}",
                modifier = Modifier.padding(4.dp)
            )
            Text(
                text = "SB: ${hittingStats.steal.stolen}",
                modifier = Modifier.padding(4.dp)
            )
        }
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = "AB: ${hittingStats.ab}",
                modifier = Modifier.padding(4.dp)
            )
            Text(
                text = "HR: ${hittingStats.onbase.hr}",
                modifier = Modifier.padding(4.dp)
            )
            Text(
                text = "K: ${hittingStats.outs.ktotal}",
                modifier = Modifier.padding(4.dp)
            )
            Text(
                text = "SLG: ${DECIMAL_FORMAT_3.format(hittingStats.slg)}",
                modifier = Modifier.padding(4.dp)
            )

        }
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = "AVG: ${DECIMAL_FORMAT_3.format(hittingStats.avg)}",
                modifier = Modifier.padding(4.dp)
            )
            Text(
                text = "OBP: ${DECIMAL_FORMAT_3.format(hittingStats.obp)}",
                modifier = Modifier.padding(4.dp)
            )
            Text(
                text = "R: ${hittingStats.runs.total}",
                modifier = Modifier.padding(4.dp)
            )
            Text(
                text = "OPS: ${DECIMAL_FORMAT_3.format(hittingStats.ops)}",
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}