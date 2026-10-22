package com.example

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.TwentyNineViewModel
import com.example.game.ads.AdMobManager
import com.example.game.auth.AccountType
import com.example.game.auth.AuthSession
import com.example.game.auth.CloudSyncStatus
import com.example.game.model.MatchMode
import com.example.game.model.UserProfile
import com.example.game.performance.DeveloperPerformanceOverlay
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.CasinoGold
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        hideSystemBars()

        window.decorView.post {
            AdMobManager.initialize(this@MainActivity)
        }

        setContent {
            MyApplicationTheme {
                TwentyNineApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

    private fun hideSystemBars() {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }
}

@Composable
fun TwentyNineApp(viewModel: TwentyNineViewModel = viewModel()) {
    val context = LocalContext.current
    val activity = context as? Activity
    val currentScreen by viewModel.currentScreen.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    val feltTheme by viewModel.feltTheme.collectAsState()
    val grandmasterAnswer by viewModel.grandmasterAnswer.collectAsState()
    val isAskingGrandmaster by viewModel.isAskingGrandmaster.collectAsState()
    val playerEconomy by viewModel.playerEconomy.collectAsState()
    val adventureProgress by viewModel.adventureProgress.collectAsState()
    val activeAdSession by viewModel.activeAdSession.collectAsState()
    val pendingLevelUp by viewModel.pendingLevelUp.collectAsState()
    val showSocialModal by viewModel.showSocialShareModal.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val friends by viewModel.friends.collectAsState(initial = emptyList())
    val incomingRequests by viewModel.incomingRequests.collectAsState(initial = emptyList())
    val suggestedPlayers by viewModel.suggestedPlayers.collectAsState(initial = emptyList())
    val recentPlayers by viewModel.recentPlayers.collectAsState(initial = emptyList())
    val blockedUsers by viewModel.blockedUsers.collectAsState(initial = emptyList())
    val pendingModeSelection by viewModel.pendingModeSelection.collectAsState()
    val pendingFriendInvite by viewModel.pendingFriendInvite.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState(initial = UserProfile())
    val authSession by viewModel.authSession.collectAsState(initial = AuthSession(userId = "", accountType = AccountType.GUEST, email = null, isLinked = false))
    val cloudSyncStatus by viewModel.cloudSyncStatus.collectAsState(initial = CloudSyncStatus.SYNCED)
    val showSettings by viewModel.showSettings.collectAsState()
    val qualityProfile by viewModel.qualityProfile.collectAsState()
    val performanceMetrics by viewModel.performanceMetrics.collectAsState()
    val isPerformanceOverlayEnabled by viewModel.isPerformanceOverlayEnabled.collectAsState()

    // Live frame telemetry loop for 60/120 FPS profiling (only active when overlay is toggled on)
    LaunchedEffect(isPerformanceOverlayEnabled) {
        if (isPerformanceOverlayEnabled) {
            while (true) {
                withFrameNanos { _ ->
                    viewModel.performanceManager.onFrameRendered()
                }
            }
        }
    }

    BackHandler(enabled = currentScreen != "LOBBY") {
        viewModel.soundManager.playButtonClick()
        viewModel.navigateTo("LOBBY")
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    if (targetState == "TABLE") {
                        (slideInHorizontally(animationSpec = tween(400), initialOffsetX = { it / 2 }) + fadeIn(animationSpec = tween(400)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(350), targetOffsetX = { -it / 2 }) + fadeOut(animationSpec = tween(350)))
                    } else if (initialState == "TABLE") {
                        (slideInHorizontally(animationSpec = tween(400), initialOffsetX = { -it / 2 }) + fadeIn(animationSpec = tween(400)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(350), targetOffsetX = { it / 2 }) + fadeOut(animationSpec = tween(350)))
                    } else {
                        (fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.96f, animationSpec = tween(300)))
                            .togetherWith(fadeOut(animationSpec = tween(250)) + scaleOut(targetScale = 1.02f, animationSpec = tween(250)))
                    }
                },
                label = "screen_nav_transition"
            ) { targetScreen ->
                when (targetScreen) {
                    "SPLASH" -> LoadingSplashScreen(
                        onLoadingComplete = {
                            viewModel.navigateTo("LOBBY")
                        }
                    )

                    "LOBBY" -> LobbyHubScreen(
                        tournamentProgress = gameState.tournamentProgress,
                        economy = playerEconomy,
                        onSelectMode = { mode ->
                            viewModel.onSelectModeRequested(mode)
                        },
                        onOpenTournament = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.navigateTo("TOURNAMENT")
                        },
                        onOpenAdventure = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.navigateTo("ADVENTURE")
                        },
                        onOpenAcademy = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.navigateTo("ACADEMY")
                        },
                        onOpenStats = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.navigateTo("STATS")
                        },
                        onOpenHouseRules = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.navigateTo("HOUSE_RULES")
                        },
                        onOpenStore = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.navigateTo("STORE")
                        },
                        onOpenSettings = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.toggleSettings(true)
                        },
                        onOpenFriends = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.navigateTo("FRIENDS")
                        },
                        onOpenSocialShare = {
                            viewModel.openSocialShareModal()
                        },
                        onOpenProfile = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.navigateTo("PROFILE")
                        },
                        onOpenRulesGuide = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.navigateTo("RULES_GUIDE")
                        },
                        onWatchRewardedAd = { placement ->
                            viewModel.soundManager.playButtonClick()
                            if (activity != null) {
                                viewModel.watchAd(placement, activity)
                            } else {
                                viewModel.watchAd(placement, null)
                            }
                        },
                        onClaimDailyReward = {
                            viewModel.gainCoins(1000L)
                            viewModel.gainXp(100)
                        },
                        hasSavedMatch = { mode -> viewModel.hasSavedMatchForMode(mode) },
                        onAskCoachBot = { query -> viewModel.askCoachBot(query) }
                    )

                    "FRIENDS" -> FriendsZoneScreen(
                        friends = friends,
                        incomingRequests = incomingRequests,
                        suggestedPlayers = suggestedPlayers,
                        recentPlayers = recentPlayers,
                        blockedUsers = blockedUsers,
                        userPlayerId = "plr_${playerEconomy.level}",
                        userDisplayName = "Player",
                        onAcceptRequest = { reqId -> viewModel.acceptFriendRequest(reqId) },
                        onDeclineRequest = { reqId -> viewModel.declineFriendRequest(reqId) },
                        onAddFriend = { user -> viewModel.addFriendUser(user) },
                        onRemoveFriend = { userId -> viewModel.removeFriendUser(userId) },
                        onBlockUser = { user -> viewModel.blockUserObj(user) },
                        onUnblockUser = { userId -> viewModel.unblockUserObj(userId) },
                        onInviteToMatch = { friend -> viewModel.setPendingFriendInvite(friend) },
                        onBack = { viewModel.navigateTo("LOBBY") }
                    )

                    "PROFILE" -> PlayerProfileScreen(
                        profile = userProfile,
                        authSession = authSession,
                        cloudSyncStatus = cloudSyncStatus,
                        onUpdateProfile = { updated -> viewModel.updateProfile(updated) },
                        onLinkGoogle = { email, id -> viewModel.linkGoogle(email, id) },
                        onLinkEmail = { email -> viewModel.linkEmail(email) },
                        onLinkFacebook = { handle -> viewModel.linkFacebook(handle) },
                        onLinkInstagram = { handle -> viewModel.linkInstagram(handle) },
                        onLinkPhone = { phone -> viewModel.linkPhone(phone) },
                        onRestorePreviousGame = { id, method, callback ->
                            viewModel.restorePreviousGame(id, method, callback)
                        },
                        onBack = { viewModel.navigateTo("LOBBY") }
                    )

                    "ADVENTURE" -> AdventureScreen(
                        adventureProgress = adventureProgress,
                        economy = playerEconomy,
                        onStartLevel = { level ->
                            viewModel.startAdventureLevel(level)
                        },
                        onUnlockWithDiamonds = { levelNumber ->
                            viewModel.unlockAdventureLevelWithDiamonds(levelNumber)
                        },
                        onWatchAdForDiamonds = { act ->
                            viewModel.watchRewardedAdForDiamonds(act)
                        },
                        onBack = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.navigateTo("LOBBY")
                        }
                    )

                    "STORE" -> StoreScreen(
                        viewModel = viewModel,
                        onBack = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.navigateTo("LOBBY")
                        }
                    )

                    "TOURNAMENT" -> TournamentScreen(
                        tournamentProgress = gameState.tournamentProgress,
                        onStartStageMatch = { stage ->
                            viewModel.soundManager.playButtonClick()
                            viewModel.startMatchWithMode(MatchMode.TOURNAMENT)
                            viewModel.navigateTo("TABLE")
                        },
                        onResetTournament = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.resetTournament()
                        },
                        onSpeedUpWithDiamonds = {
                            viewModel.skipTournamentCooldownWithDiamonds(20)
                        },
                        onSpeedUpWithAd = {
                            viewModel.skipTournamentCooldownWithAd(activity)
                        },
                        onBack = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.navigateTo("LOBBY")
                        }
                    )

                    "HOUSE_RULES" -> HouseRulesScreen(
                        currentRules = gameState.houseRules,
                        currentTheme = feltTheme,
                        onSaveRules = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.updateHouseRules(it)
                        },
                        onSetTheme = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.setFeltTheme(it)
                        },
                        onStartCustomMatch = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.startMatchWithMode(MatchMode.CUSTOM_RULES)
                            viewModel.navigateTo("TABLE")
                        },
                        onOpenRulesGuide = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.navigateTo("RULES_GUIDE")
                        },
                        onBack = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.navigateTo("LOBBY")
                        }
                    )

                    "TABLE" -> TableArenaScreen(
                        viewModel = viewModel,
                        onBackToLobby = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.navigateTo("LOBBY")
                        },
                        onOpenAcademy = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.navigateTo("ACADEMY")
                        },
                        onOpenStats = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.navigateTo("STATS")
                        },
                        onOpenStore = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.navigateTo("STORE")
                        },
                        onBackToAdventures = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.navigateTo("ADVENTURE")
                        }
                    )

                    "ACADEMY" -> AiAcademyScreen(
                        grandmasterAnswer = grandmasterAnswer,
                        isAsking = isAskingGrandmaster,
                        onAskQuestion = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.askGrandmaster(it)
                        },
                        onOpenRulesGuide = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.navigateTo("RULES_GUIDE")
                        },
                        onBackToGame = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.navigateTo("LOBBY")
                        }
                    )

                    "RULES_GUIDE" -> TwentyNineRulesGuideScreen(
                        onBack = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.navigateTo("LOBBY")
                        }
                    )

                    "STATS" -> StatsScreen(
                        repository = viewModel.statsRepository,
                        onBackToGame = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.navigateTo("LOBBY")
                        }
                    )

                    else -> LobbyHubScreen(
                        tournamentProgress = gameState.tournamentProgress,
                        economy = playerEconomy,
                        onSelectMode = { mode ->
                            viewModel.soundManager.playButtonClick()
                            viewModel.startMatchWithMode(mode)
                            viewModel.navigateTo("TABLE")
                        },
                        onOpenTournament = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.navigateTo("TOURNAMENT")
                        },
                        onOpenAdventure = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.navigateTo("ADVENTURE")
                        },
                        onOpenAcademy = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.navigateTo("ACADEMY")
                        },
                        onOpenStats = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.navigateTo("STATS")
                        },
                        onOpenHouseRules = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.navigateTo("HOUSE_RULES")
                        },
                        onOpenStore = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.navigateTo("STORE")
                        },
                        onOpenSettings = {
                            viewModel.soundManager.playButtonClick()
                            viewModel.toggleSettings(true)
                        },
                        onOpenSocialShare = {
                            viewModel.openSocialShareModal()
                        },
                        onWatchRewardedAd = { placement ->
                            viewModel.soundManager.playButtonClick()
                            viewModel.watchAd(placement, activity)
                        },
                        onClaimDailyReward = {
                            viewModel.gainCoins(1000L)
                            viewModel.gainXp(100)
                        },
                        hasSavedMatch = { mode -> viewModel.hasSavedMatchForMode(mode) },
                        onAskCoachBot = { query -> viewModel.askCoachBot(query) }
                    )
                }
            }

            // Social Accounts and Share Modal for Free Diamonds
            if (showSocialModal) {
                SocialAccountsAndShareModal(
                    viewModel = viewModel,
                    onDismiss = { viewModel.closeSocialShareModal() }
                )
            }

            // Game Mode Entry Dialog (Start New vs Resume Previous)
            pendingModeSelection?.let { mode ->
                AlertDialog(
                    onDismissRequest = { viewModel.dismissPendingModeSelection() },
                    title = { Text("Game Mode Options", color = Color.White, fontWeight = FontWeight.Bold) },
                    text = {
                        Text(
                            "You have an ongoing saved match for ${mode.title}. Would you like to start a new game or resume your previous progress?",
                            color = Color(0xFFCBD5E1),
                            fontSize = 14.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.resumeGameForPendingMode() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                        ) {
                            Text("Resume Previous", color = Color.White)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = { viewModel.startNewGameForPendingMode() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700))
                        ) {
                            Text("Start New Game")
                        }
                    },
                    containerColor = Color(0xFF0F172A),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // Friend Real-Time Match Mode Selection Dialog
            pendingFriendInvite?.let { friend ->
                var selectedModeForMatch by remember { mutableStateOf(MatchMode.CLASSIC_29) }
                AlertDialog(
                    onDismissRequest = { viewModel.setPendingFriendInvite(null) },
                    title = { Text("Play with ${friend.displayName}", color = Color.White, fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text(
                                "Select any game mode to challenge ${friend.displayName} in real-time:",
                                color = Color(0xFFCBD5E1),
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            androidx.compose.foundation.lazy.LazyColumn(
                                modifier = Modifier.height(220.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(MatchMode.entries) { mode ->
                                    val isSelected = selectedModeForMatch == mode
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedModeForMatch = mode }
                                            .border(1.dp, if (isSelected) Color(0xFFFFD700) else Color(0xFF334155), RoundedCornerShape(8.dp)),
                                        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF070B14)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(selected = isSelected, onClick = { selectedModeForMatch = mode })
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(mode.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text(mode.subtitle, color = Color(0xFF94A3B8), fontSize = 10.sp, maxLines = 1)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.startMatchWithFriend(friend, selectedModeForMatch) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                        ) {
                            Text("Start Match", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.setPendingFriendInvite(null) }) {
                            Text("Cancel", color = Color(0xFF94A3B8))
                        }
                    },
                    containerColor = Color(0xFF0F172A),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // Global Level Up Celebration Overlay - Deferred so it never interrupts active gameplay
            if (currentScreen != "TABLE") {
                pendingLevelUp?.let { level ->
                    LevelUpDialog(
                        newLevel = level,
                        onDismiss = { viewModel.dismissLevelUp() }
                    )
                }
            }

            // Global Settings Dialog (Accessible from Lobby & other screens)
            if (showSettings && currentScreen != "TABLE") {
                val isMusicOn by viewModel.isMusicEnabled.collectAsState()
                val isSoundOn by viewModel.isSoundEnabled.collectAsState()
                val isHapticsOn by viewModel.isHapticsEnabled.collectAsState()
                val currentAiDiff by viewModel.aiDifficulty.collectAsState()
                SettingsDialog(
                    selectedFeltTheme = feltTheme,
                    isSoundEnabled = isSoundOn,
                    isHapticsEnabled = isHapticsOn,
                    isMusicEnabled = isMusicOn,
                    aiDifficulty = currentAiDiff,
                    onAiDifficultySelected = { viewModel.setAiDifficulty(it) },
                    qualityProfile = qualityProfile,
                    onQualityProfileSelected = { viewModel.setQualityProfile(it) },
                    isPerformanceOverlayEnabled = isPerformanceOverlayEnabled,
                    onPerformanceOverlayToggled = { viewModel.setPerformanceOverlayEnabled(it) },
                    onMusicToggled = { viewModel.toggleBackgroundMusic(it) },
                    onFeltThemeSelected = { viewModel.setFeltTheme(it) },
                    onSoundToggled = { viewModel.toggleSound(it) },
                    onHapticsToggled = { viewModel.toggleHaptics(it) },
                    onDismiss = { viewModel.toggleSettings(false) }
                )
            }

            // Developer Performance Overlay (FPS, Frame Time, RAM, Thermals)
            if (isPerformanceOverlayEnabled) {
                DeveloperPerformanceOverlay(
                    metrics = performanceMetrics,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 10.dp, end = 12.dp)
                        .zIndex(9998f)
                )
            }

            // Global Safeguard Notification Toast
            toastMessage?.let { msg ->
                LaunchedEffect(msg) {
                    kotlinx.coroutines.delay(3500)
                    viewModel.clearToastMessage()
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, start = 40.dp, end = 40.dp)
                        .align(Alignment.TopCenter)
                        .zIndex(9999f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A).copy(alpha = 0.96f),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, CasinoGold),
                        shadowElevation = 8.dp,
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = CasinoGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
