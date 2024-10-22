package com.example.tfg_salva.adapter

import android.content.Context
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.tfg_salva.R
import com.example.tfg_salva.dataClasses.Notification

class NotificationCardAdapter(private var notificationList: List<Notification>, private val context: Context): RecyclerView.Adapter<NotificationCardAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playingTime: TextView = itemView.findViewById(R.id.playingTimeText)
        val notificationText: TextView = itemView.findViewById(R.id.notificationText)
        val blueSideTeam: TextView = itemView.findViewById(R.id.blueSideTeamNotification)
        val redSideTeam: TextView = itemView.findViewById(R.id.redSideTeamNotification)
        val blueSideKills: TextView = itemView.findViewById(R.id.blueSideKillsNotification)
        val redSideKills: TextView = itemView.findViewById(R.id.redSideKillsNotification)
        val blueSideGold: TextView = itemView.findViewById(R.id.blueSideGoldNotification)
        val redSideGold: TextView = itemView.findViewById(R.id.redSideGoldNotification)
        val blueSideTowers: TextView = itemView.findViewById(R.id.blueSideTowersNotification)
        val redSideTowers: TextView = itemView.findViewById(R.id.redSideTowersNotification)
        val blueSideDragons: TextView = itemView.findViewById(R.id.blueSideDragonsNotification)
        val redSideDragons: TextView = itemView.findViewById(R.id.redSideDragonsNotification)
        val expandNotificationDetailsButton: ImageButton = itemView.findViewById(R.id.expandNotificationDetailsButton)
        val notificationDetailsView: LinearLayout = itemView.findViewById(R.id.notificationDetailsView)
        val notificationCard: CardView = itemView.findViewById(R.id.notificationCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.notification_card, parent, false)

        return NotificationCardAdapter.ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = notificationList[position]

        holder.playingTime.text = currentItem.playingTime
        holder.notificationText.text = currentItem.text
        holder.blueSideTeam.text = currentItem.blueSideTeam
        holder.redSideTeam.text = currentItem.redSideTeam
        holder.blueSideKills.text = currentItem.blueSideKills
        holder.redSideKills.text = currentItem.redSideKills
        holder.blueSideGold.text = currentItem.blueSideGold
        holder.redSideGold.text = currentItem.redSideGold
        holder.blueSideTowers.text = currentItem.blueSideTowers
        holder.redSideTowers.text = currentItem.redSideTowers
        holder.blueSideDragons.text = currentItem.blueSideDragons
        holder.redSideDragons.text = currentItem.redSideDragons

        holder.expandNotificationDetailsButton.setOnClickListener(View.OnClickListener {
            if (holder.notificationDetailsView.visibility == View.VISIBLE) {
                holder.notificationDetailsView.visibility = View.GONE
                holder.expandNotificationDetailsButton.setImageResource(R.drawable.ic_baseline_expand_more_24)
            } else {
                TransitionManager.beginDelayedTransition(holder.notificationCard, AutoTransition())
                holder.notificationDetailsView.visibility = View.VISIBLE
                holder.expandNotificationDetailsButton.setImageResource(R.drawable.ic_baseline_expand_less_24)
            }
        })

        holder.expandNotificationDetailsButton.setImageResource(R.drawable.ic_baseline_expand_more_24)
        holder.notificationDetailsView.visibility = View.GONE
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    fun update(newList: List<Notification>){
        notificationList = newList
        notifyDataSetChanged()
    }

}