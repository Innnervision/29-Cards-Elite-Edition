package com.example.game.model

enum class GamePhase {
    IDLE,
    DEAL_PHASE_1,
    BIDDING,
    SET_TRUMP,
    DEAL_PHASE_2,
    PLAYING_TRICK,
    TRICK_RESOLVING,
    ROUND_SUMMARY,
    MATCH_OVER
}

enum class MatchMode(val title: String, val subtitle: String) {
    CLASSIC_29("Classic 29", "Traditional 4-player team match to 6 victory pips"),
    HIGH_ROLLER("High Roller VIP", "Min bid 20 • Double stakes & 3,000 Coin reward"),
    SPEED_BLITZ("Speed Blitz 60s", "Lightning-fast AI turns (350ms) • Sudden death 1-round clash"),
    ROYAL_DUEL("Royal Showdown", "Fast 4-pip match against elite Grandmasters"),
    SINGLE_HAND("Single Hand Solo", "Win all 8 tricks solo with target 29 without partner"),
    BLITZ_ROUND("Quick Blitz 1-Round", "Fast 1-round high-stakes sudden death match"),
    TOURNAMENT("Grand 29 Cup", "3-stage knockout championship against master clubs"),
    ADVENTURE("Adventures", "Campaign levels with tactical quests, bosses, and star ratings"),
    CUSTOM_RULES("House Rules", "Customizable targets, 7th-card rules, and AI speeds")
}

enum class AiDifficulty(val title: String, val description: String) {
    EASY("Easy", "Casual AI with gentle bidding and simplified trick responses"),
    NORMAL("Normal", "Balanced standard play for casual competitive games"),
    HARD("Hard", "Card counting, tactical trump cuts, and partnership awareness"),
    EXPERT("Expert", "Master tournament play with deep signaling and unblocking"),
    ADAPTIVE("Adaptive", "Dynamically tunes AI strategy based on your recent win streak")
}

data class HouseRules(
    val targetPipsToWin: Int = 6,
    val enableSeventhCardRule: Boolean = false,
    val pairValue: Int = 4, // 4 points standard or 5 points house rule
    val enableDoubleRedouble: Boolean = true,
    val aiSpeedMs: Long = 800L,
    val aiDifficulty: AiDifficulty = AiDifficulty.ADAPTIVE
)

enum class TournamentStage(val title: String, val opponentClub: String, val rewardCoins: Int, val stageNumber: Int) {
    QUARTER_FINALS("Quarter-Finals", "Kolkata Knights (Casual)", 250, 1),
    SEMI_FINALS("Semi-Finals", "Mumbai Royals (Pro)", 500, 2),
    GRAND_FINALS("Grand Championship", "Delhi Masters (Grandmasters)", 1500, 3),
    CHAMPION("Trophy Champion", "Grand 29 Cup Winner", 5000, 3)
}

data class TournamentProgress(
    val currentStage: TournamentStage = TournamentStage.QUARTER_FINALS,
    val matchesWon: Int = 0,
    val isTrophyUnlocked: Boolean = false
)

data class BidRecord(
    val player: PlayerPosition,
    val bidAmount: Int,
    val isPass: Boolean = false,
    val isDouble: Boolean = false,
    val isRedouble: Boolean = false
)

data class TeamScore(
    val team: Team,
    val cardPoints: Int = 0,
    val gamePoints: Int = 0, // Red tokens (positive) or Black tokens (negative)
    val tricksWon: Int = 0,
    val pairDeclared: Boolean = false
)

data class GameState(
    val phase: GamePhase = GamePhase.IDLE,
    val matchMode: MatchMode = MatchMode.CLASSIC_29,
    val dealer: PlayerPosition = PlayerPosition.EAST,
    val dealingTargetPlayer: PlayerPosition? = null,
    val activeTurn: PlayerPosition = PlayerPosition.SOUTH,
    val players: Map<PlayerPosition, Player> = emptyMap(),
    
    // Bidding
    val bidHistory: List<BidRecord> = emptyList(),
    val currentHighestBid: Int = 15,
    val highestBidder: PlayerPosition? = null,
    val isDoubled: Boolean = false,
    val doubledBy: PlayerPosition? = null,
    val isRedoubled: Boolean = false,
    val isSingleHand: Boolean = false,
    val consecutivePasses: Int = 0,
    val minBid: Int = 16,

    // Trump
    val trumpSuit: Suit? = null,
    val isTrumpRevealed: Boolean = false,
    val trumpRevealedBy: PlayerPosition? = null,
    val trumpRevealedInTrickIndex: Int? = null,
    val trumpCard: Card? = null, // The hidden card chosen

    // Pair / Royalty
    val pairDeclaredByTeam: Team? = null,
    val pairSuit: Suit? = null,

    // Tricks
    val currentTrickIndex: Int = 0,
    val currentTrick: Trick = Trick(0, PlayerPosition.SOUTH),
    val completedTricks: List<Trick> = emptyList(),
    val lastTrickWinner: PlayerPosition? = null,

    // Scores
    val teamAScore: TeamScore = TeamScore(Team.TEAM_A),
    val teamBScore: TeamScore = TeamScore(Team.TEAM_B),
    val targetPointsTeamA: Int = 0,
    val targetPointsTeamB: Int = 0,

    // Highlights & VFX trigger states
    val lastEventMessage: String = "Welcome to 29 Card Game!",
    val vfxTrigger: VfxEvent? = null,
    val aiMentorTip: String? = null,
    val isAiThinking: Boolean = false,
    val matchRoundNumber: Int = 1,
    val houseRules: HouseRules = HouseRules(),
    val tournamentProgress: TournamentProgress = TournamentProgress(),
    val adventureLevel: AdventureLevel? = null
)

enum class VfxType {
    CARD_DEAL,
    CARD_SLAM,
    TRUMP_REVEAL,
    TRICK_COLLECT,
    PAIR_ROYALTY,
    FIREWORKS_WIN,
    DEFEAT_GLOOM,
    HIGH_BID_ALERT
}

data class VfxEvent(
    val type: VfxType,
    val position: PlayerPosition? = null,
    val suit: Suit? = null,
    val message: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
