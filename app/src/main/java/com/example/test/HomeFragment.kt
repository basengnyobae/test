package com.example.test

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var adapter: CourseAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvCourses)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = CourseAdapter(mutableListOf())
        recyclerView.adapter = adapter

        progressBar.visibility = View.VISIBLE
        fetchCourses {
            progressBar.visibility = View.GONE
        }
    }

    private fun fetchCourses(onComplete: () -> Unit) {
        val db = Firebase.firestore
        db.collection("courses")
            .get()
            .addOnSuccessListener { result ->
                val courses = result.map { doc ->
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
                Toast.makeText(requireContext(), "Gagal memuat kursus: ${it.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                onComplete()
            }
    }
}
