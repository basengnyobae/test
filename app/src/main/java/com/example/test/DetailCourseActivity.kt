package com.example.test

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.Timestamp

class DetailCourseActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private var courseId = ""
    private var userId = ""
    private var userRole = "user"
    private var courseTitle = ""
    private var coursePrice: Long = 0
    private var userName: String = "User"

    private lateinit var tvEnrollmentCount: TextView
    private lateinit var enrollButton: Button
    private lateinit var btnViewModules: Button

    private lateinit var btnStartQuiz: Button
    private lateinit var btnAddQuizQuestion: Button

    private lateinit var btnAddModule: Button
    private lateinit var btnEditCourse: Button
    private lateinit var btnDeleteCourse: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_detail_course)

        courseId = intent.getStringExtra("COURSE_ID")?.trim() ?: ""
        userId = auth.currentUser?.uid ?: ""

        if (courseId.isEmpty()) {
            Toast.makeText(this, "Course ID tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val tvTitle = findViewById<TextView>(R.id.tvDetailTitle)
        val tvInstructor = findViewById<TextView>(R.id.tvDetailInstructor)
        val ivThumbnail = findViewById<ImageView>(R.id.ivDetailThumbnail)
        tvEnrollmentCount = findViewById(R.id.tvEnrollmentCount)

        enrollButton = findViewById(R.id.btnEnroll)
        btnViewModules = findViewById(R.id.btnViewModules)

        btnStartQuiz = findViewById(R.id.btnStartQuiz)
        btnAddQuizQuestion = findViewById(R.id.btnAddQuizQuestion)

        btnAddModule = findViewById(R.id.btnAddModule)
        btnEditCourse = findViewById(R.id.btnEditCourse)
        btnDeleteCourse = findViewById(R.id.btnDeleteCourse)

        userRole = readUserRoleFromPreferences()

        if (userId.isNotEmpty()) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { userDoc ->
                    userName = userDoc.getString("name") ?: "User"
                }
        }

        db.collection("courses").document(courseId).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Toast.makeText(this, "Course tidak ditemukan", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                courseTitle = doc.getString("title") ?: ""
                val instructor = doc.getString("instructor") ?: ""
                val thumbnailUrl = doc.getString("thumbnailUrl") ?: ""
                coursePrice = doc.getLong("price") ?: 0

                tvTitle.text = courseTitle
                tvInstructor.text = instructor
                Glide.with(this).load(thumbnailUrl).into(ivThumbnail)

                if (userRole == "admin") {
                    handleAdminView()
                    loadEnrollmentCount(courseId)
                } else {
                    if (userId.isNotEmpty()) {
                        checkEnrollmentStatus()
                    } else {
                        handleGuestView()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat data kursus", Toast.LENGTH_SHORT).show()
                finish()
            }

        enrollButton.setOnClickListener {
            if (userId.isEmpty()) {
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                initiatePayment()
            }
        }

        btnViewModules.setOnClickListener {
            openModuleList()
        }

        btnStartQuiz.setOnClickListener {
            val intent = Intent(this, QuizActivity::class.java)
            intent.putExtra("COURSE_ID", courseId)
            intent.putExtra("COURSE_TITLE", courseTitle)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        if (userId.isNotEmpty() && userRole != "admin") {
            checkEnrollmentStatus()
        }
    }

    private fun handleAdminView() {
        btnAddModule.visibility = View.VISIBLE
        btnEditCourse.visibility = View.VISIBLE
        btnDeleteCourse.visibility = View.VISIBLE
        tvEnrollmentCount.visibility = View.VISIBLE

        btnAddQuizQuestion.visibility = View.VISIBLE

        enrollButton.visibility = View.GONE
        btnViewModules.visibility = View.GONE
        btnStartQuiz.visibility = View.GONE

        btnAddModule.setOnClickListener {
            val intent = Intent(this, AddModuleActivity::class.java)
            intent.putExtra("courseId", courseId)
            startActivity(intent)
        }

        btnAddQuizQuestion.setOnClickListener {
            val intent = Intent(this, AddQuestionActivity::class.java)
            intent.putExtra("COURSE_ID", courseId)
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

    private fun handleGuestView() {
        enrollButton.text = "Login untuk Membeli (Rp $coursePrice)"
        enrollButton.isEnabled = true
        btnViewModules.visibility = View.GONE
        btnStartQuiz.visibility = View.GONE
    }

    private fun checkEnrollmentStatus() {
        if (userId.isEmpty()) return

        db.collection("users").document(userId).get()
            .addOnSuccessListener { userDoc ->
                val enrolledCourses = userDoc.get("enrolledCourses") as? List<String> ?: emptyList()

                if (enrolledCourses.contains(courseId)) {
                    enrollButton.visibility = View.GONE
                    btnViewModules.visibility = View.VISIBLE
                    btnStartQuiz.visibility = View.VISIBLE
                } else {
                    enrollButton.text = "Beli Course Ini (Rp $coursePrice)"
                    enrollButton.visibility = View.VISIBLE
                    btnViewModules.visibility = View.GONE
                    btnStartQuiz.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                enrollButton.text = "Beli Course Ini (Rp $coursePrice)"
                enrollButton.visibility = View.VISIBLE
            }
    }

    private fun readUserRoleFromPreferences(): String {
        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return sharedPref.getString("USER_ROLE", "user") ?: "user"
    }

    private fun loadEnrollmentCount(courseId: String) {
        db.collection("transactions")
            .whereEqualTo("courseId", courseId)
            .whereEqualTo("status", "success")
            .get()
            .addOnSuccessListener { snapshot ->
                tvEnrollmentCount.text = "Total Pendaftar: ${snapshot.size()} orang"
            }
    }

    private fun openModuleList() {
        val intent = Intent(this, ModuleListActivity::class.java).apply {
            putExtra("COURSE_ID", courseId)
            putExtra("COURSE_TITLE", courseTitle)
        }
        startActivity(intent)
    }

    private fun initiatePayment() {
        if (coursePrice == 0L) {
            Toast.makeText(this, "Course ini gratis atau harga tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        enrollButton.isEnabled = false
        enrollButton.text = "Memproses..."

        val newTransaction = Transaction(
            userId = userId,
            courseId = courseId,
            courseName = courseTitle,
            userName = userName,
            amount = coursePrice,
            status = "pending",
            createdAt = Timestamp.now()
        )

        db.collection("transactions")
            .add(newTransaction)
            .addOnSuccessListener { docRef ->
                val intent = Intent(this, PaymentActivity::class.java).apply {
                    putExtra("TRANSACTION_ID", docRef.id)
                    putExtra("AMOUNT", coursePrice)
                }
                startActivity(intent)

                enrollButton.isEnabled = true
                enrollButton.text = "Beli Course Ini (Rp $coursePrice)"
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memulai transaksi: ${it.message}", Toast.LENGTH_SHORT).show()
                enrollButton.isEnabled = true
                enrollButton.text = "Beli Course Ini (Rp $coursePrice)"
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
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Gagal menghapus course: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat modul: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}