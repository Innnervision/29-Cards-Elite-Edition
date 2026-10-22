package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import com.example.game.model.VfxEvent
import com.example.game.model.VfxType
import com.example.game.performance.QualityProfile
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * AAA Mobile Game Particle Effect Shapes
 */
enum class ParticleShape {
    CIRCLE_GLOW,
    STAR_SPARKLE,
    GOLD_COIN_PIP,
    FLUTTER_CONFETTI,
    ENERGY_EMBER
}

/**
 * Memory-efficient, pooled Particle data model.
 * Reused across effect triggers without garbage collection overhead.
 */
class PooledParticle {
    var active: Boolean = false
    var startX: Float = 0.5f
    var startY: Float = 0.5f
    var velocityX: Float = 0f
    var velocityY: Float = 0f
    var gravity: Float = 250f
    var drag: Float = 0.98f
    var color: Color = Color(0xFFFFD700)
    var size: Float = 6f
    var initialRotation: Float = 0f
    var rotationSpeed: Float = 0f
    var shape: ParticleShape = ParticleShape.CIRCLE_GLOW
    var pulseOffset: Float = 0f

    fun reset(
        x: Float,
        y: Float,
        vx: Float,
        vy: Float,
        g: Float,
        d: Float,
        c: Color,
        sz: Float,
        rot: Float,
        rotSpd: Float,
        shp: ParticleShape,
        pulse: Float
    ) {
        active = true
        startX = x
        startY = y
        velocityX = vx
        velocityY = vy
        gravity = g
        drag = d
        color = c
        size = sz
        initialRotation = rot
        rotationSpeed = rotSpd
        shape = shp
        pulseOffset = pulse
    }

    fun release() {
        active = false
    }
}

/**
 * High-performance Object Pool for particles.
 * Preallocates up to maximum 120 particles to eliminate runtime memory allocations during game matches.
 */
class ParticleObjectPool(capacity: Int = 120) {
    private val pool = Array(capacity) { PooledParticle() }
    private var inUseCount = 0

    fun acquire(): PooledParticle? {
        for (i in 0 until pool.size) {
            if (!pool[i].active) {
                inUseCount++
                return pool[i]
            }
        }
        return null
    }

    fun releaseAll() {
        for (i in 0 until pool.size) {
            pool[i].release()
        }
        inUseCount = 0
    }

    val activeCount: Int get() = inUseCount
}

/**
 * Production-ready AAA Particle VFX View for 29 Card Game.
 * Dynamically scales particle budgets according to device QualityProfile:
 * - LOW: 12 particles (Max battery life, 0 thermal throttling)
 * - MEDIUM: 30 particles (Balanced 60 FPS)
 * - HIGH: 65 particles (Star sparkles, gold coins, rich confetti)
 * - ULTRA: 110 particles (Full 120Hz high-refresh fidelity)
 */
