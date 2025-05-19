package com.example.classroomquizapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.classroomquizapp.api.RetrofitClient
import com.example.classroomquizapp.model.CreatedQuizResponse
import com.example.classroomquizapp.model.FeedbackItem
import com.example.classroomquizapp.model.GenericResponse
import com.example.classroomquizapp.model.SessionResponse
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TeacherDashboardActivity : AppCompatActivity() {

    private lateinit var btnCreateSession: Button
    private lateinit var etSessionTitle: EditText
    private lateinit var txtName: TextView
    private lateinit var txtSurname: TextView
    private lateinit var txtDepartment: TextView

    private lateinit var teacherId: String
    private lateinit var name: String
    private lateinit var surname: String
    private lateinit var department: String

    private fun loadCreatedQuizzes() {
        val body = mapOf("teacher_id" to teacherId)

        RetrofitClient.instance.getCreatedQuizzes(body).enqueue(object : Callback<List<CreatedQuizResponse>> {
            override fun onResponse(
                call: Call<List<CreatedQuizResponse>>,
                response: Response<List<CreatedQuizResponse>>
            ) {
                if (response.isSuccessful) {
                    val list = response.body()
                    val container = findViewById<LinearLayout>(R.id.createdQuizContainer)
                    container.removeAllViews()

                    list?.forEach { quiz ->
                        val itemLayout = LinearLayout(this@TeacherDashboardActivity).apply {
                            orientation = LinearLayout.HORIZONTAL
                            setPadding(16, 16, 16, 16)
                        }

                        val textView = TextView(this@TeacherDashboardActivity).apply {
                            text = "Q: ${quiz.question} (${quiz.type})"
                            setTextAppearance(android.R.style.TextAppearance_Medium)
                        }

                        val toggle = Switch(this@TeacherDashboardActivity).apply {
                            isChecked = quiz.is_active
                            text = if (isChecked) "Active" else "Passive"
                            setOnCheckedChangeListener { _, isChecked ->
                                val toggleBody = mapOf("quiz_id" to quiz.id)
                                RetrofitClient.instance.toggleQuizActive(toggleBody)
                                    .enqueue(object : Callback<GenericResponse> {
                                        override fun onResponse(
                                            call: Call<GenericResponse>,
                                            response: Response<GenericResponse>
                                        ) {
                                            if (response.isSuccessful) {
                                                this@apply.text = if (isChecked) "Active" else "Passive"
                                                Toast.makeText(this@TeacherDashboardActivity, "Quiz updated", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(this@TeacherDashboardActivity, "Server error", Toast.LENGTH_SHORT).show()
                                            }
                                        }

                                        override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                                            Toast.makeText(this@TeacherDashboardActivity, "Network error", Toast.LENGTH_SHORT).show()
                                        }
                                    })
                            }
                        }

                        itemLayout.addView(textView, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                        itemLayout.addView(toggle)

                        container.addView(itemLayout)
                    }

                } else {
                    Toast.makeText(this@TeacherDashboardActivity, "Failed to load quizzes", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<CreatedQuizResponse>>, t: Throwable) {
                Toast.makeText(this@TeacherDashboardActivity, "Connection error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun loadFeedbacks() {
        val body = mapOf("teacher_id" to teacherId)

        RetrofitClient.instance.getFeedbackByTeacher(body).enqueue(object : Callback<List<FeedbackItem>> {
            override fun onResponse(call: Call<List<FeedbackItem>>, response: Response<List<FeedbackItem>>) {
                if (response.isSuccessful) {
                    val list = response.body()
                    val container = findViewById<LinearLayout>(R.id.feedbackContainer)
                    container.removeAllViews()

                    list?.forEach { item ->
                        val textView = TextView(this@TeacherDashboardActivity)
                        textView.text = "📬 ${item.title}: \"${item.message}\" (${item.submitted_at})"
                        textView.setPadding(8, 8, 8, 8)
                        container.addView(textView)
                    }
                }
            }

            override fun onFailure(call: Call<List<FeedbackItem>>, t: Throwable) {
                Toast.makeText(this@TeacherDashboardActivity, "Failed to load feedbacks", Toast.LENGTH_SHORT).show()
            }
        })
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_dashboard)

        teacherId = intent.getStringExtra("teacher_id") ?: ""
        name = intent.getStringExtra("name") ?: ""
        surname = intent.getStringExtra("surname") ?: ""
        department = intent.getStringExtra("department") ?: ""

        txtName = findViewById(R.id.txtName)
        txtSurname = findViewById(R.id.txtSurname)
        txtDepartment = findViewById(R.id.txtDepartment)
        etSessionTitle = findViewById(R.id.etSessionTitle)
        btnCreateSession = findViewById(R.id.btnCreateSession)

        txtName.text = name
        txtSurname.text = surname
        txtDepartment.text = department

        Toast.makeText(this, "Welcome $name 👨‍🏫", Toast.LENGTH_SHORT).show()

        btnCreateSession.setOnClickListener {
            val title = etSessionTitle.text.toString().trim()
            if (title.isEmpty()) {
                Toast.makeText(this, "Enter session title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val body = mapOf("title" to title, "teacher_id" to teacherId)

            RetrofitClient.instance.createSession(body).enqueue(object : Callback<SessionResponse> {
                override fun onResponse(call: Call<SessionResponse>, response: Response<SessionResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val code = response.body()!!.code
                        Toast.makeText(this@TeacherDashboardActivity, "🎉 Session Code: $code", Toast.LENGTH_LONG).show()

                        val intent = Intent(this@TeacherDashboardActivity, CreateQuizActivity::class.java)
                        intent.putExtra("teacher_id", teacherId)
                        intent.putExtra("session_id", response.body()!!.session_id)
                        intent.putExtra("name", name)
                        intent.putExtra("surname", surname)
                        intent.putExtra("department", department)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@TeacherDashboardActivity, "Server error", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<SessionResponse>, t: Throwable) {
                    Toast.makeText(this@TeacherDashboardActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> true
                R.id.nav_chats -> {
                    Toast.makeText(this, "Chats clicked", Toast.LENGTH_SHORT).show(); true
                }
                R.id.nav_departments -> {
                    Toast.makeText(this, "Departments clicked", Toast.LENGTH_SHORT).show(); true
                }
                R.id.nav_leaderboard -> {
                    Toast.makeText(this, "Leaderboard clicked", Toast.LENGTH_SHORT).show(); true
                }
                R.id.nav_settings -> {
                    Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show(); true
                }
                else -> false
            }
        }
        loadCreatedQuizzes()
        loadFeedbacks()
    }
}
