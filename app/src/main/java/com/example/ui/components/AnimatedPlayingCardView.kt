package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.Card
import com.example.game.model.Rank
import com.example.game.model.Suit
import kotlinx.coroutines.launch

/**
 * Reusable, high-performance Jetpack Compose Playing Card Component.
 * Supports:
 * - Fluid animations for dealing cards (staggered entry glide from deck with physics spring)
 * - Fluid animations for playing cards (throw flick into center trick arena)
 * - Full suit and rank iconography (Hearts, Diamonds, Clubs, Spades with crisp vector pips)
 * - High-value point badges (+3 Jack, +2 Nine, +1 Ace/Ten)
 * - Trump glow, selection lift, swipe-to-play gesture support
 * - Strict 60fps/120fps frame control via RenderThread graphicsLayer transforms
 */
@Composable
fun AnimatedPlayingCard(
    card: Card?,
    modifier: Modifier = Modifier,
    isFaceUp: Boolean = true,
    isPlayable: Boolean = true,
    isSelected: Boolean = false,
    isTrump: Boolean = false,
    isHinted: Boolean = false,
    skinId: String = "CLASSIC_ROYAL",
    width: Dp = 66.dp,
    height: Dp = 94.dp,
    rotationZ: Float = 0f,
    dealIndex: Int = 0,
    animateDeal: Boolean = false,
    animatePlay: Boolean = false,
    onClick: (() -> Unit)? = null,
    onSwipeUpPlay: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()

    // Dealing entry animation values (glide from deck with alpha and scale)
    val dealProgress = remember { Animatable(if (animateDeal) 0f else 1f) }
    LaunchedEffect(animateDeal, card?.id) {
        if (animateDeal) {
            dealProgress.snapTo(0f)
            dealProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 350,
                    delayMillis = dealIndex * 60,
                    easing = FastOutSlowInEasing
                )
            )
        } else {
            dealProgress.snapTo(1f)
        }
    }

    // Interactive Drag Gesture Offsets for 3D tactile tilt & swipe-to-play
    var dragOffsetX by remember { mutableFloatStateOf(0f) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    // Lift offset when selected or hinted
    val liftOffset by animateFloatAsState(
        targetValue = if (isSelected) -22f else if (isHinted) -12f else dragOffsetY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "card_lift_offset"
    )

    // Dynamic 3D tactile tilt based on touch drag
    val tiltX = (-dragOffsetY * 0.14f).coerceIn(-14f, 14f)
    val tiltY = (dragOffsetX * 0.14f).coerceIn(-14f, 14f)

    // Scale animation
    val scaleAnim by animateFloatAsState(
        targetValue = if (isSelected) 1.12f else if (isHinted) 1.06f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "card_scale_anim"
    )

    // Pulsing aura for active trump or playable turn
    val infiniteTransition = rememberInfiniteTransition(label = "card_aura")
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "card_glow"
    )

    // Specular holographic sheen translation for premium card finish
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -120f,
        targetValue = 220f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "card_shimmer"
    )

    val currentDealProg = dealProgress.value
    val dealYOffset = (1f - currentDealProg) * 70f
    val dealScale = 0.6f + (0.4f * currentDealProg)
    val dealAlpha = currentDealProg.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .graphicsLayer {
                // Hardware accelerated layer for 60/120 FPS frame control with physical tilt
                translationY = liftOffset + dealYOffset
                scaleX = scaleAnim * dealScale
                scaleY = scaleAnim * dealScale
                this.rotationZ = rotationZ
                this.rotationX = tiltX
                this.rotationY = tiltY
                alpha = dealAlpha
                cameraDistance = 16f * density
                shadowElevation = if (isSelected) 16.dp.toPx() else 6.dp.toPx()
            }
            .shadow(
                elevation = if (isSelected) 14.dp else 4.dp,
                shape = RoundedCornerShape(8.dp),
                ambientColor = if (isTrump) Color(0xFFFFD700) else Color.Black.copy(alpha = 0.5f),
                spotColor = if (isTrump) Color(0xFFFFC107) else Color.Black
            )
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (isPlayable && onSwipeUpPlay != null) {
                    Modifier.pointerInput(card?.id) {
                        detectDragGestures(
                            onDragEnd = {
                                if (dragOffsetY < -45f) {
                                    onSwipeUpPlay()
                                }
                                dragOffsetX = 0f
                                dragOffsetY = 0f
                            },
                            onDragCancel = {
                                dragOffsetX = 0f
                                dragOffsetY = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffsetX = (dragOffsetX + dragAmount.x).coerceIn(-40f, 40f)
                                dragOffsetY = (dragOffsetY + dragAmount.y).coerceAtMost(0f)
                            }
                        )
                    }
                } else Modifier
            )
            .then(
                if (onClick != null && isPlayable) {
                    Modifier.clickable(onClick = onClick)
                } else Modifier
            )
            .testTag("animated_card_${card?.id ?: "back"}")
    ) {
        if (!isFaceUp || card == null) {
            CardBackView(skinId = skinId)
        } else {
            CardFaceView(
                card = card,
                isPlayable = isPlayable,
                isSelected = isSelected,
                isTrump = isTrump,
                isHinted = isHinted,
                glowAlpha = glowIntensity,
                shimmerOffset = shimmerOffset
            )
        }
    }
}

