package com.kyrgyzcoder.chatapp.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kyrgyzcoder.chatapp.databinding.ListChatsBinding
import com.kyrgyzcoder.chatapp.model.PrivateChat
import com.kyrgyzcoder.chatapp.model.PrivateMessage
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log

class ChatsRecyclerAdapter(
    options: FirestoreRecyclerOptions<PrivateChat>,
    val listener: ClicksListener
) :
    FirestoreRecyclerAdapter<PrivateChat, ChatsRecyclerAdapter.ChatsViewHolder>(options) {

    private val TAG = "CHATS_ADAPTER"

    inner class ChatsViewHolder(_binder: ListChatsBinding) : RecyclerView.ViewHolder(_binder.root) {

        private val binder = _binder

        init {
            binder.chatListLayout.setOnClickListener {
                listener.handleClicks(adapterPosition)
            }
            binder.chatListLayout.setOnLongClickListener {
                listener.handleDeletion(adapterPosition)
                true
            }
        }

        fun onBind(chat: PrivateChat) {
            Log.d(TAG, "onBind: $chat")
            val ref = snapshots.getSnapshot(adapterPosition).reference
            Log.d(TAG, "onBind: ref: ${ref.path}")
            ref.collection("messages").orderBy("sentTime", Query.Direction.DESCENDING)
                .get().addOnSuccessListener {
                        Log.d(TAG, "onBind: is not empty")
                        if (it.documents[0].exists()) {
                            val message = it.documents[0].toObject(PrivateMessage::class.java)
                            Log.d(TAG, "onBind: firstSuccess, $message")
                            if (message != null) {
                                val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ROOT)
                                val dateStr: String = dateFormat.format(message.sentTime.toDate())
                                binder.lastMessageDateGroupsTextView.text = dateStr
                                binder.lastMessageGroupTextView.text = message.message
                                getPartnerName(chat, message.senderUid)
                            }
                        }
                }
        }

        private fun getPartnerName(chat: PrivateChat, senderUid: String) {
            val db = FirebaseFirestore.getInstance()
            val user = FirebaseAuth.getInstance().currentUser!!
            val partnerId = if (chat.uid1 == user.uid)
                chat.uid2
            else
                chat.uid1

            Log.d(TAG, "getPartnerName: partner ID : $partnerId")
            db.collection("profiles")
                .document(partnerId).get().addOnSuccessListener {
                    val displayName = it.getString("displayName")
                    val photoUrl = it.getString("photoUrl")
                    if (displayName != null && photoUrl != null) {
                        Glide.with(binder.root).load(photoUrl).into(binder.imgLogoChat)
                        binder.groupNameTextView.text = displayName

                        if (user.uid == senderUid) {
                            val temp = "You: "
                            binder.textViewSenderName.text = temp
                        } else {
                            binder.textViewSenderName.text = displayName
                        }
                        getNumberOfNewMessages(partnerId)
                    }
                }
        }

        private fun getNumberOfNewMessages(partnerId: String) {
            Log.d(TAG, "getNumberOfNewMessages: $partnerId")
            try {
                val ref = snapshots.getSnapshot(adapterPosition).reference
                ref.collection("messages").whereEqualTo("senderUid", partnerId)
                    .whereEqualTo("isRead", false).get().addOnSuccessListener {
                        if (it.documents.isNotEmpty()) {
                            val list = it.documents
                            Log.d(TAG, "getNumberOfNewMessages: ${list.size}")
                            binder.numberOfUnread.visibility = View.VISIBLE
                            binder.numberOfUnread.text = if (list.size > 99) {
                                "99+"
                            } else {
                                list.size.toString()
                            }
                            if (list.size == 0)
                                binder.numberOfUnread.visibility = View.GONE
                        } else {
                            binder.numberOfUnread.visibility = View.GONE
                        }
                    }
            } catch (e: Exception) {
                Log.d(TAG, "getNumberOfNewMessages: ${e.message}")
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewHolder {
        val binder = ListChatsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatsViewHolder(binder)
    }

    override fun onBindViewHolder(holder: ChatsViewHolder, position: Int, model: PrivateChat) {
        holder.onBind(model)
    }

    interface ClicksListener {
        fun handleClicks(position: Int)
        fun handleDeletion(position: Int)
    }
}