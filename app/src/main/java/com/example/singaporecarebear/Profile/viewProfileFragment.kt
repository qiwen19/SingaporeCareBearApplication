package com.example.singaporecarebear.Profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.example.singaporecarebear.R
import com.example.singaporecarebear.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_view_profile.*
import java.time.LocalDateTime


class viewProfileFragment : Fragment() {

    private lateinit var userRefDoc: DocumentReference
    private var currentUser: FirebaseUser? = null
    private var myContext: FragmentActivity? = null

    // FireBase instance variables
    private lateinit var mAuth: FirebaseAuth

    // UI elements
    private var textViewFullName: TextView? = null
    private var textViewEmail: TextView? = null
    private var textViewDOB: TextView? = null

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser
    }

    override fun onAttach(activity: Activity) {
        myContext = activity as FragmentActivity
        super.onAttach(activity)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        val rootView = inflater.inflate(R.layout.activity_view_profile, container, false)

        //initalize xml objects
        val editBtn = rootView.findViewById<Button>(R.id.editBtn)
        val logoutBtn = rootView.findViewById<Button>(R.id.logoutBtn)

        //Initialise variables
        val sharePreferenceSettings = activity!!.applicationContext.getSharedPreferences("savePrefs", 0)
        val userId = sharePreferenceSettings.getString("userId", "")
        val db = FirebaseFirestore.getInstance()

        //Retrieve User details & populate user profile
        userRefDoc = db.collection("users").document(userId!!)
        userRefDoc.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    if(userNameValue != null){
                        userNameValue!!.setText(document.data?.getValue("fullname").toString())
                    }
                    if(emailValue != null){
                        emailValue!!.setText((currentUser!!.email))
                    }
                    if(ageValue != null){
                        if(document.data?.getValue("dob")!=null){
                            var delimiter = "/"
                            val dob = (document.data?.getValue("dob").toString()).split(delimiter)
                            val age = 2020- dob[2].toInt()
                            ageValue!!.setText(age.toString())
                        }
                    }
                    if(pointsValue != null){
                        pointsValue.setText(document.data?.getValue("points").toString())
                    }
                    if(userNameValue != null){
                        userNameValue.setText(document.data?.getValue("fullname").toString())
                    }
                    if (document.contains("imageUrl")) {
                        if(imageView2 != null){
                            Picasso.get().load(document.data?.getValue("imageUrl").toString())
                                .into(imageView2)
                        }
                    } else {
                        imageView2.setImageResource(R.drawable.no_photo);
                    }
                } else {
                    Log.d("Error", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("test123", "get failed with ", exception)
            }

        editBtn.setOnClickListener {view ->
            val fragManager: FragmentManager = myContext!!.supportFragmentManager;
            val fragmentTransaction = fragManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragmentContainer, editProfileFragment())
            fragmentTransaction.commit()

//            val intent = Intent (this.myContext, editProfileFragment::class.java)
//            startActivity(intent)
        }

        logoutBtn.setOnClickListener() {

            mAuth = FirebaseAuth.getInstance()
            val currentUser = mAuth.currentUser
            val currentId = currentUser!!.uid

            val tokenMapRemove = HashMap<String, Any>()
            tokenMapRemove.put("token_id", FieldValue.delete())
            db.collection("users").document(currentId).update(tokenMapRemove).addOnSuccessListener {
                Log.d("TOKEN REMOVE STATUS", "successful remove token")
                mAuth.signOut()

                //Remove shared user shared preference
                sharePreferenceSettings.edit().remove("userId").apply()
                sharePreferenceSettings.edit().remove("userEmail").apply()

                val intent = Intent (activity, LoginActivity::class.java)
                startActivity(intent)
            }.addOnFailureListener{
                Log.d("TOKEN REMOVE STATUS", "Failure to remove token")
            }
            /*
            mAuth = FirebaseAuth.getInstance()
            mAuth!!.signOut()

            //Remove shared user shared preference
            val sharePreferenceSettings = activity!!.applicationContext.getSharedPreferences("savePrefs", 0)
            sharePreferenceSettings.edit().remove("userId").commit()
            sharePreferenceSettings.edit().remove("userEmail").commit()

            val intent = Intent (activity, LoginActivity::class.java)
            startActivity(intent)
             */
        }
        return rootView
    }
//    override fun onRestart() {
//        super.onRestart()
//        this.recreate()
//    }

}