package com.example.singaporecarebear.Leaderboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.singaporecarebear.R
import com.squareup.picasso.Picasso
import java.security.AccessController.getContext

class LeaderboardAdapter(private var userNameStorage: ArrayList<String>, private var imageURL: ArrayList<String>,
                         private var pointsStorage: ArrayList<String>, private var highlightCurrentUserView: ArrayList<String>) :
    RecyclerView.Adapter<LeaderboardAdapter.leaderboardViewHolder>(){

    //used to search for CheckTextView under layout page
    class leaderboardViewHolder(itemView: View, adapter: LeaderboardAdapter):
        RecyclerView.ViewHolder(itemView){
        val profileImageView = itemView.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.profileImageView)!!
        val usernameField = itemView.findViewById<TextView>(R.id.usernameField)!!
        val pointsField = itemView.findViewById<TextView>(R.id.pointsField)!!
        val positionField = itemView.findViewById<TextView>(R.id.positionField)!!
        val recyclerViewItem = itemView.findViewById<LinearLayout>(R.id.recyclerViewItem)!!
    }

    //creates the view to be returned
    //container = variable name
    override fun onCreateViewHolder(
        container: ViewGroup,
        viewType: Int
    ): leaderboardViewHolder {
        val view = LayoutInflater.from(container.context).inflate(R.layout.recyclerview_leaderboard_list, container,false) as View
        return leaderboardViewHolder(view,this)
    }

    //binds the newly generated information to view
    override fun onBindViewHolder(holder: leaderboardViewHolder, position: Int) {
        //prevents incorrect checkbox from being marked
        holder.setIsRecyclable(false)
        //the text that is being inputted into <CheckTextView> is determined by groceryList[Position]
        //update image

        if(highlightCurrentUserView[position].equals("user")){
            holder.recyclerViewItem.setBackgroundResource(R.drawable.current_user_border)
        }
        else
        {
            holder.recyclerViewItem.setBackgroundResource(R.drawable.border)
        }

        if(imageURL[position] =="null"){
            holder.profileImageView.setImageResource(R.drawable.no_photo)
        }
        else
        {
            Picasso.get().load(imageURL[position]).fit().centerCrop().into(holder.profileImageView)
        }
        holder.usernameField.text = "Name: " + userNameStorage[position]
        holder.pointsField.text = "Points: " + pointsStorage[position]
        holder.positionField.text = "#" + (position+1).toString()
    }

    // to determine number of items in groceryList array
    override fun getItemCount(): Int {
        return userNameStorage.size
    }

    internal fun updateLeaderboardData(userNameStorage: ArrayList<String>,imageURL: ArrayList<String>,pointsStorage: ArrayList<String>,highlightCurrentUserView: ArrayList<String>) {
        this.userNameStorage = userNameStorage
        this.imageURL =imageURL
        this.pointsStorage = pointsStorage
        this.highlightCurrentUserView = highlightCurrentUserView
        //Collections.reverse(this.stations); //reverse the order of the list
        notifyDataSetChanged() //needed to inform the recycler view to update
    }

}


