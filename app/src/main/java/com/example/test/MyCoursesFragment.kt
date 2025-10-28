package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MyCoursesFragment : Fragment(R.layout.fragment_my_courses) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: CourseAdapter
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private val courseList = mutableListOf<Course>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rvMyCourses)
        tvEmpty = view.findViewById(R.id.tvEmpty)

        val user = auth.currentUser
        if (user == null) {
            tvEmpty.text = "Silakan login untuk melihat course Anda."
            tvEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            return
        }

        adapter = CourseAdapter(courseList) { course ->
            val intent = Intent(requireContext(), DetailCourseActivity::class.java)
            intent.putExtra("COURSE_ID", course.id)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

    }

    override fun onResume() {
        super.onResume()
        val user = auth.currentUser
        if (user != null) {
            loadEnrolledCourses(user.uid)
        } else {
            tvEmpty.text = "Silakan login untuk melihat course Anda."
            tvEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            adapter.updateList(emptyList())
        }
    }

    private fun loadEnrolledCourses(userId: String) {
        tvEmpty.visibility = View.GONE

        db.collection("users").document(userId).get()
            .addOnSuccessListener { userDoc ->

                println("MYCOURSES: Loading enrolled courses for user $userId")
                val enrolledCourseIds = userDoc.get("enrolledCourses") as? List<String>
                println("MYCOURSES: Fetched enrolled IDs: $enrolledCourseIds")

                if (enrolledCourseIds == null || enrolledCourseIds.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    adapter.updateList(emptyList())
                    return@addOnSuccessListener
                }

                db.collection("courses")
                    .whereIn(FieldPath.documentId(), enrolledCourseIds)
                    .get()
                    .addOnSuccessListener { courseSnapshots ->
                        courseList.clear()
                        val freshCourseList = mutableListOf<Course>()
                        println("MYCOURSES: Documents returned from Firestore: ${courseSnapshots.size()}")
                        for (doc in courseSnapshots) {
                            val course = doc.toObject(Course::class.java).apply { id = doc.id }
                            println("MYCOURSES: Successfully mapped course: ${course.title} (Price: ${course.price})")
                            freshCourseList.add(course)
                        }
                        adapter.updateList(freshCourseList)
                        println("MYCOURSES: Adapter Updated with ${freshCourseList.size} Courses!")

                        if (freshCourseList.isEmpty()) {
                            tvEmpty.visibility = View.VISIBLE
                        }
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal memuat data user.", Toast.LENGTH_SHORT).show()
            }
    }
}