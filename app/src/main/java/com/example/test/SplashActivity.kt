package com.example.test

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()

        auth = Firebase.auth

        Handler(Looper.getMainLooper()).postDelayed({
            checkUserStatus()
        }, 1500)
    }

    private fun checkUserStatus() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            goToActivity(LoginActivity::class.java)
        } else {
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    val userRole = document?.getString("role") ?: "user"

                    saveUserRoleToPreferences(userRole)

                    if (userRole == "admin") {
                        goToActivity(AdminHomeActivity::class.java)
                    } else {
                        goToActivity(MainActivity::class.java)
                    }
                }
                .addOnFailureListener {
                    saveUserRoleToPreferences("user")
                    goToActivity(MainActivity::class.java)
                }
        }
    }

    private fun goToActivity(targetActivity: Class<*>) {
        val intent = Intent(this, targetActivity)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun saveUserRoleToPreferences(role: String) {
        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putString("USER_ROLE", role)
            apply()
        }
    }
}