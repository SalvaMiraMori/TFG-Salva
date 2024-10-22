package com.example.tfg_salva.localDatabase

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tfg_salva.dataClasses.Game
import com.example.tfg_salva.dataClasses.Notification
import com.example.tfg_salva.dataClasses.Team

@Entity
data class UserDataEntity (
    @PrimaryKey val userId: String
)

@Entity(primaryKeys = ["matchId", "userId"])
data class SubscribedGamesEntity (
    val matchId: String,
    val userId: String
)

@Entity(primaryKeys = ["teamId", "esportsMatchId"])
data class TeamEntity (
    val teamId: String,
    val esportsMatchId: String,
    val homeLeagueId: String,
    val image: String,
    val name: String,
    val code: String
)

@Entity(primaryKeys = ["id", "esportsGameId"])
data class NotificationEntity (
    val id: String,
    val esportsMatchId: String,
    val esportsGameId: String,
    val playingTime: String,
    val text: String,
    val blueSideKills: String,
    val redSideKills: String,
    val blueSideGold: String,
    val redSideGold: String,
    val blueSideTowers: String,
    val redSideTowers: String,
    val blueSideDragons: String,
    val redSideDragons: String,
    val blueSideTeam: String,
    val redSideTeam: String
)

@Entity
data class MatchEntity (
    @PrimaryKey val esportsMatchId: String,
    val playingStartingTime: String,
    val status: String,
    // var gamesList: ArrayList<Game>,
    // var teams: ArrayList<Team>,
    var numberOfGames: Int
)

@Entity(primaryKeys = ["esportsGameId", "esportsMatchId"])
data class GameEntity (
    val esportsGameId: String,
    val esportsMatchId: String,
    val state: String,
    val gameNumber: Int,
    // var notifications: ArrayList<Notification>
)