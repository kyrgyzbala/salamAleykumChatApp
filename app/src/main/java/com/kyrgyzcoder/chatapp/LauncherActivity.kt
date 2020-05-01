package com.kyrgyzcoder.chatapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kyrgyzcoder.chatapp.databinding.ActivityLauncherBinding
import com.kyrgyzcoder.chatapp.login.LoginActivity

class LauncherActivity : AppCompatActivity() {

    private lateinit var binder: ActivityLauncherBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binder.root)
    }

    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            finish()
            val intent = Intent(this, LoginActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(intent)
        } else {
            FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        val isProfileDone = it.getBoolean("isProfileDone")
                        if (isProfileDone != null && isProfileDone == false) {
                            val intent = Intent(this, ProfileActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            }
                            finish()
                            startActivity(intent)
                        } else {
                            val intent = Intent(this, MainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            }
                            finish()
                            startActivity(intent)
                        }
                    }
                }
        }
    }
}
