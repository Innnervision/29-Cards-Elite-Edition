package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.ads.AdConfig
import com.example.game.model.AdventureLevel
import com.example.game.model.AdventureProgress
import com.example.game.model.PlayerEconomy
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun AdventureScreen(
    adventureProgress: AdventureProgress,
    economy: PlayerEconomy,
    onStartLevel: (AdventureLevel) -> Unit,
    onUnlockWithDiamonds: (Int) -> Boolean,
    onWatchAdForDiamonds: (Activity) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var showInsufficientDiamondsModal by remember { mutableStateOf(false) }
    var showFreeDiamondsModal by remember { mutableStateOf(false) }

    var selectedLevelNumber by remember {
        mutableStateOf(
            adventureProgress.levels.firstOrNull { it.isUnlocked && !it.isCompleted }?.levelNumber
                ?: adventureProgress.levels.firstOrNull { it.isUnlocked }?.levelNumber
                ?: 1
        )
    }

    val selectedLevel = adventureProgress.levels.firstOrNull { it.levelNumber == selectedLevelNumber }
        ?: adventureProgress.levels.first()

    LuxuryScreenBackground(
        modifier = Modifier
            .fillMaxSize()
            .testTag("adventure_screen"),
        imageResId = R.drawable.img_mode_classic,
        overlayAlpha = 0.50f
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Top Bar with Back Button & Currencies
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AaaBackButton(
                        onClick = onBack,
                        label = "LOBBY",
                        testTag = "adventure_back_button"
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = "Adventures",
                                tint = CasinoGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ROYAL 29 ADVENTURES",
                                color = CasinoGold,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            )
                        }
                        Text(
                            text = "Level Journey • Conquer Cities & Complete Tasks",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Total Stars Badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CasinoGold.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⭐", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${adventureProgress.totalStars} / ${adventureProgress.levels.size * 3} STARS",
                                color = CasinoGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    AaaCurrencyPill(
                        type = CurrencyType.DIAMOND,
                        amount = economy.diamonds.toLong(),
                        onClick = { showFreeDiamondsModal = true }
                    )
                    AaaCurrencyPill(
                        type = CurrencyType.COIN,
                        amount = economy.coins
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Content: Left/Top Level Rail, Right/Bottom Level Dossier
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left Column: Level Nodes Rail (Scrollable)
                AaaGlassCard(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight(),
                    glowColor = CasinoGold.copy(alpha = 0.15f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    ) {
                        AaaSectionHeader(
                            title = "EXPEDITION STATIONS",
                            subtitle = "${adventureProgress.levelsCompleted} of ${adventureProgress.levels.size} Levels Cleared",
                            badge = "CAMPAIGN"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(adventureProgress.levels) { level ->
                                val isSelected = level.levelNumber == selectedLevelNumber

                                AdventureLevelItemCard(
                                    level = level,
                                    isSelected = isSelected,
                                    onClick = { selectedLevelNumber = level.levelNumber }
                                )
                            }
                        }
                    }
                }

                // Right Column: Selected Level Details & Dossier
                AaaGlassCard(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight(),
                    glowColor = if (selectedLevel.isCompleted) Color(0xFF22C55E).copy(alpha = 0.2f) else CasinoGold.copy(alpha = 0.25f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            // Header Banner
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = selectedLevel.chapter.uppercase(),
                                        color = CasinoDiamond,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "LEVEL ${selectedLevel.levelNumber}: ${selectedLevel.title}",
                                        color = Color.White,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "${selectedLevel.city} • ${selectedLevel.subtitle}",
                                        color = CasinoGold,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                // Status Badge
                                when {
                                    !selectedLevel.isUnlocked -> {
                                        AaaBadge(text = "LOCKED 🔒", color = Color(0xFFEF4444))
                                    }
                                    selectedLevel.isCompleted -> {
                                        AaaBadge(text = "CLEARED 👑", color = Color(0xFF22C55E))
                                    }
                                    else -> {
                                        AaaBadge(text = "READY ⚔️", color = CasinoGold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Lore Narrative
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF0F172A).copy(alpha = 0.8f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = CasinoDiamond,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = selectedLevel.loreDescription,
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Opponents & Stakes Pill Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF1E293B)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("RIVALS", color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text("${selectedLevel.opponent1Name} & ${selectedLevel.opponent2Name}", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                }

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF1E293B)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("PARTNER", color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text(selectedLevel.partnerName, color = CasinoGold, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF1E293B)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("TARGET", color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        Text("${selectedLevel.targetPips} Pips to Win", color = Color(0xFF4ADE80), fontSize = 10.5.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Level Tasks / Objectives Checklist
                            Text(
                                text = "LEVEL TASKS (EARN 3★ STARS)",
                                color = CasinoGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                selectedLevel.tasks.forEachIndexed { index, task ->
                                    val isStarAchieved = index < selectedLevel.starsEarned || task.isCompleted

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isStarAchieved) Color(0xFF064E3B).copy(alpha = 0.5f) else Color(0xFF1E293B).copy(alpha = 0.7f),
                                        border = androidx.compose.foundation.BorderStroke(
                                            0.8.dp,
                                            if (isStarAchieved) Color(0xFF10B981) else Color(0xFF334155)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = if (isStarAchieved) "⭐" else "☆",
                                                    fontSize = 14.sp
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = task.description,
                                                    color = if (isStarAchieved) Color(0xFFE2E8F0) else Color(0xFF94A3B8),
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isStarAchieved) FontWeight.SemiBold else FontWeight.Normal
                                                )
                                            }

                                            if (isStarAchieved) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Completed",
                                                    tint = Color(0xFF22C55E),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            } else {
                                                Text(
                                                    text = task.rewardText,
                                                    color = CasinoGold,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Bottom Footer: Rewards & Launch Button
                        Column {
                            // Rewards Banner
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("CLEAR REWARDS:", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("🪙 +${selectedLevel.rewardCoins}", color = CasinoGold, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("💎 +${selectedLevel.rewardDiamonds}", color = CasinoDiamond, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("⚡ +${selectedLevel.rewardXp} XP", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }

                                if (selectedLevel.starsEarned > 0) {
                                    Row {
                                        repeat(3) { starIndex ->
                                            Text(
                                                text = if (starIndex < selectedLevel.starsEarned) "⭐" else "☆",
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            if (selectedLevel.isUnlocked) {
                                Aaa3DButton(
                                    text = if (selectedLevel.isCompleted) "REPLAY LEVEL ${selectedLevel.levelNumber}" else "START LEVEL ${selectedLevel.levelNumber}",
                                    icon = Icons.Default.PlayArrow,
                                    variant = ButtonVariant.GOLD,
                                    height = 42.dp,
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { onStartLevel(selectedLevel) },
                                    testTag = "start_adventure_level_button"
                                )
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Aaa3DButton(
                                            text = "UNLOCK (💎 5)",
                                            icon = Icons.Default.LockOpen,
                                            variant = ButtonVariant.DIAMOND,
                                            height = 40.dp,
                                            modifier = Modifier
                                                .weight(1.2f)
                                                .testTag("btn_unlock_level_diamonds"),
                                            onClick = {
                                                if (economy.diamonds >= AdConfig.DIAMOND_UNLOCK_LEVEL_COST) {
                                                    onUnlockWithDiamonds(selectedLevel.levelNumber)
                                                } else {
                                                    showInsufficientDiamondsModal = true
                                                }
                                            }
                                        )

                                        Aaa3DButton(
                                            text = "AD (+1 💎)",
                                            icon = Icons.Default.PlayArrow,
                                            variant = ButtonVariant.EMERALD,
                                            height = 40.dp,
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("btn_watch_ad_for_level"),
                                            onClick = {
                                                if (activity != null) {
                                                    onWatchAdForDiamonds(activity)
                                                }
                                            }
                                        )
                                    }

                                    Text(
                                        text = "Complete Level ${selectedLevel.levelNumber - 1} or unlock with 5 Diamonds",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Non-gameplay AdMob banner
            AdMobBanner(modifier = Modifier.fillMaxWidth())
        }

        if (showInsufficientDiamondsModal) {
            InsufficientDiamondsModal(
                requiredDiamonds = AdConfig.DIAMOND_UNLOCK_LEVEL_COST,
                currentDiamonds = economy.diamonds,
                itemName = "Level ${selectedLevel.levelNumber}",
                onWatchAd = {
                    showInsufficientDiamondsModal = false
                    if (activity != null) {
                        onWatchAdForDiamonds(activity)
                    }
                },
                onDismiss = { showInsufficientDiamondsModal = false }
            )
        }

        if (showFreeDiamondsModal) {
            FreeDiamondsModal(
                economy = economy,
                onWatchAd = { act ->
                    onWatchAdForDiamonds(act)
                },
                onDismiss = { showFreeDiamondsModal = false }
            )
        }
    }
}

@Composable
private fun AdventureLevelItemCard(
    level: AdventureLevel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("adventure_level_card_${level.levelNumber}"),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFF1E293B) else Color(0xFF0F172A).copy(alpha = 0.8f),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) CasinoGold else if (level.isCompleted) Color(0xFF22C55E).copy(alpha = 0.5f) else Color(0xFF334155)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                !level.isUnlocked -> Color(0xFF334155)
                                level.isCompleted -> Color(0xFF065F46)
                                else -> Color(0xFF78350F)
                            }
                        )
                        .border(
                            1.dp,
                            when {
                                !level.isUnlocked -> Color(0xFF64748B)
                                level.isCompleted -> Color(0xFF34D399)
                                else -> CasinoGold
                            },
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (!level.isUnlocked) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    } else if (level.isCompleted) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    } else {
                        Text(
                            text = "${level.levelNumber}",
                            color = CasinoGold,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "LVL ${level.levelNumber}: ${level.city}",
                        color = if (level.isUnlocked) Color.White else Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = level.subtitle,
                        color = if (isSelected) CasinoGold else Color(0xFF64748B),
                        fontSize = 10.sp
                    )
                }
            }

            // Star Rating
            Row {
                repeat(3) { index ->
                    Text(
                        text = if (index < level.starsEarned) "⭐" else "☆",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
