package com.kyrgyzcoder.chatapp.friends

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kyrgyzcoder.chatapp.EXTRA_CHAT_REF
import com.kyrgyzcoder.chatapp.EXTRA_FRIEND_UID
import com.kyrgyzcoder.chatapp.MainActivity
import com.kyrgyzcoder.chatapp.R
import com.kyrgyzcoder.chatapp.databinding.ActivityFriendInfoBinding
import com.kyrgyzcoder.chatapp.model.*
import java.util.*

class FriendInfoActivity : AppCompatActivity() {

    private val TAG = "FRIEND_INFO_ACTIVITY"

    private lateinit var binder: ActivityFriendInfoBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivityFriendInfoBinding.inflate(layoutInflater)
        setContentView(binder.root)

        db = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        val uid = intent.getStringExtra(EXTRA_FRIEND_UID)
        if (uid != null) {
            Log.d(TAG, "onCreate: $uid")
            initUI(uid)
            handleClicks(uid)
        } else {
            Toast.makeText(this, "Uid is NULL", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            finish()
            startActivity(intent)
        }
    }

    private fun handleClicks(uid: String) {
        binder.phoneNumberFriendInfo.setOnClickListener {
            val phoneNumber = binder.phoneNumberFriendInfo.text.toString()
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            startActivity(intent)
        }

        binder.sendRequest.setOnClickListener {
            sendFriendshipRequest(uid)
        }

        binder.cancelRequest.setOnClickListener {
            cancelFriendshipRequest(uid)
        }

        binder.acceptRequest.setOnClickListener {
            acceptFriendshipRequest(uid)
        }

        binder.writeToFriend.setOnClickListener {
            writeToUser(uid)
        }
        binder.declineRequest.setOnClickListener {
            declineFriendRequest(uid)
        }
    }

    private fun declineFriendRequest(uid: String) {
        Log.d(TAG, "declineFriendRequest: $uid")
        val user = firebaseAuth.currentUser!!

        db.collection("requests").document(uid + user.uid).delete().addOnSuccessListener {
            Log.d(TAG, "declineFriendRequest: declinedSuccessfully")
            Toast.makeText(this, "Friendship request declined", Toast.LENGTH_SHORT).show()
            goToMain()
        }.addOnFailureListener {
            Log.d(TAG, "declineFriendRequest: failed ${it.message}")
        }
    }

    private fun writeToUser(uid: String) {
        Log.d(TAG, "writeToUser: isCalled")
        val user = firebaseAuth.currentUser!!

        //First check if chat already exists or not
        val chatRef1 = db.collection("chats").document(user.uid + uid)
        val chatRef2 = db.collection("chats").document(uid + user.uid)

        chatRef1.get().addOnSuccessListener {
            if (it.exists()) {
                //if chat exists go to chat
                val path = it.reference.path
                goToExistingChat(path)
            } else {
                chatRef2.get().addOnSuccessListener { sn ->
                    if (sn.exists()) {
                        val path = sn.reference.path
                        goToExistingChat(path)
                    } else {
                        createNewChat(user, uid)
                    }
                }
            }
        }

    }

    private fun createNewChat(user: FirebaseUser, uid: String) {
        Log.d(TAG, "createNewChat: ${user.uid} $uid")
        val newChat = PrivateChat(user.uid, uid, arrayListOf(user.uid, uid), Timestamp(Date()))
        val chatRef = db.collection("chats").document(user.uid + uid)
        chatRef.set(newChat).addOnSuccessListener {
            Log.d(TAG, "createNewChat: createChat is successful")
            val newMessage = PrivateMessage(
                user.uid, uid, user.displayName, "Created chat",
                false, Timestamp(Date())
            )
            chatRef.collection("messages").document().set(newMessage, SetOptions.merge())
                .addOnSuccessListener {
                    goToExistingChat(chatRef.path)
                }
        }
    }

    private fun goToExistingChat(path: String) {
        Log.d(TAG, "goToExistingChat: $path")
        val intent = Intent(this, PrivateChatActivity::class.java)
        intent.putExtra(EXTRA_CHAT_REF, path)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        finish()
        startActivity(intent)
    }

