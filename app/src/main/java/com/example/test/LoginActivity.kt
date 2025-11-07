package com.example.test

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val email = findViewById<EditText>(R.id.etEmail).text.toString()
            val password = findViewById<EditText>(R.id.etPassword).text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser

                            if (user != null && user.isEmailVerified) {
                                db.collection("users").document(user.uid).get()
                                    .addOnSuccessListener { document ->
                                        val userRole = document.getString("role") ?: "user"
                                        saveUserRoleToPreferences(userRole)

                                        val intent: Intent
                                        if (userRole == "admin") {
                                            intent = Intent(this, AdminHomeActivity::class.java)
                                        } else {
                                            intent = Intent(this, MainActivity::class.java)
                                        }

                                        Toast.makeText(this, "Login Berhasil.", Toast.LENGTH_SHORT).show()
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                                        startActivity(intent)
                                        finish()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "Gagal mengambil data profil.", Toast.LENGTH_SHORT).show()
                                        auth.signOut()
                                    }
                            } else {
                                showVerificationWarningDialog(user)
                            }

                        } else {
                            Toast.makeText(this, "Login gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Isi email dan password", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        findViewById<TextView>(R.id.tvForgotPassword).setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun showVerificationWarningDialog(user: com.google.firebase.auth.FirebaseUser?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Email Belum Diverifikasi")
        builder.setMessage("Email Anda belum terverifikasi. Silakan cek inbox (dan folder spam) Anda. Anda tidak bisa login sebelum email terverifikasi.")
        builder.setCancelable(false)

        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            auth.signOut()
        }

        builder.setNegativeButton("Kirim Ulang Email") { dialog, _ ->
            user?.sendEmailVerification()
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Email verifikasi baru telah dikirim.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Gagal mengirim ulang. Coba lagi nanti.", Toast.LENGTH_SHORT).show()
                    }
                }
            auth.signOut()
        }
        builder.show()
    }

    private fun saveUserRoleToPreferences(role: String) {
        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putString("USER_ROLE", role)
            apply()
        }
    }

    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Lupa Password?")
        builder.setMessage("Masukkan email Anda untuk menerima link reset password.")

        val input = EditText(this)
        input.hint = "Email Anda"
        input.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        builder.setView(input)

        builder.setPositiveButton("Kirim Link") { dialog, _ ->
            val email = input.text.toString().trim()
            if (email.isNotEmpty()) {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Link reset password telah terkirim ke $email", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Gagal mengirim link: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Batal") { dialog, _ -> dialog.cancel() }
        builder.show()
    }
}