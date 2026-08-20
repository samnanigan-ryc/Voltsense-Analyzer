package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Equipment
import com.example.ui.components.EfficiencyGauge
import com.example.ui.components.getCategoryIcon
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedSoft
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.EcoGreenSoft
import com.example.ui.theme.PastelPurpleBorder
import com.example.ui.theme.PastelPurpleContainer
import com.example.ui.theme.PastelPurpleLight
import com.example.ui.theme.PastelPurplePrimary
import com.example.ui.theme.PastelPurpleSoft
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun ComparisonScreen(
    equipmentList: List<Equipment>,
    selectedDeviceA: Equipment?,
    selectedDeviceB: Equipment?,
    currencySymbol: String,
    onSelectDeviceA: (Equipment) -> Unit,
    onSelectDeviceB: (Equipment) -> Unit,
    modifier: Modifier = Modifier
) {
    var menuAExpanded by remember { mutableStateOf(false) }
    var menuBExpanded by remember { mutableStateOf(false) }

    // Auto default to 1st and 2nd items if not selected
    val devA = selectedDeviceA ?: equipmentList.getOrNull(0)
    val devB = selectedDeviceB ?: equipmentList.getOrNull(1) ?: equipmentList.getOrNull(0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 96.dp)
            .testTag("comparison_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(PastelPurpleContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CompareArrows,
                    contentDescription = null,
                    tint = PastelPurplePrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Appliance Comparison",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = "Head-to-head energy efficiency & cost audit",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Device Pickers Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Device A Selector Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { menuAExpanded = true }
                    .testTag("device_a_selector"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PastelPurpleBorder))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Device A", style = MaterialTheme.typography.labelSmall, color = PastelPurplePrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = devA?.name ?: "Select Device",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                    }

                    DropdownMenu(
                        expanded = menuAExpanded,
                        onDismissRequest = { menuAExpanded = false }
                    ) {
                        equipmentList.forEach { eq ->
                            DropdownMenuItem(
                                text = { Text("${eq.name} (${eq.powerWatts.toInt()}W)") },
                                onClick = {
                                    onSelectDeviceA(eq)
                                    menuAExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Device B Selector Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { menuBExpanded = true }
                    .testTag("device_b_selector"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PastelPurpleBorder))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Device B", style = MaterialTheme.typography.labelSmall, color = PastelPurplePrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = devB?.name ?: "Select Device",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                    }

                    DropdownMenu(
                        expanded = menuBExpanded,
                        onDismissRequest = { menuBExpanded = false }
                    ) {
                        equipmentList.forEach { eq ->
                            DropdownMenuItem(
                                text = { Text("${eq.name} (${eq.powerWatts.toInt()}W)") },
                                onClick = {
                                    onSelectDeviceB(eq)
                                    menuBExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (devA == null || devB == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Add at least 2 electronic devices to compare.", color = TextMuted)
            }
        } else {
            // Difference Summary Highlight Card
            val kwhDiff = (devA.monthlyKwh - devB.monthlyKwh).let { (it * 10).roundToInt() / 10.0 }
            val costDiff = (devA.monthlyCost - devB.monthlyCost).let { (it * 100).roundToInt() / 100.0 }
            val effDiff = devA.efficiencyScore - devB.efficiencyScore

            val moreEfficientName = if (devA.efficiencyScore >= devB.efficiencyScore) devA.name else devB.name
            val cheaperName = if (devA.monthlyCost <= devB.monthlyCost) devA.name else devB.name

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = PastelPurpleContainer)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Savings, contentDescription = null, tint = PastelPurplePrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Comparison Insight",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• Efficiency Leader: $moreEfficientName (by ${abs(effDiff)}% efficiency rating)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    Text(
                        text = "• Cost Variance: $cheaperName costs $currencySymbol${abs(costDiff)}/mo less ($currencySymbol${(abs(costDiff) * 12).roundToInt()}/yr)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = EcoGreen,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Side-by-Side Comparison Metrics Table
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(PastelPurpleBorder, PastelPurpleSoft)))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Header Row with Device Names
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Metric",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextMuted
                        )
                        Text(
                            devA.name,
                            modifier = Modifier.weight(1.2f),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PastelPurplePrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            devB.name,
                            modifier = Modifier.weight(1.2f),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PastelPurpleLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ComparisonRow(
                        label = "Power Draw",
                        valA = "${devA.powerWatts.toInt()} W",
                        valB = "${devB.powerWatts.toInt()} W",
                        isABetter = devA.powerWatts <= devB.powerWatts
                    )

                    ComparisonRow(
                        label = "Daily Hours",
                        valA = "${devA.hoursPerDay}h",
                        valB = "${devB.hoursPerDay}h",
                        isABetter = null
                    )

                    ComparisonRow(
                        label = "Monthly Power",
                        valA = "${devA.monthlyKwh} kWh",
                        valB = "${devB.monthlyKwh} kWh",
                        isABetter = devA.monthlyKwh <= devB.monthlyKwh
                    )

                    ComparisonRow(
                        label = "Monthly Bill",
                        valA = "$currencySymbol${devA.monthlyCost}",
                        valB = "$currencySymbol${devB.monthlyCost}",
                        isABetter = devA.monthlyCost <= devB.monthlyCost
                    )

                    ComparisonRow(
                        label = "Efficiency Score",
                        valA = "${devA.efficiencyScore}% (${devA.efficiencyGrade})",
                        valB = "${devB.efficiencyScore}% (${devB.efficiencyGrade})",
                        isABetter = devA.efficiencyScore >= devB.efficiencyScore
                    )

                    ComparisonRow(
                        label = "Age & Tech",
                        valA = "${devA.ageYears}y (${if (devA.isInverterOrEco) "Inverter" else "Standard"})",
                        valB = "${devB.ageYears}y (${if (devB.isInverterOrEco) "Inverter" else "Standard"})",
                        isABetter = null
                    )

                    ComparisonRow(
                        label = "Verdict",
                        valA = if (devA.verdict.shouldReplace) "Replace" else "Efficient",
                        valB = if (devB.verdict.shouldReplace) "Replace" else "Efficient",
                        isABetter = !devA.verdict.shouldReplace
                    )
                }
            }
        }
    }
}

@Composable
private fun ComparisonRow(
    label: String,
    valA: String,
    valB: String,
    isABetter: Boolean?
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = TextSecondary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = valA,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = when (isABetter) {
                        true -> EcoGreen
                        false -> AlertRed
                        else -> TextPrimary
                    }
                ),
                modifier = Modifier.weight(1.2f)
            )
            Text(
                text = valB,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = when (isABetter) {
                        true -> AlertRed
                        false -> EcoGreen
                        else -> TextPrimary
                    }
                ),
                modifier = Modifier.weight(1.2f)
            )
        }
    }
}
