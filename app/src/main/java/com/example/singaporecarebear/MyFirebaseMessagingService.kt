package com.example.singaporecarebear

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.RemoteMessage.PRIORITY_HIGH

class MyFirebaseMessagingService : FirebaseMessagingService() {

    val TAG = "FirebaseMsgService"

    companion object {
        private val NOTIFICATION_ID = 1
        private val NOTIFICATION_CHANNEL_ID = "help_requesting"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        showNotification(remoteMessage)
    }

    private fun showNotification(remoteMessage: RemoteMessage) {

        val clickAction = remoteMessage.notification!!.clickAction

        // go to notification fragment onclick notification
//        val getToActivity = Intent(this, NotificationActivity::class.java)
        val getToActivity = Intent(clickAction)
        // Get the PendingIntent containing the entire back stack
        val getToPendingIntent = PendingIntent.getActivity(this,
            NOTIFICATION_ID, getToActivity, PendingIntent.FLAG_UPDATE_CURRENT)

        // val requestingUsers = remoteMessage.data["requestingUsers"]
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//        val importance = NotificationManager.PRIORITY_HIGH

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_accept, "ACCEPT", getToPendingIntent)
            .addAction(R.drawable.ic_accept, "DECLINE", getToPendingIntent)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(remoteMessage.data["title"])
            .setContentText(remoteMessage.data["content"])
            .setContentIntent(getToPendingIntent)
            .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)    // important for heads-up notification
            .setAutoCancel(true)
            .setPriority(PRIORITY_HIGH)
            .setSound(soundUri)

        val notification = notificationBuilder.build()
        val context: Context = this
        val notifyManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifyManager.notify(NOTIFICATION_ID, notification)
    }
}