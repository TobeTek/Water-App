package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_intake_logs")
data class WaterIntakeLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amountMl: Int,
    val drinkType: String = "Water",
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String, // e.g. "2026-09-18"
    val note: String = ""
)
