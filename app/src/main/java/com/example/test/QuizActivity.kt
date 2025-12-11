package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class QuizActivity : AppCompatActivity() {
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private var courseId = ""
    private var courseTitle = ""

    private val userAnswers = mutableMapOf<Int, Int>()
    private var questionList = listOf<QuizQuestion>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        courseId = intent.getStringExtra("COURSE_ID") ?: ""
        courseTitle = intent.getStringExtra("COURSE_TITLE") ?: ""

        val rvQuiz = findViewById<RecyclerView>(R.id.rvQuiz)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitQuiz)

        rvQuiz.layoutManager = LinearLayoutManager(this)

        // Muat Soal
        db.collection("courses").document(courseId).collection("questions")
            .get()
            .addOnSuccessListener { snapshot ->
                questionList = snapshot.map { it.toObject(QuizQuestion::class.java) }

                if (questionList.isEmpty()) {
                    Toast.makeText(this, "Belum ada soal untuk kuis ini", Toast.LENGTH_SHORT).show()
                    finish()
                }

                val adapter = QuizAdapter(questionList) { index, answer ->
                    userAnswers[index] = answer
                }
                rvQuiz.adapter = adapter
            }

        btnSubmit.setOnClickListener {
            calculateScore()
        }
    }

    private fun calculateScore() {
        var correctCount = 0

        for ((index, question) in questionList.withIndex()) {
            val userAnswer = userAnswers[index]
            if (userAnswer == question.correctAnswerIndex) {
                correctCount++
            }
        }

        val score = (correctCount * 100) / questionList.size

        if (score >= 70) {
            val intent = Intent(this, CertificateActivity::class.java)
            intent.putExtra("COURSE_TITLE", courseTitle)
            intent.putExtra("USER_NAME", auth.currentUser?.displayName ?: "Peserta")
            intent.putExtra("SCORE", score)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Nilai Anda: $score. Belum lulus (KKM 70). Silakan coba lagi.", Toast.LENGTH_LONG).show()
        }
    }
}