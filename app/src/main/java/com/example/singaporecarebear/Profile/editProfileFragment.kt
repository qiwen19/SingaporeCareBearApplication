package com.example.singaporecarebear.Profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.example.singaporecarebear.R
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_edit_profile.*
import java.io.IOException
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class editProfileFragment : Fragment() {

    //firebase variables
    private lateinit var database: FirebaseFirestore
    private lateinit var userRefDoc: DocumentReference
    private lateinit var storageReference: StorageReference
    private var currentUser: FirebaseUser? = null
    private var myContext: FragmentActivity? = null

    private var checkMe: Int = 0
    private var madeNoEdits:Int =0
    private var userId: String? = null

    //to generate image
    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null

    public override fun onStart() {
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
        val rootView = inflater.inflate(R.layout.activity_edit_profile, container, false)

        //initialize xml objects
//        val profileImageView = findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.profilePicture)
//        val usernameField = findViewById<EditText>(R.id.editTextUsername)
//        val emailField = findViewById<EditText>(R.id.editTextEmail)
        val profilePicture = rootView.findViewById<CircleImageView>(R.id.profilePicture)
        val submitBtn = rootView.findViewById<Button>(R.id.submitBtn)
        val backBtn = rootView.findViewById<Button>(R.id.backBtn)

        //submitBtn.setBackgroundColor(resources.getColor(R.color.blueBtn))

        //Initialise shared preference variables
        val sharePreferenceSettings = activity!!.applicationContext.getSharedPreferences("savePrefs", 0)
        userId = sharePreferenceSettings.getString("userId", "")

        //Initialise firebase variable
        database = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        //Retrieve User details & populate user profile ( Data from both collection & firebase auth)
        userRefDoc = database.collection("users").document(userId!!)
        userRefDoc.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    if(editTextUsername != null){
                        editTextUsername!!.setText(document.data?.getValue("fullname").toString())
                    }
                    if(editTextEmail != null){
                        editTextEmail!!.setText(currentUser!!.email)
                    }
                    if (document.contains("imageUrl")) {
                        Picasso.get().load(document.data?.getValue("imageUrl").toString()).into(profilePicture)
                    }
                    else
                    {
                        profilePicture.setImageResource(R.drawable.no_photo)
                    }
                } else {
                    Log.d("Error", "No such document")
                }
            }

        //editImage
        profilePicture.setOnClickListener()
        {
            launchGallery()
        }

        //edit user account details
        submitBtn.setOnClickListener()
        {
            editAccountDetails() // main coroutine continues here immediately
                Handler().postDelayed({
                    //when it fails, checkME +1
                    //if no failure and checkme =0, direct to new page
                    if (checkMe==0 && madeNoEdits != 3){
                        //val intent = Intent(this, viewProfileFragment::class.java)
                        //getActivity()!!.finish()
                        //startActivity(intent)
                        val fragManager: FragmentManager = activity!!.getSupportFragmentManager();
                        val fragmentTransaction = fragManager.beginTransaction()
                        fragmentTransaction.replace(R.id.fragmentContainer, viewProfileFragment())
                        fragmentTransaction.commit()
                    }
                    else
                    {

                    }

                }, 6000)
        }

        //Back button
        backBtn.setOnClickListener(){
            val fragManager: FragmentManager = activity!!.getSupportFragmentManager();
            val fragmentTransaction = fragManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragmentContainer, viewProfileFragment())
            fragmentTransaction.commit()
        }
        return rootView
    }


    //search for image
    private fun launchGallery() {
        startActivityForResult(
            Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI
            ), PICK_IMAGE_REQUEST
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data == null || data.data == null) {
                return
            }

            filePath = data.data
            try {
                Picasso.get().load(filePath).into(profilePicture)
                uploadImage()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImage(){
        if(filePath != null){
            // stores in firebase STORAGE
            val ref = storageReference.child("uploads/" + UUID.randomUUID().toString())
            val uploadTask = ref.putFile(filePath!!)
            val urlTask = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation ref.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    addUploadRecordToDb(downloadUri.toString())
                } else {
                    Toast.makeText(myContext, "Image uploading failed", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener{
                Toast.makeText(myContext, "Image uploading failed", Toast.LENGTH_SHORT).show()

            }
        }else{
            Toast.makeText(myContext, "Please Upload an Image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addUploadRecordToDb(uri: String){
        val db = FirebaseFirestore.getInstance()
        val data = HashMap<String, Any>()
        data["imageUrl"] = uri
        val docRef = db.collection("users").document(userId!!)
        docRef.update(data).addOnSuccessListener { documentReference ->
            Toast.makeText(myContext, "Image has been successfully saved", Toast.LENGTH_LONG).show()
        }
            .addOnFailureListener { e ->
                Toast.makeText(myContext, "Error saving image", Toast.LENGTH_LONG).show()
            }
    }

    private fun editAccountDetails(){
        var username :String
//        val usernameField = findViewById<EditText>(R.id.editTextUsername)
        checkMe = 0
        madeNoEdits = 0

        userRefDoc = database.collection("users").document(userId!!)
        userRefDoc.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    username = (document.data?.getValue("fullname").toString())
                    if(editTextUsername.text.toString()!= username)
                    {
                        userRefDoc.update("fullname",editTextUsername.text.toString()).addOnFailureListener { e ->
                            val tempToast = Toast.makeText(myContext, "Error updating username", Toast.LENGTH_SHORT)
                            tempToast.setGravity(Gravity.CENTER, 0, 0)
                            tempToast.show()
                            checkMe+=1

                        }
                            .addOnSuccessListener { e ->
                                val tempToast=Toast.makeText(myContext, "Username has been updated", Toast.LENGTH_SHORT)
                                tempToast.setGravity(Gravity.CENTER, 0, 0)
                                tempToast.show()
                            }
                    }
                    else
                    {
                        Log.d("Error5566", "username+1")
                        madeNoEdits +=1
                    }
                }
            }
        var email = (currentUser!!.email)
//        val currentPasswordField = findViewById<EditText>(R.id.editCurrentPassword)
//        val confirmPasswordField = findViewById<EditText>(R.id.editTextConfirmPassword)
//        val newPasswordField = findViewById<EditText>(R.id.editTextNewPassword)
//        val emailField = findViewById<EditText>(R.id.editTextEmail)

        if(editTextEmail.text.toString() != email!! && editTextNewPassword.text.toString() != ""
            && editTextConfirmPassword.text.toString() != "") {
            if (editCurrentPassword.text.toString() == "") {
                val tempToast = Toast.makeText(myContext, "Enter your current password", Toast.LENGTH_SHORT)
                tempToast.setGravity(Gravity.CENTER, 0, 0)
                checkMe+=1
                return tempToast.show()
            } else {
                val pattern: Pattern
                val matcher: Matcher
                val specialCharacters = "-@%\\[\\}+'!/#$^?:;,\\(\"\\)~`.*=&\\{>\\]<_"
                val PASSWORD_REGEX =
                    "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[$specialCharacters])(?=\\S+$).{8,20}$"
                pattern = Pattern.compile(PASSWORD_REGEX)
                matcher = pattern.matcher(editTextNewPassword.text.toString())
                if (!matcher.matches()) {
                    val tempToast = Toast.makeText(
                        myContext,
                        "New password has to meet the requirements specified",
                        Toast.LENGTH_SHORT
                    )
                    tempToast.setGravity(Gravity.CENTER, 0, 0)
                    checkMe+=1
                    return tempToast.show()
                } else if (editTextNewPassword.text.toString() != editTextConfirmPassword.text.toString()) {
                    val tempToast = Toast.makeText(myContext, "New passwords do no match", Toast.LENGTH_SHORT)
                    tempToast.setGravity(Gravity.CENTER, 0, 0)
                    checkMe+=1
                    return tempToast.show()
                } else {
                    val newPassword = editTextNewPassword.text.toString()
                    val credential = EmailAuthProvider
                        .getCredential(email, editCurrentPassword.text.toString())
                    currentUser?.reauthenticate(credential)
                        ?.addOnFailureListener {
                            Toast.makeText(
                                myContext,
                                "current password do not match",
                                Toast.LENGTH_LONG
                            ).show()
                            checkMe+=1;
                        }
                        ?.addOnSuccessListener {
                            currentUser?.updateEmail(editTextEmail.text.toString())
                                ?.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val tempToast = Toast.makeText(
                                            myContext,
                                            "Successfully updated email password",
                                            Toast.LENGTH_SHORT
                                        )
                                        tempToast.setGravity(Gravity.CENTER, 0, 0)
                                        tempToast.show()
                                        currentUser?.updatePassword(newPassword)
                                            ?.addOnCompleteListener { task2 ->
                                                if (task2.isSuccessful) {
                                                    val tempToast2 = Toast.makeText(
                                                        myContext,
                                                        "Successfully update password",
                                                        Toast.LENGTH_SHORT
                                                    )
                                                    tempToast2.setGravity(Gravity.CENTER, 0, 0)
                                                    tempToast2.show()
                                                }
                                            }
                                    }
                                }
                        }
                    return
                }

            }
        }

        //update email
        if(editTextEmail.text.toString() != email){
            if(editCurrentPassword.text.toString()=="")
            {
                val tempToast = Toast.makeText(myContext, "Enter your current password", Toast.LENGTH_SHORT)
                tempToast.setGravity(Gravity.CENTER, 0, 0)
                checkMe+=1
                return tempToast.show()
            }
            else {
                val credential = EmailAuthProvider
                    .getCredential(email!!, editCurrentPassword.text.toString())
                currentUser?.reauthenticate(credential)
                    ?.addOnFailureListener{
                        val tempToast = Toast.makeText(myContext, "current password do not match", Toast.LENGTH_SHORT)
                        tempToast.setGravity(Gravity.CENTER, 0, 0)
                        checkMe+=1
                        tempToast.show()}
                    ?.addOnSuccessListener {
                        currentUser?.updateEmail(editTextEmail.text.toString())
                            ?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    email = editTextEmail.text.toString()
                                    val tempToast = Toast.makeText(myContext, "Successfully updated email", Toast.LENGTH_SHORT)
                                    tempToast.setGravity(Gravity.CENTER, 0, 0)
                                    tempToast.show()
                                } else {
                                    val tempToast = Toast.makeText(myContext, "Error updating email, try using another email address", Toast.LENGTH_SHORT)
                                    tempToast.setGravity(Gravity.CENTER, 0, 0)
                                    checkMe+=1
                                    tempToast.show()
                                }
                            }
                    }

            }
        }
        else
        {
            Log.d("Error5566", "email+1")
            madeNoEdits+=1
        }
        //update password
        if(editTextNewPassword.text.toString() != "" && editCurrentPassword.text.toString() != "" && editTextConfirmPassword.text.toString() != ""){
            val pattern: Pattern
            val matcher: Matcher
            val specialCharacters = "-@%\\[\\}+'!/#$^?:;,\\(\"\\)~`.*=&\\{>\\]<_"
            val PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[$specialCharacters])(?=\\S+$).{8,20}$"
            pattern = Pattern.compile(PASSWORD_REGEX)
            matcher = pattern.matcher(editTextNewPassword.text.toString())
            if(!matcher.matches())
            {
                val tempToast = Toast.makeText(myContext, "New password has to meet the requirements specified", Toast.LENGTH_SHORT)
                tempToast.setGravity(Gravity.CENTER, 0, 0)
                checkMe+=1
                return tempToast.show()
            }
            else if(editTextNewPassword.text.toString() != editTextConfirmPassword.text.toString())
            {
                val tempToast = Toast.makeText(myContext, "New passwords do no match", Toast.LENGTH_SHORT)
                tempToast.setGravity(Gravity.CENTER, 0, 0)
                checkMe+=1
                return tempToast.show()
            }
            val newPassword = editTextNewPassword.text.toString()
            val credential = EmailAuthProvider
                .getCredential(email!!, editCurrentPassword.text.toString())
            // Prompt the user to re-provide their sign-in credentials
            currentUser?.reauthenticate(credential)
                ?.addOnFailureListener {
                    val tempToast = Toast.makeText(myContext, "Current password does not tally", Toast.LENGTH_SHORT)
                    tempToast.setGravity(Gravity.CENTER, 0, 0)
                    checkMe+=1
                    tempToast.show()}
                ?.addOnSuccessListener {
                    currentUser?.updatePassword(newPassword)
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val tempToast = Toast.makeText(myContext, "Successfully update password", Toast.LENGTH_LONG)
                                tempToast.setGravity(Gravity.CENTER, 0, 0)
                                tempToast.show()

                            }
                        }
                }
        }
        else{
            Log.d("Error5566", "pasword+1")
            madeNoEdits+=1
        }

    }



}