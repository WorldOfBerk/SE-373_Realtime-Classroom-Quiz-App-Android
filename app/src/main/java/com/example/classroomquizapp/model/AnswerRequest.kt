package com.example.classroomquizapp.model

data class AnswerRequest(
    val quiz_id: Int,
    val student_id: String,
    val selected_option: Int
)