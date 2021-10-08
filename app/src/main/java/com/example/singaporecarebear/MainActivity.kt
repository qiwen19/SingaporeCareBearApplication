
package com.example.singaporecarebear

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.singaporecarebear.Leaderboard.LeaderboardFragment
import com.example.singaporecarebear.Profile.viewProfileFragment
import com.example.singaporecarebear.ui.login.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {

    // FireBase instance variables
    private var mFireBaseAuth: FirebaseAuth? = null
    private var mFireBaseUser: FirebaseUser? = null
    private var mMenuItemSelected: Int? = null

    // Bottom Navigation Component
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener{ item ->
        when(item.itemId){
            R.id.map -> {
                replaceFragment(MapsFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.leaderboard -> {
                replaceFragment(LeaderboardFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.profile -> {
                replaceFragment(viewProfileFragment())
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize FireBase Auth
        mFireBaseAuth = FirebaseAuth.getInstance()

        if(mFireBaseAuth!!.currentUser == null){
            super.onCreate(savedInstanceState);
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else{
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            // initialise bottom tab navigation onclick
            bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
            replaceFragment(MapsFragment())

            this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            val actionbar: ActionBar? = supportActionBar
            actionbar!!.setBackgroundDrawable(ColorDrawable(Color.rgb(30,64,114)))

        }
    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment)
        fragmentTransaction.addToBackStack("tag").commit()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 1){
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            val bottomNavigationView =
                this@MainActivity.findViewById(R.id.bottomNavigation) as BottomNavigationView
            bottomNavigationView.selectedItemId = R.id.map
        }
        else{
            finish();
            exitProcess(0);
        }
    }

}