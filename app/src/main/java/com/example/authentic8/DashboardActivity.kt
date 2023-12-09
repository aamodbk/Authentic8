package com.example.authentic8

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.authentic8.databinding.ActivityDashboardBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.squareup.picasso.Picasso

class DashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        Log.d("Profile Status:", auth.currentUser?.photoUrl.toString())

        Picasso.get().load(auth.currentUser?.photoUrl).into(binding.profileImg)

        binding.textViewUsername.text = auth.currentUser?.displayName

        binding.textViewEmail.text = auth.currentUser?.email

        binding.buttonSignOut.setOnClickListener{
            auth.signOut()
            googleSignInClient.signOut()

            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)

            finish()
        }

        binding.textBtnLogout.setOnClickListener{
            auth.signOut()
            googleSignInClient.signOut()

            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)

            finish()
        }

        binding.buttonFacebook.setOnClickListener{
            auth.signOut()
            googleSignInClient.signOut()

            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)

            finish()
        }

        binding.buttonDelete.setOnClickListener {
            val user = auth.currentUser
            user?.delete()?.addOnCompleteListener {
                if(it.isSuccessful) {
                    googleSignInClient.signOut()
                    Toast.makeText(this, "Account Deleted!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, SignInActivity::class.java)
                    startActivity(intent)
                }
                else {
                    Log.e("Err:", it.exception.toString())
                    Toast.makeText(this, it.exception?.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.textBtnDelete.setOnClickListener {
            val user = auth.currentUser
            user?.delete()?.addOnCompleteListener {
                if(it.isSuccessful) {
                    googleSignInClient.signOut()
                    Toast.makeText(this, "Account Deleted!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, SignInActivity::class.java)
                    startActivity(intent)
                }
                else {
                    Log.e("Err:", it.exception.toString())
                    Toast.makeText(this, it.exception?.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }

    }
}