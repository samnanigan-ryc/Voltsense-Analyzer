package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EquipmentCategory(val displayName: String, val defaultWatts: Double, val benchmarkWatts: Double, val avgReplacementCost: Double) {
    AIR_CONDITIONER("Air Conditioner", 1600.0, 950.0, 650.0),
    ELECTRIC_FAN("Electric Fan", 75.0, 40.0, 45.0),
    COMPUTER("Computer / PC", 350.0, 120.0, 750.0),
    REFRIGERATOR("Refrigerator", 280.0, 110.0, 600.0),
    SMART_TV("Smart TV", 160.0, 65.0, 400.0),
    WATER_HEATER("Water Heater", 2500.0, 1400.0, 300.0),
    WASHING_MACHINE("Washing Machine", 650.0, 350.0, 500.0),
    MICROWAVE("Microwave Oven", 1200.0, 800.0, 120.0),
    SPACE_HEATER("Space Heater", 1800.0, 1000.0, 90.0),
    OTHER("Other Device", 150.0, 80.0, 100.0)
}

enum class ReplacementVerdict(val title: String, val subtitle: String, val shouldReplace: Boolean) {
    EFFICIENT_KEEP(
        "Highly Energy Efficient",
        "This device supports eco-friendly operation. No replacement needed.",
        false
    ),
    MODERATE_MAINTAIN(
        "Moderately Efficient",
        "Operating within acceptable limits. Regular cleaning/maintenance recommended.",
        false
    ),
    INEFFICIENT_CONSIDER_REPLACEMENT(
        "Inefficient - Replacement Advised",
        "Consuming excessive electricity. Upgrading to an inverter/eco model saves significant costs.",
        true
    ),
    CRITICAL_REPLACE_NOW(
        "Critical Power Drain - Replace to Save Costs",
        "Severely outdated or degraded. Replacing this equipment will quickly pay for itself in bill savings.",
        true
    )
}

@Entity(tableName = "equipment")
data class Equipment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: EquipmentCategory,
    val roomLocation: String = "Living Room",
    val powerWatts: Double,
    val hoursPerDay: Double,
    val ageYears: Double,
    val brandModel: String = "",
    val starRating: Int = 3, // 1 to 5 stars
    val isInverterOrEco: Boolean = false,
    val replacementCostEstimate: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    
    // Computed Analysis Fields
    val dailyKwh: Double = 0.0,
    val monthlyKwh: Double = 0.0,
    val annualKwh: Double = 0.0,
    val monthlyCost: Double = 0.0,
    val annualCost: Double = 0.0,
    val efficiencyScore: Int = 100, // 0 - 100%
    val efficiencyGrade: String = "A", // A+, A, B, C, D, F
    val benchmarkWatts: Double = 0.0,
    val annualEnergyWasteKwh: Double = 0.0,
    val annualCostSavingsIfReplaced: Double = 0.0,
    val paybackMonths: Double = 0.0,
    val verdict: ReplacementVerdict = ReplacementVerdict.EFFICIENT_KEEP,
    val recommendations: String = "",
    val isHighConsumptionAlert: Boolean = false
)

@Entity(tableName = "monthly_logs")
data class MonthlyLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val monthName: String, // e.g. "Jan", "Feb", "Mar"
    val year: Int,
    val monthIndex: Int, // 1 - 12
    val totalKwh: Double,
    val totalCost: Double,
    val peakDeviceName: String = "",
    val peakDeviceKwh: Double = 0.0,
    val efficiencyIndex: Int = 85 // Average efficiency %
)

@Entity(tableName = "alert_notifications")
data class AlertNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val equipmentId: Long? = null,
    val equipmentName: String? = null,
    val excessKwh: Double = 0.0,
    val excessCost: Double = 0.0,
    val severity: String = "WARNING", // "INFO", "WARNING", "CRITICAL"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class EnergyAuditSummary(
    val totalDevices: Int,
    val totalMonthlyKwh: Double,
    val totalMonthlyCost: Double,
    val totalAnnualCost: Double,
    val averageEfficiencyScore: Int,
    val potentialAnnualSavings: Double,
    val highConsumptionDevicesCount: Int,
    val replaceRecommendedCount: Int,
    val topConsumerEquipment: Equipment? = null,
    val mostInefficientEquipment: Equipment? = null
)
