package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.text.Editable
import android.text.TextWatcher

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

        adapter = CourseAdapter(courseList) { course ->
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            } else {
                val intent = Intent(requireContext(), DetailCourseActivity::class.java)
                intent.putExtra("id", course.instructorId)
                intent.putExtra("title", course.title)
                intent.putExtra("instructor", course.instructor)
                intent.putExtra("thumbnailUrl", course.thumbnailUrl)
                startActivity(intent)
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        loadCourses()

        searchField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val keyword = s.toString().trim()
                filterCourses(keyword)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        return view
    }

    private fun loadCourses() {
        FirebaseFirestore.getInstance().collection("courses")
            .get()
            .addOnSuccessListener { result ->
                courseList.clear()
                for (doc in result) {
                    val course = doc.toObject(Course::class.java)
                    course.instructorId = doc.id
                    courseList.add(course)
                }


                Toast.makeText(requireContext(), "Loaded ${courseList.size} courses", Toast.LENGTH_SHORT).show()

                adapter.updateList(courseList)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show()
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
