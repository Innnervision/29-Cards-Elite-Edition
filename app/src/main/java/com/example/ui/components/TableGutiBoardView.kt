package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CasinoGold

/**
 * Authentic 29 Card Game "Chabbi" / Guti Board (Tokens & Pips Counter)
 * Scaled realistically to match the table felt proportions without overcrowding.
 * Accurate 6-pip game counter with Team A (Red) and Team B (Obsidian) guti tokens.
 */
@Composable
fun TableGutiBoardView(
    teamAGamePoints: Int,
    teamBGamePoints: Int,
    targetPips: Int = 6,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "guti_pulse")
    val gutiGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "guti_glow_alpha"
    )

    Surface(
        modifier = modifier
            .widthIn(max = 132.dp)
            .shadow(6.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1E293B).copy(alpha = 0.95f),
                        Color(0xFF0F172A).copy(alpha = 0.98f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        CasinoGold.copy(alpha = 0.7f),
                        Color(0xFF475569)
                    )
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 6.dp, vertical = 3.dp)
            .testTag("table_guti_board"),
        color = Color.Transparent
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.5.dp)
        ) {
            // Header: Authentic "GUTI / CHABBI" Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "GUTI BOARD",
                    color = CasinoGold,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.6.sp
                )
                Text(
                    text = "(${targetPips} Pips)",
                    color = Color(0xFF94A3B8),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Team A: Red Guti Row (S & N)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "WE",
                    color = Color(0xFF4ADE80),
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(16.dp)
                )

                // 6 Scaled Miniature Red Guti Tokens (reduced to 5.dp for realistic board fit)
                val aTokens = teamAGamePoints.coerceIn(0, targetPips)
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(targetPips) { idx ->
                        val isFilled = idx < aTokens
                        SingleGutiToken(
                            isFilled = isFilled,
                            tokenColor = Color(0xFFEF4444),
                            emptyBorderColor = Color(0xFF7F1D1D),
                            glowAlpha = if (isFilled && idx == aTokens - 1) gutiGlow else 1f,
                            size = 5.dp
                        )
                    }
                }

                Text(
                    text = "$aTokens",
                    color = Color.White,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Team B: Black/Obsidian Guti Row (E & W)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "OPP",
                    color = Color(0xFFF87171),
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(16.dp)
                )

                // 6 Scaled Miniature Black Guti Tokens (reduced to 5.dp for realistic board fit)
                val bTokens = teamBGamePoints.coerceIn(0, targetPips)
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(targetPips) { idx ->
                        val isFilled = idx < bTokens
                        SingleGutiToken(
                            isFilled = isFilled,
                            tokenColor = Color(0xFF1E293B),
                            emptyBorderColor = Color(0xFF334155),
                            borderColor = Color(0xFF94A3B8),
                            glowAlpha = 1f,
                            size = 5.dp
                        )
                    }
                }

                Text(
                    text = "$bTokens",
                    color = Color.White,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

/**
 * Miniature realistic guti token with jewel/metallic gloss.
 */
@Composable
private fun SingleGutiToken(
    isFilled: Boolean,
    tokenColor: Color,
    emptyBorderColor: Color,
    borderColor: Color = Color(0xFFFFD700),
    glowAlpha: Float = 1f,
    size: Dp = 6.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                if (isFilled) {
                    Brush.radialGradient(
                        listOf(
                            tokenColor.copy(alpha = glowAlpha),
                            tokenColor.copy(alpha = 0.85f),
                            Color(0xFF0F172A)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF1E293B).copy(alpha = 0.5f),
                            Color(0xFF0F172A).copy(alpha = 0.7f)
                        )
                    )
                }
            )
            .border(
                width = 0.6.dp,
                color = if (isFilled) borderColor.copy(alpha = 0.9f) else emptyBorderColor.copy(alpha = 0.5f),
                shape = CircleShape
            )
    )
}

/**
 * Scaled-down realistic mini guti pip indicator for score bars and headers.
 */
@Composable
fun MiniGutiPipView(
    points: Int,
    targetPips: Int,
    isTeamA: Boolean,
    modifier: Modifier = Modifier
) {
    val activePips = points.coerceIn(0, targetPips)
    val color = if (isTeamA) Color(0xFFEF4444) else Color(0xFF64748B)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        repeat(targetPips) { idx ->
            val isFilled = idx < activePips
            Box(
                modifier = Modifier
                    .size(5.5.dp)
                    .clip(CircleShape)
                    .background(if (isFilled) color else Color(0xFF334155).copy(alpha = 0.4f))
                    .border(0.5.dp, if (isFilled) Color(0xFFFFD700) else Color(0xFF475569), CircleShape)
            )
        }
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = "$activePips/$targetPips",
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