@Composable
fun AaaParticleVfxSystem(
    vfxEvent: VfxEvent?,
    qualityProfile: QualityProfile = QualityProfile.HIGH,
    modifier: Modifier = Modifier
) {
    if (vfxEvent == null) return

    val progress = remember(vfxEvent.timestamp) { Animatable(0f) }
    val particlePool = remember { ParticleObjectPool(120) }
    val activeParticles = remember { mutableStateListOf<PooledParticle>() }

    val particleBudget = when (qualityProfile) {
        QualityProfile.LOW -> 12
        QualityProfile.MEDIUM -> 30
        QualityProfile.HIGH -> 65
        QualityProfile.ULTRA -> 110
        QualityProfile.AUTO -> 50
    }

    LaunchedEffect(vfxEvent.timestamp) {
        particlePool.releaseAll()
        activeParticles.clear()

        val type = vfxEvent.type
        val colors = when (type) {
            VfxType.FIREWORKS_WIN -> listOf(
                Color(0xFFFFD700), Color(0xFFFF5722), Color(0xFF4CAF50),
                Color(0xFF38BDF8), Color(0xFFA855F7), Color(0xFFFACC15),
                Color(0xFFFFFFFF)
            )
            VfxType.TRUMP_REVEAL -> listOf(
                Color(0xFFFFD700), Color(0xFFFFB300), Color(0xFFFFF176),
                Color(0xFFFF8F00), Color(0xFFFFFFFF)
            )
            VfxType.PAIR_ROYALTY -> listOf(
                Color(0xFFFFD700), Color(0xFFF43F5E), Color(0xFFFB7185),
                Color(0xFFFDE047), Color(0xFFFFFFFF)
            )
            VfxType.HIGH_BID_ALERT -> listOf(
                Color(0xFFEF4444), Color(0xFFF97316), Color(0xFFFFD700)
            )
            VfxType.DEFEAT_GLOOM -> listOf(
                Color(0xFF64748B), Color(0xFF475569), Color(0xFF334155),
                Color(0xFF94A3B8)
            )
            else -> listOf(Color(0xFFFFD700), Color(0xFF38BDF8), Color.White)
        }

        val isDefeat = type == VfxType.DEFEAT_GLOOM
        val isTrump = type == VfxType.TRUMP_REVEAL
        val isWin = type == VfxType.FIREWORKS_WIN
        val isPair = type == VfxType.PAIR_ROYALTY

        val count = particleBudget.coerceAtMost(
            when (type) {
                VfxType.FIREWORKS_WIN -> 110
                VfxType.TRUMP_REVEAL -> 70
                VfxType.PAIR_ROYALTY -> 60
                VfxType.DEFEAT_GLOOM -> 35
                else -> 30
            }
        )

        for (i in 0 until count) {
            val p = particlePool.acquire() ?: break

            val angle = if (isDefeat) {
                Random.nextDouble(0.2 * Math.PI, 0.8 * Math.PI)
            } else if (isTrump) {
                // Radial outward starburst
                (i.toDouble() / count) * (2.0 * Math.PI) + (Random.nextDouble(-0.15, 0.15))
            } else {
                Random.nextDouble(0.0, 2.0 * Math.PI)
            }

            val speed = when {
                isDefeat -> Random.nextFloat() * 140f + 40f
                isWin -> Random.nextFloat() * 450f + 140f
                isTrump -> Random.nextFloat() * 380f + 100f
                else -> Random.nextFloat() * 260f + 80f
            }

            val shape = when {
                isDefeat -> ParticleShape.ENERGY_EMBER
                isWin && (i % 3 == 0) -> ParticleShape.GOLD_COIN_PIP
                isWin && (i % 3 == 1) -> ParticleShape.FLUTTER_CONFETTI
                isTrump && (i % 2 == 0) -> ParticleShape.STAR_SPARKLE
                isPair && (i % 2 == 0) -> ParticleShape.STAR_SPARKLE
                else -> ParticleShape.CIRCLE_GLOW
            }

            val size = when (shape) {
                ParticleShape.STAR_SPARKLE -> Random.nextFloat() * 8f + 6f
                ParticleShape.GOLD_COIN_PIP -> Random.nextFloat() * 7f + 5f
                ParticleShape.FLUTTER_CONFETTI -> Random.nextFloat() * 9f + 6f
                ParticleShape.ENERGY_EMBER -> Random.nextFloat() * 4f + 2f
                ParticleShape.CIRCLE_GLOW -> Random.nextFloat() * 5f + 3f
            }

            p.reset(
                x = 0.5f,
                y = if (isDefeat) 0.35f else 0.46f,
                vx = (cos(angle) * speed).toFloat(),
                vy = (sin(angle) * speed).toFloat(),
                g = if (isDefeat) 80f else if (isWin) 220f else 140f,
                d = if (isWin) 0.96f else 0.94f,
                c = colors.random(),
                sz = size,
                rot = Random.nextFloat() * 360f,
                rotSpd = Random.nextFloat() * 720f - 360f,
                shp = shape,
                pulse = Random.nextFloat() * 6.28f
            )

            activeParticles.add(p)
        }

        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = if (isWin) 1400 else 1000,
                easing = FastOutSlowInEasing
            )
        )
        particlePool.releaseAll()
        activeParticles.clear()
    }

    if (progress.value in 0.001f..0.999f && activeParticles.isNotEmpty()) {
        val p = progress.value
        val alphaFade = (1f - p * p).coerceIn(0f, 1f)

        Canvas(
            modifier = modifier
                .fillMaxSize()
                .graphicsLayer {
                    this.alpha = alphaFade
                }
        ) {
            val cx = size.width * 0.5f
            val cy = size.height * 0.46f

            for (particle in activeParticles) {
                // Apply physics integration with drag and gravity
                val time = p * 1.2f
                val currentX = cx + (particle.velocityX * time * particle.drag)
                val currentY = cy + (particle.velocityY * time * particle.drag) + (0.5f * particle.gravity * time * time)
                val currentRotation = particle.initialRotation + (particle.rotationSpeed * p)

                val particleColor = particle.color.copy(alpha = alphaFade)

                when (particle.shape) {
                    ParticleShape.STAR_SPARKLE -> {
                        drawStarSparkle(
                            center = Offset(currentX, currentY),
                            radius = particle.size * (1f - p * 0.3f),
                            color = particleColor,
                            rotation = currentRotation
                        )
                    }
                    ParticleShape.GOLD_COIN_PIP -> {
                        drawGoldCoin(
                            center = Offset(currentX, currentY),
                            radius = particle.size,
                            color = particleColor,
                            rotation = currentRotation,
                            flipProgress = (p * 4f + particle.pulseOffset) % 1f
                        )
                    }
                    ParticleShape.FLUTTER_CONFETTI -> {
                        drawConfettiRibbon(
                            center = Offset(currentX, currentY),
                            width = particle.size * 1.5f,
                            height = particle.size * 0.7f,
                            color = particleColor,
                            rotation = currentRotation,
                            flipProgress = (p * 6f + particle.pulseOffset) % 1f
                        )
                    }
                    ParticleShape.ENERGY_EMBER -> {
                        drawCircle(
                            color = particleColor,
                            radius = particle.size * (1f - p * 0.5f),
                            center = Offset(currentX, currentY)
                        )
                    }
                    ParticleShape.CIRCLE_GLOW -> {
                        // Core glow
                        drawCircle(
                            color = particleColor,
                            radius = particle.size * (1f - p * 0.4f),
                            center = Offset(currentX, currentY)
                        )
                        // Soft halo outer glow on High/Ultra profiles
                        if (qualityProfile.enableAmbientGlow) {
                            drawCircle(
                                color = particleColor.copy(alpha = alphaFade * 0.3f),
                                radius = particle.size * 2.2f * (1f - p * 0.4f),
                                center = Offset(currentX, currentY)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Draws a geometric 4-point diamond star sparkle with inner highlight.
 */
private fun DrawScope.drawStarSparkle(
    center: Offset,
    radius: Float,
    color: Color,
    rotation: Float
) {
    rotate(degrees = rotation, pivot = center) {
        val path = Path().apply {
            moveTo(center.x, center.y - radius * 1.4f)
            quadraticTo(center.x, center.y, center.x + radius * 1.4f, center.y)
            quadraticTo(center.x, center.y, center.x, center.y + radius * 1.4f)
            quadraticTo(center.x, center.y, center.x - radius * 1.4f, center.y)
            quadraticTo(center.x, center.y, center.x, center.y - radius * 1.4f)
            close()
        }
        drawPath(path = path, color = color, style = Fill)

        // Center bright highlight pip
        drawCircle(
            color = Color.White.copy(alpha = color.alpha),
            radius = radius * 0.35f,
            center = center
        )
    }
}

/**
 * Draws a 3D spinning gold coin pip that flips along its vertical axis.
 */
private fun DrawScope.drawGoldCoin(
    center: Offset,
    radius: Float,
    color: Color,
    rotation: Float,
    flipProgress: Float
) {
    val scaleX = cos(flipProgress * 2.0 * Math.PI).toFloat().coerceIn(-1f, 1f)
    val width = radius * kotlin.math.abs(scaleX)

    rotate(degrees = rotation, pivot = center) {
        // Outer coin edge
        drawOval(
            color = Color(0xFFD97706).copy(alpha = color.alpha),
            topLeft = Offset(center.x - width, center.y - radius),
            size = Size(width * 2f, radius * 2f)
        )
        // Inner gold surface
        drawOval(
            color = color,
            topLeft = Offset(center.x - width * 0.75f, center.y - radius * 0.75f),
            size = Size(width * 1.5f, radius * 1.5f)
        )
    }
}

/**
 * Draws a fluttering confetti ribbon that flips and twists in 3D perspective.
 */
private fun DrawScope.drawConfettiRibbon(
    center: Offset,
    width: Float,
    height: Float,
    color: Color,
    rotation: Float,
    flipProgress: Float
) {
    val scaleY = sin(flipProgress * 2.0 * Math.PI).toFloat()
    val scaledHeight = height * kotlin.math.abs(scaleY)

    rotate(degrees = rotation, pivot = center) {
        drawRect(
            color = if (scaleY >= 0f) color else color.copy(alpha = color.alpha * 0.75f),
            topLeft = Offset(center.x - width * 0.5f, center.y - scaledHeight * 0.5f),
            size = Size(width, scaledHeight.coerceAtLeast(1.5f))
        )
    }
}
