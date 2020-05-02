package com.kyrgyzcoder.chatapp.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.iid.FirebaseInstanceId
import com.kyrgyzcoder.chatapp.EXTRA_CODE_SENT
import com.kyrgyzcoder.chatapp.MainActivity
import com.kyrgyzcoder.chatapp.ProfileActivity
import com.kyrgyzcoder.chatapp.R
import com.kyrgyzcoder.chatapp.databinding.FragmentVerifyBinding
import kotlinx.android.synthetic.main.fragment_verify.*

/**
 * A simple [Fragment] subclass.
 */
class VerifyFragment : Fragment() {

    private val TAG = "VERIFY_FRAGMENT"

    private var _binder: FragmentVerifyBinding? = null
    private val binder get() = _binder!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView: ")
        _binder = FragmentVerifyBinding.inflate(inflater, container, false)
        return binder.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bundle = arguments
        if (bundle != null) {
            val cCode = bundle.getString(EXTRA_CODE_SENT)
            binder.verifyButton.setOnClickListener {
                verifyAndLogin(cCode!!)
            }
        }
    }

    private fun verifyAndLogin(cCode: String) {
        if (binder.verificationCodeEditText.text.toString().isEmpty()) {
            binder.verificationCodeEditText.error = getString(R.string.requiredField)
            return
        }
        val code = binder.verificationCodeEditText.text.toString()
        val credential = PhoneAuthProvider.getCredential(cCode, code)
        signInWithCredential(credential)
    }

    private fun signInWithCredential(authCredential: PhoneAuthCredential) {
        val firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        pBarVerify.visibility = View.VISIBLE
        firebaseAuth.signInWithCredential(authCredential).addOnSuccessListener {
            Log.d(TAG, "signInWithCredential: sign in success")
            val user = firebaseAuth.currentUser
            if (user != null) {
                val mapp = mutableMapOf<String, String>()
                mapp["phoneNumber"] = user.phoneNumber!!
                db.collection("users").document(user.uid).set(mapp, SetOptions.merge())
                db.collection("users").document(user.uid).get().addOnSuccessListener {
                    if (it.exists()) {
                        val isProfileDone = it.getBoolean("isProfileDone")
                        if (isProfileDone != null && isProfileDone == true) {
                            val map = mutableMapOf<String, Any>()
                            val mToken: String = FirebaseInstanceId.getInstance().token!!
                            map["token"] = mToken
                            Log.d(TAG, "signInWithCredential: token is: $mToken")
                            db.collection("fcmTokens").document(user.uid).set(map)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this.requireContext(),
                                        getString(R.string.signInSuccessful),
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    //if profile is Done go to Main Activity
                                    val intent =
                                        Intent(this.requireContext(), MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    requireActivity().finish()
                                    startActivity(intent)
                                }
                        } else {
                            val map = mutableMapOf<String, Any>()
                            val mToken = FirebaseInstanceId.getInstance().token
                            if (mToken != null)
                                map["token"] = mToken
                            Log.d(TAG, "signInWithCredential: token is: $mToken")
                            db.collection("fcmTokens").document(user.uid).set(map)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this.requireContext(),
                                        getString(R.string.signInSuccessful),
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    val newMap = mutableMapOf<String, Boolean>()
                                    newMap["isProfileDone"] = false
                                    db.collection("users").document(user.uid)
                                        .set(newMap, SetOptions.merge()).addOnSuccessListener {
                                            //if profile is not done yet, go to profile activity
                                            val intent =
                                                Intent(
                                                    this.requireContext(),
                                                    ProfileActivity::class.java
                                                )
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            requireActivity().finish()
                                            startActivity(intent)
                                        }

                                }
                        }
                    } else {
                        val map = mutableMapOf<String, Any>()
                        val mToken: String = FirebaseInstanceId.getInstance().token!!
                        map["token"] = mToken
                        Log.d(TAG, "signInWithCredential: token is: $mToken")
                        db.collection("fcmTokens").document(user.uid).set(map)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this.requireContext(),
                                    getString(R.string.signInSuccessful),
                                    Toast.LENGTH_SHORT
                                ).show()

                                val newMap = mutableMapOf<String, Boolean>()
                                newMap["isProfileDone"] = false
                                db.collection("users").document(user.uid)
                                    .set(newMap, SetOptions.merge()).addOnSuccessListener {
                                        //if profile is not done yet, go to profile activity
                                        val intent =
                                            Intent(
                                                this.requireContext(),
                                                ProfileActivity::class.java
                                            )
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        requireActivity().finish()
                                        startActivity(intent)
                                    }
                            }
                    }
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: ")
        _binder = null
    }

}
