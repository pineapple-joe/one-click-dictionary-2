package com.example.oneclickdictionary

interface WordSavedListener {
    fun onWordSaved(word: String, definitions: MutableList<String>)
}