package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.WaterIntakeLog
import com.example.ui.TodayUiState
import com.example.ui.components.AddWaterDialog
import com.example.ui.components.WaterGauge
import com.example.ui.theme.AquaTertiary
import com.example.ui.theme.CeruleanSecondary
import com.example.ui.theme.GoldStreak
import com.example.ui.theme.OceanBlueDark
import com.example.ui.theme.OceanBluePrimary
import com.example.ui.theme.SuccessEmerald
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TodayScreen(
    todayState: TodayUiState,
    streakDays: Int,
    onAddWater: (amountMl: Int, drinkType: String, note: String) -> Unit,
    onDeleteLog: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var dialogInitialAmount by remember { mutableStateOf(250) }
    var logToDelete by remember { mutableStateOf<WaterIntakeLog?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("today_screen_column"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Hero Header with Greeting & Streak
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                // Hero Image banner
                Image(
                    painter = painterResource(id = R.drawable.img_water_hero),
                    contentDescription = "Water Waves Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark gradient overlay for text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x77061A30),
                                    Color(0xDD082038)
                                )
                            )
                        )
                )

                // Header Texts & Streak Pill
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Daily Hydration",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = todayState.currentDateDisplay.ifEmpty { "Today" },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            )
                        }

                        // Streak Badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = GoldStreak.copy(alpha = 0.25f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldStreak)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Whatshot,
                                    contentDescription = "Streak",
                                    tint = GoldStreak,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$streakDays Day Streak",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // Motivational quote
                    Text(
                        text = if (todayState.progressPercent >= 1.0f) {
                            "Awesome job! Daily hydration goal reached. 💧"
                        } else {
                            "Drink regularly throughout the day for optimal energy."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    )
                }
            }
        }

        // Circular Water Gauge Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .testTag("water_gauge_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    WaterGauge(
                        currentMl = todayState.totalIntakeMl,
                        goalMl = todayState.dailyGoalMl,
                        progress = todayState.progressPercent,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        // Quick Add Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quick Add Intake",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    TextButton(
                        onClick = {
                            dialogInitialAmount = 250
                            showAddDialog = true
                        },
                        modifier = Modifier.testTag("custom_add_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Custom")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 4 Preset Buttons Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickAddCard(
                        amountMl = 150,
                        label = "Cup",
                        onClick = { onAddWater(150, "Water", "Quick cup") },
                        modifier = Modifier.weight(1f),
                        testTag = "quick_add_150"
                    )
                    QuickAddCard(
                        amountMl = 250,
                        label = "Glass",
                        onClick = { onAddWater(250, "Water", "Standard glass") },
                        modifier = Modifier.weight(1f),
                        testTag = "quick_add_250"
                    )
                    QuickAddCard(
                        amountMl = 500,
                        label = "Bottle",
                        onClick = { onAddWater(500, "Water", "Water bottle") },
                        modifier = Modifier.weight(1f),
                        testTag = "quick_add_500"
                    )
                    QuickAddCard(
                        amountMl = 750,
                        label = "Flask",
                        onClick = { onAddWater(750, "Water", "Tumbler flask") },
                        modifier = Modifier.weight(1f),
                        testTag = "quick_add_750"
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Today's Logs Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Intake Log (${todayState.logs.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (todayState.logs.isNotEmpty()) {
                    Text(
                        text = "Total ${todayState.totalIntakeMl} ml",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Empty state or list of logs
        if (todayState.logs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = AquaTertiary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No water logged yet today",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap a quick add button above to record your first drink!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(
                items = todayState.logs,
                key = { it.id }
            ) { log ->
                WaterLogItem(
                    log = log,
                    onDeleteClick = { logToDelete = log }
                )
            }
        }
    }

    // Custom Add Dialog
    if (showAddDialog) {
        AddWaterDialog(
            initialAmount = dialogInitialAmount,
            onDismiss = { showAddDialog = false },
            onConfirm = { amount, type, note ->
                onAddWater(amount, type, note)
                showAddDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    logToDelete?.let { log ->
        AlertDialog(
            onDismissRequest = { logToDelete = null },
            title = { Text("Delete Entry") },
            text = { Text("Are you sure you want to remove this ${log.amountMl}ml entry?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteLog(log.id)
                        logToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { logToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun QuickAddCard(
    amountMl: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .testTag(testTag)
            .height(94.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "+${amountMl}ml",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WaterLogItem(
    log: WaterIntakeLog,
    onDeleteClick: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = remember(log.timestamp) { timeFormat.format(Date(log.timestamp)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("log_item_${log.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            when (log.drinkType) {
                                "Tea" -> Color(0xFFD1FAE5)
                                "Electrolyte" -> Color(0xFFFEF3C7)
                                "Coffee" -> Color(0xFFFED7AA)
                                else -> MaterialTheme.colorScheme.primaryContainer
                            },
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = when (log.drinkType) {
                            "Coffee" -> Icons.Default.Coffee
                            else -> Icons.Default.LocalDrink
                        },
                        contentDescription = null,
                        tint = when (log.drinkType) {
                            "Tea" -> Color(0xFF059669)
                            "Electrolyte" -> Color(0xFFD97706)
                            "Coffee" -> Color(0xFF854D0E)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = log.drinkType,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (log.note.isNotEmpty()) {
                        Text(
                            text = log.note,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "+${log.amountMl} ml",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete entry",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
