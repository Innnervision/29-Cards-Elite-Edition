package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.model.Player
import com.example.game.model.PlayerPosition
import com.example.game.model.Team
import com.example.ui.theme.*

@Composable
fun PlayerPodView(
    player: Player,
    isActiveTurn: Boolean,
    modifier: Modifier = Modifier,
    isDealer: Boolean = false,
    currentBid: Int? = null,
    cardCount: Int = player.cards.size,
    isReceivingCards: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "turn_pulse")
    val pulseBorder by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val isHighlighted = isActiveTurn || isReceivingCards

    val avatarDrawable = when (player.position) {
        PlayerPosition.SOUTH -> R.drawable.img_avatar_player
        PlayerPosition.NORTH -> R.drawable.img_avatar_ai_master
        PlayerPosition.WEST -> R.drawable.img_avatar_rival_west
        PlayerPosition.EAST -> R.drawable.img_avatar_rival_east
    }

    Column(
        modifier = modifier.testTag("player_pod_${player.position.name.lowercase()}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Speech Bubble (if any) with animated pop
        AnimatedVisibility(
            visible = player.speechBubble != null,
            enter = fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.8f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
            exit = fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.8f)
        ) {
            Surface(
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .shadow(12.dp, RoundedCornerShape(12.dp), spotColor = CasinoGold),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.96f),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, CasinoGold)
            ) {
                Text(
                    text = player.speechBubble ?: "",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Main Player Capsule
        Surface(
            modifier = Modifier
                .shadow(
                    elevation = if (isHighlighted) 16.dp else 4.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = if (isHighlighted) CasinoGold else Color.Black
                )
                .border(
                    width = if (isHighlighted) 2.dp else 1.dp,
                    brush = if (isHighlighted) {
                        Brush.sweepGradient(
                            listOf(
                                CasinoGold.copy(alpha = pulseBorder),
                                CasinoGoldLight,
                                Color(0xFF38BDF8).copy(alpha = pulseBorder),
                                CasinoGold.copy(alpha = pulseBorder)
                            )
                        )
                    } else {
                        Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
                    },
                    shape = RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            color = if (player.position.team == Team.TEAM_A) Color(0xFF091E3A).copy(alpha = 0.94f) else Color(0xFF2A0D18).copy(alpha = 0.94f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                // High-Quality Avatar with dynamic turn ring
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .border(
                            2.dp,
                            if (isActiveTurn) CasinoGold else (if (isReceivingCards) Color(0xFF34D399) else Color(0xFF94A3B8).copy(alpha = 0.6f)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = avatarDrawable),
                        contentDescription = player.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Active turn glowing indicator dot
                    if (isActiveTurn) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4ADE80))
                                .border(1.dp, Color.White, CircleShape)
                        )
                    }
                }

                // Name & Status
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = player.name,
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1
                        )
                        if (isDealer) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = CircleShape,
                                color = CasinoGold,
                                modifier = Modifier.size(14.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "D",
                                        color = Color(0xFF0F172A),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Card Count Pill
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isReceivingCards) Color(0xFF065F46) else Color(0xFF1E293B).copy(alpha = 0.8f),
                            border = if (isReceivingCards) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF34D399)) else null
                        ) {
                            Text(
                                text = if (isReceivingCards) "🂠 $cardCount (+4)" else "🂠 $cardCount",
                                color = if (isReceivingCards) Color(0xFF6EE7B7) else Color(0xFFCBD5E1),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }

                        // Current Bid Pill
                        if (currentBid != null && currentBid >= 16) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFB45309).copy(alpha = 0.85f),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, CasinoGold)
                            ) {
                                Text(
                                    text = "$currentBid",
                                    color = CasinoGold,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
