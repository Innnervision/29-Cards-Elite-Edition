package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class LoadingEmber(
    val xRatio: Float,
    val initialY: Float,
    val speed: Float,
    val size: Float,
    val alpha: Float,
    val phaseOffset: Float
)

@Composable
fun LoadingSplashScreen(
    onLoadingComplete: () -> Unit
) {
    var rawProgress by remember { mutableFloatStateOf(0f) }
    var statusText by remember { mutableStateOf("Shuffling 32-card Royal 29 deck...") }
    var tipIndex by remember { mutableIntStateOf(0) }
    var isReadyToEnter by remember { mutableStateOf(false) }

    val tips = remember {
        listOf(
            "🃏 Scoring: Jack = 3 Pts, Nine = 2 Pts, Ace & Ten = 1 Pt each (Total 28 in deck).",
            "🏆 The final trick is worth +1 extra point, making 29 total points possible in every round!",
            "👑 Pair / Royalty: Holding King + Queen of Trump declares +4 points to your bid.",
            "🎯 Bidding starts at 16 and goes up to 28 (or 29 for high-stakes Single Hand).",
            "🧠 South & North play as a partnership against East & West. Communicate through tricks!"
        )
    }

    // Smooth animated progress
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "animated_loading_progress"
    )

    // Staggered card dealing fan progress
    val fanAnim = remember { Animatable(0f) }

    // Loading stages sequence
    LaunchedEffect(Unit) {
        // Step 1: Start card fan
        fanAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )

        // Step 2: Deck & engine initialization
        rawProgress = 0.22f
        statusText = "Dealing 32-card South Asian deck (J, 9, A, 10, K, Q, 8, 7)..."
        delay(450)

        // Step 3: AI Neural bidding networks
        rawProgress = 0.48f
        statusText = "Calibrating Grandmaster AI partnership strategies..."
        tipIndex = 1
        delay(500)

        // Step 4: High-Stakes Casino tables & brackets
        rawProgress = 0.74f
        statusText = "Assembling High-Stakes Casino Tables & Tournament Brackets..."
        tipIndex = 2
        delay(480)

        // Step 5: Royal Trump & Diamond Vault
        rawProgress = 0.92f
        statusText = "Synchronizing Diamond Vault & Player VIP Ranks..."
        tipIndex = 3
        delay(420)

        // Step 6: Ready!
        rawProgress = 1.0f
        statusText = "Arena Ready! Welcome to Royal 29."
        tipIndex = 4
        isReadyToEnter = true
        delay(600)

        onLoadingComplete()
    }

    // Infinite transitions for ambient casino life
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_casino_fx")

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    val haloRotate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "halo_rotate"
    )

    val sweepShimmer by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_shimmer"
    )

    // Generate embers for background canvas
    val embers = remember {
        List(35) {
            LoadingEmber(
                xRatio = Random.nextFloat(),
                initialY = Random.nextFloat(),
                speed = Random.nextFloat() * 0.08f + 0.04f,
                size = Random.nextFloat() * 4.5f + 1.5f,
                alpha = Random.nextFloat() * 0.5f + 0.3f,
                phaseOffset = Random.nextFloat() * 6.28f
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF023626), // Rich Emerald Velvet
                        Color(0xFF062016), // Dark Casino Green
                        Color(0xFF0B1322), // Midnight Slate
                        Color(0xFF020617)  // Deep Obsidian
                    ),
                    radius = 1400f
                )
            )
            .clickable(
                enabled = isReadyToEnter,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onLoadingComplete()
            }
            .testTag("loading_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Dynamic Canvas: Rising Golden Embers + Celestial Rotating Watermark Ring
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f

            // 1. Rotating Decorative Celestial Ring
            rotate(haloRotate, pivot = Offset(cx, cy)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFD700).copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = size.minDimension * 0.55f
                    )
                )
            }

            // 2. Rising Golden Casino Dust Particles
            val t = sweepShimmer
            for (ember in embers) {
                val currentY = ((ember.initialY - (t * ember.speed * 2f)) % 1f + 1f) % 1f
                val waveX = sin(currentY * 8f + ember.phaseOffset) * 16f
                val px = (ember.xRatio * size.width) + waveX
                val py = currentY * size.height

                drawCircle(
                    color = Color(0xFFFFD700).copy(alpha = (ember.alpha * (1f - currentY * 0.4f)).coerceIn(0f, 1f)),
                    radius = ember.size,
                    center = Offset(px, py)
                )
            }
        }

        // Main Responsive Landscape Layout
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // LEFT COLUMN: Grand 4-Card 3D Fan with Points Badges
            Box(
                modifier = Modifier
                    .weight(0.48f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                val fan = fanAnim.value

                // 4 Core 29 Cards Fan-out
                Box(
                    modifier = Modifier
                        .scale(0.92f + (0.08f * fan))
                        .offset(y = (-4).dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Jack of Spades (3 Points - The Kingmaker)
                    AnimatedLoadingCard(
                        rank = "J",
                        suit = "♠",
                        points = "3 PTS",
                        suitColor = Color(0xFF0F172A),
                        angle = -24f * fan,
                        offsetX = (-64 * fan).dp,
                        offsetY = (10 * (1f - fan)).dp,
                        shimmerPhase = sweepShimmer,
                        isKeyCard = true
                    )

                    // Nine of Hearts (2 Points - The Second Power)
                    AnimatedLoadingCard(
                        rank = "9",
                        suit = "♥",
                        points = "2 PTS",
                        suitColor = Color(0xFFDC2626),
                        angle = -8f * fan,
                        offsetX = (-22 * fan).dp,
                        offsetY = (-6 * fan).dp,
                        shimmerPhase = sweepShimmer,
                        isKeyCard = true
                    )

                    // Ace of Diamonds (1 Point - Trick Winner)
                    AnimatedLoadingCard(
                        rank = "A",
                        suit = "♦",
                        points = "1 PT",
                        suitColor = Color(0xFFDC2626),
                        angle = 8f * fan,
                        offsetX = (22 * fan).dp,
                        offsetY = (-6 * fan).dp,
                        shimmerPhase = sweepShimmer,
                        isKeyCard = false
                    )

                    // Ten of Clubs (1 Point - Trump Anchor)
                    AnimatedLoadingCard(
                        rank = "10",
                        suit = "♣",
                        points = "1 PT",
                        suitColor = Color(0xFF0F172A),
                        angle = 24f * fan,
                        offsetX = (64 * fan).dp,
                        offsetY = (10 * (1f - fan)).dp,
                        shimmerPhase = sweepShimmer,
                        isKeyCard = false
                    )
                }

                // Subtitle underneath cards
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A).copy(alpha = 0.85f))
                        .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "THE 4 POWER CARDS • 28 PTS TOTAL",
                        color = Color(0xFFFFD700),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // RIGHT COLUMN: Title, Dynamic Progress, Tips & Call to Action
            Column(
                modifier = Modifier
                    .weight(0.52f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header: Crown Emblem & Title
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("👑", fontSize = 18.sp, modifier = Modifier.scale(pulseGlow))
                        Text(
                            text = "ROYAL GRANDMASTER ARENA",
                            color = Color(0xFFFFD700),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        Text("👑", fontSize = 18.sp, modifier = Modifier.scale(pulseGlow))
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Grand 29 Metallic Badge
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF062D1F).copy(alpha = 0.95f),
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp,
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFFFFD700),
                                    Color(0xFFFFFBEB),
                                    Color(0xFFF59E0B),
                                    Color(0xFFFFD700)
                                )
                            )
                        ),
                        modifier = Modifier.shadow(16.dp, RoundedCornerShape(18.dp), spotColor = Color(0xFFFFD700))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "29",
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Serif,
                                color = Color(0xFFFFD700),
                                style = androidx.compose.ui.text.TextStyle(
                                    shadow = androidx.compose.ui.graphics.Shadow(
                                        color = Color(0xFFB45309),
                                        offset = Offset(2f, 3f),
                                        blurRadius = 6f
                                    )
                                )
                            )
                            Column {
                                Text(
                                    text = "TWENTY NINE",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    letterSpacing = 2.5.sp
                                )
                                Text(
                                    text = "AUTHENTIC SOUTH ASIAN RULES",
                                    fontSize = 8.5.sp,
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }
                    }
                }

                // Middle: Rotating Strategy Tip Box
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.92f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        AnimatedContent(
                            targetState = tips[tipIndex % tips.size],
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(300)) + slideInVertically(animationSpec = tween(300)) { it / 2 })
                                    .togetherWith(fadeOut(animationSpec = tween(200)) + slideOutVertically(animationSpec = tween(200)) { -it / 2 })
                            },
                            label = "tip_crossfade"
                        ) { tipText ->
                            Text(
                                text = tipText,
                                color = Color(0xFFF1F5F9),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                // Bottom: Dynamic Loading Bar & Live Status
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Status & Percentage
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isReadyToEnter) Color(0xFF4ADE80) else Color(0xFF38BDF8))
                            )
                            Text(
                                text = statusText,
                                color = if (isReadyToEnter) Color(0xFF4ADE80) else Color(0xFF38BDF8),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            color = Color(0xFFFFD700),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Progress Bar with Animated Gradient & Neon Tip
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(6.dp))
                    ) {
                        // Filled progress
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFF0284C7),
                                            Color(0xFF38BDF8),
                                            Color(0xFFFFD700),
                                            Color(0xFFFBBF24)
                                        )
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Ready CTA Button or Automatic Fade
                    if (isReadyToEnter) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFD700),
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .scale(pulseGlow)
                                .clickable { onLoadingComplete() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "ENTER ARENA",
                                    color = Color(0xFF0F172A),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = Color(0xFF0F172A),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Loading game assets...",
                            color = Color(0xFF64748B),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * High-craft luxury 3D animated playing card for the loading screen fan.
 */
@Composable
private fun AnimatedLoadingCard(
    rank: String,
    suit: String,
    points: String,
    suitColor: Color,
    angle: Float,
    offsetX: androidx.compose.ui.unit.Dp,
    offsetY: androidx.compose.ui.unit.Dp,
    shimmerPhase: Float,
    isKeyCard: Boolean
) {
    Surface(
        modifier = Modifier
            .offset(x = offsetX, y = offsetY)
            .rotate(angle)
            .size(width = 62.dp, height = 92.dp)
            .shadow(12.dp, RoundedCornerShape(8.dp), spotColor = if (isKeyCard) Color(0xFFFFD700) else Color.Black)
            .border(
                1.2.dp,
                if (isKeyCard) Color(0xFFFFD700) else Color(0xFFCBD5E1),
                RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFFAFAFA)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Corner Pip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = rank,
                        color = suitColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = suit,
                        color = suitColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Center Grand Suit Graphic
                Text(
                    text = suit,
                    color = suitColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.offset(y = (-2).dp)
                )

                // Bottom Point Badge Pill
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isKeyCard) Color(0xFF0F172A) else Color(0xFF334155),
                    border = androidx.compose.foundation.BorderStroke(
                        0.8.dp,
                        if (isKeyCard) Color(0xFFFFD700) else Color(0xFF94A3B8)
                    )
                ) {
                    Text(
                        text = points,
                        color = if (isKeyCard) Color(0xFFFFD700) else Color.White,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

