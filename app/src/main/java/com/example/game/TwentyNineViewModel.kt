package com.example.game

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.game.ads.AdConfig
import com.example.game.ads.AdMobManager
import com.example.game.ai.GeminiCommentaryService
import com.example.game.audio.SoundEffectsManager
import com.example.game.data.AppDatabase
import com.example.game.data.GameStatsRepository
import com.example.game.data.MatchRecord
import com.example.game.engine.RoundResult
import com.example.game.engine.TwentyNineAi
import com.example.game.engine.TwentyNineEngine
import com.example.game.model.*
import com.example.game.save.GameSaveManager
import com.example.game.save.CloudProfileSyncManager
import com.example.game.social.SocialManager
import com.example.game.prefs.GamePreferencesDataStore
import com.example.ui.components.FeltTheme
import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TwentyNineViewModel(application: Application) : AndroidViewModel(application) {

    val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("TwentyNineVM", "Safeguard caught unhandled exception: ${throwable.message}", throwable)
        _toastMessage.value = "Something went wrong. Your game progress is safe. Please try again."
    }

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun clearToastMessage() {
        _toastMessage.value = null
    }

    fun returnToMainMenu() {
        try {
            saveCurrentMatchState()
            soundManager.playNavTabClick()
            _currentScreen.value = "LOBBY"
        } catch (e: Throwable) {
            Log.e("TwentyNineVM", "Safe returnToMainMenu", e)
            _currentScreen.value = "LOBBY"
        }
    }

    val gameSaveManager = GameSaveManager(application)
    val socialManager = SocialManager(application)
    val cloudProfileSyncManager = CloudProfileSyncManager(application)
    val chatManager = com.example.game.chat.OpenSourceChatManager(application)
    val performanceManager = com.example.game.performance.PerformanceManager(application)

    val chatMessages = chatManager.messages
    val chatUnreadCount = chatManager.unreadCount
    private val _isChatDrawerOpen = MutableStateFlow(false)
    val isChatDrawerOpen: StateFlow<Boolean> = _isChatDrawerOpen.asStateFlow()

    fun toggleChatDrawer(open: Boolean) {
        _isChatDrawerOpen.value = open
        if (open) {
            chatManager.markAllRead()
        }
    }

    fun sendChatMessage(text: String) {
        chatManager.sendMessage(userProfile.value, text)
    }

    fun addChatReaction(messageId: String, emoji: String) {
        chatManager.addReaction(messageId, emoji)
    }

    val qualityProfile: StateFlow<com.example.game.performance.QualityProfile> = performanceManager.selectedProfile
    val performanceMetrics: StateFlow<com.example.game.performance.PerformanceMetrics> = performanceManager.metrics
    val isPerformanceOverlayEnabled: StateFlow<Boolean> = performanceManager.isOverlayEnabled

    fun setQualityProfile(profile: com.example.game.performance.QualityProfile) {
        performanceManager.setQualityProfile(profile)
        viewModelScope.launch {
            gamePreferencesDataStore.setQualityProfile(profile.name)
        }
    }

    fun setPerformanceOverlayEnabled(enabled: Boolean) {
        performanceManager.setOverlayEnabled(enabled)
        viewModelScope.launch {
            gamePreferencesDataStore.setFpsOverlayEnabled(enabled)
        }
    }

    fun setAiDifficulty(difficulty: AiDifficulty) {
        _aiDifficulty.value = difficulty
        viewModelScope.launch {
            gamePreferencesDataStore.setAiDifficulty(difficulty.name)
        }
    }

    val userProfile = cloudProfileSyncManager.userProfile
    val authSession = cloudProfileSyncManager.authSession
    val cloudSyncStatus = cloudProfileSyncManager.syncStatus

    fun updateProfile(profile: UserProfile) {
        cloudProfileSyncManager.updateProfileLocally(profile)
    }

    fun linkGoogle(email: String, googleId: String) {
        cloudProfileSyncManager.linkGoogleAccount(email, googleId)
    }

    fun linkEmail(email: String) {
        cloudProfileSyncManager.linkEmailAccount(email)
    }

    fun linkSocial(platform: String, handle: String) {
        cloudProfileSyncManager.linkSocialAccount(platform, handle)
    }

    fun linkFacebook(handle: String) {
        cloudProfileSyncManager.linkFacebookAccount(handle)
    }

    fun linkInstagram(handle: String) {
        cloudProfileSyncManager.linkInstagramAccount(handle)
    }

    fun linkPhone(phoneNumber: String) {
        cloudProfileSyncManager.linkPhoneNumber(phoneNumber)
    }

    fun restorePreviousGame(identifier: String, authMethod: String, onResult: (Boolean, String) -> Unit) {
        cloudProfileSyncManager.restorePreviousGame(identifier, authMethod, onResult)
    }

    val friends = socialManager.friends
    val incomingRequests = socialManager.incomingRequests
    val suggestedPlayers = socialManager.suggestedPlayers
    val recentPlayers = socialManager.recentPlayers
    val blockedUsers = socialManager.blockedUsers

    fun acceptFriendRequest(reqId: String) { socialManager.acceptFriendRequest(reqId) }
    fun declineFriendRequest(reqId: String) { socialManager.declineFriendRequest(reqId) }
    fun addFriendUser(user: SocialUser) { socialManager.addFriend(user) }
    fun removeFriendUser(userId: String) { socialManager.removeFriend(userId) }
    fun blockUserObj(user: SocialUser) { socialManager.blockUser(user) }
    fun unblockUserObj(userId: String) { socialManager.unblockUser(userId) }

    private val _pendingModeSelection = MutableStateFlow<MatchMode?>(null)
    val pendingModeSelection: StateFlow<MatchMode?> = _pendingModeSelection.asStateFlow()

    private val _pendingFriendInvite = MutableStateFlow<SocialUser?>(null)
    val pendingFriendInvite: StateFlow<SocialUser?> = _pendingFriendInvite.asStateFlow()

    fun onSelectModeRequested(mode: MatchMode) {
        soundManager.playButtonClick()
        if (gameSaveManager.hasSavedMatch(mode)) {
            _pendingModeSelection.value = mode
        } else {
            startMatchWithMode(mode, resumeIfExists = false)
            navigateTo("TABLE")
        }
    }

    fun dismissPendingModeSelection() {
        _pendingModeSelection.value = null
    }

    fun startNewGameForPendingMode() {
        val mode = _pendingModeSelection.value ?: return
        _pendingModeSelection.value = null
        startMatchWithMode(mode, resumeIfExists = false)
        navigateTo("TABLE")
    }

    fun resumeGameForPendingMode() {
        val mode = _pendingModeSelection.value ?: return
        _pendingModeSelection.value = null
        startMatchWithMode(mode, resumeIfExists = true)
        navigateTo("TABLE")
    }

    fun setPendingFriendInvite(user: SocialUser?) {
        _pendingFriendInvite.value = user
    }

    fun startMatchWithFriend(user: SocialUser, mode: MatchMode) {
        _pendingFriendInvite.value = null
        soundManager.playButtonClick()
        startMatchWithMode(mode, resumeIfExists = false)
        _gameState.update { state ->
            val updatedPlayers = state.players.toMutableMap()
            val partnerKey = PlayerPosition.NORTH
            if (updatedPlayers.containsKey(partnerKey)) {
                updatedPlayers[partnerKey] = updatedPlayers[partnerKey]!!.copy(
                    name = user.displayName
                )
            }
            state.copy(
                lastEventMessage = "Playing real-time match with friend ${user.displayName} in ${mode.title}!"
            )
        }
        navigateTo("TABLE")
    }

    private val db = AppDatabase.getDatabase(application)
    val statsRepository = GameStatsRepository(db.matchRecordDao())
    val soundManager = SoundEffectsManager(application)
    private val geminiService = GeminiCommentaryService()

    private val _gameState = MutableStateFlow(createInitialGameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _activeAdSession = MutableStateFlow<ActiveAdSession?>(null)
    val activeAdSession: StateFlow<ActiveAdSession?> = _activeAdSession.asStateFlow()

    private val _pendingLevelUp = MutableStateFlow<Int?>(null)
    val pendingLevelUp: StateFlow<Int?> = _pendingLevelUp.asStateFlow()

    private val _roundResult = MutableStateFlow<RoundResult?>(null)
    val roundResult: StateFlow<RoundResult?> = _roundResult.asStateFlow()

    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    private val _currentScreen = MutableStateFlow("LOBBY") // LOBBY, TABLE, ACADEMY, STATS, STORE, TOURNAMENT, HOUSE_RULES, SPLASH
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _feltTheme = MutableStateFlow(FeltTheme.EMERALD_CASINO)
    val feltTheme: StateFlow<FeltTheme> = _feltTheme.asStateFlow()

    private val _sortBySuit = MutableStateFlow(true)
    val sortBySuit: StateFlow<Boolean> = _sortBySuit.asStateFlow()

    private val _grandmasterAnswer = MutableStateFlow<String?>(null)
    val grandmasterAnswer: StateFlow<String?> = _grandmasterAnswer.asStateFlow()

    private val _isAskingGrandmaster = MutableStateFlow(false)
    val isAskingGrandmaster: StateFlow<Boolean> = _isAskingGrandmaster.asStateFlow()

    private var phase2DealtCards = mutableMapOf<PlayerPosition, List<Card>>()
    private var phase1DealtCards = mapOf<PlayerPosition, List<Card>>()
    private var dealingJob: Job? = null

    private var matchCountSinceLastInterstitial = 0

    private val prefs = application.getSharedPreferences("twenty_nine_prefs", Application.MODE_PRIVATE)

    private val _playerEconomy = MutableStateFlow(loadPlayerEconomy())
    val playerEconomy: StateFlow<PlayerEconomy> = _playerEconomy.asStateFlow()

    private val _adventureProgress = MutableStateFlow(loadAdventureProgress())
    val adventureProgress: StateFlow<AdventureProgress> = _adventureProgress.asStateFlow()

    private val _linkedSocialAccounts = MutableStateFlow(loadLinkedSocialAccounts())
    val linkedSocialAccounts: StateFlow<List<LinkedSocialAccount>> = _linkedSocialAccounts.asStateFlow()

    val gamePreferencesDataStore = GamePreferencesDataStore(application)

    private val _isMusicEnabled = MutableStateFlow(true)
    val isMusicEnabled: StateFlow<Boolean> = _isMusicEnabled.asStateFlow()

    private val _isSoundEnabled = MutableStateFlow(true)
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    private val _isHapticsEnabled = MutableStateFlow(true)
    val isHapticsEnabled: StateFlow<Boolean> = _isHapticsEnabled.asStateFlow()

    private val _aiDifficulty = MutableStateFlow(AiDifficulty.ADAPTIVE)
    val aiDifficulty: StateFlow<AiDifficulty> = _aiDifficulty.asStateFlow()

    private val _showSocialShareModal = MutableStateFlow(false)
    val showSocialShareModal: StateFlow<Boolean> = _showSocialShareModal.asStateFlow()

    init {
        AdMobManager.initialize(application)

        // Collect and persist preferences via Jetpack DataStore
        viewModelScope.launch {
            gamePreferencesDataStore.isMusicEnabledFlow.collect { enabled ->
                _isMusicEnabled.value = enabled
                soundManager.isMusicEnabled = enabled
            }
        }
        viewModelScope.launch {
            gamePreferencesDataStore.isSoundEffectsEnabledFlow.collect { enabled ->
                _isSoundEnabled.value = enabled
                soundManager.isSoundEnabled = enabled
            }
        }
        viewModelScope.launch {
            gamePreferencesDataStore.isHapticsEnabledFlow.collect { enabled ->
                _isHapticsEnabled.value = enabled
                soundManager.isHapticsEnabled = enabled
            }
        }
        viewModelScope.launch {
            gamePreferencesDataStore.feltThemeFlow.collect { themeName ->
                try {
                    _feltTheme.value = FeltTheme.valueOf(themeName)
                } catch (_: Exception) {}
            }
        }
        viewModelScope.launch {
            gamePreferencesDataStore.qualityProfileFlow.collect { profileName ->
                try {
                    val qp = com.example.game.performance.QualityProfile.valueOf(profileName)
                    performanceManager.setQualityProfile(qp)
                } catch (_: Exception) {}
            }
        }
        viewModelScope.launch {
            gamePreferencesDataStore.aiDifficultyFlow.collect { diffName ->
                try {
                    _aiDifficulty.value = AiDifficulty.valueOf(diffName)
                } catch (_: Exception) {}
            }
        }
        viewModelScope.launch {
            gamePreferencesDataStore.isFpsOverlayEnabledFlow.collect { enabled ->
                performanceManager.setOverlayEnabled(enabled)
            }
        }

        // Restore table felt theme based on player's saved equipped theme
        val savedThemeId = _playerEconomy.value.equippedTableTheme
        val initialTheme = when (savedThemeId) {
            "ROYAL_SAPPHIRE" -> FeltTheme.ROYAL_SAPPHIRE
            "MIDNIGHT_VELVET" -> FeltTheme.ONYX_NOIR
            "CRIMSON_GOLD" -> FeltTheme.CRIMSON_LOUNGE
            "MUGHAL_COURT" -> FeltTheme.MUGHAL_DARBAR
            "NEON_CYBER" -> FeltTheme.NEON_CYBER
            "ROYAL_AMETHYST" -> FeltTheme.ROYAL_AMETHYST
            else -> FeltTheme.EMERALD_CASINO
        }
        _feltTheme.value = initialTheme
    }

    private fun getTodayDateString(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
    }

    private fun loadPlayerEconomy(): PlayerEconomy {
        val today = getTodayDateString()
        val lastDate = prefs.getString("econ_last_ad_date", "") ?: ""
        val dailyAds = if (lastDate == today) prefs.getInt("econ_daily_ads", 0) else 0

        val coins = prefs.getLong("econ_coins", AdConfig.INITIAL_PLAYER_COINS)
        val diamonds = prefs.getInt("econ_diamonds", AdConfig.INITIAL_PLAYER_DIAMONDS)
        val level = prefs.getInt("econ_level", 1)
        val xp = prefs.getInt("econ_current_xp", 50)
        val totalAds = prefs.getInt("econ_total_ads", 0)

        val equippedSkin = prefs.getString("econ_equipped_skin", "CLASSIC_ROYAL") ?: "CLASSIC_ROYAL"
        val equippedTheme = prefs.getString("econ_equipped_theme", "EMERALD_CASINO") ?: "EMERALD_CASINO"
        val unlockedSkins = prefs.getStringSet("econ_unlocked_skins", setOf("CLASSIC_ROYAL")) ?: setOf("CLASSIC_ROYAL")
        val unlockedThemes = prefs.getStringSet("econ_unlocked_themes", setOf("EMERALD_CASINO")) ?: setOf("EMERALD_CASINO")

        return PlayerEconomy(
            coins = coins,
            diamonds = diamonds,
            level = level,
            currentXp = xp,
            totalAdsWatched = totalAds,
            dailyAdsWatched = dailyAds,
            lastAdDate = today,
            equippedCardSkin = equippedSkin,
            equippedTableTheme = equippedTheme,
            unlockedCardSkins = unlockedSkins,
            unlockedTableThemes = unlockedThemes
        )
    }

    private fun savePlayerEconomy(economy: PlayerEconomy) {
        val editor = prefs.edit()
        editor.putLong("econ_coins", economy.coins)
        editor.putInt("econ_diamonds", economy.diamonds)
        editor.putInt("econ_level", economy.level)
        editor.putInt("econ_current_xp", economy.currentXp)
        editor.putInt("econ_total_ads", economy.totalAdsWatched)
        editor.putInt("econ_daily_ads", economy.dailyAdsWatched)
        editor.putString("econ_last_ad_date", economy.lastAdDate)
        editor.putString("econ_equipped_skin", economy.equippedCardSkin)
        editor.putString("econ_equipped_theme", economy.equippedTableTheme)
        editor.putStringSet("econ_unlocked_skins", economy.unlockedCardSkins)
        editor.putStringSet("econ_unlocked_themes", economy.unlockedTableThemes)
        editor.apply()
    }

    private fun updateEconomy(transform: (PlayerEconomy) -> PlayerEconomy) {
        _playerEconomy.update { current ->
            val updated = transform(current)
            savePlayerEconomy(updated)
            updated
        }
    }

    private fun loadAdventureProgress(): AdventureProgress {
        val defaultLevels = defaultAdventureLevels()
        val completedLevelsCount = prefs.getInt("adv_completed_count", 0)
        val totalStars = prefs.getInt("adv_total_stars", 0)

        val updatedLevels = defaultLevels.map { level ->
            val isUnlocked = if (level.levelNumber == 1) true else prefs.getBoolean("adv_lvl_${level.levelNumber}_unlocked", false)
            val isCompleted = prefs.getBoolean("adv_lvl_${level.levelNumber}_completed", false)
            val stars = prefs.getInt("adv_lvl_${level.levelNumber}_stars", 0)
            level.copy(
                isUnlocked = isUnlocked,
                isCompleted = isCompleted,
                starsEarned = stars
            )
        }
        return AdventureProgress(
            currentLevelNumber = (completedLevelsCount + 1).coerceAtMost(defaultLevels.size),
            totalStars = totalStars,
            levelsCompleted = completedLevelsCount,
            levels = updatedLevels
        )
    }

    private fun saveAdventureProgress(progress: AdventureProgress) {
        val editor = prefs.edit()
        editor.putInt("adv_completed_count", progress.levelsCompleted)
        editor.putInt("adv_total_stars", progress.totalStars)
        progress.levels.forEach { level ->
            editor.putBoolean("adv_lvl_${level.levelNumber}_unlocked", level.isUnlocked)
            editor.putBoolean("adv_lvl_${level.levelNumber}_completed", level.isCompleted)
            editor.putInt("adv_lvl_${level.levelNumber}_stars", level.starsEarned)
        }
        editor.apply()
    }

    fun startAdventureLevel(level: AdventureLevel) {
        soundManager.playButtonClick()
        _roundResult.value = null

        val initialPlayers = mapOf(
            PlayerPosition.SOUTH to Player(PlayerPosition.SOUTH, "You", false),
            PlayerPosition.WEST to Player(PlayerPosition.WEST, level.opponent1Name, true),
            PlayerPosition.NORTH to Player(PlayerPosition.NORTH, level.partnerName, true),
            PlayerPosition.EAST to Player(PlayerPosition.EAST, level.opponent2Name, true)
        )

        _gameState.value = GameState(
            phase = GamePhase.IDLE,
            matchMode = MatchMode.ADVENTURE,
            dealer = PlayerPosition.NORTH,
            players = initialPlayers,
            teamAScore = TeamScore(Team.TEAM_A),
            teamBScore = TeamScore(Team.TEAM_B),
            houseRules = _gameState.value.houseRules.copy(targetPipsToWin = level.targetPips),
            tournamentProgress = _gameState.value.tournamentProgress,
            adventureLevel = level
        )

        startNewRound()
        navigateTo("TABLE")
    }


    fun navigateTo(screen: String) {
        soundManager.playButtonClick()
        if (_currentScreen.value == "TABLE" && screen != "TABLE") {
            saveCurrentMatchState()
        }
        _currentScreen.value = screen
    }

    fun saveCurrentMatchState() {
        val current = _gameState.value
        if (current.matchMode != MatchMode.ADVENTURE &&
            current.phase != GamePhase.IDLE &&
            current.phase != GamePhase.DEAL_PHASE_1 &&
            current.phase != GamePhase.MATCH_OVER
        ) {
            gameSaveManager.saveMatch(current.matchMode, current, phase2DealtCards)
        }
    }

    fun restartCurrentMatch() {
        soundManager.playButtonClick()
        val currentMode = _gameState.value.matchMode
        gameSaveManager.clearSavedMatch(currentMode)
        startMatchWithMode(currentMode, resumeIfExists = false)
    }

    fun hasSavedMatchForMode(mode: MatchMode): Boolean = gameSaveManager.hasSavedMatch(mode)

    fun toggleSettings(show: Boolean) {
        soundManager.playButtonClick()
        _showSettings.value = show
    }

    fun setFeltTheme(theme: FeltTheme) {
        _feltTheme.value = theme
        viewModelScope.launch {
            gamePreferencesDataStore.setFeltTheme(theme.name)
        }
    }

    fun toggleSound(enabled: Boolean) {
        _isSoundEnabled.value = enabled
        soundManager.isSoundEnabled = enabled
        viewModelScope.launch {
            gamePreferencesDataStore.setSoundEffectsEnabled(enabled)
        }
    }

    fun toggleHaptics(enabled: Boolean) {
        _isHapticsEnabled.value = enabled
        soundManager.isHapticsEnabled = enabled
        viewModelScope.launch {
            gamePreferencesDataStore.setHapticsEnabled(enabled)
        }
    }

    fun startNewMatch() {
        startMatchWithMode(_gameState.value.matchMode, resumeIfExists = false)
    }

    fun startMatchWithMode(mode: MatchMode, resumeIfExists: Boolean = true) {
        soundManager.playButtonClick()
        _roundResult.value = null

        // Check if there is an existing saved game for this SPECIFIC mode
        if (resumeIfExists && gameSaveManager.hasSavedMatch(mode)) {
            val saved = gameSaveManager.loadMatch(mode)
            if (saved != null) {
                val (restoredState, restoredPhase2) = saved
                phase2DealtCards = restoredPhase2.toMutableMap()
                _gameState.value = restoredState.copy(
                    lastEventMessage = "Resumed saved ${mode.title} match!"
                )
                navigateTo("TABLE")
                if (restoredState.phase == GamePhase.BIDDING) {
                    triggerAiBiddingIfNeeded()
                } else if (restoredState.phase == GamePhase.PLAYING_TRICK) {
                    triggerAiPlayIfNeeded()
                }
                return
            }
        }

        // Otherwise, start fresh for this mode and clear any previous save for it
        gameSaveManager.clearSavedMatch(mode)

        val opponentNames = when (mode) {
            MatchMode.TOURNAMENT -> {
                when (_gameState.value.tournamentProgress.currentStage) {
                    TournamentStage.QUARTER_FINALS -> Pair("Kabir (Kolkata)", "Suraj (Kolkata)")
                    TournamentStage.SEMI_FINALS -> Pair("Dev (Mumbai)", "Arjun (Mumbai)")
                    TournamentStage.GRAND_FINALS -> Pair("Rudra (Delhi Master)", "Shiv (Delhi Master)")
                    TournamentStage.CHAMPION -> Pair("Champion AI 1", "Champion AI 2")
                }
            }
            MatchMode.HIGH_ROLLER -> Pair("Lord Singhania", "Raja Dhiraj")
            MatchMode.SPEED_BLITZ -> Pair("Lightning Rahul", "Flash Sameer")
            MatchMode.ROYAL_DUEL -> Pair("Master Rudra", "Grandmaster Arya")
            MatchMode.BLITZ_ROUND -> Pair("Rohan (Blitz)", "Aarav (Blitz)")
            MatchMode.SINGLE_HAND -> Pair("Rohan", "Aarav")
            else -> Pair("Rohan", "Aarav")
        }

        val partnerName = when (mode) {
            MatchMode.HIGH_ROLLER -> "Vikram VIP (Partner)"
            MatchMode.ROYAL_DUEL -> "Arya (Partner)"
            MatchMode.SINGLE_HAND -> "Vikram (Folded)"
            else -> "Vikram (Partner)"
        }

        val initialPlayers = mapOf(
            PlayerPosition.SOUTH to Player(PlayerPosition.SOUTH, "You", false),
            PlayerPosition.WEST to Player(PlayerPosition.WEST, opponentNames.first, true),
            PlayerPosition.NORTH to Player(PlayerPosition.NORTH, partnerName, true),
            PlayerPosition.EAST to Player(PlayerPosition.EAST, opponentNames.second, true)
        )

        val appliedRules = when (mode) {
            MatchMode.SPEED_BLITZ -> _gameState.value.houseRules.copy(aiSpeedMs = 300L, targetPipsToWin = 1)
            MatchMode.BLITZ_ROUND -> _gameState.value.houseRules.copy(targetPipsToWin = 1)
            MatchMode.ROYAL_DUEL -> _gameState.value.houseRules.copy(targetPipsToWin = 4)
            MatchMode.SINGLE_HAND -> _gameState.value.houseRules.copy(targetPipsToWin = 1)
            MatchMode.HIGH_ROLLER -> _gameState.value.houseRules.copy(targetPipsToWin = 6)
            MatchMode.CUSTOM_RULES -> _gameState.value.houseRules
            else -> _gameState.value.houseRules.copy(targetPipsToWin = 6)
        }

        val initialMinBid = if (mode == MatchMode.HIGH_ROLLER) 20 else 16

        _gameState.value = GameState(
            phase = GamePhase.IDLE,
            matchMode = mode,
            minBid = initialMinBid,
            currentHighestBid = if (mode == MatchMode.HIGH_ROLLER) 19 else 15,
            dealer = if (mode == MatchMode.SINGLE_HAND) PlayerPosition.EAST else PlayerPosition.NORTH,
            players = initialPlayers,
            teamAScore = TeamScore(Team.TEAM_A),
            teamBScore = TeamScore(Team.TEAM_B),
            houseRules = appliedRules,
            tournamentProgress = _gameState.value.tournamentProgress
        )

        startNewRound()
        navigateTo("TABLE")
        saveCurrentMatchState()
    }

    fun updateHouseRules(rules: HouseRules) {
        _gameState.update { it.copy(houseRules = rules) }
    }

    fun resetTournament() {
        _gameState.update { it.copy(tournamentProgress = TournamentProgress()) }
    }

    fun onUserSingleHand() {
        val current = _gameState.value
        if (current.phase != GamePhase.BIDDING || current.activeTurn != PlayerPosition.SOUTH) return
        soundManager.playTrumpReveal()
        _gameState.update {
            it.copy(
                currentHighestBid = 29,
                highestBidder = PlayerPosition.SOUTH,
                isSingleHand = true,
                lastEventMessage = "🔥 SINGLE HAND 29 CALLED! You play solo to win all 8 tricks!"
            )
        }
        concludeBidding(PlayerPosition.SOUTH)
    }

    fun startNewRound() {
        dealingJob?.cancel()
        _roundResult.value = null
        val currentDealer = _gameState.value.dealer.next
        val (p1, p2) = TwentyNineEngine.dealDeck()
        phase1DealtCards = p1
        phase2DealtCards = p2.toMutableMap()

        val currentMode = _gameState.value.matchMode
        val modeMinBid = if (currentMode == MatchMode.HIGH_ROLLER) 20 else 16
        val modeInitialBid = if (currentMode == MatchMode.HIGH_ROLLER) 19 else 15

        // Clockwise deal order starting next to dealer
        val dealOrder = listOf(
            currentDealer.next,
            currentDealer.next.next,
            currentDealer.next.next.next,
            currentDealer
        )

        // Clear player hands initially so the dealing animation is visible
        val emptyPlayers = _gameState.value.players.mapValues { (_, player) ->
            player.copy(cards = emptyList(), speechBubble = null)
        }

        soundManager.playCardSwoosh()

        _gameState.update {
            it.copy(
                phase = GamePhase.DEAL_PHASE_1,
                dealer = currentDealer,
                activeTurn = currentDealer,
                dealingTargetPlayer = dealOrder.first(),
                players = emptyPlayers,
                bidHistory = emptyList(),
                currentHighestBid = modeInitialBid,
                minBid = modeMinBid,
                highestBidder = null,
                isDoubled = false,
                isRedoubled = false,
                isSingleHand = false,
                consecutivePasses = 0,
                trumpSuit = null,
                isTrumpRevealed = false,
                trumpRevealedBy = null,
                trumpRevealedInTrickIndex = null,
                pairDeclaredByTeam = null,
                currentTrickIndex = 0,
                currentTrick = Trick(0, currentDealer.next),
                completedTricks = emptyList(),
                lastEventMessage = "🃏 First Card Distribution: Dealer ${currentDealer.displayName} is distributing 4 cards...",
                vfxTrigger = VfxEvent(VfxType.CARD_DEAL, message = "Dealing 4 cards to each player...")
            )
        }

        dealingJob = viewModelScope.launch {
            val dealingDelay = if (currentMode == MatchMode.SPEED_BLITZ) 200L else 320L
            val accumulatedCards = mutableMapOf<PlayerPosition, List<Card>>()

            for (targetPos in dealOrder) {
                delay(dealingDelay)
                soundManager.playCardSwoosh()

                val rawCards = phase1DealtCards[targetPos] ?: emptyList()
                val targetCards = if (targetPos == PlayerPosition.SOUTH) sortUserHand(rawCards) else rawCards
                accumulatedCards[targetPos] = targetCards

                _gameState.update { state ->
                    val updatedPlayers = state.players.mapValues { (pos, player) ->
                        if (accumulatedCards.containsKey(pos)) {
                            player.copy(cards = accumulatedCards[pos] ?: emptyList())
                        } else {
                            player
                        }
                    }
                    state.copy(
                        players = updatedPlayers,
                        dealingTargetPlayer = targetPos,
                        lastEventMessage = "🃏 Dealt 4 cards to ${targetPos.displayName}"
                    )
                }
            }

            // Brief pause so player sees all 4 hands with 4 cards
            delay(dealingDelay)
            finishDealPhase1()
        }
    }

    fun skipCardDealing() {
        if (_gameState.value.phase != GamePhase.DEAL_PHASE_1) return
        dealingJob?.cancel()
        finishDealPhase1()
    }

    private fun finishDealPhase1() {
        val current = _gameState.value
        val currentDealer = current.dealer
        val currentMode = current.matchMode
        val modeMinBid = if (currentMode == MatchMode.HIGH_ROLLER) 20 else 16
        val modeInitialBid = if (currentMode == MatchMode.HIGH_ROLLER) 19 else 15

        val fullPlayers = current.players.mapValues { (pos, player) ->
            val rawCards = phase1DealtCards[pos] ?: emptyList()
            val initialCards = if (pos == PlayerPosition.SOUTH) sortUserHand(rawCards) else rawCards
            player.copy(cards = initialCards, speechBubble = null)
        }

        soundManager.playCardSwoosh()

        if (currentMode == MatchMode.SINGLE_HAND) {
            _gameState.update {
                it.copy(
                    phase = GamePhase.SET_TRUMP,
                    dealer = currentDealer,
                    activeTurn = PlayerPosition.SOUTH,
                    dealingTargetPlayer = null,
                    players = fullPlayers,
                    bidHistory = listOf(BidRecord(PlayerPosition.SOUTH, 29, isPass = false)),
                    currentHighestBid = 29,
                    highestBidder = PlayerPosition.SOUTH,
                    isSingleHand = true,
                    minBid = 29,
                    trumpSuit = null,
                    isTrumpRevealed = false,
                    trumpRevealedBy = null,
                    trumpRevealedInTrickIndex = null,
                    pairDeclaredByTeam = null,
                    currentTrickIndex = 0,
                    currentTrick = Trick(0, PlayerPosition.SOUTH),
                    completedTricks = emptyList(),
                    lastEventMessage = "🔥 SINGLE HAND 29! You play solo to win all 8 tricks! Choose your Trump:",
                    vfxTrigger = VfxEvent(VfxType.CARD_DEAL, message = "Single Hand 29: Choose Trump Suit")
                )
            }
            saveCurrentMatchState()
            return
        }

        _gameState.update {
            it.copy(
                phase = GamePhase.BIDDING,
                dealer = currentDealer,
                activeTurn = currentDealer.next,
                dealingTargetPlayer = null,
                players = fullPlayers,
                bidHistory = emptyList(),
                currentHighestBid = modeInitialBid,
                minBid = modeMinBid,
                highestBidder = null,
                isDoubled = false,
                isRedoubled = false,
                isSingleHand = false,
                consecutivePasses = 0,
                trumpSuit = null,
                isTrumpRevealed = false,
                trumpRevealedBy = null,
                trumpRevealedInTrickIndex = null,
                pairDeclaredByTeam = null,
                currentTrickIndex = 0,
                currentTrick = Trick(0, currentDealer.next),
                completedTricks = emptyList(),
                lastEventMessage = "Bidding started by ${currentDealer.next.displayName} (Min bid: $modeMinBid)",
                vfxTrigger = VfxEvent(VfxType.CARD_DEAL, message = "First card distribution complete!")
            )
        }

        saveCurrentMatchState()
        triggerAiBiddingIfNeeded()
    }

    // --- Bidding Phase ---

    fun onUserPlaceBid(bid: Int) {
        val current = _gameState.value
        if (current.phase != GamePhase.BIDDING || current.activeTurn != PlayerPosition.SOUTH) return

        soundManager.playBidChime()
        recordBid(PlayerPosition.SOUTH, bid, isPass = false)

        val currentAdv = current.adventureLevel
        if (currentAdv != null) {
            val updatedTasks = currentAdv.tasks.map { task ->
                if ((task.id == "task_bid_16" && bid >= 16) ||
                    (task.id == "task_bid_18" && bid >= 18) ||
                    (task.id == "task_bid_20" && bid >= 20) ||
                    (task.id == "task_bid_21" && bid >= 21)
                ) {
                    task.copy(isCompleted = true)
                } else task
            }
            _gameState.update { it.copy(adventureLevel = currentAdv.copy(tasks = updatedTasks)) }
        }
    }

    fun onUserPassBid() {
        val current = _gameState.value
        if (current.phase != GamePhase.BIDDING || current.activeTurn != PlayerPosition.SOUTH) return

        soundManager.playButtonClick()
        recordBid(PlayerPosition.SOUTH, 0, isPass = true)
    }

    fun onUserDouble() {
        val current = _gameState.value
        if (current.phase != GamePhase.BIDDING || current.activeTurn != PlayerPosition.SOUTH) return
        soundManager.playBidChime()
        _gameState.update {
            it.copy(
                isDoubled = true,
                doubledBy = PlayerPosition.SOUTH,
                lastEventMessage = "You called DOUBLE! Stakes are doubled!"
            )
        }
        advanceBiddingTurn()
    }

    fun onUserRedouble() {
        val current = _gameState.value
        if (current.phase != GamePhase.BIDDING || current.activeTurn != PlayerPosition.SOUTH) return
        soundManager.playBidChime()
        _gameState.update {
            it.copy(
                isRedoubled = true,
                lastEventMessage = "You called REDOUBLE! Stakes are 4X!"
            )
        }
        advanceBiddingTurn()
    }

    private fun recordBid(player: PlayerPosition, bidAmount: Int, isPass: Boolean) {
        val current = _gameState.value
        val newHistory = current.bidHistory + BidRecord(player, bidAmount, isPass)

        val updatedPlayers = current.players.toMutableMap()
        updatedPlayers[player] = updatedPlayers[player]!!.copy(
            speechBubble = if (isPass) "Pass" else "Bid $bidAmount"
        )

        val nextHighestBid = if (!isPass && bidAmount > current.currentHighestBid) bidAmount else current.currentHighestBid
        val nextHighestBidder = if (!isPass && bidAmount > current.currentHighestBid) player else current.highestBidder
        val passes = if (isPass) current.consecutivePasses + 1 else 0

        _gameState.update {
            it.copy(
                bidHistory = newHistory,
                currentHighestBid = nextHighestBid,
                highestBidder = nextHighestBidder,
                consecutivePasses = passes,
                players = updatedPlayers,
                lastEventMessage = if (isPass) "${player.displayName} Passed" else "${player.displayName} Bid $bidAmount"
            )
        }

        // Check if bidding concluded
        // Bidding concludes if 3 consecutive passes after a bid, or all 4 pass initially
        if (nextHighestBidder != null && passes >= 3) {
            concludeBidding(nextHighestBidder)
        } else if (nextHighestBidder == null && passes >= 4) {
            // Official 29 Rules: If all 4 players pass, the opener takes the forced 16 minimum bid!
            val forcedBidder = _gameState.value.dealer.next
            _gameState.update {
                it.copy(
                    lastEventMessage = "All passed! ${forcedBidder.displayName} takes forced 16 opening bid."
                )
            }
            recordBid(forcedBidder, 16, isPass = false)
        } else {
            advanceBiddingTurn()
        }
    }

    private fun advanceBiddingTurn() {
        val current = _gameState.value
        var next = current.activeTurn.next
        val passedPlayers = current.bidHistory.filter { it.isPass }.map { it.player }.toSet()
        var loopCount = 0
        while (passedPlayers.contains(next) && loopCount < 4) {
            next = next.next
            loopCount++
        }
        _gameState.update { it.copy(activeTurn = next) }
        triggerAiBiddingIfNeeded()
    }

    private fun triggerAiBiddingIfNeeded() {
        val current = _gameState.value
        if (current.phase != GamePhase.BIDDING) return
        val active = current.activeTurn
        if (active == PlayerPosition.SOUTH) return // User's turn to act

        viewModelScope.launch {
            delay(380)
            val latest = _gameState.value
            if (latest.phase != GamePhase.BIDDING || latest.activeTurn != active) return@launch
            val aiPlayer = latest.players[active] ?: return@launch
            val aiBid = TwentyNineAi.evaluateBid(
                active,
                aiPlayer.cards,
                latest.currentHighestBid,
                latest.highestBidder
            )

            if (aiBid != null) {
                soundManager.playBidChime()
                recordBid(active, aiBid, isPass = false)
            } else {
                soundManager.playButtonClick()
                recordBid(active, 0, isPass = true)
            }
        }
    }

    private fun concludeBidding(winner: PlayerPosition) {
        _gameState.update {
            it.copy(
                phase = GamePhase.SET_TRUMP,
                highestBidder = winner,
                lastEventMessage = "${winner.displayName} won the bid with ${it.currentHighestBid}!"
            )
        }

        if (winner == PlayerPosition.SOUTH) {
            // Prompt User to select hidden trump suit
        } else {
            // AI selects hidden trump
            viewModelScope.launch {
                delay(400)
                val aiCards = _gameState.value.players[winner]?.cards ?: emptyList()
                val selectedSuit = TwentyNineAi.selectTrumpSuit(aiCards)
                onSelectTrumpSuit(selectedSuit)
            }
        }
    }

    fun sortUserHand(cards: List<Card>): List<Card> {
        return if (_sortBySuit.value) {
            cards.sortedWith(compareBy({ it.suit.ordinal }, { -it.power }))
        } else {
            cards.sortedByDescending { it.power }
        }
    }

    fun onSelectTrumpSuit(suit: Suit) {
        soundManager.playTrumpReveal()
        _gameState.update {
            it.copy(
                trumpSuit = suit,
                phase = GamePhase.DEAL_PHASE_2,
                lastEventMessage = "Secret Trump Suit set! Dealing remaining 4 cards..."
            )
        }

        // Deal Phase 2
        viewModelScope.launch {
            delay(450)
            soundManager.playCardSwoosh()
            val finalPlayers = _gameState.value.players.mapValues { (pos, p) ->
                val p2 = phase2DealtCards[pos] ?: emptyList()
                val combined = p.cards + p2
                val sorted = if (pos == PlayerPosition.SOUTH) sortUserHand(combined) else combined.sortedByDescending { it.power }
                p.copy(cards = sorted, speechBubble = null)
            }

            val leadPlayer = _gameState.value.dealer.next
            _gameState.update {
                it.copy(
                    phase = GamePhase.PLAYING_TRICK,
                    players = finalPlayers,
                    activeTurn = leadPlayer,
                    currentTrick = Trick(0, leadPlayer),
                    lastEventMessage = "Trick 1 started by ${leadPlayer.displayName}"
                )
            }

            triggerAiPlayIfNeeded()
        }
    }

    // --- Playing Tricks Phase ---

    fun onUserPlayCard(card: Card) {
        val current = _gameState.value
        if (current.phase != GamePhase.PLAYING_TRICK || current.activeTurn != PlayerPosition.SOUTH) return

        val playable = TwentyNineEngine.getPlayableCards(
            current.players[PlayerPosition.SOUTH]?.cards ?: emptyList(),
            current.currentTrick,
            current.trumpSuit,
            current.isTrumpRevealed
        )

        if (!playable.contains(card)) {
            _gameState.update { it.copy(lastEventMessage = "You must follow the led suit (${current.currentTrick.ledSuit?.displayName})!") }
            return
        }

        executeCardPlay(PlayerPosition.SOUTH, card)
    }

    fun onUserRequestTrumpReveal() {
        val current = _gameState.value
        if (current.phase != GamePhase.PLAYING_TRICK || current.activeTurn != PlayerPosition.SOUTH) return
        if (!TwentyNineEngine.canRequestTrumpReveal(
                current.players[PlayerPosition.SOUTH]?.cards ?: emptyList(),
                current.currentTrick,
                current.isTrumpRevealed
            )) return

        revealTrump(PlayerPosition.SOUTH)
    }

    fun onUserDeclarePair() {
        val current = _gameState.value
        if (current.pairDeclaredByTeam != null) return
        val canDeclare = TwentyNineEngine.canDeclarePair(
            current.players[PlayerPosition.SOUTH]?.cards ?: emptyList(),
            current.trumpSuit,
            current.isTrumpRevealed,
            pairAlreadyDeclared = false
        )
        if (!canDeclare) return

        soundManager.playPairRoyalty()
        val currentAdv = current.adventureLevel
        val updatedTasks = currentAdv?.tasks?.map { task ->
            if (task.id == "task_declare_pair") task.copy(isCompleted = true) else task
        }

        _gameState.update {
            it.copy(
                pairDeclaredByTeam = Team.TEAM_A,
                pairSuit = it.trumpSuit,
                lastEventMessage = "👑 ROYALTY! You declared Pair! Bid adjusted by 4 points!",
                vfxTrigger = VfxEvent(VfxType.PAIR_ROYALTY, message = "👑 Royalty Declared!"),
                adventureLevel = if (currentAdv != null && updatedTasks != null) currentAdv.copy(tasks = updatedTasks) else currentAdv
            )
        }
    }

    private fun revealTrump(requestingPlayer: PlayerPosition) {
        soundManager.playTrumpReveal()
        val currentAdv = _gameState.value.adventureLevel
        val updatedTasks = currentAdv?.tasks?.map { task ->
            if (task.id == "task_reveal_trump" && requestingPlayer == PlayerPosition.SOUTH) task.copy(isCompleted = true) else task
        }

        _gameState.update {
            it.copy(
                isTrumpRevealed = true,
                trumpRevealedBy = requestingPlayer,
                trumpRevealedInTrickIndex = it.currentTrickIndex,
                lastEventMessage = "🔥 TRUMP REVEALED! The trump suit is ${it.trumpSuit?.displayName}!",
                vfxTrigger = VfxEvent(VfxType.TRUMP_REVEAL, suit = it.trumpSuit, message = "🔥 Trump Revealed!"),
                adventureLevel = if (currentAdv != null && updatedTasks != null) currentAdv.copy(tasks = updatedTasks) else currentAdv
            )
        }
    }

    private fun executeCardPlay(player: PlayerPosition, card: Card) {
        soundManager.playCardSnap()

        val current = _gameState.value
        val rawRemainingHand = (current.players[player]?.cards ?: emptyList()).filter { it.id != card.id }
        val updatedHand = if (player == PlayerPosition.SOUTH) sortUserHand(rawRemainingHand) else rawRemainingHand
        val updatedPlayers = current.players.toMutableMap()
        updatedPlayers[player] = updatedPlayers[player]!!.copy(cards = updatedHand, speechBubble = null)

        val updatedTrick = current.currentTrick.copy(
            plays = current.currentTrick.plays + PlayedCard(player, card)
        )

        _gameState.update {
            it.copy(
                players = updatedPlayers,
                currentTrick = updatedTrick,
                vfxTrigger = VfxEvent(VfxType.CARD_SLAM, position = player)
            )
        }

        if (updatedTrick.isComplete) {
            resolveCompletedTrick(updatedTrick)
        } else {
            val nextTurn = current.activeTurn.next
            _gameState.update { it.copy(activeTurn = nextTurn) }
            triggerAiPlayIfNeeded()
        }
    }

    private fun resolveCompletedTrick(completedTrick: Trick) {
        _gameState.update { it.copy(phase = GamePhase.TRICK_RESOLVING) }

        viewModelScope.launch {
            delay(1000)
            val current = _gameState.value
            val (winner, points) = TwentyNineEngine.evaluateTrick(
                completedTrick,
                current.trumpSuit,
                current.isTrumpRevealed
            )

            soundManager.playTrickWin()

            val updatedTeamAScore = if (winner.team == Team.TEAM_A) {
                current.teamAScore.copy(
                    cardPoints = current.teamAScore.cardPoints + points,
                    tricksWon = current.teamAScore.tricksWon + 1
                )
            } else current.teamAScore

            val updatedTeamBScore = if (winner.team == Team.TEAM_B) {
                current.teamBScore.copy(
                    cardPoints = current.teamBScore.cardPoints + points,
                    tricksWon = current.teamBScore.tricksWon + 1
                )
            } else current.teamBScore

            val isLastTrick = current.currentTrickIndex == 7

            val updatedTrickList = current.completedTricks + completedTrick.copy(winner = winner)

            if (winner == PlayerPosition.SOUTH) {
                incrementQuest("quest_win_tricks", 1)
                gainXp(15)
            }

            // Adventure task checks on trick resolution
            val currentAdv = current.adventureLevel
            var trickUpdatedAdv = currentAdv
            if (currentAdv != null && winner.team == Team.TEAM_A) {
                val jacksInTrick = completedTrick.plays.count { it.card.rank == Rank.JACK }
                val updatedTasks = currentAdv.tasks.map { task ->
                    if (task.id == "task_capture_jack" && jacksInTrick > 0) {
                        task.copy(isCompleted = true)
                    } else if (task.id == "task_capture_2_jacks" && jacksInTrick >= 1) {
                        val totalJacksTaken = current.completedTricks.filter { it.winner?.team == Team.TEAM_A }.flatMap { t -> t.plays.map { it.card } }.count { it.rank == Rank.JACK } + jacksInTrick
                        if (totalJacksTaken >= 2) task.copy(isCompleted = true) else task
                    } else if (task.id == "task_capture_all_jacks") {
                        val totalJacksTaken = current.completedTricks.filter { it.winner?.team == Team.TEAM_A }.flatMap { t -> t.plays.map { it.card } }.count { it.rank == Rank.JACK } + jacksInTrick
                        if (totalJacksTaken >= 3) task.copy(isCompleted = true) else task
                    } else if (task.id == "task_last_trick" && isLastTrick) {
                        task.copy(isCompleted = true)
                    } else task
                }
                trickUpdatedAdv = currentAdv.copy(tasks = updatedTasks)
            }

            if (!isLastTrick) {
                _gameState.update {
                    it.copy(
                        phase = GamePhase.PLAYING_TRICK,
                        teamAScore = updatedTeamAScore,
                        teamBScore = updatedTeamBScore,
                        completedTricks = updatedTrickList,
                        currentTrickIndex = it.currentTrickIndex + 1,
                        currentTrick = Trick(it.currentTrickIndex + 1, winner),
                        activeTurn = winner,
                        lastTrickWinner = winner,
                        lastEventMessage = "${winner.displayName} took Trick ${it.currentTrickIndex + 1} (+$points pts)!",
                        vfxTrigger = VfxEvent(VfxType.TRICK_COLLECT, position = winner),
                        adventureLevel = trickUpdatedAdv
                    )
                }
                saveCurrentMatchState()
                triggerAiPlayIfNeeded()
            } else {
                if (trickUpdatedAdv != null) {
                    _gameState.update { it.copy(adventureLevel = trickUpdatedAdv) }
                }
                // Last trick (8th trick) -> Add +1 bonus point to winner!
                val finalTeamA = if (winner.team == Team.TEAM_A) {
                    updatedTeamAScore.copy(cardPoints = updatedTeamAScore.cardPoints + 1)
                } else updatedTeamAScore

                val finalTeamB = if (winner.team == Team.TEAM_B) {
                    updatedTeamBScore.copy(cardPoints = updatedTeamBScore.cardPoints + 1)
                } else updatedTeamBScore

                concludeRound(finalTeamA, finalTeamB, updatedTrickList, winner)
            }
        }
    }

    private fun concludeRound(
        teamAScore: TeamScore,
        teamBScore: TeamScore,
        completedTricks: List<Trick>,
        lastWinner: PlayerPosition
    ) {
        val current = _gameState.value
        val result = TwentyNineEngine.calculateRoundScore(
            highestBidder = current.highestBidder ?: PlayerPosition.SOUTH,
            bidAmount = current.currentHighestBid,
            teamAPoints = teamAScore.cardPoints,
            teamBPoints = teamBScore.cardPoints,
            isDoubled = current.isDoubled,
            isRedoubled = current.isRedoubled,
            pairDeclaredBy = current.pairDeclaredByTeam
        )

        _roundResult.value = result

        val targetPips = when (current.matchMode) {
            MatchMode.CUSTOM_RULES -> current.houseRules.targetPipsToWin
            MatchMode.ROYAL_DUEL -> 4
            else -> 6
        }

        val newGamePointsA = (current.teamAScore.gamePoints + result.teamAGamePointsDelta).coerceIn(-targetPips, targetPips)
        val newGamePointsB = (current.teamBScore.gamePoints + result.teamBGamePointsDelta).coerceIn(-targetPips, targetPips)

        val isMatchOver = if (current.matchMode == MatchMode.BLITZ_ROUND || current.matchMode == MatchMode.SPEED_BLITZ) {
            true
        } else {
            Math.abs(newGamePointsA) >= targetPips || Math.abs(newGamePointsB) >= targetPips
        }

        val isUserTeamWin = (result.bidderTeam == Team.TEAM_A && result.isSuccess) ||
                (result.bidderTeam == Team.TEAM_B && !result.isSuccess)

        if (isUserTeamWin) {
            soundManager.playVictoryFanfare()
            val winXp = when (current.matchMode) {
                MatchMode.SINGLE_HAND -> 500
                MatchMode.HIGH_ROLLER -> 400
                MatchMode.ROYAL_DUEL -> 250
                MatchMode.SPEED_BLITZ -> 200
                else -> 150
            }
            val winCoins = when (current.matchMode) {
                MatchMode.SINGLE_HAND -> 2500L
                MatchMode.HIGH_ROLLER -> 3000L
                MatchMode.ROYAL_DUEL -> 1500L
                MatchMode.SPEED_BLITZ -> 1000L
                else -> 750L
            }
            gainXp(winXp)
            updateEconomy { it.copy(coins = it.coins + winCoins) }
            incrementQuest("quest_win_match", 1)
        } else {
            soundManager.playDefeatSound()
            gainXp(50)
            updateEconomy { it.copy(coins = it.coins + 150L) }
        }

        // Tournament Progression update
        var updatedTournament = current.tournamentProgress
        if (current.matchMode == MatchMode.TOURNAMENT && isMatchOver && isUserTeamWin) {
            val nextStage = when (current.tournamentProgress.currentStage) {
                TournamentStage.QUARTER_FINALS -> TournamentStage.SEMI_FINALS
                TournamentStage.SEMI_FINALS -> TournamentStage.GRAND_FINALS
                TournamentStage.GRAND_FINALS -> TournamentStage.CHAMPION
                TournamentStage.CHAMPION -> TournamentStage.CHAMPION
            }
            updatedTournament = current.tournamentProgress.copy(
                currentStage = nextStage,
                matchesWon = current.tournamentProgress.matchesWon + 1,
                isTrophyUnlocked = nextStage == TournamentStage.CHAMPION
            )
        }

        // Adventure Progression update
        var updatedAdventureLevel = current.adventureLevel
        if (current.matchMode == MatchMode.ADVENTURE && updatedAdventureLevel != null) {
            val updatedTasks = updatedAdventureLevel.tasks.map { task ->
                if (task.id == "task_capture_15_pts" && teamAScore.cardPoints >= 15) {
                    task.copy(isCompleted = true)
                } else if (task.id.startsWith("task_win_level") && isUserTeamWin && isMatchOver) {
                    task.copy(isCompleted = true)
                } else if (task.id == "task_defend_bid" && result.bidderTeam == Team.TEAM_B && !result.isSuccess) {
                    task.copy(isCompleted = true)
                } else task
            }

            val stars = updatedTasks.count { it.isCompleted }.coerceIn(1, 3)
            val levelNum = updatedAdventureLevel.levelNumber

            if (isMatchOver && isUserTeamWin) {
                updatedAdventureLevel = updatedAdventureLevel.copy(
                    isCompleted = true,
                    starsEarned = maxOf(updatedAdventureLevel.starsEarned, stars),
                    tasks = updatedTasks
                )

                val currentProg = _adventureProgress.value
                val updatedLevels = currentProg.levels.map { lvl ->
                    if (lvl.levelNumber == levelNum) {
                        lvl.copy(
                            isCompleted = true,
                            starsEarned = maxOf(lvl.starsEarned, stars),
                            tasks = updatedTasks
                        )
                    } else if (lvl.levelNumber == levelNum + 1) {
                        lvl.copy(isUnlocked = true)
                    } else lvl
                }
                val totalStars = updatedLevels.sumOf { it.starsEarned }
                val completedCount = updatedLevels.count { it.isCompleted }
                val newProgress = currentProg.copy(
                    levels = updatedLevels,
                    totalStars = totalStars,
                    levelsCompleted = completedCount
                )
                _adventureProgress.value = newProgress
                saveAdventureProgress(newProgress)

                gainCoins(updatedAdventureLevel.rewardCoins)
                gainDiamonds(updatedAdventureLevel.rewardDiamonds)
                gainXp(updatedAdventureLevel.rewardXp)
            } else {
                updatedAdventureLevel = updatedAdventureLevel.copy(tasks = updatedTasks)
            }
        }

        _gameState.update {
            it.copy(
                phase = if (isMatchOver) GamePhase.MATCH_OVER else GamePhase.ROUND_SUMMARY,
                teamAScore = teamAScore.copy(gamePoints = newGamePointsA),
                teamBScore = teamBScore.copy(gamePoints = newGamePointsB),
                completedTricks = completedTricks,
                tournamentProgress = updatedTournament,
                adventureLevel = updatedAdventureLevel,
                vfxTrigger = if (isUserTeamWin) {
                    VfxEvent(VfxType.FIREWORKS_WIN, message = if (isMatchOver) "Match Champion!" else "Round Victory!")
                } else {
                    VfxEvent(VfxType.DEFEAT_GLOOM, message = if (isMatchOver) "Match Defeat" else "Round Lost")
                }
            )
        }

        if (isMatchOver) {
            gameSaveManager.clearSavedMatch(current.matchMode)
        } else {
            saveCurrentMatchState()
        }

        // Record match in local Room Database
        viewModelScope.launch {
            val isWin = isUserTeamWin
            val winCoins = if (current.isSingleHand) 2500L else if (isWin) 750L else 150L
            val winXp = if (current.isSingleHand) 500 else if (isWin) 150 else 50
            val mvp = if (isWin) (current.highestBidder?.displayName ?: "South (You)") else "East (Pro)"

            statsRepository.recordMatch(
                MatchRecord(
                    matchMode = current.matchMode.title,
                    isWin = isWin,
                    bidderTeam = result.bidderTeam.displayName,
                    bidderPlayer = current.highestBidder?.displayName ?: "South (You)",
                    originalBid = result.originalBid,
                    adjustedBid = result.adjustedBid,
                    teamCardPoints = teamAScore.cardPoints,
                    opponentCardPoints = teamBScore.cardPoints,
                    gamePointsDelta = result.teamAGamePointsDelta,
                    trumpSuit = current.trumpSuit?.displayName ?: "Spades",
                    isSingleHand = current.isSingleHand,
                    isDoubled = current.isDoubled,
                    isRedoubled = current.isRedoubled,
                    pairDeclared = current.pairDeclaredByTeam != null,
                    mvpPlayer = mvp,
                    coinsEarned = winCoins,
                    xpEarned = winXp
                )
            )
        }
    }

    private fun triggerAiPlayIfNeeded() {
        val current = _gameState.value
        if (current.phase != GamePhase.PLAYING_TRICK) return
        val active = current.activeTurn
        if (active == PlayerPosition.SOUTH) {
            // Auto refresh tactical advice in background
            return
        }

        viewModelScope.launch {
            delay(450)
            val latest = _gameState.value
            if (latest.phase != GamePhase.PLAYING_TRICK || latest.activeTurn != active) return@launch
            val aiPlayer = latest.players[active] ?: return@launch
            val decision = TwentyNineAi.chooseMove(
                position = active,
                hand = aiPlayer.cards,
                currentTrick = latest.currentTrick,
                trumpSuit = latest.trumpSuit,
                isTrumpRevealed = latest.isTrumpRevealed,
                hasDeclaredPair = latest.pairDeclaredByTeam != null
            )

            if (decision.requestTrumpReveal) {
                revealTrump(active)
                delay(400)
            }

            executeCardPlay(active, decision.card)
        }
    }

    fun requestAiHint() {
        val current = _gameState.value
        _gameState.update { it.copy(isAiThinking = true) }

        viewModelScope.launch {
            val advice = geminiService.getTacticalAdvice(
                hand = current.players[PlayerPosition.SOUTH]?.cards ?: emptyList(),
                currentTrick = current.currentTrick,
                trumpSuit = current.trumpSuit,
                isTrumpRevealed = current.isTrumpRevealed,
                highestBid = current.currentHighestBid,
                highestBidder = current.highestBidder
            )

            _gameState.update {
                it.copy(
                    isAiThinking = false,
                    aiMentorTip = advice
                )
            }
        }
    }

    fun dismissAiHint() {
        _gameState.update { it.copy(aiMentorTip = null) }
    }

    fun toggleHandSort() {
        soundManager.playButtonClick()
        _sortBySuit.update { !it }
        val userCards = _gameState.value.players[PlayerPosition.SOUTH]?.cards ?: return
        val sortedCards = sortUserHand(userCards)
        val updatedPlayers = _gameState.value.players.toMutableMap()
        updatedPlayers[PlayerPosition.SOUTH] = updatedPlayers[PlayerPosition.SOUTH]!!.copy(cards = sortedCards)
        _gameState.update { it.copy(players = updatedPlayers) }
    }

    fun sendQuickEmote(userEmote: String) {
        soundManager.playButtonClick()
        val updatedPlayers = _gameState.value.players.toMutableMap()
        updatedPlayers[PlayerPosition.SOUTH] = updatedPlayers[PlayerPosition.SOUTH]!!.copy(speechBubble = userEmote)
        _gameState.update { it.copy(players = updatedPlayers) }

        viewModelScope.launch {
            delay(1200)
            val current = _gameState.value
            val partnersReaction = when (userEmote) {
                "👑 Well played!" -> listOf("Thanks partner!", "Solid game!", "Teamwork!").random()
                "🔥 Let's Double!" -> listOf("I've got your back!", "Let's crush it!", "Bold move!").random()
                "🛡️ Play safe Vikram" -> listOf("Understood!", "Holding my Jack!", "Protecting our lead.").random()
                "🤔 Need Trump!" -> listOf("Waiting for the right moment!", "Hang tight!", "Let's reveal soon.").random()
                else -> "Let's win this!"
            }
            val replyPlayers = current.players.toMutableMap()
            replyPlayers[PlayerPosition.NORTH] = replyPlayers[PlayerPosition.NORTH]!!.copy(speechBubble = partnersReaction)
            _gameState.update { it.copy(players = replyPlayers) }

            delay(2000)
            val clearPlayers = _gameState.value.players.mapValues { (_, p) -> p.copy(speechBubble = null) }
            _gameState.update { it.copy(players = clearPlayers) }
        }
    }

    fun askGrandmaster(question: String) {
        if (question.isBlank()) return
        soundManager.playButtonClick()
        _isAskingGrandmaster.value = true
        _grandmasterAnswer.value = null

        viewModelScope.launch(coroutineExceptionHandler) {
            try {
                val answer = geminiService.askGrandmasterQuestion(question)
                _grandmasterAnswer.value = answer
            } catch (e: Throwable) {
                Log.e("TwentyNineVM", "Error querying Grandmaster AI", e)
                _grandmasterAnswer.value = "Grandmaster Vikram: Protect your Jacks (3 pts) and 9s (2 pts) until trumps are revealed! Lead low cards if your partner made the bid."
            } finally {
                _isAskingGrandmaster.value = false
            }
        }
    }

    fun clearGrandmasterAnswer() {
        _grandmasterAnswer.value = null
    }

    suspend fun askCoachBot(question: String): String {
        return try {
            geminiService.askGrandmasterQuestion(question)
        } catch (e: Throwable) {
            Log.e("TwentyNineVM", "Coach Bot query fallback", e)
            "AI Coach: In 29, the card ranks are J (3 pts), 9 (2 pts), A (1 pt), 10 (1 pt). K, Q, 8, 7 score 0 pts. Total 28 card points + 1 last trick bonus = 29 total points."
        }
    }

    fun unlockAdventureLevelWithDiamonds(levelNumber: Int): Boolean {
        val cost = AdConfig.DIAMOND_UNLOCK_LEVEL_COST
        val current = _playerEconomy.value
        if (current.diamonds >= cost) {
            soundManager.playPurchaseSuccess()
            updateEconomy { it.copy(diamonds = it.diamonds - cost) }

            val currentAdv = _adventureProgress.value
            val updatedLevels = currentAdv.levels.map { lvl ->
                if (lvl.levelNumber == levelNumber) lvl.copy(isUnlocked = true) else lvl
            }
            val newProgress = currentAdv.copy(levels = updatedLevels)
            _adventureProgress.value = newProgress
            saveAdventureProgress(newProgress)
            return true
        }
        return false
    }

    fun watchRewardedAdForDiamonds(
        activity: Activity,
        onRewardGranted: () -> Unit = {},
        onLimitReached: () -> Unit = {},
        onAdFailed: () -> Unit = {}
    ) {
        val current = _playerEconomy.value
        if (current.dailyAdsWatched >= AdConfig.MAX_DIAMOND_REWARDED_ADS_PER_DAY) {
            onLimitReached()
            return
        }

        soundManager.playButtonClick()
        AdMobManager.showRewardedAd(
            activity = activity,
            onUserEarnedReward = { amount ->
                soundManager.playDiamondCollect()
                val diamondsGained = if (amount > 0) amount else AdConfig.DIAMONDS_PER_REWARDED_AD
                val coinsGained = AdConfig.COINS_PER_REWARDED_AD
                updateEconomy {
                    it.copy(
                        diamonds = it.diamonds + diamondsGained,
                        coins = it.coins + coinsGained,
                        totalAdsWatched = it.totalAdsWatched + 1,
                        dailyAdsWatched = it.dailyAdsWatched + 1,
                        lastAdDate = getTodayDateString()
                    )
                }
                incrementQuest("quest_watch_ads", 1)
                gainXp(50)
                activity.runOnUiThread {
                    android.widget.Toast.makeText(activity, "Rewarded with +$diamondsGained 💎 & +$coinsGained 🪙 Coins!", android.widget.Toast.LENGTH_SHORT).show()
                }
                onRewardGranted()
            },
            onAdClosed = { },
            onFallback = {
                if (AdConfig.IS_TEST_MODE || AdMobManager.isRunningInEmulator()) {
                    soundManager.playDiamondCollect()
                    val diamondsGained = AdConfig.DIAMONDS_PER_REWARDED_AD
                    val coinsGained = AdConfig.COINS_PER_REWARDED_AD
                    updateEconomy {
                        it.copy(
                            diamonds = it.diamonds + diamondsGained,
                            coins = it.coins + coinsGained,
                            totalAdsWatched = it.totalAdsWatched + 1,
                            dailyAdsWatched = it.dailyAdsWatched + 1,
                            lastAdDate = getTodayDateString()
                        )
                    }
                    incrementQuest("quest_watch_ads", 1)
                    gainXp(50)
                    activity.runOnUiThread {
                        android.widget.Toast.makeText(activity, "Rewarded with +$diamondsGained 💎 & +$coinsGained 🪙 Coins!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    onRewardGranted()
                } else {
                    activity.runOnUiThread {
                        android.widget.Toast.makeText(activity, "Google Ad is loading, please try again in a moment", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    onAdFailed()
                }
            }
        )
    }

    fun watchRewardedAdForCoins(
        activity: Activity,
        rewardCoins: Long = 2500L,
        onRewardGranted: () -> Unit = {},
        onAdFailed: () -> Unit = {}
    ) {
        soundManager.playButtonClick()
        AdMobManager.showRewardedAd(
            activity = activity,
            onUserEarnedReward = { _ ->
                soundManager.playCoinCollect()
                updateEconomy {
                    it.copy(
                        coins = it.coins + rewardCoins,
                        totalAdsWatched = it.totalAdsWatched + 1,
                        dailyAdsWatched = it.dailyAdsWatched + 1,
                        lastAdDate = getTodayDateString()
                    )
                }
                incrementQuest("quest_watch_ads", 1)
                gainXp(50)
                activity.runOnUiThread {
                    android.widget.Toast.makeText(activity, "Claimed +$rewardCoins 🪙 Free Coins! 🎉", android.widget.Toast.LENGTH_SHORT).show()
                }
                onRewardGranted()
            },
            onAdClosed = { },
            onFallback = {
                if (AdConfig.IS_TEST_MODE || AdMobManager.isRunningInEmulator()) {
                    soundManager.playCoinCollect()
                    updateEconomy {
                        it.copy(
                            coins = it.coins + rewardCoins,
                            totalAdsWatched = it.totalAdsWatched + 1,
                            dailyAdsWatched = it.dailyAdsWatched + 1,
                            lastAdDate = getTodayDateString()
                        )
                    }
                    incrementQuest("quest_watch_ads", 1)
                    gainXp(50)
                    activity.runOnUiThread {
                        android.widget.Toast.makeText(activity, "Claimed +$rewardCoins 🪙 Free Coins! 🎉", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    onRewardGranted()
                } else {
                    activity.runOnUiThread {
                        android.widget.Toast.makeText(activity, "Google Ad is loading, please try again in a moment", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    onAdFailed()
                }
            }
        )
    }

    fun doubleMatchCoinsWithAd(
        activity: Activity,
        baseCoins: Long,
        onDoubled: (Long) -> Unit,
        onAdFailed: () -> Unit = {}
    ) {
        soundManager.playButtonClick()
        AdMobManager.showRewardedAd(
            activity = activity,
            onUserEarnedReward = { _ ->
                soundManager.playCoinCollect()
                val additionalCoins = if (baseCoins > 0) (baseCoins * 2).coerceAtLeast(2500L) else 2500L
                updateEconomy {
                    it.copy(
                        coins = it.coins + additionalCoins,
                        totalAdsWatched = it.totalAdsWatched + 1
                    )
                }
                onDoubled(additionalCoins)
            },
            onAdClosed = { },
            onFallback = {
                if (AdConfig.IS_TEST_MODE || AdMobManager.isRunningInEmulator()) {
                    soundManager.playCoinCollect()
                    val additionalCoins = if (baseCoins > 0) (baseCoins * 2).coerceAtLeast(2500L) else 2500L
                    updateEconomy {
                        it.copy(
                            coins = it.coins + additionalCoins,
                            totalAdsWatched = it.totalAdsWatched + 1
                        )
                    }
                    activity.runOnUiThread {
                        android.widget.Toast.makeText(activity, "Bonus +$additionalCoins 🪙 Coins granted!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    onDoubled(additionalCoins)
                } else {
                    activity.runOnUiThread {
                        android.widget.Toast.makeText(activity, "Google Ad is loading, please try again in a moment", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    onAdFailed()
                }
            }
        )
    }

    fun reviveOrShieldRoundWithAd(
        activity: Activity,
        onShielded: () -> Unit,
        onAdFailed: () -> Unit = {}
    ) {
        soundManager.playButtonClick()
        AdMobManager.showRewardedAd(
            activity = activity,
            onUserEarnedReward = { _ ->
                soundManager.playVictoryFanfare()
                val res = _roundResult.value
                if (res != null) {
                    val restoredRes = res.copy(
                        teamAGamePointsDelta = 0,
                        teamBGamePointsDelta = 0,
                        isSuccess = true
                    )
                    _roundResult.value = restoredRes
                    val state = _gameState.value
                    _gameState.value = state.copy(
                        teamAScore = state.teamAScore.copy(
                            gamePoints = (state.teamAScore.gamePoints + Math.abs(res.teamAGamePointsDelta)).coerceAtLeast(0)
                        )
                    )
                }
                updateEconomy { it.copy(totalAdsWatched = it.totalAdsWatched + 1) }
                onShielded()
            },
            onAdClosed = { },
            onFallback = {
                if (AdConfig.IS_TEST_MODE || AdMobManager.isRunningInEmulator()) {
                    soundManager.playVictoryFanfare()
                    val res = _roundResult.value
                    if (res != null) {
                        val restoredRes = res.copy(
                            teamAGamePointsDelta = 0,
                            teamBGamePointsDelta = 0,
                            isSuccess = true
                        )
                        _roundResult.value = restoredRes
                        val state = _gameState.value
                        _gameState.value = state.copy(
                            teamAScore = state.teamAScore.copy(
                                gamePoints = (state.teamAScore.gamePoints + Math.abs(res.teamAGamePointsDelta)).coerceAtLeast(0)
                            )
                        )
                    }
                    updateEconomy { it.copy(totalAdsWatched = it.totalAdsWatched + 1) }
                    activity.runOnUiThread {
                        android.widget.Toast.makeText(activity, "Contract penalty shielded! 🛡️", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    onShielded()
                } else {
                    activity.runOnUiThread {
                        android.widget.Toast.makeText(activity, "Google Ad is loading, please try again in a moment", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    onAdFailed()
                }
            }
        )
    }

    fun triggerNaturalBreakInterstitial(activity: Activity, onFinished: () -> Unit) {
        AdMobManager.recordMatchCompletion()
        AdMobManager.showInterstitialIfEligible(activity = activity, forceShow = false) {
            onFinished()
        }
    }

    fun watchAd(placement: AdPlacement, activity: Activity? = null) {
        soundManager.playButtonClick()
        if (activity != null) {
            when (placement.adType) {
                "REWARDED_INTERSTITIAL" -> {
                    AdMobManager.showRewardedInterstitialAd(
                        activity = activity,
                        onUserEarnedReward = { _ ->
                            grantReward(placement)
                        },
                        onAdClosed = { },
                        onFallback = {
                            if (AdConfig.IS_TEST_MODE || AdMobManager.isRunningInEmulator()) {
                                grantReward(placement)
                                activity.runOnUiThread {
                                    android.widget.Toast.makeText(activity, "Reward granted! 💎", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                activity.runOnUiThread {
                                    android.widget.Toast.makeText(activity, "Google Ad is loading, please try again in a moment", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                    return
                }
                "REWARDED" -> {
                    AdMobManager.showRewardedAd(
                        activity = activity,
                        onUserEarnedReward = { _ ->
                            grantReward(placement)
                        },
                        onAdClosed = { },
                        onFallback = {
                            if (AdConfig.IS_TEST_MODE || AdMobManager.isRunningInEmulator()) {
                                grantReward(placement)
                                activity.runOnUiThread {
                                    android.widget.Toast.makeText(activity, "Reward granted! 💎", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                activity.runOnUiThread {
                                    android.widget.Toast.makeText(activity, "Google Ad is loading, please try again in a moment", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                    return
                }
                "INTERSTITIAL" -> {
                    AdMobManager.showInterstitialAd(
                        activity = activity,
                        onAdClosed = {
                            grantReward(placement)
                        }
                    )
                    return
                }
            }
        }

        activity?.runOnUiThread {
            android.widget.Toast.makeText(activity, "Google Ad is loading, please try again in a moment", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun grantReward(placement: AdPlacement) {
        soundManager.playDiamondCollect()
        val diamonds = placement.rewardDiamonds
        val coins = placement.rewardCoins

        updateEconomy { current ->
            current.copy(
                diamonds = current.diamonds + diamonds,
                coins = current.coins + coins,
                totalAdsWatched = current.totalAdsWatched + 1,
                dailyAdsWatched = current.dailyAdsWatched + 1
            )
        }

        incrementQuest("quest_watch_ads", 1)
        gainXp(50)

        if (placement == AdPlacement.REWARDED_DOUBLE_MATCH) {
            updateEconomy { it.copy(coins = it.coins + 2500L) }
        } else if (placement == AdPlacement.REWARDED_COIN_BAG) {
            updateEconomy { it.copy(coins = it.coins + 2500L) }
        } else if (placement == AdPlacement.REWARDED_HIGH_ROLLER_STASH) {
            updateEconomy { it.copy(coins = it.coins + 5000L) }
        } else if (placement == AdPlacement.REWARDED_JACKPOT_COIN_VAULT) {
            updateEconomy { it.copy(coins = it.coins + 10000L) }
        } else if (placement == AdPlacement.REWARDED_INTERSTITIAL_INSTANT_UNLOCK) {
            updateEconomy { it.copy(mysteryChestCooldownEndMs = 0L) }
        } else if (placement == AdPlacement.REWARDED_INTERSTITIAL_TOURNAMENT_RUSH) {
            updateEconomy { it.copy(tournamentCooldownEndMs = 0L) }
        } else if (placement == AdPlacement.REWARDED_REVIVE_ROUND) {
            val res = _roundResult.value
            if (res != null) {
                _roundResult.value = res.copy(
                    teamAGamePointsDelta = 0,
                    teamBGamePointsDelta = 0,
                    isSuccess = true
                )
            }
        }
    }

    fun claimAdReward() {
        val session = _activeAdSession.value ?: return
        grantReward(session.placement)
        _activeAdSession.value = null
    }

    fun openMysteryChestWithDiamonds(cost: Int = 1): Boolean {
        val current = _playerEconomy.value
        if (current.diamonds >= cost) {
            soundManager.playDiamondCollect()
            updateEconomy {
                it.copy(
                    diamonds = it.diamonds - cost + 2,
                    coins = it.coins + 1500L,
                    mysteryChestCooldownEndMs = System.currentTimeMillis() + (4 * 3600 * 1000L)
                )
            }
            gainXp(150)
            return true
        }
        return false
    }

    fun openMysteryChestWithAd(activity: Activity? = null) {
        watchAd(AdPlacement.REWARDED_INTERSTITIAL_INSTANT_UNLOCK, activity)
    }

    fun skipTournamentCooldownWithDiamonds(cost: Int = 1): Boolean {
        val current = _playerEconomy.value
        if (current.diamonds >= cost) {
            soundManager.playPurchaseSuccess()
            updateEconomy {
                it.copy(
                    diamonds = it.diamonds - cost,
                    tournamentCooldownEndMs = 0L
                )
            }
            return true
        }
        return false
    }

    fun skipTournamentCooldownWithAd(activity: Activity? = null) {
        watchAd(AdPlacement.REWARDED_INTERSTITIAL_TOURNAMENT_RUSH, activity)
    }

    fun triggerNaturalMatchEndInterstitial(activity: Activity? = null) {
        if (activity != null) {
            triggerNaturalBreakInterstitial(activity) {}
        }
    }

    fun closeAd() {
        _activeAdSession.value = null
    }

    fun buyCardSkin(skin: CardSkin) {
        val current = _playerEconomy.value
        if (current.diamonds >= skin.priceDiamonds) {
            soundManager.playPurchaseSuccess()
            updateEconomy {
                it.copy(
                    diamonds = it.diamonds - skin.priceDiamonds,
                    unlockedCardSkins = it.unlockedCardSkins + skin.id,
                    equippedCardSkin = skin.id
                )
            }
        }
    }

    fun buyCardSkinWithCoins(skin: CardSkin): Boolean {
        val current = _playerEconomy.value
        if (current.coins >= skin.priceCoins) {
            soundManager.playPurchaseSuccess()
            updateEconomy {
                it.copy(
                    coins = it.coins - skin.priceCoins,
                    unlockedCardSkins = it.unlockedCardSkins + skin.id,
                    equippedCardSkin = skin.id
                )
            }
            return true
        }
        return false
    }

    fun equipCardSkin(skin: CardSkin) {
        soundManager.playEquipSound()
        updateEconomy { it.copy(equippedCardSkin = skin.id) }
    }

    fun gainCoins(amount: Long) {
        updateEconomy { it.copy(coins = it.coins + amount) }
    }

    fun gainDiamonds(amount: Int) {
        updateEconomy { it.copy(diamonds = it.diamonds + amount) }
    }

    fun buyTableTheme(theme: TableTheme) {
        val current = _playerEconomy.value
        if (current.diamonds >= theme.priceDiamonds) {
            soundManager.playPurchaseSuccess()
            updateEconomy {
                it.copy(
                    diamonds = it.diamonds - theme.priceDiamonds,
                    unlockedTableThemes = it.unlockedTableThemes + theme.id,
                    equippedTableTheme = theme.id
                )
            }
            when (theme) {
                TableTheme.EMERALD_CASINO -> setFeltTheme(FeltTheme.EMERALD_CASINO)
                TableTheme.ROYAL_SAPPHIRE -> setFeltTheme(FeltTheme.ROYAL_SAPPHIRE)
                TableTheme.MIDNIGHT_VELVET -> setFeltTheme(FeltTheme.ONYX_NOIR)
                TableTheme.CRIMSON_GOLD -> setFeltTheme(FeltTheme.CRIMSON_LOUNGE)
                TableTheme.MUGHAL_COURT -> setFeltTheme(FeltTheme.MUGHAL_DARBAR)
                TableTheme.NEON_CYBER -> setFeltTheme(FeltTheme.NEON_CYBER)
                TableTheme.ROYAL_AMETHYST -> setFeltTheme(FeltTheme.ROYAL_AMETHYST)
            }
        }
    }

    fun buyTableThemeWithCoins(theme: TableTheme): Boolean {
        val current = _playerEconomy.value
        if (current.coins >= theme.priceCoins) {
            soundManager.playPurchaseSuccess()
            updateEconomy {
                it.copy(
                    coins = it.coins - theme.priceCoins,
                    unlockedTableThemes = it.unlockedTableThemes + theme.id,
                    equippedTableTheme = theme.id
                )
            }
            when (theme) {
                TableTheme.EMERALD_CASINO -> setFeltTheme(FeltTheme.EMERALD_CASINO)
                TableTheme.ROYAL_SAPPHIRE -> setFeltTheme(FeltTheme.ROYAL_SAPPHIRE)
                TableTheme.MIDNIGHT_VELVET -> setFeltTheme(FeltTheme.ONYX_NOIR)
                TableTheme.CRIMSON_GOLD -> setFeltTheme(FeltTheme.CRIMSON_LOUNGE)
                TableTheme.MUGHAL_COURT -> setFeltTheme(FeltTheme.MUGHAL_DARBAR)
                TableTheme.NEON_CYBER -> setFeltTheme(FeltTheme.NEON_CYBER)
                TableTheme.ROYAL_AMETHYST -> setFeltTheme(FeltTheme.ROYAL_AMETHYST)
            }
            return true
        }
        return false
    }

    fun equipTableTheme(theme: TableTheme) {
        soundManager.playEquipSound()
        updateEconomy { it.copy(equippedTableTheme = theme.id) }
        when (theme) {
            TableTheme.EMERALD_CASINO -> setFeltTheme(FeltTheme.EMERALD_CASINO)
            TableTheme.ROYAL_SAPPHIRE -> setFeltTheme(FeltTheme.ROYAL_SAPPHIRE)
            TableTheme.MIDNIGHT_VELVET -> setFeltTheme(FeltTheme.ONYX_NOIR)
            TableTheme.CRIMSON_GOLD -> setFeltTheme(FeltTheme.CRIMSON_LOUNGE)
            TableTheme.MUGHAL_COURT -> setFeltTheme(FeltTheme.MUGHAL_DARBAR)
            TableTheme.NEON_CYBER -> setFeltTheme(FeltTheme.NEON_CYBER)
            TableTheme.ROYAL_AMETHYST -> setFeltTheme(FeltTheme.ROYAL_AMETHYST)
        }
    }

    fun exchangeDiamondsForCoins(diamonds: Int) {
        val current = _playerEconomy.value
        if (current.diamonds >= diamonds) {
            soundManager.playCoinCollect()
            val coinsGained = (diamonds * 1500L)
            updateEconomy {
                it.copy(
                    diamonds = it.diamonds - diamonds,
                    coins = it.coins + coinsGained
                )
            }
        }
    }

    fun claimQuestReward(questId: String) {
        val current = _playerEconomy.value
        val quest = current.quests.find { it.id == questId } ?: return
        if (quest.isCompleted && !quest.isClaimed) {
            soundManager.playDiamondCollect()
            val updatedQuests = current.quests.map {
                if (it.id == questId) it.copy(isClaimed = true) else it
            }
            updateEconomy {
                it.copy(
                    diamonds = it.diamonds + quest.diamondReward,
                    coins = it.coins + quest.coinReward,
                    quests = updatedQuests
                )
            }
            gainXp(quest.xpReward)
        }
    }

    fun incrementQuest(questId: String, amount: Int = 1) {
        updateEconomy { econ ->
            val updated = econ.quests.map { q ->
                if (q.id == questId && !q.isClaimed) {
                    q.copy(current = (q.current + amount).coerceAtMost(q.target))
                } else q
            }
            econ.copy(quests = updated)
        }
    }

    fun gainXp(amount: Int) {
        val current = _playerEconomy.value
        var newXp = current.currentXp + amount
        var newLevel = current.level
        val xpNeeded = current.xpForNextLevel

        if (newXp >= xpNeeded) {
            newXp -= xpNeeded
            newLevel += 1
            soundManager.playLevelUp()
            _pendingLevelUp.value = newLevel
            val diamondBonus = 1
            val coinBonus = 500L + (newLevel * 250L)
            updateEconomy {
                it.copy(
                    level = newLevel,
                    currentXp = newXp,
                    diamonds = it.diamonds + diamondBonus,
                    coins = it.coins + coinBonus
                )
            }
        } else {
            updateEconomy { it.copy(currentXp = newXp) }
        }
    }

    fun dismissLevelUp() {
        soundManager.playButtonClick()
        _pendingLevelUp.value = null
    }

    fun toggleBackgroundMusic(enabled: Boolean) {
        _isMusicEnabled.value = enabled
        soundManager.isMusicEnabled = enabled
        viewModelScope.launch {
            gamePreferencesDataStore.setMusicEnabled(enabled)
        }
    }

    fun openSocialShareModal() {
        soundManager.playNavTabClick()
        _showSocialShareModal.value = true
    }

    fun closeSocialShareModal() {
        soundManager.playCancelClick()
        _showSocialShareModal.value = false
    }

    fun linkSocialAccount(platform: SocialPlatform, handleOrUrl: String) {
        val current = _linkedSocialAccounts.value
        val existing = current.find { it.platform == platform }
        val isFirstTime = existing == null
        val updated = current.filter { it.platform != platform } + LinkedSocialAccount(
            platform = platform,
            handleOrUrl = handleOrUrl,
            totalSharesCount = existing?.totalSharesCount ?: 0,
            totalDiamondsEarned = (existing?.totalDiamondsEarned ?: 0) + if (isFirstTime) 15 else 0
        )
        _linkedSocialAccounts.value = updated
        saveLinkedSocialAccounts(updated)
        if (isFirstTime) {
            gainDiamonds(15)
            soundManager.playDiamondShine()
            soundManager.playPositiveAction()
        }
    }

    fun recordSocialShare(platform: SocialPlatform): Boolean {
        val today = getTodayDateString()
        val lastShareDate = prefs.getString("last_share_reward_date", "") ?: ""
        val alreadyClaimedToday = (lastShareDate == today)

        val current = _linkedSocialAccounts.value
        val existing = current.find { it.platform == platform }
        val reward = if (!alreadyClaimedToday) 1 else 0
        
        val updated = current.filter { it.platform != platform } + LinkedSocialAccount(
            platform = platform,
            handleOrUrl = existing?.handleOrUrl ?: "@player",
            totalSharesCount = (existing?.totalSharesCount ?: 0) + 1,
            totalDiamondsEarned = (existing?.totalDiamondsEarned ?: 0) + reward
        )
        _linkedSocialAccounts.value = updated
        saveLinkedSocialAccounts(updated)

        if (!alreadyClaimedToday) {
            prefs.edit().putString("last_share_reward_date", today).apply()
            gainDiamonds(1)
            soundManager.playDiamondShine()
            soundManager.playSocialShareChime()
            return true
        } else {
            soundManager.playButtonClick()
            return false
        }
    }

    fun hasClaimedDailyShareToday(): Boolean {
        val today = getTodayDateString()
        val lastShareDate = prefs.getString("last_share_reward_date", "") ?: ""
        return lastShareDate == today
    }

    val userReferralCode: String by lazy {
        val existing = prefs.getString("unique_player_seed", null)
        if (existing != null) {
            existing
        } else {
            val generated = "ROYAL29-" + (1000..9999).random()
            prefs.edit().putString("unique_player_seed", generated).apply()
            generated
        }
    }

    private val _referralCount = MutableStateFlow(prefs.getInt("referral_friends_joined", 0))
    val referralCount: StateFlow<Int> = _referralCount.asStateFlow()

    fun redeemReferralCode(rawCode: String): Pair<Boolean, String> {
        val code = rawCode.trim().uppercase()
        if (code.isBlank()) return Pair(false, "Please enter a valid referral code.")
        if (code == userReferralCode) return Pair(false, "You cannot redeem your own referral code.")
        if (prefs.getBoolean("has_redeemed_referral_code", false)) {
            return Pair(false, "You have already claimed an invite bonus code.")
        }
        prefs.edit().putBoolean("has_redeemed_referral_code", true).apply()
        gainCoins(5000)
        gainDiamonds(5)
        soundManager.playDiamondShine()
        soundManager.playSocialShareChime()
        return Pair(true, "Referral code activated! +5,000 Coins & +5 Diamonds awarded!")
    }

    fun recordReferralInstall(): Boolean {
        val newCount = _referralCount.value + 1
        _referralCount.value = newCount
        prefs.edit().putInt("referral_friends_joined", newCount).apply()
        gainCoins(10000)
        gainDiamonds(5)
        soundManager.playDiamondShine()
        soundManager.playSocialShareChime()
        return true
    }

    private fun loadLinkedSocialAccounts(): List<LinkedSocialAccount> {
        val jsonStr = prefs.getString("linked_social_accounts_json", null) ?: return emptyList()
        return try {
            val array = org.json.JSONArray(jsonStr)
            val list = mutableListOf<LinkedSocialAccount>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val platformName = obj.optString("platform", "")
                val platform = SocialPlatform.values().find { it.name == platformName } ?: continue
                list.add(
                    LinkedSocialAccount(
                        platform = platform,
                        handleOrUrl = obj.optString("handleOrUrl", ""),
                        linkedTimestamp = obj.optLong("linkedTimestamp", System.currentTimeMillis()),
                        totalSharesCount = obj.optInt("totalSharesCount", 0),
                        totalDiamondsEarned = obj.optInt("totalDiamondsEarned", 0)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveLinkedSocialAccounts(accounts: List<LinkedSocialAccount>) {
        try {
            val array = org.json.JSONArray()
            for (acc in accounts) {
                val obj = org.json.JSONObject()
                obj.put("platform", acc.platform.name)
                obj.put("handleOrUrl", acc.handleOrUrl)
                obj.put("linkedTimestamp", acc.linkedTimestamp)
                obj.put("totalSharesCount", acc.totalSharesCount)
                obj.put("totalDiamondsEarned", acc.totalDiamondsEarned)
                array.put(obj)
            }
            prefs.edit().putString("linked_social_accounts_json", array.toString()).apply()
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
        performanceManager.cleanUp()
    }
}

private fun createInitialGameState(): GameState {
    val initialPlayers = mapOf(
        PlayerPosition.SOUTH to Player(PlayerPosition.SOUTH, "You", false),
        PlayerPosition.WEST to Player(PlayerPosition.WEST, "Rohan", true),
        PlayerPosition.NORTH to Player(PlayerPosition.NORTH, "Vikram (Partner)", true),
        PlayerPosition.EAST to Player(PlayerPosition.EAST, "Aarav", true)
    )
    return GameState(
        phase = GamePhase.IDLE,
        players = initialPlayers
    )
}
