package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.HouseRules
import com.example.game.model.MatchMode
import com.example.ui.components.FeltTheme
import com.example.ui.components.LuxuryScreenBackground
import com.example.ui.components.luxuryInteractiveScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseRulesScreen(
    currentRules: HouseRules,
    currentTheme: FeltTheme,
    onSaveRules: (HouseRules) -> Unit,
    onSetTheme: (FeltTheme) -> Unit,
    onStartCustomMatch: () -> Unit,
    onOpenRulesGuide: () -> Unit = {},
    onBack: () -> Unit
) {
    var targetPips by remember { mutableStateOf(currentRules.targetPipsToWin) }
    var pairValue by remember { mutableStateOf(currentRules.pairValue) }
    var enableDouble by remember { mutableStateOf(currentRules.enableDoubleRedouble) }
    var enableSeventhCard by remember { mutableStateOf(currentRules.enableSeventhCardRule) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "House Rules",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "HOUSE RULES & CUSTOM MATCH",
                            color = Color(0xFFFFD700),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("rules_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = onOpenRulesGuide,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1B4B)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("📖 RULES GUIDE", color = Color(0xFFFFD700), fontSize = 10.5.sp, fontWeight = FontWeight.Black)
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
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Left Column: Rule Options
                Surface(
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0F172A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Game Targets & Scoring",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Target Victory Pips
                        Text("Target Match Pips to Win (Default: 6)", color = Color.White, fontSize = 11.5.sp)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            listOf(6, 8, 10).forEach { pips ->
                                val isSelected = targetPips == pips
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFFFFD700) else Color(0xFF1E293B))
                                        .clickable {
                                            targetPips = pips
                                            onSaveRules(currentRules.copy(targetPipsToWin = pips))
                                        }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "$pips Pips",
                                        color = if (isSelected) Color(0xFF0F172A) else Color(0xFFE2E8F0),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFF1E293B), modifier = Modifier.padding(vertical = 8.dp))

                        // Pair / Royalty Point Value
                        Text("Pair / Royalty Adjustment Value", color = Color.White, fontSize = 11.5.sp)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            listOf(4, 5).forEach { v ->
                                val isSelected = pairValue == v
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFFFFD700) else Color(0xFF1E293B))
                                        .clickable {
                                            pairValue = v
                                            onSaveRules(currentRules.copy(pairValue = v))
                                        }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "±$v Points ${if (v == 4) "(Standard)" else "(House)"}",
                                        color = if (isSelected) Color(0xFF0F172A) else Color(0xFFE2E8F0),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFF1E293B), modifier = Modifier.padding(vertical = 8.dp))

                        // Double / Redouble Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Allow Double & Redouble", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("2x & 4x stakes on contested bids", color = Color(0xFF94A3B8), fontSize = 10.5.sp)
                            }
                            Switch(
                                checked = enableDouble,
                                onCheckedChange = {
                                    enableDouble = it
                                    onSaveRules(currentRules.copy(enableDoubleRedouble = it))
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFFD700), checkedTrackColor = Color(0xFFD97706))
                            )
                        }
                    }
                }

                // Right Column: Felt Table Theme & Launch
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0F172A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Casino Felt Table Theme",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FeltTheme.entries.forEach { theme ->
                                    val isSelected = currentTheme == theme
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(theme.primary)
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) Color(0xFFFFD700) else Color(0xFF334155),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { onSetTheme(theme) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = theme.displayName.substringBefore(" "),
                                            color = Color.White,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = onStartCustomMatch,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .padding(top = 10.dp)
                                .luxuryInteractiveScale()
                                .testTag("start_custom_match_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color(0xFF0F172A),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "START CUSTOM MATCH",
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp
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
