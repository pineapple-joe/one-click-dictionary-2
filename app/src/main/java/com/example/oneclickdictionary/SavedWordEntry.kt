package com.example.oneclickdictionary

data class SavedWordEntry(
    val word: String,
    val definitions: MutableList<String>,
    val timestamp: Long
)
