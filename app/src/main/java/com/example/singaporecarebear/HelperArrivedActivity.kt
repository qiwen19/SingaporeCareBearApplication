package com.example.singaporecarebear

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_helper_arrived.*

class HelperArrivedActivity : AppCompatActivity(){

    //firebase variables
    private lateinit var db: FirebaseFirestore

    //sharedpreference userid
    private var userId: String? = null
    private var requesterId: String? = null
    private var helperID: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_helper_arrived)

        //Initialise firebase variable
        db = FirebaseFirestore.getInstance()

        //Initialise shared preference variables
        val sharePreferenceSettings = getSharedPreferences("savePrefs", 0)
        userId = sharePreferenceSettings.getString("userId", "")
        //requesterId = sharePreferenceSettings.getString("requesterId", "")

        var usernameValue = findViewById<TextView>(R.id.userNameValue)
        var infoMsg = findViewById<TextView>(R.id.informationMsg)
        var helperImage = findViewById<ImageView>(R.id.helperImage)
        var completeBtn = findViewById<Button>(R.id.completeBtn)
        //completeBtn.setBackgroundColor(resources.getColor(R.color.blueBtn))

        //get helper details
        val userRefDoc = db.collection("requestLocation").document(userId!!)
        userRefDoc.get()
            .addOnSuccessListener { document ->
                if (document.contains("helperId")) {
                    helperID = document.data?.getValue("helperId").toString()
                    //get helper id details
                    val helperRefDoc = db.collection("users").document(helperID)
                    Log.d("testing","helperID --> $helperID")
                    helperRefDoc.get()
                        .addOnSuccessListener { document2 ->
                            val helperName = document2.data?.getValue("fullname").toString()
                            val helperImg = document2.data?.getValue("imageUrl").toString()
                            Log.d("testing",helperName)
                            usernameValue.text = helperName
                            infoMsg.text = getString(R.string.infoMsg, helperName)
                            Picasso.get().load(helperImg)
                                .into(helperImage)
                        }
                }
            }

        completeBtn.setOnClickListener() {
            Log.d("helerId", "ARLO")
            db.collection("requestLocation").document(userId!!)
                .update("helpDone",true,"arrivalStatus",false)
                .addOnSuccessListener {
                     db.collection("users").document(helperID!!)
                        .update("points", FieldValue.increment(99))
                        .addOnSuccessListener {
                            Log.d("test123", "points have been added")
                            //reset map fragment at main activity ( so marker will be deleted)
                            // Reload current fragment
                            Log.d("test123", "reset fragment")

                            //val intent = Intent(this, MainActivity::class.java)
                            //startActivity(intent)

                            finish()
                        }
                        .addOnFailureListener {
                            Log.d("test123", "failed to add points")
                        }
                }

        }
    }
}
