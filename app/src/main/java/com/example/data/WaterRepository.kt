package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WaterRepository(private val waterDao: WaterDao) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val config: Flow<HydrationConfig> = waterDao.getConfig().map { it ?: HydrationConfig() }

    fun getLogsForDate(dateString: String): Flow<List<WaterIntakeLog>> {
        return waterDao.getLogsForDate(dateString)
    }

    fun getLogsBetween(startDate: String, endDate: String): Flow<List<WaterIntakeLog>> {
        return waterDao.getLogsBetweenDates(startDate, endDate)
    }

    fun getAllLogs(): Flow<List<WaterIntakeLog>> {
        return waterDao.getAllLogs()
    }

    suspend fun logWater(
        amountMl: Int,
        drinkType: String = "Water",
        note: String = "",
        timestamp: Long = System.currentTimeMillis()
    ): Long = withContext(Dispatchers.IO) {
        val dateString = dateFormat.format(Date(timestamp))
        val entry = WaterIntakeLog(
            amountMl = amountMl,
            drinkType = drinkType,
            timestamp = timestamp,
            dateString = dateString,
            note = note
        )
        waterDao.insertLog(entry)
    }

    suspend fun deleteLog(id: Long) = withContext(Dispatchers.IO) {
        waterDao.deleteLogById(id)
    }

    suspend fun updateConfig(config: HydrationConfig) = withContext(Dispatchers.IO) {
        waterDao.saveConfig(config)
    }

    suspend fun getConfigDirect(): HydrationConfig = withContext(Dispatchers.IO) {
        waterDao.getConfigDirect() ?: HydrationConfig().also {
            waterDao.saveConfig(it)
        }
    }

    suspend fun seedPastWeekDataIfEmpty() = withContext(Dispatchers.IO) {
        val count = waterDao.countLogs()
        if (count == 0) {
            // Seed initial config
            waterDao.saveConfig(HydrationConfig())

            // Seed logs for the past 6 days so weekly analytics and charts immediately shine
            val calendar = Calendar.getInstance()
            val sampleLogs = mutableListOf<WaterIntakeLog>()

            // Past 6 days (leaving today with fresh sample entries or blank)
            for (dayOffset in 6 downTo 1) {
                val cal = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -dayOffset)
                }
                val dateStr = dateFormat.format(cal.time)

                // Give each day realistic hydration pattern
                val dayIntakes = when (dayOffset) {
                    6 -> listOf(Pair(250, "Water"), Pair(500, "Water"), Pair(300, "Tea"), Pair(500, "Water"), Pair(400, "Electrolyte")) // 1950 ml
                    5 -> listOf(Pair(350, "Water"), Pair(500, "Water"), Pair(250, "Lemon Water"), Pair(500, "Water"), Pair(350, "Water"), Pair(250, "Tea")) // 2200 ml (met)
                    4 -> listOf(Pair(500, "Water"), Pair(500, "Water"), Pair(300, "Juice"), Pair(500, "Water"), Pair(500, "Water")) // 2300 ml (met)
                    3 -> listOf(Pair(250, "Water"), Pair(250, "Coffee"), Pair(500, "Water"), Pair(500, "Water"), Pair(300, "Tea")) // 1800 ml
                    2 -> listOf(Pair(500, "Water"), Pair(500, "Water"), Pair(500, "Water"), Pair(400, "Electrolyte"), Pair(500, "Water")) // 2400 ml (met)
                    1 -> listOf(Pair(350, "Water"), Pair(500, "Water"), Pair(500, "Water"), Pair(250, "Tea"), Pair(500, "Water"), Pair(250, "Water")) // 2350 ml (met)
                    else -> emptyList()
                }

                dayIntakes.forEachIndexed { index, (amount, type) ->
                    val entryCal = (cal.clone() as Calendar).apply {
                        set(Calendar.HOUR_OF_DAY, 8 + (index * 2))
                        set(Calendar.MINUTE, (index * 12) % 60)
                    }
                    sampleLogs.add(
                        WaterIntakeLog(
                            amountMl = amount,
                            drinkType = type,
                            timestamp = entryCal.timeInMillis,
                            dateString = dateStr,
                            note = "Daily hydration"
                        )
                    )
                }
            }

            // Also add one morning intake for today so today's tracker shows started progress
            val todayCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 8)
                set(Calendar.MINUTE, 30)
            }
            sampleLogs.add(
                WaterIntakeLog(
                    amountMl = 350,
                    drinkType = "Water",
                    timestamp = todayCal.timeInMillis,
                    dateString = dateFormat.format(Date()),
                    note = "Morning glass"
                )
            )

            waterDao.insertAll(sampleLogs)
        }
    }
}
