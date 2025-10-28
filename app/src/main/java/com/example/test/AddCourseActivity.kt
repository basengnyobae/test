package com.example.test

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.test.Course

class AddCourseActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_add_course)

        val etTitle = findViewById<EditText>(R.id.etCourseTitle)
        val etThumbnailUrl = findViewById<EditText>(R.id.etCourseThumbnail)
        val etPrice = findViewById<EditText>(R.id.etCoursePrice)
        val btnSave = findViewById<Button>(R.id.btnSaveCourse)

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val thumbnailUrl = etThumbnailUrl.text.toString().trim()
            val priceString = etPrice.text.toString().trim()
            val instructorId = auth.currentUser?.uid ?: return@setOnClickListener

            if (title.isEmpty() || thumbnailUrl.isEmpty() || priceString.isEmpty()) {
                Toast.makeText(this, "Lengkapi semua data (Judul, URL, dan Harga)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val price = priceString.toLongOrNull() ?: 0L

            db.collection("users").document(instructorId)
                .get()
                .addOnSuccessListener { doc ->
                    val instructorName = doc.getString("name") ?: "Instruktur"

                    val newCourse = Course(
                        id = "",
                        title = title,
                        thumbnailUrl = thumbnailUrl,
                        instructor = instructorName,
                        instructorId = instructorId,
                        price = price
                    )

                    db.collection("courses")
                        .add(newCourse)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Course berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Gagal menyimpan: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal mengambil nama pengguna", Toast.LENGTH_SHORT).show()
                }
        }
    }
}