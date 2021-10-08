package com.example.singaporecarebear.ui.login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import com.example.singaporecarebear.ForgetPasswordActivity
import com.example.singaporecarebear.MainActivity
import com.example.singaporecarebear.R
import com.example.singaporecarebear.RegisterActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId

class LoginActivity : AppCompatActivity() {

    // globalise shared preferences
    val sharePrefs = R.string.savePrefs
    var prefs: SharedPreferences? = null

    // global variables
    private var email: String? = null
    private var password: String? = null
    private var userId: String? = null
    private var user: FirebaseUser? = null

    // UI elements
    private var textViewForgotPassword: TextView? = null
    private var etEmail: EditText? = null
    private var etPassword: EditText? = null
    private var btnLogin: Button? = null
    private var btnCreateAccount: TextView? = null
    private var loginProgressBar: ProgressBar? = null

    // FireBase references
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // initialise share preferences
        prefs = this.getSharedPreferences(sharePrefs.toString(), 0)

        initialise()
//        if(FirebaseAuth.getInstance().currentUser != null){
//            updateUI()
//        }
    }

    private fun initialise() {
        textViewForgotPassword = findViewById<View>(R.id.forgetPasswordTextView) as TextView
        etEmail = findViewById<View>(R.id.email) as EditText
        etPassword = findViewById<View>(R.id.password) as EditText
        btnLogin = findViewById<View>(R.id.login) as Button
        btnCreateAccount = findViewById<View>(R.id.register) as TextView
        loginProgressBar = findViewById(R.id.loginProgressBar)
        mAuth = FirebaseAuth.getInstance()

        // onclick forget password
        textViewForgotPassword!!
            .setOnClickListener {
                startActivity(
                    Intent(
                        this,
                        ForgetPasswordActivity::class.java
                    )
                )
            }

        // onclick register
        btnCreateAccount!!
            .setOnClickListener {
                startActivity(
                    Intent(
                        this@LoginActivity,
                        RegisterActivity::class.java
                    )
                )
            }

        // onclick login
        btnLogin!!.setOnClickListener { loginUser() }
    }

    private fun loginUser() {
        initialise()

        loginProgressBar!!.isVisible = true

        email = etEmail?.text.toString().trim()
        password = etPassword?.text.toString().trim()

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            mAuth!!.signInWithEmailAndPassword(email!!, password!!)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        val currentUser = mAuth!!.currentUser
                        val currentId = currentUser!!.uid
                        val db = FirebaseFirestore.getInstance()

                        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener{
                            // Get new Instance ID token
                            val tokenId = it.result!!.token

                            val tokenMap = HashMap<String, Any>()
                            tokenMap.put("token_id", tokenId)

                            db.collection("users").document(currentId).update(tokenMap).addOnSuccessListener {
                                Log.d("TOKEN INSERT STATUS", "successful insert token")
                                loginProgressBar!!.isVisible = false
                                updateUI()
                                user = FirebaseAuth.getInstance().currentUser
                                userId = user!!.uid
                                saveLoginUserCredential(userId.toString(), email!!)
                            }.addOnFailureListener{
                                loginProgressBar!!.isVisible = false
                                Log.d("TOKEN INSERT STATUS", "Failure to insert token")
                            }
                        }.addOnFailureListener {
                            Log.d("TAG", "getIdTokenError")
                        }
                    } else {
                        loginProgressBar!!.isVisible = false
                        Toast.makeText(
                            this, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            loginProgressBar!!.isVisible = false
            Toast.makeText(this, "Enter all details", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI() {
        val intent = Intent(this, MainActivity::class.java)
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) --> causing error where user A creates a request and logout and login as user B, when user B create request and cancel it results in error
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    // Check user login status
    private fun saveLoginUserCredential(userId: String, userEmail: String){
        // Initialise editor
        val settings = getSharedPreferences("savePrefs", 0)
        val editor = settings.edit()
        editor.putString("userId", userId)
        editor.putString("userEmail", userEmail)

        editor.commit()
    }
}
