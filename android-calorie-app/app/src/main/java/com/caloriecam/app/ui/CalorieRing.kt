package com.caloriecam.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Circular "consumed vs daily goal" calorie indicator. */
@Composable
fun CalorieRing(
    consumed: Int,
    goal: Int,
    modifier: Modifier = Modifier
) {
    val rawProgress = if (goal > 0) consumed.toFloat() / goal.toFloat() else 0f
    val progress by animateFloatAsState(
        targetValue = rawProgress.coerceIn(0f, 1f),
        animationSpec = tween(700),
        label = "calorieRingProgress"
    )
    val overGoal = goal > 0 && consumed > goal

    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val progressColor = if (overGoal) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    Box(modifier = modifier.size(190.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(190.dp)) {
            val strokeWidth = 16.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$consumed",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (goal > 0) "з $goal ккал" else "ккал",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
