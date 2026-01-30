package com.example.oneclickdictionary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

sealed class QuizState {
    object Loading : QuizState()
    data class Question(
        val word: String,
        val correctDefinition: String,
        val options: List<String>,
        val correctIndex: Int
    ) : QuizState()
    data class AnswerResult(
        val word: String,
        val correctDefinition: String,
        val options: List<String>,
        val correctIndex: Int,
        val selectedIndex: Int,
        val isCorrect: Boolean
    ) : QuizState()
    data class Error(val message: String) : QuizState()
}

class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val database: DictionaryDBHelper = DictionaryDBHelper(application)

    private val _quizState = MutableLiveData<QuizState>()
    val quizState: LiveData<QuizState> = _quizState

    private val _score = MutableLiveData<Int>()
    val score: LiveData<Int> = _score

    init {
        _score.value = ScoreHelper.getTotalScore(application)
    }

    fun loadNewQuestion() {
        _quizState.value = QuizState.Loading
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val randomWord = database.getRandomWord()
                if (randomWord.word.isEmpty()) {
                    _quizState.postValue(QuizState.Error("No words available"))
                    return@launch
                }

                val wrongDefinitions = database.getRandomDefinitions(2, randomWord.word)
                if (wrongDefinitions.size < 2) {
                    _quizState.postValue(QuizState.Error("Not enough words for quiz"))
                    return@launch
                }

                val allOptions = mutableListOf(randomWord.definition)
                allOptions.addAll(wrongDefinitions)
                allOptions.shuffle()

                val correctIndex = allOptions.indexOf(randomWord.definition)

                _quizState.postValue(
                    QuizState.Question(
                        word = randomWord.word,
                        correctDefinition = randomWord.definition,
                        options = allOptions,
                        correctIndex = correctIndex
                    )
                )
            } catch (e: Exception) {
                _quizState.postValue(QuizState.Error("Error loading question: ${e.message}"))
            }
        }
    }

    fun submitAnswer(selectedIndex: Int) {
        val currentState = _quizState.value
        if (currentState is QuizState.Question) {
            val isCorrect = selectedIndex == currentState.correctIndex

            ScoreHelper.recordAnswer(getApplication(), isCorrect)
            _score.postValue(ScoreHelper.getTotalScore(getApplication()))

            _quizState.value = QuizState.AnswerResult(
                word = currentState.word,
                correctDefinition = currentState.correctDefinition,
                options = currentState.options,
                correctIndex = currentState.correctIndex,
                selectedIndex = selectedIndex,
                isCorrect = isCorrect
            )
        }
    }

    fun saveCurrentWord(): Boolean {
        val currentState = _quizState.value
        if (currentState is QuizState.AnswerResult) {
            val definitions = arrayListOf(currentState.correctDefinition)
            database.addWord(currentState.word, definitions)
            return true
        }
        return false
    }
}
