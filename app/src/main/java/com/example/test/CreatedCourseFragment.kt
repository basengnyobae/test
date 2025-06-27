package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CreatedCourseFragment : Fragment(R.layout.fragment_created_courses) {
    private lateinit var adapter: CourseAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val btnAddCourse = view.findViewById<Button>(R.id.btnAddCourse)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvCreatedCourses)
        adapter = CourseAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        btnAddCourse.setOnClickListener {
            val intent = Intent(requireContext(), AddCourseActivity::class.java)
            startActivity(intent)
        }

        Firebase.firestore.collection("courses")
            .whereEqualTo("instructorId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val courses = snapshot.map { doc ->
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
                Toast.makeText(requireContext(), "Gagal memuat course buatan", Toast.LENGTH_SHORT).show()
            }
    }

}
