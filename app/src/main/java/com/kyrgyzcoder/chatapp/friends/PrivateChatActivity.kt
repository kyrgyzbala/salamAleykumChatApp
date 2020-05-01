package com.kyrgyzcoder.chatapp.friends

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kyrgyzcoder.chatapp.EXTRA_CHAT_REF
import com.kyrgyzcoder.chatapp.EXTRA_FRIEND_UID
import com.kyrgyzcoder.chatapp.MainActivity
import com.kyrgyzcoder.chatapp.R
import com.kyrgyzcoder.chatapp.adapters.PrivateMessagesAdapter
import com.kyrgyzcoder.chatapp.databinding.ActivityPrivateChatBinding
import com.kyrgyzcoder.chatapp.model.PrivateChat
import com.kyrgyzcoder.chatapp.model.PrivateMessage
import kotlinx.android.synthetic.main.activity_private_chat.*
import java.util.*

class PrivateChatActivity : AppCompatActivity() {

    private val TAG = "PRIVATE_CHAT"

    private lateinit var binder: ActivityPrivateChatBinding


    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var partnerId: String
    private lateinit var chatRef: DocumentReference
    private var ref: String = ""

    private lateinit var adapter: PrivateMessagesAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivityPrivateChatBinding.inflate(layoutInflater)
        setContentView(binder.root)
        setSupportActionBar(toolbar_private_chat)
        title = ""

        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val ref = intent.getStringExtra(EXTRA_CHAT_REF)
        if (ref != null) {
            chatRef = db.document(ref)
            initUI()
        } else {
            Toast.makeText(this, "Error ref == NULL", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            finish()
            startActivity(intent)
        }

        binder.sendButtonChat.setOnClickListener {
            sendMessage()
        }
    }

    private fun sendMessage() {
        val message = binder.messageEditTextChat.text.toString()
        if (message.isEmpty()) {
            binder.messageEditTextChat.error = ""
            return
        }
        val user = firebaseAuth.currentUser!!
        val newMessage =
            PrivateMessage(user.uid, partnerId, user.displayName, message, false, Timestamp(Date()))
        chatRef.collection("messages").document().set(newMessage).addOnSuccessListener {
            Log.d(TAG, "sendMessage: new message sent $newMessage")
            binder.messageEditTextChat.setText("")
            val map = mutableMapOf<String, Any>()
            map["lastMessage"] = Timestamp(Date())
            chatRef.set(map, SetOptions.merge()).addOnSuccessListener {
                Log.d(TAG, "sendMessage: lastMessageTime Updated")
            }
            initRecyclerView(FirebaseAuth.getInstance().currentUser!!)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        finish()
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_private_chat, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.action_friend_info) {
            val intent = Intent(this, FriendInfoActivity::class.java)
            intent.putExtra(EXTRA_FRIEND_UID, partnerId)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onStart() {
        super.onStart()
        if (ref.isNotEmpty()) {
            val user = firebaseAuth.currentUser
            if (user != null)
                initRecyclerView(user)
        }
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    private fun initRecyclerView(user: FirebaseUser) {
        Log.d(TAG, "initRecyclerView: ")
        db = FirebaseFirestore.getInstance()
        val query =
            db.collection("chats").document(ref).collection("messages").orderBy("sentTime")
        val options: FirestoreRecyclerOptions<PrivateMessage> =
            FirestoreRecyclerOptions.Builder<PrivateMessage>()
                .setQuery(query, PrivateMessage::class.java).build()
        adapter = PrivateMessagesAdapter(options)

        binder.recyclerViewChat.adapter = adapter

        adapter.startListening()

        try {
            Log.d(TAG, "initRecyclerView: trySmoothScroll ${adapter.itemCount - 1}")
            binder.recyclerViewChat.smoothScrollToPosition(adapter.itemCount - 1)
        } catch (e: IllegalArgumentException) {
            Log.d(TAG, e.message!!)
        }

        binder.recyclerViewChat.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                binder.recyclerViewChat.postDelayed({
                    Log.d(TAG, "initRecyclerView: postDelayed")
                    try {
                        Log.d(TAG, "initRecyclerView: try2 ${adapter.itemCount - 1}")
                        binder.recyclerViewChat.smoothScrollToPosition(adapter.itemCount - 1)
                    } catch (e: IllegalArgumentException) {
                        Log.d(TAG, e.message!!)
                    }
                }, 100)
            }
        }
    }

    private fun initUI() {
        Log.d(TAG, "initUI: isCalled")
        binder.pBarPrivateChat.visibility = View.VISIBLE
        val user = firebaseAuth.currentUser!!

        chatRef.get().addOnSuccessListener {
            val chat = it.toObject(PrivateChat::class.java)
            if (chat != null) {
                partnerId = if (user.uid == chat.uid1) {
                    ref = user.uid + chat.uid2
                    chat.uid2
                } else {
                    ref = chat.uid1 + user.uid
                    chat.uid1
                }
                fillBlanks()
            }
        }
    }

    private fun fillBlanks() {
        Log.d(TAG, "fillBlanks: ")
        db.collection("profiles").document(partnerId).get().addOnSuccessListener {
            val displayName = it.getString("displayName")
            val photoUrl = it.getString("photoUrl")
            if (displayName != null && photoUrl != null) {
                binder.chatterNamePrivate.text = displayName
                Glide.with(this).load(photoUrl).into(binder.imgIconPrivateChat)
            }
            binder.pBarPrivateChat.visibility = View.GONE
            val user = firebaseAuth.currentUser
            if (user != null)
                initRecyclerView(user)
        }.addOnFailureListener {
            Log.d(TAG, "fillBlanks: failure ${it.message}")
            binder.pBarPrivateChat.visibility = View.GONE
        }
    }
}
