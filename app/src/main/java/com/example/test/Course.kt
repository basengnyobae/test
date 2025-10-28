package com.example.test
import com.google.firebase.firestore.Exclude

data class Course(
    @get:Exclude @set:Exclude var id: String = "",
    val title: String = "",
    val description: String = "",
    val instructor: String = "",
    val instructorId: String = "",
    val thumbnailUrl: String = "",
    val price: Long = 0
)