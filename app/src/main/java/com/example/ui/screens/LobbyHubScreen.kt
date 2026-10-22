package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.game.model.AdPlacement
import com.example.game.model.MatchMode
import com.example.game.model.PlayerEconomy
import com.example.game.model.TournamentProgress
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun LobbyHubScreen(
    tournamentProgress: TournamentProgress,
    economy: PlayerEconomy = PlayerEconomy(),
    onSelectMode: (MatchMode) -> Unit,
    onOpenTournament: () -> Unit,
    onOpenAdventure: () -> Unit = {},
    onOpenAcademy: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenHouseRules: () -> Unit,
    onOpenStore: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenFriends: () -> Unit = {},
    onOpenSocialShare: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenRulesGuide: () -> Unit = {},
    onWatchRewardedAd: (AdPlacement) -> Unit = {},
    onClaimDailyReward: () -> Unit = {},
    hasSavedMatch: (MatchMode) -> Boolean = { false },
    onAskCoachBot: (suspend (String) -> String)? = null
) {
    var showAiCoachBot by remember { mutableStateOf(false) }
    var showDailyRewardDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showFreeDiamondsModal by remember { mutableStateOf(false) }
    var dailyRewardClaimed by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "lobby_fx")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val ambientGlow by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_glow"
    )

    val heroShimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hero_shimmer"
    )

    AnimatedCasinoBackground(
        modifier = Modifier.fillMaxSize(),
        imageResId = R.drawable.img_banner_casino,
        overlayAlpha = 0.48f,
        baseDarkColor = Color(0xFF04060B),
        enableFloatingSuits = true,
        enableSparkles = true,
        enableLightRays = true,
        enableBackdropMotion = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Panoramic Bar: Left = Brand + Level, Right = Diamonds + Coins + Settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title Brand with Crown & Shimmer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showProfileDialog = true }
                        .padding(2.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_icon),
                        contentDescription = "29 Royale App Icon",
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(10.dp, CircleShape, spotColor = CasinoGold)
                            .clip(CircleShape)
                            .border(1.5.dp, Color(0xFFFFF5C0), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "TWENTY-NINE ROYALE",
                                color = CasinoGold,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            AaaBadge(text = "LVL ${economy.level}", color = CasinoGold)
                        }
                        Text(
                            text = "${economy.vipTier.title} • Tap for Dossier",
                            color = CasinoDiamond,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Currency Pills & Settings
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AaaCurrencyPill(
                        type = CurrencyType.DIAMOND,
                        amount = economy.diamonds.toLong(),
                        onClick = { showFreeDiamondsModal = true }
                    )

                    AaaCurrencyPill(
                        type = CurrencyType.COIN,
                        amount = economy.coins,
                        onClick = { showDailyRewardDialog = true },
                        hasNotification = !dailyRewardClaimed
                    )

                    IconButton(
                        onClick = onOpenFriends,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .border(1.2.dp, CasinoGold.copy(alpha = 0.8f), CircleShape)
                            .testTag("nav_friends_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Friends Zone",
                            tint = CasinoGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .border(1.2.dp, CasinoGold.copy(alpha = 0.8f), CircleShape)
                            .testTag("nav_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = CasinoGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Menu Action Icons Row (Compact size and prioritized: 1. Classic/Play, 2. Rules Guide, 3. AI Coach, 4. Stats, 5. Store, 6. Adventures, 7. Academy, 8. House Rules, 9. Social Share)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Classic Play / Match (Top Priority)
                HubHeaderIconButton(
                    icon = Icons.Default.Casino,
                    contentDescription = "Classic Match",
                    tag = "nav_classic_match_button",
                    accentColor = Color(0xFF22C55E),
                    onClick = { onSelectMode(MatchMode.CLASSIC_29) }
                )

                // 2. 29 Interactive Rules Guide (High Priority)
                HubHeaderIconButton(
                    icon = Icons.Default.MenuBook,
                    contentDescription = "29 Interactive Rules Guide",
                    tag = "nav_rules_guide_button",
                    accentColor = Color(0xFFFFD700),
                    hasBadge = true,
                    onClick = onOpenRulesGuide
                )

                // 3. AI Coach Bot (High Priority)
                HubHeaderIconButton(
                    icon = Icons.Default.SmartToy,
                    contentDescription = "AI Coach Bot (Doubts & Rules)",
                    tag = "nav_ai_coach_button",
                    accentColor = Color(0xFF38BDF8),
                    hasBadge = true,
                    onClick = { showAiCoachBot = true }
                )

                // 4. Career & Records
                HubHeaderIconButton(
                    icon = Icons.Default.Leaderboard,
                    contentDescription = "Career & Records",
                    tag = "nav_stats_button",
                    accentColor = CasinoGold,
                    onClick = onOpenStats
                )

                // 5. Store Decks & Felt Themes
                HubHeaderIconButton(
                    icon = Icons.Default.ShoppingCart,
                    contentDescription = "Store (Decks & Themes)",
                    tag = "nav_store_button",
                    accentColor = Color(0xFFF59E0B),
                    onClick = onOpenStore
                )

                // 6. Adventures Campaign Quests
                HubHeaderIconButton(
                    icon = Icons.Default.Explore,
                    contentDescription = "Adventures Quests",
                    tag = "nav_adventure_button",
                    accentColor = Color(0xFFA855F7),
                    onClick = onOpenAdventure
                )

                // 7. Twenty-Nine Academy
                HubHeaderIconButton(
                    icon = Icons.Default.School,
                    contentDescription = "Academy Strategy",
                    tag = "nav_academy_button",
                    accentColor = Color(0xFF0EA5E9),
                    onClick = onOpenAcademy
                )

                // 8. House Rules Customization
                HubHeaderIconButton(
                    icon = Icons.Default.Tune,
                    contentDescription = "House Rules",
                    tag = "nav_rules_button",
                    accentColor = Color(0xFF10B981),
                    onClick = onOpenHouseRules
                )

                // 9. Share & Earn +20 Diamonds Social Hub
                HubHeaderIconButton(
                    icon = Icons.Default.Share,
                    contentDescription = "Share & Earn Free Diamonds",
                    tag = "nav_social_share_button",
                    accentColor = Color(0xFFEC4899),
                    hasBadge = true,
                    onClick = onOpenSocialShare
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive 29 Rules Master Guide Banner
            AaaGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clickable(onClick = onOpenRulesGuide),
                borderBrush = GoldMetallicBrush,
                glowColor = Color(0xFFFFD700)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF064E3B).copy(alpha = 0.35f),
                                    Color(0xFF0F172A).copy(alpha = 0.30f),
                                    Color(0xFF1E1B4B).copy(alpha = 0.35f)
                                )
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("📖", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "29 RULES & BIDDING MASTER GUIDE",
                                    color = CasinoGold,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                AaaBadge(text = "INTERACTIVE", color = CasinoGold, textColor = Color(0xFF0F172A))
                            }
                            Text(
                                text = "Learn card rank powers (J>9>A>10), secret trump reveal & bidding tricks",
                                color = Color(0xFFE2E8F0),
                                fontSize = 9.5.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Aaa3DButton(
                        text = "LEARN RULES",
                        onClick = onOpenRulesGuide,
                        variant = ButtonVariant.GLASS_GOLD,
                        isTranslucent = true,
                        height = 26.dp,
                        horizontalPadding = 10.dp,
                        verticalPadding = 3.dp,
                        fontSize = 10.sp,
                        modifier = Modifier.testTag("lobby_open_rules_guide_btn")
                    )
                }
            }

            // Quick Free Coins & Diamonds Sponsor Banner
            AaaGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                borderBrush = GoldMetallicBrush,
                glowColor = CasinoGold
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF1E1B4B).copy(alpha = 0.35f),
                                    Color(0xFF312E81).copy(alpha = 0.30f),
                                    Color(0xFF1E293B).copy(alpha = 0.35f)
                                )
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🪙", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "NEED COINS? WATCH SPONSOR AD",
                                    color = CasinoGold,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                AaaBadge(text = "+2,500 🪙", color = CasinoGold, textColor = Color(0xFF1E1B4B))
                            }
                            Text(
                                text = "Earn +2,500 Gold Coins and +1 Free Diamond per video ad",
                                color = Color(0xFFE2E8F0),
                                fontSize = 9.5.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Aaa3DButton(
                        text = "WATCH (+2.5K 🪙)",
                        onClick = { onWatchRewardedAd(AdPlacement.REWARDED_VAULT_DIAMONDS) },
                        variant = ButtonVariant.GLASS_GOLD,
                        isTranslucent = true,
                        height = 26.dp,
                        horizontalPadding = 10.dp,
                        verticalPadding = 3.dp,
                        fontSize = 10.sp,
                        modifier = Modifier.testTag("lobby_watch_coins_ad_btn")
                    )
                }
            }

            val isClassicSaved = hasSavedMatch(MatchMode.CLASSIC_29)

            // Hero Quick Play Action Card (Immediate 1-tap game launch with live shimmer)
            AaaGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectMode(MatchMode.CLASSIC_29) },
                borderBrush = Brush.horizontalGradient(
                    listOf(
                        CasinoGold,
                        Color(0xFFFFF5C0),
                        CasinoGold
                    )
                ),
                glowColor = CasinoGold
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF0B291A).copy(alpha = 0.95f),
                                    Color(0xFF064E3B).copy(alpha = 0.88f),
                                    Color(0xFF0F172A).copy(alpha = 0.96f)
                                )
                            )
                        )
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Live online indicator with pulsating dot
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(CasinoEmerald)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isClassicSaved) "SAVED MATCH IN PROGRESS • 4,820 PLAYERS ONLINE" else "LIVE ARENA • 4,820 PLAYERS ONLINE",
                                color = if (isClassicSaved) Color(0xFF4ADE80) else CasinoEmerald,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "♠ ♥ ♦ ♣", color = CasinoGold, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isClassicSaved) "RESUME CLASSIC 29 MATCH" else "START CLASSIC 29 MATCH",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.6.sp
                        )
                        Text(
                            text = if (isClassicSaved) "Resume your ongoing match right where you left off!" else "Play alongside partner Vikram AI vs Rohan & Aarav with authentic 29 rules & pips.",
                            color = Color(0xFFCBD5E1),
                            fontSize = 10.5.sp,
                            lineHeight = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Aaa3DButton(
                        text = if (isClassicSaved) "RESUME" else "PLAY NOW",
                        icon = if (isClassicSaved) Icons.Default.PlayCircle else Icons.Default.PlayArrow,
                        height = 30.dp,
                        horizontalPadding = 12.dp,
                        verticalPadding = 4.dp,
                        fontSize = 11.sp,
                        gradient = if (isClassicSaved) Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF34D399))) else GoldMetallicBrush,
                        textColor = Color(0xFF0F172A),
                        onClick = { onSelectMode(MatchMode.CLASSIC_29) },
                        testTag = "hero_start_game_button",
                        modifier = Modifier.scale(pulseScale)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Game Modes Header
            AaaSectionHeader(
                title = "SELECT ARENA MODE",
                subtitle = "Choose your table stakes & challenge rules",
                badge = "8 MODES"
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Royal Adventures Campaign
                item {
                    GameModeCard(
                        title = "Royal Adventures",
                        subtitle = "City Expeditions & Quests",
                        badge = "CAMPAIGN",
                        badgeColor = Color(0xFFA855F7),
                        imageRes = R.drawable.img_mode_tournament,
                        gradient = listOf(Color(0xFF581C87).copy(alpha = 0.85f), Color(0xFF2E1065).copy(alpha = 0.95f)),
                        icon = Icons.Default.Explore,
                        description = "Travel across Kolkata, Howrah, Dhaka & Delhi! Complete tactical tasks & earn 3★ stars.",
                        buttonText = "PLAY ADVENTURES",
                        tag = "mode_adventure_button",
                        onClick = onOpenAdventure
                    )
                }

                // 2. High Roller VIP
                item {
                    GameModeCard(
                        title = "High Roller VIP",
                        subtitle = "20+ Min Bid • Double Stakes",
                        badge = "VIP 💎",
                        badgeColor = Color(0xFF38BDF8),
                        imageRes = R.drawable.img_mode_classic,
                        gradient = listOf(Color(0xFF78350F).copy(alpha = 0.85f), Color(0xFF0F172A).copy(alpha = 0.95f)),
                        icon = Icons.Default.Diamond,
                        description = "High-roller arena with Lord Singhania & Raja Dhiraj. Starts at 20 bid with 3,000 Coin bounty!",
                        buttonText = "ENTER VIP ARENA",
                        tag = "mode_high_roller_button",
                        isSaved = hasSavedMatch(MatchMode.HIGH_ROLLER),
                        onClick = { onSelectMode(MatchMode.HIGH_ROLLER) }
                    )
                }

                // 3. Classic 29
                item {
                    GameModeCard(
                        title = "Classic 29",
                        subtitle = "Standard 4-Player Team",
                        badge = "POPULAR",
                        badgeColor = Color(0xFF22C55E),
                        imageRes = R.drawable.img_mode_classic,
                        gradient = listOf(Color(0xFF065F46).copy(alpha = 0.85f), Color(0xFF022C22).copy(alpha = 0.95f)),
                        icon = Icons.Default.Casino,
                        description = "Authentic 6-Pip Indian 29 with Vikram AI, Royal Pairs, & Match Pips.",
                        buttonText = "PLAY CLASSIC",
                        tag = "mode_classic_button",
                        isSaved = hasSavedMatch(MatchMode.CLASSIC_29),
                        onClick = { onSelectMode(MatchMode.CLASSIC_29) }
                    )
                }

                // 4. Speed Blitz 60s
                item {
                    GameModeCard(
                        title = "Speed Blitz 60s",
                        subtitle = "Lightning-Fast AI Turns",
                        badge = "LIGHTNING ⚡",
                        badgeColor = Color(0xFFFBBF24),
                        imageRes = R.drawable.img_mode_blitz,
                        gradient = listOf(Color(0xFF92400E).copy(alpha = 0.85f), Color(0xFF0C4A6E).copy(alpha = 0.95f)),
                        icon = Icons.Default.FlashOn,
                        description = "Ultra-rapid turns! Lightning-fast AI plays and instant sudden death 1-round resolution.",
                        buttonText = "PLAY SPEED BLITZ",
                        tag = "mode_speed_blitz_button",
                        isSaved = hasSavedMatch(MatchMode.SPEED_BLITZ),
                        onClick = { onSelectMode(MatchMode.SPEED_BLITZ) }
                    )
                }

                // 5. Grand 29 Cup Tournament
                item {
                    GameModeCard(
                        title = "Grand 29 Cup",
                        subtitle = "Championship Bracket",
                        badge = "STAGE ${tournamentProgress.currentStage.stageNumber}/3",
                        badgeColor = CasinoGold,
                        imageRes = R.drawable.img_mode_tournament,
                        gradient = listOf(Color(0xFF78350F).copy(alpha = 0.85f), Color(0xFF451A03).copy(alpha = 0.95f)),
                        icon = Icons.Default.EmojiEvents,
                        description = "Knockout Cup vs Kolkata Knights, Mumbai Royals, and Delhi Masters.",
                        buttonText = "TOURNAMENT",
                        tag = "mode_tournament_button",
                        onClick = onOpenTournament
                    )
                }

                // 6. Royal Showdown
                item {
                    GameModeCard(
                        title = "Royal Showdown",
                        subtitle = "Fast 4-Pip Duel vs Masters",
                        badge = "ELITE 👑",
                        badgeColor = Color(0xFFEC4899),
                        imageRes = R.drawable.img_mode_single_hand,
                        gradient = listOf(Color(0xFF831843).copy(alpha = 0.85f), Color(0xFF3B0764).copy(alpha = 0.95f)),
                        icon = Icons.Default.MilitaryTech,
                        description = "High-intensity 4-pip championship duel against Master Rudra & Arya. Decisive and intense.",
                        buttonText = "PLAY SHOWDOWN",
                        tag = "mode_royal_duel_button",
                        isSaved = hasSavedMatch(MatchMode.ROYAL_DUEL),
                        onClick = { onSelectMode(MatchMode.ROYAL_DUEL) }
                    )
                }

                // 7. Single Hand Solo 29
                item {
                    GameModeCard(
                        title = "Single Hand 29",
                        subtitle = "Solo 1 vs 2 Challenge",
                        badge = "HARDCORE",
                        badgeColor = Color(0xFFEF4444),
                        imageRes = R.drawable.img_mode_single_hand,
                        gradient = listOf(Color(0xFF881337).copy(alpha = 0.85f), Color(0xFF4C0519).copy(alpha = 0.95f)),
                        icon = Icons.Default.Whatshot,
                        description = "Bid 29 solo! Win all 8 tricks without partner help for ultimate prestige.",
                        buttonText = "PLAY SOLO 29",
                        tag = "mode_single_hand_button",
                        isSaved = hasSavedMatch(MatchMode.SINGLE_HAND),
                        onClick = { onSelectMode(MatchMode.SINGLE_HAND) }
                    )
                }

                // 8. Quick Blitz (1 Round)
                item {
                    GameModeCard(
                        title = "Quick Blitz",
                        subtitle = "1-Round Sudden Death",
                        badge = "2X STAKES",
                        badgeColor = Color(0xFF38BDF8),
                        imageRes = R.drawable.img_mode_blitz,
                        gradient = listOf(Color(0xFF0369A1).copy(alpha = 0.85f), Color(0xFF082F49).copy(alpha = 0.95f)),
                        icon = Icons.Default.Bolt,
                        description = "Fast single round showdown with 2x point multipliers for quick sessions.",
                        buttonText = "PLAY BLITZ",
                        tag = "mode_blitz_button",
                        isSaved = hasSavedMatch(MatchMode.BLITZ_ROUND),
                        onClick = { onSelectMode(MatchMode.BLITZ_ROUND) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Daily Challenge & Live Academy Tip Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Daily Quest Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(10.dp, RoundedCornerShape(14.dp), spotColor = Color(0xFFF59E0B).copy(alpha = 0.3f))
                        .clickable { showDailyRewardDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0F172A),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFF59E0B).copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF78350F).copy(alpha = 0.2f), Color(0xFF0F172A))
                                )
                            )
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(Color(0xFFF59E0B).copy(alpha = 0.25f))
                                .border(1.dp, Color(0xFFF59E0B), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = "Quest",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (dailyRewardClaimed) "DAILY REWARD CLAIMED ✓" else "CLAIM DAILY BONUS 🎁",
                                color = if (dailyRewardClaimed) Color(0xFF22C55E) else Color(0xFFF59E0B),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Tap to claim +1,000 daily coins & card bonus",
                                color = Color(0xFFE2E8F0),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // AI Grandmaster Tip Card
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(10.dp, RoundedCornerShape(14.dp), spotColor = CasinoGold.copy(alpha = 0.3f))
                        .clickable(onClick = onOpenAcademy),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0F172A),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, CasinoGold.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFB45309).copy(alpha = 0.2f), Color(0xFF0F172A))
                                )
                            )
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(CasinoGold.copy(alpha = 0.25f))
                                .border(1.dp, CasinoGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Coach Tip",
                                tint = CasinoGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "GRANDMASTER VIKRAM TIP",
                                color = CasinoGold,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Jacks are worth 3 pts each. Protect them until Trump is revealed!",
                                color = Color(0xFFE2E8F0),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // AdMob Sponsor Banner Unit
            AdMobBanner(modifier = Modifier.fillMaxWidth())
        }

        // Daily Bonus Claim Modal
        if (showDailyRewardDialog) {
            DailyRewardModal(
                isClaimed = dailyRewardClaimed,
                onClaim = {
                    dailyRewardClaimed = true
                    onClaimDailyReward()
                },
                onWatchRewardedAd = {
                    onWatchRewardedAd(AdPlacement.REWARDED_VAULT_DIAMONDS)
                },
                onDismiss = { showDailyRewardDialog = false }
            )
        }

        // Player Profile Modal
        if (showProfileDialog) {
            PlayerProfileModal(
                economy = economy,
                onOpenProfile = onOpenProfile,
                onDismiss = { showProfileDialog = false }
            )
        }

        // Free Diamonds Rewarded Ad Modal
        if (showFreeDiamondsModal) {
            FreeDiamondsModal(
                economy = economy,
                onWatchAd = { act ->
                    onWatchRewardedAd(AdPlacement.REWARDED_VAULT_DIAMONDS)
                },
                onDismiss = { showFreeDiamondsModal = false }
            )
        }

        // AI Coach Bot Dialog (short window for rules & asking doubts)
        AiCoachBotDialog(
            isOpen = showAiCoachBot,
            onDismiss = { showAiCoachBot = false },
            onAskGeminiQuery = onAskCoachBot
        )
    }
}

