package com.example.singaporecarebear

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope


class RequesterObserverService(): Service() , CoroutineScope by MainScope() {

    //firebase variables
    private lateinit var db: FirebaseFirestore
    var randomNumber: MutableList<Int> = mutableListOf()

    //to store shared preference userId
    private var userId: String? = null
    //Shared preference component
    private lateinit var sharePreferenceSettings: SharedPreferences

    private lateinit var snapshotObs1: ListenerRegistration
    private lateinit var snapshotObs2: ListenerRegistration


    inner class LocalBinder: Binder(){
        //returns the context of random service?
        fun getService(): RequesterObserverService = this@RequesterObserverService
    }

    //initialise inner class object
    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    private fun observeDocumentForRequesterToResetMap(){

        val docRef = db.collection("requestLocation").document(userId!!)
        snapshotObs1 = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("error5566", "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                Log.d("hhh","snapshot not null")
                if (!snapshot.exists()) {
                    Log.d("rrr","ABOUT TO SEND MESSAGE TO UNBIND SERVICE OUT")
                    sendMessage()
                }
            }
        }
    }

    private fun sendMessage() {
        Log.d("rrr","sending intent")
        val intent = Intent("requesterResetMap")
        // add data
        //intent.action=("requesterResetMap")
        intent.putExtra("requesterResetMap", true)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun hideCancelButtonMsg() {
        val intent = Intent("hideCancelButton")
        intent.putExtra("hideCancelButton", true)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }


    //run the random number generated function here
    override fun onCreate() {
        super.onCreate()
        db = FirebaseFirestore.getInstance()
        sharePreferenceSettings = applicationContext.getSharedPreferences("savePrefs", 0)
        userId = sharePreferenceSettings.getString("userId", "")
        //key code to get the function to run on repeat
        Log.d("rrr","in service")
        observeHelperLocationStatus()
        observeDocumentForRequesterToResetMap()
    }

    private fun observeHelperLocationStatus() {
        val docRef = db.collection("requestLocation").document(userId!!)
        Log.d("rrr","userId --> $userId")
        snapshotObs2 = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("error5566", "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                Log.d("rrr","checking arrival status")
                //check if key exists
                if (snapshot.contains("arrivalStatus")) {
                    //if helper has arrived inform user
                    if (snapshot.getBoolean("arrivalStatus") == true) {
                        Log.d("rrr","arrival status is true")
                        hideCancelButtonMsg()
                        val intent = Intent(this?.applicationContext, HelperArrivedActivity::class.java)
                        intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent)
                    }
                }
            } else {
                Log.d("error 5566", "Current data: null")
            }
        }
    }

    override fun onDestroy() {
        Log.d("rrr","requester service has been terminated")
        snapshotObs1.remove()
        snapshotObs2.remove()
        super.onDestroy()
    }
}