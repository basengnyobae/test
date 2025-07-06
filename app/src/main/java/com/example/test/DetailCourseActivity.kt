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
    private lateinit var enrollButton: Button
    private lateinit var btnUnenroll: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var rvModules: RecyclerView
    private lateinit var btnAddModule: Button
    private lateinit var btnEditCourse: Button
    private lateinit var btnDeleteCourse: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_detail_course)

        // Ambil ID dari intent & user login
        courseId = intent.getStringExtra("id") ?: ""
        userId = auth.currentUser?.uid ?: ""

        if (courseId.isEmpty() || userId.isEmpty()) {
            Toast.makeText(this, "Data tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Inisialisasi UI
        val tvTitle = findViewById<TextView>(R.id.tvDetailTitle)
        val tvInstructor = findViewById<TextView>(R.id.tvDetailInstructor)
        val ivThumbnail = findViewById<ImageView>(R.id.ivDetailThumbnail)
        enrollButton = findViewById(R.id.btnEnroll)
        progressBar = findViewById(R.id.progressBarCourse)
        rvModules = findViewById(R.id.rvModules)
        btnAddModule = findViewById(R.id.btnAddModule)
        btnEditCourse = findViewById(R.id.btnEditCourse)
        btnDeleteCourse = findViewById(R.id.btnDeleteCourse)
        btnUnenroll = findViewById(R.id.btnUnenroll)
        btnUnenroll.visibility = View.GONE

        // Sembunyikan tombol terlebih dahulu
        btnAddModule.visibility = View.GONE
        btnEditCourse.visibility = View.GONE
        btnDeleteCourse.visibility = View.GONE

        // Ambil data course
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

                // Tampilkan tombol jika user adalah instruktur
                if (userId == instructorId) {
                    btnAddModule.visibility = View.VISIBLE
                    btnEditCourse.visibility = View.VISIBLE
                    btnDeleteCourse.visibility = View.VISIBLE

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

                    btnDeleteCourse.setOnClickListener {
                        deleteCourse(courseId)
                    }
                }

                // Lanjut ke progress & modul
                checkEnrollmentStatus()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat data kursus", Toast.LENGTH_SHORT).show()
                finish()
            }

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
                    // Sudah terdaftar
                    enrollButton.text = "Sudah Terdaftar"
                    enrollButton.isEnabled = false
                    btnUnenroll.visibility = View.VISIBLE

                    btnUnenroll.setOnClickListener {
                        unenrollFromCourse()
                    }

                    // Load modul hanya jika sudah daftar
                    loadModulesAndProgress()
                } else {
                    // Belum terdaftar
                    enrollButton.text = "Daftar Sekarang"
                    enrollButton.isEnabled = true
                    btnUnenroll.visibility = View.GONE

                    // Jangan tampilkan modul
                    rvModules.visibility = View.GONE
                    progressBar.visibility = View.GONE
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
                checkEnrollmentStatus()
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
                        order = doc.getLong("order")?.toInt() ?: 0,
                        videoUrl = doc.getString("videoUrl") ?: ""
                    )
                }

                val progressRef = db.collection("progress").document("${userId}_$courseId")
                progressRef.get()
                    .addOnSuccessListener { progressDoc ->
                        val completed = progressDoc.get("completedModuleIds") as? List<String> ?: emptyList()

                        val adapter = ModuleAdapter(
                            modules,
                            completed,
                            onChecked = { module, checked ->
                                updateProgress(progressRef, module.id, checked)
                            },
                            onDelete = { module ->
                                deleteModule(courseId, module.id)
                            },
                            onItemClick = { module ->
                                val intent = Intent(this, VideoPlayerActivity::class.java)
                                intent.putExtra("title", module.title)
                                intent.putExtra("videoUrl", module.videoUrl) // pastikan field ini ada di model
                                startActivity(intent)
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
            loadModulesAndProgress()
        }
    }

    private fun deleteCourse(courseId: String) {
        val courseRef = db.collection("courses").document(courseId)
        val modulesRef = courseRef.collection("modules")

        modulesRef.get()
            .addOnSuccessListener { moduleSnap ->
                val batch = db.batch()
                for (doc in moduleSnap) {
                    batch.delete(doc.reference)
                }

                batch.commit().addOnSuccessListener {
                    courseRef.delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Course berhasil dihapus", Toast.LENGTH_SHORT).show()
                            finish()
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

    private fun deleteModule(courseId: String, moduleId: String) {
        db.collection("courses")
            .document(courseId)
            .collection("modules")
            .document(moduleId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Modul berhasil dihapus", Toast.LENGTH_SHORT).show()
                loadModulesAndProgress()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menghapus modul: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun unenrollFromCourse() {
        val enrollmentId = "${userId}_$courseId"
        db.collection("enrollments").document(enrollmentId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Berhasil membatalkan pendaftaran", Toast.LENGTH_SHORT).show()
                enrollButton.text = "Daftar Sekarang"
                enrollButton.isEnabled = true
                btnUnenroll.visibility = View.GONE
                checkEnrollmentStatus()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal membatalkan pendaftaran: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
