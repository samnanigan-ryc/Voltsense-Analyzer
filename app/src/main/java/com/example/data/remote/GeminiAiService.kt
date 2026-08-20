package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.EnergyAuditSummary
import com.example.data.model.Equipment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAiService {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun askEnergyAdvisor(
        userQuery: String,
        equipmentList: List<Equipment>,
        auditSummary: EnergyAuditSummary?,
        currencySymbol: String,
        costPerKwh: Double
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        val contextInfo = buildContextPrompt(equipmentList, auditSummary, currencySymbol, costPerKwh)
        val fullPrompt = """
            System Instruction: You are VoltSense AI, an expert electrical energy auditor and smart appliance efficiency consultant. 
            Provide clear, practical, numbered or bulleted advice on energy conservation, appliance replacement vs repair decisions, 
            payback calculations, and electricity bill savings based on the user's specific appliances. Keep your tone encouraging, 
            objective, professional, and easy to understand.
            
            Current Household Energy Context:
            $contextInfo
            
            User Inquiry:
            $userQuery
        """.trimIndent()

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Intelligent local energy advisor fallback
            return@withContext generateLocalExpertResponse(userQuery, equipmentList, auditSummary, currencySymbol, costPerKwh)
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
            
            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", fullPrompt)
                            })
                        })
                    })
                })
            }

            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                val json = JSONObject(responseBody)
                val candidates = json.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text")
                        if (text.isNotBlank()) {
                            return@withContext text
                        }
                    }
                }
            }
            Log.w("GeminiAiService", "API response empty or unsuccessful: ${response.code}")
            generateLocalExpertResponse(userQuery, equipmentList, auditSummary, currencySymbol, costPerKwh)
        } catch (e: Exception) {
            Log.e("GeminiAiService", "Error calling Gemini API", e)
            generateLocalExpertResponse(userQuery, equipmentList, auditSummary, currencySymbol, costPerKwh)
        }
    }

    private fun buildContextPrompt(
        equipmentList: List<Equipment>,
        summary: EnergyAuditSummary?,
        currencySymbol: String,
        costPerKwh: Double
    ): String {
        val sb = StringBuilder()
        sb.appendLine("- Cost per kWh: $currencySymbol$costPerKwh")
        if (summary != null) {
            sb.appendLine("- Total Devices: ${summary.totalDevices}")
            sb.appendLine("- Total Monthly Consumption: ${summary.totalMonthlyKwh} kWh ($currencySymbol${summary.totalMonthlyCost}/mo)")
            sb.appendLine("- Average Household Efficiency Score: ${summary.averageEfficiencyScore}/100")
            sb.appendLine("- Potential Annual Savings: $currencySymbol${summary.potentialAnnualSavings}/yr")
        }
        sb.appendLine("- Equipment Breakdown:")
        equipmentList.forEach { eq ->
            sb.appendLine("  * ${eq.name} (${eq.category.displayName}): ${eq.powerWatts}W, ${eq.hoursPerDay}h/day, Age: ${eq.ageYears} yrs, Monthly: ${eq.monthlyKwh} kWh ($currencySymbol${eq.monthlyCost}), Efficiency: ${eq.efficiencyScore}% (${eq.efficiencyGrade}), Verdict: ${eq.verdict.title}")
        }
        return sb.toString()
    }

    private fun generateLocalExpertResponse(
        query: String,
        equipmentList: List<Equipment>,
        summary: EnergyAuditSummary?,
        currencySymbol: String,
        costPerKwh: Double
    ): String {
        val lower = query.lowercase()
        val totalMonthlyCost = summary?.totalMonthlyCost ?: 0.0
        val topConsumer = summary?.topConsumerEquipment
        val worstDevice = summary?.mostInefficientEquipment

        return when {
            lower.contains("ac") || lower.contains("air condition") || lower.contains("cooling") -> {
                val acs = equipmentList.filter { it.category.name.contains("AIR_CONDITIONER") }
                val acDetails = acs.joinToString("\n") {
                    "• ${it.name}: ${it.powerWatts}W, ${it.hoursPerDay}h/day → ${it.monthlyKwh} kWh/mo ($currencySymbol${it.monthlyCost}/mo). Efficiency: ${it.efficiencyScore}% (${it.efficiencyGrade}). Verdict: ${it.verdict.title}"
                }
                """
                ❄️ **Air Conditioner Efficiency Analysis**
                
                ${if (acs.isNotEmpty()) "Here is your AC status:\n$acDetails" else "You have not registered an AC unit yet."}
                
                **Top AC Energy Saving Strategies:**
                1. **Optimal Thermostat (24°C - 25°C / 75°F - 77°F):** Every 1°C increase reduces cooling power draw by approximately 6-8%.
                2. **Clean Filters Bi-weekly:** Clogged dust screens force blower motors to work 15% harder, causing excess power draw.
                3. **Inverter Technology Upgrade:** If your AC is over 6-7 years old or non-inverter, upgrading to a 5-Star Inverter unit can save up to 45% on cooling bills.
                """.trimIndent()
            }

            lower.contains("fan") -> {
                """
                🌀 **Electric Fan Energy Optimization**
                
                Electric fans are among the most cost-effective cooling methods (drawing between 45W - 75W vs 1500W+ for ACs).
                
                **Key Tips:**
                1. **Pair Fan with AC:** Use your fan alongside your AC set at 25°C to circulate air. This lets you feel 2°C cooler with negligible fan power cost.
                2. **DC Brushless Motors:** Modern DC motor fans consume as little as 20W-30W, cutting fan consumption by more than half.
                3. **Timer Off Settings:** Set automated sleep timers for overnight usage.
                """.trimIndent()
            }

            lower.contains("pc") || lower.contains("computer") -> {
                """
                💻 **Computer & Workspace Power Optimization**
                
                Desktop rigs and multi-monitors can pull significant baseline loads throughout the day.
                
                **Actionable Recommendations:**
                1. **Enable Deep Sleep:** Set display sleep to 10 minutes and system sleep to 30 minutes in OS power settings.
                2. **Monitor Power Supply Rating:** 80-Plus Gold or Platinum PSUs waste 15-20% less energy as ambient heat compared to standard PSUs.
                3. **Smart Power Strip:** Plug peripherals (speakers, secondary screens, docks) into a master-controlled power strip to cut standby phantom loads.
                """.trimIndent()
            }

            lower.contains("replace") || lower.contains("upgrade") || lower.contains("worth") -> {
                if (worstDevice != null && worstDevice.verdict.shouldReplace) {
                    """
                    🔄 **Appliance Replacement Assessment**
                    
                    **Primary Replacement Candidate: ${worstDevice.name}**
                    • **Current Efficiency:** ${worstDevice.efficiencyScore}% (${worstDevice.efficiencyGrade})
                    • **Current Monthly Cost:** $currencySymbol${worstDevice.monthlyCost}/month
                    • **Estimated Annual Savings:** $currencySymbol${worstDevice.annualCostSavingsIfReplaced}/year
                    • **Estimated Payback Time:** ${worstDevice.paybackMonths} months
                    
                    **Verdict:** ${worstDevice.verdict.title}
                    Replacing this device with a modern 5-star / Inverter certified alternative is financially recommended, as the electricity savings will offset the purchase cost within ${worstDevice.paybackMonths} months.
                    """.trimIndent()
                } else {
                    """
                    🔄 **Appliance Replacement Assessment**
                    
                    Your registered equipment is currently operating at reasonable efficiency. 
                    • Total Household Monthly Bill: ~$currencySymbol$totalMonthlyCost
                    • Devices needing immediate replacement: ${summary?.replaceRecommendedCount ?: 0}
                    
                    Keep regular maintenance schedules for refrigeration coils, air filters, and fan bearings to sustain top efficiency.
                    """.trimIndent()
                }
            }

            else -> {
                """
                ⚡ **VoltSense Energy Summary & Recommendations**
                
                • **Total Tracked Devices:** ${summary?.totalDevices ?: 0}
                • **Estimated Monthly Power:** ${summary?.totalMonthlyKwh ?: 0.0} kWh ($currencySymbol$totalMonthlyCost/mo)
                • **Overall Efficiency Score:** ${summary?.averageEfficiencyScore ?: 0}/100
                ${if (topConsumer != null) "• **Highest Power Consumer:** ${topConsumer.name} (${topConsumer.monthlyKwh} kWh/mo, $currencySymbol${topConsumer.monthlyCost}/mo)" else ""}
                
                **Top Recommended Actions:**
                1. **Target the Largest Energy Consumers:** Focus on continuous-load and heating/cooling devices first.
                2. **Maintain Regular Servicing:** Dust build-up and mechanical friction account for 10-20% of silent efficiency loss.
                3. **Eliminate Standby Power:** Switch off entertainment centers and chargers at the wall when unused.
                """.trimIndent()
            }
        }
    }
}
