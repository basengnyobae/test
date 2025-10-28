package com.example.test

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EditCourseActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private lateinit var courseId: String

    private lateinit var etTitle: EditText
    private lateinit var etThumbnail: EditText
    private lateinit var etPrice: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_edit_course)

        etTitle = findViewById(R.id.etEditTitle)
        etThumbnail = findViewById(R.id.etEditThumbnail)
        etPrice = findViewById(R.id.etEditPrice)
        val btnSave = findViewById<Button>(R.id.btnSaveEdit)

        courseId = intent.getStringExtra("courseId") ?: ""
        if (courseId.isEmpty()) {
            Toast.makeText(this, "ID Course tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        db.collection("courses").document(courseId).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    etTitle.setText(doc.getString("title") ?: "")
                    etThumbnail.setText(doc.getString("thumbnailUrl") ?: "")

                    val currentPrice = doc.getLong("price") ?: 0L
                    etPrice.setText(currentPrice.toString())
                } else {
                    Toast.makeText(this, "Course tidak ditemukan", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat data: ${it.message}", Toast.LENGTH_SHORT).show()
            }

        btnSave.setOnClickListener {
            val newTitle = etTitle.text.toString().trim()
            val newThumbnail = etThumbnail.text.toString().trim()
            val newPriceString = etPrice.text.toString().trim()

            if (newTitle.isEmpty() || newThumbnail.isEmpty() || newPriceString.isEmpty()) {
                Toast.makeText(this, "Lengkapi semua data (Judul, URL, dan Harga)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newPrice = newPriceString.toLongOrNull() ?: 0L

            val updatedData = mapOf(
                "title" to newTitle,
                "thumbnailUrl" to newThumbnail,
                "price" to newPrice
            )

            db.collection("courses").document(courseId)
                .update(updatedData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Course berhasil diupdate", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}