package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DayIntakeSummary
import com.example.ui.theme.AquaTertiary
import com.example.ui.theme.CeruleanSecondary
import com.example.ui.theme.OceanBluePrimary
import com.example.ui.theme.SuccessEmerald

@Composable
fun WeeklyBarChart(
    days: List<DayIntakeSummary>,
    dailyGoalMl: Int,
    selectedDateString: String?,
    onSelectDay: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (days.isEmpty()) return

    val maxDayMl = (days.maxOfOrNull { it.totalMl } ?: dailyGoalMl).coerceAtLeast(dailyGoalMl)
    // Scale so max bar isn't clipped and goal line has room
    val chartUpperLimit = (maxDayMl * 1.15f).coerceAtLeast(dailyGoalMl * 1.2f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        // Chart Header with legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Weekly Progress Report",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Daily goal: ${dailyGoalMl}ml",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Legend
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(SuccessEmerald, CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Goal met",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Chart Area (Canvas + Columns)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            // Background Canvas for Target Goal Line
            val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            val primaryColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)

            Canvas(modifier = Modifier.fillMaxSize()) {
                val goalY = size.height * (1f - (dailyGoalMl.toFloat() / chartUpperLimit))

                // Dashed line for daily goal
                drawLine(
                    color = primaryColor,
                    start = Offset(0f, goalY),
                    end = Offset(size.width, goalY),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                )
            }

            // Bars row
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                days.forEach { day ->
                    val isSelected = day.dateString == selectedDateString
                    val barHeightRatio = (day.totalMl.toFloat() / chartUpperLimit).coerceIn(0f, 1f)

                    WeeklyBarColumn(
                        day = day,
                        heightRatio = barHeightRatio,
                        isSelected = isSelected,
                        onClick = { onSelectDay(day.dateString) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Day Labels Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEach { day ->
                val isSelected = day.dateString == selectedDateString
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelectDay(day.dateString) }
                ) {
                    Text(
                        text = day.dayLabel,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (day.isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        color = when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            day.isToday -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )

                    if (day.isToday) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyBarColumn(
    day: DayIntakeSummary,
    heightRatio: Float,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedHeight by animateFloatAsState(
        targetValue = heightRatio,
        animationSpec = tween(durationMillis = 800),
        label = "bar_column_anim"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag("weekly_bar_${day.dayLabel}")
    ) {
        // Goal met indicator
        if (day.isGoalMet) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(16.dp)
                    .background(SuccessEmerald, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(11.dp)
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
        } else {
            Spacer(modifier = Modifier.height(19.dp))
        }

        // The Bar
        Box(
            modifier = Modifier
                .width(if (isSelected) 22.dp else 16.dp)
                .fillMaxHeight(fraction = animatedHeight.coerceAtLeast(0.04f))
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                .background(
                    brush = when {
                        day.isGoalMet -> Brush.verticalGradient(
                            listOf(SuccessEmerald, AquaTertiary)
                        )
                        isSelected -> Brush.verticalGradient(
                            listOf(AquaTertiary, OceanBluePrimary)
                        )
                        day.totalMl > 0 -> Brush.verticalGradient(
                            listOf(CeruleanSecondary, OceanBluePrimary)
                        )
                        else -> Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                )
        )
    }
}
