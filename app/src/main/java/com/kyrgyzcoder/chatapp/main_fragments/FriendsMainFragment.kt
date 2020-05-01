package com.kyrgyzcoder.chatapp.main_fragments

import android.app.AlertDialog
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
import com.kyrgyzcoder.chatapp.MainActivity
import com.kyrgyzcoder.chatapp.R
import com.kyrgyzcoder.chatapp.adapters.FriendsRecyclerAdapter
import com.kyrgyzcoder.chatapp.databinding.FragmentFriendsMainBinding
import com.kyrgyzcoder.chatapp.friends.FriendInfoActivity
import com.kyrgyzcoder.chatapp.model.Friend

/**
 * A simple [Fragment] subclass.
 */
class FriendsMainFragment : Fragment(), FriendsRecyclerAdapter.ClicksListener {

    private val TAG = "FRIENDS_FRAGMENT"

    private var _binder: FragmentFriendsMainBinding? = null
    private val binder get() = _binder!!

    private lateinit var adapter: FriendsRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binder = FragmentFriendsMainBinding.inflate(inflater, container, false)
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
        adapter.stopListening()
    }

    private fun initRecyclerView(user: FirebaseUser) {
        Log.d(TAG, "initRecyclerView: $user")
        val db = FirebaseFirestore.getInstance()
        val query = db.collection("users").document(user.uid).collection("friends")
        val options: FirestoreRecyclerOptions<Friend> =
            FirestoreRecyclerOptions.Builder<Friend>().setQuery(query, Friend::class.java).build()

        adapter = FriendsRecyclerAdapter(options, this)
        binder.recyclerViewFriends.adapter = adapter

        adapter.startListening()
    }

    override fun handleClicks(position: Int) {
        Log.d(TAG, "handleClicks: $position")
        val snapshot = adapter.snapshots.getSnapshot(position)
        val friend = snapshot.toObject(Friend::class.java)
        if (friend != null) {
            val intent = Intent(this.requireContext(), FriendInfoActivity::class.java)
            intent.putExtra(EXTRA_FRIEND_UID, friend.uid)
            startActivity(intent)
        } else {
            Toast.makeText(this.requireContext(), "Uid is NULL", Toast.LENGTH_SHORT).show()
            val intent = Intent(this.requireContext(), MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            requireActivity().finish()
            startActivity(intent)
        }
    }

    override fun handleDeletion(position: Int) {
        Log.d(TAG, "handleDeletion: $position")
        val builder = AlertDialog.Builder(this.requireContext(), R.style.AlertDialogStyle)
            .setMessage("Are you sure to delete this friend?")
            .setPositiveButton("Delete") { _, _ ->
                deleteFriend(position)
            }
            .setNegativeButton("Cancel") { _, _ ->
                Toast.makeText(
                    this.requireContext(),
                    getString(R.string.cancelled),
                    Toast.LENGTH_SHORT
                ).show()
            }
        builder.create().show()
    }

    private fun deleteFriend(position: Int) {
        Log.d(TAG, "deleteFriend: $position")

        val snapshot = adapter.snapshots.getSnapshot(position)

        val friendUid = snapshot.getString("uid")!!
        snapshot.reference.delete().addOnSuccessListener {
            FirebaseFirestore.getInstance().collection("users").document(friendUid)
                .collection("friends").document(friendUid).delete()
            Toast.makeText(
                this.requireContext(),
                "Friend deleted successfully!",
                Toast.LENGTH_SHORT
            ).show()
        }.addOnFailureListener {
            Log.d(TAG, "deleteChat: failed  ${it.message}")
        }
    }


}
