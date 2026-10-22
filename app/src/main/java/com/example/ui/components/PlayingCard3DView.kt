package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.model.Card
import com.example.game.model.Rank
import com.example.game.model.Suit

@Composable
fun PlayingCard3DView(
    card: Card?,
    modifier: Modifier = Modifier,
    isFaceUp: Boolean = true,
    isPlayable: Boolean = true,
    isSelected: Boolean = false,
    isTrump: Boolean = false,
    isHinted: Boolean = false,
    skinId: String = "CLASSIC_ROYAL",
    width: Dp = 68.dp,
    height: Dp = 96.dp,
    rotationZ: Float = 0f,
    onClick: (() -> Unit)? = null,
    onSwipeUpPlay: (() -> Unit)? = null
) {
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    val liftOffset by animateFloatAsState(
        targetValue = if (isSelected) -20f else if (isHinted) -10f else dragOffsetY,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 450f),
        label = "card_lift"
    )

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.10f else if (isHinted) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 450f),
        label = "card_scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "playable_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .graphicsLayer {
                translationY = liftOffset
                scaleX = scale
                scaleY = scale
                this.rotationZ = rotationZ
                cameraDistance = 14f * density
                shadowElevation = if (isSelected) 20.dp.toPx() else 8.dp.toPx()
            }
            .shadow(
                elevation = if (isSelected) 14.dp else 4.dp,
                shape = RoundedCornerShape(8.dp),
                ambientColor = if (isHinted || (isPlayable && isSelected)) Color(0xFFFFD700) else Color.Black.copy(alpha = 0.6f),
                spotColor = if (isTrump) Color(0xFFFFC107) else Color.Black
            )
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (isPlayable && onSwipeUpPlay != null) {
                    Modifier.pointerInput(card?.id) {
                        detectDragGestures(
                            onDragEnd = {
                                if (dragOffsetY < -50f) {
                                    onSwipeUpPlay()
                                }
                                dragOffsetY = 0f
                            },
                            onDragCancel = { dragOffsetY = 0f },
                            onDrag = { change, dragAmount ->
                                change.consume()
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
            .testTag("card_${card?.id ?: "back"}")
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
                glowAlpha = glowAlpha
            )
        }
    }
}

@Composable
private fun CardFaceView(
    card: Card,
    isPlayable: Boolean,
    isSelected: Boolean,
    isTrump: Boolean,
    isHinted: Boolean,
    glowAlpha: Float
) {
    val suitColor = if (card.suit.isRed) Color(0xFFD32F2F) else Color(0xFF1E293B)
    val cardBg = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFF8FAFC),
            Color(0xFFEDE9FE).copy(alpha = 0.25f)
        )
    )

    val isHighValue = card.rank == Rank.JACK || card.rank == Rank.NINE

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(cardBg)
            .border(
                width = if (isSelected) 2.5.dp else if (isHinted) 2.dp else if (isTrump) 2.dp else 1.dp,
                brush = if (isSelected) {
                    Brush.sweepGradient(listOf(Color(0xFFFFD700), Color(0xFFFF9800), Color(0xFFFFD700)))
                } else if (isHinted) {
                    Brush.sweepGradient(listOf(Color(0xFF38BDF8), Color(0xFF818CF8), Color(0xFF38BDF8)))
                } else if (isTrump) {
                    Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFB8860B)))
                } else if (isPlayable) {
                    Brush.verticalGradient(listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1)))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFF64748B).copy(alpha = 0.4f), Color(0xFF475569).copy(alpha = 0.4f)))
                },
                shape = RoundedCornerShape(8.dp)
            )
            .padding(3.5.dp)
    ) {
        // Top-Left Rank & Suit
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Text(
                text = card.rank.symbol,
                color = suitColor,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 12.5.sp
            )
            Text(
                text = card.suit.symbol,
                color = suitColor,
                fontSize = 10.5.sp,
                lineHeight = 10.5.sp
            )
        }

        // Center Suit Emblem & Points Indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(
                text = card.suit.symbol,
                color = suitColor.copy(alpha = if (card.points > 0) 0.95f else 0.70f),
                fontSize = if (isHighValue) 25.sp else 21.sp,
                lineHeight = 25.sp
            )

            if (card.points > 0) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                if (card.rank == Rank.JACK) listOf(Color(0xFFB45309), Color(0xFFFFD700))
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
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 12.5.sp
            )
            Text(
                text = card.suit.symbol,
                color = suitColor,
                fontSize = 10.5.sp,
                lineHeight = 10.5.sp
            )
        }

        // Dim overlay if not playable
        if (!isPlayable) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f), RoundedCornerShape(8.dp))
            )
        }
    }
}

