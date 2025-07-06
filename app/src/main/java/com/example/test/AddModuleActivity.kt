package com.example.test

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AddModuleActivity : AppCompatActivity() {
    private lateinit var courseId: String

    private lateinit var edtTitle: EditText
    private lateinit var edtDuration: EditText
    private lateinit var edtVideoUrl: EditText
    private lateinit var btnUpload: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_add_module)

        courseId = intent.getStringExtra("courseId") ?: ""

        edtTitle = findViewById(R.id.edtModuleTitle)
        edtDuration = findViewById(R.id.edtModuleDuration)
        edtVideoUrl = findViewById(R.id.edtVideoUrl)
        btnUpload = findViewById(R.id.btnUploadModule)
        progressBar = findViewById(R.id.progressBar)

        btnUpload.setOnClickListener {
            uploadModule()
        }
    }

    private fun uploadModule() {
        val title = edtTitle.text.toString().trim()
        val duration = edtDuration.text.toString().trim()
        val videoUrl = edtVideoUrl.text.toString().trim()

        if (title.isEmpty() || duration.isEmpty() || videoUrl.isEmpty()) {
            Toast.makeText(this, "Harap isi semua data", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnUpload.isEnabled = false

        saveModuleToFirestore(title, duration, videoUrl)
    }

    private fun saveModuleToFirestore(title: String, duration: String, videoUrl: String) {
        val db = Firebase.firestore
        val moduleData = hashMapOf(
            "title" to title,
            "duration" to duration,
            "videoUrl" to videoUrl,
            "order" to System.currentTimeMillis()
        )

        db.collection("courses").document(courseId)
            .collection("modules")
            .add(moduleData)
            .addOnSuccessListener {
                Toast.makeText(this, "Modul berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menyimpan modul: ${it.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                progressBar.visibility = View.GONE
                btnUpload.isEnabled = true
            }
    }
}
