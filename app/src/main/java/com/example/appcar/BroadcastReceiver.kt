package com.example.appcar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ServiceReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationHelper = NotificationHelper(context)
        val title = intent.getStringExtra("title") ?: "Przypomnienie o przeglądzie"
        val message = intent.getStringExtra("message") ?: "Nadszedł czas na przegląd pojazdu"
        notificationHelper.sendNotification(title, message)
    }
}