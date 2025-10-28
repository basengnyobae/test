package com.example.test

import com.google.firebase.Timestamp

data class Transaction(
    var id: String = "",
    val userId: String = "",
    val courseId: String = "",
    val courseName: String = "",
    val userName: String = "",
    val amount: Long = 0,
    val status: String = "pending",
    val createdAt: Timestamp = Timestamp.now()
)