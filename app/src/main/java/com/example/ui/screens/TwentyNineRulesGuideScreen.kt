package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.Card
import com.example.game.model.Rank
import com.example.game.model.Suit
import com.example.ui.components.*
import com.example.ui.theme.*

enum class GuideTab(val title: String, val iconEmoji: String) {
    OVERVIEW("Overview", "📜"),
    CARD_RANKS("Card Ranks & Points", "🃏"),
    BIDDING("Bidding & Contracts", "🗣️"),
    TRUMP_REVEAL("Trump & Reveal", "👑"),
    TRICK_SIMULATOR("Trick Simulator", "⚔️"),
    SCORING("Scoring & Pips", "🏆"),
    QUIZ("Mastery Quiz", "🎯")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwentyNineRulesGuideScreen(
    onBack: () -> Unit,
    initialTab: GuideTab = GuideTab.OVERVIEW
) {
    var activeTab by remember { mutableStateOf(initialTab) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldMetallicBrush),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("29", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "29 INTERACTIVE RULES GUIDE",
                                color = CasinoGold,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                "Master Bidding, Trumps & Scoring",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("rules_guide_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0B0F19),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF050811)
    ) { padding ->
        LuxuryScreenBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Interactive Scrollable Tab Row
                TabNavigationHeader(
                    activeTab = activeTab,
                    onTabSelected = { activeTab = it }
                )

                // Main Tab Content Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AnimatedContent(
                        targetState = activeTab,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.98f, animationSpec = tween(220)))
                                .togetherWith(fadeOut(animationSpec = tween(180)))
                        },
                        label = "guide_tab_transition"
                    ) { tab ->
                        when (tab) {
                            GuideTab.OVERVIEW -> OverviewTabSection(
                                onNavigateToTab = { activeTab = it }
                            )
                            GuideTab.CARD_RANKS -> CardRanksTabSection()
                            GuideTab.BIDDING -> BiddingTabSection()
                            GuideTab.TRUMP_REVEAL -> TrumpRevealTabSection()
                            GuideTab.TRICK_SIMULATOR -> TrickSimulatorTabSection()
                            GuideTab.SCORING -> ScoringPipsTabSection()
                            GuideTab.QUIZ -> RulesMasteryQuizTabSection()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabNavigationHeader(
    activeTab: GuideTab,
    onTabSelected: (GuideTab) -> Unit
) {
    Surface(
        color = Color(0xFF0B101D).copy(alpha = 0.95f),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.8.dp, Color(0xFF1E293B), RoundedCornerShape(0.dp))
    ) {
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GuideTab.values().forEach { tab ->
                val isSelected = activeTab == tab
                val tabBorder = if (isSelected) GoldMetallicBrush else Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
                val tabBg = if (isSelected) Brush.verticalGradient(listOf(Color(0xFF2E1065), Color(0xFF1E1B4B)))
                            else Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF090D16)))

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .border(if (isSelected) 1.2.dp else 0.8.dp, tabBorder, RoundedCornerShape(20.dp))
                        .clickable { onTabSelected(tab) }
                        .testTag("tab_${tab.name.lowercase()}"),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .background(tabBg)
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(tab.iconEmoji, fontSize = 13.sp)
                        Text(
                            text = tab.title,
                            color = if (isSelected) CasinoGold else Color(0xFF94A3B8),
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// TAB 1: OVERVIEW & ESSENTIALS
// -----------------------------------------------------------------------------------------
@Composable
private fun OverviewTabSection(
    onNavigateToTab: (GuideTab) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Hero Intro Card
            AaaGlassCard(
                borderBrush = GoldMetallicBrush,
                glowColor = CasinoGold
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFFFFD700), Color(0xFFB45309))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("♠️", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "WELCOME TO 29 (TWENTY-NINE)",
                                color = CasinoGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            AaaBadge(text = "SOUTH ASIA #1", color = Color(0xFFEF4444), textColor = Color.White)
                        }
                        Text(
                            "A strategic 4-player trick-taking game played in fixed 2v2 partnerships with hidden trumps and dynamic bidding.",
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        item {
            // 4 Pillars of 29 Grid
            AaaSectionHeader(title = "CORE GAME PILLARS", badge = "4 ESSENTIALS")
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PillarSummaryCard(
                        modifier = Modifier.weight(1f),
                        emoji = "👥",
                        title = "4 Players in 2 Teams",
                        subtitle = "North & South vs East & West. You always cooperate with your opposite partner."
                    )
                    PillarSummaryCard(
                        modifier = Modifier.weight(1f),
                        emoji = "🎴",
                        title = "32-Card Deck",
                        subtitle = "Only 8 cards per suit (7, 8, Q, K, 10, A, 9, J). 2s through 6s are removed."
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PillarSummaryCard(
                        modifier = Modifier.weight(1f),
                        emoji = "🔢",
                        title = "Why 29 Points?",
                        subtitle = "28 points in card values (J=3, 9=2, A=1, 10=1) + 1 bonus point for winning the last trick!"
                    )
                    PillarSummaryCard(
                        modifier = Modifier.weight(1f),
                        emoji = "🤐",
                        title = "Hidden Secret Trump",
                        subtitle = "The highest bidder places a secret trump card face down until legally revealed during play."
                    )
                }
            }
        }

        item {
            // Quick Flow Diagram
            AaaGlassCard {
                Text("ROUND SEQUENCE FLOW", color = CasinoGold, fontSize = 12.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(8.dp))

                SequenceStepItem(step = 1, title = "Deal First 4 Cards", desc = "Dealer distributes 4 cards to each of the 4 players.")
                SequenceStepItem(step = 2, title = "Bidding Phase", desc = "Players bid between 16 and 28 (or 29/Single Hand). Highest bidder sets secret trump.")
                SequenceStepItem(step = 3, title = "Deal Second 4 Cards", desc = "Dealer distributes the remaining 4 cards. Total 8 cards per player.")
                SequenceStepItem(step = 4, title = "8 Trick Rounds", desc = "Players lead & follow suit. Trumps can be requested when void to cut tricks.")
                SequenceStepItem(step = 5, title = "Scoring & Red/Black Pips", desc = "Bidder team must reach bid points to gain +1 Game Point (or lose -1 / -2 / -4).")
            }
        }

        item {
            // Quick Launch Action Cards to Deep-Dive Tabs
            AaaSectionHeader(title = "EXPLORE INTERACTIVE GUIDES", badge = "TAP TO LEARN")
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickGuideLinkCard(
                    emoji = "🃏",
                    title = "Card Ranking & Point Values",
                    desc = "Why Jacks (3 pts) and 9s (2 pts) rule the table. Try the interactive rank tester.",
                    badge = "INTERACTIVE",
                    onClick = { onNavigateToTab(GuideTab.CARD_RANKS) }
                )
                QuickGuideLinkCard(
                    emoji = "🗣️",
                    title = "Bidding Tactics & Hand Evaluator",
                    desc = "Learn how to choose contract targets (16-29), doubles, and evaluate opening hands.",
                    badge = "SIMULATOR",
                    onClick = { onNavigateToTab(GuideTab.BIDDING) }
                )
                QuickGuideLinkCard(
                    emoji = "👑",
                    title = "Secret Trump & Reveal Rules",
                    desc = "Master the precise conditions for revealing trumps and claiming Roy (K+Q marriage).",
                    badge = "FLIP CARDS",
                    onClick = { onNavigateToTab(GuideTab.TRUMP_REVEAL) }
                )
                QuickGuideLinkCard(
                    emoji = "⚔️",
                    title = "Live Trick-Taking Simulator",
                    desc = "Step through hands turn-by-turn to see how lead suits and trumps win tricks.",
                    badge = "WALKTHROUGH",
                    onClick = { onNavigateToTab(GuideTab.TRICK_SIMULATOR) }
                )
            }
        }
    }
}

