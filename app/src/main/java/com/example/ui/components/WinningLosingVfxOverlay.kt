package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class ConfettiPiece(
    val xRatio: Float,
    val initialY: Float,
    val speedY: Float,
    val speedX: Float,
    val color: Color,
    val width: Float,
    val height: Float,
    val rotationSpeed: Float,
    val flutterSpeed: Float,
    val phase: Float
)

private data class TwinkleStar(
    val xRatio: Float,
    val yRatio: Float,
    val size: Float,
    val color: Color,
    val rotation: Float,
    val phase: Float
)

private data class RainStreak(
    val xRatio: Float,
    val yRatio: Float,
    val length: Float,
    val speed: Float,
    val alpha: Float,
    val strokeWidth: Float
)

private data class EmberSpark(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val speedY: Float,
    val driftSpeed: Float,
    val color: Color,
    val phase: Float
)

/**
 * High-fidelity visual VFX for Winning and Losing events.
 * Renders full-screen celebratory confetti/sunburst/stars on victory,
 * or atmospheric rain/ember ash/red vignette on defeat.
 */
@Composable
fun WinningLosingVfxOverlay(
    isVictory: Boolean,
    modifier: Modifier = Modifier,
    continuousLoop: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vfx_anim")
    val animTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "anim_time"
    )

    val sunburstAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sunburst_rot"
    )

    val defeatPulse by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "defeat_pulse"
    )

    val confettiList = remember(isVictory) {
        if (!isVictory) emptyList()
        else {
            val palette = listOf(
                Color(0xFFFFD700), // Gold
                Color(0xFFFFC107), // Amber
                Color(0xFF10B981), // Emerald
                Color(0xFF38BDF8), // Cyan
                Color(0xFFE879F9), // Magenta
                Color(0xFFF43F5E), // Rose
                Color(0xFFFFFFFF)  // White diamond
            )
            List(120) {
                ConfettiPiece(
                    xRatio = Random.nextFloat(),
                    initialY = Random.nextFloat() * -0.6f - 0.1f,
                    speedY = Random.nextFloat() * 0.45f + 0.35f,
                    speedX = (Random.nextFloat() - 0.5f) * 0.12f,
                    color = palette.random(),
                    width = Random.nextFloat() * 12f + 8f,
                    height = Random.nextFloat() * 8f + 5f,
                    rotationSpeed = Random.nextFloat() * 360f + 180f,
                    flutterSpeed = Random.nextFloat() * 8f + 4f,
                    phase = Random.nextFloat() * 2f * PI.toFloat()
                )
            }
        }
    }

    val starsList = remember(isVictory) {
        if (!isVictory) emptyList()
        else {
            List(35) {
                TwinkleStar(
                    xRatio = Random.nextFloat(),
                    yRatio = Random.nextFloat() * 0.85f + 0.05f,
                    size = Random.nextFloat() * 14f + 6f,
                    color = if (Random.nextBoolean()) Color(0xFFFFD700) else Color(0xFFFFFBEB),
                    rotation = Random.nextFloat() * 45f,
                    phase = Random.nextFloat() * 2f * PI.toFloat()
                )
            }
        }
    }

    val rainList = remember(isVictory) {
        if (isVictory) emptyList()
        else {
            List(75) {
                RainStreak(
                    xRatio = Random.nextFloat(),
                    yRatio = Random.nextFloat(),
                    length = Random.nextFloat() * 38f + 16f,
                    speed = Random.nextFloat() * 1.2f + 0.8f,
                    alpha = Random.nextFloat() * 0.4f + 0.2f,
                    strokeWidth = Random.nextFloat() * 1.5f + 1f
                )
            }
        }
    }

    val embersList = remember(isVictory) {
        if (isVictory) emptyList()
        else {
            val emberColors = listOf(
                Color(0xFFEF4444),
                Color(0xFFF97316),
                Color(0xFFDC2626),
                Color(0xFF7F1D1D)
            )
            List(30) {
                EmberSpark(
                    xRatio = Random.nextFloat(),
                    yRatio = Random.nextFloat(),
                    radius = Random.nextFloat() * 3f + 1.5f,
                    speedY = Random.nextFloat() * 0.15f + 0.08f,
                    driftSpeed = Random.nextFloat() * 4f + 2f,
                    color = emberColors.random(),
                    phase = Random.nextFloat() * 2f * PI.toFloat()
                )
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        if (isVictory) {
            // 1. Subtle Golden Sunburst Rays from center
            drawSunburst(center = Offset(w * 0.5f, h * 0.45f), radius = w.coerceAtLeast(h) * 0.8f, angle = sunburstAngle)

            // 2. Twinkling Stars
            for (star in starsList) {
                val twinkle = (sin(animTime * 2 * PI + star.phase).toFloat() * 0.5f + 0.5f).coerceIn(0.1f, 1f)
                drawTwinkleStar(
                    center = Offset(star.xRatio * w, star.yRatio * h),
                    size = star.size * twinkle,
                    color = star.color.copy(alpha = twinkle * 0.85f),
                    rotation = star.rotation + animTime * 45f
                )
            }

            // 3. 3D Tumbling Confetti Ribbons
            for (confetti in confettiList) {
                val totalTravel = confetti.speedY * (animTime * 3500f / 1000f)
                val currentY = (confetti.initialY * h + totalTravel * h) % (h + 60f) - 30f
                val currentX = (confetti.xRatio * w + sin(animTime * confetti.flutterSpeed + confetti.phase) * 35f)
                    .coerceIn(10f, w - 10f)

                val rotX = (animTime * confetti.rotationSpeed + confetti.phase * 57.3f) % 360f
                val scaleY = cos(rotX * PI / 180.0).toFloat().coerceIn(-1f, 1f)

                rotate(degrees = rotX, pivot = Offset(currentX, currentY)) {
                    drawRect(
                        color = confetti.color.copy(alpha = 0.95f),
                        topLeft = Offset(currentX - confetti.width * 0.5f, currentY - (confetti.height * scaleY * 0.5f)),
                        size = Size(confetti.width, kotlin.math.abs(confetti.height * scaleY).coerceAtLeast(1f))
                    )
                }
            }
        } else {
            // Defeat Mode
            // 1. Pulsing Crimson Danger Vignette on outer borders
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF450A0A).copy(alpha = defeatPulse * 0.4f),
                        Color(0xFF7F1D1D).copy(alpha = defeatPulse * 0.75f)
                    ),
                    center = Offset(w * 0.5f, h * 0.5f),
                    radius = (w * 0.7f).coerceAtLeast(h * 0.7f)
                ),
                size = size
            )

            // 2. Stormy Rain Streaks
            for (rain in rainList) {
                val dropY = ((rain.yRatio + animTime * rain.speed) % 1.0f) * h
                val dropX = (rain.xRatio * w + (dropY / h) * 45f) % w // Slight diagonal wind drift
                drawLine(
                    color = Color(0xFF94A3B8).copy(alpha = rain.alpha),
                    start = Offset(dropX, dropY),
                    end = Offset(dropX + 6f, dropY + rain.length),
                    strokeWidth = rain.strokeWidth
                )
            }

            // 3. Falling Embers with lateral swaying
            for (ember in embersList) {
                val emberY = ((ember.yRatio + animTime * ember.speedY) % 1.0f) * h
                val emberX = (ember.xRatio * w + sin(animTime * ember.driftSpeed + ember.phase) * 20f).coerceIn(5f, w - 5f)
                val emberAlpha = (1f - (emberY / h)).coerceIn(0.2f, 0.9f)

                drawCircle(
                    color = ember.color.copy(alpha = emberAlpha),
                    radius = ember.radius,
                    center = Offset(emberX, emberY)
                )
            }
        }
    }
}

