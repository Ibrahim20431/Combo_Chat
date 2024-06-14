package com.chats.combochat

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.chats.combochat.Adapter.MessageAdapter
import com.chats.combochat.databinding.ActivityChatBinding
import com.chats.combochat.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.Date

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: MessageAdapter
    private lateinit var messagesList: ArrayList<Message>
    private lateinit var dialog: ProgressDialog
    //Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    //user info
    private lateinit var senderRoom: String
    private lateinit var receiverRoom: String
    private lateinit var senderId: String
    private lateinit var recieverId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        //Firebase Instance
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        //List of Messages
        messagesList = ArrayList()
        //Reciver id & Sender id
        recieverId = intent.getStringExtra("uid").toString()
        senderId = auth.uid.toString()

        //Progress dialog Loading
        loading()

        //get & set user Information. and come back
        userInfo()
        //set User Status
        userStatus()
        //Load Chat Messages
        chatMessages()
        //Send Message
        sendMessage()
        //send Images in chat
        sendImage()
    }

    //get & set user Information. and come back (toolbar)
    private fun userInfo(){
        //get user info
        val name = intent.getStringExtra("name")
        val image = intent.getStringExtra("image")
        //set user info
        binding.userName.text = name
        Glide.with(this@ChatActivity).load(image)
            .placeholder(R.drawable.image_placeholder)
            .into(binding.userImage)
        //back
        binding.backIcon.setOnClickListener{ finish() }
    }

    //set User Status
    private fun userStatus(){
        database.reference.child("presence")
            .child(recieverId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val status = snapshot.getValue(String::class.java)
                        if (status == "offline"){
                            binding.status.visibility = View.GONE
                        }
                        else{
                            binding.status.visibility = View.VISIBLE
                            binding.status.text = status
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    //Load Chat Messages
    private fun chatMessages(){
        senderRoom = senderId + recieverId
        receiverRoom = recieverId + senderId
        adapter = MessageAdapter(this@ChatActivity, messagesList, senderRoom, receiverRoom)

        //Set MessageAdapter to RecyclerView
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this@ChatActivity, LinearLayoutManager.VERTICAL, false)
        binding.chatRecyclerView.adapter = adapter

        database.reference.child("chats")
            .child(senderRoom)
            .child("message")
            .addValueEventListener(object : ValueEventListener{
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    messagesList.clear()
                    for (snap in snapshot.children){
                        val message : Message? = snap.getValue(Message::class.java)
                        message!!.messageId = snap.key!!
                        messagesList.add(message)
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) { }
            })
    }

    //Send Message
    private fun sendMessage(){
        binding.sendBtn.setOnClickListener{
            val messagetext : String = binding.enterMessage.text.toString()
            val date = Date()
            val makeMessage = Message(messagetext, senderId,date.time)

            binding.enterMessage.setText("")
            val randomKey = database.reference.push().key
            val lastMsgObj = HashMap<String,Any>()
            lastMsgObj["lastMsg"] = makeMessage.messageText.toString()
            lastMsgObj["lastMsgTime"] = date.time

            database.reference.child("chats")
                .child(senderRoom)
                .updateChildren(lastMsgObj)
            database.reference.child("chats")
                .child(receiverRoom)
                .updateChildren(lastMsgObj)

            database.reference.child("chats").child(senderRoom)
                .child("messages")
                .child(randomKey!!)
                .setValue(makeMessage)
                .addOnSuccessListener {

                    database.reference.child("chats").child(receiverRoom)
                        .child("messages")
                        .child(randomKey)
                        .setValue(makeMessage)
                        .addOnSuccessListener {  }
                }
        }
    }

    //Add Image to Send in Message
    private fun sendImage(){
        binding.addPhoto.setOnClickListener{
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 25)
        }

        val handler = Handler()
        binding.enterMessage.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                database.reference.child("presence")
                    .child(senderId)
                    .setValue("Typing...")

                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedTyping, 1000)
            }

            var userStoppedTyping = Runnable {
                database.reference.child("presence")
                    .child(senderId)
                    .setValue("Online")
            }
        })

        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    //Progress Dialog for Loading
    private fun loading(){
        dialog = ProgressDialog(this@ChatActivity)
        dialog.setMessage("Uploading Image...")
        dialog.setCancelable(false)
    }

    /////////////////////////////////////////////////////////
    //On Activiy Result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 25) {
            if (data == null) {
                if (data?.data != null) {
                    val selectedImage = data.data
                    val calender = Calendar.getInstance()
                    var chatReference = storage.reference.child("chats")
                        .child(calender.timeInMillis.toString())
                    dialog.show()

                    chatReference.putFile(selectedImage!!)
                        .addOnCompleteListener { task ->
                            dialog.dismiss()
                            if (task.isSuccessful) {
                                chatReference.downloadUrl.addOnSuccessListener { uri ->
                                    val filePath = uri.toString()
                                    val messageText = binding.enterMessage.text.toString()
                                    val date = Date()

                                    val makeMessage = Message(messageText, senderId,date.time)
                                    makeMessage.messageText = "photo"
                                    makeMessage.imageUrl = filePath

                                    binding.enterMessage.setText("")
                                    val randomKey = database.reference.push().key
                                    val lastMsgObj = HashMap<String,Any>()
                                    lastMsgObj["lastMsg"] = makeMessage.messageText.toString()
                                    lastMsgObj["lastMsgTime"] = date.time

                                    database.reference.child("chats")
                                        .child(senderRoom)
                                        .updateChildren(lastMsgObj)
                                    database.reference.child("chats")
                                        .child(receiverRoom)
                                        .updateChildren(lastMsgObj)

                                    database.reference.child("chats").child(senderRoom)
                                        .child("messages")
                                        .child(randomKey!!)
                                        .setValue(makeMessage)
                                        .addOnSuccessListener {

                                            database.reference.child("chats").child(receiverRoom)
                                                .child("messages")
                                                .child(randomKey)
                                                .setValue(makeMessage)
                                                .addOnSuccessListener {  }
                                        }
                                }
                            }
                        }
                }
            }
        }
    }
    /////////////////////////////////////////////////////////
    //On Resume
    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database.reference.child("presence")
            .child(currentId!!).setValue("Online")
    }

    /////////////////////////////////////////////////////////
    //On Pause
    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database.reference.child("presence")
            .child(currentId!!).setValue("OffLine")
    }
}
