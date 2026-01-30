package com.example.oneclickdictionary.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.oneclickdictionary.QuizState
import com.example.oneclickdictionary.QuizViewModel
import com.example.oneclickdictionary.R
import com.google.android.material.button.MaterialButton

class QuizFragment : Fragment(R.layout.quiz_fragment) {
    private lateinit var viewModel: QuizViewModel

    private lateinit var scoreText: TextView
    private lateinit var wordText: TextView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var optionsContainer: View
    private lateinit var option1Button: MaterialButton
    private lateinit var option2Button: MaterialButton
    private lateinit var option3Button: MaterialButton
    private lateinit var feedbackText: TextView
    private lateinit var nextButton: MaterialButton
    private lateinit var errorText: TextView

    private lateinit var optionButtons: List<MaterialButton>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(requireActivity())[QuizViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.quiz_fragment, container, false)

        scoreText = view.findViewById(R.id.scoreText)
        wordText = view.findViewById(R.id.wordText)
        loadingIndicator = view.findViewById(R.id.loadingIndicator)
        optionsContainer = view.findViewById(R.id.optionsContainer)
        option1Button = view.findViewById(R.id.option1Button)
        option2Button = view.findViewById(R.id.option2Button)
        option3Button = view.findViewById(R.id.option3Button)
        feedbackText = view.findViewById(R.id.feedbackText)
        nextButton = view.findViewById(R.id.nextButton)
        errorText = view.findViewById(R.id.errorText)

        optionButtons = listOf(option1Button, option2Button, option3Button)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        optionButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                viewModel.submitAnswer(index)
            }
        }

        nextButton.setOnClickListener {
            viewModel.loadNewQuestion()
        }

        viewModel.score.observe(viewLifecycleOwner) { score ->
            scoreText.text = "Score: $score"
        }

        viewModel.quizState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is QuizState.Loading -> showLoading()
                is QuizState.Question -> showQuestion(state)
                is QuizState.AnswerResult -> showAnswerResult(state)
                is QuizState.Error -> showError(state.message)
            }
        }

        if (viewModel.quizState.value == null) {
            viewModel.loadNewQuestion()
        }
    }

    private fun showLoading() {
        loadingIndicator.visibility = View.VISIBLE
        wordText.visibility = View.GONE
        optionsContainer.visibility = View.GONE
        feedbackText.visibility = View.GONE
        nextButton.visibility = View.GONE
        errorText.visibility = View.GONE
    }

    private fun showQuestion(state: QuizState.Question) {
        loadingIndicator.visibility = View.GONE
        wordText.visibility = View.VISIBLE
        optionsContainer.visibility = View.VISIBLE
        feedbackText.visibility = View.GONE
        nextButton.visibility = View.GONE
        errorText.visibility = View.GONE

        wordText.text = state.word

        optionButtons.forEachIndexed { index, button ->
            button.text = state.options[index]
            button.isEnabled = true
            button.strokeColor = ContextCompat.getColorStateList(requireContext(), R.color.colorPrimary)
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        }
    }

    private fun showAnswerResult(state: QuizState.AnswerResult) {
        loadingIndicator.visibility = View.GONE
        wordText.visibility = View.VISIBLE
        optionsContainer.visibility = View.VISIBLE
        feedbackText.visibility = View.VISIBLE
        nextButton.visibility = View.VISIBLE
        errorText.visibility = View.GONE

        optionButtons.forEach { it.isEnabled = false }

        val correctColor = Color.parseColor("#4CAF50")
        val wrongColor = Color.parseColor("#F44336")

        optionButtons[state.correctIndex].apply {
            strokeColor = android.content.res.ColorStateList.valueOf(correctColor)
            setTextColor(correctColor)
        }

        if (!state.isCorrect) {
            optionButtons[state.selectedIndex].apply {
                strokeColor = android.content.res.ColorStateList.valueOf(wrongColor)
                setTextColor(wrongColor)
            }
        }

        if (state.isCorrect) {
            feedbackText.text = "Correct! +10 points"
            feedbackText.setTextColor(correctColor)
        } else {
            feedbackText.text = "Wrong!"
            feedbackText.setTextColor(wrongColor)
        }
    }

    private fun showError(message: String) {
        loadingIndicator.visibility = View.GONE
        wordText.visibility = View.GONE
        optionsContainer.visibility = View.GONE
        feedbackText.visibility = View.GONE
        nextButton.visibility = View.VISIBLE
        errorText.visibility = View.VISIBLE
        errorText.text = message
    }
}
