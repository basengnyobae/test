package com.example.test

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var welcomeText: TextView
    private val courses = listOf(
        Course("Kotlin untuk Pemula", "Budi Santoso", "https://your-image-link.com/kotlin.jpg"),
        Course("Android Jetpack Compose", "Dina Rahma", "https://your-image-link.com/compose.jpg")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.rvCourses)
        welcomeText = findViewById(R.id.tvWelcome)

        val user = Firebase.auth.currentUser
        welcomeText.text = "Selamat datang, ${user?.email}"

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = CourseAdapter(courses)

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            Firebase.auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
