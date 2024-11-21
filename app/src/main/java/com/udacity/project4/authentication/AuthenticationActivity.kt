package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity


/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    companion object {
        const val SIGN_IN_CODE = 1
    }

    private lateinit var binding: ActivityAuthenticationBinding
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)



        if (FirebaseAuth.getInstance().currentUser != null) {

            startRemindersActivity()
            return
        }

        // Registering the ActivityResultLauncher to handle sign-in result
        signInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val response = IdpResponse.fromResultIntent(result.data)
            if (result.resultCode == Activity.RESULT_OK) {
                // Successful sign-in, navigate to the RemindersActivity
                Log.i("onActivityResult", "Successfully Signed in")
                startRemindersActivity()
            } else {
                // Sign-in failed, handle the error cases
                if (response == null) {
                    Log.i("onActivityResult", "Back button pressed")
                    return@registerForActivityResult
                }
                if (response.error?.errorCode == ErrorCodes.NO_NETWORK) {
                    Log.i("onActivityResult", "No Network")
                    Snackbar.make(
                        binding.root,
                        getString(R.string.no_network),
                        Snackbar.LENGTH_SHORT
                    ).show()


                } else {
                    Log.i("onActivityResult", "Sign-in Error: ${response.error?.message}")
                    Snackbar.make(binding.root, R.string.error_happened, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry) {
                            launchSignInFlow()
                        }.show()

                }
            }
        }

        // Observe the authentication state
        viewModel.authenticationState.observe(this, Observer { authenticationState ->
            if (authenticationState == LoginViewModel.AuthenticationState.UNAUTHENTICATED) {
                // If user is unauthenticated, prompt to login
                binding.login.setOnClickListener {
                    launchSignInFlow()
                }
            } else if (authenticationState == LoginViewModel.AuthenticationState.AUTHENTICATED) {
                // If user is authenticated, directly start the RemindersActivity
                binding.login.setOnClickListener {
                    startRemindersActivity()
                }
            }
        })
    }

    // Launches the sign-in flow using Firebase Authentication with email and Google sign-in providers
    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create the intent for FirebaseUI and launch it using the registered ActivityResultLauncher
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()

        signInLauncher.launch(signInIntent)  // Use the launcher to start the sign-in activity
    }

    // Navigate to RemindersActivity when authentication is successful
    private fun startRemindersActivity() {
        val intent = Intent(this, RemindersActivity::class.java)
        startActivity(intent)
        finish()
    }
}

