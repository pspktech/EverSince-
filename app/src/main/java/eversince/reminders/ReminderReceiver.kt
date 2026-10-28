package com.eversince.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.eversince.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel("special_days", "Special days", NotificationManager.IMPORTANCE_DEFAULT))
        val notification = NotificationCompat.Builder(context, "special_days")
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(intent.getStringExtra("title") ?: "A special day is here")
            .setContentText(intent.getStringExtra("note")?.ifBlank { "Make today memorable." } ?: "Make today memorable.")
            .setAutoCancel(true)
            .build()
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