    private fun acceptFriendshipRequest(uid: String) {
        Log.d(TAG, "acceptFriendshipRequest: isCalled")
        val user = firebaseAuth.currentUser!!
        //first delete request
        db.collection("requests").document(uid + user.uid).delete().addOnSuccessListener {
            //now add him to ur friend list
            val friend = Friend(uid)
            val ref = db.collection("users").document(user.uid).collection("friends").document(uid)
            ref.set(friend).addOnSuccessListener {

                //nooow add urself to his friend list
                val fr = Friend(user.uid)
                val reff =
                    db.collection("users").document(uid).collection("friends").document(user.uid)
                reff.set(fr).addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Request has been accepted successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    goToMain()
                }.addOnFailureListener {
                    Log.d(TAG, "acceptFriendshipRequest: ${it.message}")
                }
            }.addOnFailureListener {
                Log.d(TAG, "acceptFriendshipRequest: ${it.message}")
            }
        }.addOnFailureListener {
            Log.d(TAG, "acceptFriendshipRequest: ${it.message}")
        }
    }

    private fun cancelFriendshipRequest(uid: String) {
        Log.d(TAG, "cancelFriendshipRequest: isCalled")
        val user = firebaseAuth.currentUser!!
        db.collection("requests").document(user.uid + uid).delete().addOnSuccessListener {
            Toast.makeText(this, "Request has been cancelled successfully", Toast.LENGTH_SHORT)
                .show()
            goToMain()
        }
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        finish()
        startActivity(intent)
    }

    private fun sendFriendshipRequest(uid: String) {
        Log.d(TAG, "sendFriendshipRequest: isCalled")
        val user = firebaseAuth.currentUser!!

        //check if user viewing his/her own profile
        if (user.uid == uid) {
            Toast.makeText(this, "You cannot send request to yourself!", Toast.LENGTH_SHORT).show()
            return
        }

        val newRequest = Request(user.uid, uid, user.displayName, false)
        db.collection("requests").document(user.uid + uid).set(newRequest).addOnSuccessListener {
            Toast.makeText(this, getString(R.string.requestSent), Toast.LENGTH_LONG).show()
            initUI(uid)
        }.addOnFailureListener {
            Log.d(TAG, "sendFriendshipRequest: failure ${it.message}")
        }
    }

    private fun initUI(uid: String) {
        Log.d(TAG, "initUI: $uid")
        binder.pBarFriendInfo.visibility = View.VISIBLE
        db.collection("profiles").document(uid).get().addOnSuccessListener {
            val profile = it.toObject(Profile::class.java)
            if (profile != null) {
                Glide.with(binder.root.context).load(profile.photoUrl)
                    .into(binder.imgViewFriendInfo)
                binder.displayNameFriendInfo.text = profile.displayName
                binder.statusFriendInfo.text = profile.status
                binder.phoneNumberFriendInfo.text = profile.phoneNumber
                val user = firebaseAuth.currentUser
                if (user != null) {
                    val ref = db.collection("users").document(user.uid).collection("friends")
                        .document(uid)
                    ref.get().addOnSuccessListener { sn ->
                        if (sn.exists()) {
                            binder.pBarFriendInfo.visibility = View.GONE
                            //if already friend can write to him/her
                            binder.sendRequest.visibility = View.GONE
                            binder.cancelRequest.visibility = View.GONE
                            binder.acceptRequest.visibility = View.GONE
                            binder.declineRequest.visibility = View.GONE
                            binder.writeToFriend.visibility = View.VISIBLE
                        } else {
                            //if not friend there are three option 1. he sent request to that user,
                            // 2. that user sent request to him
                            // 3. no one sent any requests
                            binder.writeToFriend.visibility = View.GONE
                            db.collection("requests").document(user.uid + uid).get()
                                .addOnSuccessListener { s ->
                                    if (s.exists()) {
                                        binder.pBarFriendInfo.visibility = View.GONE
                                        //if he send request he can cancel it
                                        binder.sendRequest.visibility = View.GONE
                                        binder.acceptRequest.visibility = View.GONE
                                        binder.declineRequest.visibility = View.GONE
                                        binder.cancelRequest.visibility = View.VISIBLE
                                    } else {
                                        binder.cancelRequest.visibility = View.GONE
                                        db.collection("requests").document(uid + user.uid).get()
                                            .addOnSuccessListener { a ->
                                                binder.pBarFriendInfo.visibility = View.GONE
                                                if (a.exists()) {

                                                    //if he received a request he can accept it
                                                    binder.acceptRequest.visibility = View.VISIBLE
                                                    binder.declineRequest.visibility = View.VISIBLE
                                                    binder.sendRequest.visibility = View.GONE
                                                } else {
                                                    //if no one sent requests, he can send it
                                                    binder.sendRequest.visibility = View.VISIBLE
                                                    binder.acceptRequest.visibility = View.GONE
                                                    binder.declineRequest.visibility = View.GONE
                                                }
                                            }
                                    }
                                }
                        }
                    }
                }
            }
        }
    }
}
