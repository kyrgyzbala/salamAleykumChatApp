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
import com.google.firebase.firestore.Query
import com.kyrgyzcoder.chatapp.EXTRA_CHAT_REF
import com.kyrgyzcoder.chatapp.R
import com.kyrgyzcoder.chatapp.adapters.ChatsRecyclerAdapter
import com.kyrgyzcoder.chatapp.databinding.FragmentChatsMainBinding
import com.kyrgyzcoder.chatapp.friends.PrivateChatActivity
import com.kyrgyzcoder.chatapp.model.PrivateChat

/**
 * A simple [Fragment] subclass.
 */
class ChatsMainFragment : Fragment(), ChatsRecyclerAdapter.ClicksListener {

    private val TAG = "CHATS_MAIN"

    private var _binder: FragmentChatsMainBinding? = null
    private val binder get() = _binder!!

    private lateinit var adapter: ChatsRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binder = FragmentChatsMainBinding.inflate(inflater, container, false)
        return (binder.root)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: ")
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
        val query =
            db.collection("chats").whereArrayContains("chatters", user.uid)
                .orderBy("lastMessage", Query.Direction.DESCENDING)
        val options: FirestoreRecyclerOptions<PrivateChat> =
            FirestoreRecyclerOptions.Builder<PrivateChat>().setQuery(query, PrivateChat::class.java)
                .build()
        adapter = ChatsRecyclerAdapter(options, this)
        binder.chatRecyclerView.adapter = adapter
        adapter.startListening()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binder = null
    }

    override fun handleClicks(position: Int) {
        Log.d(TAG, "handleClicks: $position")

        val snapshot = adapter.snapshots.getSnapshot(position)
        val chatPath = snapshot.reference.path
        val intent = Intent(this.requireContext(), PrivateChatActivity::class.java)
        intent.putExtra(EXTRA_CHAT_REF, chatPath)
        startActivity(intent)
    }

    override fun handleDeletion(position: Int) {
        Log.d(TAG, "handleDeletion: $position")

        val builder = AlertDialog.Builder(this.requireContext(), R.style.AlertDialogStyle)
            .setMessage("Are you sure to delete whole conversation?")
            .setPositiveButton("Delete") { _, _ ->
                deleteChat(position)
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

    private fun deleteChat(position: Int) {
        Log.d(TAG, "deleteChat: ")

        val snapshot = adapter.snapshots.getSnapshot(position)

        snapshot.reference.delete().addOnSuccessListener {
            snapshot.reference.collection("messages").get().addOnSuccessListener {
                for (doc in it.documents) {
                    doc.reference.delete()
                }
                Toast.makeText(
                    this.requireContext(),
                    "Chat deleted successfully!",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }

        }.addOnFailureListener {
            Log.d(TAG, "deleteChat: failed  ${it.message}")
        }
    }
}
