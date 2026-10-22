package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.model.TournamentProgress
import com.example.game.model.TournamentStage
import com.example.ui.components.AdMobBanner
import com.example.ui.components.LuxuryScreenBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentScreen(
    tournamentProgress: TournamentProgress,
    onStartStageMatch: (TournamentStage) -> Unit,
    onResetTournament: () -> Unit,
    onSpeedUpWithDiamonds: () -> Unit = {},
    onSpeedUpWithAd: () -> Unit = {},
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Tournament",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "GRAND 29 CHAMPIONSHIP CUP",
                            color = Color(0xFFFFD700),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("tournament_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = onResetTournament,
                        modifier = Modifier.testTag("reset_tournament_button")
                    ) {
                        Text("Reset Cup", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        containerColor = Color(0xFF060913)
    ) { padding ->
        LuxuryScreenBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            imageResId = R.drawable.img_mode_tournament,
            overlayAlpha = 0.50f
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            // Stage Progression Cards in Landscape Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stage 1: Quarter Finals
                TournamentStageCard(
                    stage = TournamentStage.QUARTER_FINALS,
                    isCurrent = tournamentProgress.currentStage == TournamentStage.QUARTER_FINALS,
                    isCompleted = tournamentProgress.currentStage.ordinal > TournamentStage.QUARTER_FINALS.ordinal,
                    opponentAvatar = R.drawable.img_avatar_rival_west,
                    modifier = Modifier.weight(1f),
                    onPlay = { onStartStageMatch(TournamentStage.QUARTER_FINALS) }
                )

                // Stage 2: Semi Finals
                TournamentStageCard(
                    stage = TournamentStage.SEMI_FINALS,
                    isCurrent = tournamentProgress.currentStage == TournamentStage.SEMI_FINALS,
                    isCompleted = tournamentProgress.currentStage.ordinal > TournamentStage.SEMI_FINALS.ordinal,
                    opponentAvatar = R.drawable.img_avatar_rival_east,
                    modifier = Modifier.weight(1f),
                    onPlay = { onStartStageMatch(TournamentStage.SEMI_FINALS) }
                )

                // Stage 3: Grand Finals
                TournamentStageCard(
                    stage = TournamentStage.GRAND_FINALS,
                    isCurrent = tournamentProgress.currentStage == TournamentStage.GRAND_FINALS,
                    isCompleted = tournamentProgress.currentStage == TournamentStage.CHAMPION,
                    opponentAvatar = R.drawable.img_avatar_ai_master,
                    modifier = Modifier.weight(1f),
                    onPlay = { onStartStageMatch(TournamentStage.GRAND_FINALS) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Luxury 3D Trophy Showcase Status Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = if (tournamentProgress.isTrophyUnlocked) Color(0xFF451A03) else Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (tournamentProgress.isTrophyUnlocked) Color(0xFFFFD700) else Color(0xFF334155)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .shadow(10.dp, CircleShape)
                                .clip(CircleShape)
                                .border(2.dp, Color(0xFFFFD700), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_trophy_gold),
                                contentDescription = "Championship Trophy",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (tournamentProgress.isTrophyUnlocked) "🏆 29 GRAND CUP CHAMPION!" else "CHAMPIONSHIP TROPHY",
                                    color = Color(0xFFFFD700),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFFFD700).copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFFFFD700))
                                ) {
                                    Text(
                                        text = "+5,000 🪙",
                                        color = Color(0xFFFFD700),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (tournamentProgress.isTrophyUnlocked)
                                    "Congratulations! You defeated the Delhi Masters and conquered the 29 Grand Cup!"
                                else
                                    "Defeat Kolkata Knights, Mumbai Royals, and Delhi Masters to claim the Grand Trophy!",
                                color = Color(0xFFE2E8F0),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Instant Stage Skip / Tournament Rush Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("⚡", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "TOURNAMENT RUSH SPEED-UP",
                                color = Color(0xFFFFD700),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Use Diamonds (source of truth) or watch a Rewarded Interstitial Ad to unlock stages instantly!",
                                color = Color(0xFFCBD5E1),
                                fontSize = 9.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = onSpeedUpWithAd,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("tournament_ad_speedup_button")
                        ) {
                            Text("WATCH AD", color = Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp)
                        }

                        Button(
                            onClick = onSpeedUpWithDiamonds,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("tournament_diamond_speedup_button")
                        ) {
                            Text("💎 20", color = Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // AdMob Sponsor Banner
            AdMobBanner(modifier = Modifier.fillMaxWidth())
        }
    }
}
}

@Composable
private fun TournamentStageCard(
    stage: TournamentStage,
    isCurrent: Boolean,
    isCompleted: Boolean,
    opponentAvatar: Int,
    modifier: Modifier = Modifier,
    onPlay: () -> Unit
) {
    val isLocked = !isCurrent && !isCompleted

    Surface(
        modifier = modifier
            .height(205.dp)
            .shadow(if (isCurrent) 14.dp else 4.dp, RoundedCornerShape(16.dp))
            .border(
                width = if (isCurrent) 2.dp else 1.dp,
                color = when {
                    isCompleted -> Color(0xFF22C55E)
                    isCurrent -> Color(0xFFFFD700)
                    else -> Color(0xFF334155)
                },
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        color = when {
            isCompleted -> Color(0xFF064E3B)
            isCurrent -> Color(0xFF1E293B)
            else -> Color(0xFF0B1120)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(11.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stage Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stage.title,
                    color = if (isCurrent || isCompleted) Color.White else Color(0xFF64748B),
                    fontWeight = FontWeight.Black,
                    fontSize = 11.5.sp
                )

                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Cleared",
                        tint = Color(0xFF4ADE80),
                        modifier = Modifier.size(16.dp)
                    )
                } else if (isLocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Opponent Avatar & Details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .border(
                            1.5.dp,
                            if (isCurrent) Color(0xFFFFD700) else Color(0xFF64748B),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = opponentAvatar),
                        contentDescription = stage.opponentClub,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = stage.opponentClub,
                        color = if (isCurrent) Color(0xFFFFD700) else Color(0xFFCBD5E1),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "Reward: ${stage.rewardCoins} 🏆",
                        color = Color(0xFF94A3B8),
                        fontSize = 9.5.sp
                    )
                }
            }

            // Action Button
            Button(
                onClick = onPlay,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .testTag("tournament_stage_${stage.name.lowercase()}"),
                enabled = isCurrent || isCompleted,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCompleted) Color(0xFF059669) else Color(0xFFFFD700),
                    disabledContainerColor = Color(0xFF1E293B)
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isCurrent) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = when {
                            isCompleted -> "REPLAY"
                            isCurrent -> "ENTER MATCH"
                            else -> "LOCKED"
                        },
                        color = if (isCompleted) Color.White else if (isCurrent) Color(0xFF0F172A) else Color(0xFF64748B),
                        fontWeight = FontWeight.Black,
                        fontSize = 10.5.sp
                    )
                }
            }
        }
    }
}
