package com.example.test

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.*

class AddModuleActivity : AppCompatActivity() {
    private val PICK_VIDEO_REQUEST = 1001
    private var selectedVideoUri: Uri? = null
    private lateinit var courseId: String

    private lateinit var edtTitle: EditText
    private lateinit var edtDuration: EditText
    private lateinit var btnPickVideo: Button
    private lateinit var btnUpload: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvVideoName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_add_module)

        courseId = intent.getStringExtra("courseId") ?: ""

        edtTitle = findViewById(R.id.edtModuleTitle)
        edtDuration = findViewById(R.id.edtModuleDuration)
        btnPickVideo = findViewById(R.id.btnPickVideo)
        btnUpload = findViewById(R.id.btnUploadModule)
        progressBar = findViewById(R.id.progressBar)
        tvVideoName = findViewById(R.id.tvVideoName)

        btnPickVideo.setOnClickListener {
            pickVideoFromDevice()
        }

        btnUpload.setOnClickListener {
            uploadModule()
        }
    }

    private fun pickVideoFromDevice() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "video/*"
        startActivityForResult(Intent.createChooser(intent, "Pilih Video"), PICK_VIDEO_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_VIDEO_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedVideoUri = data.data
            val fileName = getFileName(selectedVideoUri!!)
            tvVideoName.text = "Dipilih: $fileName"
        }
    }

    private fun getFileName(uri: Uri): String {
        var name = "video.mp4"
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex != -1) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    private fun uploadModule() {
        val title = edtTitle.text.toString().trim()
        val duration = edtDuration.text.toString().trim()

        if (title.isEmpty() || duration.isEmpty() || selectedVideoUri == null) {
            Toast.makeText(this, "Isi semua data & pilih video", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnUpload.isEnabled = false

        // Upload ke Firebase Storage
        val storageRef = Firebase.storage.reference
        val videoRef = storageRef.child("videos/${UUID.randomUUID()}.mp4")

        videoRef.putFile(selectedVideoUri!!)
            .addOnSuccessListener {
                videoRef.downloadUrl.addOnSuccessListener { uri ->
                    saveModuleToFirestore(title, duration, uri.toString())
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                btnUpload.isEnabled = true
                Toast.makeText(this, "Gagal upload video: ${it.message}", Toast.LENGTH_SHORT).show()
            }
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
                Toast.makeText(this, "Gagal simpan modul: ${it.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                progressBar.visibility = View.GONE
                btnUpload.isEnabled = true
            }
    }
}
