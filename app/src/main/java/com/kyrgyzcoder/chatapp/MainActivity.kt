package com.kyrgyzcoder.chatapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.kyrgyzcoder.chatapp.adapters.SectionPagerAdapter
import com.kyrgyzcoder.chatapp.databinding.ActivityMainBinding
import com.kyrgyzcoder.chatapp.friends.AddFriendActivity
import com.kyrgyzcoder.chatapp.login.LoginActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener {
    private val TAG = "MAIN_ACTIVITY"

    private lateinit var binder: ActivityMainBinding

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binder.root)
        setSupportActionBar(toolbar_main)


        firebaseAuth = FirebaseAuth.getInstance()

        val sectionPager = SectionPagerAdapter(this, supportFragmentManager)
        binder.mainViewPager.adapter = sectionPager
        binder.tabsMain.setupWithViewPager(binder.mainViewPager)

        for (i in 0 until binder.tabsMain.tabCount) {
            when (i) {
                0 -> {
                    binder.tabsMain.getTabAt(0)?.setIcon(R.drawable.ic_mail)
                }
                1 -> {
                    binder.tabsMain.getTabAt(1)?.setIcon(R.drawable.ic_friend)
                }
                2 -> {
                    binder.tabsMain.getTabAt(2)?.setIcon(R.drawable.ic_request)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_friend -> addFriend()
            R.id.action_profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra(EXTRA_PROFILE_MODE, true)
                startActivity(intent)
            }
            R.id.action_signOut -> signOut()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun signOut() {
        val builder = AlertDialog.Builder(this, R.style.AlertDialogStyle)
            .setMessage(getString(R.string.signOutWarning))
            .setPositiveButton(getString(R.string.sign_out)) { _, _ ->
                firebaseAuth.signOut()
                val intent = Intent(this, LoginActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                finish()
                startActivity(intent)
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                Toast.makeText(this, getString(R.string.cancelled), Toast.LENGTH_SHORT).show()
            }
        builder.create().show()
    }


    private fun addFriend() {
        val intent = Intent(this, AddFriendActivity::class.java)
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: isCalled")
        binder.pBarRelativeMain.visibility = View.VISIBLE
        firebaseAuth.addAuthStateListener(this)
        val user = firebaseAuth.currentUser
        if (user == null) {
            finish()
            val intent = Intent(this, LoginActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(intent)
        }
        binder.pBarRelativeMain.visibility = View.GONE
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: isCalled")
        firebaseAuth.removeAuthStateListener(this)
    }

    override fun onAuthStateChanged(auth: FirebaseAuth) {
        if (auth.currentUser == null) {
            finish()
            val intent = Intent(this, LoginActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(intent)
        }
    }
}