/**
 * Card Face View with Authentic Royal Indian 29 Iconography:
 * Crisp suit vector iconography, points indicator, and corner ranks.
 */
@Composable
private fun CardFaceView(
    card: Card,
    isPlayable: Boolean,
    isSelected: Boolean,
    isTrump: Boolean,
    isHinted: Boolean,
    glowAlpha: Float,
    shimmerOffset: Float = 0f
) {
    val suitColor = if (card.suit.isRed) Color(0xFFDC2626) else Color(0xFF0F172A)
    val cardBg = remember {
        Brush.verticalGradient(
            listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF8FAFC),
                Color(0xFFEDE9FE).copy(alpha = 0.20f)
            )
        )
    }

    val isHighValue = card.rank == Rank.JACK || card.rank == Rank.NINE

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(cardBg)
            .border(
                width = if (isSelected) 2.5.dp else if (isHinted) 2.dp else if (isTrump) 2.dp else 1.dp,
                brush = when {
                    isSelected -> Brush.sweepGradient(listOf(Color(0xFFFFD700), Color(0xFFFF9800), Color(0xFFFFD700)))
                    isHinted -> Brush.sweepGradient(listOf(Color(0xFF38BDF8), Color(0xFF818CF8), Color(0xFF38BDF8)))
                    isTrump -> Brush.linearGradient(listOf(Color(0xFFFFD700).copy(alpha = glowAlpha), Color(0xFFB8860B)))
                    isPlayable -> Brush.verticalGradient(listOf(Color(0xFFCBD5E1), Color(0xFF94A3B8)))
                    else -> Brush.verticalGradient(listOf(Color(0xFF475569).copy(alpha = 0.35f), Color(0xFF334155).copy(alpha = 0.35f)))
                },
                shape = RoundedCornerShape(8.dp)
            )
            .padding(3.dp)
    ) {
        // Top-Left Rank & Crisp Suit Pip
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Text(
                text = card.rank.symbol,
                color = suitColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 12.sp
            )
            CardSuitIcon(
                suit = card.suit,
                size = 10.dp,
                color = suitColor
            )
        }

        // Center Suit Emblem & Points Indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.align(Alignment.Center)
        ) {
            CardSuitIcon(
                suit = card.suit,
                size = if (isHighValue) 26.dp else 22.dp,
                color = suitColor
            )

            if (card.points > 0) {
                Spacer(modifier = Modifier.height(1.dp))
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                if (card.rank == Rank.JACK) listOf(Color(0xFFB45309), Color(0xFFFFD700))
                                else if (card.rank == Rank.NINE) listOf(Color(0xFF0F766E), Color(0xFF14B8A6))
                                else listOf(Color(0xFF1E293B), Color(0xFF334155))
                            ),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 3.dp, vertical = 0.5.dp)
                ) {
                    Text(
                        text = "+${card.points}",
                        color = if (card.rank == Rank.JACK) Color(0xFF0F172A) else Color.White,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 7.5.sp
                    )
                }
            }
        }

        // Bottom-Right Rank & Suit (Inverted)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .graphicsLayer { rotationZ = 180f }
        ) {
            Text(
                text = card.rank.symbol,
                color = suitColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 12.sp
            )
            CardSuitIcon(
                suit = card.suit,
                size = 10.dp,
                color = suitColor
            )
        }

        // Trump Crown Indicator Badge
        if (isTrump) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(14.dp)
                    .background(Color(0xFFFFD700), CircleShape)
                    .border(0.8.dp, Color(0xFFB45309), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "★",
                    color = Color(0xFF78350F),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Holographic Specular Foil Sheen Sweep for tactile card finish
        if (isSelected || isTrump || isHinted) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val sheenBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = if (isSelected) 0.35f else 0.18f),
                        Color(0xFFFFD700).copy(alpha = if (isTrump) 0.28f else 0.14f),
                        Color.Transparent
                    ),
                    start = Offset(shimmerOffset, -40f),
                    end = Offset(shimmerOffset + 70f, size.height + 40f)
                )
                drawRect(sheenBrush)
            }
        }
    }
}

