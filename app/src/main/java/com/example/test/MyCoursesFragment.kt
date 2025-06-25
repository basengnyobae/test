package com.example.test

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.FieldPath


class MyCoursesFragment : Fragment(R.layout.fragment_my_courses) {
    private lateinit var adapter: CourseAdapter
    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvMyCourses)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBarMyCourses)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = CourseAdapter(mutableListOf())
        recyclerView.adapter = adapter

        progressBar.visibility = View.VISIBLE

        val userId = auth.currentUser?.uid ?: return

        db.collection("enrollments")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { enrollmentSnapshot ->
                val courseIds = enrollmentSnapshot.documents.mapNotNull { it.getString("courseId") }

                if (courseIds.isEmpty()) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Belum mendaftar kursus apa pun", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                db.collection("courses")
                    .whereIn(FieldPath.documentId(), courseIds)
                    .get()
                    .addOnSuccessListener { courseSnapshot ->
                        val courses = courseSnapshot.map { doc ->
                            Course(
                                id = doc.id,
                                title = doc.getString("title") ?: "",
                                instructor = doc.getString("instructor") ?: "",
                                thumbnailUrl = doc.getString("thumbnailUrl") ?: ""
                            )
                        }
                        adapter.updateList(courses)
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Gagal memuat kursus", Toast.LENGTH_SHORT).show()
                    }
                    .addOnCompleteListener {
                        progressBar.visibility = View.GONE
                    }

            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal mengambil enrollments", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }
    }
}
