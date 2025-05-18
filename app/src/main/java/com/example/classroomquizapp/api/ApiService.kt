package com.example.classroomquizapp.api

import com.example.classroomquizapp.model.ActiveQuizResponse
import com.example.classroomquizapp.model.AnswerRequest
import com.example.classroomquizapp.model.AnswerResponse
import com.example.classroomquizapp.model.CreatedQuizResponse
import com.example.classroomquizapp.model.GenericResponse
import com.example.classroomquizapp.model.LoginRequest
import com.example.classroomquizapp.model.LoginResponse
import com.example.classroomquizapp.model.SessionResponse
import com.example.classroomquizapp.model.JoinResponse
import com.example.classroomquizapp.model.QuizCreateRequest
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("api/session/create")
    fun createSession(@Body body: Map<String, String>): Call<SessionResponse>

    @POST("api/session/join")
    fun joinSession(@Body body: Map<String, String>): Call<JoinResponse>

    @POST("api/quiz/create")
    fun createQuiz(@Body body: QuizCreateRequest): Call<GenericResponse>

    @POST("api/quiz/active")
    fun getActiveQuiz(@Body body: Map<String, Int>): Call<ActiveQuizResponse>

    @POST("api/quiz/answer")
    fun submitAnswer(@Body body: AnswerRequest): Call<AnswerResponse>

    @POST("/api/quiz/multi")
    fun submitMultipleQuestions(@Body body: List<QuizCreateRequest>): Call<GenericResponse>

    @POST("api/quiz/created")
    fun getCreatedQuizzes(@Body body: Map<String, String>): Call<List<CreatedQuizResponse>>

    @POST("api/quiz/toggle")
    fun toggleQuizActive(@Body body: Map<String, Int>): Call<GenericResponse>
}



