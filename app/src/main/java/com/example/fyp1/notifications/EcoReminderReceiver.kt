package com.example.fyp1.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class EcoReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == EcoReminderScheduler.ACTION_ECO_REMINDER) {
            EcoReminderScheduler.showReminderNotification(context)
            EcoReminderScheduler.scheduleNext(context)
        } else if (intent?.action == EcoReminderScheduler.ACTION_LEADERBOARD_CHAMPION) {
            EcoReminderScheduler.showLeaderboardChampionNotification(context)
            EcoReminderScheduler.scheduleNext(context)
        } else if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            EcoReminderScheduler.scheduleNext(context)
        }
    }
}
