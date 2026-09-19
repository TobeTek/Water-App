package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import com.example.ui.WeeklyAnalyticsState
import com.example.ui.components.WeeklyBarChart
import com.example.ui.theme.AquaTertiary
import com.example.ui.theme.CeruleanSecondary
import com.example.ui.theme.GoldStreak
import com.example.ui.theme.OceanBluePrimary
import com.example.ui.theme.SuccessEmerald

@Composable
fun WeeklyAnalyticsScreen(
    analyticsState: WeeklyAnalyticsState,
    dailyGoalMl: Int,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onResetToCurrentWeek: () -> Unit,
    onSelectDay: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("weekly_analytics_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Week Selector Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onPreviousWeek,
                        modifier = Modifier.testTag("prev_week_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Week"
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = analyticsState.weekRangeLabel.ifEmpty { "Weekly Overview" },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (analyticsState.weekOffset != 0) {
                            TextButton(
                                onClick = onResetToCurrentWeek,
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(
                                    text = "Return to Current Week",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            Text(
                                text = "Current Week",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(
                        onClick = onNextWeek,
                        enabled = analyticsState.weekOffset < 0,
                        modifier = Modifier.testTag("next_week_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Week"
                        )
                    }
                }
            }
        }

        // Weekly Visual Progress Report (Bar Chart)
        item {
            WeeklyBarChart(
                days = analyticsState.days,
                dailyGoalMl = dailyGoalMl,
                selectedDateString = analyticsState.selectedDay?.dateString,
                onSelectDay = onSelectDay,
                modifier = Modifier.testTag("weekly_chart_component")
            )
        }

        // Selected Day Details Card
        analyticsState.selectedDay?.let { selected ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${selected.dayLabel} (${selected.dateDisplay})",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                if (selected.isToday) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(
                                            text = "Today",
                                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${selected.logCount} drinks recorded",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${selected.totalMl} ml",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            val percentInt = (selected.percent * 100).toInt()
                            Text(
                                text = "$percentInt% of daily goal",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (selected.isGoalMet) SuccessEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (selected.isGoalMet) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }
        }

        // 2x2 Grid of Key Weekly Analytics Metrics
        item {
            Text(
                text = "Key Weekly Metrics",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Daily Average",
                    value = "${analyticsState.averageMlPerDay} ml",
                    subtitle = "Target: ${dailyGoalMl} ml",
                    icon = Icons.Default.Speed,
                    iconTint = OceanBluePrimary,
                    modifier = Modifier.weight(1f)
                )

                val liters = String.format(Locale.US, "%.1f L", analyticsState.totalWeekMl / 1000f)
                MetricCard(
                    title = "Weekly Total",
                    value = liters,
                    subtitle = "${analyticsState.totalWeekMl} ml drank",
                    icon = Icons.Default.WaterDrop,
                    iconTint = AquaTertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val completionPercent = if (analyticsState.days.isNotEmpty()) {
                    (analyticsState.daysGoalMetCount * 100) / analyticsState.days.size
                } else 0

                MetricCard(
                    title = "Goal Reached",
                    value = "${analyticsState.daysGoalMetCount}/7 Days",
                    subtitle = "$completionPercent% success rate",
                    icon = Icons.Default.CheckCircle,
                    iconTint = SuccessEmerald,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Current Streak",
                    value = "${analyticsState.currentStreakDays} Days",
                    subtitle = "Best: ${analyticsState.bestDayName}",
                    icon = Icons.Default.Whatshot,
                    iconTint = GoldStreak,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Beverage Type Breakdown Card
        if (analyticsState.drinkTypeBreakdown.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Weekly Beverage Breakdown",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val totalVol = analyticsState.drinkTypeBreakdown.values.sum().coerceAtLeast(1)
                        analyticsState.drinkTypeBreakdown.forEach { (type, volume) ->
                            val ratio = volume.toFloat() / totalVol.toFloat()
                            val pct = (ratio * 100).toInt()

                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = type,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "$volume ml ($pct%)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { ratio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = when (type) {
                                        "Tea" -> Color(0xFF059669)
                                        "Electrolyte" -> Color(0xFFD97706)
                                        "Juice" -> Color(0xFFEA580C)
                                        "Coffee" -> Color(0xFF854D0E)
                                        else -> OceanBluePrimary
                                    },
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Day by Day Log Details for the Week
        item {
            Text(
                text = "Day-by-Day Summary",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(
            count = analyticsState.days.size,
            key = { analyticsState.days[it].dateString }
        ) { index ->
            val day = analyticsState.days[index]
            DaySummaryRowItem(
                day = day,
                dailyGoalMl = dailyGoalMl,
                onClick = { onSelectDay(day.dateString) }
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .background(iconTint.copy(alpha = 0.12f), CircleShape)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun DaySummaryRowItem(
    day: com.example.ui.DayIntakeSummary,
    dailyGoalMl: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (day.isGoalMet) SuccessEmerald.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                ) {
                    if (day.isGoalMet) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SuccessEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = day.dayLabel.take(1),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${day.dayLabel}, ${day.dateDisplay}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (day.isToday) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Today",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Text(
                        text = "${day.logCount} drinks logged",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${day.totalMl} ml",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (day.isGoalMet) SuccessEmerald else MaterialTheme.colorScheme.onSurface
                    )
                )

                val pct = (day.percent * 100).toInt()
                Text(
                    text = "$pct% goal",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
