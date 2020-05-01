package com.kyrgyzcoder.chatapp.main_fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.kyrgyzcoder.chatapp.EXTRA_FRIEND_UID
import com.kyrgyzcoder.chatapp.adapters.RequestsRecyclerAdapter
import com.kyrgyzcoder.chatapp.databinding.FragmentRequestsBinding
import com.kyrgyzcoder.chatapp.friends.FriendInfoActivity
import com.kyrgyzcoder.chatapp.model.Friend
import com.kyrgyzcoder.chatapp.model.Request

/**
 * A simple [Fragment] subclass.
 */
class RequestsFragment : Fragment(), RequestsRecyclerAdapter.RequestsListener {

    private val TAG = "REQUESTS_FRAGMENT"

    private var _binder: FragmentRequestsBinding? = null
    private val binder get() = _binder!!

    private lateinit var adapter: RequestsRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binder = FragmentRequestsBinding.inflate(inflater, container, false)
        return binder.root
    }

    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null)
            initRecyclerView(user)
    }

    override fun onStop() {
        super.onStop()
        try {
            adapter.stopListening()
        } catch (e: Exception) {
            Log.d(TAG, "onStop: ${e.message}")
        }
    }

    private fun initRecyclerView(user: FirebaseUser) {
        Log.d(TAG, "initRecyclerView: $user")
        val db = FirebaseFirestore.getInstance()
        val query =
            db.collection("requests").whereEqualTo("receiverUid", user.uid).orderBy("sentTime")
        val options: FirestoreRecyclerOptions<Request> =
            FirestoreRecyclerOptions.Builder<Request>().setQuery(query, Request::class.java).build()
        adapter = RequestsRecyclerAdapter(options, this)

        binder.recyclerViewRequests.adapter = adapter
        adapter.startListening()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binder = null
    }

    override fun handleAccept(position: Int) {
        Log.d(TAG, "handleAccept: position")
        val db = FirebaseFirestore.getInstance()
        val snapshot = adapter.snapshots.getSnapshot(position)
        val uid = snapshot.getString("senderUid")!!

        val user = FirebaseAuth.getInstance().currentUser!!
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
                        this.requireContext(),
                        "Request has been accepted successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
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

    override fun handleDecline(position: Int) {
        Log.d(TAG, "handleDecline: ")

        val db = FirebaseFirestore.getInstance()
        val snapshot = adapter.snapshots.getSnapshot(position)
        val uid = snapshot.getString("senderUid")!!

        val user = FirebaseAuth.getInstance().currentUser!!

        db.collection("requests").document(uid + user.uid).delete().addOnSuccessListener {
            Toast.makeText(this.requireContext(), "Friendship declined!", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "declineFriendRequest: declinedSuccessfully")
        }.addOnFailureListener {
            Log.d(TAG, "declineFriendRequest: failed ${it.message}")
        }
    }

    override fun handleClick(position: Int) {
        Log.d(TAG, "handleClick: ")
        val snapshot = adapter.snapshots.getSnapshot(position)
        val uid = snapshot.getString("senderUid")!!

        val intent = Intent(this.requireContext(), FriendInfoActivity::class.java)
        intent.putExtra(EXTRA_FRIEND_UID, uid)
        startActivity(intent)
    }

}
