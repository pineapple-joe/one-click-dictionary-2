package com.example.oneclickdictionary.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.oneclickdictionary.DictionaryDBHelper
import com.example.oneclickdictionary.R
import com.example.oneclickdictionary.Word

class AlarmReceiver : BroadcastReceiver() {

    private fun showWordNotification(context: Context, word: Word) {
        val channelId = "word_channel"
        val NOTIFICATION_ID = 0
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Word of the Day", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val saveIntent = Intent(context, SaveWordReceiver::class.java).apply {
            action = "SAVE_WORD"
            putExtra("WORD", word.word)
            putExtra("DEFINITION", word.definition)
        }
        val savePendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 0, saveIntent, PendingIntent.FLAG_MUTABLE)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Word of the Day: ${word.word}")
            .setContentText(word.definition)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(R.drawable.baseline_bookmark_24, "Save Word", savePendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    override fun onReceive(context: Context, intent: Intent) {
        val dbHelper = DictionaryDBHelper(context)
        val randomWord = dbHelper.getRandomWord()
        showWordNotification(context, randomWord)

        Log.d("AlarmReceiver", "Random word: $randomWord")
    }
}