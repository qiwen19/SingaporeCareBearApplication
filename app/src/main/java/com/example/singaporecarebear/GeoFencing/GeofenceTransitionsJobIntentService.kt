package com.example.singaporecarebear.GeoFencing

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.firestore.FirebaseFirestore
import java.util.HashMap

class GeofenceTransitionsJobIntentService : JobIntentService() {

    //notification
    private var notificationManager: NotificationManager? = null

    //Shared preference component
    private lateinit var sharePreferenceSettings: SharedPreferences
    private lateinit var requesterId: String


    companion object {
        private const val LOG_TAG = "GeoTrIntentService"

        private const val JOB_ID = 573

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent)
        }
    }

    override fun onHandleWork(intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceErrorMessages.getErrorString(this,
                geofencingEvent.errorCode)
            Log.e(LOG_TAG, errorMessage)
            return
        }

        handleEvent(geofencingEvent)
    }

    private fun handleEvent(event: GeofencingEvent) {
        if (event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            sharePreferenceSettings = applicationContext.getSharedPreferences("savePrefs", 0)
            requesterId = sharePreferenceSettings.getString("requesterId", "")!!

            notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            createNotificationChannel(
//                "Reach Destination",
//                "Reach Destination",
//                "Reach Destinatio")
                Log.e("requester Id geofence", requesterId)
            val db = FirebaseFirestore.getInstance()
            val helperInfo = HashMap<String, Any>()
            helperInfo.put("arrivalStatus", true)
            val requestedLocationDocRef = db.collection("requestLocation").document(requesterId)
            requestedLocationDocRef.update(helperInfo)

            //Remove the requester Id after helping
            sharePreferenceSettings.edit().remove("requesterId").commit()
//            sendSecuredNotification()
        }
    }

    //notification code
//    private fun createNotificationChannel(id: String, name: String,
//                                          description: String) {

        //makes notification pop up
//        val importance = NotificationManager.IMPORTANCE_HIGH
//        val channel = NotificationChannel(id, name, importance)

//        channel.description = description
//        channel.enableLights(true)
//        channel.lightColor = Color.RED
//        channel.enableVibration(true)
//        channel.vibrationPattern =
//            longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
//        notificationManager?.createNotificationChannel(channel)
    }

//    fun sendSecuredNotification() {
//
//        val notificationID = 105
//
//        //send to main activity via the notification
//        val resultIntent = Intent(applicationContext, MapsFragment::class.java)
//        resultIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
//
//        val pendingIntent = PendingIntent.getActivity(
//            applicationContext,
//            0,
//            resultIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT
//        )

        //make sure ID matches or code wont run
//        val channelID = "Reach Destination"
//
//        val notification = Notification.Builder(applicationContext,
//            channelID)
//            .setContentTitle("Destination Reached!")
//            .setContentText("Congratulations, you have reached your destination!")
//            .setSmallIcon(android.R.drawable.ic_dialog_info) //required
//            .setChannelId(channelID)//required
//            .setContentIntent(pendingIntent)
//            .setAutoCancel(true)
//            .setTicker("Notification")
//            .build()

//        notificationManager?.notify(notificationID, notification)
//    }
//}