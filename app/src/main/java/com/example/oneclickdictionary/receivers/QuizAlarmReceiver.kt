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
import com.example.oneclickdictionary.MainActivity
import com.example.oneclickdictionary.R

class QuizAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val QUIZ_ALARM_ID = 2
        const val EXTRA_ALARM_ID = "alarm_id"
    }

    private fun showQuizNotification(context: Context) {
        val channelId = "quiz_channel"
        val NOTIFICATION_ID = 1

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Daily Quiz", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent to open MainActivity and navigate to Quiz tab
        val openQuizIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_TAB_INDEX, 2) // Quiz tab is at position 2
        }
        val openQuizPendingIntent = PendingIntent.getActivity(
            context,
            QUIZ_ALARM_ID,
            openQuizIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Daily Quiz")
            .setContentText("Test your vocabulary! Take today's quiz now.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(openQuizPendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    override fun onReceive(context: Context, intent: Intent) {
        showQuizNotification(context)

        Log.d("QuizAlarmReceiver", "Quiz notification shown")

        // Reschedule for tomorrow
        MainActivity.scheduleQuizNotification(context, QUIZ_ALARM_ID, 12, 0)
    }
}