@Composable
private fun DailyRewardModal(
    isClaimed: Boolean,
    onClaim: () -> Unit,
    onWatchRewardedAd: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .shadow(28.dp, RoundedCornerShape(20.dp), spotColor = CasinoGold)
                .border(2.dp, GoldMetallicBrush, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A)
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF080C16))
                        )
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎁 7-DAY REWARD STREAK",
                    color = CasinoGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Log in daily to claim escalating coin rewards & custom card backs!",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 7 Days Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val days = listOf("D1\n500", "D2\n750", "D3\n1,000", "D4\n1,250", "D5\n1,500", "D6\n2,000", "D7\n💎 5K")
                    days.forEachIndexed { idx, dayText ->
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = if (idx == 0) Color(0xFFB45309) else Color(0xFF1E293B),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (idx == 0) CasinoGold else Color(0xFF334155)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = dayText,
                                    color = if (idx == 0) CasinoGold else Color(0xFFCBD5E1),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Black,
                                    lineHeight = 12.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Rewarded Ad Diamond & Coin Vault Button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onWatchRewardedAd()
                            onDismiss()
                        }
                        .border(1.dp, CasinoGold, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E1B4B).copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎬", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("SPONSOR VIDEO COIN VAULT", color = CasinoGold, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                Text("Watch short video for +2,500 Coins & +1 Diamond", color = Color(0xFFE2E8F0), fontSize = 9.5.sp)
                            }
                        }
                        AaaBadge(text = "+2.5K 🪙", color = CasinoGold, textColor = Color(0xFF1E1B4B))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("CLOSE", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Aaa3DButton(
                        text = if (isClaimed) "CLAIMED ✓" else "CLAIM +1,000 COINS",
                        onClick = {
                            if (!isClaimed) {
                                onClaim()
                            }
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.6f),
                        enabled = !isClaimed,
                        gradient = GoldMetallicBrush,
                        textColor = Color(0xFF0F172A)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerProfileModal(
    economy: PlayerEconomy,
    onOpenProfile: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .shadow(28.dp, RoundedCornerShape(20.dp), spotColor = CasinoDiamond)
                .border(2.dp, DiamondGlowBrush, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A)
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF080C16))
                        )
                    )
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "PLAYER DOSSIER",
                            color = CasinoDiamond,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        AaaBadge(text = economy.vipTier.title, color = CasinoGold)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Avatar & Name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(10.dp, CircleShape, spotColor = CasinoGold)
                            .clip(CircleShape)
                            .border(2.dp, GoldMetallicBrush, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_avatar_player),
                            contentDescription = "Player Profile Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Grandmaster Player", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
                        Text(economy.vipTier.title, color = CasinoGold, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        Text("Level ${economy.level} • XP: ${economy.currentXp}/${economy.xpForNextLevel}", color = Color(0xFF94A3B8), fontSize = 10.5.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("COINS", color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("%,d".format(economy.coins), color = CasinoGold, fontSize = 13.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("DIAMONDS", color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("💎 ${economy.diamonds}", color = CasinoDiamond, fontSize = 13.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TROPHIES", color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("2,450", color = Color(0xFF4ADE80), fontSize = 13.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Aaa3DButton(
                    text = "VIEW PROFILE & CAREER STATS",
                    onClick = {
                        onDismiss()
                        onOpenProfile()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    gradient = DiamondGlowBrush,
                    textColor = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Close Dossier", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun HubHeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    tag: String,
    accentColor: Color = CasinoGold,
    hasBadge: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .sizeIn(minWidth = 34.dp, minHeight = 34.dp)
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .luxuryInteractiveScale()
            .clickable(onClick = onClick)
            .testTag(tag),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1E293B).copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, accentColor.copy(alpha = 0.7f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = accentColor,
                modifier = Modifier.size(17.dp)
            )
            if (hasBadge) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .align(Alignment.TopEnd)
                        .padding(top = 3.dp, end = 3.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF38BDF8))
                        .border(0.8.dp, Color.White, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun GameModeCard(
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    imageRes: Int,
    gradient: List<Color>,
    icon: ImageVector,
    description: String,
    buttonText: String,
    tag: String,
    isSaved: Boolean = false,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "card_glow")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_glow"
    )

    Surface(
        modifier = Modifier
            .width(240.dp)
            .height(195.dp)
            .shadow(18.dp, RoundedCornerShape(16.dp), spotColor = badgeColor.copy(alpha = 0.4f))
            .border(
                1.5.dp,
                Brush.sweepGradient(
                    listOf(
                        CasinoGold.copy(alpha = borderAlpha),
                        badgeColor.copy(alpha = borderAlpha * 0.8f),
                        CasinoGold.copy(alpha = borderAlpha)
                    )
                ),
                RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(tag),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F172A)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Artwork Image with Scrim
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dark Rich Gradient Scrim Overlay for Contrast & Readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF0F172A).copy(alpha = 0.55f),
                                Color(0xFF090D16).copy(alpha = 0.85f),
                                Color(0xFF030712).copy(alpha = 0.96f)
                            )
                        )
                    )
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header with Icon & Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0F172A).copy(alpha = 0.8f))
                                    .border(1.dp, CasinoGold.copy(alpha = 0.8f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title,
                                    tint = CasinoGold,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = title,
                                color = Color.White,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.3.sp
                            )
                        }

                        if (isSaved) {
                            AaaBadge(text = "💾 RESUME", color = Color(0xFF22C55E))
                        } else {
                            AaaBadge(text = badge, color = badgeColor)
                        }
                    }

                    Text(
                        text = description,
                        color = Color(0xFFF1F5F9),
                        fontSize = 9.5.sp,
                        lineHeight = 13.5.sp,
                        maxLines = 3,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    Aaa3DButton(
                        text = if (isSaved) "RESUME MATCH" else buttonText,
                        icon = if (isSaved) Icons.Default.PlayCircle else Icons.Default.PlayArrow,
                        onClick = onClick,
                        modifier = Modifier.fillMaxWidth(),
                        height = 28.dp,
                        horizontalPadding = 10.dp,
                        verticalPadding = 3.dp,
                        fontSize = 10.5.sp,
                        gradient = if (isSaved) Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF34D399))) else GoldMetallicBrush,
                        textColor = Color(0xFF0F172A),
                        testTag = tag
                    )
                }
            }
        }
    }
}
