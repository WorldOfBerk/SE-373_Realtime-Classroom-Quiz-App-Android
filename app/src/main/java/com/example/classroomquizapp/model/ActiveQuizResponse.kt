package com.example.classroomquizapp.model

data class ActiveQuizResponse(
    val id: Int,
    val session_id: Int,
    val question: String,
    val type: String,
    val options: List<String>
)
