package com.example.classroomquizapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.classroomquizapp.api.RetrofitClient
import com.example.classroomquizapp.model.AnswerRequest
import com.example.classroomquizapp.model.AnswerResponse
import com.example.classroomquizapp.model.GenericResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AnswerQuizActivity : AppCompatActivity() {

    private lateinit var txtQuestion: TextView
    private lateinit var radioGroup: RadioGroup
    private lateinit var btnSubmit: Button

    private var quizId: Int = -1
    private var selectedOptionIndex: Int = -1
    private lateinit var studentId: String
    private var sessionId: Int = -1  // class dışına

    private fun showFeedbackDialog(sessionId: Int) {
        val etFeedback = EditText(this)
        etFeedback.hint = "Write your feedback..."

        AlertDialog.Builder(this)
            .setTitle("Submit Anonymous Feedback")
            .setView(etFeedback)
            .setPositiveButton("Submit") { _, _ ->
                val message = etFeedback.text.toString().trim()
                if (message.isNotEmpty()) {
                    val body = mapOf(
                        "session_id" to sessionId.toString(),
                        "message" to message
                    )

                    RetrofitClient.instance.submitFeedback(body).enqueue(object : Callback<GenericResponse> {
                        override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                            Toast.makeText(this@AnswerQuizActivity, "✅ Feedback submitted", Toast.LENGTH_SHORT).show()
                            finish()
                        }

                        override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                            Toast.makeText(this@AnswerQuizActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_answer_quiz)
        sessionId = intent.getIntExtra("session_id", -1)

        txtQuestion = findViewById(R.id.txtQuestion)
        radioGroup = findViewById(R.id.radioOptions)
        btnSubmit = findViewById(R.id.btnSubmitAnswer)

        quizId = intent.getIntExtra("quiz_id", -1)
        val question = intent.getStringExtra("question") ?: ""
        val options = intent.getStringArrayListExtra("options") ?: listOf()
        studentId = intent.getStringExtra("student_id") ?: ""

        txtQuestion.text = question

        options.forEachIndexed { index, option ->
            val radioButton = RadioButton(this)
            radioButton.text = option
            radioButton.id = index
            radioGroup.addView(radioButton)
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedOptionIndex = checkedId
        }

        btnSubmit.setOnClickListener {
            if (selectedOptionIndex == -1) {
                Toast.makeText(this, "Lütfen bir seçenek seçin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val body = AnswerRequest(
                quiz_id = quizId,
                student_id = studentId,
                selected_option = selectedOptionIndex
            )

            RetrofitClient.instance.submitAnswer(body).enqueue(object : Callback<AnswerResponse> {
                override fun onResponse(call: Call<AnswerResponse>, response: Response<AnswerResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val isCorrect = response.body()!!.is_correct
                        Toast.makeText(
                            this@AnswerQuizActivity,
                            if (isCorrect) "✅ Correct! +5 points" else "❌ Wrong answer",
                            Toast.LENGTH_SHORT
                        ).show()
                        showFeedbackDialog(sessionId)

                    } else if (response.code() == 409) {
                        Toast.makeText(
                            this@AnswerQuizActivity,
                            "⚠️ You already answered this quiz",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()

                    } else {
                        Toast.makeText(
                            this@AnswerQuizActivity,
                            "Server error: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<AnswerResponse>, t: Throwable) {
                    Toast.makeText(this@AnswerQuizActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })


        }
    }
}
