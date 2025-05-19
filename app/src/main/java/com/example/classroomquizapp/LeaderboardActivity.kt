package com.example.classroomquizapp

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.classroomquizapp.api.RetrofitClient
import com.example.classroomquizapp.model.LeaderboardEntry
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var studentId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        studentId = intent.getStringExtra("student_id") ?: ""

        val container = findViewById<LinearLayout>(R.id.leaderboardContainer)

        RetrofitClient.instance.getLeaderboard().enqueue(object : Callback<List<LeaderboardEntry>> {
            override fun onResponse(
                call: Call<List<LeaderboardEntry>>,
                response: Response<List<LeaderboardEntry>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    container.removeAllViews()
                    response.body()!!.forEachIndexed { index, entry ->
                        val textView = TextView(this@LeaderboardActivity)
                        textView.text = "${index + 1}. ${entry.name} ${entry.surname} - ⭐ ${entry.points}"
                        textView.textSize = 18f
                        textView.setPadding(8, 8, 8, 8)
                        container.addView(textView)
                    }
                }
            }

            override fun onFailure(call: Call<List<LeaderboardEntry>>, t: Throwable) {
                Toast.makeText(this@LeaderboardActivity, "Leaderboard yüklenemedi", Toast.LENGTH_SHORT).show()
            }
        })

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_leaderboard

        bottomNav.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, StudentDashboardActivity::class.java)
                    intent.putExtra("student_id", studentId)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_leaderboard -> true
                else -> false
            }
        }
    }
}
