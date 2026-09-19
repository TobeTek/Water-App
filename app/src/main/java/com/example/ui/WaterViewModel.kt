package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.HydrationConfig
import com.example.data.WaterIntakeLog
import com.example.data.WaterRepository
import com.example.reminder.HydrationNotificationHelper
import com.example.reminder.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DayIntakeSummary(
    val dateString: String,
    val dayLabel: String, // e.g., "Mon", "Tue"
    val dateDisplay: String, // e.g. "Sep 15"
    val totalMl: Int,
    val goalMl: Int,
    val percent: Float, // 0.0 to 1.0+
    val isGoalMet: Boolean,
    val isToday: Boolean,
    val logCount: Int
)

data class WeeklyAnalyticsState(
    val weekRangeLabel: String = "",
    val days: List<DayIntakeSummary> = emptyList(),
    val averageMlPerDay: Int = 0,
    val totalWeekMl: Int = 0,
    val daysGoalMetCount: Int = 0,
    val currentStreakDays: Int = 0,
    val bestDayName: String = "-",
    val bestDayMl: Int = 0,
    val selectedDay: DayIntakeSummary? = null,
    val drinkTypeBreakdown: Map<String, Int> = emptyMap(),
    val weekOffset: Int = 0
)

data class TodayUiState(
    val currentDateDisplay: String = "",
    val todayDateString: String = "",
    val totalIntakeMl: Int = 0,
    val dailyGoalMl: Int = 2200,
    val progressPercent: Float = 0f,
    val remainingMl: Int = 2200,
    val logs: List<WaterIntakeLog> = emptyList()
)

class WaterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WaterRepository
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())

    val config: StateFlow<HydrationConfig>

    private val _todayDateString = MutableStateFlow(dateFormat.format(Date()))
    val todayDateString = _todayDateString.asStateFlow()

    private val _weekOffset = MutableStateFlow(0)
    val weekOffset = _weekOffset.asStateFlow()

    private val _selectedAnalyticsDayDate = MutableStateFlow<String?>(null)
    val selectedAnalyticsDayDate = _selectedAnalyticsDayDate.asStateFlow()

    val todayUiState: StateFlow<TodayUiState>
    val weeklyAnalytics: StateFlow<WeeklyAnalyticsState>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = WaterRepository(db.waterDao())

        // Seed initial data for historical analytics if fresh app
        viewModelScope.launch {
            repository.seedPastWeekDataIfEmpty()
        }

        config = repository.config.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HydrationConfig()
        )

        // Today UI State flow
        todayUiState = combine(
            _todayDateString,
            repository.getAllLogs(),
            config
        ) { todayDate, allLogs, cfg ->
            val todayLogs = allLogs.filter { it.dateString == todayDate }
            val total = todayLogs.sumOf { it.amountMl }
            val goal = cfg.dailyGoalMl
            val percent = if (goal > 0) (total.toFloat() / goal.toFloat()).coerceIn(0f, 2f) else 0f
            val remaining = (goal - total).coerceAtLeast(0)

            TodayUiState(
                currentDateDisplay = displayFormat.format(Date()),
                todayDateString = todayDate,
                totalIntakeMl = total,
                dailyGoalMl = goal,
                progressPercent = percent,
                remainingMl = remaining,
                logs = todayLogs
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TodayUiState()
        )

        // Weekly Analytics State flow
        weeklyAnalytics = combine(
            _weekOffset,
            repository.getAllLogs(),
            config,
            _selectedAnalyticsDayDate
        ) { offset, allLogs, cfg, selectedDate ->
            computeWeeklyAnalytics(offset, allLogs, cfg, selectedDate)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WeeklyAnalyticsState()
        )
    }

    private fun computeWeeklyAnalytics(
        offset: Int,
        allLogs: List<WaterIntakeLog>,
        cfg: HydrationConfig,
        selectedDateStr: String?
    ): WeeklyAnalyticsState {
        val cal = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            add(Calendar.WEEK_OF_YEAR, offset)
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val dayShortFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val monthDayFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        val rangeMonthFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        val days = mutableListOf<DayIntakeSummary>()
        val startCal = cal.clone() as Calendar

        val todayStr = dateFormat.format(Date())

        for (i in 0 until 7) {
            val dStr = dateFormat.format(cal.time)
            val dLogs = allLogs.filter { it.dateString == dStr }
            val dTotal = dLogs.sumOf { it.amountMl }
            val percent = if (cfg.dailyGoalMl > 0) dTotal.toFloat() / cfg.dailyGoalMl.toFloat() else 0f

            days.add(
                DayIntakeSummary(
                    dateString = dStr,
                    dayLabel = dayShortFormat.format(cal.time),
                    dateDisplay = monthDayFormat.format(cal.time),
                    totalMl = dTotal,
                    goalMl = cfg.dailyGoalMl,
                    percent = percent,
                    isGoalMet = dTotal >= cfg.dailyGoalMl,
                    isToday = dStr == todayStr,
                    logCount = dLogs.size
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        val endCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
        val weekRangeLabel = "${rangeMonthFormat.format(startCal.time)} – ${rangeMonthFormat.format(endCal.time)}"

        val weekDateStrings = days.map { it.dateString }.toSet()
        val weekLogs = allLogs.filter { it.dateString in weekDateStrings }

        val totalWeekMl = days.sumOf { it.totalMl }
        val daysWithIntake = days.count { it.totalMl > 0 }
        val avgMl = if (daysWithIntake > 0) totalWeekMl / daysWithIntake else totalWeekMl / 7
        val goalMetCount = days.count { it.isGoalMet }

        val bestDay = days.maxByOrNull { it.totalMl }
        val bestDayLabel = if (bestDay != null && bestDay.totalMl > 0) "${bestDay.dayLabel} (${bestDay.totalMl}ml)" else "None yet"

        // Streak calculation (consecutive days ending today or yesterday where goal was met)
        val streak = calculateStreak(allLogs, cfg.dailyGoalMl)

        // Drink type breakdown for this week
        val breakdown = mutableMapOf<String, Int>()
        weekLogs.forEach { log ->
            breakdown[log.drinkType] = (breakdown[log.drinkType] ?: 0) + log.amountMl
        }

        val selectedDay = days.find { it.dateString == selectedDateStr } ?: days.find { it.isToday } ?: days.lastOrNull()

        return WeeklyAnalyticsState(
            weekRangeLabel = weekRangeLabel,
            days = days,
            averageMlPerDay = avgMl,
            totalWeekMl = totalWeekMl,
            daysGoalMetCount = goalMetCount,
            currentStreakDays = streak,
            bestDayName = bestDayLabel,
            bestDayMl = bestDay?.totalMl ?: 0,
            selectedDay = selectedDay,
            drinkTypeBreakdown = breakdown,
            weekOffset = offset
        )
    }

    private fun calculateStreak(allLogs: List<WaterIntakeLog>, goalMl: Int): Int {
        val intakeByDate = allLogs.groupBy { it.dateString }
            .mapValues { entry -> entry.value.sumOf { it.amountMl } }

        var streak = 0
        val checkCal = Calendar.getInstance()
        val todayStr = dateFormat.format(checkCal.time)

        val todayIntake = intakeByDate[todayStr] ?: 0
        if (todayIntake >= goalMl) {
            streak++
        }

        // Check backwards from yesterday
        checkCal.add(Calendar.DAY_OF_YEAR, -1)
        while (true) {
            val dStr = dateFormat.format(checkCal.time)
            val intake = intakeByDate[dStr] ?: 0
            if (intake >= goalMl) {
                streak++
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    fun addWater(amountMl: Int, drinkType: String = "Water", note: String = "") {
        viewModelScope.launch {
            repository.logWater(
                amountMl = amountMl,
                drinkType = drinkType,
                note = note,
                timestamp = System.currentTimeMillis()
            )
        }
    }

    fun deleteLog(id: Long) {
        viewModelScope.launch {
            repository.deleteLog(id)
        }
    }

    fun updateDailyGoal(newGoalMl: Int) {
        viewModelScope.launch {
            val current = config.value
            val updated = current.copy(dailyGoalMl = newGoalMl)
            repository.updateConfig(updated)
        }
    }

    fun updateReminderSettings(
        enabled: Boolean,
        intervalMinutes: Int,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        vibrate: Boolean,
        sound: Boolean
    ) {
        viewModelScope.launch {
            val current = config.value
            val updated = current.copy(
                remindersEnabled = enabled,
                reminderIntervalMinutes = intervalMinutes,
                startHour = startHour,
                startMinute = startMinute,
                endHour = endHour,
                endMinute = endMinute,
                vibrateEnabled = vibrate,
                soundEnabled = sound
            )
            repository.updateConfig(updated)

            val context = getApplication<Application>().applicationContext
            if (enabled) {
                ReminderScheduler.scheduleNextReminder(context, updated)
            } else {
                ReminderScheduler.cancelReminders(context)
            }
        }
    }

    fun triggerTestReminder() {
        val context = getApplication<Application>().applicationContext
        HydrationNotificationHelper.showHydrationNotification(
            context = context,
            title = "Time to Hydrate! 💧",
            message = "Stay refreshed! Drink a glass of water now."
        )
    }

    fun changeWeekOffset(delta: Int) {
        _weekOffset.value += delta
    }

    fun resetToCurrentWeek() {
        _weekOffset.value = 0
    }

    fun selectAnalyticsDay(dateString: String) {
        _selectedAnalyticsDayDate.value = dateString
    }
}
