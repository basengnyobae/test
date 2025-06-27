package com.example.test.com.example.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.Course
import com.example.test.CourseAdapter
import com.example.test.R
import com.google.firebase.firestore.FirebaseFirestore

class CourseListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchField: EditText
    private val courseList = mutableListOf<Course>()
    private lateinit var adapter: CourseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_course_list, container, false)

        searchField = view.findViewById(R.id.etSearch)
        recyclerView = view.findViewById(R.id.rvCourseList)

        adapter = CourseAdapter(courseList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        loadCourses()

        // Filter saat mengetik
        searchField.setOnEditorActionListener { _, _, _ ->
            val keyword = searchField.text.toString().trim()
            filterCourses(keyword)
            true
        }

        return view
    }

    private fun loadCourses() {
        FirebaseFirestore.getInstance().collection("courses")
            .get()
            .addOnSuccessListener { result ->
                courseList.clear()
                for (doc in result) {
                    val course = doc.toObject(Course::class.java)
                    courseList.add(course)
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun filterCourses(keyword: String) {
        val filtered = courseList.filter {
            it.title.contains(keyword, ignoreCase = true) ||
                    it.instructor.contains(keyword, ignoreCase = true)
        }
        adapter.updateList(filtered)
    }
}
