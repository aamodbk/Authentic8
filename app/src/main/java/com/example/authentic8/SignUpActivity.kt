package com.example.authentic8

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.authentic8.databinding.ActivitySignupBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage


class SignupActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignupBinding
    private lateinit var storage: FirebaseStorage
    private lateinit var profile: String

    companion object {
        val IMG_REQ_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        storage = Firebase.storage

        binding.buttonLogin.setOnClickListener {
            val email = binding.editEmailInput.text.toString()
            val password = binding.editPasswordInputConfirm.text.toString()
            if (checkAllField()) {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val user = Firebase.auth.currentUser

                        val profileUpdates = userProfileChangeRequest {
                            displayName = binding.editUsernameInput.text.toString()
                            photoUri = Uri.parse(profile)
                        }

                        user!!.updateProfile(profileUpdates)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("Profile Status:", "User profile updated.")
                                    Toast.makeText(this, "Account Created", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, DashboardActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                    } else {
                        Log.e("Err:", it.exception.toString())
                        Toast.makeText(this, it.exception?.message.toString(), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        binding.buttonProfile.setOnClickListener {
            pickImgfromGallery()
        }
    }

    private fun pickImgfromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMG_REQ_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == IMG_REQ_CODE) {
            binding.profileImg.setImageURI(data?.data)
            profile = data?.data.toString()
        }
    }

    private fun checkAllField(): Boolean {
        val email = binding.editEmailInput.text.toString()
        if (binding.editUsernameInput.text.toString() == "") {
            binding.editUsername.error = "Required"
            return false
        }
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
        if (binding.editPasswordInput.length() <= 6) {
            binding.editPassword.error = "Password should be at-least 7 characters long"
            return false
        }
        if (binding.editPasswordInputConfirm.text.toString() == "") {
            binding.editPasswordConfirm.error = "Required"
            return false
        }
        if (binding.editPasswordInput.text.toString() != binding.editPasswordInputConfirm.text.toString()) {
            binding.editPasswordConfirm.error = "Password Does not Match"
            return false
        }
        return true
    }
}