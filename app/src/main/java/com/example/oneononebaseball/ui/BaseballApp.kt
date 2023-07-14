package com.example.oneononebaseball.ui

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.oneononebaseball.ui.screens.BaseballViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.oneononebaseball.R
import com.example.oneononebaseball.data.FantasyGame
import com.example.oneononebaseball.ui.screens.FantasyGameDetailsScreen
import com.example.oneononebaseball.ui.screens.GameDetailsScreen
import com.example.oneononebaseball.ui.screens.GameHistoryScreen
import com.example.oneononebaseball.ui.screens.HomeScreen
import com.example.oneononebaseball.ui.screens.NewGameScreen
import com.example.oneononebaseball.ui.screens.PlayerDetailsScreen
import com.google.firebase.Timestamp


enum class BaseballScreen(@StringRes val title: Int) {
    Start(title = R.string.app_name),
    GameDetails(title = R.string.game_details),
    PlayerDetails(title = R.string.player_details),
    NewGame(title = R.string.new_game),
    GameHistory(title = R.string.game_history),
    FantasyGameDetails(title = R.string.fantasy_game_details)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseballAppBar(
    currentScreen: BaseballScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = { Text(stringResource(currentScreen.title)) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseballApp(
    baseballViewModel: BaseballViewModel = viewModel(factory = BaseballViewModel.Factory),
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
) {

    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Get the name of the current screen
    val currentScreen = BaseballScreen.valueOf(
        backStackEntry?.destination?.route ?: BaseballScreen.Start.name
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            BaseballAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() })
        }
    ) {
        innerPadding ->

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(245,245,245),
            shadowElevation = 4.dp,
        ) {
            NavHost(
                navController = navController,
                startDestination = BaseballScreen.Start.name,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(route = BaseballScreen.Start.name) {
                    HomeScreen(
                        baseballUiState = baseballViewModel.baseballUiState,
                        retryAction = baseballViewModel::getDailyBoxscoreData,
                        onGameDetailsClicked = {
                            baseballViewModel.setGameId(it)
                            baseballViewModel.getGameBoxscoreData()
                            navController.navigate(BaseballScreen.GameDetails.name)
                        },
                        onPrevDayClicked = {
                            baseballViewModel.incCurrDay(-1)
                            baseballViewModel.getDailyBoxscoreData()
                        },
                        onNextDayClicked = {
                            baseballViewModel.incCurrDay(1)
                            baseballViewModel.getDailyBoxscoreData()
                        },
                        onNewGameClicked = {
                            baseballViewModel.getNewGameData()
                            navController.navigate(BaseballScreen.NewGame.name)
                        },
                        onGameHistoryClicked = {
                            baseballViewModel.getGameHistoryData()
                            navController.navigate(BaseballScreen.GameHistory.name)
                        }
                    )
                }
                composable(route = BaseballScreen.GameDetails.name) {
                    GameDetailsScreen(
                        gameUiState = baseballViewModel.gameUiState,
                        retryAction = baseballViewModel::getGameBoxscoreData,
                        onPlayerClicked = {
                            baseballViewModel.setPlayerId(it)
                            baseballViewModel.getPlayerData()
                            navController.navigate(BaseballScreen.PlayerDetails.name)
                        }
                    )
                }
                composable(route = BaseballScreen.NewGame.name) {
                    NewGameScreen(
                        newGameUiState = baseballViewModel.newGameUiState,
                        retryAction = baseballViewModel::getNewGameData,
                        onPlayerClicked = {
                            baseballViewModel.setPlayerId(it)
                            baseballViewModel.getPlayerData()
                            navController.navigate(BaseballScreen.PlayerDetails.name)
                        },
                        onSubmitButtonClicked = { playerMatchups, randomMatchups ->
                            baseballViewModel.addData(FantasyGame(playerMatchups, randomMatchups, Timestamp.now()))
                            navController.navigateUp()
                        }
                    )
                }
                composable(route = BaseballScreen.GameHistory.name) {
                    GameHistoryScreen(
                        gameHistoryUiState = baseballViewModel.gameHistoryUiState,
                        retryAction = baseballViewModel::getGameHistoryData,
                        onGetScoreClicked = { baseballViewModel.getFantasyScore(it) },
                        onDetailsClicked = {
                            baseballViewModel.fantasyGame = it
                            navController.navigate(BaseballScreen.FantasyGameDetails.name)
                        }
                    )
                }
                composable(route = BaseballScreen.PlayerDetails.name) {
                    PlayerDetailsScreen(
                        playerDetialsUiState = baseballViewModel.playerDetailsUiState,
                        retryAction = baseballViewModel::getPlayerData
                    )
                }
                composable(route = BaseballScreen.FantasyGameDetails.name) {
                    FantasyGameDetailsScreen(
                        fantasyGame = baseballViewModel.fantasyGame,
                        onPlayerClicked = {
                            baseballViewModel.setPlayerId(it)
                            baseballViewModel.getPlayerData()
                            navController.navigate(BaseballScreen.PlayerDetails.name)
                        }
                    )
                }
            }
        }
    }
}