package com.chats.combochat

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.chats.combochat.databinding.ActivityOtpactivityBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.otpview.OTPListener
import java.util.concurrent.TimeUnit

class OTPActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpactivityBinding
    private lateinit var verificationId : String
    private lateinit var auth : FirebaseAuth
    private lateinit var dialog : ProgressDialog

    private lateinit var phoneNumber : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        //Loading sending OTP
        loading()

        phoneNumber = intent.getStringExtra("phoneNumber").toString()
        binding.verifyHeader.setText("Verify "+ phoneNumber)

        //sending Phone Number to firebase
        //sending OTP
        //Entering OTP
        onSendingPhoneNumber()
    }

    //sending Phone Number to firebase
    private fun onSendingPhoneNumber(){
        val option = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                override fun onVerificationCompleted(p0: PhoneAuthCredential) {

                }

                override fun onVerificationFailed(p0: FirebaseException) {

                }

                override fun onCodeSent(verifyId: String,
                                        forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
                    super.onCodeSent(verifyId, forceResendingToken)
                    dialog.dismiss()

                    verificationId = verifyId
                    val input = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    input.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

                    binding.otpView.requestFocusOTP()
                }
            }).build()

        PhoneAuthProvider.verifyPhoneNumber(option)

        //In Write and Check OTP
        onEnteredOtpCode()
    }

    //In Write and Check OTP
    private fun onEnteredOtpCode(){
        binding.otpView.otpListener = object : OTPListener {
            override fun onInteractionListener() {

            }

            override fun onOTPComplete(otp: String) {
                Toast.makeText(this@OTPActivity, "The OTP is $otp", Toast.LENGTH_LONG).show()

                //Credential
                val credential = PhoneAuthProvider.getCredential(verificationId, otp)
                signInWithPhoneAuthCredential(credential)
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential : PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this){task ->
            if (task.isSuccessful){
                startActivity(Intent(this@OTPActivity, SetupProfileActivity::class.java))
                finishAffinity()
            }
            else{
                Toast.makeText(this@OTPActivity, "The OTP entered was invalid!!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loading(){
        dialog = ProgressDialog(this)
        dialog.setMessage("Sending OTP...")
        dialog.setCancelable(false)
        dialog.show()
    }
}