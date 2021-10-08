package com.example.singaporecarebear

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

class HelperObserverService: Service() , CoroutineScope by MainScope() {

    //firebase variables
    private lateinit var db: FirebaseFirestore

    //to store shared preference userId
    private var requesterId: String? = null
    //Shared preference component
    private lateinit var sharePreferenceSettings: SharedPreferences

    private lateinit var snapshotObs:ListenerRegistration




    inner class LocalBinder: Binder(){
        //returns the context of random service?
        fun getService(): HelperObserverService= this@HelperObserverService
    }

    //initialise inner class object
    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    private fun  removePolyRouteAndCancelBtn() {
        Log.d("xxx","removePolyRouteAndCancelBtn")
        val intent = Intent("removePolyRouteAndCancelBtn")
        intent.putExtra("removePolyRouteAndCancelBtn", true)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun sendMessage() {
        Log.d("xxx","helper sending intent")
        val intent = Intent("resetMapHelper")
        intent.putExtra("helperResetMap", true)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        Log.d("hhh","message sent")
    }

    private fun deleteRoute() {
        val intent = Intent("deleteRoute")
        intent.putExtra("deleteRoute", true)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    //run the random number generated function here
    override fun onCreate() {
        super.onCreate()
        db = FirebaseFirestore.getInstance()
        sharePreferenceSettings = applicationContext.getSharedPreferences("savePrefs", 0)
        requesterId = sharePreferenceSettings.getString("requesterId", "")
        //key code to get the function to run on repeat
        observeDocumentForUserHelpingOthers(requesterId!!)
    }

    private fun observeDocumentForUserHelpingOthers(requesterId: String){
        val docRef = db.collection("requestLocation").document(requesterId!!)
        snapshotObs = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("error5566", "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                if (snapshot.contains("helpDone"))
                {
                    Log.d("test123", "help done field exists")
                    val isHelpDone = snapshot.data?.getValue("helpDone")
                    if(isHelpDone == true){
                        Log.d("test123","Help is done")
                        val userRefDoc = db.collection("requestLocation").document(requesterId!!)
                        userRefDoc.delete()
                        deleteRoute()
                        sendMessage()
                    }
                }
                else if (!snapshot.exists()) { // if the someone request for help, i help then he cancel. remove polyroute & cancel btn
                    removePolyRouteAndCancelBtn()
                }

            }
        }
    }

    override fun onDestroy() {
        snapshotObs.remove()
        super.onDestroy()
    }
}