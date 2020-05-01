package com.kyrgyzcoder.chatapp.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.kyrgyzcoder.chatapp.databinding.ListRequestsBinding
import com.kyrgyzcoder.chatapp.model.Profile
import com.kyrgyzcoder.chatapp.model.Request

class RequestsRecyclerAdapter(
    options: FirestoreRecyclerOptions<Request>,
    val listener: RequestsListener
) :
    FirestoreRecyclerAdapter<Request, RequestsRecyclerAdapter.RequestsViewHolder>(options) {

    private val TAG = "REQUESTS_ADAPTER"

    inner class RequestsViewHolder(_binder: ListRequestsBinding) :
        RecyclerView.ViewHolder(_binder.root) {

        private val binder = _binder

        init {
            binder.buttonAccept.setOnClickListener {
                listener.handleAccept(adapterPosition)
            }
            binder.buttonDecline.setOnClickListener {
                listener.handleDecline(adapterPosition)
            }

            binder.requestsLayout.setOnClickListener {
                listener.handleClick(adapterPosition)
            }

        }

        fun onBind(request: Request) {
            Log.d(TAG, "onBind: ")
            val db = FirebaseFirestore.getInstance()
            db.collection("profiles").document(request.senderUid).get().addOnSuccessListener {
                val profile = it.toObject(Profile::class.java)
                if (profile != null) {
                    fillBlanks(profile)
                }
            }
        }

        private fun fillBlanks(profile: Profile) {
            binder.userNameListRequest.text = profile.displayName
            binder.statusListRequest.text = profile.status
            Glide.with(binder.root.context).load(profile.photoUrl).into(binder.imgListRequest)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestsViewHolder {
        val binder = ListRequestsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RequestsViewHolder(binder)
    }

    override fun onBindViewHolder(holder: RequestsViewHolder, position: Int, model: Request) {
        holder.onBind(model)
    }

    interface RequestsListener {
        fun handleAccept(position: Int)
        fun handleDecline(position: Int)
        fun handleClick(position: Int)
    }
}