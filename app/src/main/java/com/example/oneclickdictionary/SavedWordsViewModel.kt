package com.example.oneclickdictionary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SavedWordsViewModel(application: Application) : AndroidViewModel(application) {
    private val database: DictionaryDBHelper = DictionaryDBHelper(application)
    private val _savedWords = MutableLiveData<MutableMap<String, MutableList<String>>>()
    val savedWords: LiveData<MutableMap<String, MutableList<String>>> = _savedWords

    init {
        loadSavedWordsFromDb()
    }

    fun addWord(word: String, definitions: MutableList<String>) {
        val currentMap = _savedWords.value?.toMutableMap() ?: mutableMapOf()
        currentMap[word] = definitions
        _savedWords.value = currentMap
    }

    fun removeWord(word: String) {
        CoroutineScope(Dispatchers.IO).launch {
            database.removeWord(word)
            loadSavedWordsFromDb()
        }
    }

    fun loadSavedWords() {
        loadSavedWordsFromDb()
    }

    private fun loadSavedWordsFromDb() {
        CoroutineScope(Dispatchers.IO).launch {
            val wordsFromDb = database.getSavedWords()
            _savedWords.postValue(wordsFromDb)
        }
    }
}