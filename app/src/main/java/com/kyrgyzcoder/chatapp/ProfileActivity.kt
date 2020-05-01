package com.kyrgyzcoder.chatapp

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.kyrgyzcoder.chatapp.databinding.ActivityProfileBinding
import com.kyrgyzcoder.chatapp.model.Profile
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    private val TAG = "PROFILE_ACTIVITY"

    private lateinit var binder: ActivityProfileBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var isPhotoUploaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binder.root)
        setSupportActionBar(toolbar_profile)

        title = getString(R.string.profile)
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        if (intent.hasExtra(EXTRA_PROFILE_MODE))
            initUI()

        binder.uploadProfilePhoto.setOnClickListener {
            showUploadPhoto()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_profile, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.action_save) {
            saveProfile()
        }
        return super.onOptionsItemSelected(item)

    }

    private fun saveProfile() {
        Log.d(TAG, "saveProfile: ")
        val user = firebaseAuth.currentUser
        if (isInputValid() && user != null) {
            val displayName = binder.displayNameProfile.text.toString()
            val status = binder.statusProfileActivity.text.toString()
            val profile =
                UserProfileChangeRequest.Builder().setDisplayName(displayName).build()
            user.updateProfile(profile).addOnSuccessListener {
                val ref = db.collection("profiles")
                    .document(user.uid)
                val newProfile = Profile(
                    displayName,
                    user.uid,
                    user.photoUrl.toString(),
                    user.phoneNumber,
                    status
                )
                ref.set(newProfile).addOnSuccessListener {
                    Toast.makeText(this, getString(R.string.profileUpdated), Toast.LENGTH_SHORT)
                        .show()

                    val newMap = mutableMapOf<String, Boolean>()
                    newMap["isProfileDone"] = true
                    db.collection("users").document(user.uid).set(newMap, SetOptions.merge()).addOnSuccessListener {
                        Log.d(TAG, "saveProfile: Profile is done")
                        val intent = Intent(this, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                        finish()
                        startActivity(intent)
                    }

                }
            }
        }
    }

    private fun isInputValid(): Boolean {
        var ret = true
        if (!isPhotoUploaded) {
            Toast.makeText(this, getString(R.string.pleaseUploadPhoto), Toast.LENGTH_SHORT).show()
            ret = false
        }

        val dName = binder.displayNameProfile.text.toString()
        if (dName.isEmpty()) {
            binder.displayNameProfile.error = getString(R.string.requiredField)
            ret = false
        } else if (dName.length < 3 || dName.length > 20) {
            ret = false
            binder.displayNameProfile.error = getString(R.string.nameError)
        }
        return ret
    }

    private fun showUploadPhoto() {
        Log.d(TAG, "showUploadPhoto: ")
        val builder = AlertDialog.Builder(this, R.style.AlertDialogStyle)
            .setMessage(getString(R.string.uploadPhotoAlert))
            .setPositiveButton(getString(R.string.upload)) { _, _ ->
                chooseImageFromGallery()
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                Toast.makeText(this, getString(R.string.cancelled), Toast.LENGTH_SHORT).show()
            }
        builder.create().show()
    }

    private fun chooseImageFromGallery() {
        Log.d(TAG, "chooseImageFromGallery: isCalled")

        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(
            Intent.createChooser(intent, "Choose profile photo"),
            IMG_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMG_REQUEST_CODE && resultCode == Activity.RESULT_OK &&
            data != null && data.data != null
        ) {
            val uriImage = data.data
            if (uriImage != null) {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uriImage)
                binder.imgProfile.setImageBitmap(bitmap)
                uploadPhotoToCloud(uriImage)
            }
        }
    }

    private fun uploadPhotoToCloud(uriImage: Uri) {
        Log.d(TAG, "uploadPhotoToCloud: $uriImage")
        binder.pBarProfile.visibility = View.VISIBLE
        val user = firebaseAuth.currentUser
        if (user != null) {
            val imgRef =
                FirebaseStorage.getInstance().getReference("profilePhotos/" + user.uid + ".jpg")
            imgRef.putFile(uriImage).addOnSuccessListener {
                imgRef.downloadUrl.addOnSuccessListener {
                    val downloadUrl = it.toString()
                    if (downloadUrl.isNotEmpty()) {
                        val profile =
                            UserProfileChangeRequest.Builder().setPhotoUri(Uri.parse(downloadUrl))
                                .build()
                        user.updateProfile(profile).addOnSuccessListener {
                            binder.pBarProfile.visibility = View.GONE
                            isPhotoUploaded = true
                            Toast.makeText(
                                this,
                                getString(R.string.profilePhotoUploaded),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun initUI() {
        Log.d(TAG, "initUI: ")
        val user = firebaseAuth.currentUser
        if (user != null) {
            binder.pBarProfile.visibility = View.VISIBLE
            val photoUrl = user.photoUrl
            val displayName = user.displayName
            if (photoUrl != null && displayName != null) {
                Glide.with(this).load(photoUrl).listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binder.pBarProfile.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binder.pBarProfile.visibility = View.GONE
                        return false
                    }

                }).into(binder.imgProfile)
                isPhotoUploaded = true
                binder.displayNameProfile.setText(displayName)
                db.collection("profiles").document(user.uid).get().addOnSuccessListener {
                    if (it.exists()) {
                        val status = it.getString("status")
                        if (status != null) {
                            binder.statusProfileActivity.setText(status)
                        }
                    }
                }
            }
        }
    }
}
