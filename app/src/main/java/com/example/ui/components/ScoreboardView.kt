package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.model.GameState
import com.example.game.model.PlayerPosition
import com.example.game.model.Suit
import com.example.game.model.Team

@Composable
fun ScoreboardView(
    gameState: GameState,
    modifier: Modifier = Modifier,
    onRevealTrumpClick: (() -> Unit)? = null,
    onPairDeclareClick: (() -> Unit)? = null,
    canDeclarePair: Boolean = false,
    canRevealTrump: Boolean = false
) {
    var showScorecardDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Team A Score (Us) & Red/Black tokens
        TeamScorePod(
            teamName = "Us (S & N)",
            cardPoints = gameState.teamAScore.cardPoints,
            gamePoints = gameState.teamAScore.gamePoints,
            isBidder = gameState.highestBidder?.team == Team.TEAM_A,
            targetBid = if (gameState.highestBidder?.team == Team.TEAM_A) gameState.currentHighestBid else null,
            modifier = Modifier.weight(1f),
            onPodClick = { showScorecardDialog = true }
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Center: Trump Indicator & Live Trick Info & Scorecard button
        CenterGameStatusPod(
            gameState = gameState,
            canRevealTrump = canRevealTrump,
            canDeclarePair = canDeclarePair,
            onRevealTrumpClick = onRevealTrumpClick,
            onPairDeclareClick = onPairDeclareClick,
            onOpenScorecard = { showScorecardDialog = true }
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Right: Team B Score (Them) & Red/Black tokens
        TeamScorePod(
            teamName = "Them (E & W)",
            cardPoints = gameState.teamBScore.cardPoints,
            gamePoints = gameState.teamBScore.gamePoints,
            isBidder = gameState.highestBidder?.team == Team.TEAM_B,
            targetBid = if (gameState.highestBidder?.team == Team.TEAM_B) gameState.currentHighestBid else null,
            modifier = Modifier.weight(1f),
            onPodClick = { showScorecardDialog = true }
        )
    }

    if (showScorecardDialog) {
        RealtimeScorecardModal(
            gameState = gameState,
            onDismiss = { showScorecardDialog = false }
        )
    }
}

@Composable
private fun TeamScorePod(
    teamName: String,
    cardPoints: Int,
    gamePoints: Int,
    isBidder: Boolean,
    targetBid: Int?,
    modifier: Modifier = Modifier,
    onPodClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onPodClick)
            .border(
                1.dp,
                if (isBidder) Color(0xFFFFD700).copy(alpha = 0.8f) else Color(0xFF334155),
                RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.90f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = teamName,
                    color = if (isBidder) Color(0xFFFFD700) else Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                if (isBidder) {
                    Text(
                        text = " (Bidder)",
                        color = Color(0xFFFFC107),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$cardPoints",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = if (targetBid != null) " / $targetBid pts" else " / 28 pts",
                    color = if (targetBid != null) Color(0xFFFFD700) else Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 1.dp)
                )
            }

            // Red / Black Indian 29 Tokens (Pip indicators)
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                val tokens = gamePoints.coerceIn(-6, 6)
                repeat(6) { idx ->
                    val isFilled = idx < Math.abs(tokens)
                    val tokenColor = if (tokens >= 0) Color(0xFFEF4444) else Color(0xFF1E293B)
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) tokenColor else Color(0xFF334155).copy(alpha = 0.4f)
                            )
                            .border(0.5.dp, Color(0xFF64748B), CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
private fun CenterGameStatusPod(
    gameState: GameState,
    canRevealTrump: Boolean,
    canDeclarePair: Boolean,
    onRevealTrumpClick: (() -> Unit)?,
    onPairDeclareClick: (() -> Unit)?,
    onOpenScorecard: () -> Unit
) {
    Surface(
        modifier = Modifier
            .shadow(8.dp, RoundedCornerShape(14.dp))
            .border(1.2.dp, Color(0xFFFFD700), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1E1B4B).copy(alpha = 0.95f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Trump Status Box
            TrumpStatusBadge(
                trumpSuit = gameState.trumpSuit,
                isRevealed = gameState.isTrumpRevealed,
                canReveal = canRevealTrump,
                onRevealClick = onRevealTrumpClick
            )

            // Bid & Trick Info & Scorecard button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable(onClick = onOpenScorecard)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "BID: ${if (gameState.currentHighestBid > 15) gameState.currentHighestBid else "--"}",
                        color = Color(0xFFFFD700),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Black
                    )
                    if (gameState.isDoubled) {
                        Text(
                            text = if (gameState.isRedoubled) " x4" else " x2",
                            color = Color(0xFFEF4444),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Trick ${gameState.currentTrickIndex + 1}/8",
                        color = Color(0xFFE2E8F0),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = "Scorecard",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            // Pair/Royalty Declare Button
            if (canDeclarePair && onPairDeclareClick != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFFE11D48), Color(0xFFBE123C)))
                        )
                        .clickable(onClick = onPairDeclareClick)
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                        .testTag("declare_pair_button")
                ) {
                    Text(
                        text = "👑 PAIR",
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TrumpStatusBadge(
    trumpSuit: Suit?,
    isRevealed: Boolean,
    canReveal: Boolean,
    onRevealClick: (() -> Unit)?
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isRevealed) {
                    Brush.radialGradient(listOf(Color(0xFF831843), Color(0xFF4C0519)))
                } else if (canReveal) {
                    Brush.horizontalGradient(listOf(Color(0xFFD97706), Color(0xFFB45309)))
                } else {
                    Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
                }
            )
            .border(
                1.dp,
                if (canReveal) Color(0xFFFFD700) else Color(0xFF64748B),
                RoundedCornerShape(8.dp)
            )
            .then(
                if (canReveal && onRevealClick != null) {
                    Modifier.clickable(onClick = onRevealClick)
                } else Modifier
            )
            .padding(horizontal = 7.dp, vertical = 3.dp)
            .testTag("trump_status_badge")
    ) {
        if (isRevealed && trumpSuit != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = trumpSuit.symbol,
                    color = if (trumpSuit.isRed) Color(0xFFEF4444) else Color(0xFFF8FAFC),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "TRUMP",
                    color = Color(0xFFFFD700),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    imageVector = if (canReveal) Icons.Default.Visibility else Icons.Default.Lock,
                    contentDescription = "Trump status",
                    tint = if (canReveal) Color(0xFFFFD700) else Color(0xFF94A3B8),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = if (canReveal) "SHOW" else "TRUMP 🔒",
                    color = if (canReveal) Color(0xFFFFD700) else Color(0xFF94A3B8),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun RealtimeScorecardModal(
    gameState: GameState,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .shadow(24.dp, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF0F172A),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFFFD700).copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE MATCH SCORECARD",
                            color = Color(0xFFFFD700),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                // Summary Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Team Us (S & N)", color = Color(0xFF38BDF8), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            Text("${gameState.teamAScore.cardPoints} pts", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                            Text("${gameState.teamAScore.gamePoints} Pips", color = Color(0xFF4ADE80), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Team Them (E & W)", color = Color(0xFFF87171), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            Text("${gameState.teamBScore.cardPoints} pts", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                            Text("${gameState.teamBScore.gamePoints} Pips", color = Color(0xFFF87171), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Tricks list
                Text(
                    text = "COMPLETED TRICKS (${gameState.completedTricks.size}/8)",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Black
                )

                if (gameState.completedTricks.isEmpty()) {
                    Text(
                        text = "No tricks completed in this round yet.",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 140.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(gameState.completedTricks) { trick ->
                            val winner = trick.findWinner(gameState.trumpSuit, gameState.isTrumpRevealed)
                            val isUs = winner.team == Team.TEAM_A
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF131D31),
                                border = androidx.compose.foundation.BorderStroke(
                                    0.8.dp,
                                    if (isUs) Color(0xFF38BDF8).copy(alpha = 0.4f) else Color(0xFFF87171).copy(alpha = 0.4f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Trick #${trick.trickNumber}: ${winner.displayName} won (${trick.points} pts)",
                                        color = if (isUs) Color(0xFF38BDF8) else Color(0xFFF87171),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = trick.plays.joinToString(" ") { it.card.rank.symbol + it.card.suit.symbol },
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
