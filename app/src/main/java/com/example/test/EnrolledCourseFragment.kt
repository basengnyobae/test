package com.example.test

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EnrolledCourseFragment : Fragment(R.layout.fragment_enrolled_courses) {
    private lateinit var adapter: CourseAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvEnrolledCourses)
        adapter = CourseAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = Firebase.firestore

        db.collection("enrollments")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val courseIds = snapshot.mapNotNull { it.getString("courseId") }
                if (courseIds.isEmpty()) return@addOnSuccessListener

                db.collection("courses")
                    .whereIn(FieldPath.documentId(), courseIds)
                    .get()
                    .addOnSuccessListener { coursesSnap ->
                        val courses = coursesSnap.map { doc ->
                            Course(
                                instructorId = doc.id,
                                title = doc.getString("title") ?: "",
                                instructor = doc.getString("instructor") ?: "",
                                thumbnailUrl = doc.getString("thumbnailUrl") ?: ""
                            )
                        }
                        adapter.updateList(courses)
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal memuat course yang di-enroll", Toast.LENGTH_SHORT).show()
            }
    }
}
