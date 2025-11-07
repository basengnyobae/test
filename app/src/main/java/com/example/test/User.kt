package com.example.test

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "user",
    val enrolledCourses: List<String> = emptyList(),
    val photoUrl: String = ""
)