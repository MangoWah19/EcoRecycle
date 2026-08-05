package com.example.fyp1.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.example.fyp1.MainActivity
import com.example.fyp1.R
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

object EcoReminderScheduler {
    const val ACTION_ECO_REMINDER = "com.example.fyp1.notifications.ECO_REMINDER"
    const val ACTION_LEADERBOARD_CHAMPION = "com.example.fyp1.notifications.LEADERBOARD_CHAMPION"

    private const val CHANNEL_ID = "eco_daily_reminders"
    private const val CHANNEL_NAME = "EcoRecycle reminders"
    private const val REMINDER_REQUEST_CODE = 7104
    private const val LEADERBOARD_REQUEST_CODE = 7106
    private const val PREFS_NAME = "ecorecycle_notifications"
    private const val DAILY_CHAMPION_NAME = "daily_champion_name"
    private const val DAILY_CHAMPION_POINTS = "daily_champion_points"
    private const val DAILY_CHAMPION_GENERATED_AT = "daily_champion_generated_at"
    private val malaysiaZone: ZoneId = ZoneId.of("Asia/Kuala_Lumpur")

    fun ensureScheduled(context: Context) {
        createChannel(context)
        scheduleNext(context.applicationContext)
    }

    fun scheduleNext(context: Context) {
        scheduleNextReminder(context)
        scheduleNextLeaderboardChampion(context)
    }

    private fun scheduleNextReminder(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val triggerAt = nextMalaysiaTimeMillis(hour = 12, minute = 0)
        val pendingIntent = reminderPendingIntent(context, ACTION_ECO_REMINDER, REMINDER_REQUEST_CODE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    private fun scheduleNextLeaderboardChampion(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val triggerAt = nextMalaysiaTimeMillis(hour = 15, minute = 0)
        val pendingIntent = reminderPendingIntent(context, ACTION_LEADERBOARD_CHAMPION, LEADERBOARD_REQUEST_CODE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun cacheDailyChampion(context: Context, name: String, points: Int, generatedAt: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(DAILY_CHAMPION_NAME, name)
            .putInt(DAILY_CHAMPION_POINTS, points)
            .putString(DAILY_CHAMPION_GENERATED_AT, generatedAt)
            .apply()
    }

    fun showReminderNotification(context: Context) {
        if (!canPostNotifications(context)) return
        createChannel(context)

        val dayIndex = ZonedDateTime.now(malaysiaZone).dayOfYear % reminderMessages.size
        val message = reminderMessages[dayIndex]
        showNotification(
            context = context,
            notificationId = REMINDER_REQUEST_CODE + dayIndex,
            title = message.title,
            body = message.body
        )
    }

    fun showLeaderboardChampionNotification(context: Context) {
        if (!canPostNotifications(context)) return
        createChannel(context)

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(DAILY_CHAMPION_NAME, null)
        val points = prefs.getInt(DAILY_CHAMPION_POINTS, 0)
        val body = if (!name.isNullOrBlank() && points > 0) {
            "Congratulations to $name, today's EcoRecycle leaderboard champion with $points points."
        } else {
            "Check today's EcoRecycle leaderboard and see who is leading the campus recycling challenge."
        }
        showNotification(
            context = context,
            notificationId = LEADERBOARD_REQUEST_CODE,
            title = "Daily Leaderboard Champion",
            body = body
        )
    }

    private fun showNotification(context: Context, notificationId: Int, title: String, body: String) {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(context, notificationId + 1, launchIntent, pendingIntentFlags())

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context)
        }

        val notification = builder
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(Notification.BigTextStyle().bigText(body))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        context.getSystemService(NotificationManager::class.java)
            .notify(notificationId, notification)
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Daily EcoRecycle motivation and reminder notifications."
        }
        manager.createNotificationChannel(channel)
    }

    private fun reminderPendingIntent(context: Context, action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, EcoReminderReceiver::class.java).apply {
            this.action = action
        }
        return PendingIntent.getBroadcast(context, requestCode, intent, pendingIntentFlags())
    }

    private fun pendingIntentFlags(): Int =
        PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

    private fun canPostNotifications(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun nextMalaysiaTimeMillis(hour: Int, minute: Int): Long {
        val now = ZonedDateTime.now(malaysiaZone)
        var nextTime = now.with(LocalTime.of(hour, minute))
        if (!nextTime.isAfter(now)) {
            nextTime = nextTime.plusDays(1)
        }
        return nextTime.toInstant().toEpochMilli()
    }

    private data class ReminderMessage(val title: String, val body: String)

    private val reminderMessages = listOf(
        ReminderMessage(
            title = "Ready for a quick eco win?",
            body = "Open EcoRecycle and check if there is a mission or recycling deposit you can complete today."
        ),
        ReminderMessage(
            title = "Your campus impact grows daily",
            body = "A small recycling action today can move your points, badges, and leaderboard rank."
        ),
        ReminderMessage(
            title = "Keep your green streak alive",
            body = "Visit EcoRecycle to review missions, learn something new, or record your next deposit."
        )
    )
}