/**
 * Proper Suit Iconography rendered with precision Canvas geometry:
 * Hearts, Diamonds, Clubs, Spades.
 */
@Composable
fun CardSuitIcon(
    suit: Suit,
    size: Dp,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        when (suit) {
            Suit.HEARTS -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.82f)
                    cubicTo(w * 0.15f, h * 0.55f, 0f, h * 0.32f, 0f, h * 0.20f)
                    cubicTo(0f, h * 0.05f, w * 0.20f, 0f, w * 0.5f, h * 0.24f)
                    cubicTo(w * 0.80f, 0f, w, h * 0.05f, w, h * 0.20f)
                    cubicTo(w, h * 0.32f, w * 0.85f, h * 0.55f, w * 0.5f, h * 0.82f)
                    close()
                }
                drawPath(path, color)
            }
            Suit.DIAMONDS -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, 0f)
                    lineTo(w * 0.96f, h * 0.5f)
                    lineTo(w * 0.5f, h)
                    lineTo(w * 0.04f, h * 0.5f)
                    close()
                }
                drawPath(path, color)
            }
            Suit.SPADES -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, 0f)
                    cubicTo(w * 0.85f, h * 0.35f, w, h * 0.52f, w, h * 0.65f)
                    cubicTo(w, h * 0.80f, w * 0.70f, h * 0.85f, w * 0.5f, h * 0.72f)
                    cubicTo(w * 0.30f, h * 0.85f, 0f, h * 0.80f, 0f, h * 0.65f)
                    cubicTo(0f, h * 0.52f, w * 0.15f, h * 0.35f, w * 0.5f, 0f)
                    close()
                }
                drawPath(path, color)
                // Stem
                val stemPath = Path().apply {
                    moveTo(w * 0.44f, h * 0.68f)
                    lineTo(w * 0.35f, h)
                    lineTo(w * 0.65f, h)
                    lineTo(w * 0.56f, h * 0.68f)
                    close()
                }
                drawPath(stemPath, color)
            }
            Suit.CLUBS -> {
                val r = w * 0.23f
                // Top Circle
                drawCircle(color, radius = r, center = Offset(w * 0.5f, h * 0.28f))
                // Left Circle
                drawCircle(color, radius = r, center = Offset(w * 0.27f, h * 0.55f))
                // Right Circle
                drawCircle(color, radius = r, center = Offset(w * 0.73f, h * 0.55f))
                // Center infill
                drawCircle(color, radius = r * 0.8f, center = Offset(w * 0.5f, h * 0.48f))
                // Stem
                val stemPath = Path().apply {
                    moveTo(w * 0.44f, h * 0.50f)
                    lineTo(w * 0.35f, h)
                    lineTo(w * 0.65f, h)
                    lineTo(w * 0.56f, h * 0.50f)
                    close()
                }
                drawPath(stemPath, color)
            }
        }
    }
}

/**
 * Animated Card Play Throw Effect:
 * Animates a card smoothly from player's hand position to center trick arena.
 */
@Composable
fun AnimatedCardThrow(
    card: Card,
    startPosition: Offset,
    targetPosition: Offset,
    onAnimationEnd: () -> Unit
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 350,
                easing = FastOutSlowInEasing
            )
        )
        onAnimationEnd()
    }

    val currentProg = progress.value
    val currentX = startPosition.x + (targetPosition.x - startPosition.x) * currentProg
    val currentY = startPosition.y + (targetPosition.y - startPosition.y) * currentProg
    val currentScale = 1f - (0.15f * currentProg)
    val currentRotation = (1f - currentProg) * -15f

    Box(
        modifier = Modifier
            .offset(x = currentX.dp, y = currentY.dp)
            .graphicsLayer {
                scaleX = currentScale
                scaleY = currentScale
                rotationZ = currentRotation
                alpha = 1f
            }
    ) {
        AnimatedPlayingCard(
            card = card,
            isFaceUp = true,
            isPlayable = false,
            width = 54.dp,
            height = 78.dp
        )
    }
}
