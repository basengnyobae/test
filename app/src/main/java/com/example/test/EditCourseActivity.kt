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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_edit_course)

        val etTitle = findViewById<EditText>(R.id.etEditTitle)
        val etThumbnail = findViewById<EditText>(R.id.etEditThumbnail)
        val btnSave = findViewById<Button>(R.id.btnSaveEdit)

        courseId = intent.getStringExtra("courseId") ?: return

        db.collection("courses").document(courseId).get()
            .addOnSuccessListener { doc ->
                etTitle.setText(doc.getString("title") ?: "")
                etThumbnail.setText(doc.getString("thumbnailUrl") ?: "")
            }

        btnSave.setOnClickListener {
            val newTitle = etTitle.text.toString().trim()
            val newThumbnail = etThumbnail.text.toString().trim()

            db.collection("courses").document(courseId)
                .update(
                    mapOf(
                        "title" to newTitle,
                        "thumbnailUrl" to newThumbnail
                    )
                )
                .addOnSuccessListener {
                    Toast.makeText(this, "Course updated", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Update failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
