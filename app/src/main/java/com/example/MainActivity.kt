package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.monetization.RevenueCatAdManager
import com.example.ui.WaterViewModel
import com.example.ui.components.AddWaterDialog
import com.example.ui.components.InAppAdDialog
import com.example.ui.components.RevenueCatPaywallDialog
import com.example.ui.screens.RemindersScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.screens.WeeklyAnalyticsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize RevenueCat & Google AdMob ad integration
        RevenueCatAdManager.initialize(this)

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                WaterTrackerApp()
            }
        }
    }
}

@Composable
fun WaterTrackerApp(viewModel: WaterViewModel = viewModel()) {
    val context = LocalContext.current
    val activity = context as? Activity

    val todayState by viewModel.todayUiState.collectAsStateWithLifecycle()
    val analyticsState by viewModel.weeklyAnalytics.collectAsStateWithLifecycle()
    val config by viewModel.config.collectAsStateWithLifecycle()

    val activeInAppAd by RevenueCatAdManager.activeInAppAd.collectAsStateWithLifecycle()
    val isProUser by RevenueCatAdManager.isProUser.collectAsStateWithLifecycle()
    val adCount by RevenueCatAdManager.adCount.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showPaywallDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.navigationBars,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Default.WaterDrop else Icons.Outlined.WaterDrop,
                            contentDescription = "Today",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Today") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_today")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Default.BarChart else Icons.Outlined.BarChart,
                            contentDescription = "Analytics",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Analytics") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_analytics")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Default.Notifications else Icons.Outlined.Notifications,
                            contentDescription = "Reminders",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Reminders") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_reminders")
                )
            }
        },
        floatingActionButton = {
            if (selectedTab != 2) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("fab_log_water")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Log Water Intake"
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = selectedTab, label = "screen_crossfade") { tab ->
                when (tab) {
                    0 -> TodayScreen(
                        todayState = todayState,
                        streakDays = analyticsState.currentStreakDays,
                        onAddWater = { amount, type, note ->
                            viewModel.addWater(amount, type, note)
                            activity?.let { act ->
                                RevenueCatAdManager.showAdOnAction(act, "Logged $amount ml of $type")
                            }
                        },
                        onDeleteLog = { id ->
                            viewModel.deleteLog(id)
                            activity?.let { act ->
                                RevenueCatAdManager.showAdOnAction(act, "Deleted water log")
                            }
                        }
                    )
                    1 -> WeeklyAnalyticsScreen(
                        analyticsState = analyticsState,
                        dailyGoalMl = config.dailyGoalMl,
                        onPreviousWeek = { viewModel.changeWeekOffset(-1) },
                        onNextWeek = { viewModel.changeWeekOffset(1) },
                        onResetToCurrentWeek = { viewModel.resetToCurrentWeek() },
                        onSelectDay = { dateStr -> viewModel.selectAnalyticsDay(dateStr) }
                    )
                    2 -> RemindersScreen(
                        config = config,
                        onSaveReminderSettings = { enabled, interval, startH, startM, endH, endM, vib, snd ->
                            viewModel.updateReminderSettings(
                                enabled = enabled,
                                intervalMinutes = interval,
                                startHour = startH,
                                startMinute = startM,
                                endHour = endH,
                                endMinute = endM,
                                vibrate = vib,
                                sound = snd
                            )
                            activity?.let { act ->
                                RevenueCatAdManager.showAdOnAction(act, "Saved reminder schedule")
                            }
                        },
                        onUpdateDailyGoal = { newGoal ->
                            viewModel.updateDailyGoal(newGoal)
                            activity?.let { act ->
                                RevenueCatAdManager.showAdOnAction(act, "Updated daily intake goal")
                            }
                        },
                        onTriggerTestReminder = {
                            viewModel.triggerTestReminder()
                        },
                        isProUser = isProUser,
                        adCount = adCount,
                        onOpenPaywall = { showPaywallDialog = true },
                        onTestTriggerAd = {
                            activity?.let { act ->
                                RevenueCatAdManager.showAdOnAction(act, "Manual Test Ad Trigger")
                            }
                        }
                    )
                }
            }

            // Quick Add Dialog
            if (showAddDialog) {
                AddWaterDialog(
                    initialAmount = 250,
                    onDismiss = { showAddDialog = false },
                    onConfirm = { amount, type, note ->
                        viewModel.addWater(amount, type, note)
                        showAddDialog = false
                        activity?.let { act ->
                            RevenueCatAdManager.showAdOnAction(act, "Logged $amount ml of $type")
                        }
                    }
                )
            }

            // In-App Ad Display Overlay (Triggered on action completion)
            activeInAppAd?.let { ad ->
                InAppAdDialog(
                    ad = ad,
                    onDismiss = {
                        RevenueCatAdManager.dismissCurrentInAppAd()
                    },
                    onRemoveAdsClick = {
                        RevenueCatAdManager.dismissCurrentInAppAd()
                        showPaywallDialog = true
                    }
                )
            }

            // RevenueCat Subscriptions / Paywall Dialog
            if (showPaywallDialog) {
                RevenueCatPaywallDialog(
                    isProUser = isProUser,
                    onTogglePro = {
                        RevenueCatAdManager.toggleProStatus()
                    },
                    onDismiss = {
                        showPaywallDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Water Tracker $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("Preview") }
}
