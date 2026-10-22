package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.example.game.TwentyNineViewModel
import com.example.game.engine.TwentyNineEngine
import com.example.game.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlin.math.abs

@Composable
fun TableArenaScreen(
    viewModel: TwentyNineViewModel,
    onBackToLobby: () -> Unit,
    onOpenAcademy: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenStore: () -> Unit = {},
    onBackToAdventures: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val gameState by viewModel.gameState.collectAsState()
    val roundResult by viewModel.roundResult.collectAsState()
    val playerEconomy by viewModel.playerEconomy.collectAsState()
    val showSettings by viewModel.showSettings.collectAsState()
    val feltTheme by viewModel.feltTheme.collectAsState()

    var selectedCardId by remember { mutableStateOf<String?>(null) }
    var showAiCoachBot by remember { mutableStateOf(false) }

    val chatUnreadCount by viewModel.chatUnreadCount.collectAsState()
    val isChatOpen by viewModel.isChatDrawerOpen.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()

    val userCards = gameState.players[PlayerPosition.SOUTH]?.cards ?: emptyList()
    val playableCards = remember(gameState, userCards) {
        if (gameState.phase == GamePhase.PLAYING_TRICK && gameState.activeTurn == PlayerPosition.SOUTH) {
            TwentyNineEngine.getPlayableCards(
                userCards,
                gameState.currentTrick,
                gameState.trumpSuit,
                gameState.isTrumpRevealed
            )
        } else emptyList()
    }

    val canRevealTrump = remember(gameState, userCards) {
        gameState.phase == GamePhase.PLAYING_TRICK &&
                gameState.activeTurn == PlayerPosition.SOUTH &&
                TwentyNineEngine.canRequestTrumpReveal(userCards, gameState.currentTrick, gameState.isTrumpRevealed)
    }

    val canDeclarePair = remember(gameState, userCards) {
        TwentyNineEngine.canDeclarePair(
            userCards,
            gameState.trumpSuit,
            gameState.isTrumpRevealed,
            gameState.pairDeclaredByTeam != null
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF060913))
            .testTag("table_arena_screen")
    ) {
        // High-definition casino backdrop
        Image(
            painter = painterResource(id = R.drawable.img_banner_casino),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.35f)
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Panoramic HUD Header
            LandscapeTopHeader(
                matchMode = gameState.matchMode,
                adventureLevel = gameState.adventureLevel,
                lastEventMessage = gameState.lastEventMessage,
                teamAGamePoints = gameState.teamAScore.gamePoints,
                teamBGamePoints = gameState.teamBScore.gamePoints,
                targetPips = gameState.houseRules.targetPipsToWin,
                diamonds = playerEconomy.diamonds,
                coins = playerEconomy.coins,
                chatUnreadCount = chatUnreadCount,
                onOpenChat = { viewModel.toggleChatDrawer(true) },
                onBackToLobby = {
                    if (gameState.matchMode == MatchMode.ADVENTURE && onBackToAdventures != null) {
                        onBackToAdventures()
                    } else {
                        onBackToLobby()
                    }
                },
                onOpenAcademy = onOpenAcademy,
                onOpenStats = onOpenStats,
                onOpenStore = onOpenStore,
                onOpenAiCoach = { showAiCoachBot = true },
                onOpenSettings = { viewModel.toggleSettings(true) },
                onNewRound = { viewModel.startNewRound() }
            )

            // Main Landscape Arena Body (Left Panel, 3D Center Table, Right Panel)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                // Left Control Pane (Scores, Trump Box, Pair & Cut Actions)
                LeftArenaControlPane(
                    gameState = gameState,
                    canDeclarePair = canDeclarePair,
                    canRevealTrump = canRevealTrump,
                    onRevealTrumpClick = { viewModel.onUserRequestTrumpReveal() },
                    onPairDeclareClick = { viewModel.onUserDeclarePair() },
                    modifier = Modifier
                        .width(135.dp)
                        .fillMaxHeight()
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Center 3D Perspective Casino Table & Player Hand
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    PerspectiveTableView(
                        feltTheme = feltTheme,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Authentic Miniature Guti / Chabbi Board on the Table Felt
                        TableGutiBoardView(
                            teamAGamePoints = gameState.teamAScore.gamePoints,
                            teamBGamePoints = gameState.teamBScore.gamePoints,
                            targetPips = gameState.houseRules.targetPipsToWin,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(start = 10.dp, top = 6.dp)
                        )

                        // North Player Pod (Partner Vikram)
                        gameState.players[PlayerPosition.NORTH]?.let { partner ->
                            PlayerPodView(
                                player = partner,
                                isActiveTurn = gameState.activeTurn == PlayerPosition.NORTH,
                                isDealer = gameState.dealer == PlayerPosition.NORTH,
                                currentBid = if (gameState.highestBidder == PlayerPosition.NORTH) gameState.currentHighestBid else null,
                                isReceivingCards = gameState.phase == GamePhase.DEAL_PHASE_1 && gameState.dealingTargetPlayer == PlayerPosition.NORTH,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 2.dp)
                            )
                        }

                        // West Player Pod (Opponent 1)
                        gameState.players[PlayerPosition.WEST]?.let { opp1 ->
                            PlayerPodView(
                                player = opp1,
                                isActiveTurn = gameState.activeTurn == PlayerPosition.WEST,
                                isDealer = gameState.dealer == PlayerPosition.WEST,
                                currentBid = if (gameState.highestBidder == PlayerPosition.WEST) gameState.currentHighestBid else null,
                                isReceivingCards = gameState.phase == GamePhase.DEAL_PHASE_1 && gameState.dealingTargetPlayer == PlayerPosition.WEST,
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 4.dp)
                            )
                        }

                        // East Player Pod (Opponent 2)
                        gameState.players[PlayerPosition.EAST]?.let { opp2 ->
                            PlayerPodView(
                                player = opp2,
                                isActiveTurn = gameState.activeTurn == PlayerPosition.EAST,
                                isDealer = gameState.dealer == PlayerPosition.EAST,
                                currentBid = if (gameState.highestBidder == PlayerPosition.EAST) gameState.currentHighestBid else null,
                                isReceivingCards = gameState.phase == GamePhase.DEAL_PHASE_1 && gameState.dealingTargetPlayer == PlayerPosition.EAST,
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 4.dp)
                            )
                        }

                        // Center 3D Trick Card Arena / Dealing Deck Arena
                        CenterTrickTableArea(
                            currentTrick = gameState.currentTrick,
                            trumpSuit = gameState.trumpSuit,
                            isTrumpRevealed = gameState.isTrumpRevealed,
                            cardSkin = playerEconomy.equippedCardSkin,
                            isDealing = gameState.phase == GamePhase.DEAL_PHASE_1,
                            dealingTargetPlayer = gameState.dealingTargetPlayer,
                            dealer = gameState.dealer,
                            onSkipDeal = { viewModel.skipCardDealing() },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    // South Foreground Player Hand
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                    ) {
                        LandscapeSouthPlayerHandView(
                            cards = userCards,
                            playableCards = playableCards,
                            selectedCardId = selectedCardId,
                            trumpSuit = gameState.trumpSuit,
                            isTrumpRevealed = gameState.isTrumpRevealed,
                            isMyTurn = gameState.activeTurn == PlayerPosition.SOUTH && gameState.phase == GamePhase.PLAYING_TRICK,
                            isDealingPhase = gameState.phase == GamePhase.DEAL_PHASE_1,
                            cardSkin = playerEconomy.equippedCardSkin,
                            onCardClick = { card ->
                                if (selectedCardId == card.id) {
                                    viewModel.onUserPlayCard(card)
                                    selectedCardId = null
                                } else {
                                    selectedCardId = card.id
                                }
                            },
                            onSortToggle = { viewModel.toggleHandSort() }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Right Pane (AI Coach & Quick Banter Emotes)
                RightArenaCoachPane(
                    tip = gameState.aiMentorTip,
                    isThinking = gameState.isAiThinking,
                    onRequestHint = { viewModel.requestAiHint() },
                    onDismissHint = { viewModel.dismissAiHint() },
                    onEmoteClick = { viewModel.sendQuickEmote(it) },
                    modifier = Modifier
                        .width(140.dp)
                        .fillMaxHeight()
                )
            }
        }

        // Particle VFX Overlay
        val currentQualityProfile by viewModel.qualityProfile.collectAsState()
        ParticleVfxView(
            vfxEvent = gameState.vfxTrigger,
            qualityProfile = currentQualityProfile
        )

        // AAA Dramatic Event Announcement Overlay (Trump Reveal, Pair Royalty, High Stakes, Win)
        DramaticAnnouncementOverlay(vfxEvent = gameState.vfxTrigger)

        // Bidding Modal (Robust Non-Dismissible)
        if (gameState.phase == GamePhase.BIDDING && gameState.activeTurn == PlayerPosition.SOUTH) {
            BiddingDialog(
                currentHighestBid = gameState.currentHighestBid,
                highestBidder = gameState.highestBidder,
                minBid = gameState.minBid,
                userCards = userCards,
                cardSkin = playerEconomy.equippedCardSkin,
                onSortToggle = { viewModel.toggleHandSort() },
                canDouble = !gameState.isDoubled && (gameState.highestBidder?.team == Team.TEAM_B),
                canRedouble = gameState.isDoubled && !gameState.isRedoubled && (gameState.highestBidder?.team == Team.TEAM_A),
                onBidPlaced = { viewModel.onUserPlaceBid(it) },
                onPass = { viewModel.onUserPassBid() },
                onDouble = { viewModel.onUserDouble() },
                onRedouble = { viewModel.onUserRedouble() },
                onSingleHand = { viewModel.onUserSingleHand() }
            )
        } else if (gameState.phase == GamePhase.BIDDING) {
            // Live Auction Status Indicator when AI is bidding
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp)
                    .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = CasinoGold),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.95f),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, CasinoGold)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(CasinoGold, CircleShape)
                    )
                    Text(
                        text = "AUCTION: ${gameState.activeTurn.displayName.uppercase()} IS BIDDING...",
                        color = CasinoGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Trump Suit Selection Modal (When User Wins Bid)
        if (gameState.phase == GamePhase.SET_TRUMP && gameState.highestBidder == PlayerPosition.SOUTH && gameState.trumpSuit == null) {
            TrumpSelectorDialog(
                onSuitSelected = { viewModel.onSelectTrumpSuit(it) }
            )
        }

        // Round & Match Summary Dialog
        if (gameState.phase == GamePhase.ROUND_SUMMARY || gameState.phase == GamePhase.MATCH_OVER) {
            RoundSummaryDialog(
                roundResult = roundResult,
                isMatchOver = gameState.phase == GamePhase.MATCH_OVER,
                onNextRound = {
                    if (activity != null) {
                        viewModel.triggerNaturalBreakInterstitial(activity) {
                            viewModel.startNewRound()
                        }
                    } else {
                        viewModel.startNewRound()
                    }
                },
                onNewMatch = {
                    if (activity != null) {
                        viewModel.triggerNaturalBreakInterstitial(activity) {
                            viewModel.startNewMatch()
                        }
                    } else {
                        viewModel.startNewMatch()
                    }
                },
                adventureLevel = gameState.adventureLevel,
                onBackToAdventures = {
                    if (activity != null) {
                        viewModel.triggerNaturalBreakInterstitial(activity) {
                            onBackToAdventures?.invoke()
                        }
                    } else {
                        onBackToAdventures?.invoke()
                    }
                },
                onWatchAdDoubleRewards = if (activity != null) {
                    {
                        val result = roundResult
                        val isWinner = result != null && (
                            (result.bidderTeam == Team.TEAM_A && result.isSuccess) ||
                            (result.bidderTeam == Team.TEAM_B && !result.isSuccess)
                        )
                        val baseCoins = if (isWinner) 1500L else 750L
                        viewModel.doubleMatchCoinsWithAd(
                            activity = activity,
                            baseCoins = baseCoins,
                            onDoubled = { }
                        )
                    }
                } else null,
                onWatchAdRevive = if (activity != null) {
                    {
                        viewModel.reviveOrShieldRoundWithAd(
                            activity = activity,
                            onShielded = { }
                        )
                    }
                } else null,
                onExitToLobby = {
                    if (activity != null) {
                        viewModel.triggerNaturalBreakInterstitial(activity) {
                            onBackToLobby?.invoke()
                        }
                    } else {
                        onBackToLobby?.invoke()
                    }
                }
            )
        }

        // In-Game Settings Dialog (Toggles BGM, SFX, Haptics, Themes, & Quality via DataStore)
        if (showSettings) {
            val isMusicOn by viewModel.isMusicEnabled.collectAsState()
            val isSoundOn by viewModel.isSoundEnabled.collectAsState()
            val isHapticsOn by viewModel.isHapticsEnabled.collectAsState()
            val currentAiDiff by viewModel.aiDifficulty.collectAsState()
            val isPerfOverlayOn by viewModel.isPerformanceOverlayEnabled.collectAsState()
            SettingsDialog(
                selectedFeltTheme = feltTheme,
                isSoundEnabled = isSoundOn,
                isHapticsEnabled = isHapticsOn,
                isMusicEnabled = isMusicOn,
                aiDifficulty = currentAiDiff,
                onAiDifficultySelected = { viewModel.setAiDifficulty(it) },
                qualityProfile = currentQualityProfile,
                onQualityProfileSelected = { viewModel.setQualityProfile(it) },
                isPerformanceOverlayEnabled = isPerfOverlayOn,
                onPerformanceOverlayToggled = { viewModel.setPerformanceOverlayEnabled(it) },
                onMusicToggled = { viewModel.toggleBackgroundMusic(it) },
                onFeltThemeSelected = { viewModel.setFeltTheme(it) },
                onSoundToggled = { viewModel.toggleSound(it) },
                onHapticsToggled = { viewModel.toggleHaptics(it) },
                onDismiss = { viewModel.toggleSettings(false) }
            )
        }

        // AI Coach Bot Dialog (short window for rules & asking doubts)
        AiCoachBotDialog(
            isOpen = showAiCoachBot,
            onDismiss = { showAiCoachBot = false },
            onAskGeminiQuery = { viewModel.askCoachBot(it) }
        )

        // Open Source Table Live Chat & Tactical Callouts Drawer
        TableLiveChatDrawer(
            isOpen = isChatOpen,
            messages = chatMessages,
            onSendMessage = { viewModel.sendChatMessage(it) },
            onAddReaction = { id, emoji -> viewModel.addChatReaction(id, emoji) },
            onDismiss = { viewModel.toggleChatDrawer(false) }
        )
    }
}

