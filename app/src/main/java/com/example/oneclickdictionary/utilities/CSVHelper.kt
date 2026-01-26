package com.example.oneclickdictionary.utilities

import android.content.Context
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class CSVHelper {

    fun readCSV(fileName: String, context: Context): BufferedReader {
        val manager = context.assets
        var inStream: InputStream? = null
        try {
            inStream = manager.open(fileName)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val buffer = BufferedReader(InputStreamReader(inStream))
        return buffer
    }

    fun splitLine(input: String?): MutableList<String> {
        val tokens: MutableList<String> = ArrayList()
        var startPosition = 0
        var isInQuotes = false
        if (input !== null){
            for (currentPosition in 0 until input.length) {
                if (input[currentPosition] === '\"') {
                    isInQuotes = !isInQuotes
                } else if (input[currentPosition] === ',' && !isInQuotes) {
                    tokens.add(input.substring(startPosition, currentPosition))
                    startPosition = currentPosition + 1
                }
            }

            val lastToken: String = input.substring(startPosition)
            if (lastToken == ",") {
                tokens.add("")
            } else {
                tokens.add(lastToken)
            }
        }
        return tokens
    }
}