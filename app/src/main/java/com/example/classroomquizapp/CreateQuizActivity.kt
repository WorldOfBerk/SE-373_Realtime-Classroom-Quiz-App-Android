package com.example.classroomquizapp

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.classroomquizapp.api.RetrofitClient
import com.example.classroomquizapp.model.GenericResponse
import com.example.classroomquizapp.model.QuizCreateRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateQuizActivity : AppCompatActivity() {

    private lateinit var questionContainer: LinearLayout
    private lateinit var btnAddQuestion: Button
    private lateinit var btnSubmitAll: Button
    private lateinit var radioQuizType: RadioGroup
    private var dynamicQuestionCount = 0
    private lateinit var teacherId: String
    private var sessionId: Int = -1
    private var selectedType: String = "quiz"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_quiz)

        questionContainer = findViewById(R.id.questionContainer)
        btnAddQuestion = findViewById(R.id.btnAddQuestion)
        btnSubmitAll = findViewById(R.id.btnSubmitAll)
        radioQuizType = findViewById(R.id.radioTypeGroup)

        teacherId = intent.getStringExtra("teacher_id") ?: ""
        sessionId = intent.getIntExtra("session_id", -1)

        radioQuizType.setOnCheckedChangeListener { _, checkedId ->
            selectedType = when (checkedId) {
                R.id.radioPoll -> "poll"
                else -> "quiz"
            }
        }

        btnAddQuestion.setOnClickListener {
            addDynamicQuestionCard()
        }

        btnSubmitAll.setOnClickListener {
            submitAllQuestions()
        }
    }

    private fun addDynamicQuestionCard() {
        dynamicQuestionCount++
        val inflater = LayoutInflater.from(this)
        val card = inflater.inflate(R.layout.item_question_card, null)
        card.findViewById<TextView>(R.id.txtCardTitle)?.text = "Question ${dynamicQuestionCount + 1}"
        questionContainer.addView(card)
    }

    private fun submitAllQuestions() {
        val quizList = mutableListOf<QuizCreateRequest>()

        val allCards = (0 until questionContainer.childCount)
            .map { questionContainer.getChildAt(it) }
            .filter { it is LinearLayout || it is androidx.cardview.widget.CardView }

        for ((index, cardView) in allCards.withIndex()) {
            try {
                val root = cardView.findViewById<LinearLayout>(R.id.questionRoot) ?: continue
                val questionText = root.findViewById<EditText>(R.id.etQuestion)?.text.toString().trim()
                val optionA = root.findViewById<EditText>(R.id.etOptionA)?.text.toString().trim()
                val optionB = root.findViewById<EditText>(R.id.etOptionB)?.text.toString().trim()
                val optionC = root.findViewById<EditText>(R.id.etOptionC)?.text.toString().trim()
                val optionD = root.findViewById<EditText>(R.id.etOptionD)?.text.toString().trim()

                val selectedId = root.findViewById<RadioGroup>(R.id.rgCorrect)?.checkedRadioButtonId
                val correctIndex = when (selectedId?.let { root.findViewById<RadioButton>(it).text.toString() }) {
                    "A" -> 0
                    "B" -> 1
                    "C" -> 2
                    "D" -> 3
                    else -> -1
                }

                if (questionText.isEmpty() || (selectedType == "quiz" && correctIndex == -1)) {
                    Toast.makeText(this, "Please complete Question ${index + 1}", Toast.LENGTH_SHORT).show()
                    return
                }

                val quiz = QuizCreateRequest(
                    session_id = sessionId,
                    teacher_id = teacherId,
                    type = selectedType,
                    question = questionText,
                    options = listOf(optionA, optionB, optionC, optionD),
                    correct_option = if (selectedType == "quiz") correctIndex else null
                )

                quizList.add(quiz)

            } catch (e: Exception) {
                Toast.makeText(this, "Error in Question ${index + 1}", Toast.LENGTH_SHORT).show()
                return
            }
        }

        RetrofitClient.instance.submitMultipleQuestions(quizList).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CreateQuizActivity, "✅ Tüm sorular gönderildi", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@CreateQuizActivity, "Gönderim başarısız", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                Toast.makeText(this@CreateQuizActivity, "Hata: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
