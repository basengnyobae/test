package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DetailCourseActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private var courseId = ""
    private var userId = ""
    private lateinit var instructorId: String
    private lateinit var enrollButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var rvModules: RecyclerView
    private lateinit var btnAddModule: Button
    private lateinit var btnEditCourse: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_detail_course)

        courseId = intent.getStringExtra("id") ?: ""
        userId = auth.currentUser?.uid ?: ""

        if (courseId.isEmpty() || userId.isEmpty()) {
            Toast.makeText(this, "Data tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val tvTitle = findViewById<TextView>(R.id.tvDetailTitle)
        val tvInstructor = findViewById<TextView>(R.id.tvDetailInstructor)
        val ivThumbnail = findViewById<ImageView>(R.id.ivDetailThumbnail)
        enrollButton = findViewById(R.id.btnEnroll)
        progressBar = findViewById(R.id.progressBarCourse)
        rvModules = findViewById(R.id.rvModules)
        btnAddModule = findViewById(R.id.btnAddModule)
        btnEditCourse = findViewById(R.id.btnEditCourse)

        // Default hide buttons
        btnAddModule.visibility = View.GONE
        btnEditCourse.visibility = View.GONE

        // Load course detail and setup UI
        db.collection("courses").document(courseId).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, "Course tidak ditemukan", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                val title = doc.getString("title") ?: ""
                val instructor = doc.getString("instructor") ?: ""
                val thumbnailUrl = doc.getString("thumbnailUrl") ?: ""
                val instructorId = doc.getString("instructorId") ?: ""

                tvTitle.text = title
                tvInstructor.text = instructor
                Glide.with(this).load(thumbnailUrl).into(ivThumbnail)

                // Tampilkan tombol edit dan tambah modul jika user adalah pemilik course
                if (userId == instructorId) {
                    btnAddModule.visibility = View.VISIBLE
                    btnEditCourse.visibility = View.VISIBLE

                    btnAddModule.setOnClickListener {
                        val intent = Intent(this, AddModuleActivity::class.java)
                        intent.putExtra("courseId", courseId)
                        startActivity(intent)
                    }

                    btnEditCourse.setOnClickListener {
                        val intent = Intent(this, EditCourseActivity::class.java)
                        intent.putExtra("courseId", courseId)
                        startActivity(intent)
                    }
                }

                // Setelah detail berhasil dimuat, lanjut ke progress & modules
                checkEnrollmentStatus()
                loadModulesAndProgress()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat data kursus", Toast.LENGTH_SHORT).show()
                finish()
            }

        enrollButton.setOnClickListener {
            enrollToCourse()

            val btnDeleteCourse = findViewById<Button>(R.id.btnDeleteCourse)
            btnDeleteCourse.visibility = View.GONE

            if (userId == instructorId) {
                btnDeleteCourse.visibility = View.VISIBLE
                btnDeleteCourse.setOnClickListener {
                    deleteCourse(courseId)
                }
            }

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

    private fun deleteCourse(courseId: String) {
        // Hapus semua modul terlebih dahulu
        val courseRef = db.collection("courses").document(courseId)
        val modulesRef = courseRef.collection("modules")

        modulesRef.get()
            .addOnSuccessListener { moduleSnap ->
                val batch = db.batch()
                for (doc in moduleSnap) {
                    batch.delete(doc.reference)
                }

                // Setelah modul dihapus, hapus course-nya
                batch.commit().addOnSuccessListener {
                    courseRef.delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Course berhasil dihapus", Toast.LENGTH_SHORT).show()
                            finish() // kembali ke fragment sebelumnya
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Gagal menghapus course: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat modul: ${it.message}", Toast.LENGTH_SHORT).show()
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

                        val adapter = ModuleAdapter(modules, completed,
                            onChecked = { module, checked ->
                                updateProgress(progressRef, module.id, checked)
                            },
                            onDelete = { module ->
                                deleteModule(courseId, module.id)
                            }
                        )

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

            if (checked && !current.contains(moduleId)) {
                current.add(moduleId)
            } else if (!checked) {
                current.remove(moduleId)
            }

            transaction.set(ref, mapOf(
                "userId" to userId,
                "courseId" to courseId,
                "completedModuleIds" to current
            ))

            null
        }.addOnSuccessListener {
            loadModulesAndProgress() // refresh
        }
    }
    private fun deleteModule(courseId: String, moduleId: String) {
        db.collection("courses")
            .document(courseId)
            .collection("modules")
            .document(moduleId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Modul berhasil dihapus", Toast.LENGTH_SHORT).show()
                loadModulesAndProgress() // Refresh setelah dihapus
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menghapus modul: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
