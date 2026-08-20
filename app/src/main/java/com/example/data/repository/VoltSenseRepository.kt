package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.engine.EnergyCalculationEngine
import com.example.data.local.EquipmentDao
import com.example.data.model.AlertNotification
import com.example.data.model.EnergyAuditSummary
import com.example.data.model.Equipment
import com.example.data.model.EquipmentCategory
import com.example.data.model.MonthlyLog
import com.example.data.model.ReplacementVerdict
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class UserSettings(
    val costPerKwh: Double = 0.16,
    val currencySymbol: String = "$",
    val highConsumptionThresholdKwh: Double = 120.0,
    val notificationsEnabled: Boolean = true
)

class VoltSenseRepository(
    private val equipmentDao: EquipmentDao,
    context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("voltsense_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    val allEquipment: Flow<List<Equipment>> = equipmentDao.getAllEquipment()
    val allMonthlyLogs: Flow<List<MonthlyLog>> = equipmentDao.getAllMonthlyLogs()
    val allAlerts: Flow<List<AlertNotification>> = equipmentDao.getAllAlerts()
    val unreadAlertCount: Flow<Int> = equipmentDao.getUnreadAlertCount()

    val auditSummary: Flow<EnergyAuditSummary> = allEquipment.map { list ->
        if (list.isEmpty()) {
            EnergyAuditSummary(
                totalDevices = 0,
                totalMonthlyKwh = 0.0,
                totalMonthlyCost = 0.0,
                totalAnnualCost = 0.0,
                averageEfficiencyScore = 0,
                potentialAnnualSavings = 0.0,
                highConsumptionDevicesCount = 0,
                replaceRecommendedCount = 0
            )
        } else {
            val totalMonthlyKwh = list.sumOf { it.monthlyKwh }
            val totalMonthlyCost = list.sumOf { it.monthlyCost }
            val totalAnnualCost = list.sumOf { it.annualCost }
            val avgEfficiency = (list.sumOf { it.efficiencyScore } / list.size.toDouble()).roundToInt()
            val potentialSavings = list.sumOf { it.annualCostSavingsIfReplaced }
            val highConsumers = list.count { it.isHighConsumptionAlert }
            val replaceCount = list.count { it.verdict.shouldReplace }
            val topConsumer = list.maxByOrNull { it.monthlyKwh }
            val worstEfficiency = list.minByOrNull { it.efficiencyScore }

            EnergyAuditSummary(
                totalDevices = list.size,
                totalMonthlyKwh = (totalMonthlyKwh * 10).roundToInt() / 10.0,
                totalMonthlyCost = (totalMonthlyCost * 100).roundToInt() / 100.0,
                totalAnnualCost = (totalAnnualCost * 100).roundToInt() / 100.0,
                averageEfficiencyScore = avgEfficiency,
                potentialAnnualSavings = (potentialSavings * 100).roundToInt() / 100.0,
                highConsumptionDevicesCount = highConsumers,
                replaceRecommendedCount = replaceCount,
                topConsumerEquipment = topConsumer,
                mostInefficientEquipment = worstEfficiency
            )
        }
    }

    private fun loadSettings(): UserSettings {
        return UserSettings(
            costPerKwh = prefs.getFloat("cost_per_kwh", 0.16f).toDouble(),
            currencySymbol = prefs.getString("currency_symbol", "$") ?: "$",
            highConsumptionThresholdKwh = prefs.getFloat("high_kwh_threshold", 120.0f).toDouble(),
            notificationsEnabled = prefs.getBoolean("notif_enabled", true)
        )
    }

    fun updateSettings(newSettings: UserSettings, scope: CoroutineScope) {
        prefs.edit()
            .putFloat("cost_per_kwh", newSettings.costPerKwh.toFloat())
            .putString("currency_symbol", newSettings.currencySymbol)
            .putFloat("high_kwh_threshold", newSettings.highConsumptionThresholdKwh.toFloat())
            .putBoolean("notif_enabled", newSettings.notificationsEnabled)
            .apply()
        _settings.value = newSettings

        // Recalculate all equipment with new rate & threshold
        scope.launch(Dispatchers.IO) {
            val items = equipmentDao.getAllEquipment().first()
            items.forEach { item ->
                val recalculated = EnergyCalculationEngine.analyzeEquipment(
                    name = item.name,
                    category = item.category,
                    roomLocation = item.roomLocation,
                    powerWatts = item.powerWatts,
                    hoursPerDay = item.hoursPerDay,
                    ageYears = item.ageYears,
                    brandModel = item.brandModel,
                    starRating = item.starRating,
                    isInverterOrEco = item.isInverterOrEco,
                    replacementCostEstimate = item.replacementCostEstimate,
                    notes = item.notes,
                    ratePerKwh = newSettings.costPerKwh,
                    highConsumptionThresholdKwh = newSettings.highConsumptionThresholdKwh,
                    id = item.id
                )
                equipmentDao.updateEquipment(recalculated)
            }
            refreshMonthlyLogsInternal(newSettings.costPerKwh)
        }
    }

    suspend fun saveEquipment(
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
        id: Long = 0
    ): Long {
        val currentSettings = _settings.value
        val analyzed = EnergyCalculationEngine.analyzeEquipment(
            name = name,
            category = category,
            roomLocation = roomLocation,
            powerWatts = powerWatts,
            hoursPerDay = hoursPerDay,
            ageYears = ageYears,
            brandModel = brandModel,
            starRating = starRating,
            isInverterOrEco = isInverterOrEco,
            replacementCostEstimate = replacementCostEstimate,
            notes = notes,
            ratePerKwh = currentSettings.costPerKwh,
            highConsumptionThresholdKwh = currentSettings.highConsumptionThresholdKwh,
            id = id
        )

        val insertedId = if (id == 0L) {
            equipmentDao.insertEquipment(analyzed)
        } else {
            equipmentDao.updateEquipment(analyzed)
            id
        }

        // Check if high consumption alert needs to be generated
        if (analyzed.isHighConsumptionAlert && currentSettings.notificationsEnabled) {
            val alert = AlertNotification(
                title = "High Energy Consumption: ${analyzed.name}",
                message = "${analyzed.name} is consuming ${analyzed.monthlyKwh} kWh/month (${currentSettings.currencySymbol}${analyzed.monthlyCost}/mo), which exceeds the high usage threshold of ${currentSettings.highConsumptionThresholdKwh} kWh.",
                equipmentId = insertedId,
                equipmentName = analyzed.name,
                excessKwh = analyzed.monthlyKwh,
                excessCost = analyzed.monthlyCost,
                severity = if (analyzed.verdict == ReplacementVerdict.CRITICAL_REPLACE_NOW) "CRITICAL" else "WARNING"
            )
            equipmentDao.insertAlert(alert)
        }

        refreshMonthlyLogsInternal(currentSettings.costPerKwh)
        return insertedId
    }

    suspend fun deleteEquipment(equipment: Equipment) {
        equipmentDao.deleteEquipment(equipment)
        refreshMonthlyLogsInternal(_settings.value.costPerKwh)
    }

    suspend fun markAlertAsRead(alertId: Long) {
        equipmentDao.markAlertAsRead(alertId)
    }

    suspend fun markAllAlertsAsRead() {
        equipmentDao.markAllAlertsAsRead()
    }

    suspend fun triggerSampleHighConsumptionAnomaly() {
        val currentSettings = _settings.value
        val alert = AlertNotification(
            title = "Unusual Surge: Living Room Old AC",
            message = "Power consumption spiked to 380 kWh this cycle (+42% vs last month). Air filters may be clogged or compressor duty cycle is stuck at 100%. Consider inspecting or replacing with an inverter unit.",
            equipmentName = "Living Room Old AC",
            excessKwh = 380.0,
            excessCost = 380.0 * currentSettings.costPerKwh,
            severity = "CRITICAL"
        )
        equipmentDao.insertAlert(alert)
    }

    private suspend fun refreshMonthlyLogsInternal(ratePerKwh: Double) {
        val currentEquipment = equipmentDao.getAllEquipment().first()
        val totalMonthlyKwh = currentEquipment.sumOf { it.monthlyKwh }
        val peakDevice = currentEquipment.maxByOrNull { it.monthlyKwh }

        // Generate 6-month historical line graph data with realistic seasonal variation
        val months = listOf(
            Triple("Mar", 3, 0.88),
            Triple("Apr", 4, 0.94),
            Triple("May", 5, 1.12), // hotter month peak
            Triple("Jun", 6, 1.25), // peak AC season
            Triple("Jul", 7, 1.18),
            Triple("Aug", 8, 1.00)  // current month baseline
        )

        val logs = months.map { (name, index, multiplier) ->
            val monthKwh = (totalMonthlyKwh * multiplier).let { (it * 10).roundToInt() / 10.0 }
            val monthCost = (monthKwh * ratePerKwh).let { (it * 100).roundToInt() / 100.0 }
            val peakKwh = ((peakDevice?.monthlyKwh ?: 0.0) * multiplier).let { (it * 10).roundToInt() / 10.0 }
            MonthlyLog(
                monthName = name,
                year = 2026,
                monthIndex = index,
                totalKwh = if (totalMonthlyKwh == 0.0) 0.0 else monthKwh,
                totalCost = if (totalMonthlyKwh == 0.0) 0.0 else monthCost,
                peakDeviceName = peakDevice?.name ?: "N/A",
                peakDeviceKwh = peakKwh,
                efficiencyIndex = if (index >= 5) 72 else 79
            )
        }

        equipmentDao.deleteAllMonthlyLogs()
        equipmentDao.insertMonthlyLogs(logs)
    }

    suspend fun seedInitialDataIfEmpty() {
        val existing = equipmentDao.getAllEquipment().first()
        if (existing.isNotEmpty()) return

        val rate = _settings.value.costPerKwh
        val threshold = _settings.value.highConsumptionThresholdKwh

        // Preload realistic variety of appliances (AC, Fan, PC, Refrigerator, Smart TV, Water Heater)
        val initialDevices = listOf(
            EnergyCalculationEngine.analyzeEquipment(
                name = "Living Room Old AC",
                category = EquipmentCategory.AIR_CONDITIONER,
                roomLocation = "Living Room",
                powerWatts = 2200.0,
                hoursPerDay = 8.0,
                ageYears = 8.5,
                brandModel = "CoolMaster 2.0 HP (Non-Inverter)",
                starRating = 2,
                isInverterOrEco = false,
                replacementCostEstimate = 650.0,
                notes = "Old unit, runs loud and compressor cycles constantly.",
                ratePerKwh = rate,
                highConsumptionThresholdKwh = threshold
            ),
            EnergyCalculationEngine.analyzeEquipment(
                name = "Master Bedroom AC",
                category = EquipmentCategory.AIR_CONDITIONER,
                roomLocation = "Master Bedroom",
                powerWatts = 850.0,
                hoursPerDay = 7.0,
                ageYears = 1.2,
                brandModel = "EcoInverter DualCool 1.0 HP",
                starRating = 5,
                isInverterOrEco = true,
                replacementCostEstimate = 500.0,
                notes = "High-efficiency modern inverter unit.",
                ratePerKwh = rate,
                highConsumptionThresholdKwh = threshold
            ),
            EnergyCalculationEngine.analyzeEquipment(
                name = "Desktop Gaming PC & Monitor",
                category = EquipmentCategory.COMPUTER,
                roomLocation = "Home Office",
                powerWatts = 420.0,
                hoursPerDay = 6.0,
                ageYears = 3.0,
                brandModel = "Custom Rig (RTX 3080 + 144Hz Monitor)",
                starRating = 3,
                isInverterOrEco = false,
                replacementCostEstimate = 800.0,
                notes = "High performance workstation and gaming rig.",
                ratePerKwh = rate,
                highConsumptionThresholdKwh = threshold
            ),
            EnergyCalculationEngine.analyzeEquipment(
                name = "Kitchen Refrigerator",
                category = EquipmentCategory.REFRIGERATOR,
                roomLocation = "Kitchen",
                powerWatts = 180.0,
                hoursPerDay = 24.0, // 24h duty cycle
                ageYears = 4.0,
                brandModel = "FrostFree Multi-Door 350L",
                starRating = 4,
                isInverterOrEco = true,
                replacementCostEstimate = 600.0,
                notes = "Smart inverter compressor running smoothly.",
                ratePerKwh = rate,
                highConsumptionThresholdKwh = threshold
            ),
            EnergyCalculationEngine.analyzeEquipment(
                name = "Bedroom Electric Fan",
                category = EquipmentCategory.ELECTRIC_FAN,
                roomLocation = "Bedroom 2",
                powerWatts = 65.0,
                hoursPerDay = 10.0,
                ageYears = 2.0,
                brandModel = "AirFlow 16-inch Stand Fan",
                starRating = 4,
                isInverterOrEco = false,
                replacementCostEstimate = 40.0,
                notes = "Good airflow, low daily energy cost.",
                ratePerKwh = rate,
                highConsumptionThresholdKwh = threshold
            ),
            EnergyCalculationEngine.analyzeEquipment(
                name = "Living Room 65\" Smart TV",
                category = EquipmentCategory.SMART_TV,
                roomLocation = "Living Room",
                powerWatts = 140.0,
                hoursPerDay = 4.5,
                ageYears = 2.5,
                brandModel = "4K OLED Smart Display",
                starRating = 4,
                isInverterOrEco = true,
                replacementCostEstimate = 550.0,
                notes = "OLED with eco ambient light sensor enabled.",
                ratePerKwh = rate,
                highConsumptionThresholdKwh = threshold
            )
        )

        initialDevices.forEach { equipmentDao.insertEquipment(it) }

        // Seed initial high-consumption notification for the inefficient Living Room AC
        val alert = AlertNotification(
            title = "High Energy Alert: Living Room Old AC",
            message = "Living Room Old AC consumes 528.0 kWh/mo ($${(528.0 * rate).roundToInt()}/mo). Replacing with a modern inverter AC will save ~$${((528.0 - 228.0) * rate * 12).roundToInt()}/year with a 15-month payback!",
            equipmentName = "Living Room Old AC",
            excessKwh = 528.0,
            excessCost = 528.0 * rate,
            severity = "CRITICAL"
        )
        equipmentDao.insertAlert(alert)

        refreshMonthlyLogsInternal(rate)
    }

    suspend fun resetAllData() {
        equipmentDao.deleteAllEquipment()
        equipmentDao.deleteAllMonthlyLogs()
        equipmentDao.deleteAllAlerts()
        seedInitialDataIfEmpty()
    }
}
