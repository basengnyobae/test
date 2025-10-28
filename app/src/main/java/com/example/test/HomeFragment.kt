package com.example.test

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var adapter: CourseAdapter
    private lateinit var searchField: EditText
    private val masterCourseList = mutableListOf<Course>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvCourses)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        searchField = view.findViewById(R.id.etSearch)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = CourseAdapter(mutableListOf()) { course ->
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            } else {
                val intent = Intent(requireContext(), DetailCourseActivity::class.java)

                intent.putExtra("COURSE_ID", course.id)
                intent.putExtra("title", course.title)
                intent.putExtra("instructor", course.instructor)
                intent.putExtra("thumbnailUrl", course.thumbnailUrl)
                startActivity(intent)
            }
        }

        recyclerView.adapter = adapter

        searchField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterCourses(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

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
                masterCourseList.clear()
                for (doc in result) {
                    val course = doc.toObject(Course::class.java)

                    course.id = doc.id

                    masterCourseList.add(course)
                }
                adapter.updateList(masterCourseList)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal memuat kursus: ${it.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                onComplete()
            }
    }

    private fun filterCourses(keyword: String) {
        val filteredList = if (keyword.isEmpty()) {
            masterCourseList
        } else {
            masterCourseList.filter {
                it.title.contains(keyword, ignoreCase = true) ||
                        it.instructor.contains(keyword, ignoreCase = true)
            }
        }
        adapter.updateList(filteredList)
    }
}