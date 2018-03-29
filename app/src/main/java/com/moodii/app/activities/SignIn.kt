package com.moodii.app.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.moodii.app.R
import android.content.Intent
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task

private const val RC_SIGN_IN = 107 // request code for starting sign on activity (this can be any number).

class SignIn : AppCompatActivity() {
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Configure sign-in options
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()

        // Build GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        //if signOut was called, explicitly sign out and prompt user for account
        if(this.intent.hasExtra("signOut")) mGoogleSignInClient.signOut()

        // Handle Sign in button click.  Intent prompts the user to select a Google account to sign in with
        findViewById<SignInButton>(R.id.sign_in_button).setOnClickListener{ startActivityForResult(mGoogleSignInClient.signInIntent, RC_SIGN_IN) }
        }

    override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) startMoodii(account)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) { // If a sign in (Result code from launching the Intent from mGoogleSignInClient.signInIntent)
            handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(data))
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI.
            startMoodii(account)

        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("LOGIN", "signInResult:failed code=" + e.statusCode)
        }
    }

    private fun startMoodii(account: GoogleSignInAccount) {
        val intent = Intent(this, MoodAvatar::class.java)
        intent.putExtra("accountId",account.id) //nts.. should be verifying properly with tokenid
        startActivity(intent)
        overridePendingTransition(0, 0) //stop flicker on activity change
        finish() //forget back button
    }
}