@Composable
private fun PillarSummaryCard(
    modifier: Modifier,
    emoji: String,
    title: String,
    subtitle: String
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)),
        color = Color(0xFF0F172A).copy(alpha = 0.85f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(emoji, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, color = CasinoGold, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = Color(0xFF94A3B8), fontSize = 10.sp, lineHeight = 13.sp)
        }
    }
}

@Composable
private fun SequenceStepItem(step: Int, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFF38BDF8), Color(0xFF0284C7)))),
            contentAlignment = Alignment.Center
        ) {
            Text("$step", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            Text(desc, color = Color(0xFF94A3B8), fontSize = 10.sp)
        }
    }
}

@Composable
private fun QuickGuideLinkCard(
    emoji: String,
    title: String,
    desc: String,
    badge: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = Color(0xFF0F172A).copy(alpha = 0.9f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    AaaBadge(text = badge, color = CasinoGold, textColor = Color(0xFF0F172A))
                }
                Text(desc, color = Color(0xFF94A3B8), fontSize = 10.sp, lineHeight = 13.sp)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = CasinoGold,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// -----------------------------------------------------------------------------------------
// TAB 2: CARD RANKS & POINTS EXPLORER
// -----------------------------------------------------------------------------------------
@Composable
private fun CardRanksTabSection() {
    var selectedRank by remember { mutableStateOf(Rank.JACK) }
    var comparisonCard1 by remember { mutableStateOf(Card(Suit.SPADES, Rank.JACK)) }
    var comparisonCard2 by remember { mutableStateOf(Card(Suit.SPADES, Rank.NINE)) }
    var testTrumpSuit by remember { mutableStateOf<Suit?>(Suit.HEARTS) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Golden Math Rule Header
            AaaGlassCard(
                borderBrush = GoldMetallicBrush,
                glowColor = CasinoGold
            ) {
                Text(
                    "THE 29 POINT FORMULA",
                    color = CasinoGold,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "In 29, only Jacks, 9s, Aces, and 10s carry point values. Kings, Queens, 8s, and 7s have 0 points but can win tricks by rank or as trumps!",
                    color = Color(0xFFE2E8F0),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0B101D), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PointSummaryPill(rank = "J (Jack)", pts = "3 pts", count = "× 4 = 12 pts", color = Color(0xFFFFD700))
                    PointSummaryPill(rank = "9 (Nine)", pts = "2 pts", count = "× 4 = 8 pts", color = Color(0xFF38BDF8))
                    PointSummaryPill(rank = "A (Ace)", pts = "1 pt", count = "× 4 = 4 pts", color = Color(0xFF4ADE80))
                    PointSummaryPill(rank = "10 (Ten)", pts = "1 pt", count = "× 4 = 4 pts", color = Color(0xFFA78BFA))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "28 Deck Points + 1 Last Trick Point = 29 Total Points 🏆",
                        color = CasinoGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            // Interactive Rank Selector Grid
            AaaSectionHeader(
                title = "INTERACTIVE CARD RANK EXPLORER",
                subtitle = "Tap any card to view its power rank and strategic role",
                badge = "TAP CARD"
            )
            Spacer(modifier = Modifier.height(6.dp))

            val ranksInOrder = listOf(
                Rank.JACK, Rank.NINE, Rank.ACE, Rank.TEN,
                Rank.KING, Rank.QUEEN, Rank.EIGHT, Rank.SEVEN
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ranksInOrder) { rank ->
                    val isSelected = selectedRank == rank
                    val sampleCard = Card(Suit.SPADES, rank)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedRank = rank }
                            .padding(2.dp)
                    ) {
                        PlayingCard3DView(
                            card = sampleCard,
                            isSelected = isSelected,
                            isPlayable = true,
                            width = 54.dp,
                            height = 76.dp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        AaaBadge(
                            text = "${rank.points} PTS",
                            color = if (rank.points > 0) CasinoGold else Color(0xFF64748B),
                            textColor = if (rank.points > 0) Color(0xFF0F172A) else Color.White
                        )
                    }
                }
            }
        }

        item {
            // Selected Rank Deep Dive Detail Card
            RankDetailBreakdownCard(selectedRank)
        }

        item {
            // Interactive 2-Card Battle / Compare Tool
            AaaSectionHeader(
                title = "CARD POWER COMPARISON ARENA",
                subtitle = "Select two cards and see which one wins the trick!",
                badge = "TEST POWER"
            )
            Spacer(modifier = Modifier.height(6.dp))

            CardBattleArena(
                card1 = comparisonCard1,
                card2 = comparisonCard2,
                trumpSuit = testTrumpSuit,
                onCard1Change = { comparisonCard1 = it },
                onCard2Change = { comparisonCard2 = it },
                onTrumpChange = { testTrumpSuit = it }
            )
        }
    }
}

