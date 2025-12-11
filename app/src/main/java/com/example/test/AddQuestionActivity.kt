package com.example.test

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AddQuestionActivity : AppCompatActivity() {
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_question)

        val courseId = intent.getStringExtra("COURSE_ID") ?: return

        val etQuestion = findViewById<EditText>(R.id.etQuestion)
        val etA = findViewById<EditText>(R.id.etOptionA)
        val etB = findViewById<EditText>(R.id.etOptionB)
        val etC = findViewById<EditText>(R.id.etOptionC)
        val etD = findViewById<EditText>(R.id.etOptionD)
        val rgCorrect = findViewById<RadioGroup>(R.id.rgCorrectAnswer)
        val btnSave = findViewById<Button>(R.id.btnSaveQuestion)

        btnSave.setOnClickListener {
            val question = etQuestion.text.toString()
            val opA = etA.text.toString()
            val opB = etB.text.toString()
            val opC = etC.text.toString()
            val opD = etD.text.toString()

            val correctIndex = when (rgCorrect.checkedRadioButtonId) {
                R.id.rbA -> 0
                R.id.rbB -> 1
                R.id.rbC -> 2
                R.id.rbD -> 3
                else -> -1
            }

            if (question.isNotEmpty() && opA.isNotEmpty() && correctIndex != -1) {
                val newQuestion = hashMapOf(
                    "question" to question,
                    "optionA" to opA,
                    "optionB" to opB,
                    "optionC" to opC,
                    "optionD" to opD,
                    "correctAnswerIndex" to correctIndex
                )

                db.collection("courses").document(courseId)
                    .collection("questions")
                    .add(newQuestion)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Soal berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Mohon lengkapi semua data", Toast.LENGTH_SHORT).show()
            }
        }
    }
}