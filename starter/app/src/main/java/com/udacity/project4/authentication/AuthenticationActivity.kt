package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {
    companion object {
        const val TAG = "Authentication State"
    }

    private lateinit var authBinding:ActivityAuthenticationBinding
    private val auth = FirebaseAuth.getInstance()

    private val loginResultLauncher =
        registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
            val response = IdpResponse.fromResultIntent(result.data)
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = Intent(this, RemindersActivity::class.java)
                startActivity(intent)
                Log.i(TAG, "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!")
                finish()
            } else {
                Log.i(TAG, "Error! Couldn't sign in ${response?.error?.errorCode}")
//                if (idpResponse == null) {
//                    showSnackBar("Login cancelled")
//                } else {
//                    showSnackBar(
//                        idpResponse.error?.localizedMessage
//                            ?: "An unknown error occurred while trying to log in."
//                    )
//                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authBinding=ActivityAuthenticationBinding.inflate(layoutInflater)
        if (auth.currentUser!= null) {
            val intent = Intent(this, RemindersActivity::class.java)
            startActivity(intent)
            Log.i(TAG, "logged in")
        } else {
            setContentView(authBinding.root)
            launchSignInFlow()
        }
    }

    private fun launchSignInFlow() {
        val providers= arrayListOf(AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build())

        authBinding.loginBtn.setOnClickListener {
            loginResultLauncher.launch(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build(),
            )
        }

    }
}