@Composable
private fun PointSummaryPill(rank: String, pts: String, count: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(rank, color = color, fontSize = 11.sp, fontWeight = FontWeight.Black)
        Text(pts, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(count, color = Color(0xFF94A3B8), fontSize = 8.5.sp)
    }
}

@Composable
private fun RankDetailBreakdownCard(rank: Rank) {
    val (powerName, strategyTip, hierarchyPos) = when (rank) {
        Rank.JACK -> Triple("Highest Rank (Power 8/8)", "The most dominant card in 29. Worth 3 points each. Jacks take almost every trick unless trumped.", "1st (King of the Table)")
        Rank.NINE -> Triple("Second Highest (Power 7/8)", "Unique to 29: The 9 is higher than Aces and 10s! Worth 2 points. Crucial for establishing trick control.", "2nd (Beats A & 10)")
        Rank.ACE -> Triple("Third Highest (Power 6/8)", "Worth 1 point. Wins tricks over Kings and 10s, but is beaten by Jacks and Nines in the same suit.", "3rd")
        Rank.TEN -> Triple("Fourth Highest (Power 5/8)", "Worth 1 point. Vulnerable to Aces, Nines, and Jacks. Often played strategically to duck or feed points to partner.", "4th")
        Rank.KING -> Triple("Fifth Rank (Power 4/8)", "Worth 0 points. Forms the high-value Roy (Marriage) bonus with Queen if in Trump suit (+4 points).", "5th (0 pts)")
        Rank.QUEEN -> Triple("Sixth Rank (Power 3/8)", "Worth 0 points. Forms Roy with King. Used as safe lead or exit card.", "6th (0 pts)")
        Rank.EIGHT -> Triple("Seventh Rank (Power 2/8)", "Worth 0 points. Safe discard or low trump card to capture high point cards played by opponents.", "7th (0 pts)")
        Rank.SEVEN -> Triple("Lowest Rank (Power 1/8)", "Worth 0 points. Lowest card in suit, but if it is a TRUMP card, a 7 of Trumps still beats an Ace or Jack of any other non-trump suit!", "8th (0 pts)")
    }

    AaaGlassCard(
        borderColor = CasinoGold.copy(alpha = 0.5f),
        glowColor = CasinoGold
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1B4B))
                    .border(1.dp, CasinoGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(rank.symbol, color = CasinoGold, fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${rank.displayName} Breakdown", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    AaaBadge(text = "${rank.points} POINTS", color = CasinoGold, textColor = Color(0xFF0F172A))
                }
                Text(powerName, color = Color(0xFF38BDF8), fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = Color(0xFF334155))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "💡 Strategy Insight: $strategyTip",
            color = Color(0xFFE2E8F0),
            fontSize = 11.sp,
            lineHeight = 15.sp
        )
    }
}

