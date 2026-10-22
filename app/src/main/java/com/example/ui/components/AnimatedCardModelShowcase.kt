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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.CardRarity
import com.example.game.model.CardSkin
import com.example.game.model.PlayerEconomy
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-end interactive 3D animated model showcase for card decks and skins in the Store.
 * Features:
 * - Glowing 3D pedestal stage with rotating neon rings and upward energy glow
 * - Dynamic floating physics (Y-bobbing) and gyro tilt
 * - Holographic sheen streak passing across card surface
 * - Full 3D drag-to-inspect gesture and smooth 3D flip between Back and Face
 * - Instant "SETUP IN MY GAMES" flow with clear active state confirmation
 */
@Composable
fun AnimatedCardModelShowcase(
    skin: CardSkin,
    economy: PlayerEconomy,
    onEquip: (CardSkin) -> Unit,
    onBuy: (CardSkin) -> Unit,
    onWatchAdForDiamonds: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFlipped by remember(skin.id) { mutableStateOf(false) }
    var manualTiltX by remember(skin.id) { mutableFloatStateOf(0f) }
    var manualTiltY by remember(skin.id) { mutableFloatStateOf(0f) }

    val flipRotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "showcase_flip"
    )

    val animatedManualTiltX by animateFloatAsState(
        targetValue = manualTiltX,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "manual_tilt_x"
    )
    val animatedManualTiltY by animateFloatAsState(
        targetValue = manualTiltY,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "manual_tilt_y"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "showcase_fx")

    // Idle floating bobbing
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )

    // Gentle continuous gyro roll
    val gyroPitch by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gyro_pitch"
    )

    // Holographic sheen streak progression (0 to 1)
    val sheenProgress by infiniteTransition.animateFloat(
        initialValue = -0.4f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheen_prog"
    )

    // Pedestal ring rotation
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rot"
    )

    val isUnlocked = economy.unlockedCardSkins.contains(skin.id) || skin.priceDiamonds == 0
    val isEquipped = economy.equippedCardSkin == skin.id
    val hasEnoughDiamonds = economy.diamonds >= skin.priceDiamonds
    val accentColor = Color(skin.accentColor)
    val rarityColor = Color(skin.rarity.color)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(24.dp, RoundedCornerShape(20.dp), spotColor = accentColor.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0B1120),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            Brush.sweepGradient(listOf(accentColor, rarityColor, Color(0xFFFFD700), accentColor))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0F172A).copy(alpha = 0.95f),
                            Color(0xFF060D1E).copy(alpha = 0.98f)
                        )
                    )
                )
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar: Rarity badge & in-game setup status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = skin.iconEmoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = rarityColor.copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, rarityColor)
                    ) {
                        Text(
                            text = skin.rarity.label,
                            color = rarityColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                if (isEquipped) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ACTIVE IN GAMES",
                                color = Color(0xFF10B981),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                } else if (isUnlocked) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF38BDF8).copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8))
                    ) {
                        Text(
                            text = "UNLOCKED ✓",
                            color = Color(0xFF38BDF8),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF38BDF8).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8))
                    ) {
                        Text(
                            text = "💎 ${skin.priceDiamonds} DIAMONDS",
                            color = Color(0xFF38BDF8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3D Pedestal Stage & Floating Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp),
                contentAlignment = Alignment.Center
            ) {
                // 1. Pedestal Base Plinth
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .width(170.dp)
                        .height(44.dp)
                        .graphicsLayer {
                            scaleY = 0.40f
                            rotationZ = ringRotation
                        }
                        .border(
                            2.5.dp,
                            Brush.sweepGradient(listOf(accentColor, rarityColor, Color(0xFFFFD700), accentColor)),
                            CircleShape
                        )
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    accentColor.copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )

                // 2. Upward light energy beam from plinth
                Canvas(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .width(140.dp)
                        .height(110.dp)
                ) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                accentColor.copy(alpha = 0.15f)
                            )
                        )
                    )
                }

                // 3. Floating 3D Card with drag-to-inspect
                Box(
                    modifier = Modifier
                        .width(115.dp)
                        .height(160.dp)
                        .offset(y = floatOffset.dp)
                        .graphicsLayer {
                            rotationY = flipRotation + animatedManualTiltX
                            rotationX = gyroPitch + animatedManualTiltY
                            cameraDistance = 14f * density
                            shadowElevation = 20.dp.toPx()
                        }
                        .shadow(20.dp, RoundedCornerShape(10.dp), spotColor = accentColor)
                        .clip(RoundedCornerShape(10.dp))
                        .pointerInput(skin.id) {
                            detectDragGestures(
                                onDragEnd = {
                                    manualTiltX = 0f
                                    manualTiltY = 0f
                                },
                                onDragCancel = {
                                    manualTiltX = 0f
                                    manualTiltY = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    manualTiltX = (manualTiltX + dragAmount.x * 0.4f).coerceIn(-35f, 35f)
                                    manualTiltY = (manualTiltY - dragAmount.y * 0.4f).coerceIn(-25f, 25f)
                                }
                            )
                        }
                        .clickable { isFlipped = !isFlipped },
                    contentAlignment = Alignment.Center
                ) {
                    if (flipRotation <= 90f) {
                        // Card Back View
                        CardBackView(skinId = skin.id)
                    } else {
                        // Card Face View (flipped 180 so typography is correct)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { rotationY = 180f }
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9))
                                    )
                                )
                                .border(2.dp, accentColor, RoundedCornerShape(10.dp))
                                .padding(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("J ♠", color = Color(0xFF0F172A), fontSize = 12.sp, fontWeight = FontWeight.Black)
                                    Text("3 Pts", color = Color(0xFFD97706), fontSize = 9.5.sp, fontWeight = FontWeight.Black)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "👑", fontSize = 28.sp)
                                    Text(
                                        text = "JACK OF SPADES",
                                        color = Color(0xFF1E293B),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(skin.iconEmoji, fontSize = 11.sp)
                                    Text("♠ J", color = Color(0xFF0F172A), fontSize = 12.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }

                    // Dynamic Specular Holographic Sheen Overlay
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val sheenStart = Offset(w * (sheenProgress - 0.3f), 0f)
                        val sheenEnd = Offset(w * (sheenProgress + 0.3f), h)
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.35f),
                                    Color.Transparent
                                ),
                                start = sheenStart,
                                end = sheenEnd
                            )
                        )
                    }
                }
            }

            // Interactive 3D Orbit & Flip Hint Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1E293B))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                    .clickable { isFlipped = !isFlipped }
                    .padding(horizontal = 14.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Flip",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isFlipped) "FLIP TO CARD BACK" else "FLIP TO FACE • DRAG TO TILT (3D)",
                    color = Color(0xFFE2E8F0),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.4.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title & Description
            Text(
                text = skin.title,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
            Text(
                text = skin.description,
                color = Color(0xFFCBD5E1),
                fontSize = 11.5.sp,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons: Equip / Buy with Diamonds / Watch Ad for Diamonds
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isUnlocked) {
                    Aaa3DButton(
                        text = if (isEquipped) "✅ ACTIVE ON MY TABLE" else "SETUP IN MY GAMES (EQUIP)",
                        icon = if (isEquipped) Icons.Default.Check else Icons.Default.AutoAwesome,
                        onClick = { onEquip(skin) },
                        enabled = !isEquipped,
                        gradient = if (isEquipped) Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
                        else Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669))),
                        textColor = if (isEquipped) Color(0xFF94A3B8) else Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        height = 42.dp,
                        testTag = "showcase_equip_button"
                    )
                } else {
                    if (hasEnoughDiamonds) {
                        Aaa3DButton(
                            text = "BUY & SETUP NOW (💎 ${skin.priceDiamonds})",
                            icon = Icons.Default.ShoppingCart,
                            onClick = { onBuy(skin) },
                            gradient = Brush.horizontalGradient(listOf(Color(0xFF38BDF8), Color(0xFF0284C7))),
                            textColor = Color(0xFF0F172A),
                            modifier = Modifier.fillMaxWidth(),
                            height = 42.dp,
                            testTag = "showcase_buy_button"
                        )
                    } else {
                        val needed = skin.priceDiamonds - economy.diamonds
                        Aaa3DButton(
                            text = "NEED 💎 $needed (WATCH AD +5 💎)",
                            icon = Icons.Default.PlayArrow,
                            onClick = onWatchAdForDiamonds,
                            gradient = Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706))),
                            textColor = Color(0xFF0F172A),
                            modifier = Modifier.fillMaxWidth(),
                            height = 42.dp,
                            testTag = "showcase_watch_ad_button"
                        )
                    }
                }
            }
        }
    }
}
