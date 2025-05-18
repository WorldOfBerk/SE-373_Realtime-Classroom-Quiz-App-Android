package com.example.classroomquizapp.model

data class ActiveQuizResponse(
    val id: Int,
    val question: String,
    val type: String,
    val options: List<String>
)
