package com.example.singaporecarebear

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.singaporecarebear.Profile.viewProfileFragment
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_notification.*

class NotificationActivity : AppCompatActivity() {

    private val mCurrentId = FirebaseAuth.getInstance().uid

    private var adapter: NotiAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        //actionbar
        val actionbar = supportActionBar
        //set actionbar title
        actionbar!!.title = "Notifications"
        //set back button
        actionbar.setDisplayHomeAsUpEnabled(true)

        // Initialise recycler view
        recyclerViewNotifications.layoutManager = LinearLayoutManager(this)
        val notiRef = FirebaseFirestore.getInstance()
        val query = notiRef.collection("users/$mCurrentId/Notifications").orderBy("status", Query.Direction.ASCENDING)

        val options = FirestoreRecyclerOptions.Builder<Notifications>().setQuery(query, Notifications::class.java).build()

        adapter = NotiAdapter(options)
        recyclerViewNotifications.adapter = adapter

    }

    override fun onStart() {
        super.onStart()
        adapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()

        if (adapter != null) {
            adapter!!.stopListening()
        }
    }

    // Back Action Bar onPressed
    override fun onSupportNavigateUp(): Boolean {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        return true
    }
}
