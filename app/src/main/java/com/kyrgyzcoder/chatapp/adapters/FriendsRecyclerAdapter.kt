package com.kyrgyzcoder.chatapp.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.kyrgyzcoder.chatapp.databinding.ListFriendsBinding
import com.kyrgyzcoder.chatapp.model.Friend
import com.kyrgyzcoder.chatapp.model.Profile

class FriendsRecyclerAdapter(
    options: FirestoreRecyclerOptions<Friend>,
    val listener: ClicksListener
) :
    FirestoreRecyclerAdapter<Friend, FriendsRecyclerAdapter.FriendsViewHolder>(options) {

    private val TAG = "FRIENDS_ADAPTER"

    inner class FriendsViewHolder(_binder: ListFriendsBinding) :
        RecyclerView.ViewHolder(_binder.root) {
        private val binder = _binder

        init {
            binder.listFriendsLayout.setOnClickListener {
                listener.handleClicks(adapterPosition)
            }
            binder.listFriendsLayout.setOnLongClickListener {
                listener.handleDeletion(adapterPosition)
                true
            }
        }

        fun onBind(friend: Friend) {
            Log.d(TAG, "onBind: $friend")
            val db = FirebaseFirestore.getInstance()
            db.collection("profiles").document(friend.uid).get().addOnSuccessListener {
                val profile = it.toObject(Profile::class.java)
                if (profile != null)
                    fillInfo(profile)
            }.addOnFailureListener {
                Log.d(TAG, "onBind: Failed ${it.message}")
            }
        }

        private fun fillInfo(profile: Profile) {
            binder.userNameListFriends.text = profile.displayName
            binder.userStatusListFriends.text = profile.status
            Glide.with(binder.root.context).load(profile.photoUrl).into(binder.imgViewListFriends)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
        val binder = ListFriendsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendsViewHolder(binder)
    }

    override fun onBindViewHolder(holder: FriendsViewHolder, position: Int, model: Friend) {
        holder.onBind(model)
    }

    interface ClicksListener {
        fun handleClicks(position: Int)
        fun handleDeletion(position: Int)
    }
}