private fun DrawScope.drawSunburst(center: Offset, radius: Float, angle: Float) {
    val rayCount = 16
    val sweepAngle = 360f / rayCount
    for (i in 0 until rayCount) {
        if (i % 2 == 0) {
            val startDeg = (i * sweepAngle + angle) % 360f
            val rad1 = (startDeg * PI / 180.0).toFloat()
            val rad2 = ((startDeg + sweepAngle * 0.6f) * PI / 180.0).toFloat()

            val path = Path().apply {
                moveTo(center.x, center.y)
                lineTo(center.x + cos(rad1) * radius, center.y + sin(rad1) * radius)
                lineTo(center.x + cos(rad2) * radius, center.y + sin(rad2) * radius)
                close()
            }
            drawPath(
                path = path,
                color = Color(0xFFFFD700).copy(alpha = 0.05f)
            )
        }
    }
}

private fun DrawScope.drawTwinkleStar(
    center: Offset,
    size: Float,
    color: Color,
    rotation: Float
) {
    rotate(degrees = rotation, pivot = center) {
        val path = Path().apply {
            val half = size * 0.5f
            val inner = size * 0.15f
            moveTo(center.x, center.y - half)
            lineTo(center.x + inner, center.y - inner)
            lineTo(center.x + half, center.y)
            lineTo(center.x + inner, center.y + inner)
            lineTo(center.x, center.y + half)
            lineTo(center.x - inner, center.y + inner)
            lineTo(center.x - half, center.y)
            lineTo(center.x - inner, center.y - inner)
            close()
        }
        drawPath(path = path, color = color)
    }
}
