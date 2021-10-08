package com.example.singaporecarebear

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

internal class NotiAdapter internal constructor(options: FirestoreRecyclerOptions<Notifications>) :
    FirestoreRecyclerAdapter<Notifications, NotiAdapter.NotiViewHolder>(options) {

    inner class NotiViewHolder constructor(private val view: View) :
        RecyclerView.ViewHolder(view) {
        fun setNotificationInformation(title: String, message: String, helpRequired: String) {
            val Title = view.findViewById<TextView>(R.id.notiTitle)
            val Message = view.findViewById<TextView>(R.id.notiText)
            Title.text = "$title - $helpRequired"
            Message.text = message
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotiViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_noti, parent, false)
        return NotiViewHolder(view)
    }

    override fun onBindViewHolder(
        notiViewHolder: NotiViewHolder,
        position: Int,
        notifications: Notifications
    ) {
        notiViewHolder.setNotificationInformation(notifications.title, notifications.message, notifications.helpRequired)
    }
}