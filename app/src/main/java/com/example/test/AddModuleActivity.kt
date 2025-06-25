package com.example.test

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AddModuleActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private lateinit var courseId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_add_module)

        courseId = intent.getStringExtra("courseId") ?: ""

        val etTitle = findViewById<EditText>(R.id.etModuleTitle)
        val etDuration = findViewById<EditText>(R.id.etDuration)
        val etOrder = findViewById<EditText>(R.id.etOrder)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitModule)

        btnSubmit.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val duration = etDuration.text.toString().trim()
            val order = etOrder.text.toString().trim().toIntOrNull()

            if (title.isNotEmpty() && duration.isNotEmpty() && order != null) {
                val data = hashMapOf(
                    "title" to title,
                    "duration" to duration,
                    "order" to order
                )

                db.collection("courses")
                    .document(courseId)
                    .collection("modules")
                    .add(data)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Modul berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Lengkapi semua kolom dengan benar", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