@Composable
private fun CardBattleArena(
    card1: Card,
    card2: Card,
    trumpSuit: Suit?,
    onCard1Change: (Card) -> Unit,
    onCard2Change: (Card) -> Unit,
    onTrumpChange: (Suit?) -> Unit
) {
    // Determine winner assuming card 1 is led
    val (winnerCard, reason) = remember(card1, card2, trumpSuit) {
        val c1IsTrump = card1.suit == trumpSuit
        val c2IsTrump = card2.suit == trumpSuit

        if (c1IsTrump && !c2IsTrump) {
            Pair(card1, "Card 1 wins because it is a TRUMP (${card1.suit.displayName})!")
        } else if (!c1IsTrump && c2IsTrump) {
            Pair(card2, "Card 2 wins because it CUT with TRUMP (${card2.suit.displayName})!")
        } else if (c1IsTrump && c2IsTrump) {
            if (card1.power > card2.power) {
                Pair(card1, "Both are Trumps! Card 1 has higher rank (${card1.rank.symbol} > ${card2.rank.symbol}).")
            } else {
                Pair(card2, "Both are Trumps! Card 2 has higher rank (${card2.rank.symbol} > ${card1.rank.symbol}).")
            }
        } else {
            // Neither is trump
            if (card2.suit == card1.suit) {
                if (card1.power > card2.power) {
                    Pair(card1, "Same suit! Card 1 has higher rank (${card1.rank.symbol} > ${card2.rank.symbol}).")
                } else {
                    Pair(card2, "Same suit! Card 2 has higher rank (${card2.rank.symbol} > ${card1.rank.symbol}).")
                }
            } else {
                Pair(card1, "Card 1 was led. Card 2 played an off-suit non-trump (${card2.suit.displayName}), so Card 1 wins!")
            }
        }
    }

    AaaGlassCard {
        // Trump suit picker for testing
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Active Trump Suit:", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Suit.values().forEach { suit ->
                    val isSelected = trumpSuit == suit
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) Color(0xFFFFD700) else Color(0xFF1E293B))
                            .border(1.dp, if (isSelected) Color(0xFFFFD700) else Color(0xFF475569), RoundedCornerShape(6.dp))
                            .clickable { onTrumpChange(if (isSelected) null else suit) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            suit.symbol,
                            fontSize = 14.sp,
                            color = if (isSelected) Color(0xFF0F172A) else if (suit.isRed) Color(0xFFEF4444) else Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cards Head-to-Head display
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Card 1 (Lead)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AaaBadge(text = "CARD 1 (LEAD)", color = Color(0xFF38BDF8), textColor = Color(0xFF0F172A))
                Spacer(modifier = Modifier.height(4.dp))
                PlayingCard3DView(
                    card = card1,
                    isSelected = winnerCard == card1,
                    isTrump = card1.suit == trumpSuit,
                    width = 62.dp,
                    height = 88.dp
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Quick cycle rank button
                Aaa3DButton(
                    text = "CHANGE",
                    onClick = {
                        val ranks = Rank.values()
                        val nextIdx = (ranks.indexOf(card1.rank) + 1) % ranks.size
                        onCard1Change(card1.copy(rank = ranks[nextIdx]))
                    },
                    variant = ButtonVariant.SECONDARY,
                    height = 26.dp
                )
            }

            Text("VS", color = CasinoGold, fontSize = 16.sp, fontWeight = FontWeight.Black)

            // Card 2 (Response)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AaaBadge(text = "CARD 2 (FOLLOW)", color = Color(0xFFA855F7), textColor = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                PlayingCard3DView(
                    card = card2,
                    isSelected = winnerCard == card2,
                    isTrump = card2.suit == trumpSuit,
                    width = 62.dp,
                    height = 88.dp
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Quick cycle rank button
                Aaa3DButton(
                    text = "CHANGE",
                    onClick = {
                        val ranks = Rank.values()
                        val nextIdx = (ranks.indexOf(card2.rank) + 1) % ranks.size
                        onCard2Change(card2.copy(rank = ranks[nextIdx]))
                    },
                    variant = ButtonVariant.SECONDARY,
                    height = 26.dp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Winner Announcement Box
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, if (winnerCard == card1) Color(0xFF38BDF8) else Color(0xFFA855F7), RoundedCornerShape(8.dp)),
            color = Color(0xFF0F172A)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🏆 WINNER: ", color = CasinoGold, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Text(
                        "${winnerCard.rank.symbol}${winnerCard.suit.symbol} (${winnerCard.rank.displayName} of ${winnerCard.suit.displayName})",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(reason, color = Color(0xFF94A3B8), fontSize = 10.sp)
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// TAB 3: BIDDING & CONTRACTS SIMULATOR
// -----------------------------------------------------------------------------------------
@Composable
private fun BiddingTabSection() {
    var bidValue by remember { mutableIntStateOf(19) }
    var isDouble by remember { mutableStateOf(false) }
    var isRedouble by remember { mutableStateOf(false) }

    // Sample hand simulation
    var sampleHandIndex by remember { mutableIntStateOf(0) }
    val presetHands = listOf(
        listOf(Card(Suit.SPADES, Rank.JACK), Card(Suit.SPADES, Rank.NINE), Card(Suit.HEARTS, Rank.ACE), Card(Suit.DIAMONDS, Rank.SEVEN)),
        listOf(Card(Suit.HEARTS, Rank.JACK), Card(Suit.HEARTS, Rank.TEN), Card(Suit.CLUBS, Rank.ACE), Card(Suit.CLUBS, Rank.NINE)),
        listOf(Card(Suit.DIAMONDS, Rank.KING), Card(Suit.DIAMONDS, Rank.QUEEN), Card(Suit.SPADES, Rank.EIGHT), Card(Suit.HEARTS, Rank.SEVEN)),
        listOf(Card(Suit.CLUBS, Rank.JACK), Card(Suit.CLUBS, Rank.NINE), Card(Suit.CLUBS, Rank.ACE), Card(Suit.CLUBS, Rank.TEN))
    )
    val activeHand = presetHands[sampleHandIndex]

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Interactive Bid Target Calculator
            AaaGlassCard(
                borderBrush = GoldMetallicBrush,
                glowColor = CasinoGold
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("INTERACTIVE BID TARGET CALCULATOR", color = CasinoGold, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        Text("Drag or step the bid to see required points and match stakes", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoldMetallicBrush)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("BID: $bidValue", color = Color(0xFF0F172A), fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bid Slider
                Slider(
                    value = bidValue.toFloat(),
                    onValueChange = { bidValue = it.toInt() },
                    valueRange = 16f..28f,
                    steps = 11,
                    colors = SliderDefaults.colors(
                        thumbColor = CasinoGold,
                        activeTrackColor = CasinoGold,
                        inactiveTrackColor = Color(0xFF334155)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Min: 16", color = Color(0xFF94A3B8), fontSize = 9.sp)
                    Text("Standard: 19-21", color = Color(0xFF38BDF8), fontSize = 9.sp)
                    Text("High: 24+", color = Color(0xFFF97316), fontSize = 9.sp)
                    Text("Max: 28", color = Color(0xFFEF4444), fontSize = 9.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stakes & Risk Breakdown
                val riskLevel = when {
                    bidValue <= 17 -> "Low Risk (Safe bid with J + 9)"
                    bidValue <= 20 -> "Medium Risk (Standard competitive contract)"
                    bidValue <= 24 -> "High Risk (Requires solid side-suit aces & partner help)"
                    else -> "Legendary High Stakes (Near slam target!)"
                }

                val pointsNeededByBidder = bidValue
                val pointsNeededByDefenders = 29 - bidValue

                val baseGamePoints = if (bidValue >= 21) 2 else 1
                val multiplier = if (isRedouble) 4 else if (isDouble) 2 else 1
                val matchPointsAtStake = baseGamePoints * multiplier

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    color = Color(0xFF0B101D)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Bidder Target:", color = Color.White, fontSize = 11.sp)
                            Text("$pointsNeededByBidder / 29 pts", color = Color(0xFF4ADE80), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Defenders Target to Defeat:", color = Color.White, fontSize = 11.sp)
                            Text("$pointsNeededByDefenders pts", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Contract Stakes:", color = Color.White, fontSize = 11.sp)
                            Text(
                                "±$matchPointsAtStake Game Point(s)",
                                color = CasinoGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("📊 Assessment: $riskLevel", color = Color(0xFF38BDF8), fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Double / Redouble interactive toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, if (isDouble) Color(0xFFEF4444) else Color(0xFF334155), RoundedCornerShape(8.dp))
                            .clickable {
                                isDouble = !isDouble
                                if (!isDouble) isRedouble = false
                            },
                        color = if (isDouble) Color(0xFF7F1D1D) else Color(0xFF1E293B)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(if (isDouble) "✓ DOUBLE (2X)" else "TOGGLE DOUBLE (2X)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, if (isRedouble) Color(0xFFFFD700) else Color(0xFF334155), RoundedCornerShape(8.dp))
                            .clickable {
                                if (isDouble) {
                                    isRedouble = !isRedouble
                                }
                            },
                        color = if (isRedouble) Color(0xFF78350F) else Color(0xFF1E293B)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(if (isRedouble) "✓ REDOUBLE (4X)" else "REDOUBLE (4X)", color = if (isDouble) Color.White else Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            // Interactive Hand Strength & Recommended Bid Analyzer
            AaaSectionHeader(
                title = "PRACTICE HAND BID EVALUATOR",
                subtitle = "See what the Grandmaster AI would bid with this opening 4-card deal",
                badge = "TRY HANDS"
            )
            Spacer(modifier = Modifier.height(6.dp))

            HandEvaluatorSimulator(
                cards = activeHand,
                onNextSampleHand = {
                    sampleHandIndex = (sampleHandIndex + 1) % presetHands.size
                }
            )
        }

        item {
            // Key Bidding Rules Cheat Sheet
            AaaGlassCard {
                Text("ESSENTIAL BIDDING RULES & PROTOCOL", color = CasinoGold, fontSize = 12.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(6.dp))

                RuleBulletPoint("Minimum Opening Bid is 16. If all 4 players pass, cards are reshuffled and redealt.")
                RuleBulletPoint("Player to the dealer's left bids first. Subsequent bids must strictly exceed the current highest bid.")
                RuleBulletPoint("The player who wins the bid chooses the TRUMP SUIT from their initial 4 cards and places it face-down without showing anyone.")
                RuleBulletPoint("Bids of 21 and above carry double default stakes (+2 or -2 Game Points).")
                RuleBulletPoint("Single Hand / Roy: A special bid of 28/29 where the bidder plays alone without partner support to win all 8 tricks!")
            }
        }
    }
}

@Composable
private fun HandEvaluatorSimulator(
    cards: List<Card>,
    onNextSampleHand: () -> Unit
) {
    // Calculate simple valuation
    val totalPoints = cards.sumOf { it.points }
    val suitCounts = cards.groupBy { it.suit }
    val bestSuit = suitCounts.maxByOrNull { it.value.sumOf { c -> c.points * 3 + c.power } }?.key ?: Suit.SPADES

    val (recommendedBid, advice) = when {
        cards.any { it.rank == Rank.JACK } && cards.any { it.rank == Rank.NINE } -> {
            Pair("18 - 20 in ${bestSuit.displayName}", "Strong hand! You hold both Jack and 9. Great base for a trump suit.")
        }
        cards.count { it.points > 0 } >= 3 -> {
            Pair("17 - 18 in ${bestSuit.displayName}", "Balanced hand with good honors. Safe to open with 17.")
        }
        cards.any { it.rank == Rank.JACK } -> {
            Pair("16 - 17 in ${bestSuit.displayName}", "Single Jack allows a safe minimum bid of 16 to secure your preferred suit.")
        }
        else -> {
            Pair("PASS (or 16 if forced)", "Weak holding with 0 Jacks or 9s. Better to pass and support partner's bid.")
        }
    }

    AaaGlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("YOUR 4-CARD DEAL:", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            Aaa3DButton(
                text = "NEXT HAND",
                onClick = onNextSampleHand,
                variant = ButtonVariant.GOLD,
                height = 28.dp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Card Fan Display
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            cards.forEachIndexed { idx, card ->
                PlayingCard3DView(
                    card = card,
                    isPlayable = true,
                    width = 58.dp,
                    height = 82.dp,
                    rotationZ = (idx - 1.5f) * 6f,
                    modifier = Modifier.offset(x = (idx * -8).dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp)),
            color = Color(0xFF0F172A)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💡 AI Recommendation: ", color = CasinoGold, fontSize = 11.5.sp, fontWeight = FontWeight.Black)
                    Text(recommendedBid, color = Color(0xFF38BDF8), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(advice, color = Color(0xFFE2E8F0), fontSize = 10.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Hand Point Count: $totalPoints pts", color = Color(0xFF94A3B8), fontSize = 9.5.sp)
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// TAB 4: SECRET TRUMP & REVEAL MECHANICS
// -----------------------------------------------------------------------------------------
@Composable
private fun TrumpRevealTabSection() {
    var isTrumpRevealed by remember { mutableStateOf(false) }
    var selectedSecretTrump by remember { mutableStateOf(Card(Suit.HEARTS, Rank.JACK)) }
    var showRoyMarriage by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Interactive Secret Trump Reveal Simulator
            AaaGlassCard(
                borderBrush = GoldMetallicBrush,
                glowColor = CasinoGold
            ) {
                Text("INTERACTIVE TRUMP REVEAL SIMULATOR", color = CasinoGold, fontSize = 12.sp, fontWeight = FontWeight.Black)
                Text("Experience the signature hidden-trump reveal mechanic of 29", color = Color(0xFF94A3B8), fontSize = 10.sp)

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Secret Trump Card with 3D Flip
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (isTrumpRevealed) "REVEALED TRUMP" else "SECRET TRUMP (HIDDEN)",
                            color = if (isTrumpRevealed) Color(0xFF4ADE80) else Color(0xFFFFD700),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        PlayingCard3DView(
                            card = selectedSecretTrump,
                            isFaceUp = isTrumpRevealed,
                            isSelected = isTrumpRevealed,
                            isTrump = true,
                            width = 68.dp,
                            height = 98.dp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Aaa3DButton(
                            text = if (isTrumpRevealed) "HIDE TRUMP" else "REVEAL TRUMP 👑",
                            onClick = { isTrumpRevealed = !isTrumpRevealed },
                            variant = if (isTrumpRevealed) ButtonVariant.SECONDARY else ButtonVariant.GOLD,
                            height = 32.dp
                        )
                    }

                    // Explanation Column
                    Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
                        Text("LEGAL REVEAL CONDITIONS:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "1. A card has been led (e.g. ♠ Spades).\n" +
                            "2. You have NO cards in Spades (you are VOID).\n" +
                            "3. You ask to reveal the Trump card before playing.\n" +
                            "4. Once revealed, you MUST play a Trump card if you hold one!",
                            color = Color(0xFFE2E8F0),
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        item {
            // Roy / Marriage (King + Queen) Explainer
            AaaGlassCard(
                borderColor = Color(0xFFA855F7),
                glowColor = Color(0xFFA855F7)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👑", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("ROY (PAIR / MARRIAGE) BONUS", color = Color(0xFFA855F7), fontSize = 12.sp, fontWeight = FontWeight.Black)
                            Text("King + Queen of the Trump Suit", color = Color(0xFF94A3B8), fontSize = 9.5.sp)
                        }
                    }
                    AaaBadge(text = "±4 POINTS", color = Color(0xFFA855F7), textColor = Color.White)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlayingCard3DView(
                        card = Card(selectedSecretTrump.suit, Rank.KING),
                        isTrump = true,
                        width = 54.dp,
                        height = 76.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("+", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    PlayingCard3DView(
                        card = Card(selectedSecretTrump.suit, Rank.QUEEN),
                        isTrump = true,
                        width = 54.dp,
                        height = 76.dp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F172A)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("• If Bidder's Team claims Roy: Target reduces by 4 points (e.g. 19 becomes 15)!", color = Color(0xFF4ADE80), fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text("• If Defender's Team claims Roy: Bidder's target increases by 4 points (e.g. 19 becomes 23)!", color = Color(0xFFEF4444), fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text("• Must be claimed AFTER trump is revealed and before the 7th trick begins.", color = Color(0xFF94A3B8), fontSize = 9.5.sp)
                    }
                }
            }
        }

        item {
            // Trump Rules Golden Checklist
            AaaGlassCard {
                Text("TRUMP REVEAL RULES & PENALTIES", color = CasinoGold, fontSize = 12.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(6.dp))

                RuleBulletPoint("Must Follow Suit: If you have a card of the suit led, you MUST play it. You cannot ask for trump or cut with trump if you have the led suit.")
                RuleBulletPoint("Before Trump is Revealed: Cards played off-suit are just discards and CANNOT win the trick.")
                RuleBulletPoint("After Trump is Revealed: If you are void in the lead suit, you are required to play a Trump card if you hold one.")
                RuleBulletPoint("Over-Trumping: If another player has already played a trump card in this trick, you must play a higher trump if you can.")
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// TAB 5: TRICK SIMULATOR (STEP-BY-STEP ROUND WALKTHROUGH)
// -----------------------------------------------------------------------------------------
@Composable
private fun TrickSimulatorTabSection() {
    var stepIndex by remember { mutableIntStateOf(0) }

    val trickScenarioSteps = listOf(
        TrickStep(
            player = "West (Opponent)",
            card = Card(Suit.SPADES, Rank.TEN),
            comment = "West leads with 10♠ (Worth 1 pt). Lead suit is Spades.",
            currentLeader = "West (10♠)"
        ),
        TrickStep(
            player = "North (Partner)",
            card = Card(Suit.SPADES, Rank.ACE),
            comment = "North follows suit with A♠ (Worth 1 pt). Ace beats 10!",
            currentLeader = "North (A♠)"
        ),
        TrickStep(
            player = "East (Opponent)",
            card = Card(Suit.SPADES, Rank.NINE),
            comment = "East follows suit with 9♠ (Worth 2 pts). In 29, 9 is higher than Ace, so East takes the lead!",
            currentLeader = "East (9♠)"
        ),
        TrickStep(
            player = "South (You)",
            card = Card(Suit.SPADES, Rank.JACK),
            comment = "You play the almighty J♠ (Worth 3 pts)! Jack is the highest rank in 29 and wins the entire trick!",
            currentLeader = "South (You with J♠ - WINNER!)"
        )
    )

    val currentCardsPlayed = trickScenarioSteps.take(stepIndex + 1)
    val totalPointsInTrick = currentCardsPlayed.sumOf { it.card.points }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            AaaGlassCard(
                borderBrush = GoldMetallicBrush,
                glowColor = CasinoGold
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("STEP-BY-STEP TRICK PLAYTHROUGH", color = CasinoGold, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        Text("Watch how cards interact and how 9s beat Aces in 29!", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    }
                    AaaBadge(text = "STEP ${stepIndex + 1}/4", color = Color(0xFF38BDF8), textColor = Color(0xFF0F172A))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Table Felt Arena Simulation
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.5.dp, GoldMetallicBrush, RoundedCornerShape(12.dp)),
                    color = Color(0xFF064E3B)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // 4 Player Cards in Cross
                        currentCardsPlayed.forEachIndexed { idx, step ->
                            val alignment = when (idx) {
                                0 -> Alignment.CenterStart
                                1 -> Alignment.TopCenter
                                2 -> Alignment.CenterEnd
                                else -> Alignment.BottomCenter
                            }
                            val offsetX = when (idx) {
                                0 -> 24.dp
                                2 -> (-24).dp
                                else -> 0.dp
                            }
                            val offsetY = when (idx) {
                                1 -> 14.dp
                                3 -> (-14).dp
                                else -> 0.dp
                            }

                            Column(
                                modifier = Modifier
                                    .align(alignment)
                                    .offset(x = offsetX, y = offsetY),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    step.player.substringBefore(" "),
                                    color = Color(0xFFFFD700),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                PlayingCard3DView(
                                    card = step.card,
                                    isPlayable = true,
                                    width = 44.dp,
                                    height = 62.dp
                                )
                            }
                        }

                        // Center Live Trick Points Pill
                        Surface(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .clip(RoundedCornerShape(16.dp)),
                            color = Color(0xFF0F172A).copy(alpha = 0.9f)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("TRICK POT", color = Color(0xFF94A3B8), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                Text("$totalPointsInTrick PTS", color = CasinoGold, fontSize = 12.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Current Step Explanation Box
                val activeStep = trickScenarioSteps[stepIndex]
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0B101D)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(activeStep.player, color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(activeStep.comment, color = Color.White, fontSize = 11.sp, lineHeight = 15.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Current Winning Card: ${activeStep.currentLeader}", color = CasinoGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Step Navigation Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Aaa3DButton(
                        text = "PREVIOUS MOVE",
                        onClick = { if (stepIndex > 0) stepIndex-- },
                        enabled = stepIndex > 0,
                        variant = ButtonVariant.SECONDARY,
                        height = 32.dp
                    )

                    Aaa3DButton(
                        text = if (stepIndex == 3) "RESTART TRICK" else "NEXT MOVE ⚔️",
                        onClick = {
                            if (stepIndex == 3) stepIndex = 0 else stepIndex++
                        },
                        variant = ButtonVariant.GOLD,
                        height = 32.dp
                    )
                }
            }
        }

        item {
            // Trick Winning Rules Recap
            AaaGlassCard {
                Text("TRICK EVALUATION RULES", color = CasinoGold, fontSize = 12.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(6.dp))

                RuleBulletPoint("The player who plays the highest card of the suit led wins the trick, unless trumped.")
                RuleBulletPoint("If one or more trumps are played, the highest trump card wins the trick regardless of non-trump cards.")
                RuleBulletPoint("The winner of each trick leads the next card for the following trick.")
                RuleBulletPoint("Winning the 8th (last) trick awards a +1 bonus point to the team that takes it.")
            }
        }
    }
}

private data class TrickStep(
    val player: String,
    val card: Card,
    val comment: String,
    val currentLeader: String
)

// -----------------------------------------------------------------------------------------
// TAB 6: SCORING & PIPS (RED / BLACK PIPS)
// -----------------------------------------------------------------------------------------
@Composable
private fun ScoringPipsTabSection() {
    var bidderScore by remember { mutableIntStateOf(19) }
    var targetBid by remember { mutableIntStateOf(18) }

    val didBidderWin = bidderScore >= targetBid
    val isDouble = false

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Interactive Pip Scoreboard
            AaaGlassCard(
                borderBrush = GoldMetallicBrush,
                glowColor = CasinoGold
            ) {
                Text("INTERACTIVE GAME POINTS (PIPS) SIMULATOR", color = CasinoGold, fontSize = 12.sp, fontWeight = FontWeight.Black)
                Text("29 is scored using a set of 6 Game Points (Red Pips for wins, Black Pips for losses)", color = Color(0xFF94A3B8), fontSize = 10.sp)

                Spacer(modifier = Modifier.height(14.dp))

                // Score adjusters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Target Bid: $targetBid", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Aaa3DButton("-", onClick = { if (targetBid > 16) targetBid-- }, variant = ButtonVariant.SECONDARY, height = 24.dp)
                            Aaa3DButton("+", onClick = { if (targetBid < 28) targetBid++ }, variant = ButtonVariant.SECONDARY, height = 24.dp)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Points Won: $bidderScore / 29", color = if (didBidderWin) Color(0xFF4ADE80) else Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Aaa3DButton("-", onClick = { if (bidderScore > 0) bidderScore-- }, variant = ButtonVariant.SECONDARY, height = 24.dp)
                            Aaa3DButton("+", onClick = { if (bidderScore < 29) bidderScore++ }, variant = ButtonVariant.SECONDARY, height = 24.dp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Visual Red/Black Pips Indicator
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    color = Color(0xFF0F172A)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (didBidderWin) "🎉 BIDDER SUCCESS! (+1 RED PIP)" else "❌ BIDDER DEFEAT! (+1 BLACK PIP)",
                            color = if (didBidderWin) Color(0xFF4ADE80) else Color(0xFFEF4444),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // 6 Pip Track
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (i in 1..6) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (didBidderWin && i <= 3) Color(0xFFEF4444)
                                            else if (!didBidderWin && i <= 2) Color(0xFF1E293B)
                                            else Color(0xFF334155)
                                        )
                                        .border(1.dp, Color(0xFFFFD700), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        if (didBidderWin && i <= 3) "🔴" else if (!didBidderWin && i <= 2) "⚫" else "⚪",
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            // Match Scoring Rules
            AaaGlassCard {
                Text("MATCH SCORING SYSTEM", color = CasinoGold, fontSize = 12.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(6.dp))

                RuleBulletPoint("Normal Win: Bidder makes the contract -> Bidder team scores +1 Game Point.")
                RuleBulletPoint("Normal Fail: Bidder falls short -> Defenders score +1 Game Point (or Bidder loses -1).")
                RuleBulletPoint("High Bid (21+): Bids of 21 or higher are worth ±2 Game Points automatically.")
                RuleBulletPoint("Doubled Contract: Scores ±2 Game Points (or ±4 if bid was 21+).")
                RuleBulletPoint("Redoubled Contract: Scores ±4 Game Points (or ±8 if bid was 21+).")
                RuleBulletPoint("Winning the Match: First partnership to accumulate 6 Game Points wins the full match!")
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// TAB 7: RULES MASTERY QUIZ (INTERACTIVE CHALLENGE)
// -----------------------------------------------------------------------------------------
@Composable
private fun RulesMasteryQuizTabSection() {
    val questions = remember {
        listOf(
            QuizQuestion(
                question = "Which card has the HIGHEST point value and rank in 29?",
                options = listOf("Ace (A)", "King (K)", "Jack (J)", "Nine (9)"),
                correctIndex = 2,
                explanation = "Correct! The Jack is the highest card rank in 29 and is worth 3 points."
            ),
            QuizQuestion(
                question = "What is the ranking order between Ace, 9, 10, and Jack of the same suit?",
                options = listOf("A > K > Q > J", "J > 9 > A > 10", "A > 10 > K > 9", "J > A > 9 > 10"),
                correctIndex = 1,
                explanation = "Spot on! The distinctive 29 rank order is Jack (3 pts) > Nine (2 pts) > Ace (1 pt) > Ten (1 pt)."
            ),
            QuizQuestion(
                question = "When can a player legally request to reveal the secret Trump suit?",
                options = listOf(
                    "At the very start of round 1",
                    "Any time they want to score points",
                    "When they have NO cards in the suit led (void)",
                    "Only after the 6th trick is played"
                ),
                correctIndex = 2,
                explanation = "Exactly! You can only request trump when you are void in the current lead suit."
            ),
            QuizQuestion(
                question = "What is the total number of points available in a full round of 29?",
                options = listOf("28 points", "29 points", "32 points", "24 points"),
                correctIndex = 1,
                explanation = "Yes! 28 card points (4 Jacks=12, 4 Nines=8, 4 Aces=4, 4 Tens=4) + 1 bonus point for the 8th trick = 29 points."
            ),
            QuizQuestion(
                question = "What does the 'Roy' (King + Queen of Trump) bonus do?",
                options = listOf(
                    "Instantly wins the whole game",
                    "Modifies the bidder's target by 4 points",
                    "Gives the player 10 extra coins",
                    "Cancels all trumps"
                ),
                correctIndex = 1,
                explanation = "Correct! Roy adjusts the contract target by 4 points (+4 for defenders or -4 for bidder)."
            )
        )
    }

    var currentQuestionIdx by remember { mutableIntStateOf(0) }
    var selectedAnswerIdx by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var isAnswerSubmitted by remember { mutableStateOf(false) }

    val activeQ = questions[currentQuestionIdx]

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            AaaGlassCard(
                borderBrush = GoldMetallicBrush,
                glowColor = CasinoGold
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("29 RULES MASTERY CHALLENGE", color = CasinoGold, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        Text("Test your knowledge and prove you're ready for the tables!", color = Color(0xFF94A3B8), fontSize = 10.sp)
                    }
                    AaaBadge(text = "SCORE: $score / ${questions.size}", color = CasinoGold, textColor = Color(0xFF0F172A))
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    "Question ${currentQuestionIdx + 1} of ${questions.size}",
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    activeQ.question,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Options
                activeQ.options.forEachIndexed { optIdx, optText ->
                    val isSelected = selectedAnswerIdx == optIdx
                    val isCorrect = isAnswerSubmitted && optIdx == activeQ.correctIndex
                    val isWrong = isAnswerSubmitted && isSelected && optIdx != activeQ.correctIndex

                    val borderColor = when {
                        isCorrect -> Color(0xFF4ADE80)
                        isWrong -> Color(0xFFEF4444)
                        isSelected -> CasinoGold
                        else -> Color(0xFF334155)
                    }

                    val bgColor = when {
                        isCorrect -> Color(0xFF064E3B)
                        isWrong -> Color(0xFF7F1D1D)
                        isSelected -> Color(0xFF1E1B4B)
                        else -> Color(0xFF0F172A)
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                            .clickable(enabled = !isAnswerSubmitted) {
                                selectedAnswerIdx = optIdx
                            },
                        color = bgColor
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${'A' + optIdx}.",
                                color = if (isSelected || isCorrect) CasinoGold else Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                optText,
                                color = Color.White,
                                fontSize = 11.5.sp,
                                modifier = Modifier.weight(1f)
                            )
                            if (isCorrect) {
                                Text("✓", color = Color(0xFF4ADE80), fontSize = 14.sp, fontWeight = FontWeight.Black)
                            } else if (isWrong) {
                                Text("✗", color = Color(0xFFEF4444), fontSize = 14.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Explanation when submitted
                if (isAnswerSubmitted) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0B101D)
                    ) {
                        Text(
                            activeQ.explanation,
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Submit / Next Button
                if (!isAnswerSubmitted) {
                    Aaa3DButton(
                        text = "SUBMIT ANSWER",
                        onClick = {
                            if (selectedAnswerIdx != null) {
                                isAnswerSubmitted = true
                                if (selectedAnswerIdx == activeQ.correctIndex) {
                                    score++
                                }
                            }
                        },
                        enabled = selectedAnswerIdx != null,
                        variant = ButtonVariant.GOLD,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Aaa3DButton(
                        text = if (currentQuestionIdx < questions.size - 1) "NEXT QUESTION ➔" else "RESTART QUIZ 🔄",
                        onClick = {
                            if (currentQuestionIdx < questions.size - 1) {
                                currentQuestionIdx++
                                selectedAnswerIdx = null
                                isAnswerSubmitted = false
                            } else {
                                currentQuestionIdx = 0
                                selectedAnswerIdx = null
                                isAnswerSubmitted = false
                                score = 0
                            }
                        },
                        variant = ButtonVariant.GOLD,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

private data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

@Composable
private fun RuleBulletPoint(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("•", color = CasinoGold, fontSize = 12.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = Color(0xFFE2E8F0), fontSize = 10.5.sp, lineHeight = 14.sp)
    }
}
