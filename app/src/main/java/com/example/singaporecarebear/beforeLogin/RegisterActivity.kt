package com.example.singaporecarebear

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.view.View
import android.widget.TextView
import com.example.singaporecarebear.ui.login.LoginActivity
import java.util.regex.Matcher
import java.util.regex.Pattern


class RegisterActivity : AppCompatActivity() {

    // UI elements
    private var etFullName: EditText? = null
    private var etPassword: EditText? = null
    private var etConfirmPassword: EditText? = null
    private var etEmail: EditText? = null
    private var etDob: EditText? = null
    private var registerBtn: Button? = null
    var registerErrorMsg: TextView? = null

    // FireBase references
//    private var mDatabaseReference: DatabaseReference? = null
//    private var mDatabase: FirebaseDatabase? = null
    private var mAuth: FirebaseAuth? = null

    // global variable
    private var fullName: String? = null
    private var password: String? = null
    private var confirmPassword: String? = null
    private var email: String? = null
    private var dob: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initialise()

        //actionbar
        val actionbar = supportActionBar
        //set actionbar title
        actionbar!!.title = "Register"
        //set back button
        actionbar.setDisplayHomeAsUpEnabled(true)

    }

    private fun initialise(){
        etFullName = findViewById<View>(R.id.nameEditText) as EditText
        etPassword = findViewById<View>(R.id.passwordEditText) as EditText
        etConfirmPassword = findViewById<View>(R.id.confirmPasswordEditText) as EditText
        etEmail = findViewById<View>(R.id.emailEditText) as EditText
        etDob = findViewById<View>(R.id.DOBEditText) as EditText
        registerBtn = findViewById<View>(R.id.registerButton) as Button

        registerBtn!!.setOnClickListener{createNewAccount()}

//        mDatabase = FirebaseDatabase.getInstance()
//        mDatabaseReference = mDatabase!!.reference.child("users")
        mAuth = FirebaseAuth.getInstance()
    }

    private fun createNewAccount(){
        //Get elements
        fullName = etFullName?.text.toString().trim()
        password = etPassword?.text.toString().trim()
        confirmPassword = etConfirmPassword?.text.toString().trim()
        email = etEmail?.text.toString().trim()
        dob = etDob?.text.toString().trim()

        registerErrorMsg = this.findViewById(R.id.registerErrorMsg)

        var validationFlag = false

        if(fullName!!.isEmpty()){
            registerErrorMsg!!.text="Please fill in your name"
            Toast.makeText(this, "Please fill in your name", Toast.LENGTH_LONG).show()
            validationFlag = true
        }
        else if(password!!.isEmpty()){
            registerErrorMsg!!.text = "Please fill in your password!"
            Toast.makeText(this, "Please fill in your password!", Toast.LENGTH_LONG).show()
            validationFlag = true
        }
        else if(password!!.isNotEmpty()){
            val pattern: Pattern
            val matcher: Matcher
            val specialCharacters = "-@%\\[\\}+'!/#$^?:;,\\(\"\\)~`.*=&\\{>\\]<_"
            val PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[$specialCharacters])(?=\\S+$).{8,20}$"
            pattern = Pattern.compile(PASSWORD_REGEX)
            matcher = pattern.matcher(password)
            if(!matcher.matches()){
                registerErrorMsg!!.text = "Password has to contain at least one special character, one digit, one alphabet, one capital letter and at least 8 characters long!"
                val toast = Toast.makeText(this, "Password has to contain at least one special character, one digit, one alphabet, one capital letter and at least 8 characters long!", Toast.LENGTH_LONG)
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                validationFlag = true
            }
            else if(confirmPassword!!.isEmpty() || confirmPassword!!.length < 8){
                registerErrorMsg!!.text = "Please fill in your confirm password!"
                Toast.makeText(this, "Please fill in your confirm password!", Toast.LENGTH_LONG).show()
                validationFlag = true
            }
            else if(password != confirmPassword){
                registerErrorMsg!!.text ="Confirm Password does not match!"
                Toast.makeText(this, "Confirm Password does not match!", Toast.LENGTH_LONG).show()
                validationFlag = true
            }
            else if(email!!.isEmpty()){
                registerErrorMsg!!.text = "Please fill in your email!"
                Toast.makeText(this, "Please fill in your email!", Toast.LENGTH_LONG).show()
                validationFlag = true
            }
            else if(!Patterns.EMAIL_ADDRESS.matcher(email!!).matches()){
                registerErrorMsg!!.text = "Please fill in a valid email!"
                Toast.makeText(this, "Please fill in a valid email!", Toast.LENGTH_LONG).show()
                validationFlag = true
            }
            else if(dob!!.isEmpty()){
                registerErrorMsg!!.text = "Please fill in your dob!"
                Toast.makeText(this, "Please fill in your dob!", Toast.LENGTH_LONG).show()
                validationFlag = true
            }
        }
        if(validationFlag == false){
            mAuth!!.createUserWithEmailAndPassword(email!!,password!!)
                .addOnCompleteListener(this) { task ->
                    if(task.isSuccessful){
                        Toast.makeText(this, "Account created successfully", Toast.LENGTH_LONG).show()
                        val userId = mAuth!!.currentUser!!.uid

                        val db = FirebaseFirestore.getInstance()
                        val user = HashMap<String, Any>()
                        user.put("id", userId)
                        user.put("fullname", fullName!!)
                        user.put("dob", dob!!)
                        user.put("points" ,0)
                        db.collection("users").document(userId).set(user)

                        // Verify Email
                        verifyEmail()

                        updateUserInfoAndUI()
                    }
                }.addOnFailureListener{
                    Toast.makeText(this, "Failed to create user: ${ it.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
    private fun updateUserInfoAndUI() {
        //start next activity
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
    private fun verifyEmail() {
        val mUser = mAuth!!.currentUser
        mUser!!.sendEmailVerification()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this,
                        "Verification email sent to " + mUser.email,
                        Toast.LENGTH_SHORT).show()
                } else {
//                    Log.e(TAG, "sendEmailVerification", task.exception)
                    Toast.makeText(this,
                        "Failed to send verification email.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}