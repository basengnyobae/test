package com.example.test

data class QuizQuestion(
    var id: String = "",
    val question: String = "",
    val optionA: String = "",
    val optionB: String = "",
    val optionC: String = "",
    val optionD: String = "",
    val correctAnswerIndex: Int = 0
)