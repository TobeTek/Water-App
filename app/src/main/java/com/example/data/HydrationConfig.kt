package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hydration_config")
data class HydrationConfig(
    @PrimaryKey
    val id: Int = 1,
    val dailyGoalMl: Int = 2200,
    val remindersEnabled: Boolean = true,
    val reminderIntervalMinutes: Int = 90,
    val startHour: Int = 8,
    val startMinute: Int = 0,
    val endHour: Int = 22,
    val endMinute: Int = 0,
    val vibrateEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val defaultCupMl: Int = 250
)
