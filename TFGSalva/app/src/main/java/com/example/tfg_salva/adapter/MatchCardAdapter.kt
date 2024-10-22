package com.example.tfg_salva.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.tfg_salva.R
import com.example.tfg_salva.dataClasses.Match
import com.example.tfg_salva.dataClasses.UserData
import com.example.tfg_salva.view.GamesNotificationsListActivity
import com.example.tfg_salva.viewModel.UsersViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.like.LikeButton
import com.like.OnLikeListener
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class MatchCardAdapter(private val matchList: List<Match>, private val context: Context): RecyclerView.Adapter<MatchCardAdapter.ViewHolder>() {

    val TAG = "MatchCardAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.match_card, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = matchList[position]

        holder.nameTeam1.text = currentItem.teams[0].name
        holder.nameTeam2.text = currentItem.teams[1].name
        holder.playingStartingTime.text = (currentItem.playingStartingTime.substring(0, 10) + ' ' + currentItem.playingStartingTime.substring(11, 19)).toDate().formatTo()
        holder.gameNumberAndStatus.text = "Best of " + currentItem.numberOfGames
        try {
            Picasso.with(context).load(currentItem.teams[0].image)
                .placeholder(R.mipmap.league_logo_foreground).resize(324, 324)
                .into(holder.logoTeam1)
        }catch (exception: Exception){
            Log.d(TAG, exception.toString())
        }
        // Log.d(TAG, "Team " + currentItem.teams[0].name + " image downloaded: " + currentItem.teams[0].image)
        try {
            Picasso.with(context).load(currentItem.teams[1].image).placeholder(R.mipmap.league_logo_foreground).resize(324, 324).into(holder.logoTeam2)
        }catch (exception: Exception){
            Log.d(TAG, exception.toString())
        }
        // Log.d(TAG, "Team " + currentItem.teams[1].name + " image downloaded: " + currentItem.teams[1].image)

        if(currentItem.status != "Unstarted"){
            holder.itemView.setOnClickListener(View.OnClickListener { view ->
                when (view.id) {
                    R.id.matchCard -> {
                        val intent = Intent(context, GamesNotificationsListActivity::class.java)
                        intent.putExtra("esportsMatchId", currentItem.esportsMatchId)
                        context.startActivity(intent)
                    }
                }
            })
        }

        if(currentItem.status != "Completed"){
            holder.subscriptionButton.isLiked = UsersViewModel.userDataMLD.value?.subscribedGames?.contains(currentItem.esportsMatchId) == true

            holder.subscriptionButton.setOnLikeListener(object : OnLikeListener {
                override fun liked(likeButton: LikeButton) {
                    UsersViewModel.addSubscribedGame(currentItem.esportsMatchId)
                    Firebase.messaging.subscribeToTopic(currentItem.esportsMatchId)
                        .addOnCompleteListener { task ->
                            var msg = "Subscribed to match " + currentItem.teams[0].code + " V/S " + currentItem.teams[1].code
                            if (!task.isSuccessful) {
                                msg = "Subscribe failed"
                            }
                            Log.d(TAG, msg)
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                }

                override fun unLiked(likeButton: LikeButton) {
                    UsersViewModel.removeSubscribedGame(currentItem.esportsMatchId)
                    Firebase.messaging.unsubscribeFromTopic(currentItem.esportsMatchId)
                        .addOnCompleteListener { task ->
                            var msg = "Unsubscribed from match " + currentItem.teams[0].code + " V/S " + currentItem.teams[1].code
                            if (!task.isSuccessful) {
                                msg = "Unsubscribe failed"
                            }
                            Log.d(TAG, msg)
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                }
            })
        }else{
            holder.subscriptionButton.isVisible = false
        }


    }

    override fun getItemCount(): Int {
        return matchList.size
    }

    fun String.toDate(dateFormat: String = "yyyy-MM-dd HH:mm:ss", timeZone: TimeZone = TimeZone.getTimeZone("UTC")): Date {
        val parser = SimpleDateFormat(dateFormat, Locale.getDefault())
        // Log.d(TAG, "Format timezone: " + timeZone.displayName + " Time: " + this)
        parser.timeZone = timeZone
        return parser.parse(this)
    }

    fun Date.formatTo(dateFormat: String = "dd/MM/yyyy HH:mm:ss", timeZone: TimeZone = TimeZone.getDefault()): String {
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        formatter.timeZone = timeZone
        // Log.d(TAG, "Local timezone: " + timeZone.displayName + " Time: " + formatter.format(this))
        return formatter.format(this)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTeam1: TextView = itemView.findViewById(R.id.nameTeam1Text)
        val nameTeam2: TextView = itemView.findViewById(R.id.nameTeam2Text)
        val playingStartingTime: TextView = itemView.findViewById(R.id.playingStartingTime)
        val gameNumberAndStatus: TextView = itemView.findViewById(R.id.gameNumberAndStatusText)
        val logoTeam1: ImageView = itemView.findViewById(R.id.logoTeam1)
        val logoTeam2: ImageView = itemView.findViewById(R.id.logoTeam2)
        val vsImage: ImageView = itemView.findViewById(R.id.vsImageView)
        val subscriptionButton: LikeButton = itemView.findViewById(R.id.subscriptionButton)
    }
}