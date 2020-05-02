package com.kyrgyzcoder.chatapp.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseException
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
import com.kyrgyzcoder.chatapp.databinding.FragmentLoginBinding
import kotlinx.android.synthetic.main.fragment_login.*
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass.
 */
class LoginFragment : Fragment() {

    private val TAG = "LOGIN_FRAGMENT"

    private var _binder: FragmentLoginBinding? = null
    private val binder get() = _binder!!
    private var phoneNumber = ""

    private lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    companion object {
        private var resendingToken: PhoneAuthProvider.ForceResendingToken? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Log.d(TAG, "onCreateView: ")
        _binder = FragmentLoginBinding.inflate(inflater, container, false)
        return binder.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binder.ccPicker.registerCarrierNumberEditText(binder.editTextPhoneNumber)

        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(authCredential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted: $authCredential")
                try {
                    binder.pBarLogin.visibility = View.GONE
                } catch (e: Exception) {
                    Log.d(TAG, "onVerificationCompleted: ${e.message}")
                }
                signInWithCredential(authCredential)
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                try {
                    binder.pBarLogin.visibility = View.GONE
                } catch (e: Exception) {
                    Log.d(TAG, "onVerificationFailed: ${e.message}")
                }
                Log.d(TAG, "onVerificationFailed: ${exception.message}")
            }

            override fun onCodeSent(s: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(s, p1)
                Log.d(TAG, "onCodeSent: $s")
                binder.pBarLogin.visibility = View.GONE
                resendingToken = p1
                //create bundle to send an an argument to another fragment
                val bundle = Bundle()
                bundle.putString(EXTRA_CODE_SENT, s)
                findNavController().navigate(R.id.action_loginFragment_to_verifyFragment, bundle)
            }

        }

        binder.loginButton.setOnClickListener {
            binder.editTextPhoneNumber.onEditorAction(EditorInfo.IME_ACTION_DONE)
            binder.editTextPhoneNumber.clearFocus()
            val imm: InputMethodManager =
                binder.root.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binder.root.windowToken, 0)
            signIn()
        }

        binder.resendCodeTextViewLogin.setOnClickListener {
            resendCode()
        }
    }

    private fun resendCode() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            60,
            TimeUnit.SECONDS,
            requireActivity(),
            mCallbacks,
            resendingToken
        )
    }

    private fun signIn() {
        if (binder.editTextPhoneNumber.text.toString().isEmpty()) {
            binder.editTextPhoneNumber.error = getString(R.string.requiredField)
            return
        }
        if (!binder.checkBoxAgree.isChecked) {
            binder.checkBoxAgree.error = getString(R.string.mustAgree)
            return
        }
        phoneNumber = binder.ccPicker.fullNumberWithPlus
        Log.d(TAG, "signIn: $phoneNumber")
        if (phoneNumber.isNotEmpty()) {
            Log.d(TAG, "signIn: phone number is not empty")
            pBarLogin.visibility = View.VISIBLE
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                requireActivity(),
                mCallbacks
            )
        }
    }

    private fun signInWithCredential(authCredential: PhoneAuthCredential) {
        pBarLogin.visibility = View.VISIBLE
        val firebaseAuth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
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
                            val mToken: String? = FirebaseInstanceId.getInstance().token
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

                                    //if profile is Done go to Main Activity
                                    val intent =
                                        Intent(this.requireContext(), MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    requireActivity().finish()
                                    startActivity(intent)
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

                                    val newMap = mutableMapOf<String, Any>()
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

                                val newMap = mutableMapOf<String, Any>()
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
