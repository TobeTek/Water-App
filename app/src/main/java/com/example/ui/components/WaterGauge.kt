package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.AquaTertiary
import com.example.ui.theme.CeruleanSecondary
import com.example.ui.theme.OceanBluePrimary
import com.example.ui.theme.SuccessEmerald

@Composable
fun WaterGauge(
    currentMl: Int,
    goalMl: Int,
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 230.dp,
    strokeWidth: Dp = 18.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1.5f),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "water_gauge_progress"
    )

    val percentInt = (progress * 100).toInt()
    val isGoalReached = currentMl >= goalMl && goalMl > 0

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        val primaryColor = MaterialTheme.colorScheme.primary
        val tertiaryColor = MaterialTheme.colorScheme.tertiary

        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)

            // Background full track (280 degrees arc)
            val startAngle = 130f
            val sweepAngleTotal = 280f

            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = sweepAngleTotal,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Progress Arc
            val currentSweep = (sweepAngleTotal * animatedProgress.coerceAtMost(1f))

            if (currentSweep > 0f) {
                val progressBrush = Brush.sweepGradient(
                    0.0f to AquaTertiary,
                    0.5f to CeruleanSecondary,
                    1.0f to OceanBluePrimary,
                    center = Offset(this.size.width / 2f, this.size.height / 2f)
                )

                drawArc(
                    brush = progressBrush,
                    startAngle = startAngle,
                    sweepAngle = currentSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }

            // If exceeded 100%, draw extra glow/emerald ring
            if (animatedProgress > 1f) {
                val overflowSweep = sweepAngleTotal * (animatedProgress - 1f).coerceAtMost(0.5f)
                drawArc(
                    color = SuccessEmerald,
                    startAngle = startAngle,
                    sweepAngle = overflowSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx * 0.7f, cap = StrokeCap.Round)
                )
            }
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isGoalReached) Icons.Default.CheckCircle else Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = if (isGoalReached) SuccessEmerald else primaryColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$percentInt%",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 38.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "$currentMl / $goalMl ml",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(2.dp))

            if (isGoalReached) {
                Surface(
                    color = SuccessEmerald.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = "Goal Reached! 🎉",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SuccessEmerald
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            } else {
                val remaining = (goalMl - currentMl).coerceAtLeast(0)
                Text(
                    text = "$remaining ml remaining",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
