package com.example.authentic8

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.authentic8.databinding.ActivitySpotifyBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class SpotifyDashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySpotifyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpotifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        val extras = intent.extras
        if (extras != null) {
            val token = extras.getString("AccessToken")
            fetchSpotifyUserProfile(token, binding)
        }

        binding.buttonSignOut.setOnClickListener{
            auth.signOut()

            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)

            finish()
        }

        binding.textBtnLogout.setOnClickListener{
            auth.signOut()

            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)

            finish()
        }

    }

    private fun fetchSpotifyUserProfile(token: String?, binding: ActivitySpotifyBinding) {
        Log.d("Status: ", "Please Wait...")
        if (token == null) {
            Log.i("Status: ", "Something went wrong - No Access Token found")
            return
        }
        val getUserProfileURL = "https://api.spotify.com/v1/me"
        GlobalScope.launch(Dispatchers.Default) {
            val url = URL(getUserProfileURL)
            val httpsURLConnection = withContext(Dispatchers.IO) {url.openConnection() as HttpsURLConnection }
            httpsURLConnection.requestMethod = "GET"
            httpsURLConnection.setRequestProperty("Authorization", "Bearer $token")
            httpsURLConnection.doInput = true
            httpsURLConnection.doOutput = false
            val response = httpsURLConnection.inputStream.bufferedReader()
                .use { it.readText() }  // defaults to UTF-8
            withContext(Dispatchers.Main) {
                val jsonObject = JSONObject(response)
                // Spotify Id
                val spotifyId = jsonObject.getString("id")
                Log.d("Spotify Id :", spotifyId)
                // Spotify Display Name
                val spotifyDisplayName = jsonObject.getString("display_name")
                Log.d("Spotify Display Name :", spotifyDisplayName)
                binding.textViewUsername.text = spotifyDisplayName
                // Spotify Email
                val spotifyEmail = jsonObject.getString("email")
                Log.d("Spotify Email :", spotifyEmail)
                binding.textViewEmail.text = spotifyEmail

                val spotifyAvatarArray = jsonObject.getJSONArray("images")

                var spotifyAvatarURL = ""
                if (spotifyAvatarArray.length() > 0) {
                    spotifyAvatarURL = spotifyAvatarArray.getJSONObject(0).getString("url")
                    Log.d("Spotify Avatar : ", spotifyAvatarURL)
                    Picasso.get().load(spotifyAvatarURL).into(binding.profileImg)
                }
                Log.d("Spotify AccessToken :", token)

            }
        }
    }
}