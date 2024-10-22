package com.example.tfg_salva.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tfg_salva.dataClasses.Game
import com.example.tfg_salva.dataClasses.Match
import com.example.tfg_salva.dataClasses.Notification
import com.example.tfg_salva.database.DatabaseAdapter
import com.example.tfg_salva.localDatabase.LocalDatabaseRepo
import kotlin.concurrent.thread

object LiveMatchViewModel: ViewModel(), DatabaseAdapter.LiveMatchInterface {

    var liveMatches: MutableLiveData<ArrayList<Match>>
    var databaseAdapter: DatabaseAdapter

    init {
        liveMatches = MutableLiveData()
        databaseAdapter = DatabaseAdapter()
        databaseAdapter.getLiveMatches()
    }

    override fun setMatches(matchList: ArrayList<Match>) {
        if(liveMatches.value != matchList){
            thread{
                val matchIdArray: ArrayList<String>
                matchIdArray = ArrayList()
                for(match in matchList){
                    matchIdArray.add(match.esportsMatchId)
                }
                for(match in LocalDatabaseRepo.getLiveMatches()){
                    if(match.esportsMatchId !in matchIdArray){
                        LocalDatabaseRepo.deleteMatch(match.esportsMatchId)
                    }
                }
            }

            var matchListArray = emptyArray<Match>()
            for(match in matchList){
                matchListArray = matchListArray.plus(match)
            }
            LocalDatabaseRepo.insertMatches(*matchListArray)
        }
        liveMatches.value = matchList
    }

    override fun setGames(esportsMatchId: String, games: ArrayList<Game>) {
        var matchList = ArrayList<Match>()
        for(match in liveMatches.value!!){
            if(match.esportsMatchId == esportsMatchId && match.gamesList == games){
                match.gamesList = games

                var gamesArray = emptyArray<Game>()
                for(game in games){
                    gamesArray = gamesArray.plus(game)
                }
                LocalDatabaseRepo.insertGames(*gamesArray, matchId = esportsMatchId)
            }
            matchList += match
        }

        liveMatches.value = matchList
    }

    override fun setNotifications(
        esportsMatchId: String,
        esportsGameId: String,
        notifications: ArrayList<Notification>
    ) {
        var matchList = ArrayList<Match>()
        for(match in liveMatches.value!!){
            if(match.esportsMatchId == esportsMatchId){
                for(game in match.gamesList){
                    if(game.esportsGameId == esportsGameId && game.notifications != notifications){
                        game.notifications = notifications

                        var notificationsArray = emptyArray<Notification>()
                        for(notification in notifications){
                            notificationsArray = notificationsArray.plus(notification)
                        }
                        LocalDatabaseRepo.insertNotifications(*notificationsArray, matchId = esportsMatchId, gameId = esportsGameId)
                    }
                }
            }
            matchList += match
        }

        liveMatches.value = matchList
    }
}