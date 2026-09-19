package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.WorkspacePremium
import com.example.data.HydrationConfig
import com.example.ui.theme.AquaTertiary
import com.example.ui.theme.CeruleanSecondary
import com.example.ui.theme.GoldStreak
import com.example.ui.theme.OceanBluePrimary
import com.example.ui.theme.SuccessEmerald

@Composable
fun RemindersScreen(
    config: HydrationConfig,
    onSaveReminderSettings: (
        enabled: Boolean,
        intervalMin: Int,
        startHour: Int,
        startMin: Int,
        endHour: Int,
        endMin: Int,
        vibrate: Boolean,
        sound: Boolean
    ) -> Unit,
    onUpdateDailyGoal: (Int) -> Unit,
    onTriggerTestReminder: () -> Unit,
    isProUser: Boolean = false,
    adCount: Int = 0,
    onOpenPaywall: () -> Unit = {},
    onTestTriggerAd: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Check notification permission for Android 13+
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Notification permission granted! 💧", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notifications are required for hydration reminders", Toast.LENGTH_LONG).show()
        }
    }

    // Local mutable state initialized from config
    var isEnabled by remember(config) { mutableStateOf(config.remindersEnabled) }
    var intervalMinutes by remember(config) { mutableIntStateOf(config.reminderIntervalMinutes) }
    var startHour by remember(config) { mutableIntStateOf(config.startHour) }
    var endHour by remember(config) { mutableIntStateOf(config.endHour) }
    var vibrate by remember(config) { mutableStateOf(config.vibrateEnabled) }
    var sound by remember(config) { mutableStateOf(config.soundEnabled) }

    var goalMl by remember(config) { mutableFloatStateOf(config.dailyGoalMl.toFloat()) }

    val intervalOptions = listOf(
        Pair(45, "45 min"),
        Pair(60, "1 hour"),
        Pair(90, "1.5 hours"),
        Pair(120, "2 hours"),
        Pair(180, "3 hours")
    )

    val goalPresets = listOf(1800, 2000, 2200, 2500, 3000)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("reminders_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Permission Request Banner if not granted
        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Enable Notifications",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "To receive timely sips reminders, please allow notification permissions.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            },
                            modifier = Modifier.testTag("grant_notification_permission_button")
                        ) {
                            Text("Allow Notifications")
                        }
                    }
                }
            }
        }

        // Reminders Master Toggle Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(
                                        if (isEnabled) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = if (isEnabled) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Hydration Reminders",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isEnabled) "Active during scheduled hours" else "Reminders disabled",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { checked ->
                                isEnabled = checked
                                onSaveReminderSettings(
                                    checked,
                                    intervalMinutes,
                                    startHour,
                                    0,
                                    endHour,
                                    0,
                                    vibrate,
                                    sound
                                )
                            },
                            modifier = Modifier.testTag("reminders_master_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }

                    // Test notification action button
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = {
                            if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                onTriggerTestReminder()
                                Toast.makeText(context, "Test reminder sent! Check your notification bar.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_reminder_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Test Reminder Now")
                    }
                }
            }
        }

        // Reminder Interval Card
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reminder Frequency",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "How often you would like to be nudged to drink water:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        intervalOptions.forEach { (mins, label) ->
                            val selected = intervalMinutes == mins
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    intervalMinutes = mins
                                    onSaveReminderSettings(
                                        isEnabled,
                                        mins,
                                        startHour,
                                        0,
                                        endHour,
                                        0,
                                        vibrate,
                                        sound
                                    )
                                },
                                label = { Text(label, fontSize = 12.sp) },
                                leadingIcon = if (selected) {
                                    {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Active Hours Card
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
                        text = "Active Schedule",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Reminders will only ring during your waking hours.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Start Time",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val startAmPm = if (startHour < 12) "AM" else "PM"
                            val startDisplayH = if (startHour % 12 == 0) 12 else startHour % 12
                            Text(
                                text = String.format("%02d:00 %s", startDisplayH, startAmPm),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = {
                                    if (startHour > 5) {
                                        startHour--
                                        onSaveReminderSettings(isEnabled, intervalMinutes, startHour, 0, endHour, 0, vibrate, sound)
                                    }
                                },
                                modifier = Modifier.size(38.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = {
                                    if (startHour < endHour - 1) {
                                        startHour++
                                        onSaveReminderSettings(isEnabled, intervalMinutes, startHour, 0, endHour, 0, vibrate, sound)
                                    }
                                },
                                modifier = Modifier.size(38.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Bedtime / End Time",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val endAmPm = if (endHour < 12) "AM" else "PM"
                            val endDisplayH = if (endHour % 12 == 0) 12 else endHour % 12
                            Text(
                                text = String.format("%02d:00 %s", endDisplayH, endAmPm),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = {
                                    if (endHour > startHour + 1) {
                                        endHour--
                                        onSaveReminderSettings(isEnabled, intervalMinutes, startHour, 0, endHour, 0, vibrate, sound)
                                    }
                                },
                                modifier = Modifier.size(38.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = {
                                    if (endHour < 23) {
                                        endHour++
                                        onSaveReminderSettings(isEnabled, intervalMinutes, startHour, 0, endHour, 0, vibrate, sound)
                                    }
                                },
                                modifier = Modifier.size(38.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Sound & Vibration Alert Options
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
                        text = "Alert Preferences",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = OceanBluePrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Vibrate", style = MaterialTheme.typography.bodyMedium)
                        }
                        Switch(
                            checked = vibrate,
                            onCheckedChange = {
                                vibrate = it
                                onSaveReminderSettings(isEnabled, intervalMinutes, startHour, 0, endHour, 0, it, sound)
                            }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = OceanBluePrimary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Sound", style = MaterialTheme.typography.bodyMedium)
                        }
                        Switch(
                            checked = sound,
                            onCheckedChange = {
                                sound = it
                                onSaveReminderSettings(isEnabled, intervalMinutes, startHour, 0, endHour, 0, vibrate, it)
                            }
                        )
                    }
                }
            }
        }

        // Daily Hydration Target Goal Setting
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Daily Intake Goal",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "${goalMl.toInt()}",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ml / day",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Slider(
                        value = goalMl,
                        onValueChange = {
                            val rounded = (it / 50).toInt() * 50f
                            goalMl = rounded
                        },
                        onValueChangeFinished = {
                            onUpdateDailyGoal(goalMl.toInt())
                        },
                        valueRange = 1000f..4000f,
                        steps = 59,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("daily_goal_slider")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Goal preset chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        goalPresets.forEach { preset ->
                            val selected = goalMl.toInt() == preset
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    goalMl = preset.toFloat()
                                    onUpdateDailyGoal(preset)
                                },
                                label = { Text("${preset}ml", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // RevenueCat & Ad Monetization Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        if (isProUser) GoldStreak.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer,
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = if (isProUser) Icons.Default.WorkspacePremium else Icons.Default.Campaign,
                                    contentDescription = null,
                                    tint = if (isProUser) GoldStreak else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "RevenueCat Monetization",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isProUser) "Pro Member (Ad-Free Active)" else "Free Tier (Action-Triggered Ads)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isProUser) GoldStreak else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "$adCount ads shown",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Ads are shown whenever a user completes an action (logging water, saving goals, or modifying reminders), automatically tracked via RevenueCat Ad Monetization.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onTestTriggerAd,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("trigger_action_ad_button")
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Trigger Ad", fontSize = 12.sp)
                        }

                        Button(
                            onClick = onOpenPaywall,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_paywall_button")
                        ) {
                            Icon(Icons.Default.Diamond, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isProUser) "Manage Pro" else "Remove Ads", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
