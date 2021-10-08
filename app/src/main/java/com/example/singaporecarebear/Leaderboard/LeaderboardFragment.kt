package com.example.singaporecarebear.Leaderboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.singaporecarebear.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_leaderboard.*


class LeaderboardFragment : Fragment() {
    //TODO declare the database content as an ArrayList?
    var imageURL = ArrayList<String>()
    var userNameStorage = ArrayList<String>()
    var pointsStorage = ArrayList<String>()
    var highlightCurrentUserView = ArrayList<String>()

    //firebase variables
    private lateinit var db: FirebaseFirestore
    private lateinit var userRefDoc: DocumentReference

    //firebase auth user
    private var currentUser: FirebaseUser? = null

    //sharedpreference userid
    private var userId: String? = null

    //TODO implement the adapter for the recyclerview IN A SEPARATE CLASS
    private lateinit var viewManager: LinearLayoutManager
    private lateinit var viewAdapter: LeaderboardAdapter

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        val rootView = inflater.inflate(R.layout.activity_leaderboard, container, false)
        //setContentView(R.layout.activity_leaderboard)

        //Initialise shared preference variables
        val sharePreferenceSettings =  activity!!.applicationContext.getSharedPreferences("savePrefs", 0)
        userId = sharePreferenceSettings.getString("userId", "")

        //Initialise firebase variable
        db = FirebaseFirestore.getInstance()


        //UPDATE ARRAYLIST WITH DATA FROM FIREBASE
        //readData function call back function to populate recycler view and initialize observe
        //readData({generateRecyclerViewAndObserveFireBase()})
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewManager = LinearLayoutManager(this.context)
        viewAdapter =
            LeaderboardAdapter(userNameStorage, imageURL, pointsStorage, highlightCurrentUserView)

        leaderboardRecyclerView.apply {
            layoutManager = viewManager
            adapter = viewAdapter
            //observes the firebase for new data and updates the recycler view accordingly
            observeFirebase()
        }
    }

    private fun observeFirebase(){
        val docRef = db.collection("users").orderBy("points", Query.Direction.DESCENDING).limit(10)
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("error5566", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                readData { viewAdapter.updateLeaderboardData(userNameStorage,imageURL,pointsStorage,highlightCurrentUserView)}
            } else {
                Log.d("error 5566", "Current data: null")
            }
        }
    }

    fun readData(myCallback: () -> Unit) {
        //observer
        val usersCollection = db.collection("users")
        usersCollection.orderBy("points", Query.Direction.DESCENDING).limit(10)
            .get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                //reset the list of data other recycler view will have duplicating data
                //it will keep adding existing records into the array that will populate the recycler view because of the
                userNameStorage = ArrayList<String>()
                imageURL = ArrayList<String>()
                pointsStorage= ArrayList<String>()
                highlightCurrentUserView = ArrayList<String>()
                for (document in task.result!!) {
                    //highlight this view
                    if(currentUser!!.uid ==  document.data.getValue("id").toString()) {
                        userNameStorage.add(document.data.getValue("fullname").toString())
                        pointsStorage.add(document.data.getValue("points").toString())
                        highlightCurrentUserView.add("user")
                        if (document.contains("imageUrl")) {
                            imageURL.add(document.data.getValue("imageUrl").toString())
                        } else {
                            imageURL.add("null")
                        }
                    }
                    else {
                        userNameStorage.add(document.data.getValue("fullname").toString())
                        pointsStorage.add(document.data.getValue("points").toString())
                        highlightCurrentUserView.add("null")
                        if (document.contains("imageUrl")) {
                            imageURL.add(document.data.getValue("imageUrl").toString())
                        } else {
                            imageURL.add("null")
                        }
                    }
                }
                myCallback()
            }
        }
    }
}