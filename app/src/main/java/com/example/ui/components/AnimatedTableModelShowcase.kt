package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.PlayerEconomy
import com.example.game.model.TableTheme

/**
 * 3D Animated Casino Table Model Showcase for StoreScreen.
 * Renders an interactive, perspective-angled table model with felt shimmer,
 * brass rail trim, and direct "SETUP ON MY TABLE" flow.
 */
@Composable
fun AnimatedTableModelShowcase(
    theme: TableTheme,
    economy: PlayerEconomy,
    onEquip: (TableTheme) -> Unit,
    onBuy: (TableTheme) -> Unit,
    onWatchAdForDiamonds: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUnlocked = economy.unlockedTableThemes.contains(theme.id) || theme.priceDiamonds == 0
    val isEquipped = economy.equippedTableTheme == theme.id
    val hasEnoughDiamonds = economy.diamonds >= theme.priceDiamonds

    val infiniteTransition = rememberInfiniteTransition(label = "table_shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_prog"
    )

    val tableColors = theme.feltGradients.map { Color(it) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(24.dp, RoundedCornerShape(20.dp), spotColor = tableColors.first().copy(alpha = 0.5f)),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0B1120),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            Brush.sweepGradient(listOf(Color(0xFFFFD700), tableColors.first(), Color(0xFFFFD700)))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0F172A).copy(alpha = 0.95f), Color(0xFF060D1E).copy(alpha = 0.98f))
                    )
                )
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "3D ARENA TABLE MODEL",
                    color = Color(0xFFFFD700),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

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
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ACTIVE ON TABLE", color = Color(0xFF10B981), fontSize = 9.5.sp, fontWeight = FontWeight.Black)
                        }
                    }
                } else if (isUnlocked) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF38BDF8).copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8))
                    ) {
                        Text("UNLOCKED ✓", color = Color(0xFF38BDF8), fontSize = 9.5.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF38BDF8).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8))
                    ) {
                        Text("💎 ${theme.priceDiamonds} DIAMONDS", color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3D Perspective Table View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer Brass Rail
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.90f)
                        .height(120.dp)
                        .graphicsLayer {
                            scaleY = 0.65f
                            cameraDistance = 12f * density
                        }
                        .shadow(28.dp, RoundedCornerShape(80.dp), spotColor = Color(0xFFFFD700))
                        .clip(RoundedCornerShape(80.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF78350F), Color(0xFFD97706), Color(0xFF451A03), Color(0xFFD97706))
                            )
                        )
                        .padding(7.dp)
                ) {
                    // Inner Plush Felt Surface with radial glow
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(74.dp))
                            .background(Brush.radialGradient(tableColors))
                            .border(1.5.dp, Color(0xFFFFD700).copy(alpha = 0.4f), RoundedCornerShape(74.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Center 29 Inscription
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "TWENTY-NINE",
                                color = Color(0xFFFFD700).copy(alpha = 0.5f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "♠ ♥ ♣ ♦",
                                color = Color(0xFFFFD700).copy(alpha = 0.4f),
                                fontSize = 11.sp
                            )
                        }

                        // Shimmer sweep
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.18f), Color.Transparent),
                                    start = Offset(w * (shimmerOffset - 0.25f), 0f),
                                    end = Offset(w * (shimmerOffset + 0.25f), h)
                                )
                            )
                        }
                    }
                }
            }

            // Theme Name & Description
            Text(
                text = theme.title,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = theme.description,
                color = Color(0xFFCBD5E1),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action Button: Setup on Table
            if (isUnlocked) {
                Aaa3DButton(
                    text = if (isEquipped) "✅ ACTIVE ON MY TABLE" else "SETUP ON MY TABLE (EQUIP)",
                    icon = if (isEquipped) Icons.Default.Check else Icons.Default.AutoAwesome,
                    onClick = { onEquip(theme) },
                    enabled = !isEquipped,
                    gradient = if (isEquipped) Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
                    else Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669))),
                    textColor = if (isEquipped) Color(0xFF94A3B8) else Color.White,
                    modifier = Modifier.fillMaxWidth(),
                    height = 42.dp,
                    testTag = "showcase_equip_theme_button"
                )
            } else {
                if (hasEnoughDiamonds) {
                    Aaa3DButton(
                        text = "BUY & SETUP NOW (💎 ${theme.priceDiamonds})",
                        icon = Icons.Default.ShoppingCart,
                        onClick = { onBuy(theme) },
                        gradient = Brush.horizontalGradient(listOf(Color(0xFF38BDF8), Color(0xFF0284C7))),
                        textColor = Color(0xFF0F172A),
                        modifier = Modifier.fillMaxWidth(),
                        height = 42.dp,
                        testTag = "showcase_buy_theme_button"
                    )
                } else {
                    val needed = theme.priceDiamonds - economy.diamonds
                    Aaa3DButton(
                        text = "NEED 💎 $needed (WATCH AD +5 💎)",
                        icon = Icons.Default.PlayArrow,
                        onClick = onWatchAdForDiamonds,
                        gradient = Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706))),
                        textColor = Color(0xFF0F172A),
                        modifier = Modifier.fillMaxWidth(),
                        height = 42.dp,
                        testTag = "showcase_watch_ad_theme_button"
                    )
                }
            }
        }
    }
}
