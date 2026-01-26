package com.example.oneclickdictionary

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.oneclickdictionary.utilities.CSVHelper
import java.io.IOException


class DictionaryDBHelper(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "dictionary.db"
        private const val DATABASE_VERSION = 1
        private const val DATABASE_DATA_FILE = "english_dictionary.csv"
        private const val TABLE_DICTIONARY = "dictionary"
        private const val TABLE_MY_WORDS = "words"
        private const val KEY_WORD = "word"
        private const val KEY_DEFINITION = "definition"
    }

    fun createDatabase() {
        val db = writableDatabase
        if (!tableExists(db, TABLE_DICTIONARY)){
            this.initDatabase(db)
        }
    }

    private fun tableExists(sqLiteDatabase: SQLiteDatabase?, table: String?): Boolean {
        if (sqLiteDatabase == null || !sqLiteDatabase.isOpen || table == null) {
            return false
        }
        var count = 0
        val cursor = sqLiteDatabase.rawQuery(
            "SELECT COUNT(*) FROM $table",
            null,
            null
        )
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count > 0
    }

    private fun initDatabase(db: SQLiteDatabase) {
        val csvHelper = CSVHelper()
        val inputStream = csvHelper.readCSV(DATABASE_DATA_FILE, context)
        var line: String?
        db.beginTransaction()
        try {
            while (inputStream.readLine().also { line = it } != null) {
                val columns = csvHelper.splitLine(line)
                if (columns.size != 3) {
                    Log.d("CSVParser", "Skipping Bad CSV Row")
                    continue
                }
                val cv = ContentValues(3)
                cv.put(KEY_WORD, columns[0].trim { it <= ' ' })
                cv.put(KEY_DEFINITION, columns[2].trim { it <= ' ' })
                db.insert(TABLE_DICTIONARY, null, cv)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                inputStream.close()
            } catch (e: Exception) {
                println("An error occurred while closing the file: ${e.message}")
            }
        }
        db.setTransactionSuccessful()
        db.endTransaction()
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createDictionaryTable = (((("CREATE TABLE $TABLE_DICTIONARY").toString() + "("
                + KEY_WORD) + " TEXT,"
                + KEY_DEFINITION) + " TEXT" + ")")
        db!!.execSQL(createDictionaryTable)

        db.execSQL(("CREATE INDEX word_index ON $TABLE_DICTIONARY($KEY_WORD)"))

        val createMyWordsTable = (((("CREATE TABLE $TABLE_MY_WORDS").toString() + "("
                + KEY_WORD) + " TEXT,"
                + KEY_DEFINITION) + " TEXT" + ")")
        db.execSQL(createMyWordsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS " + TABLE_DICTIONARY)
        onCreate(db)
    }

    fun getWord(wordToFind: String): MutableList<Word> {
        val wordDefinitions = mutableListOf<Word>()
        try {
            val db = this.readableDatabase

            val cursor = db.query(
                TABLE_DICTIONARY,
                arrayOf(KEY_DEFINITION),
                "$KEY_WORD=?",
                arrayOf(wordToFind.replaceFirstChar { it.uppercase() }),
                null, null, null, null
            )


            with(cursor) {
                while (moveToNext()) {
                    val definition = getString(cursor.getColumnIndexOrThrow(KEY_DEFINITION))
                    val word = Word(wordToFind, definition)
                    wordDefinitions.add(word)
                }
            }
            cursor.close()
            return wordDefinitions
        }
        catch (e: Exception){
            println("An error occurred while getting word definition: ${e.message}")
        }
        return wordDefinitions

    }

    fun addWord(word: String, definitions: ArrayList<String>) {
        val db = this.writableDatabase
        try {
            db.beginTransaction()
            val values = ContentValues()
            for (definition in definitions){
                values.put(KEY_WORD, word)
                values.put(KEY_DEFINITION, definition)
                db.insert(TABLE_MY_WORDS, null, values)
            }
            db.setTransactionSuccessful()
            db.endTransaction()
        } catch (e: Exception){
            println("An error occurred while adding word definition: ${e.message}")
        }
    }

    fun removeWord(word: String) {
        val db = this.writableDatabase
        db.delete(TABLE_MY_WORDS, "$KEY_WORD = ?", arrayOf(word))
        db.close()
    }

    fun getSavedWords(): MutableMap<String, MutableList<String>> {
        val wordDefinitions = ArrayList<Word>()
        try {
            val db = this.readableDatabase

            val cursor = db.query(
                TABLE_MY_WORDS,
                arrayOf(KEY_WORD, KEY_DEFINITION),
                null,
                null,
                null, null, null, null
            )

            with(cursor) {
                while (moveToNext()) {
                    val definition = getString(cursor.getColumnIndexOrThrow(KEY_DEFINITION))
                    val word = getString(cursor.getColumnIndexOrThrow(KEY_WORD))
                    val savedWord = Word(word, definition)
                    wordDefinitions.add(savedWord)
                }
            }
            cursor.close()
        }
        catch (e: Exception){
            println("An error occurred while getting word definition: ${e.message}")
        }
        return wordDefinitions.groupByTo(mutableMapOf(), { it.word }, { it.definition })
    }

    fun getRandomWord(): Word {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_DICTIONARY,
            arrayOf(KEY_WORD, KEY_DEFINITION),
            null,
            null,
            null,
            null,
            "RANDOM()",    // orderBy
            "1"            // limit
        )

        var randomWord = Word()
        if (cursor.moveToFirst()) {
            val word = cursor.getString(cursor.getColumnIndexOrThrow(KEY_WORD))
            val definition = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DEFINITION))
            randomWord.word = word
            randomWord.definition = definition
        }

        cursor.close()
        db.close()
        return randomWord
    }
}