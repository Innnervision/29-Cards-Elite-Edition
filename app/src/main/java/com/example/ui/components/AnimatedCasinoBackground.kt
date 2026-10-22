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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.theme.*
import kotlin.math.*

private enum class SuitType { SPADE, HEART, DIAMOND, CLUB }

private data class FloatingSuit(
    val type: SuitType,
    val initialX: Float,       // 0f..1f
    val speedY: Float,         // normalized speed
    val baseSize: Float,       // in dp or px
    val rotationSpeed: Float,  // degrees per second / phase
    val wobbleSpeed: Float,
    val wobbleAmp: Float,
    val maxAlpha: Float,
    val color: Color,
    val glowColor: Color
)

private data class ShimmerSparkle(
    val xNorm: Float,
    val speed: Float,
    val size: Float,
    val baseAlpha: Float,
    val wobblePhase: Float,
    val isStarFlare: Boolean,
    val color: Color
)

/**
 * AAA Animated Casino Background for the Main Menu & Hub screens.
 * Features:
 * 1. Living Aurora & Dynamic Radial Light Orbs (Royal Indigo, Velvet Purple, Amber Gold, Emerald)
 * 2. Subtle Cinematic Ken-Burns Camera Drift on the HD Casino Hall Backdrop
 * 3. 3D Floating & Rotating Card Suit Emblems (♠, ♥, ♦, ♣) with Neon/Golden Glow
 * 4. Multi-layered Rising Golden Stardust & Twinkling 4-Point Star Flares
 * 5. Angled Golden God-Ray / Light Shimmer Sweeps
 * 6. Luxury Diamond Geometric Felt Lattice with Pulsing Energy
 * 7. Scrim & Vignette for maximum UI contrast and readability
 */
