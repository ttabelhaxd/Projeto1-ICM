package com.example.snapquest.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.snapquest.MainActivity
import com.example.snapquest.R
import com.example.snapquest.SnapQuestApp.Companion.CHANNEL_ID

class NotificationService(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showQuestJoinedNotification(questName: String) {
        showNotification(
            title = "Quest Joined!",
            message = "You've successfully joined $questName",
            icon = R.drawable.ic_joined
        )
    }

    fun showChallengeCompletedNotification(challengeName: String) {
        showNotification(
            title = "Challenge Completed!",
            message = "You've completed $challengeName",
            icon = R.drawable.ic_completed
        )
    }

    fun showQuestCompletedNotification(questName: String) {
        showNotification(
            title = "Quest Master!",
            message = "Congratulations! You've completed the $questName quest!",
            icon = R.drawable.ic_trophy
        )
    }

    private fun showNotification(title: String, message: String, icon: Int) {
        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val activityPendingIntent = PendingIntent.getActivity(
            context,
            0,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(activityPendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(title.hashCode(), notification)
    }
}