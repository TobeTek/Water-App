package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HydrationReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Show reminder notification
        HydrationNotificationHelper.showHydrationNotification(context)

        // Reschedule next reminder based on database config
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val config = db.waterDao().getConfigDirect() ?: com.example.data.HydrationConfig()
                ReminderScheduler.scheduleNextReminder(context, config)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
