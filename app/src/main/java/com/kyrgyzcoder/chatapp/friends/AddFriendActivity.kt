package com.kyrgyzcoder.chatapp.friends

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.kyrgyzcoder.chatapp.EXTRA_FRIEND_UID
import com.kyrgyzcoder.chatapp.R
import com.kyrgyzcoder.chatapp.adapters.UsersRecyclerAdapter
import com.kyrgyzcoder.chatapp.databinding.ActivityAddFriendBinding
import com.kyrgyzcoder.chatapp.model.Profile
import kotlinx.android.synthetic.main.activity_add_friend.*

class AddFriendActivity : AppCompatActivity(), UsersRecyclerAdapter.ClickListener {

    private val TAG = "ADD_FRIEND_ACTIVITY"

    private lateinit var binder: ActivityAddFriendBinding
    private lateinit var adapter: UsersRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivityAddFriendBinding.inflate(layoutInflater)
        setContentView(binder.root)
        setSupportActionBar(toolbar_add_friend)

        title = getString(R.string.allUsers)

    }

    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null)
            initRecyclerView(user)
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    private fun initRecyclerView(user: FirebaseUser) {
        Log.d(TAG, "initRecyclerView: ${user.displayName}")
        val db = FirebaseFirestore.getInstance()
        val query = db.collection("profiles").orderBy("displayName")
        val options: FirestoreRecyclerOptions<Profile> =
            FirestoreRecyclerOptions.Builder<Profile>().setQuery(query, Profile::class.java).build()
        adapter = UsersRecyclerAdapter(options, this)

        binder.recyclerViewAddFriend.adapter = adapter
        adapter.startListening()
    }

    override fun handleClicks(position: Int) {
        Log.d(TAG, "handleClicks: $position")
        val snapshot = adapter.snapshots.getSnapshot(position)
        val uid = snapshot.getString("uid")
        if (uid != null) {
            val intent = Intent(this, FriendInfoActivity::class.java)
            intent.putExtra(EXTRA_FRIEND_UID, uid)
            startActivity(intent)
        }
    }
}
