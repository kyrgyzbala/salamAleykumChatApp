package com.kyrgyzcoder.chatapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.kyrgyzcoder.chatapp.databinding.ListFriendsBinding
import com.kyrgyzcoder.chatapp.model.Profile

class UsersRecyclerAdapter(
    options: FirestoreRecyclerOptions<Profile>,
    val listener: ClickListener
) :
    FirestoreRecyclerAdapter<Profile, UsersRecyclerAdapter.AllUserViewHolder>(options) {

    inner class AllUserViewHolder(_binder: ListFriendsBinding) :
        RecyclerView.ViewHolder(_binder.root) {
        private val binder = _binder

        init {
            binder.listFriendsLayout.setOnClickListener {
                listener.handleClicks(adapterPosition)
            }
        }

        fun onBind(profile: Profile) {
            binder.userNameListFriends.text = profile.displayName
            binder.userStatusListFriends.text = profile.status
            Glide.with(binder.root.context).load(profile.photoUrl).into(binder.imgViewListFriends)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllUserViewHolder {
        val binder = ListFriendsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AllUserViewHolder(binder)
    }

    override fun onBindViewHolder(holder: AllUserViewHolder, position: Int, model: Profile) {
        holder.onBind(model)
    }

    interface ClickListener {
        fun handleClicks(position: Int)
    }
}