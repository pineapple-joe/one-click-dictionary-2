package com.example.oneclickdictionary

import android.content.Context

object ScoreHelper {
    private const val PREFS_NAME = "quiz_score_prefs"
    private const val KEY_TOTAL_SCORE = "total_score"
    private const val KEY_CORRECT_ANSWERS = "correct_answers"
    private const val KEY_TOTAL_QUESTIONS = "total_questions"

    fun getTotalScore(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_TOTAL_SCORE, 0)
    }

    fun addScore(context: Context, points: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentScore = prefs.getInt(KEY_TOTAL_SCORE, 0)
        prefs.edit().putInt(KEY_TOTAL_SCORE, currentScore + points).apply()
    }

    fun getCorrectAnswers(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_CORRECT_ANSWERS, 0)
    }

    fun incrementCorrectAnswers(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getInt(KEY_CORRECT_ANSWERS, 0)
        prefs.edit().putInt(KEY_CORRECT_ANSWERS, current + 1).apply()
    }

    fun getTotalQuestions(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_TOTAL_QUESTIONS, 0)
    }

    fun incrementTotalQuestions(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getInt(KEY_TOTAL_QUESTIONS, 0)
        prefs.edit().putInt(KEY_TOTAL_QUESTIONS, current + 1).apply()
    }

    fun recordAnswer(context: Context, isCorrect: Boolean) {
        incrementTotalQuestions(context)
        if (isCorrect) {
            incrementCorrectAnswers(context)
            addScore(context, 10)
        }
    }
}