@Composable
fun AnimatedCasinoBackground(
    modifier: Modifier = Modifier,
    imageResId: Int = R.drawable.img_banner_casino,
    overlayAlpha: Float = 0.50f,
    baseDarkColor: Color = Color(0xFF04060B),
    enableFloatingSuits: Boolean = true,
    enableSparkles: Boolean = true,
    enableLightRays: Boolean = true,
    enableBackdropMotion: Boolean = true,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "casino_animated_bg")

    // 1. Slow cosmic background light cycle (14 seconds)
    val colorCycle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bg_color_cycle"
    )

    // 2. Light beam shimmer sweep (7.5 seconds)
    val raySweep by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(7500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ray_sweep"
    )

    // 3. Floating particle upward drift (10 seconds)
    val particlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_phase"
    )

    // 4. Floating suits lifecycle & rotation phase (16 seconds)
    val suitPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "suit_phase"
    )

    // 5. Subtle camera scale breathing for Ken Burns (18 seconds)
    val cameraScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "camera_scale"
    )

    val cameraPanX by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "camera_pan_x"
    )

    // Pre-calculated deterministic floating suits
    val floatingSuits = remember {
        listOf(
            FloatingSuit(SuitType.SPADE, 0.12f, 0.7f, 32f, 180f, 1.8f, 22f, 0.28f, Color(0xFFE2E8F0), Color(0xFF38BDF8)),
            FloatingSuit(SuitType.HEART, 0.82f, 0.6f, 36f, -150f, 1.4f, 26f, 0.32f, Color(0xFFF87171), Color(0xFFEF4444)),
            FloatingSuit(SuitType.DIAMOND, 0.28f, 0.9f, 28f, 220f, 2.2f, 18f, 0.35f, Color(0xFFFFD700), Color(0xFFF59E0B)),
            FloatingSuit(SuitType.CLUB, 0.72f, 0.75f, 30f, -190f, 1.6f, 20f, 0.26f, Color(0xFF34D399), Color(0xFF10B981)),
            FloatingSuit(SuitType.SPADE, 0.90f, 0.55f, 26f, 140f, 1.2f, 16f, 0.24f, Color(0xFF93C5FD), Color(0xFF3B82F6)),
            FloatingSuit(SuitType.HEART, 0.08f, 0.85f, 30f, -200f, 2.0f, 24f, 0.28f, Color(0xFFFB7185), Color(0xFFE11D48)),
            FloatingSuit(SuitType.DIAMOND, 0.52f, 0.65f, 34f, 160f, 1.5f, 22f, 0.30f, Color(0xFFFDE047), Color(0xFFD97706)),
            FloatingSuit(SuitType.CLUB, 0.38f, 0.80f, 28f, -170f, 1.9f, 18f, 0.25f, Color(0xFF6EE7B7), Color(0xFF059669))
        )
    }

    // Pre-calculated golden sparkles & star flares
    val sparkles = remember {
        List(36) { index ->
            val isStar = index % 5 == 0
            val color = when (index % 4) {
                0 -> Color(0xFFFFD700) // Gold
                1 -> Color(0xFFFFF0A5) // Pale gold
                2 -> Color(0xFF38BDF8) // Diamond blue
                else -> Color(0xFFFDE047) // Amber yellow
            }
            ShimmerSparkle(
                xNorm = (index * 0.028f + 0.015f) % 1.0f,
                speed = 0.35f + (index % 6) * 0.12f,
                size = if (isStar) 4f + (index % 3) * 2f else 1.8f + (index % 4) * 1.2f,
                baseAlpha = 0.25f + (index % 4) * 0.2f,
                wobblePhase = (index * 1.37f),
                isStarFlare = isStar,
                color = color
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseDarkColor)
    ) {
        // 1. High-Definition Casino Artwork with Ken Burns animated drift
        if (enableBackdropMotion) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Background base color
                drawRect(baseDarkColor)
            }
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = cameraScale
                        scaleY = cameraScale
                        translationX = cameraPanX
                    }
            )
        } else {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. Animated Custom Canvas Graphics Engine
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            if (width <= 0f || height <= 0f) return@Canvas

            // A. Dark Vignette Base Gradient (Ensures UI components are hyper-legible)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        baseDarkColor.copy(alpha = overlayAlpha * 0.95f),
                        baseDarkColor.copy(alpha = overlayAlpha * 0.65f),
                        baseDarkColor.copy(alpha = overlayAlpha * 1.15f).copy(alpha = overlayAlpha.coerceAtMost(0.92f))
                    )
                )
            )

            // B. Moving Ambient Aurora Orbs (Smooth Lissajous harmonic curves)
            val orb1X = width * (0.35f + 0.25f * sin(colorCycle * 2 * Math.PI.toFloat()))
            val orb1Y = height * (0.25f + 0.15f * cos(colorCycle * 2 * Math.PI.toFloat()))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF4C1D95).copy(alpha = 0.42f), // Royal Velvet Purple
                        Color(0xFF1E1B4B).copy(alpha = 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(orb1X, orb1Y),
                    radius = width * 0.75f
                ),
                radius = width * 0.75f,
                center = Offset(orb1X, orb1Y)
            )

            val orb2X = width * (0.75f - 0.20f * cos(colorCycle * 2 * Math.PI.toFloat() * 0.8f))
            val orb2Y = height * (0.65f + 0.18f * sin(colorCycle * 2 * Math.PI.toFloat() * 0.8f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF065F46).copy(alpha = 0.35f), // Royal Emerald Felt Glow
                        Color(0xFF0F766E).copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = Offset(orb2X, orb2Y),
                    radius = width * 0.85f
                ),
                radius = width * 0.85f,
                center = Offset(orb2X, orb2Y)
            )

            val orb3X = width * (0.50f + 0.30f * sin(colorCycle * 2 * Math.PI.toFloat() * 1.3f))
            val orb3Y = height * (0.15f + 0.10f * cos(colorCycle * 2 * Math.PI.toFloat() * 1.3f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFD97706).copy(alpha = 0.28f), // Golden Amber Flare
                        Color(0xFFB45309).copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(orb3X, orb3Y),
                    radius = width * 0.65f
                ),
                radius = width * 0.65f,
                center = Offset(orb3X, orb3Y)
            )

            // C. Luxury Diamond Weave / Card Felt Micro-Lattice with Shimmer Pulse
            val latticePulse = (0.018f + 0.012f * sin(colorCycle * 4 * Math.PI.toFloat())).coerceIn(0.01f, 0.04f)
            drawLuxuryDiamondLattice(width, height, latticePulse)

            // D. Angled Golden God-Rays / Chandelier Light Beams
            if (enableLightRays) {
                drawGodRayBeams(width, height, raySweep)
            }

            // E. Floating Holographic Card Suit Emblems (♠, ♥, ♦, ♣)
            if (enableFloatingSuits) {
                for (suit in floatingSuits) {
                    val progress = (suitPhase * suit.speedY + suit.initialX) % 1.0f
                    val posY = height * (1.1f - progress * 1.2f) // Travel from bottom to top
                    val wobbleX = sin(suitPhase * 2 * Math.PI.toFloat() * suit.wobbleSpeed + suit.initialX * 10f) * suit.wobbleAmp
                    val posX = (suit.initialX * width + wobbleX).coerceIn(16f, width - 16f)

                    // Sinusoidal fade in/out near screen edges
                    val edgeFade = sin((posY / height).coerceIn(0f, 1f) * Math.PI.toFloat())
                    val currentAlpha = (suit.maxAlpha * edgeFade).coerceIn(0f, 1f)

                    if (currentAlpha > 0.02f) {
                        val currentRotation = (suitPhase * suit.rotationSpeed + suit.initialX * 360f) % 360f
                        withTransform({
                            translate(left = posX, top = posY)
                            rotate(degrees = currentRotation, pivot = Offset.Zero)
                        }) {
                            drawCardSuitIcon(
                                suitType = suit.type,
                                size = suit.baseSize,
                                color = suit.color.copy(alpha = currentAlpha),
                                glowColor = suit.glowColor.copy(alpha = currentAlpha * 0.5f)
                            )
                        }
                    }
                }
            }

            // F. Multi-layered Rising Golden Stardust & 4-Point Star Sparkles
            if (enableSparkles) {
                for (sp in sparkles) {
                    val progress = (particlePhase * sp.speed + sp.xNorm) % 1.0f
                    val posY = height * (1.05f - progress * 1.1f)
                    val wobbleX = sin(particlePhase * 2 * Math.PI.toFloat() * 1.8f + sp.wobblePhase) * 18f
                    val posX = (sp.xNorm * width + wobbleX).coerceIn(0f, width)

                    val edgeFade = sin((posY / height).coerceIn(0f, 1f) * Math.PI.toFloat())
                    val pulseTwinkle = (0.5f + 0.5f * sin((particlePhase * 10f + sp.wobblePhase) * Math.PI.toFloat()))
                    val currentAlpha = (sp.baseAlpha * edgeFade * pulseTwinkle).coerceIn(0f, 1f)

                    if (currentAlpha > 0.03f) {
                        if (sp.isStarFlare) {
                            drawStarFlare(
                                center = Offset(posX, posY),
                                radius = sp.size * 2.2f,
                                color = sp.color.copy(alpha = currentAlpha)
                            )
                        } else {
                            // Circular glowing ember particle
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        sp.color.copy(alpha = currentAlpha),
                                        sp.color.copy(alpha = currentAlpha * 0.35f),
                                        Color.Transparent
                                    ),
                                    center = Offset(posX, posY),
                                    radius = sp.size * 2.5f
                                ),
                                radius = sp.size * 2.5f,
                                center = Offset(posX, posY)
                            )
                        }
                    }
                }
            }

            // G. Outer Perimeter Luxury Edge Vignette for depth
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        baseDarkColor.copy(alpha = 0.40f),
                        baseDarkColor.copy(alpha = 0.85f)
                    ),
                    center = Offset(width * 0.5f, height * 0.5f),
                    radius = width.coerceAtLeast(height) * 0.75f
                )
            )
        }

        // 3. Main Screen UI Content Overlay
        content()
    }
}

