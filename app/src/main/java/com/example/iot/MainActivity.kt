package com.example.iot

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.iot.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider


class MainActivity : AppCompatActivity() {
    private var googleSignInClient: GoogleSignInClient?=null

    private var mBinding: ActivityMainBinding? = null
    private val binding get() = mBinding!!


    private lateinit var activityLauncher: ActivityResultLauncher<Intent>
    private lateinit var auth: FirebaseAuth



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("509256394910-fd6hedh7vh7mffu4q0bd022o6689pllk.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    task.getResult(ApiException::class.java)?.let { account ->
                        val tokenId = account.idToken
                        if (tokenId != null && tokenId != "") {
                            val credential: AuthCredential = GoogleAuthProvider.getCredential(account.idToken, null)
                            auth.signInWithCredential(credential)
                                .addOnCompleteListener {
                                    if (auth.currentUser != null) {
                                        val user: FirebaseUser = auth.currentUser!!
                                        val name = user.displayName.toString()
                                        val email = user.email.toString()
                                        val uid = user.uid
                                        val photoUrl = user.photoUrl.toString()
                                        val googleSignInToken = account.idToken ?: ""
                                        if (googleSignInToken != "") {
                                            Log.e(TAG, "googleSignInToken : $googleSignInToken")
                                        } else {
                                            Log.e(TAG, "googleSignInToken이 null")
                                        }

                                        val intent = Intent(this, ReservationActivity::class.java)

                                        intent.putExtra("Uid", uid)

                                        intent.putExtra("Name", name)
                                        intent.putExtra("Email", email)
                                        intent.putExtra("PhotoUrl", photoUrl)

                                        startActivity(intent)

                                    }
                                }
                        }
                    } ?: throw Exception()
                }   catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        binding.googleSignInButton.setOnClickListener {
            activityLauncher.launch(googleSignInClient!!.signInIntent)
        }



    } // create



    override fun onDestroy() {
        super.onDestroy()
        firebaseAuthSignOut()
    }

    private fun firebaseAuthSignOut() {
        auth.signOut()
    }






}


