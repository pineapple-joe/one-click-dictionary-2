package com.example.oneclickdictionary.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.oneclickdictionary.DictionaryDBHelper

class SaveWordReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val word = intent.getStringExtra("WORD")
        val definition = intent.getStringExtra("DEFINITION")
        val NOTIFICATION_ID = 0
        if (word != null) {
            val dbHelper = DictionaryDBHelper(context)
            val definitions = arrayListOf<String>()
            if (definition != null) {
                definitions.add(definition)
            }
            dbHelper.addWord(word, definitions)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIFICATION_ID)
        }
    }
}