@Composable
internal fun CardBackView(skinId: String = "CLASSIC_ROYAL") {
    when (skinId) {
        "GOLDEN_MANDALA" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        1.5.dp,
                        Brush.sweepGradient(listOf(Color(0xFFFFD700), Color(0xFFB45309), Color(0xFFFFD700))),
                        RoundedCornerShape(8.dp)
                    )
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_cardback_royal_gold),
                    contentDescription = "Golden Mandala Card Back",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        "CYBER_NEON" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF083344), Color(0xFF020617))
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.linearGradient(listOf(Color(0xFF06B6D4), Color(0xFFD946EF), Color(0xFF06B6D4))),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 1.dp,
                            color = Color(0xFF06B6D4).copy(alpha = 0.7f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .background(Color(0xFF082F49)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "⚡29⚡",
                            color = Color(0xFF22D3EE),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "CYBER",
                            color = Color(0xFFE879F9),
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
            }
        }
        "CRIMSON_DRAGON" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF881337), Color(0xFF2A000D))
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFEF4444), Color(0xFFFFD700))),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 1.dp,
                            color = Color(0xFFFFD700).copy(alpha = 0.6f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .background(Color(0xFF4C0519)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "29",
                            color = Color(0xFFFFD700),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            text = "🐉 ROYAL",
                            color = Color(0xFFFECDD3),
                            fontSize = 6.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        "DIAMOND_LUXURY" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF0F172A), Color(0xFF020617))
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.sweepGradient(listOf(Color(0xFF38BDF8), Color(0xFFE0E7FF), Color(0xFF38BDF8))),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 1.dp,
                            color = Color(0xFF38BDF8).copy(alpha = 0.8f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .background(Color(0xFF0F172A)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "💎 29 💎",
                            color = Color(0xFF38BDF8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "DIAMOND VIP",
                            color = Color(0xFFFFD700),
                            fontSize = 6.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
        "MIDNIGHT_OBSIDIAN" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF1E1B4B), Color(0xFF020617))
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.sweepGradient(listOf(Color(0xFFA855F7), Color(0xFFEC4899), Color(0xFF6366F1), Color(0xFFA855F7))),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 1.dp,
                            color = Color(0xFFA855F7).copy(alpha = 0.7f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .background(Color(0xFF0B0F19)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🌌 29 🌌",
                            color = Color(0xFFC084FC),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "OBSIDIAN",
                            color = Color(0xFFE2E8F0),
                            fontSize = 6.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
        "SOLAR_ECLIPSE" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF451A03), Color(0xFF0F0B08))
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.sweepGradient(listOf(Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFFFFD700), Color(0xFFF59E0B))),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 1.dp,
                            color = Color(0xFFF59E0B).copy(alpha = 0.8f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .background(Color(0xFF1C130B)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "☀️ 29 ☀️",
                            color = Color(0xFFFBBF24),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "SOLAR ECLIPSE",
                            color = Color(0xFFFDE68A),
                            fontSize = 5.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
        "EMERALD_SERPENT" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF064E3B), Color(0xFF022C22))
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFFFFD700), Color(0xFF059669))),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 1.dp,
                            color = Color(0xFF10B981).copy(alpha = 0.7f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .background(Color(0xFF042F2E)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🐍 29 🐍",
                            color = Color(0xFF34D399),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "JADE COBRA",
                            color = Color(0xFFFFD700),
                            fontSize = 6.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
        "ROYAL_RAJPUTANA" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF831843), Color(0xFF500724))
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.sweepGradient(listOf(Color(0xFFFFD700), Color(0xFFF43F5E), Color(0xFFFFD700))),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 1.dp,
                            color = Color(0xFFFFD700).copy(alpha = 0.8f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .background(Color(0xFF4C0519)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🦚 29 🦚",
                            color = Color(0xFFFFD700),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "RAJPUTANA",
                            color = Color(0xFFFBCFE8),
                            fontSize = 6.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
        "PHANTOM_GHOST" -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF4C1D95), Color(0xFF0F0728))
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.sweepGradient(listOf(Color(0xFFC084FC), Color(0xFF22D3EE), Color(0xFFC084FC))),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 1.dp,
                            color = Color(0xFFC084FC).copy(alpha = 0.8f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .background(Color(0xFF2E1065)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "👻 29 👻",
                            color = Color(0xFFE879F9),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "PHANTOM",
                            color = Color(0xFF67E8F9),
                            fontSize = 6.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
        else -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF1E3A8A), Color(0xFF0F172A))
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            listOf(Color(0xFFD4AF37), Color(0xFF996515), Color(0xFFD4AF37))
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 1.dp,
                            color = Color(0xFFD4AF37).copy(alpha = 0.5f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .background(Color(0xFF172554)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "29",
                            color = Color(0xFFFFD700),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            text = "♠ ♥ ♦ ♣",
                            color = Color(0xFFE2E8F0).copy(alpha = 0.8f),
                            fontSize = 7.5.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}
