package com.example.tfg_salva.database

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.tfg_salva.dataClasses.*
import com.example.tfg_salva.viewModel.CompletedMatchViewModel
import com.example.tfg_salva.viewModel.LiveMatchViewModel
import com.example.tfg_salva.viewModel.ScheduledMatchViewModel
import com.example.tfg_salva.viewModel.UsersViewModel
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DatabaseAdapter: AppCompatActivity() {
    val db: FirebaseFirestore
    val TAG: String
    val completedMatchListener: CompletedMatchViewModel
    val scheduledMatchListener: ScheduledMatchViewModel
    val liveMatchListener: LiveMatchViewModel
    var liveMatchList: ArrayList<Match>
    var completedMatchList: ArrayList<Match>
    var scheduledMatchList: ArrayList<Match>

    init{
        db = Firebase.firestore
        TAG = "DatabaseAdapter"
        completedMatchListener = CompletedMatchViewModel
        scheduledMatchListener = ScheduledMatchViewModel
        liveMatchListener = LiveMatchViewModel
        completedMatchList = ArrayList<Match>()
        liveMatchList = ArrayList<Match>()
        scheduledMatchList = ArrayList<Match>()
        listenChanges()
    }

    interface CompletedMatchInterface{
        fun setMatches(matches: ArrayList<Match>)
        fun setGames(esportsMatchId: String, games: ArrayList<Game>)
        fun setNotifications(esportsMatchId: String, esportsGameId: String, notifications: ArrayList<Notification>)
    }

    interface ScheduledMatchInterface{
        fun setMatches(matches: ArrayList<Match>)
        fun setGames(esportsMatchId: String, games: ArrayList<Game>)
        fun setNotifications(esportsMatchId: String, esportsGameId: String, notifications: ArrayList<Notification>)
    }

    interface LiveMatchInterface{
        fun setMatches(matches: ArrayList<Match>)
        fun setGames(esportsMatchId: String, games: ArrayList<Game>)
        fun setNotifications(esportsMatchId: String, esportsGameId: String, notifications: ArrayList<Notification>)
    }

    interface UsersInterface{
        fun setUserData(userData: UserData)
    }

    fun getCompletedMatches() {
        db.collection("CompletedMatches").orderBy("startTime", Query.Direction.DESCENDING).limit(20).get().addOnSuccessListener { result ->
            completedMatchList = ArrayList<Match>()

            for (matchDocument in result) {
                var teamsList = ArrayList<Team>()
                var gamesList = ArrayList<Game>()
                var teamsListAux = matchDocument.data["teams"] as ArrayList<Map<String, Any>>

                var match = Match(
                    matchDocument.data["startTime"] as String, "Completed",
                    matchDocument.data["esportsMatchId"] as String, gamesList, teamsList, (matchDocument.data["numberOfGames"] as Long).toInt()
                )

                for (teamAux in teamsListAux) {
                    var teamName = teamAux["name"]
                    if (teamName == null) {
                        teamsList += Team("", "",
                            "", "TBD", ""
                        )
                    }else{
                        teamName = teamAux["name"] as String
                        val homeLeague = teamAux["homeLeague"] as Map<String, String>


                        teamsList += Team(
                            teamAux["code"] as String, homeLeague["id"] as String,
                            (teamAux["image"] as String).replace("http:", "https:"), teamName, teamAux["id"] as String
                        )
                    }
                }

                match.teams = teamsList
                completedMatchList += match

                db.collection("CompletedMatches").document(matchDocument.id).addSnapshotListener { value, error ->
                    addCompletedMatchesToApp()
                }

                getGamesFromCompletedMatch(matchDocument.id)
            }

            addCompletedMatchesToApp()

            db.collection("CompletedMatches").addSnapshotListener { value, error ->
                addCompletedMatchesToApp()
            }
        }

    }

    fun getGamesFromCompletedMatch(esportsMatchId: String) {
        db.collection("CompletedMatches").document(esportsMatchId).collection("Games").get()
            .addOnSuccessListener { result ->
                var games = ArrayList<Game>()
                // Log.d(TAG, "${esportsMatchId}")
                for (gameDocument in result) {
                    // Log.d(TAG, "${gameDocument.id} => ${gameDocument.data}")
                    var notificationsList = ArrayList<Notification>()
                    var game = Game(
                        gameDocument["esportsGameId"] as String,
                        gameDocument["state"] as String,
                        (gameDocument["number"] as Long).toInt(),
                        notificationsList
                    )

                    games += game

                    getNotificationsFromCompletedMatch(esportsMatchId, gameDocument.id)

                    db.collection("CompletedMatches").document(esportsMatchId).collection("Games").document(gameDocument.id).addSnapshotListener { value, error ->
                        addCompletedMatchesToApp()
                    }

                }
                completedMatchListener.setGames(esportsMatchId, games)
            }

        db.collection("CompletedMatches").document(esportsMatchId).collection("Games").addSnapshotListener { value, error ->
            addCompletedMatchesToApp()
        }
    }

    fun getNotificationsFromCompletedMatch(esportsMatchId: String, esportsGameId: String){
        db.collection("CompletedMatches").document(esportsMatchId).collection("Games").document(esportsGameId).collection("Notifications").orderBy("playingTime", Query.Direction.ASCENDING).get().addOnSuccessListener { result ->
            var notifications = ArrayList<Notification>()
            // Log.d(TAG, "${esportsMatchId} => ${esportsGameId}")
            for(notificationDocument in result){
                // Log.d(TAG, "${notificationDocument.id} => ${notificationDocument.data}")
                var notification = Notification(notificationDocument.id, notificationDocument["playingTime"] as String, notificationDocument["text"] as String,
                    notificationDocument["blueTeamKills"] as String, notificationDocument["redTeamKills"] as String,
                    notificationDocument["blueTeamGold"] as String, notificationDocument["redTeamGold"] as String,
                    notificationDocument["blueTeamTowers"] as String, notificationDocument["redTeamTowers"] as String,
                    notificationDocument["blueTeamDragons"] as String, notificationDocument["redTeamDragons"] as String,
                    notificationDocument["blueSideTeam"] as String, notificationDocument["redSideTeam"] as String)
                notifications += notification
            }

            completedMatchListener.setNotifications(esportsMatchId, esportsGameId, notifications)
        }

        db.collection("CompletedMatches").document(esportsMatchId).collection("Games").document(esportsGameId).collection("Notifications").addSnapshotListener { value, error ->
            addCompletedMatchesToApp()
        }
    }

    fun getScheduledMatches() {
        db.collection("ScheduledMatches").orderBy("startTime").get().addOnSuccessListener { result ->
            scheduledMatchList = ArrayList<Match>()

            for (matchDocument in result) {
                // Log.d(TAG, "${matchDocument.id} => ${matchDocument.data}")
                var teamsList = ArrayList<Team>()
                var gamesList = ArrayList<Game>()
                var teamsListAux = matchDocument.data["teams"] as ArrayList<Map<String, Any>>

                var match = Match(
                    matchDocument.data["startTime"] as String, "Unstarted",
                    matchDocument.data["esportsMatchId"] as String, gamesList, teamsList, (matchDocument.data["numberOfGames"] as Long).toInt()
                )

                for (teamAux in teamsListAux) {
                    var teamName = teamAux["name"]
                    if (teamName == null) {
                        teamsList += Team("", "",
                            "", "TBD", ""
                        )
                    }else{
                        teamName = teamAux["name"] as String

                        val homeLeague: Map<String, String>

                        if (teamAux["homeLeague"] == null) {
                            homeLeague = mapOf("id" to "null")
                        }else {
                            homeLeague = teamAux["homeLeague"] as Map<String, String>
                        }

                        teamsList += Team(
                            teamAux["code"] as String, homeLeague["id"] as String,
                            (teamAux["image"] as String).replace("http:", "https:"), teamName, teamAux["id"] as String
                        )
                    }
                }
                match.teams = teamsList
                scheduledMatchList += match
            }
            addScheduledMatchesToApp()
        }

        /*db.collection("ScheduledMatches").addSnapshotListener { value, error ->
            addScheduledMatchesToApp()
        }*/
    }

    fun getLiveMatches() {
        db.collection("LiveMatches").orderBy("startTime").get().addOnSuccessListener { result ->
            liveMatchList = ArrayList<Match>()

            for (matchDocument in result) {
                // Log.d(TAG, "${matchDocument.id} => ${matchDocument.data}")
                var teamsList = ArrayList<Team>()
                var gamesList = ArrayList<Game>()
                var teamsListAux = matchDocument.data["teams"] as ArrayList<Map<String, Any>>

                var match = Match(
                    matchDocument.data["startTime"] as String, "In progress",
                    matchDocument.data["esportsMatchId"] as String, gamesList, teamsList, (matchDocument.data["numberOfGames"] as Long).toInt()
                )

                for (teamAux in teamsListAux) {
                    var teamName = teamAux["name"]
                    if (teamName == null) {
                        teamsList += Team("", "",
                            "", "TBD", ""
                        )
                    }else{
                        teamName = teamAux["name"] as String
                        val homeLeague = teamAux["homeLeague"] as Map<String, String>


                        teamsList += Team(
                            teamAux["code"] as String, homeLeague["id"] as String,
                            (teamAux["image"] as String).replace("http:", "https:"), teamName, teamAux["id"] as String
                        )
                    }

                }

                match.teams = teamsList
                liveMatchList += match
                db.collection("LiveMatches").document(matchDocument.id).addSnapshotListener { value, error ->
                    addLiveMatchesToApp()
                }

                getGamesFromLiveMatch(matchDocument.id)
            }
            addLiveMatchesToApp()

            db.collection("LiveMatches").addSnapshotListener { value, error ->
                addLiveMatchesToApp()
            }
        }
    }

    fun getGamesFromLiveMatch(esportsMatchId: String) {
        db.collection("LiveMatches").document(esportsMatchId).collection("Games").get()
            .addOnSuccessListener { result ->
                var games = ArrayList<Game>()

                for (gameDocument in result) {
                    // Log.d(TAG, "${gameDocument.id} => ${gameDocument.data}")
                    var notificationsList = ArrayList<Notification>()
                    var game = Game(
                        gameDocument["esportsGameId"] as String,
                        gameDocument["state"] as String,
                        (gameDocument["number"] as Long).toInt(),
                        notificationsList
                    )

                    games += game

                    getNotificationsFromLiveMatch(esportsMatchId, gameDocument.id)

                    db.collection("LiveMatches").document(esportsMatchId).collection("Games").document(gameDocument.id).addSnapshotListener { value, error ->
                        addLiveMatchesToApp()
                    }
                }
                liveMatchListener.setGames(esportsMatchId, games)

                db.collection("LiveMatches").document(esportsMatchId).collection("Games").addSnapshotListener { value, error ->
                    liveMatchListener.setGames(esportsMatchId, games)
                }
            }


    }

    fun getNotificationsFromLiveMatch(esportsMatchId: String, esportsGameId: String){
        db.collection("LiveMatches").document(esportsMatchId).collection("Games").document(esportsGameId).collection("Notifications").orderBy("playingTime", Query.Direction.ASCENDING).get().addOnSuccessListener { result ->
            var notifications = ArrayList<Notification>()

            for(notificationDocument in result){
                var notification = Notification(notificationDocument.id, notificationDocument["playingTime"] as String, notificationDocument["text"] as String,
                    notificationDocument["blueTeamKills"] as String, notificationDocument["redTeamKills"] as String,
                    notificationDocument["blueTeamGold"] as String, notificationDocument["redTeamGold"] as String,
                    notificationDocument["blueTeamTowers"] as String, notificationDocument["redTeamTowers"] as String,
                    notificationDocument["blueTeamDragons"] as String, notificationDocument["redTeamDragons"] as String,
                    notificationDocument["blueSideTeam"] as String, notificationDocument["redSideTeam"] as String)
                notifications += notification
            }

            liveMatchListener.setNotifications(esportsMatchId, esportsGameId, notifications)
            db.collection("LiveMatches").document(esportsMatchId).collection("Games").document(esportsGameId).collection("Notifications").addSnapshotListener { value, error ->
                liveMatchListener.setNotifications(esportsMatchId, esportsGameId, notifications)
            }
        }
    }

    fun getNotificationsFromLiveMatchSync(esportsMatchId: String, esportsGameId: String){
        val task : Task<QuerySnapshot> = db.collection("LiveMatches").document(esportsMatchId).collection("Games").document(esportsGameId).collection("Notifications").get()
        val result : QuerySnapshot = Tasks.await(task)

        var notifications = ArrayList<Notification>()

        for(notificationDocument in result){
            var notification = Notification(notificationDocument.id, notificationDocument["playingTime"] as String, notificationDocument["text"] as String,
                notificationDocument["blueTeamKills"] as String, notificationDocument["redTeamKills"] as String,
                notificationDocument["blueTeamGold"] as String, notificationDocument["redTeamGold"] as String,
                notificationDocument["blueTeamTowers"] as String, notificationDocument["redTeamTowers"] as String,
                notificationDocument["blueTeamDragons"] as String, notificationDocument["redTeamDragons"] as String,
                notificationDocument["blueSideTeam"] as String, notificationDocument["redSideTeam"] as String)
            notifications += notification
        }

        liveMatchListener.setNotifications(esportsMatchId, esportsGameId, notifications)
    }

    fun listenChanges(){
        db.collection("LiveMatches").addSnapshotListener { snapshot, error ->
            getLiveMatches()
        }
        db.collection("CompletedMatches").addSnapshotListener { snapshot, error ->
            getCompletedMatches()
        }
        db.collection("ScheduledMatches").addSnapshotListener { snapshot, error ->
            getScheduledMatches()
        }
    }

    fun addCompletedMatchesToApp(){
        completedMatchListener.setMatches(completedMatchList)
    }

    fun addLiveMatchesToApp(){
        liveMatchListener.setMatches(liveMatchList)
    }

    fun addScheduledMatchesToApp(){
        scheduledMatchListener.setMatches(scheduledMatchList)
    }

    fun addUserIdToDB(userID: String){

        db.collection("Users").get().addOnSuccessListener { result ->
            for(user in result){
                if(user["userID"] == userID){
                    getUserData(userID)
                    return@addOnSuccessListener
                }
            }

            var data = HashMap<String, String>()

            data.put("userID", userID)

            db.collection("Users").document(userID).set(data).addOnSuccessListener {
                getUserData(userID)
            }
        }
    }

    fun getUserData(userID: String){
        db.collection("Users").document(userID).get().addOnSuccessListener { result ->
            var userData = UserData(result.data?.get("userID") as String, ArrayList<String>())
            if((result.data as HashMap<String, Any>).keys.contains("subscribedGames")){
                userData = UserData(result.data?.get("userID") as String, result.data?.get("subscribedGames") as ArrayList<String>)
            }
            UsersViewModel.setUserData(userData)
        }
    }

    fun addSubscribedGame(esportsMatchId: String, userID: String){
        db.collection("Users").document(userID).get().addOnSuccessListener { result ->
            var subscribedGames = ArrayList<String>()
            if((result.data as HashMap<String, Any>).keys.contains("subscribedGames")){
                subscribedGames = result["subscribedGames"] as ArrayList<String>
            }
            subscribedGames.add(esportsMatchId)

            var data = HashMap<String, Any>()
            data.put("userID", result["userID"] as String)
            data.put("subscribedGames", subscribedGames)

            db.collection("Users").document(userID).set(data).addOnSuccessListener {
                getUserData(userID)
            }
        }
    }

    fun removeSubscribedGame(esportsMatchId: String, userID: String){
        db.collection("Users").document(userID).get().addOnSuccessListener { result ->

            var subscribedGames = result["subscribedGames"] as ArrayList<String>
            subscribedGames.remove(esportsMatchId)

            var data = HashMap<String, Any>()
            data.put("userID", result["userID"] as String)
            data.put("subscribedGames", subscribedGames)

            db.collection("Users").document(userID).set(data).addOnSuccessListener {
                getUserData(userID)
            }
        }
    }

}