/**
 * Draws diagonal God-Ray light beams sweeping across the screen
 */
private fun DrawScope.drawGodRayBeams(width: Float, height: Float, sweepProgress: Float) {
    val beamCenter = width * sweepProgress
    val beamWidth = width * 0.55f

    val rayBrush = Brush.linearGradient(
        colors = listOf(
            Color.Transparent,
            Color(0xFFFFD700).copy(alpha = 0.035f),
            Color(0xFFFFFBEB).copy(alpha = 0.070f),
            Color(0xFFFFD700).copy(alpha = 0.035f),
            Color.Transparent
        ),
        start = Offset(beamCenter - beamWidth * 0.5f, 0f),
        end = Offset(beamCenter + beamWidth * 0.5f, height)
    )

    drawRect(
        brush = rayBrush,
        size = Size(width, height)
    )
}

/**
 * Draws high-precision royal casino watermark diamond lattice
 */
private fun DrawScope.drawLuxuryDiamondLattice(width: Float, height: Float, alpha: Float) {
    val step = 75f
    val lineColor = Color(0xFFFFD700).copy(alpha = alpha)

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

/**
 * Draws a 4-point celestial star sparkle flare
 */
private fun DrawScope.drawStarFlare(center: Offset, radius: Float, color: Color) {
    val path = Path().apply {
        // Top tip
        moveTo(center.x, center.y - radius)
        quadraticTo(center.x, center.y, center.x + radius * 0.25f, center.y)
        // Right tip
        quadraticTo(center.x, center.y, center.x + radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + radius * 0.25f)
        // Bottom tip
        quadraticTo(center.x, center.y, center.x, center.y + radius)
        quadraticTo(center.x, center.y, center.x - radius * 0.25f, center.y)
        // Left tip
        quadraticTo(center.x, center.y, center.x - radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - radius * 0.25f)
        close()
    }

    // Glow halo
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color, color.copy(alpha = color.alpha * 0.4f), Color.Transparent),
            center = center,
            radius = radius * 1.8f
        ),
        radius = radius * 1.8f,
        center = center
    )

    drawPath(path = path, color = color)
}

