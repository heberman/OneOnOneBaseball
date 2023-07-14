package com.example.oneononebaseball.network

import kotlinx.serialization.Serializable

@Serializable
data class DailyBoxscoreResponseData(
    val league: League
)

@Serializable
data class League(
    val date: String,
    val games: List<Game>
)

@Serializable
data class Game(
    val game: GameDetails
)

@Serializable
data class GameDetails(
    val id: String,
    val status: String,
    val scheduled: String,
    val home_team: String,
    val away_team: String,
    val outcome: Outcome? = null,
    val final: Final? = null,
    val home: TeamStats,
    val away: TeamStats,
    val pitching: SummarizedPitchingStats? = null
)

@Serializable
data class Final(
    val inning: Int,
    val inning_half: String
)

@Serializable
data class Outcome(
    val type: String? = null,
    val current_inning: Int,
    val current_inning_half: String,
    val count: Count? = null,
    val hitter: PlayerStats? = null,
    val pitcher: PlayerStats? = null,
    val runners: List<PlayerStats>? = null
)

@Serializable
data class Count(
    val balls: Int,
    val strikes: Int,
    val outs: Int,
    val inning: Int,
    val inning_half: String,
    val half_over: Boolean
)

@Serializable
data class TeamStats(
    val name: String,
    val market: String,
    val abbr: String,
    val id: String,
    val runs: Int,
    val hits: Int,
    val errors: Int,
    val win: Int,
    val loss: Int,
    val probable_pitcher: PlayerStats? = null,
    val starting_pitcher: PlayerStats? = null,
    val lineup: List<LineupEntry>? = null,
    val scoring: List<Inning>? = null,
    val statistics: Statistics? = null,
    val players: List<PlayerStats>? = null
)

@Serializable
data class SummarizedPitchingStats(
    val win: PlayerStats,
    val loss: PlayerStats,
    val save: PlayerStats? = null,
    val hold: List<PlayerStats>? = null,
    val blown_save: List<PlayerStats>? = null
)

@Serializable
data class Player(
    val player: PlayerStats
)

@Serializable
data class PlayerStats(
    val first_name: String,
    val last_name: String,
    val full_name: String? = null,
    val jersey_number: String? = null,
    val status: String? = null,
    val position: String? = null,
    val primary_position: String? = null,
    val id: String,
    val throw_hand: String? = null,
    val bat_hand: String? = null,
    val team: Team? = null,
    val seasons: List<Season>? = null,
    val statistics: Statistics? = null,
    val ending_base: Int? = null,
    val win: Int? = null,
    val loss: Int? = null,
    val save: Int? = null,
    val hold: Int? = null,
    val blown_save: Int? = null,
    val era: Double? = null
)

@Serializable
data class Team(
    val name: String,
    val market: String,
    val abbr: String,
    val id: String
)

@Serializable
data class Season(
    val id: String,
    val year: Int,
    val type: String,
    val totals: Total,
)

@Serializable
data class Total(
    val statistics: Statistics
)

@Serializable
data class Inning(
    val number: Int,
    val sequence: Int,
    val runs: String,
    val hits: String,
    val errors: String,
    val type: String? = null
)

@Serializable
data class LineupEntry(
    val id: String,
    val inning: Int,
    val order: Int,
    val position: Int,
    val sequence: Int,
    val inning_half: String? = null
)

@Serializable
data class Statistics(
    val hitting: Hitting? = null,
    val pitching: Pitching? = null
)

@Serializable
data class Hitting(
    val overall: HittingStats
)

@Serializable
data class Pitching(
    val overall: PitchingStats
)

@Serializable
data class HittingStats(
    val ab: Int,
    val rbi: Int,
    val obp: Double,
    val ops: Double,
    val slg: Double,
    val avg: Double,
    val runs: Runs,
    val onbase: OnBase,
    val outs: Outs,
    val steal: Steal
)

@Serializable
data class OnBase(
    val h: Int,
    val bb: Int,
    val hr: Int,
    val tb: Int
)

@Serializable
data class Steal(
    val stolen: Int
)

@Serializable
data class PitchingStats(
    val oba: String,
    val era: String,
    val k9: Double,
    val whip: String,
    val kbb: Double,
    val ip_2: Double,
    val slg: String,
    val onbase: OnBase,
    val runs: Runs,
    val outs: Outs,
    val games: GameResult
)

@Serializable
data class GameResult(
    val win: Int,
    val loss: Int,
    val save: Int
)

@Serializable
data class Outs(
    val ktotal: Int
)

@Serializable
data class Runs(
    val total: Int,
    val unearned: Int? = null,
    val earned: Int? = null,
)

@Serializable
data class DailySummary(
    val league: SimplifiedLeague
)

@Serializable
data class SimplifiedLeague(
    val games: List<SimplifiedGame>
)

@Serializable
data class SimplifiedGame(
    val game: SimplifiedGameDetails
)

@Serializable
data class SimplifiedGameDetails(
    val home_team: String,
    val away_team: String,
    val home: SimplifiedTeamDetails,
    val away: SimplifiedTeamDetails
)

@Serializable
data class SimplifiedTeamDetails(
    val name: String,
    val market: String,
    val abbr: String,
    val id: String,
    var probable_pitcher: PlayerStats? = null,
    val roster: List<PlayerStats>? = null,
    val lineup: List<LineupEntry>? = null
)


@Serializable
data class FantasyPlayerData(
    val league: FantasyLeague
)

@Serializable
data class FantasyLeague(
    val games: List<FantasyGameObject>
)

@Serializable
data class FantasyGameObject(
    val game: FantasyGameDetails
)

@Serializable
data class FantasyGameDetails(
    val home: FantasyTeamDetails,
    val away: FantasyTeamDetails
)

@Serializable
data class FantasyTeamDetails(
    val players: List<FantasyPlayerStats>? = null
)

@Serializable
data class FantasyPlayerStats(
    val id: String,
    val first_name: String,
    val last_name: String,
    val primary_position: String? = null,
    val statistics: Statistics? = null,
)
