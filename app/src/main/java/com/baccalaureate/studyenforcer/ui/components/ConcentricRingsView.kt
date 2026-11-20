package com.baccalaureate.studyenforcer.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.baccalaureate.studyenforcer.ui.theme.AccentEmerald
import com.baccalaureate.studyenforcer.ui.theme.AccentGold
import com.baccalaureate.studyenforcer.ui.theme.BorderHairline
import com.baccalaureate.studyenforcer.ui.theme.InkTitanium

@Composable
fun ConcentricRingsView(
    modifier: Modifier = Modifier,
    size: Dp = 150.dp,
    streakProgress: Float = 0.85f, // Outer (Emerald)
    focusProgress: Float = 0.65f,  // Middle (Gold)
    accuracyProgress: Float = 0.90f // Inner (Titanium)
) {
    val animatedStreak = remember { Animatable(0f) }
    val animatedFocus = remember { Animatable(0f) }
    val animatedAccuracy = remember { Animatable(0f) }

    LaunchedEffect(streakProgress, focusProgress, accuracyProgress) {
        animatedStreak.animateTo(streakProgress.coerceIn(0f, 1f), tween(900, easing = FastOutSlowInEasing))
        animatedFocus.animateTo(focusProgress.coerceIn(0f, 1f), tween(1100, easing = FastOutSlowInEasing))
        animatedAccuracy.animateTo(accuracyProgress.coerceIn(0f, 1f), tween(1300, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 11.dp.toPx()
            val ringGap = 6.dp.toPx()
            val center = Offset(this.size.width / 2, this.size.height / 2)

            // 1. Outer Ring: Streak (Emerald)
            val outerRadius = (this.size.minDimension / 2) - (strokeWidth / 2)
            drawRing(
                center = center,
                radius = outerRadius,
                strokeWidth = strokeWidth,
                progress = animatedStreak.value,
                activeColor = AccentEmerald,
                trackColor = BorderHairline.copy(alpha = 0.45f)
            )

            // 2. Middle Ring: Focus (Gold)
            val middleRadius = outerRadius - strokeWidth - ringGap
            drawRing(
                center = center,
                radius = middleRadius,
                strokeWidth = strokeWidth,
                progress = animatedFocus.value,
                activeColor = AccentGold,
                trackColor = BorderHairline.copy(alpha = 0.45f)
            )

            // 3. Inner Ring: Accuracy (Titanium)
            val innerRadius = middleRadius - strokeWidth - ringGap
            drawRing(
                center = center,
                radius = innerRadius,
                strokeWidth = strokeWidth,
                progress = animatedAccuracy.value,
                activeColor = InkTitanium,
                trackColor = BorderHairline.copy(alpha = 0.45f)
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRing(
    center: Offset,
    radius: Float,
    strokeWidth: Float,
    progress: Float,
    activeColor: Color,
    trackColor: Color
) {
    val topLeft = Offset(center.x - radius, center.y - radius)
    val ringSize = Size(radius * 2, radius * 2)

    // Background track
    drawArc(
        color = trackColor,
        startAngle = -90f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = topLeft,
        size = ringSize,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )

    // Active progress arc
    if (progress > 0.01f) {
        drawArc(
            color = activeColor,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = topLeft,
            size = ringSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}
