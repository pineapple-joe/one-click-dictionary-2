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
    private val _savedWords = MutableLiveData<List<SavedWordEntry>>()
    val savedWords: LiveData<List<SavedWordEntry>> = _savedWords

    private var _sortOrder = SortOrder.TIME_ADDED
    private var unsortedWords: List<SavedWordEntry> = emptyList()

    init {
        loadSavedWordsFromDb()
    }

    fun addWord(word: String, definitions: MutableList<String>) {
        val currentList = _savedWords.value?.toMutableList() ?: mutableListOf()
        currentList.add(SavedWordEntry(word, definitions, System.currentTimeMillis()))
        unsortedWords = currentList
        _savedWords.value = applySorting(currentList)
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

    fun setSortOrder(sortOrder: SortOrder) {
        _sortOrder = sortOrder
        _savedWords.value = applySorting(unsortedWords)
    }

    fun getSortOrder(): SortOrder = _sortOrder

    private fun applySorting(words: List<SavedWordEntry>): List<SavedWordEntry> {
        return when (_sortOrder) {
            SortOrder.TIME_ADDED -> words.sortedByDescending { it.timestamp }
            SortOrder.ALPHABETICAL -> words.sortedBy { it.word.lowercase() }
            SortOrder.RANDOM -> words.shuffled()
        }
    }

    private fun loadSavedWordsFromDb() {
        CoroutineScope(Dispatchers.IO).launch {
            val wordsFromDb = database.getSavedWords()
            unsortedWords = wordsFromDb
            _savedWords.postValue(applySorting(wordsFromDb))
        }
    }
}