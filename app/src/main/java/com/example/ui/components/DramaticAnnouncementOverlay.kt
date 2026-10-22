package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.VfxEvent
import com.example.game.model.VfxType
import kotlinx.coroutines.delay

@Composable
fun DramaticAnnouncementOverlay(
    vfxEvent: VfxEvent?,
    modifier: Modifier = Modifier
) {
    if (vfxEvent == null) return

    var isVisible by remember(vfxEvent.timestamp) { mutableStateOf(true) }

    LaunchedEffect(vfxEvent.timestamp) {
        isVisible = true
        delay(2200)
        isVisible = false
    }

    val infiniteTransition = rememberInfiniteTransition(label = "banner_fx")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotate_angle"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(initialScale = 0.4f, animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f)) + fadeIn(),
        exit = scaleOut(targetScale = 1.2f) + fadeOut(animationSpec = tween(300)),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center
        ) {
            when (vfxEvent.type) {
                VfxType.TRUMP_REVEAL -> {
                    Surface(
                        modifier = Modifier
                            .scale(pulseScale)
                            .shadow(32.dp, RoundedCornerShape(24.dp))
                            .border(
                                3.dp,
                                Brush.sweepGradient(listOf(Color(0xFFFFD700), Color(0xFFFF5722), Color(0xFFFFD700))),
                                RoundedCornerShape(24.dp)
                            ),
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF0F172A).copy(alpha = 0.95f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 28.dp, vertical = 18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Trump Reveal",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "TRUMP SUIT REVEALED!",
                                    color = Color(0xFFFFD700),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Trump Reveal",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            if (vfxEvent.suit != null) {
                                Text(
                                    text = "${vfxEvent.suit.symbol} ${vfxEvent.suit.displayName.uppercase()}",
                                    color = if (vfxEvent.suit.isRed) Color(0xFFEF4444) else Color(0xFFF8FAFC),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }

                            Text(
                                text = vfxEvent.message ?: "Trump cards can now beat leading suit cards!",
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                VfxType.PAIR_ROYALTY -> {
                    Surface(
                        modifier = Modifier
                            .scale(pulseScale)
                            .graphicsLayer { rotationZ = rotateAngle }
                            .shadow(32.dp, RoundedCornerShape(24.dp))
                            .border(
                                3.dp,
                                Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFE11D48))),
                                RoundedCornerShape(24.dp)
                            ),
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF1E1B4B).copy(alpha = 0.95f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 28.dp, vertical = 18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "👑 ROYAL PAIR DECLARED! 👑",
                                color = Color(0xFFFFD700),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "King & Queen of Trump held!",
                                color = Color(0xFFF472B6),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Text(
                                text = vfxEvent.message ?: "Target bid adjusted by ±4 Points!",
                                color = Color(0xFFE2E8F0),
                                fontSize = 11.5.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                VfxType.HIGH_BID_ALERT -> {
                    Surface(
                        modifier = Modifier
                            .scale(pulseScale)
                            .shadow(32.dp, RoundedCornerShape(24.dp))
                            .border(
                                3.dp,
                                Brush.sweepGradient(listOf(Color(0xFFEF4444), Color(0xFFFFD700), Color(0xFFEF4444))),
                                RoundedCornerShape(24.dp)
                            ),
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF450A0A).copy(alpha = 0.95f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 28.dp, vertical = 18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Whatshot,
                                    contentDescription = "High Bid",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(26.dp)
                                )
                                Text(
                                    text = "🔥 HIGH STAKES BID! 🔥",
                                    color = Color(0xFFFFD700),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.5.sp
                                )
                            }
                            Text(
                                text = vfxEvent.message ?: "High challenge in play!",
                                color = Color(0xFFFEF08A),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }

                VfxType.FIREWORKS_WIN -> {
                    Surface(
                        modifier = Modifier
                            .scale(pulseScale)
                            .shadow(36.dp, RoundedCornerShape(24.dp))
                            .border(
                                3.dp,
                                Brush.sweepGradient(listOf(Color(0xFFFFD700), Color(0xFF22C55E), Color(0xFFFFD700))),
                                RoundedCornerShape(24.dp)
                            ),
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF064E3B).copy(alpha = 0.95f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Win",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "🏆 VICTORY! 🏆",
                                color = Color(0xFFFFD700),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Text(
                                text = vfxEvent.message ?: "Masterful play! Victory tokens earned!",
                                color = Color(0xFFBBF7D0),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                VfxType.DEFEAT_GLOOM -> {
                    Surface(
                        modifier = Modifier
                            .scale(pulseScale)
                            .shadow(32.dp, RoundedCornerShape(24.dp))
                            .border(
                                2.dp,
                                Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFF64748B), Color(0xFF1E293B))),
                                RoundedCornerShape(24.dp)
                            ),
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF0F172A).copy(alpha = 0.96f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Defeat",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "ROUND CONCLUDED",
                                color = Color(0xFFF87171),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Text(
                                text = vfxEvent.message ?: "Opponents took the contract. Regroup for next round!",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                else -> { /* Normal dealing or minor SFX */ }
            }
        }
    }
}
