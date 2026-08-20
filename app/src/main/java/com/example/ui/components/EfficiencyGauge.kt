package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedSoft
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.EcoGreenSoft
import com.example.ui.theme.PastelPurpleContainer
import com.example.ui.theme.PastelPurplePrimary
import com.example.ui.theme.PastelPurpleSoft
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningOrange
import com.example.ui.theme.WarningOrangeSoft

@Composable
fun EfficiencyGauge(
    score: Int, // 0 - 100
    grade: String,
    size: Dp = 120.dp,
    strokeWidth: Dp = 10.dp,
    showGradeBadge: Boolean = true,
    modifier: Modifier = Modifier
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(score) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = (score.coerceIn(0, 100) / 100f),
            animationSpec = tween(800)
        )
    }

    val (gaugeColor, containerColor) = when {
        score >= 80 -> EcoGreen to EcoGreenSoft
        score >= 65 -> PastelPurplePrimary to PastelPurpleContainer
        score >= 45 -> WarningOrange to WarningOrangeSoft
        else -> AlertRed to AlertRedSoft
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val strokePx = strokeWidth.toPx()
                val arcSize = Size(size.toPx() - strokePx, size.toPx() - strokePx)
                val topLeft = Offset(strokePx / 2, strokePx / 2)

                // Background Track (240 degrees sweep)
                drawArc(
                    color = Color(0xFFEDE7F6),
                    startAngle = 150f,
                    sweepAngle = 240f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                // Animated Progress Arc
                val sweep = 240f * animatedProgress.value
                if (sweep > 0f) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(PastelPurpleSoft, gaugeColor)
                        ),
                        startAngle = 150f,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                }
            }

            // Center Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "$score%",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = if (size > 100.dp) 22.sp else 16.sp
                    ),
                    color = TextPrimary
                )
                Text(
                    text = "Efficiency",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextMuted
                )
            }
        }

        if (showGradeBadge) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = containerColor,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Grade $grade",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = gaugeColor
                    )
                }
            }
        }
    }
}
