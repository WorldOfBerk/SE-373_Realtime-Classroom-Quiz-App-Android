package com.example.classroomquizapp.model

data class QuizCreateRequest(
    val session_id: Int,
    val teacher_id: String,
    val question: String,
    val type: String,
    val options: List<String>,
    val correct_option: Int? = null
)
