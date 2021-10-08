package com.example.singaporecarebear.Profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.singaporecarebear.R
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import java.io.IOException
import java.util.*

class PersonalDocumentActivity : AppCompatActivity() {

    //firebase variables
    private lateinit var database: FirebaseFirestore
    private lateinit var userRefDoc: DocumentReference
    private lateinit var storageReference: StorageReference

    //to generate image
    private val PICK_IMAGE_REQUEST = 71
    private var filePath: Uri? = null

    //sharedpreference userid
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_document)

        //initialise xml variables
        var personalDocumentImageView = findViewById<ImageView>(R.id.personalDocumentImageView)
        var uploadBtn = findViewById<Button>(R.id.uploadBtn)
        var submitDocumentBtn = findViewById<Button>(R.id.submitDocumentBtn)

        //change button color
        submitDocumentBtn.setBackgroundColor(resources.getColor(R.color.blueBtn))
        uploadBtn.setBackgroundColor(resources.getColor(R.color.blueBtn))

        //Initialise shared preference variables
        val sharePreferenceSettings = getSharedPreferences("savePrefs", 0)
        userId = sharePreferenceSettings.getString("userId", "")

        //Initialise firebase variable
        database = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        //Retrieve User details & populate user profile ( Data from both collection & firebase auth)
        userRefDoc = database.collection("users").document(userId!!)
        userRefDoc.get()
            .addOnSuccessListener { document ->
                if (document.contains("personalDocuments")) {
                    uploadBtn.text = "Override existing document"
                    Picasso.get().load(document.data?.getValue("personalDocuments").toString()).fit().centerCrop().into(personalDocumentImageView)
                }
                else
                {
                    personalDocumentImageView.setImageResource(R.drawable.no_photo)
                }
            }

        //select image
        submitDocumentBtn.setOnClickListener()
        {
            launchGallery()
        }

        uploadBtn.setOnClickListener{
            uploadImage()
        }

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
                val personalDocumentImageView= findViewById<ImageView>(R.id.personalDocumentImageView)
                Picasso.get().load(filePath).fit().centerCrop().into(personalDocumentImageView)
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
                    Toast.makeText(this, "Image uploading failed", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener{
                Toast.makeText(this, "Image uploading failed", Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(this, "Please Upload an Image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addUploadRecordToDb(uri: String){
        val db = FirebaseFirestore.getInstance()
        val data = HashMap<String, Any>()
        data["personalDocuments"] = uri
        val docRef = db.collection("users").document(userId!!)
        docRef.update(data).addOnSuccessListener { documentReference ->
            Toast.makeText(this, "Image has been successfully saved", Toast.LENGTH_LONG).show()
        }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving image", Toast.LENGTH_LONG).show()
            }
    }


}