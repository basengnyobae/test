package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class CourseListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAddCourse: Button
    private val courseList = mutableListOf<Course>()
    private lateinit var adapter: CourseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_courses, container, false)

        btnAddCourse = view.findViewById(R.id.btnAddCourse)
        recyclerView = view.findViewById(R.id.rvAllCourses)

        adapter = CourseAdapter(courseList) { course ->
            val intent = Intent(requireContext(), DetailCourseActivity::class.java)

            intent.putExtra("COURSE_ID", course.id)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        btnAddCourse.setOnClickListener {
            val intent = Intent(requireContext(), AddCourseActivity::class.java)
            startActivity(intent)
        }


        return view
    }

    override fun onResume() {
        super.onResume()
        loadCourses()
    }

    private fun loadCourses() {
        FirebaseFirestore.getInstance().collection("courses")
            .get()
            .addOnSuccessListener { result ->
                courseList.clear()
                for (doc in result) {
                    val course = doc.toObject(Course::class.java)
                    course.id = doc.id
                    courseList.add(course) }
                adapter.notifyDataSetChanged()
                Toast.makeText(requireContext(), "Admin loaded: ${courseList.size} courses", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
    }

}