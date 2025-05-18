package com.example.classroomquizapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.classroomquizapp.api.RetrofitClient
import com.example.classroomquizapp.model.ActiveQuizResponse
import com.example.classroomquizapp.model.JoinResponse
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StudentDashboardActivity : AppCompatActivity() {

    private lateinit var txtName: TextView
    private lateinit var txtSurname: TextView
    private lateinit var txtDepartment: TextView
    private lateinit var txtPoints: TextView
    private lateinit var btnJoinSession: Button
    private lateinit var btnCheckQuiz: Button

    private lateinit var studentId: String
    private var joinedSessionId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_dashboard)

        // Intent'ten gelen kullanıcı bilgileri
        studentId = intent.getStringExtra("student_id") ?: ""
        val name = intent.getStringExtra("name") ?: ""
        val surname = intent.getStringExtra("surname") ?: ""
        val department = intent.getStringExtra("department") ?: ""
        val points = intent.getIntExtra("points", 0)

        // UI Referansları
        txtName = findViewById(R.id.txtName)
        txtSurname = findViewById(R.id.txtSurname)
        txtDepartment = findViewById(R.id.txtDepartment)
        txtPoints = findViewById(R.id.txtPoints)
        btnJoinSession = findViewById(R.id.btnJoinSession)

        // Bilgileri göster
        txtName.text = name
        txtSurname.text = surname
        txtDepartment.text = department
        txtPoints.text = "⭐ $points"

        // Oturuma Katıl Butonu
        btnJoinSession.setOnClickListener {
            val etSessionCode = EditText(this)
            etSessionCode.hint = "Enter session code"

            AlertDialog.Builder(this)
                .setTitle("Join Session")
                .setView(etSessionCode)
                .setPositiveButton("Join") { _, _ ->
                    val code = etSessionCode.text.toString().trim().uppercase()

                    if (code.isEmpty()) {
                        Toast.makeText(this, "Session code is empty", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    val joinBody = mapOf("code" to code, "student_id" to studentId)

                    RetrofitClient.instance.joinSession(joinBody).enqueue(object : Callback<JoinResponse> {
                        override fun onResponse(call: Call<JoinResponse>, response: Response<JoinResponse>) {
                            if (response.isSuccessful && response.body() != null) {
                                val msg = response.body()!!.message
                                val sessionId = response.body()!!.session_id
                                Toast.makeText(this@StudentDashboardActivity, "$msg → Session ID: $sessionId", Toast.LENGTH_SHORT).show()

                                // ✅ Ardından aktif quiz kontrolü:
                                val quizBody = mapOf("session_id" to sessionId)
                                RetrofitClient.instance.getActiveQuiz(quizBody)
                                    .enqueue(object : Callback<ActiveQuizResponse> {
                                        override fun onResponse(call: Call<ActiveQuizResponse>, response: Response<ActiveQuizResponse>) {
                                            if (response.isSuccessful && response.body() != null) {
                                                val quiz = response.body()!!
                                                val intent = Intent(this@StudentDashboardActivity, AnswerQuizActivity::class.java)
                                                intent.putExtra("quiz_id", quiz.id)
                                                intent.putExtra("question", quiz.question)
                                                intent.putStringArrayListExtra("options", ArrayList(quiz.options))
                                                intent.putExtra("student_id", studentId)
                                                startActivity(intent)
                                            } else if (response.code() == 204) {
                                                Toast.makeText(this@StudentDashboardActivity, "Oturum aktif ama quiz yok", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(this@StudentDashboardActivity, "Sunucu hatası (quiz)", Toast.LENGTH_SHORT).show()
                                            }
                                        }

                                        override fun onFailure(call: Call<ActiveQuizResponse>, t: Throwable) {
                                            Toast.makeText(this@StudentDashboardActivity, "Quiz hatası: ${t.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    })

                            } else {
                                Toast.makeText(this@StudentDashboardActivity, "Join Failed", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<JoinResponse>, t: Throwable) {
                            Toast.makeText(this@StudentDashboardActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
                .setNegativeButton("Cancel", null)
                .show()
        }


        // Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_chats -> {
                    Toast.makeText(this, "Chats clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_departments -> {
                    Toast.makeText(this, "Departments clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_leaderboard -> {
                    Toast.makeText(this, "Leaderboard clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_settings -> {
                    Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }
}
