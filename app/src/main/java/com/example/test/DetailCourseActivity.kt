package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ProgressBar
import com.google.firebase.firestore.DocumentReference


class DetailCourseActivity : AppCompatActivity() {
    private lateinit var enrollButton: Button
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private var courseId = ""
    private var userId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_detail_course)

        courseId = intent.getStringExtra("id") ?: ""
        val title = intent.getStringExtra("title") ?: ""
        val instructor = intent.getStringExtra("instructor") ?: ""
        val thumbnailUrl = intent.getStringExtra("thumbnailUrl") ?: ""
        userId = auth.currentUser?.uid ?: ""

        val tvTitle = findViewById<TextView>(R.id.tvDetailTitle)
        val tvInstructor = findViewById<TextView>(R.id.tvDetailInstructor)
        val ivThumbnail = findViewById<ImageView>(R.id.ivDetailThumbnail)
        enrollButton = findViewById(R.id.btnEnroll)

        val btnAddModule = findViewById<Button>(R.id.btnAddModule)
        if (userId == "3upmgWQdKaYriqezsiP6iK8wck73") {
            btnAddModule.visibility = View.VISIBLE
            btnAddModule.setOnClickListener {
                val intent = Intent(this, AddModuleActivity::class.java)
                intent.putExtra("courseId", courseId)
                startActivity(intent)
            }
        } else {
            btnAddModule.visibility = View.GONE
        }

        tvTitle.text = title
        tvInstructor.text = instructor
        Glide.with(this).load(thumbnailUrl).into(ivThumbnail)

        checkEnrollmentStatus()

        loadModulesAndProgress()

        enrollButton.setOnClickListener {
            enrollToCourse()
        }
    }

    private fun checkEnrollmentStatus() {
        if (userId.isEmpty() || courseId.isEmpty()) return

        val enrollmentId = "${userId}_$courseId"
        db.collection("enrollments").document(enrollmentId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    enrollButton.text = "Sudah Terdaftar"
                    enrollButton.isEnabled = false
                }
            }
    }

    private fun enrollToCourse() {
        if (userId.isEmpty() || courseId.isEmpty()) return

        val enrollmentId = "${userId}_$courseId"
        val data = hashMapOf(
            "userId" to userId,
            "courseId" to courseId,
            "enrolledAt" to Timestamp.now()
        )

        db.collection("enrollments").document(enrollmentId)
            .set(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Berhasil mendaftar kursus", Toast.LENGTH_SHORT).show()
                enrollButton.text = "Sudah Terdaftar"
                enrollButton.isEnabled = false
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mendaftar: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun loadModulesAndProgress() {
        val rvModules = findViewById<RecyclerView>(R.id.rvModules)
        val progressBar = findViewById<ProgressBar>(R.id.progressBarCourse)

        db.collection("courses").document(courseId).collection("modules")
            .orderBy("order")
            .get()
            .addOnSuccessListener { moduleSnapshot ->
                val modules = moduleSnapshot.map { doc ->
                    Module(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        duration = doc.getString("duration") ?: "",
                        order = doc.getLong("order")?.toInt() ?: 0
                    )
                }

                val progressRef = db.collection("progress").document("${userId}_$courseId")
                progressRef.get()
                    .addOnSuccessListener { progressDoc ->
                        val completed = progressDoc.get("completedModuleIds") as? List<String> ?: emptyList()

                        val adapter = ModuleAdapter(modules, completed) { module, checked ->
                            updateProgress(progressRef, module.id, checked)
                        }

                        rvModules.layoutManager = LinearLayoutManager(this)
                        rvModules.adapter = adapter

                        val percent = if (modules.isNotEmpty())
                            (completed.size * 100) / modules.size
                        else 0
                        progressBar.progress = percent
                    }
            }
    }

    private fun updateProgress(ref: DocumentReference, moduleId: String, checked: Boolean) {
        db.runTransaction { transaction ->
            val snapshot = transaction.get(ref)
            val current = snapshot.get("completedModuleIds") as? MutableList<String> ?: mutableListOf()

            if (checked) {
                if (!current.contains(moduleId)) current.add(moduleId)
            } else {
                current.remove(moduleId)
            }

            transaction.set(ref, mapOf(
                "userId" to userId,
                "courseId" to courseId,
                "completedModuleIds" to current
            ))

            null
        }.addOnSuccessListener {
            loadModulesAndProgress() // refresh progress
        }
    }
}
