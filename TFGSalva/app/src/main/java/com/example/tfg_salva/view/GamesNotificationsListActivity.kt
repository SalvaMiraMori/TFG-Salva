package com.example.tfg_salva.view


import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ToggleButton
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.tfg_salva.R
import com.example.tfg_salva.adapter.NotificationCardAdapter
import com.example.tfg_salva.dataClasses.Game
import com.example.tfg_salva.viewModel.CompletedMatchViewModel
import com.example.tfg_salva.viewModel.LiveMatchViewModel


class GamesNotificationsListActivity : AppCompatActivity() {
    val TAG = "GamesListActivity"
    lateinit var esportsMatchId: String
    lateinit var gamesList: ArrayList<Game>
    lateinit var recyclerViewNotifications: RecyclerView
    lateinit var game1ToggleButton: ToggleButton
    lateinit var game2ToggleButton: ToggleButton
    lateinit var game3ToggleButton: ToggleButton
    lateinit var game4ToggleButton: ToggleButton
    lateinit var game5ToggleButton: ToggleButton
    lateinit var notificationsRefreshSwipe: SwipeRefreshLayout
    lateinit var liveMatchViewModel: LiveMatchViewModel
    lateinit var completedMatchViewModel: CompletedMatchViewModel


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_games_and_notifications_list)

        esportsMatchId = intent.extras?.getString("esportsMatchId") as String
        Log.d(TAG, "Showing notifications of matchId: " + esportsMatchId)
        liveMatchViewModel = LiveMatchViewModel
        completedMatchViewModel = CompletedMatchViewModel

        recyclerViewNotifications = findViewById(R.id.recyclerViewNotificationsList)
        recyclerViewNotifications.layoutManager = StaggeredGridLayoutManager(1, LinearLayoutManager.VERTICAL)

        notificationsRefreshSwipe = findViewById(R.id.notifications_refresh_swipe)

        game1ToggleButton = findViewById(R.id.game1ToggleButton)
        game2ToggleButton = findViewById(R.id.game2ToggleButton)
        game3ToggleButton = findViewById(R.id.game3ToggleButton)
        game4ToggleButton = findViewById(R.id.game4ToggleButton)
        game5ToggleButton = findViewById(R.id.game5ToggleButton)

        for(match in CompletedMatchViewModel.completedMatches.value!!){
            if(match.esportsMatchId == esportsMatchId){
                gamesList = match.gamesList
                recyclerViewNotifications.adapter = NotificationCardAdapter(match.gamesList[0].notifications, this)
            }
        }

        for(match in LiveMatchViewModel.liveMatches.value!!){
            if(match.esportsMatchId == esportsMatchId){
                gamesList = match.gamesList
                recyclerViewNotifications.adapter = NotificationCardAdapter(match.gamesList[0].notifications, this)
            }
        }

        setRefreshSwipeAction()

        setVisibleButtons()

        setOnClickListeners()

        setNotificationsListeners()

        game1ToggleButton.isChecked = true
    }

    fun setRefreshSwipeAction(){
        notificationsRefreshSwipe.setOnRefreshListener{
            liveMatchViewModel = LiveMatchViewModel
            liveMatchViewModel.databaseAdapter.getLiveMatches()
        }
    }

    fun setVisibleButtons(){
        when(gamesList.size){
            1 -> {
                game2ToggleButton.visibility = View.INVISIBLE
                game3ToggleButton.visibility = View.INVISIBLE
                game4ToggleButton.visibility = View.INVISIBLE
                game5ToggleButton.visibility = View.INVISIBLE
            }
            2-> {
                game3ToggleButton.visibility = View.INVISIBLE
                game4ToggleButton.visibility = View.INVISIBLE
                game5ToggleButton.visibility = View.INVISIBLE
            }
            3-> {
                game4ToggleButton.visibility = View.INVISIBLE
                game5ToggleButton.visibility = View.INVISIBLE
            }
            4-> {
                game5ToggleButton.visibility = View.INVISIBLE
            }
        }
    }

    fun setOnClickListeners(){
        game1ToggleButton.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                game1ToggleButton.isClickable = false
                game2ToggleButton.isChecked = false
                game3ToggleButton.isChecked = false
                game4ToggleButton.isChecked = false
                game5ToggleButton.isChecked = false

                setNotifications(1)

            }else{
                game1ToggleButton.isClickable = true
            }
        }

        game2ToggleButton.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                game1ToggleButton.isChecked = false
                game2ToggleButton.isClickable = false
                game3ToggleButton.isChecked = false
                game4ToggleButton.isChecked = false
                game5ToggleButton.isChecked = false

                setNotifications(2)
            }else{
                game2ToggleButton.isClickable = true
            }
        }

        game3ToggleButton.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                game1ToggleButton.isChecked = false
                game2ToggleButton.isChecked = false
                game3ToggleButton.isClickable = false
                game4ToggleButton.isChecked = false
                game5ToggleButton.isChecked = false

                setNotifications(3)
            }else{
                game3ToggleButton.isClickable = true
            }
        }

        game4ToggleButton.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                game1ToggleButton.isChecked = false
                game2ToggleButton.isChecked = false
                game3ToggleButton.isChecked = false
                game4ToggleButton.isClickable = false
                game5ToggleButton.isChecked = false

                setNotifications(4)
            }else{
                game4ToggleButton.isClickable = true
            }
        }

        game5ToggleButton.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                game1ToggleButton.isChecked = false
                game2ToggleButton.isChecked = false
                game3ToggleButton.isChecked = false
                game4ToggleButton.isChecked = false
                game5ToggleButton.isClickable = false

                setNotifications(5)
            }else{
                game5ToggleButton.isClickable = true
            }
        }
    }

    fun setNotifications(gameNumber: Int){
        for(game in gamesList){
            if(game.gameNumber == gameNumber){
                (recyclerViewNotifications.adapter as NotificationCardAdapter).update(game.notifications)
            }
        }
    }

    fun setNotificationsListeners(){
        liveMatchViewModel.liveMatches.observe(this, Observer {
            for(match in it){
                if(match.esportsMatchId == esportsMatchId){
                    gamesList = match.gamesList
                    Log.d(TAG, "Found match with id " + esportsMatchId)
                }
            }
            Log.d(TAG, "Refreshed notifications")
            Log.d(TAG, "Game selected: " + getNumberGameSelected().toString())
            getNumberGameSelected()?.let { numberGameSelected -> setNotifications(numberGameSelected) }
            notificationsRefreshSwipe.isRefreshing = false
        })
    }

    fun getNumberGameSelected(): Int? {
        if(game1ToggleButton.isChecked){
            return 1
        }else if(game2ToggleButton.isChecked){
            return 2
        }else if(game3ToggleButton.isChecked){
            return 3
        }else if(game4ToggleButton.isChecked){
            return 4
        }else if(game5ToggleButton.isChecked){
            return 5
        }
        return null
    }
}