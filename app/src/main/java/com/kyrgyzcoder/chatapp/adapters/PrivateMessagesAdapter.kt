package com.kyrgyzcoder.chatapp.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kyrgyzcoder.chatapp.databinding.ListMessagesBinding
import com.kyrgyzcoder.chatapp.model.PrivateMessage
import java.text.SimpleDateFormat
import java.util.*

class PrivateMessagesAdapter(options: FirestoreRecyclerOptions<PrivateMessage>) :
    FirestoreRecyclerAdapter<PrivateMessage, PrivateMessagesAdapter.MessagesViewHolder>(options) {

    private val TAG = "MESSAGES_ADAPTER"

    inner class MessagesViewHolder(_binder: ListMessagesBinding) :
        RecyclerView.ViewHolder(_binder.root) {

        private val binder = _binder

        fun onBind(message: PrivateMessage) {
            Log.d(TAG, "onBind: isCalled ${message.isRead} $message")
            val user = FirebaseAuth.getInstance().currentUser!!
            if (message.senderUid == user.uid) {
                Log.d(TAG, "onBind: ${user.uid} , $message")
                binder.layoutReceived.visibility = View.GONE
                binder.layoutSent.visibility = View.VISIBLE
                binder.messageTextViewMy.text = message.message
                try {
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ROOT)
                    val dateStr: String = dateFormat.format(message.sentTime.toDate())
                    binder.sendDateTextViewMy.text = dateStr
                } catch (e: Exception) {
                    Log.d(TAG, "onBind: ${e.message}")
                } finally {
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ROOT)
                    val dateStr: String =
                        dateFormat.format(message.sentTimeStr.toDate())
                    binder.sendDateTextViewMy.text = dateStr
                }
                if (message.isRead) {
                    binder.imageViewReadGr.visibility = View.VISIBLE
                } else {
                    binder.imageViewReadGr.visibility = View.GONE
                }
            } else {
                Log.d(TAG, "onBind: Else")
                binder.layoutSent.visibility = View.GONE
                binder.layoutReceived.visibility = View.VISIBLE
                binder.messageTextView.text = message.message
                try {
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ROOT)
                    val dateStr: String = dateFormat.format(Date(message.sentTime.seconds * 1000))
                    binder.sendDateTextView.text = dateStr
                } catch (e: Exception) {
                    Log.d(TAG, "onBind: else error ${e.message}")
                } finally {
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ROOT)
                    val dateStr: String =
                        dateFormat.format(Date(message.sentTimeStr.seconds * 1000))
                    binder.sendDateTextView.text = dateStr
                }
                val db = FirebaseFirestore.getInstance()
                val ref =
                    db.collection("profiles").document(message.senderUid)
                ref.get().addOnSuccessListener {
                    val displayName = it.getString("displayName")
                    if (displayName != null) {
                        binder.senderNameTextView.text = displayName
                        Log.d(TAG, "onBind: displayName Got successfully $displayName")
                    }
                }
                val sn = snapshots.getSnapshot(position)
                val map = mutableMapOf<String, Boolean>()
                map["isRead"] = true
                sn.reference.set(map, SetOptions.merge()).addOnSuccessListener {
                    Log.d(TAG, "isRead is true now $message")
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesViewHolder {
        val binder = ListMessagesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessagesViewHolder(binder)
    }

    override fun onBindViewHolder(
        holder: MessagesViewHolder,
        position: Int,
        model: PrivateMessage
    ) {
        holder.onBind(model)
    }
}