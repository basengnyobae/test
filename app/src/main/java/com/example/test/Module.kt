package com.example.test

import com.google.firebase.firestore.Exclude

data class Module(
    @get:Exclude @set:Exclude var id: String = "",
    val title: String = "",
    val duration: Any = "",
    val order: Int = 0,
    val videoUrl: String = ""
)
