package com.chats.combochat.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chats.combochat.ChatActivity
import com.chats.combochat.R
import com.chats.combochat.databinding.PersonItemBinding
import com.chats.combochat.model.User

class UserAdapter(var context : Context, var userList : ArrayList<User>)
    : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

        //2
    class UserViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val binding : PersonItemBinding = PersonItemBinding.bind(itemView)
    }
    //1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.person_item, parent, false)
        return UserViewHolder(view)
    }
    //3
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.binding.personName.text = user.name

        Glide.with(context).load(user.profileImage)
            .placeholder(R.drawable.person)
            .into(holder.binding.personImage)

        holder.itemView.setOnClickListener{
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("uid", user.id)
            intent.putExtra("name", user.name)
            intent.putExtra("image", user.profileImage)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = userList.size
}