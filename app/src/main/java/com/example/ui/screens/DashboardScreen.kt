package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertNotification
import com.example.data.model.EnergyAuditSummary
import com.example.data.model.Equipment
import com.example.data.model.MonthlyLog
import com.example.ui.components.ApplianceCard
import com.example.ui.components.ApplianceDistributionChart
import com.example.ui.components.HighConsumptionAlertBanner
import com.example.ui.components.MonthlyEnergyLineChart
import com.example.ui.theme.AlertRed
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.DeepPurple
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.PastelPurpleBorder
import com.example.ui.theme.PastelPurpleBorderStrong
import com.example.ui.theme.PastelPurpleContainer
import com.example.ui.theme.PastelPurpleLight
import com.example.ui.theme.PastelPurplePrimary
import com.example.ui.theme.PastelPurpleSoft
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TrackingPurple

@Composable
fun DashboardScreen(
    equipmentList: List<Equipment>,
    monthlyLogs: List<MonthlyLog>,
    auditSummary: EnergyAuditSummary,
    alerts: List<AlertNotification>,
    unreadAlertsCount: Int,
    currencySymbol: String,
    costPerKwh: Double,
    onEquipmentClick: (Equipment) -> Unit,
    onAddEquipmentClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToEquipment: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 18.dp, bottom = 96.dp)
                .testTag("dashboard_screen")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    // Tracking subtitle
                    Text(
                        text = "VOLTSENSE ANALYTICS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            fontSize = 10.sp
                        ),
                        color = TrackingPurple,
                        modifier = Modifier.testTag("voltsense_title_header")
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp
                        ),
                        color = TextPrimary,
                        modifier = Modifier.testTag("dashboard_title_header")
                    )
                }

                // Notification Bell with Red Dot Indicator
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, PastelPurpleBorderStrong, CircleShape)
                        .clickable(onClick = onNotificationClick)
                        .testTag("notification_bell_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = DeepPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    if (unreadAlertsCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = (-8).dp, y = 8.dp)
                                .background(AlertRed, CircleShape)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Unusually High Consumption Alert Banner
            val highAlerts = alerts.filter { !it.isRead || it.severity == "CRITICAL" }
            if (highAlerts.isNotEmpty()) {
                HighConsumptionAlertBanner(
                    activeAlerts = highAlerts,
                    unreadCount = unreadAlertsCount,
                    onClick = onNotificationClick
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 1. Line Graph / Monthly Usage Trend Card
            MonthlyEnergyLineChart(
                logs = monthlyLogs,
                currencySymbol = currencySymbol
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 2. AI Intelligence Banner Card (Professional Polish styled banner)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToAi)
                    .testTag("ai_intelligence_banner"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF7C3AED)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF7C3AED), Color(0xFF6D28D9))
                            )
                        )
                        .padding(18.dp)
                ) {
                    // Decorative glow circle
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 24.dp, y = 24.dp)
                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                    )

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "AI INTELLIGENCE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    fontSize = 10.sp
                                ),
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val worstDevice = auditSummary.mostInefficientEquipment
                        val potentialSavings = auditSummary.potentialAnnualSavings
                        val savingsMonthly = if (potentialSavings > 0) (potentialSavings / 12.0).toInt() else 14

                        Text(
                            text = if (worstDevice != null && worstDevice.verdict.shouldReplace) {
                                "Replace or service ${worstDevice.name} to save $currencySymbol$savingsMonthly/mo. High energy drain detected in compressor duty cycle."
                            } else {
                                "Optimize air conditioner thermostat to 24°C and clean filters to reduce total cooling bills by up to $currencySymbol$savingsMonthly/mo."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.5.sp,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Comparison between appliances Breakdown
            ApplianceDistributionChart(
                equipmentList = equipmentList,
                currencySymbol = currencySymbol
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Equipment Analysis Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Equipment Analysis",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = TextPrimary
                )

                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    color = PastelPurplePrimary,
                    modifier = Modifier
                        .clickable(onClick = onNavigateToEquipment)
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Top Appliance Cards
            if (equipmentList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No equipment added yet. Tap + to add devices.", color = TextMuted)
                }
            } else {
                equipmentList.take(3).forEach { equipment ->
                    ApplianceCard(
                        equipment = equipment,
                        currencySymbol = currencySymbol,
                        onClick = { onEquipmentClick(equipment) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onAddEquipmentClick,
            containerColor = PastelPurplePrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
                .testTag("add_equipment_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Equipment")
        }
    }
}
