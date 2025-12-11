package com.example.test

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class QuizAdapter(
    private val questions: List<QuizQuestion>,
    private val onAnswerSelected: (Int, Int) -> Unit
) : RecyclerView.Adapter<QuizAdapter.QuizViewHolder>() {

    private val userAnswers = mutableMapOf<Int, Int>()

    inner class QuizViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvQuestion: TextView = itemView.findViewById(R.id.tvQuestionText)
        val rgOptions: RadioGroup = itemView.findViewById(R.id.rgOptions)
        val rb0: RadioButton = itemView.findViewById(R.id.rbOption0)
        val rb1: RadioButton = itemView.findViewById(R.id.rbOption1)
        val rb2: RadioButton = itemView.findViewById(R.id.rbOption2)
        val rb3: RadioButton = itemView.findViewById(R.id.rbOption3)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_quiz_question, parent, false)
        return QuizViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        val q = questions[position]
        holder.tvQuestion.text = "${position + 1}. ${q.question}"
        holder.rb0.text = q.optionA
        holder.rb1.text = q.optionB
        holder.rb2.text = q.optionC
        holder.rb3.text = q.optionD

        holder.rgOptions.setOnCheckedChangeListener(null)

        holder.rgOptions.clearCheck()
        val selected = userAnswers[position]
        if (selected != null) {
            when(selected) {
                0 -> holder.rb0.isChecked = true
                1 -> holder.rb1.isChecked = true
                2 -> holder.rb2.isChecked = true
                3 -> holder.rb3.isChecked = true
            }
        }

        holder.rgOptions.setOnCheckedChangeListener { _, checkedId ->
            val answerIndex = when(checkedId) {
                R.id.rbOption0 -> 0
                R.id.rbOption1 -> 1
                R.id.rbOption2 -> 2
                R.id.rbOption3 -> 3
                else -> -1
            }
            userAnswers[position] = answerIndex
            onAnswerSelected(position, answerIndex)
        }
    }

    override fun getItemCount() = questions.size
}