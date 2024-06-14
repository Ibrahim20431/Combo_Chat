package com.chats.combochat

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.chats.combochat.Adapter.UserAdapter
import com.chats.combochat.databinding.ActivityMainBinding
import com.chats.combochat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database : FirebaseDatabase
    private lateinit var adapter: UserAdapter
    private lateinit var list : ArrayList<User>
    private lateinit var dialog : ProgressDialog
    private lateinit var user : User

    //
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        list = ArrayList()
        adapter = UserAdapter(this, list)

        LoadChats()
    }

    private fun LoadChats(){
        loading()
        //RecyclerView
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = layoutManager
        //Get data from Firebase
        database.reference.child("users")
            .child(FirebaseAuth.getInstance().uid!!)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    user = snapshot.getValue(User::class.java) as User
                }
                override fun onCancelled(error: DatabaseError) {}

            })

        //Set Adapter to Recycler View
        binding.recyclerView.adapter = adapter
        database.reference.child("users")
            .addValueEventListener(object: ValueEventListener{
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    list.clear()
                    for (snap in snapshot.children){
                        user = snap.getValue(User::class.java) as User
                        if (user.id.equals(FirebaseAuth.getInstance().uid)) {
                            list.add(user)
                        }
                    }
                    dialog.dismiss()
                    adapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loading(){
        dialog = ProgressDialog(this@MainActivity)
        dialog.setMessage("Loading..")
        dialog.setCancelable(false)
        dialog.show()
    }

//On Resume
    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database.reference.child("presence")
            .child(currentId!!).setValue("Online")
        dialog.dismiss()
    }

//On Pause
    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database.reference.child("presence")
            .child(currentId!!).setValue("OffLine")
    }
}