package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.example.data.AppDatabase
import com.example.data.WaterIntakeLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HydrationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_LOG_WATER = "com.example.reminder.ACTION_LOG_WATER"
        const val ACTION_SNOOZE = "com.example.reminder.ACTION_SNOOZE"
        const val EXTRA_AMOUNT = "extra_amount"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(HydrationNotificationHelper.NOTIFICATION_ID)

        when (intent.action) {
            ACTION_LOG_WATER -> {
                val amount = intent.getIntExtra(EXTRA_AMOUNT, 250)
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getDatabase(context)
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val now = System.currentTimeMillis()
                        val log = WaterIntakeLog(
                            amountMl = amount,
                            drinkType = "Water",
                            timestamp = now,
                            dateString = dateFormat.format(Date(now)),
                            note = "Quick log from reminder"
                        )
                        db.waterDao().insertLog(log)
                    } finally {
                        pendingResult.finish()
                    }
                }
                Toast.makeText(context, "Logged ${amount}ml of water! 💧", Toast.LENGTH_SHORT).show()
            }
            ACTION_SNOOZE -> {
                ReminderScheduler.scheduleSnooze(context, 15)
                Toast.makeText(context, "Reminder snoozed for 15 minutes", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
