package com.example.test

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment(R.layout.fragment_home) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val courses = listOf(
            Course("Kotlin untuk Pemula", "Budi Santoso", "https://your-image-link.com/kotlin.jpg"),
            Course("Android Jetpack Compose", "Dina Rahma", "https://your-image-link.com/compose.jpg")
        ).toMutableList()

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvCourses)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = CourseAdapter(courses)
    }
}
