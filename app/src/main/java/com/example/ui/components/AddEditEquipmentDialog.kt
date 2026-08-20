package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.engine.EnergyCalculationEngine
import com.example.data.model.Equipment
import com.example.data.model.EquipmentCategory
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.PastelPurpleBorder
import com.example.ui.theme.PastelPurpleContainer
import com.example.ui.theme.PastelPurpleLight
import com.example.ui.theme.PastelPurplePrimary
import com.example.ui.theme.PastelPurpleSoft
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditEquipmentDialog(
    equipmentToEdit: Equipment?,
    currencySymbol: String,
    costPerKwh: Double,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        category: EquipmentCategory,
        roomLocation: String,
        powerWatts: Double,
        hoursPerDay: Double,
        ageYears: Double,
        brandModel: String,
        starRating: Int,
        isInverterOrEco: Boolean,
        replacementCostEstimate: Double?,
        notes: String,
        id: Long
    ) -> Unit
) {
    val isEditing = equipmentToEdit != null

    var selectedCategory by remember { mutableStateOf(equipmentToEdit?.category ?: EquipmentCategory.AIR_CONDITIONER) }
    var name by remember { mutableStateOf(equipmentToEdit?.name ?: "") }
    var roomLocation by remember { mutableStateOf(equipmentToEdit?.roomLocation ?: "Living Room") }
    var powerWattsStr by remember { mutableStateOf(equipmentToEdit?.powerWatts?.toInt()?.toString() ?: selectedCategory.defaultWatts.toInt().toString()) }
    var hoursPerDay by remember { mutableDoubleStateOf(equipmentToEdit?.hoursPerDay ?: 6.0) }
    var ageYearsStr by remember { mutableStateOf(equipmentToEdit?.ageYears?.toString() ?: "2.0") }
    var brandModel by remember { mutableStateOf(equipmentToEdit?.brandModel ?: "") }
    var starRating by remember { mutableIntStateOf(equipmentToEdit?.starRating ?: 4) }
    var isInverterOrEco by remember { mutableStateOf(equipmentToEdit?.isInverterOrEco ?: false) }
    var replacementCostStr by remember { mutableStateOf(equipmentToEdit?.replacementCostEstimate?.toInt()?.toString() ?: selectedCategory.avgReplacementCost.toInt().toString()) }
    var notes by remember { mutableStateOf(equipmentToEdit?.notes ?: "") }

    // Live preview analysis calculation
    val currentWatts = powerWattsStr.toDoubleOrNull() ?: selectedCategory.defaultWatts
    val currentAge = ageYearsStr.toDoubleOrNull() ?: 0.0
    val currentReplacementCost = replacementCostStr.toDoubleOrNull()

    val previewAnalysis = remember(
        selectedCategory,
        currentWatts,
        hoursPerDay,
        currentAge,
        starRating,
        isInverterOrEco,
        costPerKwh
    ) {
        EnergyCalculationEngine.analyzeEquipment(
            name = name.ifBlank { selectedCategory.displayName },
            category = selectedCategory,
            roomLocation = roomLocation,
            powerWatts = currentWatts,
            hoursPerDay = hoursPerDay,
            ageYears = currentAge,
            brandModel = brandModel,
            starRating = starRating,
            isInverterOrEco = isInverterOrEco,
            replacementCostEstimate = currentReplacementCost,
            notes = notes,
            ratePerKwh = costPerKwh
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(680.dp)
                .testTag("add_edit_equipment_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isEditing) "Edit Equipment" else "Add Electronic Device",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Analyze energy efficiency & monthly costs",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Form Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Category Selection Chips
                    Text(
                        text = "Appliance Type",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        EquipmentCategory.values().forEach { cat ->
                            val isSelected = cat == selectedCategory
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) PastelPurplePrimary else PastelPurpleContainer,
                                modifier = Modifier
                                    .clickable {
                                        selectedCategory = cat
                                        if (!isEditing && name.isBlank()) {
                                            name = cat.displayName
                                        }
                                        powerWattsStr = cat.defaultWatts.toInt().toString()
                                        replacementCostStr = cat.avgReplacementCost.toInt().toString()
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(cat),
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else PastelPurplePrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = cat.displayName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) Color.White else TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Equipment Name & Location
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Equipment Name (e.g. Living Room AC)") },
                        placeholder = { Text(selectedCategory.displayName) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("equipment_name_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PastelPurplePrimary,
                            unfocusedBorderColor = PastelPurpleBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = roomLocation,
                            onValueChange = { roomLocation = it },
                            label = { Text("Room / Location") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PastelPurplePrimary,
                                unfocusedBorderColor = PastelPurpleBorder
                            )
                        )

                        OutlinedTextField(
                            value = brandModel,
                            onValueChange = { brandModel = it },
                            label = { Text("Brand / Model (Opt)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PastelPurplePrimary,
                                unfocusedBorderColor = PastelPurpleBorder
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Power Consumption (Watts) & Daily Hours
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = powerWattsStr,
                            onValueChange = { powerWattsStr = it },
                            label = { Text("Power (Watts)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("power_watts_input"),
                            trailingIcon = { Text("W", modifier = Modifier.padding(end = 12.dp), color = TextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PastelPurplePrimary,
                                unfocusedBorderColor = PastelPurpleBorder
                            )
                        )

                        OutlinedTextField(
                            value = ageYearsStr,
                            onValueChange = { ageYearsStr = it },
                            label = { Text("Age (Years)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            trailingIcon = { Text("yrs", modifier = Modifier.padding(end = 12.dp), color = TextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PastelPurplePrimary,
                                unfocusedBorderColor = PastelPurpleBorder
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Daily Usage Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daily Usage Hours",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Text(
                                text = "${(hoursPerDay * 10).roundToInt() / 10.0} hrs/day",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = PastelPurplePrimary
                            )
                        }

                        Slider(
                            value = hoursPerDay.toFloat(),
                            onValueChange = { hoursPerDay = it.toDouble() },
                            valueRange = 0.5f..24f,
                            steps = 46,
                            colors = SliderDefaults.colors(
                                thumbColor = PastelPurplePrimary,
                                activeTrackColor = PastelPurplePrimary,
                                inactiveTrackColor = PastelPurpleSoft
                            ),
                            modifier = Modifier.testTag("daily_hours_slider")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Inverter & Eco Mode Toggle
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = PastelPurpleContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Inverter / Energy Star Certified",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Modern variable-speed compressor or high-efficiency rating",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Switch(
                                checked = isInverterOrEco,
                                onCheckedChange = { isInverterOrEco = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PastelPurplePrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Star Rating (1 - 5)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Energy Star Rating",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Row {
                            (1..5).forEach { star ->
                                IconButton(
                                    onClick = { starRating = star },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "$star Stars",
                                        tint = if (star <= starRating) Color(0xFFFFB300) else Color(0xFFE0E0E0),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Analysis Calculation Preview Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PastelPurpleBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ElectricBolt,
                                        contentDescription = null,
                                        tint = PastelPurplePrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Live Calculation Preview",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                }
                                Text(
                                    text = "Grade ${previewAnalysis.efficiencyGrade} (${previewAnalysis.efficiencyScore}%)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = PastelPurplePrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Monthly Power", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text(
                                        "${previewAnalysis.monthlyKwh} kWh",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                }
                                Column {
                                    Text("Monthly Bill", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text(
                                        "$currencySymbol${previewAnalysis.monthlyCost}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Verdict", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text(
                                        if (previewAnalysis.verdict.shouldReplace) "Replace Advised" else "Keep Using",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (previewAnalysis.verdict.shouldReplace) Color(0xFFE53935) else EcoGreen
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            val finalWatts = powerWattsStr.toDoubleOrNull() ?: selectedCategory.defaultWatts
                            val finalHours = hoursPerDay
                            val finalAge = ageYearsStr.toDoubleOrNull() ?: 0.0
                            val finalRepCost = replacementCostStr.toDoubleOrNull() ?: selectedCategory.avgReplacementCost

                            onSave(
                                name.ifBlank { selectedCategory.displayName },
                                selectedCategory,
                                roomLocation.ifBlank { "Living Room" },
                                finalWatts,
                                finalHours,
                                finalAge,
                                brandModel,
                                starRating,
                                isInverterOrEco,
                                finalRepCost,
                                notes,
                                equipmentToEdit?.id ?: 0L
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PastelPurplePrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_equipment_button")
                    ) {
                        Text(
                            text = if (isEditing) "Update Equipment" else "Analyze & Save",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
