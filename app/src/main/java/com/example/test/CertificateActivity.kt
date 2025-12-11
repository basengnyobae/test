package com.example.test

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class CertificateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_certificate)

        val name = intent.getStringExtra("USER_NAME")
        val course = intent.getStringExtra("COURSE_TITLE")
        val score = intent.getIntExtra("SCORE", 0)

        findViewById<TextView>(R.id.tvCertName).text = name
        findViewById<TextView>(R.id.tvCertCourse).text = course
        findViewById<TextView>(R.id.tvCertScore).text = "Nilai Akhir: $score"
    }
}