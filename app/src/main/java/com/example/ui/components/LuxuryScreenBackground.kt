package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import kotlin.math.sin

/**
 * Animated luxury royal casino background with real high-definition casino artwork,
 * ambient vignette scrim, floating gold sparkles, card suit watermarks, and radial glow lights.
 */
@Composable
fun LuxuryScreenBackground(
    modifier: Modifier = Modifier,
    imageResId: Int = R.drawable.img_banner_casino,
    overlayAlpha: Float = 0.55f,
    baseDarkColor: Color = Color(0xFF070B14),
    accentGlowColor: Color = Color(0xFF1E1B4B),
    enableParticles: Boolean = true,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "luxury_bg_transition")

    // Ambient breathing radial pulse
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.50f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bg_pulse"
    )

    // Particle drift animation
    val particlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_phase"
    )

    // Pre-calculated particle seed
    val particles = remember {
        List(24) { index ->
            BackgroundParticle(
                xNorm = (index * 0.041f + 0.02f) % 1.0f,
                speed = 0.4f + (index % 5) * 0.15f,
                size = 2f + (index % 4) * 1.5f,
                alpha = 0.2f + (index % 3) * 0.25f,
                wobbleSpeed = 2f + (index % 3)
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseDarkColor)
    ) {
        // 1. Real High-Definition Casino Background Image
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Cinematic luxury vignette scrim and atmospheric lighting overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Scrim to make UI elements pop while keeping casino hall visible
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        baseDarkColor.copy(alpha = overlayAlpha),
                        baseDarkColor.copy(alpha = overlayAlpha * 0.75f),
                        baseDarkColor.copy(alpha = overlayAlpha * 1.15f).copy(alpha = overlayAlpha.coerceAtMost(0.85f))
                    )
                )
            )

            // Deep radial gradient vignette
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accentGlowColor.copy(alpha = pulseAlpha),
                        Color(0xFF0F172A).copy(alpha = 0.5f),
                        baseDarkColor.copy(alpha = 0.7f)
                    ),
                    center = Offset(width * 0.5f, height * 0.35f),
                    radius = width.coerceAtLeast(height) * 0.85f
                )
            )

            // Subtle luxury felt grid / card watermark texture
            drawWatermarkPattern(width, height)

            // Floating gold sparkles
            if (enableParticles) {
                for (p in particles) {
                    val currentY = ((p.xNorm * 1000f + particlePhase * 1000f * p.speed) % height)
                    val wobbleX = sin((particlePhase * 2 * Math.PI * p.wobbleSpeed) + p.xNorm * 10f).toFloat() * 24f
                    val posX = (p.xNorm * width + wobbleX).coerceIn(0f, width)
                    val posY = height - currentY // Float upwards

                    val currentAlpha = (p.alpha * sin((posY / height) * Math.PI.toFloat())).coerceIn(0f, 1f)

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFD700).copy(alpha = currentAlpha),
                                Color(0xFFF59E0B).copy(alpha = currentAlpha * 0.5f),
                                Color.Transparent
                            ),
                            center = Offset(posX, posY),
                            radius = p.size * 3f
                        ),
                        radius = p.size * 3f,
                        center = Offset(posX, posY)
                    )
                }
            }
        }

        // 3. Screen Content
        content()
    }
}

private fun DrawScope.drawWatermarkPattern(width: Float, height: Float) {
    // Subtle luxury diagonal lattice
    val step = 90f
    val lineColor = Color(0xFFFFD700).copy(alpha = 0.025f)

    var x = -height
    while (x < width + height) {
        drawLine(
            color = lineColor,
            start = Offset(x, 0f),
            end = Offset(x + height, height),
            strokeWidth = 1f
        )
        drawLine(
            color = lineColor,
            start = Offset(x, height),
            end = Offset(x + height, 0f),
            strokeWidth = 1f
        )
        x += step
    }
}

private data class BackgroundParticle(
    val xNorm: Float,
    val speed: Float,
    val size: Float,
    val alpha: Float,
    val wobbleSpeed: Float
)