@Composable
fun CenterTrickTableArea(
    currentTrick: Trick,
    trumpSuit: Suit?,
    isTrumpRevealed: Boolean,
    cardSkin: String = "CLASSIC_ROYAL",
    modifier: Modifier = Modifier,
    isDealing: Boolean = false,
    dealingTargetPlayer: PlayerPosition? = null,
    dealer: PlayerPosition = PlayerPosition.EAST,
    onSkipDeal: (() -> Unit)? = null
) {
    if (isDealing) {
        CenterDealingDeckArea(
            dealer = dealer,
            dealingTargetPlayer = dealingTargetPlayer,
            cardSkin = cardSkin,
            onSkipDeal = onSkipDeal,
            modifier = modifier
        )
        return
    }

    val totalPoints = currentTrick.points
    val playedCardsMap = currentTrick.plays.associate { it.player to it.card }

    Box(
        modifier = modifier
            .size(160.dp)
            .shadow(12.dp, CircleShape)
            .clip(CircleShape)
            .background(Color(0xFF000000).copy(alpha = 0.25f))
            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.35f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // North Played Card
        playedCardsMap[PlayerPosition.NORTH]?.let { card ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 6.dp)
            ) {
                AnimatedPlayingCard(
                    card = card,
                    isPlayable = true,
                    isSelected = false,
                    isTrump = isTrumpRevealed && card.suit == trumpSuit,
                    skinId = cardSkin,
                    rotationZ = 180f,
                    width = 46.dp,
                    height = 64.dp
                )
            }
        }

        // South Played Card
        playedCardsMap[PlayerPosition.SOUTH]?.let { card ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-6).dp)
            ) {
                AnimatedPlayingCard(
                    card = card,
                    isPlayable = true,
                    isSelected = false,
                    isTrump = isTrumpRevealed && card.suit == trumpSuit,
                    skinId = cardSkin,
                    rotationZ = 0f,
                    width = 46.dp,
                    height = 64.dp
                )
            }
        }

        // West Played Card
        playedCardsMap[PlayerPosition.WEST]?.let { card ->
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = 6.dp)
            ) {
                AnimatedPlayingCard(
                    card = card,
                    isPlayable = true,
                    isSelected = false,
                    isTrump = isTrumpRevealed && card.suit == trumpSuit,
                    skinId = cardSkin,
                    rotationZ = 90f,
                    width = 46.dp,
                    height = 64.dp
                )
            }
        }

        // East Played Card
        playedCardsMap[PlayerPosition.EAST]?.let { card ->
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = (-6).dp)
            ) {
                AnimatedPlayingCard(
                    card = card,
                    isPlayable = true,
                    isSelected = false,
                    isTrump = isTrumpRevealed && card.suit == trumpSuit,
                    skinId = cardSkin,
                    rotationZ = -90f,
                    width = 46.dp,
                    height = 64.dp
                )
            }
        }

        // Center Trick Point Badge (if trick active)
        if (currentTrick.plays.isNotEmpty()) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF0F172A).copy(alpha = 0.88f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700))
            ) {
                Text(
                    text = "$totalPoints pts",
                    color = Color(0xFFFFD700),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun LandscapeTopHeader(
    matchMode: MatchMode,
    adventureLevel: AdventureLevel? = null,
    lastEventMessage: String,
    teamAGamePoints: Int,
    teamBGamePoints: Int,
    targetPips: Int,
    diamonds: Int,
    coins: Long,
    chatUnreadCount: Int = 0,
    onOpenChat: () -> Unit = {},
    onBackToLobby: () -> Unit,
    onOpenAcademy: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenStore: () -> Unit,
    onOpenAiCoach: () -> Unit,
    onOpenSettings: () -> Unit,
    onNewRound: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        color = Color(0xFF0B1329),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home / Mode
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, CasinoGold),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .luxuryInteractiveScale()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onBackToLobby)
                        .testTag("back_to_lobby_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (matchMode == MatchMode.ADVENTURE) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Home,
                            contentDescription = if (matchMode == MatchMode.ADVENTURE) "Adventure Map" else "Main Menu",
                            tint = CasinoGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (matchMode == MatchMode.ADVENTURE) "MAP" else "MAIN MENU",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CasinoGold.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CasinoGold.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = if (matchMode == MatchMode.ADVENTURE && adventureLevel != null) {
                            "LVL ${adventureLevel.levelNumber}: ${adventureLevel.city}".uppercase()
                        } else {
                            matchMode.title
                        },
                        color = CasinoGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Quick Diamonds & Coins Pill
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AaaCurrencyPill(
                        type = CurrencyType.DIAMOND,
                        amount = diamonds.toLong(),
                        onClick = onOpenStore,
                        showAddBadge = false
                    )
                    AaaCurrencyPill(
                        type = CurrencyType.COIN,
                        amount = coins,
                        onClick = onOpenStore,
                        showAddBadge = false
                    )
                }
            }

            // Match Tokens Indicator (Red / Black Pips)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("YOU & VIKRAM: ", color = Color(0xFF4ADE80), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = if (teamAGamePoints >= 0) "$teamAGamePoints/$targetPips 🔴" else "$teamAGamePoints/$targetPips ⚫",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("OPPONENTS: ", color = Color(0xFFF87171), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = if (teamBGamePoints >= 0) "$teamBGamePoints/$targetPips 🔴" else "$teamBGamePoints/$targetPips ⚫",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Event Message Ticker & Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = lastEventMessage,
                    color = Color(0xFFE2E8F0),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier.widthIn(max = 160.dp)
                )

                IconButton(
                    onClick = onOpenStore,
                    modifier = Modifier.size(28.dp).testTag("header_store_button")
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Store", tint = CasinoGold, modifier = Modifier.size(16.dp))
                }

                IconButton(
                    onClick = onOpenAcademy,
                    modifier = Modifier.size(28.dp).testTag("header_academy_button")
                ) {
                    Icon(Icons.Default.School, contentDescription = "Academy", tint = CasinoGold, modifier = Modifier.size(16.dp))
                }

                IconButton(
                    onClick = onOpenStats,
                    modifier = Modifier.size(28.dp).testTag("header_stats_button")
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = "Stats", tint = CasinoGold, modifier = Modifier.size(16.dp))
                }

                IconButton(
                    onClick = onOpenAiCoach,
                    modifier = Modifier.size(28.dp).testTag("header_ai_coach_button")
                ) {
                    Icon(Icons.Default.SmartToy, contentDescription = "AI Coach Bot (Rules & Doubts)", tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                }

                // Table Live Chat Button with Unread Badge
                Box {
                    IconButton(
                        onClick = onOpenChat,
                        modifier = Modifier.size(28.dp).testTag("header_live_chat_button")
                    ) {
                        Icon(Icons.Default.Forum, contentDescription = "Live Chat", tint = CasinoGold, modifier = Modifier.size(16.dp))
                    }
                    if (chatUnreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$chatUnreadCount", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E293B).copy(alpha = 0.9f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CasinoGold.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onOpenSettings)
                        .testTag("header_settings_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Game Settings", tint = CasinoGold, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("SETTINGS", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun LeftArenaControlPane(
    gameState: GameState,
    canDeclarePair: Boolean,
    canRevealTrump: Boolean,
    onRevealTrumpClick: () -> Unit,
    onPairDeclareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AaaGlassCard(
        modifier = modifier,
        borderColor = Color(0xFF1E293B),
        glowColor = CasinoGold.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card Points Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    Text("CARD POINTS", color = CasinoGold, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("We: ${gameState.teamAScore.cardPoints} pt", color = Color(0xFF4ADE80), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Opp: ${gameState.teamBScore.cardPoints} pt", color = Color(0xFFF87171), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Current Bid Status Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    Text("CURRENT BID", color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (gameState.currentHighestBid >= 16)
                            "${gameState.currentHighestBid} by ${gameState.highestBidder?.displayName ?: ""}"
                        else "No Bid Yet",
                        color = CasinoGold,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Secret Trump Card Slot
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("TRUMP SUIT", color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Surface(
                    modifier = Modifier
                        .size(width = 44.dp, height = 58.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .shadow(8.dp, RoundedCornerShape(8.dp), spotColor = if (gameState.isTrumpRevealed) CasinoGold else Color.Black)
                        .border(
                            1.5.dp,
                            if (gameState.isTrumpRevealed) CasinoGold else Color(0xFF475569),
                            RoundedCornerShape(8.dp)
                        ),
                    shape = RoundedCornerShape(8.dp),
                    color = if (gameState.isTrumpRevealed) Color(0xFF1E293B) else Color(0xFF0F172A)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (gameState.isTrumpRevealed && gameState.trumpSuit != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = gameState.trumpSuit.symbol,
                                    color = if (gameState.trumpSuit.isRed) Color(0xFFEF4444) else Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = gameState.trumpSuit.displayName.take(4),
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Text(
                                text = "🎴",
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }

            // Action Buttons (Reveal Trump & Pair Declaration)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (canRevealTrump) {
                    Aaa3DButton(
                        text = "SHOW TRUMP",
                        icon = Icons.Default.Visibility,
                        onClick = onRevealTrumpClick,
                        variant = ButtonVariant.DANGER,
                        height = 32.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reveal_trump_button")
                    )
                }

                if (canDeclarePair) {
                    Aaa3DButton(
                        text = "DECLARE PAIR",
                        onClick = onPairDeclareClick,
                        variant = ButtonVariant.GOLD,
                        iconEmoji = "👑",
                        height = 32.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("declare_pair_button")
                    )
                }
            }
        }
    }
}

@Composable
private fun RightArenaCoachPane(
    tip: String?,
    isThinking: Boolean,
    onRequestHint: () -> Unit,
    onDismissHint: () -> Unit,
    onEmoteClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AaaGlassCard(
        modifier = modifier,
        borderColor = Color(0xFF1E293B),
        glowColor = Color(0xFF4338CA)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // AI Coach Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1E1B4B).copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4338CA))
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = CasinoGold, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("AI COACH", color = CasinoGold, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }

                        if (isThinking) {
                            CircularProgressIndicator(color = CasinoGold, modifier = Modifier.size(10.dp), strokeWidth = 1.5.dp)
                        }
                    }

                    Text(
                        text = tip ?: "Live tactical advice from Vikram.",
                        color = Color(0xFFE2E8F0),
                        fontSize = 9.5.sp,
                        lineHeight = 12.sp,
                        maxLines = 4,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    Aaa3DButton(
                        text = "ASK COACH",
                        onClick = onRequestHint,
                        variant = ButtonVariant.GOLD,
                        height = 26.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_ask_hint_button")
                    )
                }
            }

            // Quick Emote Banter Column
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text("QUICK CHAT", color = Color(0xFF94A3B8), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                val emotes = listOf("👑 Well played!", "🔥 Double!", "🛡️ Play safe", "🤔 Need Trump")
                emotes.forEach { emote ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onEmoteClick(emote) },
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFF334155))
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emote,
                                color = Color(0xFFCBD5E1),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CenterDealingDeckArea(
    dealer: PlayerPosition,
    dealingTargetPlayer: PlayerPosition?,
    cardSkin: String = "CLASSIC_ROYAL",
    onSkipDeal: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "deck_glow")
    val deckPulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "deck_scale"
    )

    // Directional flight offsets for the dealing card
    val targetOffsetX by animateFloatAsState(
        targetValue = when (dealingTargetPlayer) {
            PlayerPosition.WEST -> -65f
            PlayerPosition.EAST -> 65f
            else -> 0f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "deal_card_x"
    )

    val targetOffsetY by animateFloatAsState(
        targetValue = when (dealingTargetPlayer) {
            PlayerPosition.NORTH -> -52f
            PlayerPosition.SOUTH -> 52f
            else -> 0f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "deal_card_y"
    )

    Box(
        modifier = modifier
            .size(170.dp)
            .shadow(16.dp, CircleShape)
            .clip(CircleShape)
            .background(Color(0xFF000000).copy(alpha = 0.35f))
            .border(1.5.dp, CasinoGold.copy(alpha = 0.45f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Stacked 3D Deck Underneath
        Box(
            modifier = Modifier
                .offset(x = (-3).dp, y = (-3).dp)
                .width(44.dp)
                .height(62.dp)
                .shadow(4.dp, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E293B))
                .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
        )
        Box(
            modifier = Modifier
                .offset(x = (-1).dp, y = (-1).dp)
                .width(44.dp)
                .height(62.dp)
                .shadow(6.dp, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
        )

        // Top Deck Card (Center of the table)
        AnimatedPlayingCard(
            card = null,
            isFaceUp = false,
            skinId = cardSkin,
            width = 44.dp,
            height = 62.dp,
            modifier = Modifier.scale(deckPulse)
        )

        // Flying Card Packet to Target Player
        if (dealingTargetPlayer != null) {
            Box(
                modifier = Modifier.offset(x = targetOffsetX.dp, y = targetOffsetY.dp)
            ) {
                AnimatedPlayingCard(
                    card = null,
                    isFaceUp = false,
                    skinId = cardSkin,
                    width = 38.dp,
                    height = 54.dp,
                    animateDeal = true
                )
            }
        }

        // Deal Info Badge
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-4).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.94f),
                border = androidx.compose.foundation.BorderStroke(1.dp, CasinoGold)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFF38BDF8), CircleShape)
                    )
                    Text(
                        text = if (dealingTargetPlayer != null) "DEALING ➔ ${dealingTargetPlayer.displayName.uppercase()}" else "DEALER PREPARING",
                        color = CasinoGold,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        if (onSkipDeal != null) {
            Surface(
                onClick = onSkipDeal,
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.4f)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 6.dp)
            ) {
                Text(
                    text = "SKIP ❯❯",
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun LandscapeSouthPlayerHandView(
    cards: List<Card>,
    playableCards: List<Card>,
    selectedCardId: String?,
    trumpSuit: Suit?,
    isTrumpRevealed: Boolean,
    isMyTurn: Boolean,
    isDealingPhase: Boolean = false,
    cardSkin: String = "CLASSIC_ROYAL",
    onCardClick: (Card) -> Unit,
    onSortToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.96f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Turn Status and Hand Sort Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isDealingPhase) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(CasinoGold)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (cards.isEmpty()) "DEALING IN PROGRESS (WAITING FOR TURN)..." else "FIRST 4 CARDS DEALT (${cards.size}/4)",
                            color = CasinoGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    } else if (isMyTurn) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22C55E))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("YOUR TURN - TAP TO PLAY", color = Color(0xFF4ADE80), fontSize = 10.sp, fontWeight = FontWeight.Black)
                    } else {
                        Text("YOUR HAND (${cards.size} Cards)", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(onClick = onSortToggle)
                        .testTag("sort_hand_button"),
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF1E293B)
                ) {
                    Text(
                        text = "⇅ Sort",
                        color = Color(0xFFFFD700),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (cards.isEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                ) {
                    repeat(4) {
                        Box(
                            modifier = Modifier
                                .width(54.dp)
                                .height(76.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.04f))
                                .border(1.dp, CasinoGold.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🂠", color = CasinoGold.copy(alpha = 0.5f), fontSize = 18.sp)
                        }
                    }
                }
            } else {
                // Cards Horizontal Fan
                val cardCount = cards.size
                val cardSpacing = if (cardCount > 6) (-12).dp else 4.dp
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(cardSpacing, Alignment.CenterHorizontally),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    itemsIndexed(cards) { index, card ->
                        val isPlayable = isMyTurn && playableCards.contains(card)
                        val isSelected = selectedCardId == card.id
                        val isTrump = isTrumpRevealed && card.suit == trumpSuit

                        val angle = if (cardCount > 1) {
                            (index - (cardCount - 1) / 2.0f) * 3f
                        } else 0f

                        val yOffset = if (cardCount > 1) {
                            (abs(index - (cardCount - 1) / 2.0f) * 1.5f).dp
                        } else 0.dp

                        Box(
                            modifier = Modifier.offset(y = yOffset)
                        ) {
                            AnimatedPlayingCard(
                                card = card,
                                isPlayable = if (isMyTurn) isPlayable else true,
                                isSelected = isSelected,
                                isTrump = isTrump,
                                skinId = cardSkin,
                                rotationZ = angle,
                                dealIndex = index,
                                animateDeal = true,
                                width = if (cardCount > 6) 52.dp else 58.dp,
                                height = if (cardCount > 6) 74.dp else 82.dp,
                                onClick = {
                                    if (isPlayable) {
                                        onCardClick(card)
                                    }
                                },
                                onSwipeUpPlay = {
                                    if (isPlayable) {
                                        onCardClick(card)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
