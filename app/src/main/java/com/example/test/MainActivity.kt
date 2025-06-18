package com.example.test

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var welcomeText: TextView

    private val courses = listOf(
        Course("Kotlin untuk Pemula", "Budi Santoso", "https://your-image-link.com/kotlin.jpg"),
        Course("Android Jetpack Compose", "Dina Rahma", "https://your-image-link.com/compose.jpg")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.rvCourses)
        welcomeText = findViewById(R.id.tvWelcome)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = CourseAdapter(courses)

        // BottomNavigationView handler
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.home // tandai Home aktif

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    // Sudah di MainActivity, tidak perlu pindah
                    true
                }
                R.id.akun -> {
                    val intent = Intent(this, AccountActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}