/**
 * Draws crisp vector card suit emblems (Spade, Heart, Diamond, Club) with outer neon glow
 */
private fun DrawScope.drawCardSuitIcon(
    suitType: SuitType,
    size: Float,
    color: Color,
    glowColor: Color
) {
    val half = size * 0.5f
    val center = Offset.Zero

    // Subtle radial glow behind the suit
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(glowColor, Color.Transparent),
            center = center,
            radius = size * 1.2f
        ),
        radius = size * 1.2f,
        center = center
    )

    when (suitType) {
        SuitType.DIAMOND -> {
            val path = Path().apply {
                moveTo(0f, -half)
                lineTo(half * 0.75f, 0f)
                lineTo(0f, half)
                lineTo(-half * 0.75f, 0f)
                close()
            }
            drawPath(path, color)
        }
        SuitType.HEART -> {
            val path = Path().apply {
                moveTo(0f, half * 0.8f)
                cubicTo(-half * 1.1f, 0f, -half * 0.9f, -half * 0.9f, 0f, -half * 0.35f)
                cubicTo(half * 0.9f, -half * 0.9f, half * 1.1f, 0f, 0f, half * 0.8f)
                close()
            }
            drawPath(path, color)
        }
        SuitType.SPADE -> {
            val path = Path().apply {
                moveTo(0f, -half * 0.85f)
                cubicTo(-half * 1.1f, 0f, -half * 0.9f, half * 0.6f, 0f, half * 0.25f)
                cubicTo(half * 0.9f, half * 0.6f, half * 1.1f, 0f, 0f, -half * 0.85f)
                close()
            }
            // Stem
            val stemPath = Path().apply {
                moveTo(-half * 0.22f, half * 0.85f)
                lineTo(half * 0.22f, half * 0.85f)
                lineTo(0f, half * 0.2f)
                close()
            }
            drawPath(path, color)
            drawPath(stemPath, color)
        }
        SuitType.CLUB -> {
            // Three circular lobes
            val lobeRadius = size * 0.24f
            drawCircle(color, lobeRadius, Offset(0f, -half * 0.38f))
            drawCircle(color, lobeRadius, Offset(-half * 0.38f, half * 0.12f))
            drawCircle(color, lobeRadius, Offset(half * 0.38f, half * 0.12f))
            // Center circle to fill gap
            drawCircle(color, lobeRadius * 0.8f, Offset(0f, 0f))
            // Stem
            val stemPath = Path().apply {
                moveTo(-half * 0.22f, half * 0.85f)
                lineTo(half * 0.22f, half * 0.85f)
                lineTo(0f, 0f)
                close()
            }
            drawPath(stemPath, color)
        }
    }
}
