package com.example.data.engine

import com.example.data.model.Equipment
import com.example.data.model.EquipmentCategory
import com.example.data.model.ReplacementVerdict
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object EnergyCalculationEngine {

    /**
     * Analyzes an equipment's energy profile, efficiency rating, ROI calculation,
     * and replacement feedback.
     */
    fun analyzeEquipment(
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
        ratePerKwh: Double,
        highConsumptionThresholdKwh: Double = 120.0,
        id: Long = 0
    ): Equipment {
        val safeWatts = max(1.0, powerWatts)
        val safeHours = max(0.1, min(24.0, hoursPerDay))
        val safeAge = max(0.0, ageYears)
        val safeStars = min(5, max(1, starRating))

        // 1. Consumption Calculations
        val dailyKwh = (safeWatts * safeHours) / 1000.0
        val monthlyKwh = dailyKwh * 30.0
        val annualKwh = dailyKwh * 365.0
        val monthlyCost = monthlyKwh * ratePerKwh
        val annualCost = annualKwh * ratePerKwh

        // 2. Efficiency Benchmark & Calculation
        val benchmarkWatts = category.benchmarkWatts
        val benchmarkDailyKwh = (benchmarkWatts * safeHours) / 1000.0
        val benchmarkAnnualKwh = benchmarkDailyKwh * 365.0

        // Ratio score compared to modern efficient benchmark (100 = matches benchmark, <100 = higher consumption)
        val powerRatioScore = (benchmarkWatts / safeWatts) * 100.0

        // Age Degradation penalty: ~2.5% per year of age
        val ageDegradationPercent = safeAge * 2.5

        // Inverter/Eco technology bonus (+10%) & Star rating weight (+3% per star above 3, -4% per star below 3)
        val inverterBonus = if (isInverterOrEco) 12.0 else 0.0
        val starBonus = (safeStars - 3) * 4.0

        // Compute raw efficiency percentage
        val rawEfficiency = (powerRatioScore - ageDegradationPercent + inverterBonus + starBonus)
        val efficiencyScore = min(100, max(10, rawEfficiency.roundToInt()))

        // Grade mapping
        val efficiencyGrade = when {
            efficiencyScore >= 90 -> "A+"
            efficiencyScore >= 80 -> "A"
            efficiencyScore >= 70 -> "B"
            efficiencyScore >= 55 -> "C"
            efficiencyScore >= 40 -> "D"
            else -> "F"
        }

        // 3. Waste & Replacement Financial Analysis
        val annualWasteKwh = max(0.0, annualKwh - benchmarkAnnualKwh)
        val annualCostSavingsIfReplaced = annualWasteKwh * ratePerKwh
        val monthlySavingsIfReplaced = annualCostSavingsIfReplaced / 12.0

        val finalReplacementCost = if (replacementCostEstimate != null && replacementCostEstimate > 0) {
            replacementCostEstimate
        } else {
            category.avgReplacementCost
        }

        val paybackMonths = if (monthlySavingsIfReplaced > 0.5) {
            (finalReplacementCost / monthlySavingsIfReplaced).let { (it * 10).roundToInt() / 10.0 }
        } else {
            0.0
        }

        // 4. Verdict Decision
        val verdict = when {
            efficiencyScore >= 80 -> ReplacementVerdict.EFFICIENT_KEEP
            efficiencyScore >= 65 -> ReplacementVerdict.MODERATE_MAINTAIN
            efficiencyScore >= 45 -> ReplacementVerdict.INEFFICIENT_CONSIDER_REPLACEMENT
            else -> ReplacementVerdict.CRITICAL_REPLACE_NOW
        }

        // 5. High Consumption Alert Flag
        val isHighConsumptionAlert = monthlyKwh >= highConsumptionThresholdKwh ||
                (verdict == ReplacementVerdict.CRITICAL_REPLACE_NOW && monthlyKwh >= 40.0)

        // 6. Actionable Feedback & Recommendation Text
        val recommendations = generateDetailedRecommendations(
            category = category,
            efficiencyScore = efficiencyScore,
            verdict = verdict,
            ageYears = safeAge,
            annualSavings = annualCostSavingsIfReplaced,
            paybackMonths = paybackMonths,
            isInverter = isInverterOrEco
        )

        return Equipment(
            id = id,
            name = name.ifBlank { category.displayName },
            category = category,
            roomLocation = roomLocation,
            powerWatts = safeWatts,
            hoursPerDay = safeHours,
            ageYears = safeAge,
            brandModel = brandModel,
            starRating = safeStars,
            isInverterOrEco = isInverterOrEco,
            replacementCostEstimate = finalReplacementCost,
            notes = notes,
            dailyKwh = (dailyKwh * 100).roundToInt() / 100.0,
            monthlyKwh = (monthlyKwh * 10).roundToInt() / 10.0,
            annualKwh = (annualKwh * 10).roundToInt() / 10.0,
            monthlyCost = (monthlyCost * 100).roundToInt() / 100.0,
            annualCost = (annualCost * 100).roundToInt() / 100.0,
            efficiencyScore = efficiencyScore,
            efficiencyGrade = efficiencyGrade,
            benchmarkWatts = benchmarkWatts,
            annualEnergyWasteKwh = (annualWasteKwh * 10).roundToInt() / 10.0,
            annualCostSavingsIfReplaced = (annualCostSavingsIfReplaced * 100).roundToInt() / 100.0,
            paybackMonths = paybackMonths,
            verdict = verdict,
            recommendations = recommendations,
            isHighConsumptionAlert = isHighConsumptionAlert
        )
    }

    private fun generateDetailedRecommendations(
        category: EquipmentCategory,
        efficiencyScore: Int,
        verdict: ReplacementVerdict,
        ageYears: Double,
        annualSavings: Double,
        paybackMonths: Double,
        isInverter: Boolean
    ): String {
        val tips = mutableListOf<String>()

        when (category) {
            EquipmentCategory.AIR_CONDITIONER -> {
                if (verdict.shouldReplace) {
                    tips.add("High cooling load detected. Upgrading to an Inverter unit with SEER 18+ can cut electricity use by 40-50%.")
                    if (paybackMonths > 0) tips.add("Estimated payback period: $paybackMonths months via monthly bill reductions.")
                } else {
                    tips.add("Unit is operating effectively. Keep thermostat set to 24°C-25°C (75°F-77°F) for optimal efficiency balance.")
                }
                tips.add("Clean air filters every 2-3 weeks to avoid 15% blower motor power penalty.")
                tips.add("Seal doors and windows to prevent cool air leakage.")
            }
            EquipmentCategory.ELECTRIC_FAN -> {
                if (verdict.shouldReplace) {
                    tips.add("Old fan motor is drawing excess power. Consider upgrading to a DC Brushless motor fan which uses up to 60% less wattage.")
                } else {
                    tips.add("Electric fan is highly cost-effective for localized cooling.")
                }
                tips.add("Regularly remove dust from fan blades and motor housing to minimize drag.")
                tips.add("Pair with air conditioning to circulate air and allow higher AC thermostat settings.")
            }
            EquipmentCategory.COMPUTER -> {
                if (verdict.shouldReplace) {
                    tips.add("High idle/operational power consumption. Modern processors and 80+ Gold/Platinum power supplies deliver 3x better efficiency.")
                } else {
                    tips.add("PC power profile is healthy. Enable OS power-saving sleep mode when idle for 15+ minutes.")
                }
                tips.add("Turn off high-refresh external monitors and per-key RGB when stepping away.")
                tips.add("Clean dust from heatsinks to keep cooling fans running at lower RPM.")
            }
            EquipmentCategory.REFRIGERATOR -> {
                if (verdict.shouldReplace) {
                    tips.add("Older non-inverter compressors run 24/7 with massive thermal losses. A modern inverter fridge will significantly reduce continuous load.")
                } else {
                    tips.add("Compressor performance is solid. Maintain 3°C to 5°C fridge and -18°C freezer temperatures.")
                }
                tips.add("Check door gasket seals with the paper test to prevent cold air leakage.")
                tips.add("Vacuum back condenser coils every 6 months to reduce compressor strain.")
            }
            EquipmentCategory.SMART_TV -> {
                tips.add("Enable ambient light sensor (Auto-brightness) or eco mode in picture settings.")
                tips.add("Turn off 'Quick Start' / instant-on background standby mode when not in use.")
            }
            EquipmentCategory.WATER_HEATER -> {
                tips.add("Lower water heater thermostat to 50°C-55°C (120°F-130°F) to avoid standby heat dissipation.")
                tips.add("Flush sediment annually to ensure direct heat transfer to water.")
            }
            EquipmentCategory.WASHING_MACHINE -> {
                tips.add("Wash laundry in cold water (30°C) where possible — heating water accounts for 85% of washing energy.")
                tips.add("Always wash full loads and use high spin speeds to shorten drying time.")
            }
            EquipmentCategory.MICROWAVE -> {
                tips.add("Keep interior clean for optimal microwave wave reflections and faster cooking.")
            }
            EquipmentCategory.SPACE_HEATER -> {
                tips.add("Use directional radiant heating only for occupied zones rather than heating entire rooms.")
                tips.add("Use a programmable thermostat or timer to prevent running unattended.")
            }
            EquipmentCategory.OTHER -> {
                tips.add("Unplug when not in use or connect to a smart power strip to eliminate phantom standby loads.")
            }
        }

        if (ageYears >= 7.0 && !isInverter) {
            tips.add("Device age is over ${ageYears.toInt()} years; electronic capacitors and insulation efficiency degrade over time.")
        }

        return tips.joinToString("\n• ", prefix = "• ")
    }
}
