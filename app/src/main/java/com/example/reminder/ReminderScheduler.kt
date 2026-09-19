package com.example.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.HydrationConfig
import java.util.Calendar

object ReminderScheduler {

    private const val ALARM_REQUEST_CODE = 2001

    fun scheduleNextReminder(context: Context, config: HydrationConfig) {
        if (!config.remindersEnabled) {
            cancelReminders(context)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, HydrationReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextTriggerTime = calculateNextReminderTime(config)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTriggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    nextTriggerTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Permission fallback
        }
    }

    fun cancelReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, HydrationReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun calculateNextReminderTime(config: HydrationConfig): Long {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            add(Calendar.MINUTE, config.reminderIntervalMinutes)
        }

        val startCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, config.startHour)
            set(Calendar.MINUTE, config.startMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val endCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, config.endHour)
            set(Calendar.MINUTE, config.endMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return when {
            // If right now is before start time today, trigger at start time
            now.before(startCal) -> {
                startCal.timeInMillis
            }
            // If next scheduled reminder is after end time, roll over to tomorrow start time
            next.after(endCal) -> {
                startCal.apply { add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis
            }
            // Otherwise, schedule next + interval
            else -> {
                next.timeInMillis
            }
        }
    }

    fun scheduleSnooze(context: Context, minutes: Int = 15) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, HydrationReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + (minutes * 60 * 1000)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Ignore if security restricted
        }
    }
}
