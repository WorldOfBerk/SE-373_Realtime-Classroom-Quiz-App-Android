package com.example.classroomquizapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.classroomquizapp.api.RetrofitClient
import com.example.classroomquizapp.model.LoginRequest
import com.example.classroomquizapp.model.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var etId: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etId = findViewById(R.id.etId)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val id = etId.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (id.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val loginRequest = LoginRequest(id, password)
            RetrofitClient.instance.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val user = response.body()!!
                        Toast.makeText(this@LoginActivity, "Welcome ${user.name ?: user.id}", Toast.LENGTH_SHORT).show()

                        when (user.role) {
                            "student" -> {
                                val intent = Intent(this@LoginActivity, StudentDashboardActivity::class.java)
                                intent.putExtra("student_id", user.id)
                                intent.putExtra("name", user.name ?: "")
                                intent.putExtra("surname", user.surname ?: "")
                                startActivity(intent)
                            }
                            "teacher" -> {
                                val intent = Intent(this@LoginActivity, TeacherDashboardActivity::class.java)
                                intent.putExtra("teacher_id", user.id)
                                intent.putExtra("name", user.name ?: "")
                                intent.putExtra("surname", user.surname ?: "")
                                startActivity(intent)
                            }
                            "admin" -> startActivity(Intent(this@LoginActivity, AdminDashboardActivity::class.java))
                        }


                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Login failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
