package com.chats.combochat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.chats.combochat.databinding.ActivityVerificationBinding
import com.google.firebase.auth.FirebaseAuth

class VerificationActivity : AppCompatActivity() {

    private lateinit var binding :ActivityVerificationBinding
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }


        binding.phoneNumber.requestFocus()

        binding.sendCode.setOnClickListener{
            val intent = Intent(this , OTPActivity::class.java)
            intent.putExtra("phoneNumber", binding.phoneNumber.text.toString())
            startActivity(intent)
        }

    }
}