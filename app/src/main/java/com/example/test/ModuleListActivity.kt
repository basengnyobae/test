package com.example.test

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.util.Log

class ModuleListActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private lateinit var courseId: String
    private lateinit var tvTitle: TextView
    private lateinit var rvModules: RecyclerView
    private lateinit var progressBar: ProgressBar

    private var userId = ""
    private var userRole = "user"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_module_list)

        courseId = intent.getStringExtra("COURSE_ID") ?: ""
        Log.d("DEBUG", "COURSE ID YANG DITERIMA: '$courseId'")
        Toast.makeText(this, "COURSE ID = $courseId", Toast.LENGTH_LONG).show()
        val courseTitle = intent.getStringExtra("COURSE_TITLE") ?: "Modul Kursus"

        userId = auth.currentUser?.uid ?: ""
        userRole = readUserRoleFromPreferences()

        supportActionBar?.title = "Modul: $courseTitle"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tvTitle = findViewById(R.id.tvCourseTitle)
        rvModules = findViewById(R.id.rvModulesStandalone)
        progressBar = findViewById(R.id.progressBarModuleList)

        tvTitle.text = courseTitle
        Log.d("DEBUG", "courseId: $courseId")
        Toast.makeText(this, "courseId: $courseId", Toast.LENGTH_LONG).show()
        loadModulesAndProgress()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun readUserRoleFromPreferences(): String {
        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return sharedPref.getString("USER_ROLE", "user") ?: "user"
    }

    private fun loadModulesAndProgress() {
        progressBar.visibility = View.VISIBLE

        Log.d("DEBUG", "Memulai loadModulesAndProgress untuk course: $courseId")
        Toast.makeText(this, "DEBUG: loadModulesAndProgress TERPANGGIL", Toast.LENGTH_SHORT).show()
        Log.d("DEBUG", "=== MEMULAI QUERY MODULES UNTUK COURSE: $courseId ===")
        db.collection("courses").document(courseId).collection("modules")
            .orderBy("order")
            .get()
            .addOnSuccessListener { moduleSnapshot ->
                Log.d("DEBUG", "QUERY BERHASIL")
                Log.d("DEBUG", "JUMLAH MODULE: ${moduleSnapshot.size()}")
                for (doc in moduleSnapshot) {
                    Log.d("DEBUG", "MODULE DATA: ${doc.data}")
                }
                progressBar.visibility = View.GONE

                val modules = moduleSnapshot.mapNotNull { doc ->
                    doc.toObject(Module::class.java).apply { id = doc.id }
                }
                Log.d("DEBUG", "progressRef ID = ${userId}_$courseId")
                Log.d("DEBUG", "PROGRESS REF AKAN DIAMBIL SEKARANG")
                val progressRef = db.collection("progress").document("${userId}_$courseId")
                progressRef.get()
                    .addOnSuccessListener { progressDoc ->
                        Log.d("DEBUG", "MASUK onSuccess progress SNAPSHOT, ADA = ${progressDoc.exists()}")
                        val completed = progressDoc.get("completedModuleIds") as? List<String> ?: emptyList()

                        val adapter = ModuleAdapter(
                            modules,
                            completed,
                            userRole,
                            onChecked = { module, checked ->
                                updateProgress(progressRef, module.id, checked)
                            },
                            onDelete = { module ->
                                deleteModule(courseId, module.id)
                            },
                            onItemClick = { module ->
                                val intent = Intent(this, VideoPlayerActivity::class.java)
                                intent.putExtra("title", module.title)
                                intent.putExtra("videoUrl", module.videoUrl)
                                startActivity(intent)
                            }
                        )
                        Toast.makeText(this, "Adapter DISET, jumlah module: " + modules.size, Toast.LENGTH_LONG).show()
                        rvModules.layoutManager = LinearLayoutManager(this)
                        rvModules.adapter = adapter

                        val percent = if (modules.isNotEmpty()) (completed.size * 100) / modules.size else 0
                    }
            }
            .addOnFailureListener { e ->
                Log.e("DEBUG", "PROGRESS GAGAL: ${e.message}")
                Log.e("DEBUG", "QUERY GAGAL: ${e.message}")
                Toast.makeText(this, "ERROR: ${e.message}", Toast.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Gagal memuat modul: ${e.message}", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this, "Progress updated", Toast.LENGTH_SHORT).show()
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
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menghapus modul: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}