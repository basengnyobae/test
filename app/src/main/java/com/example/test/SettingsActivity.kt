package com.example.test

import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        val btnChangePassword = findViewById<Button>(R.id.btnChangePassword)

        btnChangePassword.setOnClickListener {
            val user = auth.currentUser
            if (user != null && user.email != null) {
                btnChangePassword.isEnabled = false
                btnChangePassword.text = "Mengirim..."

                auth.sendPasswordResetEmail(user.email!!)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Link reset password terkirim ke: ${user.email}", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Gagal mengirim link: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            btnChangePassword.isEnabled = true
                            btnChangePassword.text = "Kirim Link Ubah Password"
                        }
                    }
            } else {
                Toast.makeText(this, "Error: User tidak ditemukan atau tidak punya email.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}