package com.example.authentic8

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.authentic8.databinding.ActivitySigninBinding
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySigninBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private lateinit var buttonFacebookLogin: LoginButton

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Toast.makeText(this, "Account Logged In", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FacebookSdk.sdkInitialize(applicationContext)
        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.buttonGoogle.setOnClickListener {
            googleSignIn()
        }

        binding.buttonSpotify.setOnClickListener {
            spotifySignIn()
        }

        buttonFacebookLogin = binding.buttonFacebook

        callbackManager = CallbackManager.Factory.create()

        buttonFacebookLogin.setReadPermissions("email", "public_profile")
        buttonFacebookLogin.registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    Log.d(TAG, "facebook:onSuccess:$result")
                    handleFacebookAccessToken(result.accessToken)
                }

                override fun onCancel() {
                    Log.d(TAG, "facebook:onCancel")
                }

                override fun onError(error: FacebookException) {
                    Log.d(TAG, "facebook:onError", error)
                }
            },
        )

        binding.buttonSignUp.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.buttonLogin.setOnClickListener {
            val email = binding.editEmailInput.text.toString()
            val password = binding.editPasswordInput.text.toString()
            if(checkAllField()) {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener{
                    if(it.isSuccessful) {
                        Toast.makeText(this, "Signed In", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, DashboardActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else {
                        Log.e("Err:", it.exception.toString())
                        Toast.makeText(this, it.exception?.message.toString(), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        binding.buttonForgotPwd.setOnClickListener {
            val email = binding.editEmailInput.text.toString()
            if(checkEmail()) {
                auth.sendPasswordResetEmail(email).addOnCompleteListener {
                    if(it.isSuccessful) {
                        Toast.makeText(this, "Password Reset Email Sent!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun googleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            result(task)
        }
    }

    private fun result(task: Task<GoogleSignInAccount>) {
        if(task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
            auth.signInWithCredential(credential).addOnCompleteListener {
                if(it.isSuccessful) {
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else {
                    Log.e("Err:", it.exception.toString())
                    Toast.makeText(this, it.exception?.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun spotifySignIn() {
        val builder = AuthorizationRequest.Builder(
            SpotifyConstants.CLIENT_ID,
            AuthorizationResponse.Type.TOKEN,
            SpotifyConstants.REDIRECT_URI
        )
        builder.setScopes(arrayOf("user-read-email"))
        val request = builder.build()

        AuthorizationClient.openLoginActivity(
            this,
            SpotifyConstants.AUTH_TOKEN_REQUEST_CODE,
            request
        )
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        task.exception.toString(),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        // Check if result comes from the correct activity
        if (requestCode == SpotifyConstants.AUTH_TOKEN_REQUEST_CODE) {
            val response = AuthorizationClient.getResponse(resultCode, intent)
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    Log.d("Status: ", "Successful Sign in")
                    val accessToken: String? = response.accessToken
                    val intent = Intent(this, SpotifyDashboardActivity::class.java)
                    intent.putExtra("AccessToken", accessToken)
                    startActivity(intent)

                    finish()
                }
                AuthorizationResponse.Type.ERROR -> {
                    Log.d("Status: ", "Error Signing in")
                }
                else -> {}
            }
        }
        callbackManager.onActivityResult(requestCode, resultCode, intent)
    }

    private fun checkEmail(): Boolean {
        val email = binding.editEmailInput.text.toString()
        if (binding.editEmailInput.text.toString() == "") {
            binding.editEmail.error = "Required"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editEmail.error = "Check Email Format"
            return false
        }
        return true
    }

    private fun checkAllField(): Boolean {
        val email = binding.editEmailInput.text.toString()
        if (binding.editEmailInput.text.toString() == "") {
            binding.editEmail.error = "Required"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editEmail.error = "Check Email Format"
            return false
        }
        if (binding.editPasswordInput.text.toString() == "") {
            binding.editPassword.error = "Required"
            return false
        }
        return true
    }
}