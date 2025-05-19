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

    private lateinit var studentId: String
    private var joinedSessionId: Int = -1

    private fun loadAllActiveSessionsWithQuizzes() {
        RetrofitClient.instance.getAllActiveQuizzes().enqueue(object : Callback<List<ActiveQuizResponse>> {
            override fun onResponse(call: Call<List<ActiveQuizResponse>>, response: Response<List<ActiveQuizResponse>>) {
                if (response.isSuccessful && response.body() != null) {
                    val quizList = response.body()!!
                    val grouped = quizList.groupBy { it.session_id }

                    val container = findViewById<LinearLayout>(R.id.activeQuizContainer)
                    container.removeAllViews()

                    for ((sessionId, quizzes) in grouped) {
                        val btn = Button(this@StudentDashboardActivity)
                        btn.text = "📘 Session $sessionId → ${quizzes.size} quizzes"
                        btn.setOnClickListener {
                            for (quiz in quizzes) {
                                val intent = Intent(this@StudentDashboardActivity, AnswerQuizActivity::class.java)
                                intent.putExtra("quiz_id", quiz.id)
                                intent.putExtra("question", quiz.question)
                                intent.putStringArrayListExtra("options", ArrayList(quiz.options))
                                intent.putExtra("student_id", studentId)
                                intent.putExtra("session_id", quiz.session_id)
                                startActivity(intent)
                            }
                        }
                        container.addView(btn)
                    }
                }
            }

            override fun onFailure(call: Call<List<ActiveQuizResponse>>, t: Throwable) {
                Toast.makeText(this@StudentDashboardActivity, "Aktif oturumlar yüklenemedi", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_dashboard)

        studentId = intent.getStringExtra("student_id") ?: ""
        val name = intent.getStringExtra("name") ?: ""
        val surname = intent.getStringExtra("surname") ?: ""
        val department = intent.getStringExtra("department") ?: ""
        val points = intent.getIntExtra("points", 0)

        txtName = findViewById(R.id.txtName)
        txtSurname = findViewById(R.id.txtSurname)
        txtDepartment = findViewById(R.id.txtDepartment)
        txtPoints = findViewById(R.id.txtPoints)
        btnJoinSession = findViewById(R.id.btnJoinSession)

        txtName.text = name
        txtSurname.text = surname
        txtDepartment.text = department
        txtPoints.text = "⭐ $points"

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
                                val sessionId = response.body()!!.session_id
                                joinedSessionId = sessionId
                                Toast.makeText(this@StudentDashboardActivity, "Joined Session $sessionId", Toast.LENGTH_SHORT).show()
                                loadAllActiveSessionsWithQuizzes()
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

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> true
                R.id.nav_chats -> {
                    Toast.makeText(this, "Chats clicked", Toast.LENGTH_SHORT).show(); true
                }
                R.id.nav_departments -> {
                    Toast.makeText(this, "Departments clicked", Toast.LENGTH_SHORT).show(); true
                }
                R.id.nav_leaderboard -> {
                    val intent = Intent(this, LeaderboardActivity::class.java)
                    intent.putExtra("student_id", studentId)
                    startActivity(intent)
                    true
                }
                R.id.nav_settings -> {
                    Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show(); true
                }
                else -> false
            }
        }

        loadAllActiveSessionsWithQuizzes()
    }
}
