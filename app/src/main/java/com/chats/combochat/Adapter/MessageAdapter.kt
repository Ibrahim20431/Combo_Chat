package com.chats.combochat.Adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chats.combochat.R
import com.chats.combochat.databinding.DeleteLayoutBinding
import com.chats.combochat.databinding.ReceiveMessageBinding
import com.chats.combochat.databinding.SendMessageBinding
import com.chats.combochat.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MessageAdapter(var context: Context, var messages: ArrayList<Message>, var senderRoom: String, var receiverRoom: String)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

//    lateinit var messages1: ArrayList<Message>
    val ITEM_SEND = 1
    val ITEM_RECEIVE = 2
//    lateinit var senderRoom1: String
//    lateinit var receiverRoom1: String

    inner class SendMessageVH(itemView : View): RecyclerView.ViewHolder(itemView){
        val binding : SendMessageBinding = SendMessageBinding.bind(itemView)
    }

    inner class ReceiveMessageVH(itemView : View): RecyclerView.ViewHolder(itemView){
        val binding : ReceiveMessageBinding = ReceiveMessageBinding.bind(itemView)
    }

//    init {
//        if(messages != null){
//            this.messages1 = messages
//        }
//        this.senderRoom1 = senderRoom
//        this.receiverRoom1 = receiverRoom
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == ITEM_SEND){
            val view = LayoutInflater.from(context).inflate(R.layout.send_message, parent, false)
            SendMessageVH(view)
        }
        else{
            val view = LayoutInflater.from(context).inflate(R.layout.receive_message, parent, false)
            ReceiveMessageVH(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (FirebaseAuth.getInstance().uid == message.senderId){ ITEM_SEND }
        else{ ITEM_RECEIVE }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder.javaClass == SendMessageVH::class.java){
            val viewHolder = holder as SendMessageVH
            //with send photo
            if (message.messageText.equals("photo")){
                viewHolder.binding.messageImage.visibility = View.VISIBLE
                viewHolder.binding.messageText.visibility = View.GONE
                viewHolder.binding.messagetextLayout.visibility = View.GONE
                Glide.with(context)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.image_placeholder)
                    .into(viewHolder.binding.messageImage)
            }
            //Without send photo
            viewHolder.binding.messageText.text = message.messageText

            //long clicked in message for deleting
            viewHolder.itemView.setOnLongClickListener {
                //inflate delete layout
                val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                val binding = DeleteLayoutBinding.bind(view)

                //build dialog
                val dialog = AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setView(binding.root)
                    .create()

                //on clicked in delete for me
                binding.deleteForMe.setOnClickListener{

                    message.messageId.let { its ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(its!!)
                            .setValue(null)
                    }
                    dialog.dismiss()
                }
                //on clicked in delete for everyone
                binding.deleteForEveryone.setOnClickListener {
                    message.messageText = "This Message is Removed"

                    message.messageId.let { its ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(its!!)
                            .setValue(message)
                    }
                    message.messageId.let { itr ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(itr!!)
                            .setValue(message)
                    }
                    dialog.dismiss()
                }
                //on Clicked in Cancel
                binding.cancel.setOnClickListener { dialog.dismiss() }

                dialog.show()
                false
            }
        }
        else{
            val viewHolder = holder as ReceiveMessageVH
            //with send photo
            if (message.messageText.equals("photo")){
                viewHolder.binding.messageImage.visibility = View.VISIBLE
                viewHolder.binding.messageText.visibility = View.GONE
                viewHolder.binding.messagetextLayout.visibility = View.GONE
                Glide.with(context)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.image_placeholder)
                    .into(viewHolder.binding.messageImage)
            }
            //Without send photo
            viewHolder.binding.messageText.text = message.messageText

            //long clicked in message for deleting
            viewHolder.itemView.setOnLongClickListener {
                //inflate delete layout
                val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                val binding = DeleteLayoutBinding.bind(view)

                //build dialog
                val dialog = AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setView(binding.root)
                    .create()

                //on clicked in delete for me
                binding.deleteForMe.setOnClickListener{

                    message.messageId.let { its ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(receiverRoom)
                            .child("message")
                            .child(its!!)
                            .setValue(null)
                    }
                    dialog.dismiss()
                }
                //on clicked in delete for everyone
                binding.deleteForEveryone.setOnClickListener {
                    message.messageText = "This Message is Removed"

                    message.messageId.let { its ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(receiverRoom)
                            .child("message")
                            .child(its!!)
                            .setValue(message)
                    }
                    message.messageId.let { itr ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(receiverRoom)
                            .child("message")
                            .child(itr!!)
                            .setValue(message)
                    }
                    dialog.dismiss()
                }
                //on Clicked in Cancel
                binding.cancel.setOnClickListener { dialog.dismiss() }

                dialog.show()
                false
            }
        }
    }

    override fun getItemCount(): Int = messages.size

}

