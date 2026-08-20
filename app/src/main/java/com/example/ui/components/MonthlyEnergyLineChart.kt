package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MonthlyLog
import com.example.ui.theme.DeepPurple
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.PastelPurpleBorder
import com.example.ui.theme.PastelPurpleContainer
import com.example.ui.theme.PastelPurpleLight
import com.example.ui.theme.PastelPurplePrimary
import com.example.ui.theme.PastelPurpleSoft
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.max

enum class ChartMetric {
    KWH, COST
}

@Composable
fun MonthlyEnergyLineChart(
    logs: List<MonthlyLog>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    var selectedMetric by remember { mutableStateOf(ChartMetric.KWH) }
    var selectedPointIndex by remember { mutableIntStateOf(if (logs.isNotEmpty()) logs.size - 1 else 0) }

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(logs, selectedMetric) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(700))
    }

    val activeLog = logs.getOrNull(selectedPointIndex) ?: logs.lastOrNull()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_energy_line_chart"),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(PastelPurpleBorder)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monthly Usage Trend",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DeepPurple
                    )
                    Text(
                        text = if (logs.isNotEmpty()) "${logs.first().monthName} - ${logs.last().monthName}, ${logs.last().year}" else "Historical usage",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = TextSecondary
                    )
                }

                if (activeLog != null) {
                    Text(
                        text = if (selectedMetric == ChartMetric.KWH) "${activeLog.totalKwh} kWh" else "$currencySymbol${activeLog.totalCost}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        ),
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metric Toggle Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PastelPurpleContainer.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Selected: ",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "${activeLog?.monthName ?: ""} (${if (selectedMetric == ChartMetric.KWH) "$currencySymbol${activeLog?.totalCost ?: 0.0}" else "${activeLog?.totalKwh ?: 0.0} kWh"})",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = DeepPurple
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = selectedMetric == ChartMetric.KWH,
                        onClick = { selectedMetric = ChartMetric.KWH },
                        label = { Text("kWh", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PastelPurplePrimary,
                            selectedLabelColor = Color.White,
                            containerColor = PastelPurpleSoft.copy(alpha = 0.3f),
                            labelColor = TextSecondary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(30.dp)
                    )
                    FilterChip(
                        selected = selectedMetric == ChartMetric.COST,
                        onClick = { selectedMetric = ChartMetric.COST },
                        label = { Text(currencySymbol, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PastelPurplePrimary,
                            selectedLabelColor = Color.White,
                            containerColor = PastelPurpleSoft.copy(alpha = 0.3f),
                            labelColor = TextSecondary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Add equipment to see monthly consumption trend.", color = TextMuted)
                }
            } else {
                // Canvas Line Graph
                val values = logs.map { if (selectedMetric == ChartMetric.KWH) it.totalKwh else it.totalCost }
                val maxVal = max(10.0, (values.maxOrNull() ?: 100.0) * 1.2)
                val minVal = 0.0

                val primaryColor = PastelPurplePrimary
                val lightColor = PastelPurpleLight
                val softBgColor = PastelPurpleSoft

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .pointerInput(logs) {
                            detectTapGestures { offset ->
                                val spacing = size.width / (logs.size - 1).coerceAtLeast(1)
                                val index = (offset.x / spacing).toInt().coerceIn(0, logs.size - 1)
                                selectedPointIndex = index
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val bottomPadding = 26.dp.toPx()
                    val topPadding = 14.dp.toPx()
                    val chartHeight = height - bottomPadding - topPadding

                    val spacing = width / (logs.size - 1).coerceAtLeast(1)

                    // Draw Horizontal Gridlines
                    val gridLines = 3
                    for (i in 0..gridLines) {
                        val y = topPadding + (chartHeight / gridLines) * i
                        drawLine(
                            color = Color(0xFFF1EEFA),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    // Build Curve Points
                    val points = logs.indices.map { i ->
                        val value = values[i]
                        val x = i * spacing
                        val y = topPadding + chartHeight * (1f - ((value - minVal) / (maxVal - minVal)).toFloat() * animationProgress.value)
                        Offset(x, y)
                    }

                    // Draw Smooth Gradient Area
                    if (points.isNotEmpty()) {
                        val fillPath = Path().apply {
                            moveTo(points.first().x, height - bottomPadding)
                            lineTo(points.first().x, points.first().y)
                            for (i in 0 until points.size - 1) {
                                val p0 = points[i]
                                val p1 = points[i + 1]
                                val cx = (p0.x + p1.x) / 2f
                                cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                            }
                            lineTo(points.last().x, height - bottomPadding)
                            close()
                        }

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.35f),
                                    softBgColor.copy(alpha = 0.15f),
                                    Color.Transparent
                                ),
                                startY = topPadding,
                                endY = height - bottomPadding
                            )
                        )

                        // Draw Curve Stroke
                        val strokePath = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 0 until points.size - 1) {
                                val p0 = points[i]
                                val p1 = points[i + 1]
                                val cx = (p0.x + p1.x) / 2f
                                cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                            }
                        }

                        drawPath(
                            path = strokePath,
                            color = primaryColor,
                            style = Stroke(
                                width = 3.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        )

                        // Draw Data Points & Month Labels
                        points.forEachIndexed { index, point ->
                            val isSelected = index == selectedPointIndex
                            val pointRadius = if (isSelected) 7.dp.toPx() else 4.dp.toPx()

                            if (isSelected) {
                                drawCircle(
                                    color = primaryColor.copy(alpha = 0.25f),
                                    radius = pointRadius + 6.dp.toPx(),
                                    center = point
                                )
                            }

                            drawCircle(
                                color = Color.White,
                                radius = pointRadius,
                                center = point
                            )
                            drawCircle(
                                color = if (isSelected) primaryColor else lightColor,
                                radius = pointRadius - 1.5.dp.toPx(),
                                center = point
                            )

                            // Month Label
                            val monthText = logs[index].monthName
                            drawContext.canvas.nativeCanvas.apply {
                                val paint = android.graphics.Paint().apply {
                                    color = if (isSelected) android.graphics.Color.parseColor("#6D28D9") else android.graphics.Color.parseColor("#94A3B8")
                                    textSize = 28f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isFakeBoldText = isSelected
                                }
                                drawText(monthText, point.x, height - 4.dp.toPx(), paint)
                            }
                        }
                    }
                }
            }
        }
    }
}
