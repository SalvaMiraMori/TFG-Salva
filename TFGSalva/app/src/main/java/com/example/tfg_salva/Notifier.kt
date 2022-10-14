package com.example.tfg_salva

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.*
import kotlin.Comparator

class Notifier(val context: Context) {

    private val compareNotificationByPostTime : Comparator<StatusBarNotification>
    private lateinit var notificationManager : NotificationManager

    init {
        createNotificationChannel()
        compareNotificationByPostTime = Comparator<StatusBarNotification> { o1, o2 ->
            return@Comparator (o1.postTime).compareTo(o2.postTime)
        }
    }


    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channelName = context.getString(R.string.channelName)
            val channelDescription = context.getString(R.string.channelDescription)
            val channelId = channelName
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply{
                description = channelDescription
            }
            notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun notification(title: String, content: String) : Notification {
        val bigTextStyle = NotificationCompat.BigTextStyle().bigText(content)

        val builder = NotificationCompat.Builder(context, context.getString(R.string.channelName))
        builder.setSmallIcon(androidx.loader.R.drawable.notification_bg)
        builder.setContentTitle(title)
        builder.setStyle(bigTextStyle)
        builder.setContentText(content)
        builder.priority = NotificationCompat.PRIORITY_HIGH
        return builder.build()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun sendNotification(title: String, content: String){
        val notification = notification(title, content)

        val currentNotifications = notificationManager.activeNotifications

        if(currentNotifications.size >= 50){
            Collections.sort(currentNotifications.toList(), compareNotificationByPostTime)
            notificationManager.cancel(currentNotifications.first().tag,
                currentNotifications.first().id)
        }

        with(NotificationManagerCompat.from(context)){
            val notificationId = title.hashCode()
            notify(notificationId, notification)
        }
    }

}