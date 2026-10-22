package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.data.GameStatsRepository
import com.example.game.data.MatchRecord
import com.example.ui.components.LuxuryScreenBackground
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    repository: GameStatsRepository,
    onBackToGame: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val matches by repository.allMatches.collectAsState(initial = emptyList())
    val totalMatches by repository.totalMatches.collectAsState(initial = 0)
    val totalWins by repository.totalWins.collectAsState(initial = 0)
    val highestBidWon by repository.highestBidWon.collectAsState(initial = 0)
    val totalPairs by repository.totalPairsDeclared.collectAsState(initial = 0)
    val singleHandWins by repository.singleHandWins.collectAsState(initial = 0)

    var selectedFilter by remember { mutableStateOf("All") }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val winRate = if (totalMatches > 0) (totalWins * 100) / totalMatches else 0

    val filteredMatches = remember(matches, selectedFilter) {
        when (selectedFilter) {
            "Wins" -> matches.filter { it.isWin }
            "Single Hand" -> matches.filter { it.isSingleHand }
            "Pairs" -> matches.filter { it.pairDeclared }
            else -> matches
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD700).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Career Stats",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "CAREER & RECORDS",
                                color = Color(0xFFFFD700),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                "Recorded Career Matches & Statistics",
                                color = Color(0xFF94A3B8),
                                fontSize = 9.5.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackToGame,
                        modifier = Modifier.testTag("stats_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (matches.isNotEmpty()) {
                        IconButton(onClick = { showClearConfirmDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Clear History",
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        containerColor = Color(0xFF070B14)
    ) { padding ->
        LuxuryScreenBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
            // Hero Win-Rate & Overview Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0F172A),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Brush.horizontalGradient(listOf(Color(0xFFFFD700).copy(alpha = 0.5f), Color(0xFF38BDF8).copy(alpha = 0.3f)))
                    )
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
                            Column {
                                Text(
                                    text = "ROYALE RECORD SUMMARY",
                                    color = Color(0xFFFFD700),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "$totalWins Victories / $totalMatches Matches",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            // Win rate badge
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (winRate >= 50) Color(0xFF22C55E).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (winRate >= 50) Color(0xFF22C55E) else Color(0xFFEF4444)
                                )
                            ) {
                                Text(
                                    text = "$winRate% WIN RATE",
                                    color = if (winRate >= 50) Color(0xFF4ADE80) else Color(0xFFF87171),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        // Progress Bar for Win Rate
                        LinearProgressIndicator(
                            progress = { (winRate / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFFFFD700),
                            trackColor = Color(0xFF1E293B)
                        )
                    }
                }
            }

            // High Score & Milestone Highlights Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HighlightStatCard(
                        title = "Highest Win Bid",
                        value = if (highestBidWon > 0) "$highestBidWon pts" else "—",
                        icon = Icons.Default.TrendingUp,
                        color = Color(0xFFFFD700),
                        modifier = Modifier.weight(1f)
                    )
                    HighlightStatCard(
                        title = "Pairs Declared",
                        value = "$totalPairs",
                        icon = Icons.Default.AutoAwesome,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.weight(1f)
                    )
                    HighlightStatCard(
                        title = "Single Hands",
                        value = "$singleHandWins",
                        icon = Icons.Default.MilitaryTech,
                        color = Color(0xFFA855F7),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FILTER:",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(listOf("All", "Wins", "Single Hand", "Pairs")) { filter ->
                            val isSelected = selectedFilter == filter
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) Color(0xFFFFD700) else Color(0xFF1E293B),
                                modifier = Modifier.clickable { selectedFilter = filter }
                            ) {
                                Text(
                                    text = filter,
                                    color = if (isSelected) Color(0xFF0F172A) else Color(0xFFCBD5E1),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Match Logs Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MATCH LOGS (${filteredMatches.size})",
                        color = Color(0xFFFFD700),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }

            if (filteredMatches.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF0F172A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (matches.isEmpty()) "No matches played yet." else "No matches found matching filter.",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Play rounds on the table to build your career legacy!",
                                color = Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            } else {
                items(filteredMatches, key = { it.id }) { match ->
                    RichMatchHistoryCard(match)
                }
            }
        }
    }
}

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Reset Career History?", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to clear all recorded match logs? This action cannot be undone.", color = Color(0xFFCBD5E1)) },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            repository.resetStats()
                            showClearConfirmDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Clear All", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF0F172A)
        )
    }
}

@Composable
private fun HighlightStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.shadow(6.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(3.dp))
            Text(text = title, color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            Text(text = value, color = color, fontSize = 15.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 1.dp))
        }
    }
}

@Composable
private fun RichMatchHistoryCard(match: MatchRecord) {
    val dateStr = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(match.timestamp))
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (match.isWin) Color(0xFF22C55E).copy(alpha = 0.5f) else Color(0xFFEF4444).copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Header: Status badge & timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (match.isWin) Color(0xFF22C55E).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (match.isWin) "VICTORY" else "DEFEAT",
                            color = if (match.isWin) Color(0xFF4ADE80) else Color(0xFFF87171),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Text(
                            text = match.matchMode,
                            color = Color(0xFF38BDF8),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (match.isSingleHand) {
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFA855F7).copy(alpha = 0.25f)) {
                            Text(
                                text = "SOLO 29",
                                color = Color(0xFFC084FC),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(text = dateStr, color = Color(0xFF64748B), fontSize = 10.sp)
            }

            // Score details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Bid: ${match.originalBid} pts by ${match.bidderPlayer} | Trump: ${match.trumpSuit}",
                        color = Color(0xFFE2E8F0),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Team Points: ${match.teamCardPoints} pts vs Opponents: ${match.opponentCardPoints} pts",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.5.sp
                    )
                }

                // Tokens Delta
                Text(
                    text = if (match.gamePointsDelta >= 0) "+${match.gamePointsDelta} Pips" else "${match.gamePointsDelta} Pips",
                    color = if (match.gamePointsDelta >= 0) Color(0xFF4ADE80) else Color(0xFFF87171),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Footer Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (match.pairDeclared) {
                        Text(text = "👑 Royal Pair", color = Color(0xFFFFD700), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }
                    if (match.isDoubled) {
                        Text(text = "⚡ 2X Double", color = Color(0xFFF59E0B), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }
                    if (match.isRedoubled) {
                        Text(text = "🔥 4X Redouble", color = Color(0xFFEF4444), fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "+${match.coinsEarned} 🪙", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(text = "+${match.xpEarned} XP", color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
