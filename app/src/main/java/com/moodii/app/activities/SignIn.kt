package com.moodii.app.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.moodii.app.R
import android.content.Intent
import android.graphics.drawable.Animatable
import android.util.Log
import android.widget.ImageView
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes.getStatusCodeString
import com.google.android.gms.tasks.Task
import com.moodii.app.api.MoodiiApi

private const val RC_SIGN_IN = 107 // request code for starting sign on activity (this can be any number).

class SignIn : AppCompatActivity() {
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Configure sign-in options
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id)) //backend client_id setup on dev. console
                .requestEmail()
                .build()

        // Build GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        //if signOut was called, explicitly sign out and prompt user for account
        if(this.intent.hasExtra("signOut")) {
            this.intent.removeExtra("signOut")
            mGoogleSignInClient.signOut()
        }

        // Handle Sign in button click.  Intent prompts the user to select a Google account to sign in with
        findViewById<SignInButton>(R.id.sign_in_button).setOnClickListener{ startActivityForResult(mGoogleSignInClient.signInIntent, RC_SIGN_IN) }
        }

    override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) startMoodii(account)

        //show animate swipe icon
        val splash = findViewById<ImageView>(R.id.splashMoodii)
        val draw = splash.drawable
        if (draw is Animatable) {
            draw.start()
        }

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
            Toast.makeText(this,  "Oops! " + getStatusCodeString(e.statusCode), Toast.LENGTH_SHORT).show()
        }
    }

    private fun startMoodii(account: GoogleSignInAccount) {
        val intent: Intent
        val token: String? = account.idToken
        Log.w("LOGIN","idToken is " + account.idToken)
        if (token != null) {
            val (resultCode, mooderId) = MoodiiApi.getId(token)
            Log.w("LOGIN","MooderId is " + mooderId)
            when (resultCode) {
                200 -> { //retrieved existing mooder
                    intent = Intent(this, MoodAvatar::class.java)
                    intent.putExtra("mooderId", mooderId)
                    intent.putExtra("reloadMooder", true)
                }
                201 -> { //new mooder
                    intent = Intent(this, EditAvatar::class.java)
                    intent.putExtra("mooderId", mooderId)
                    intent.putExtra("reloadMooder", true)
                }
                else -> { //no mooder Id, login again
                    intent = Intent(this, SignIn::class.java)
                    intent.putExtra("signOut",true)
                    Toast.makeText(applicationContext, "Failed (Internet connection active?)", Toast.LENGTH_SHORT).show()
                }
            }
        } else { //no google token, login again
            intent = Intent(this, SignIn::class.java)
            intent.putExtra("signOut",true)
        }
        startActivity(intent)
        finish() //forget back button
    }
}

