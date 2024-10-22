package com.example.tfg_salva.localDatabase

import com.example.tfg_salva.MyApplication
import com.example.tfg_salva.dataClasses.*
import com.example.tfg_salva.localDatabase.DAOs.*
import com.example.tfg_salva.viewModel.UsersViewModel
import java.lang.reflect.TypeVariable
import kotlin.concurrent.thread

object LocalDatabaseRepo {
    var appDatabase: AppDatabase
    var userDataDao: UserDataDao
    var subscribedGamesDao: SubscribedGamesDao
    var matchDao: MatchDao
    var gameDao: GameDao
    var teamDao: TeamDao
    var notificationDao: NotificationDao

    init{
        var myApplication = MyApplication
        appDatabase = AppDatabase.getDatabase(myApplication.getContext())
        userDataDao = appDatabase.userDataDao()
        subscribedGamesDao = appDatabase.subscribedGamesDao()
        matchDao = appDatabase.matchDao()
        gameDao = appDatabase.gameDao()
        teamDao = appDatabase.teamDao()
        notificationDao = appDatabase.notificationDao()
    }

    fun insertUserData(userData: UserData){
        thread{
            userDataDao.insertAll(UserDataEntity(userData.userID))
            for(subscribedGame in userData.subscribedGames){
                subscribedGamesDao.insertAll(SubscribedGamesEntity(subscribedGame, userData.userID))
            }
        }
    }

    fun insertSubscribedGame(esportsMatchId: String, userId:String){
        thread{
            subscribedGamesDao.insertAll(SubscribedGamesEntity(esportsMatchId, userId))
        }
    }

    fun deleteSubscribedGame(esportsMatchId: String, userId: String){
        thread{
            subscribedGamesDao.delete(SubscribedGamesEntity(esportsMatchId, userId))
        }
    }

    fun insertMatches(vararg matches: Match){
        var matchEntityList = emptyArray<MatchEntity>()
        for(match in matches){
            matchEntityList = matchEntityList.plus(MatchEntity(match.esportsMatchId, match.playingStartingTime, match.status, match.numberOfGames))

            var teamsArray = emptyArray<Team>()
            for(team in match.teams){
                teamsArray = teamsArray.plus(team)
            }
            insertTeams(*teamsArray, matchId = match.esportsMatchId)

            /*var gamesArray = emptyArray<Game>()
            for(game in match.gamesList){
                gamesArray = gamesArray.plus(game)
            }
            insertGames(*gamesArray, matchId = match.esportsMatchId)*/
        }

        thread{
            matchDao.insertAll(*matchEntityList)
            println("Matches inserted: " + matchDao.getAll().toString())
        }
    }

    fun insertGames(vararg games: Game, matchId: String){
        var gameEntityList = emptyArray<GameEntity>()
        for(game in games){
            gameEntityList = gameEntityList.plus(GameEntity(game.esportsGameId, matchId, game.state, game.gameNumber))
        }

        thread{
            gameDao.insertAll(*gameEntityList)
        }
    }

    fun insertTeams(vararg teams: Team, matchId: String){
        var teamEntityList = emptyArray<TeamEntity>()
        for(team in teams){
            teamEntityList = teamEntityList.plus(TeamEntity(team.teamId, matchId, team.homeLeagueId, team.image, team.name, team.code))
        }

        thread{
            teamDao.insertAll(*teamEntityList)
        }
    }

    fun insertNotifications(vararg notifications: Notification, gameId: String, matchId: String){
        var notificationEntityList = emptyArray<NotificationEntity>()
        for(notification in notifications){
            notificationEntityList = notificationEntityList.plus(NotificationEntity(notification.id, matchId, gameId, notification.playingTime, notification.text, notification.blueSideKills, notification.redSideKills, notification.blueSideGold, notification.redSideGold, notification.blueSideTowers, notification.redSideTowers, notification.blueSideDragons, notification.redSideDragons, notification.blueSideTeam, notification.redSideTeam))
        }

        thread{
            notificationDao.insertAll(*notificationEntityList)
        }
    }

    fun deleteMatch(matchId: String){
        thread {
            matchDao.deleteMatchById(matchId)
            deleteGamesOfMatch(matchId)
            deleteTeamsOfMatch(matchId)
            deleteNotificationsOfMatch(matchId)
        }
    }

    fun deleteGamesOfMatch(matchId: String){
        gameDao.deleteGameFromMatch(matchId)
    }

    fun deleteTeamsOfMatch(matchId: String){
        teamDao.deleteTeamsOfMatch(matchId)
    }

    fun deleteGame(gameId: String){

    }

    fun deleteTeam(teamId: String){

    }

    fun deleteNotificationsOfMatch(matchId: String){
        notificationDao.deleteNotificationsFromMatch(matchId)
    }

