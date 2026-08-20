package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CompareArrows
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.VoltSenseViewModel
import com.example.ui.components.AddEditEquipmentDialog
import com.example.ui.components.EquipmentDetailSheet
import com.example.ui.components.NotificationCenterSheet
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.ComparisonScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EquipmentScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PastelPurpleBorder
import com.example.ui.theme.PastelPurpleContainer
import com.example.ui.theme.PastelPurpleLight
import com.example.ui.theme.PastelPurplePrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Dashboard : Screen(
        route = "dashboard",
        title = "Dashboard",
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard
    )

    object Equipment : Screen(
        route = "equipment",
        title = "Equipment",
        selectedIcon = Icons.Filled.Devices,
        unselectedIcon = Icons.Outlined.Devices
    )

    object Comparison : Screen(
        route = "comparison",
        title = "Compare",
        selectedIcon = Icons.Filled.CompareArrows,
        unselectedIcon = Icons.Outlined.CompareArrows
    )

    object AiHelp : Screen(
        route = "ai_help",
        title = "AI Help",
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome
    )

    object Settings : Screen(
        route = "settings",
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                VoltSenseApp()
            }
        }
    }
}

@Composable
fun VoltSenseApp(
    viewModel: VoltSenseViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

    val allEquipment by viewModel.allEquipment.collectAsState()
    val filteredEquipment by viewModel.filteredEquipment.collectAsState()
    val monthlyLogs by viewModel.allMonthlyLogs.collectAsState()
    val auditSummary by viewModel.auditSummary.collectAsState()
    val alerts by viewModel.allAlerts.collectAsState()
    val unreadCount by viewModel.unreadAlertCount.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()

    val selectedDetailEquipment by viewModel.selectedEquipmentForDetail.collectAsState()
    val showAddEditModal by viewModel.showAddEditModal.collectAsState()
    val equipmentToEdit by viewModel.equipmentToEdit.collectAsState()
    val showNotificationSheet by viewModel.showNotificationSheet.collectAsState()

    val compareA by viewModel.compareDeviceA.collectAsState()
    val compareB by viewModel.compareDeviceB.collectAsState()

    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAiThinking by viewModel.isAiThinking.collectAsState()

    val navItems = listOf(
        Screen.Dashboard,
        Screen.Equipment,
        Screen.Comparison,
        Screen.AiHelp,
        Screen.Settings
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Surface(
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = Color.White,
                shadowElevation = 10.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, PastelPurpleBorder)
            ) {
                NavigationBar(
                    containerColor = Color.White,
                    modifier = Modifier.height(76.dp)
                ) {
                    navItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    letterSpacing = 0.4.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PastelPurplePrimary,
                                selectedTextColor = PastelPurplePrimary,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted,
                                indicatorColor = PastelPurpleContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    equipmentList = allEquipment,
                    monthlyLogs = monthlyLogs,
                    auditSummary = auditSummary,
                    alerts = alerts,
                    unreadAlertsCount = unreadCount,
                    currencySymbol = settings.currencySymbol,
                    costPerKwh = settings.costPerKwh,
                    onEquipmentClick = { eq -> viewModel.selectEquipmentForDetail(eq) },
                    onAddEquipmentClick = { viewModel.openAddEquipmentDialog() },
                    onNotificationClick = { viewModel.setShowNotificationSheet(true) },
                    onNavigateToAi = {
                        navController.navigate(Screen.AiHelp.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToEquipment = {
                        navController.navigate(Screen.Equipment.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Equipment.route) {
                EquipmentScreen(
                    equipmentList = filteredEquipment,
                    searchQuery = searchQuery,
                    selectedCategory = selectedCategory,
                    sortOption = sortOption,
                    currencySymbol = settings.currencySymbol,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onCategorySelect = { viewModel.setSelectedCategory(it) },
                    onSortChange = { viewModel.setSortOption(it) },
                    onEquipmentClick = { eq -> viewModel.selectEquipmentForDetail(eq) },
                    onAddEquipmentClick = { viewModel.openAddEquipmentDialog() }
                )
            }

            composable(Screen.Comparison.route) {
                ComparisonScreen(
                    equipmentList = allEquipment,
                    selectedDeviceA = compareA,
                    selectedDeviceB = compareB,
                    currencySymbol = settings.currencySymbol,
                    onSelectDeviceA = { viewModel.setCompareDeviceA(it) },
                    onSelectDeviceB = { viewModel.setCompareDeviceB(it) }
                )
            }

            composable(Screen.AiHelp.route) {
                AiAssistantScreen(
                    chatMessages = chatMessages,
                    isAiThinking = isAiThinking,
                    onSendMessage = { prompt -> viewModel.sendAiMessage(prompt) }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    currentSettings = settings,
                    onSaveSettings = { cost, curr, thresh, notifs ->
                        viewModel.updateSettings(cost, curr, thresh, notifs)
                    },
                    onTriggerAnomaly = { viewModel.triggerAnomalySimulation() },
                    onResetData = { viewModel.resetData() }
                )
            }
        }

        // Add / Edit Equipment Dialog
        if (showAddEditModal) {
            AddEditEquipmentDialog(
                equipmentToEdit = equipmentToEdit,
                currencySymbol = settings.currencySymbol,
                costPerKwh = settings.costPerKwh,
                onDismiss = { viewModel.closeAddEditDialog() },
                onSave = { name, cat, room, watts, hours, age, brand, stars, inverter, repCost, notes, id ->
                    viewModel.saveEquipment(
                        name = name,
                        category = cat,
                        roomLocation = room,
                        powerWatts = watts,
                        hoursPerDay = hours,
                        ageYears = age,
                        brandModel = brand,
                        starRating = stars,
                        isInverterOrEco = inverter,
                        replacementCostEstimate = repCost,
                        notes = notes,
                        id = id
                    )
                }
            )
        }

        // Equipment Detail & Analysis Sheet
        selectedDetailEquipment?.let { equipment ->
            EquipmentDetailSheet(
                equipment = equipment,
                currencySymbol = settings.currencySymbol,
                costPerKwh = settings.costPerKwh,
                onDismiss = { viewModel.selectEquipmentForDetail(null) },
                onEdit = { eq ->
                    viewModel.openEditEquipmentDialog(eq)
                },
                onDelete = { eq ->
                    viewModel.deleteEquipment(eq)
                },
                onAskAi = { eq ->
                    viewModel.selectEquipmentForDetail(null)
                    val prompt = "Analyze the energy efficiency of my ${eq.name} (${eq.powerWatts.toInt()}W, ${eq.hoursPerDay}h/day, ${eq.monthlyKwh} kWh/mo, Score: ${eq.efficiencyScore}%). Is it worth replacing?"
                    viewModel.sendAiMessage(prompt)
                    navController.navigate(Screen.AiHelp.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // Notification Center Sheet
        if (showNotificationSheet) {
            NotificationCenterSheet(
                alerts = alerts,
                onDismiss = { viewModel.setShowNotificationSheet(false) },
                onAlertClick = { alert ->
                    viewModel.markAlertAsRead(alert.id)
                }
            )
        }
    }
}
