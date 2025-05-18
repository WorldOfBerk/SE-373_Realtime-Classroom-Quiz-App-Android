package com.example.classroomquizapp.model

data class LoginResponse(
    val message: String,
    val id: String,
    val role: String,
    val name: String?,
    val surname: String?,
    val department: String?,
    val faculty: String?
)