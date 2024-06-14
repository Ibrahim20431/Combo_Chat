package com.chats.combochat

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.chats.combochat.databinding.ActivitySetUpProfileBinding
import com.chats.combochat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.Date

class SetupProfileActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySetUpProfileBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseDatabase
    private lateinit var storage : FirebaseStorage
    private lateinit var dialog : ProgressDialog
    private lateinit var selectedImage : Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetUpProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Firebase Instance
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        setListener()

    }

    private fun setListener(){
        binding.profileImage.setOnClickListener{ showImages() }
        binding.setupProfile.setOnClickListener{ setupProfile() }
    }

    //show media Images
    private fun showImages() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        startActivityForResult(intent, 45)
    }

    //Uploading Image and Name to Firebase
    //Image in FirebaseStorage
    //Name in FirebaseDatabase
    private fun setupProfile() {
//        val name : String = binding.profileName.text.toString()
        //ProgressDialog
        loading()

        //select Image
        if (selectedImage != null){
            val referance = storage.reference.child("Profile")
                .child(auth.uid!!)

            referance.putFile(selectedImage).addOnCompleteListener{ task ->

                if (task.isSuccessful){
                    referance.downloadUrl.addOnCompleteListener{ uri ->
                        val imageUrl = uri.toString()
                        val uid = auth.uid.toString()
                        val phone = auth.currentUser!!.phoneNumber.toString()
                        val name = binding.profileName.text.toString()

                        val user = User(uid, name, phone, imageUrl)

                        database.reference
                            .child("users")
                            .child(uid)
                            .setValue(user)

                            .addOnCompleteListener{
                                dialog.dismiss()
                                startActivity(Intent(this@SetupProfileActivity, MainActivity::class.java))
                                finish()
                            }
                    }
                }
                else{
                    val uid = auth.uid.toString()
                    val phone = auth.currentUser!!.phoneNumber.toString()
                    val name = binding.profileName.text.toString()

                    val user = User(uid, name, phone, "No Image")

                    database.reference
                        .child("users")
                        .child(uid)
                        .setValue(user)

                        .addOnCanceledListener{
                            dialog.dismiss()
                            startActivity(Intent(this@SetupProfileActivity, MainActivity::class.java))
                            finish()
                        }
                }
            }
        }
    }

    //ProgressDialog
    private fun loading() {
        dialog = ProgressDialog(this@SetupProfileActivity)
        dialog.setMessage("Building Profile..")
        dialog.setCancelable(false)
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null){
            if (data.data != null){
                val uri = data.data  //file path
                val storage = FirebaseStorage.getInstance()
                val time = Date().time
                val reference = storage.reference.child("Profile")
                    .child(time.toString() + "")

                reference.putFile(uri!!).addOnCompleteListener { task ->

                    if (task.isSuccessful){
                        reference.downloadUrl.addOnCompleteListener{ uri ->
                            val filePath = uri.toString()
                            val obj = HashMap<String,Any>()
                            obj["image"] = filePath

                            database.reference
                                .child("users")
                                .child(auth.uid!!)
                                .updateChildren(obj).addOnSuccessListener {  }
                        }
                    }
                }

                binding.profileImage.setImageURI(data.data)
                selectedImage = data.data!!
            }
        }
    }
}