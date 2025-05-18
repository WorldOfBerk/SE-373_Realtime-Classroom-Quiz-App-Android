package com.example.classroomquizapp

import android.os.Bundle
import android.widget.*
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
    private lateinit var studentId: String  // Giriş yapan öğrenci ID'si

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_answer_quiz)

        txtQuestion = findViewById(R.id.txtQuestion)
        radioGroup = findViewById(R.id.radioOptions)
        btnSubmit = findViewById(R.id.btnSubmitAnswer)

        // Intent ile gelen verileri al
        quizId = intent.getIntExtra("quiz_id", -1)
        val question = intent.getStringExtra("question") ?: ""
        val options = intent.getStringArrayListExtra("options") ?: listOf()
        studentId = intent.getStringExtra("student_id") ?: ""

        txtQuestion.text = question

        // Seçenekleri dinamik olarak ekle
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

            // Cevap gönderme isteği oluştur
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
                            if (isCorrect) "✅ Doğru cevap!" else "❌ Yanlış cevap!",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(this@AnswerQuizActivity, "Gönderim başarısız", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AnswerResponse>, t: Throwable) {
                    Toast.makeText(this@AnswerQuizActivity, "Hata: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })

        }
    }
}
