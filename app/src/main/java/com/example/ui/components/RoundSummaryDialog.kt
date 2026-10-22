package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.game.engine.RoundResult
import com.example.game.model.AdventureLevel
import com.example.game.model.Team
import com.example.ui.theme.CasinoGold

@Composable
fun RoundSummaryDialog(
    roundResult: RoundResult?,
    isMatchOver: Boolean,
    onNextRound: () -> Unit,
    onNewMatch: () -> Unit,
    adventureLevel: AdventureLevel? = null,
    onBackToAdventures: (() -> Unit)? = null,
    onWatchAdDoubleRewards: (() -> Unit)? = null,
    onWatchAdRevive: (() -> Unit)? = null,
    onExitToLobby: (() -> Unit)? = null
) {
    if (roundResult == null) return

    // If opponent bidder loses contract -> we win! If opponent bidder wins contract -> we lose!
    val isUserTeamWinner = (roundResult.bidderTeam == Team.TEAM_A && roundResult.isSuccess) ||
            (roundResult.bidderTeam == Team.TEAM_B && !roundResult.isSuccess)

    val teamAScore = if (roundResult.bidderTeam == Team.TEAM_A) roundResult.bidderScore else (28 - roundResult.bidderScore)
    val teamBScore = if (roundResult.bidderTeam == Team.TEAM_B) roundResult.bidderScore else (28 - roundResult.bidderScore)

    // Animated Score Counter
    var animatedBidderScore by remember { mutableStateOf(0) }
    LaunchedEffect(roundResult) {
        val target = roundResult.bidderScore
        for (i in 0..target) {
            animatedBidderScore = i
            kotlinx.coroutines.delay(18)
        }
    }

    val animatedTeamAScore = if (roundResult.bidderTeam == Team.TEAM_A) animatedBidderScore else (28 - animatedBidderScore)
    val animatedTeamBScore = if (roundResult.bidderTeam == Team.TEAM_B) animatedBidderScore else (28 - animatedBidderScore)

    val infiniteTransition = rememberInfiniteTransition(label = "victory_glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    Dialog(
        onDismissRequest = { /* Modal */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Celebratory or Gloomy Particle VFX Overlay
            WinningLosingVfxOverlay(
                isVictory = isUserTeamWinner,
                modifier = Modifier.fillMaxSize()
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .wrapContentHeight()
                    .scale(glowScale)
                    .shadow(32.dp, RoundedCornerShape(24.dp))
                    .border(
                        2.5.dp,
                        Brush.linearGradient(
                            if (isUserTeamWinner) listOf(Color(0xFFFFD700), Color(0xFFF59E0B), Color(0xFFFFD700))
                            else listOf(Color(0xFFEF4444), Color(0xFF7F1D1D), Color(0xFFEF4444))
                        ),
                        RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF0B1120).copy(alpha = 0.96f)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column: Victory Banner, Title, Exact Score Comparison Cards
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(76.dp)
                                .clip(RoundedCornerShape(14.dp))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_banner_casino),
                                contentDescription = "Banner",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color(0xFF0B1120).copy(alpha = 0.85f))
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Title Header with Icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isUserTeamWinner) Icons.Default.EmojiEvents else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (isUserTeamWinner) Color(0xFFFFD700) else Color(0xFFEF4444),
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isMatchOver) {
                                    if (adventureLevel != null) {
                                        if (isUserTeamWinner) "LEVEL CLEARED! 👑" else "LEVEL FAILED"
                                    } else {
                                        if (isUserTeamWinner) "VICTORY! YOU WIN" else "DEFEAT! OPPONENT WINS"
                                    }
                                } else {
                                    if (isUserTeamWinner) "ROUND WON! (OPPONENT LOST)" else "ROUND LOST! (OPPONENT WON)"
                                },
                                color = if (isUserTeamWinner) Color(0xFFFFD700) else Color(0xFFEF4444),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            text = if (roundResult.bidderTeam == Team.TEAM_B) {
                                if (roundResult.isSuccess) "Opponent made bid of ${roundResult.adjustedBid} pts -> Opponent Wins"
                                else "Opponent fell short of ${roundResult.adjustedBid} pts -> Opponent Lost, You Win!"
                            } else {
                                if (roundResult.isSuccess) "Your team made bid of ${roundResult.adjustedBid} pts -> You Win!"
                                else "Your team fell short of ${roundResult.adjustedBid} pts -> Opponent Wins"
                            },
                            color = Color(0xFFCBD5E1),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Exact Score Comparison Card (Team A vs Team B)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Team A (You & Partner) Card
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isUserTeamWinner) Color(0xFF065F46).copy(alpha = 0.4f) else Color(0xFF1E293B),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.5.dp,
                                    if (isUserTeamWinner) Color(0xFF34D399) else Color(0xFF334155)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("YOUR TEAM (TEAM A)", color = Color(0xFF94A3B8), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("$animatedTeamAScore / 28 pts", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                    Text(
                                        text = if (roundResult.teamAGamePointsDelta >= 0) "+${roundResult.teamAGamePointsDelta} Game Pips" else "${roundResult.teamAGamePointsDelta} Pips",
                                        color = if (roundResult.teamAGamePointsDelta >= 0) Color(0xFF34D399) else Color(0xFFF87171),
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Team B (Opponent) Card
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = if (!isUserTeamWinner) Color(0xFF7F1D1D).copy(alpha = 0.4f) else Color(0xFF1E293B),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.5.dp,
                                    if (!isUserTeamWinner) Color(0xFFF87171) else Color(0xFF334155)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("OPPONENT (TEAM B)", color = Color(0xFF94A3B8), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("$animatedTeamBScore / 28 pts", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                    Text(
                                        text = if (roundResult.teamBGamePointsDelta >= 0) "+${roundResult.teamBGamePointsDelta} Game Pips" else "${roundResult.teamBGamePointsDelta} Pips",
                                        color = if (roundResult.teamBGamePointsDelta >= 0) Color(0xFF34D399) else Color(0xFFF87171),
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Rewards Box
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF131D31),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🪙", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isUserTeamWinner) "+750 Coins" else "+150 Coins",
                                        color = Color(0xFFFFD700),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.5.sp
                                    )
                                }
                                Divider(
                                    modifier = Modifier
                                        .height(16.dp)
                                        .width(1.dp),
                                    color = Color(0xFF334155)
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("⭐", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isUserTeamWinner) "+150 XP" else "+50 XP",
                                        color = Color(0xFF38BDF8),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                        }
                    }

                    // Right Column: Breakdown Ledger & Action Buttons
                    Column(
                        modifier = Modifier.weight(1.3f),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF131D31),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Contract Bidder:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    Text(
                                        "${roundResult.bidder.displayName} (${roundResult.bidderTeam.displayName})",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Original Bid:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    Text("${roundResult.originalBid} pts", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 11.sp)
                                }

                                if (roundResult.pairDeclaredBy != null) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("👑 Royal Pair Royalty:", color = Color(0xFFFFC107), fontSize = 11.sp)
                                        Text(
                                            if (roundResult.pairDeclaredBy == roundResult.bidderTeam) "-4 Target (Royalty)" else "+4 Target (Opponent)",
                                            color = Color(0xFFFFD700),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Target to Win Contract:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    Text("${roundResult.adjustedBid} pts", color = Color(0xFFFFC107), fontWeight = FontWeight.Black, fontSize = 11.sp)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Bidder Captured Points:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    Text(
                                        "$animatedBidderScore / 28 pts",
                                        color = if (roundResult.isSuccess) Color(0xFF4ADE80) else Color(0xFFF87171),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp
                                    )
                                }

                                // Visual Points Bar vs Target Bid
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(Color(0xFF0F172A))
                                        .border(0.8.dp, Color(0xFF334155), RoundedCornerShape(5.dp))
                                ) {
                                    val progressFraction = (animatedBidderScore.toFloat() / 28f).coerceIn(0f, 1f)
                                    val targetFraction = (roundResult.adjustedBid.toFloat() / 28f).coerceIn(0f, 1f)

                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(progressFraction)
                                            .background(
                                                Brush.horizontalGradient(
                                                    if (roundResult.isSuccess) listOf(Color(0xFF10B981), Color(0xFF34D399))
                                                    else listOf(Color(0xFFEF4444), Color(0xFFF87171))
                                                )
                                            )
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(2.dp)
                                            .align(Alignment.CenterStart)
                                            .offset(x = (targetFraction * 200).dp)
                                            .background(Color(0xFFFFD700))
                                    )
                                }

                                Divider(modifier = Modifier.padding(vertical = 2.dp), color = Color(0xFF1E293B))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Exact Outcome Result:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    Text(
                                        text = if (isUserTeamWinner) "YOU WIN 🎉" else "OPPONENT WINS ❌",
                                        color = Color(0xFF4ADE80),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (onWatchAdDoubleRewards != null && isUserTeamWinner) {
                            Aaa3DButton(
                                text = "2X COINS 🪙 (Watch Video)",
                                onClick = onWatchAdDoubleRewards,
                                variant = ButtonVariant.EMERALD,
                                iconEmoji = "🎬",
                                height = 36.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_ad_double_reward")
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        } else if (onWatchAdRevive != null && !isUserTeamWinner) {
                            Aaa3DButton(
                                text = "CONTRACT SHIELD 🛡️ (Cancel Penalty)",
                                onClick = onWatchAdRevive,
                                variant = ButtonVariant.DIAMOND,
                                iconEmoji = "🛡️",
                                height = 36.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_ad_revive_shield")
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        if (adventureLevel != null && isMatchOver) {
                            Aaa3DButton(
                                text = if (isUserTeamWinner) "ADVENTURE MAP 🗺️" else "RETRY LEVEL 🔄",
                                onClick = if (isUserTeamWinner) (onBackToAdventures ?: onExitToLobby ?: {}) else onNewMatch,
                                variant = if (isUserTeamWinner) ButtonVariant.GOLD else ButtonVariant.PRIMARY,
                                height = 38.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("adventure_map_button")
                            )
                            if (isUserTeamWinner) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Aaa3DButton(
                                    text = "REPLAY LEVEL",
                                    onClick = onNewMatch,
                                    variant = ButtonVariant.SECONDARY,
                                    height = 32.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_replay_level")
                                )
                            }
                        } else {
                            Aaa3DButton(
                                text = if (isMatchOver) "PLAY NEW MATCH" else "START NEXT ROUND",
                                onClick = if (isMatchOver) onNewMatch else onNextRound,
                                variant = if (isUserTeamWinner) ButtonVariant.GOLD else ButtonVariant.PRIMARY,
                                height = 38.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("round_continue_button")
                            )
                        }

                        if (onExitToLobby != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Aaa3DButton(
                                text = "RETURN TO LOBBY",
                                onClick = onExitToLobby,
                                variant = ButtonVariant.SECONDARY,
                                icon = Icons.Default.Home,
                                height = 32.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_exit_to_lobby")
                            )
                        }
                    }
                }
            }
        }
    }
}