    fun deleteNotificationsOfGame(gameId: String){

    }

    fun deleteNotification(id: String){

    }

    // Use the following functions in a thread
    fun getUserData(): UserData{
        var subscribedGames: ArrayList<String>
        subscribedGames = ArrayList()
        for(subscribedGame in subscribedGamesDao.getAll()){
            subscribedGames.add(subscribedGame.matchId)
        }

        // println(userDataDao.getAll().toString())
        var userId = userDataDao.getAll()[0].userId

        return UserData(userId, subscribedGames)
    }

    fun getScheduledMatches(): ArrayList<Match>{
        var matches: ArrayList<Match>
        matches = ArrayList()

        for(match in matchDao.getScheduledMatches()){
            var gamesList: ArrayList<Game>
            gamesList = ArrayList()
            for(game in gameDao.getGameFromMatch(match.esportsMatchId)){

                var notificationsList: ArrayList<Notification>
                notificationsList = ArrayList()
                for(notification in notificationDao.getGameNotifications(game.esportsGameId)){
                    notificationsList.add(Notification(notification.id, notification.playingTime, notification.text, notification.blueSideKills, notification.redSideKills, notification.blueSideGold, notification.redSideGold, notification.blueSideTowers, notification.redSideTowers, notification.blueSideDragons, notification.redSideDragons, notification.blueSideTeam, notification.redSideTeam))
                }

                gamesList.add(Game(game.esportsGameId, game.state, game.gameNumber, notificationsList))
            }

            var teamsList: ArrayList<Team>
            teamsList = ArrayList()
            for(team in teamDao.getTeamsFromMatch(match.esportsMatchId)){
                teamsList.add(Team(team.code, team.homeLeagueId, team.image, team.name, team.teamId))
            }

            matches.add(Match(match.playingStartingTime, match.status, match.esportsMatchId, gamesList, teamsList, match.numberOfGames))
        }

        return matches
    }

    fun getCompletedMatches(): ArrayList<Match>{
        var matches: ArrayList<Match>
        matches = ArrayList()

        for(match in matchDao.getCompletedMatches()){
            var gamesList: ArrayList<Game>
            gamesList = ArrayList()
            for(game in gameDao.getGameFromMatch(match.esportsMatchId)){

                var notificationsList: ArrayList<Notification>
                notificationsList = ArrayList()
                for(notification in notificationDao.getGameNotifications(game.esportsGameId)){
                    notificationsList.add(Notification(notification.id, notification.playingTime, notification.text, notification.blueSideKills, notification.redSideKills, notification.blueSideGold, notification.redSideGold, notification.blueSideTowers, notification.redSideTowers, notification.blueSideDragons, notification.redSideDragons, notification.blueSideTeam, notification.redSideTeam))
                }

                gamesList.add(Game(game.esportsGameId, game.state, game.gameNumber, notificationsList))
            }

            var teamsList: ArrayList<Team>
            teamsList = ArrayList()
            for(team in teamDao.getTeamsFromMatch(match.esportsMatchId)){
                teamsList.add(Team(team.code, team.homeLeagueId, team.image, team.name, team.teamId))
            }

            matches.add(Match(match.playingStartingTime, match.status, match.esportsMatchId, gamesList, teamsList, match.numberOfGames))
        }

        return matches
    }

    fun getLiveMatches(): ArrayList<Match>{
        var matches: ArrayList<Match>
        matches = ArrayList()

        for(match in matchDao.getLiveMatches()){
            var gamesList: ArrayList<Game>
            gamesList = ArrayList()
            for(game in gameDao.getGameFromMatch(match.esportsMatchId)){

                var notificationsList: ArrayList<Notification>
                notificationsList = ArrayList()
                for(notification in notificationDao.getGameNotifications(game.esportsGameId)){
                    notificationsList.add(Notification(notification.id, notification.playingTime, notification.text, notification.blueSideKills, notification.redSideKills, notification.blueSideGold, notification.redSideGold, notification.blueSideTowers, notification.redSideTowers, notification.blueSideDragons, notification.redSideDragons, notification.blueSideTeam, notification.redSideTeam))
                }

                gamesList.add(Game(game.esportsGameId, game.state, game.gameNumber, notificationsList))
            }

            var teamsList: ArrayList<Team>
            teamsList = ArrayList()
            for(team in teamDao.getTeamsFromMatch(match.esportsMatchId)){
                teamsList.add(Team(team.code, team.homeLeagueId, team.image, team.name, team.teamId))
            }

            matches.add(Match(match.playingStartingTime, match.status, match.esportsMatchId, gamesList, teamsList, match.numberOfGames))
        }

        return matches
    }
}