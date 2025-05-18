package com.example.classroomquizapp.model

data class CreatedQuizResponse(
    val id: Int,
    val question: String,
    val type: String,
    val is_active: Boolean
)

