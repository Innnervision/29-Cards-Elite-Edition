package com.example.game.save

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.game.model.*
import org.json.JSONArray
import org.json.JSONObject

class GameSaveManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("twenty_nine_game_saves", Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "GameSaveManager"
        private const val PREFIX = "saved_match_"
        const val CURRENT_SAVE_VERSION = 2
        private const val KEY_PROFILE = "user_profile_data"
    }

    fun saveUserProfile(profile: UserProfile) {
        try {
            val root = JSONObject().apply {
                put("playerId", profile.playerId)
                put("username", profile.username)
                put("displayName", profile.displayName)
                put("bio", profile.bio)
                put("avatarId", profile.avatarId)
                put("joinedDate", profile.joinedDate)
                put("privacy", profile.privacy.name)
                put("gamesPlayed", profile.gamesPlayed)
                put("gamesWon", profile.gamesWon)
                put("gamesLost", profile.gamesLost)
                put("winStreak", profile.winStreak)
                put("bestWinStreak", profile.bestWinStreak)
                put("total29TrickPoints", profile.total29TrickPoints)
                put("royaltyPairsDeclared", profile.royaltyPairsDeclared)
                put("singleHandVictories", profile.singleHandVictories)
                put("tournamentsWon", profile.tournamentsWon)

                val achArr = JSONArray()
                profile.achievements.forEach { ach ->
                    achArr.put(JSONObject().apply {
                        put("id", ach.id)
                        put("currentProgress", ach.currentProgress)
                    })
                }
                put("achievements", achArr)
            }
            prefs.edit().putString(KEY_PROFILE, root.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed saving user profile: ${e.message}")
        }
    }

    fun loadUserProfile(): UserProfile {
        val jsonStr = prefs.getString(KEY_PROFILE, null) ?: return UserProfile()
        return try {
            val root = JSONObject(jsonStr)
            val defaultAchievements = UserProfile.createDefaultAchievements().associateBy { it.id }.toMutableMap()
            val achArr = root.optJSONArray("achievements")
            if (achArr != null) {
                for (i in 0 until achArr.length()) {
                    val aObj = achArr.getJSONObject(i)
                    val id = aObj.getString("id")
                    val progress = aObj.getInt("currentProgress")
                    defaultAchievements[id]?.let {
                        defaultAchievements[id] = it.copy(
                            currentProgress = progress,
                            isUnlocked = progress >= it.targetProgress
                        )
                    }
                }
            }

            UserProfile(
                playerId = root.optString("playerId", "P29-100000"),
                username = root.optString("username", "RoyalAce"),
                displayName = root.optString("displayName", "Grandmaster Player"),
                bio = root.optString("bio", "Mastering the 28-pip trick-taking strategy in 29 Royale."),
                avatarId = root.optString("avatarId", "avatar_player"),
                joinedDate = root.optString("joinedDate", "September 2026"),
                privacy = ProfilePrivacy.valueOf(root.optString("privacy", ProfilePrivacy.PUBLIC.name)),
                gamesPlayed = root.optInt("gamesPlayed", 18),
                gamesWon = root.optInt("gamesWon", 14),
                gamesLost = root.optInt("gamesLost", 4),
                winStreak = root.optInt("winStreak", 3),
                bestWinStreak = root.optInt("bestWinStreak", 7),
                total29TrickPoints = root.optLong("total29TrickPoints", 342L),
                royaltyPairsDeclared = root.optInt("royaltyPairsDeclared", 8),
                singleHandVictories = root.optInt("singleHandVictories", 2),
                tournamentsWon = root.optInt("tournamentsWon", 1),
                achievements = defaultAchievements.values.toList()
            )
        } catch (e: Exception) {
            UserProfile()
        }
    }

    private fun keyForMode(mode: MatchMode): String = "$PREFIX${mode.name}"

    fun hasSavedMatch(mode: MatchMode): Boolean {
        val key = keyForMode(mode)
        val jsonStr = prefs.getString(key, null) ?: return false
        return try {
            val json = JSONObject(jsonStr)
            val phaseStr = json.optString("phase", "")
            // Only valid if game is active, not IDLE, DEAL_PHASE_1, or MATCH_OVER
            phaseStr != GamePhase.IDLE.name && phaseStr != GamePhase.DEAL_PHASE_1.name && phaseStr != GamePhase.MATCH_OVER.name
        } catch (e: Exception) {
            false
        }
    }

    fun clearSavedMatch(mode: MatchMode) {
        prefs.edit().remove(keyForMode(mode)).apply()
    }

    fun clearAllSavedMatches() {
        prefs.edit().clear().apply()
    }

    fun saveMatch(
        mode: MatchMode,
        state: GameState,
        phase2Cards: Map<PlayerPosition, List<Card>>
    ) {
        if (state.phase == GamePhase.IDLE || state.phase == GamePhase.DEAL_PHASE_1 || state.phase == GamePhase.MATCH_OVER) {
            clearSavedMatch(mode)
            return
        }

        try {
            val root = JSONObject()
            root.put("matchMode", mode.name)
            root.put("phase", state.phase.name)
            root.put("dealer", state.dealer.name)
            root.put("activeTurn", state.activeTurn.name)
            root.put("currentHighestBid", state.currentHighestBid)
            root.put("minBid", state.minBid)
            root.put("isDoubled", state.isDoubled)
            root.put("doubledBy", state.doubledBy?.name ?: "")
            root.put("isRedoubled", state.isRedoubled)
            root.put("isSingleHand", state.isSingleHand)
            root.put("consecutivePasses", state.consecutivePasses)
            root.put("highestBidder", state.highestBidder?.name ?: "")
            root.put("trumpSuit", state.trumpSuit?.name ?: "")
            root.put("isTrumpRevealed", state.isTrumpRevealed)
            root.put("trumpRevealedBy", state.trumpRevealedBy?.name ?: "")
            root.put("trumpRevealedInTrickIndex", state.trumpRevealedInTrickIndex ?: -1)
            root.put("pairDeclaredByTeam", state.pairDeclaredByTeam?.name ?: "")
            root.put("pairSuit", state.pairSuit?.name ?: "")
            root.put("currentTrickIndex", state.currentTrickIndex)
            root.put("lastTrickWinner", state.lastTrickWinner?.name ?: "")
            root.put("lastEventMessage", state.lastEventMessage)
            root.put("matchRoundNumber", state.matchRoundNumber)
            root.put("targetPointsTeamA", state.targetPointsTeamA)
            root.put("targetPointsTeamB", state.targetPointsTeamB)

            // Trump card
            state.trumpCard?.let {
                root.put("trumpCard", "${it.suit.name}|${it.rank.name}")
            }

            // Players
            val playersObj = JSONObject()
            state.players.forEach { (pos, player) ->
                val playerJson = JSONObject()
                playerJson.put("name", player.name)
                playerJson.put("isAi", player.isAi)
                playerJson.put("avatarRes", player.avatarRes)
                val cardsArr = JSONArray()
                player.cards.forEach { card ->
                    cardsArr.put("${card.suit.name}|${card.rank.name}")
                }
                playerJson.put("cards", cardsArr)
                playersObj.put(pos.name, playerJson)
            }
            root.put("players", playersObj)

            // Phase 2 Dealt Cards (remaining 4 cards if dealt in phase 1)
            val p2Obj = JSONObject()
            phase2Cards.forEach { (pos, cards) ->
                val p2CardsArr = JSONArray()
                cards.forEach { card ->
                    p2CardsArr.put("${card.suit.name}|${card.rank.name}")
                }
                p2Obj.put(pos.name, p2CardsArr)
            }
            root.put("phase2Cards", p2Obj)

            // Bid History
            val bidsArr = JSONArray()
            state.bidHistory.forEach { bid ->
                val bJson = JSONObject()
                bJson.put("player", bid.player.name)
                bJson.put("bidAmount", bid.bidAmount)
                bJson.put("isPass", bid.isPass)
                bJson.put("isDouble", bid.isDouble)
                bJson.put("isRedouble", bid.isRedouble)
                bidsArr.put(bJson)
            }
            root.put("bidHistory", bidsArr)

            // Scores
            val teamAJson = JSONObject().apply {
                put("cardPoints", state.teamAScore.cardPoints)
                put("gamePoints", state.teamAScore.gamePoints)
                put("tricksWon", state.teamAScore.tricksWon)
                put("pairDeclared", state.teamAScore.pairDeclared)
            }
            root.put("teamAScore", teamAJson)

            val teamBJson = JSONObject().apply {
                put("cardPoints", state.teamBScore.cardPoints)
                put("gamePoints", state.teamBScore.gamePoints)
                put("tricksWon", state.teamBScore.tricksWon)
                put("pairDeclared", state.teamBScore.pairDeclared)
            }
            root.put("teamBScore", teamBJson)

            // House rules
            val rulesJson = JSONObject().apply {
                put("targetPipsToWin", state.houseRules.targetPipsToWin)
                put("enableSeventhCardRule", state.houseRules.enableSeventhCardRule)
                put("pairValue", state.houseRules.pairValue)
                put("enableDoubleRedouble", state.houseRules.enableDoubleRedouble)
                put("aiSpeedMs", state.houseRules.aiSpeedMs)
                put("aiDifficulty", state.houseRules.aiDifficulty.name)
            }
            root.put("houseRules", rulesJson)
            root.put("saveVersion", CURRENT_SAVE_VERSION)

            // Current Trick
            val curTrickJson = serializeTrick(state.currentTrick)
            root.put("currentTrick", curTrickJson)

            // Completed Tricks
            val compTricksArr = JSONArray()
            state.completedTricks.forEach { trick ->
                compTricksArr.put(serializeTrick(trick))
            }
            root.put("completedTricks", compTricksArr)

            prefs.edit().putString(keyForMode(mode), root.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save match for mode $mode: ${e.message}", e)
        }
    }

    fun loadMatch(mode: MatchMode): Pair<GameState, Map<PlayerPosition, List<Card>>>? {
        val key = keyForMode(mode)
        val jsonStr = prefs.getString(key, null) ?: return null
        return try {
            val root = JSONObject(jsonStr)
            val phase = GamePhase.valueOf(root.getString("phase"))
            if (phase == GamePhase.IDLE || phase == GamePhase.DEAL_PHASE_1 || phase == GamePhase.MATCH_OVER) {
                return null
            }

            val dealer = PlayerPosition.valueOf(root.getString("dealer"))
            val activeTurn = PlayerPosition.valueOf(root.getString("activeTurn"))
            val currentHighestBid = root.optInt("currentHighestBid", 16)
            val minBid = root.optInt("minBid", 16)
            val isDoubled = root.optBoolean("isDoubled", false)
            val doubledByStr = root.optString("doubledBy", "")
            val doubledBy = if (doubledByStr.isNotEmpty()) PlayerPosition.valueOf(doubledByStr) else null
            val isRedoubled = root.optBoolean("isRedoubled", false)
            val isSingleHand = root.optBoolean("isSingleHand", false)
            val consecutivePasses = root.optInt("consecutivePasses", 0)

            val highestBidderStr = root.optString("highestBidder", "")
            val highestBidder = if (highestBidderStr.isNotEmpty()) PlayerPosition.valueOf(highestBidderStr) else null

            val trumpSuitStr = root.optString("trumpSuit", "")
            val trumpSuit = if (trumpSuitStr.isNotEmpty()) Suit.valueOf(trumpSuitStr) else null
            val isTrumpRevealed = root.optBoolean("isTrumpRevealed", false)

            val trumpRevealedByStr = root.optString("trumpRevealedBy", "")
            val trumpRevealedBy = if (trumpRevealedByStr.isNotEmpty()) PlayerPosition.valueOf(trumpRevealedByStr) else null

            val trickIdx = root.optInt("trumpRevealedInTrickIndex", -1)
            val trumpRevealedInTrickIndex = if (trickIdx >= 0) trickIdx else null

            val pairDeclaredByTeamStr = root.optString("pairDeclaredByTeam", "")
            val pairDeclaredByTeam = if (pairDeclaredByTeamStr.isNotEmpty()) Team.valueOf(pairDeclaredByTeamStr) else null

            val pairSuitStr = root.optString("pairSuit", "")
            val pairSuit = if (pairSuitStr.isNotEmpty()) Suit.valueOf(pairSuitStr) else null

            val currentTrickIndex = root.optInt("currentTrickIndex", 0)
            val lastTrickWinnerStr = root.optString("lastTrickWinner", "")
            val lastTrickWinner = if (lastTrickWinnerStr.isNotEmpty()) PlayerPosition.valueOf(lastTrickWinnerStr) else null

            val lastEventMessage = root.optString("lastEventMessage", "Game resumed")
            val matchRoundNumber = root.optInt("matchRoundNumber", 1)
            val targetPointsTeamA = root.optInt("targetPointsTeamA", 0)
            val targetPointsTeamB = root.optInt("targetPointsTeamB", 0)

            val trumpCardStr = root.optString("trumpCard", "")
            val trumpCard = parseCard(trumpCardStr)

            // Deserialize Players
            val players = mutableMapOf<PlayerPosition, Player>()
            val playersObj = root.optJSONObject("players")
            if (playersObj != null) {
                PlayerPosition.values().forEach { pos ->
                    val pJson = playersObj.optJSONObject(pos.name)
                    if (pJson != null) {
                        val name = pJson.optString("name", pos.displayName)
                        val isAi = pJson.optBoolean("isAi", !pos.isUser)
                        val avatar = pJson.optString("avatarRes", "avatar_${pos.name.lowercase()}")
                        val cardsArr = pJson.optJSONArray("cards")
                        val cards = mutableListOf<Card>()
                        if (cardsArr != null) {
                            for (i in 0 until cardsArr.length()) {
                                parseCard(cardsArr.getString(i))?.let { cards.add(it) }
                            }
                        }
                        players[pos] = Player(
                            position = pos,
                            name = name,
                            isAi = isAi,
                            avatarRes = avatar,
                            cards = cards
                        )
                    }
                }
            }

            // Phase 2 Cards
            val phase2Cards = mutableMapOf<PlayerPosition, List<Card>>()
            val p2Obj = root.optJSONObject("phase2Cards")
            if (p2Obj != null) {
                PlayerPosition.values().forEach { pos ->
                    val cardsArr = p2Obj.optJSONArray(pos.name)
                    if (cardsArr != null) {
                        val cList = mutableListOf<Card>()
                        for (i in 0 until cardsArr.length()) {
                            parseCard(cardsArr.getString(i))?.let { cList.add(it) }
                        }
                        phase2Cards[pos] = cList
                    }
                }
            }

            // Bid history
            val bidHistory = mutableListOf<BidRecord>()
            val bidsArr = root.optJSONArray("bidHistory")
            if (bidsArr != null) {
                for (i in 0 until bidsArr.length()) {
                    val b = bidsArr.getJSONObject(i)
                    bidHistory.add(
                        BidRecord(
                            player = PlayerPosition.valueOf(b.getString("player")),
                            bidAmount = b.getInt("bidAmount"),
                            isPass = b.optBoolean("isPass", false),
                            isDouble = b.optBoolean("isDouble", false),
                            isRedouble = b.optBoolean("isRedouble", false)
                        )
                    )
                }
            }

            // Team Scores
            val aJson = root.optJSONObject("teamAScore")
            val teamAScore = TeamScore(
                team = Team.TEAM_A,
                cardPoints = aJson?.optInt("cardPoints", 0) ?: 0,
                gamePoints = aJson?.optInt("gamePoints", 0) ?: 0,
                tricksWon = aJson?.optInt("tricksWon", 0) ?: 0,
                pairDeclared = aJson?.optBoolean("pairDeclared", false) ?: false
            )

            val bJson = root.optJSONObject("teamBScore")
            val teamBScore = TeamScore(
                team = Team.TEAM_B,
                cardPoints = bJson?.optInt("cardPoints", 0) ?: 0,
                gamePoints = bJson?.optInt("gamePoints", 0) ?: 0,
                tricksWon = bJson?.optInt("tricksWon", 0) ?: 0,
                pairDeclared = bJson?.optBoolean("pairDeclared", false) ?: false
            )

            // House Rules
            val rJson = root.optJSONObject("houseRules")
            val diffStr = rJson?.optString("aiDifficulty", AiDifficulty.ADAPTIVE.name) ?: AiDifficulty.ADAPTIVE.name
            val diff = try { AiDifficulty.valueOf(diffStr) } catch (_: Exception) { AiDifficulty.ADAPTIVE }
            val houseRules = HouseRules(
                targetPipsToWin = rJson?.optInt("targetPipsToWin", 6) ?: 6,
                enableSeventhCardRule = rJson?.optBoolean("enableSeventhCardRule", false) ?: false,
                pairValue = rJson?.optInt("pairValue", 4) ?: 4,
                enableDoubleRedouble = rJson?.optBoolean("enableDoubleRedouble", true) ?: true,
                aiSpeedMs = rJson?.optLong("aiSpeedMs", 800L) ?: 800L,
                aiDifficulty = diff
            )

            // Current Trick
            val curTrick = deserializeTrick(root.optJSONObject("currentTrick")) ?: Trick(0, activeTurn)

            // Completed Tricks
            val completedTricks = mutableListOf<Trick>()
            val compArr = root.optJSONArray("completedTricks")
            if (compArr != null) {
                for (i in 0 until compArr.length()) {
                    deserializeTrick(compArr.getJSONObject(i))?.let { completedTricks.add(it) }
                }
            }

            val restoredState = GameState(
                phase = phase,
                matchMode = mode,
                dealer = dealer,
                activeTurn = activeTurn,
                players = players,
                bidHistory = bidHistory,
                currentHighestBid = currentHighestBid,
                highestBidder = highestBidder,
                isDoubled = isDoubled,
                doubledBy = doubledBy,
                isRedoubled = isRedoubled,
                isSingleHand = isSingleHand,
                consecutivePasses = consecutivePasses,
                minBid = minBid,
                trumpSuit = trumpSuit,
                isTrumpRevealed = isTrumpRevealed,
                trumpRevealedBy = trumpRevealedBy,
                trumpRevealedInTrickIndex = trumpRevealedInTrickIndex,
                trumpCard = trumpCard,
                pairDeclaredByTeam = pairDeclaredByTeam,
                pairSuit = pairSuit,
                currentTrickIndex = currentTrickIndex,
                currentTrick = curTrick,
                completedTricks = completedTricks,
                lastTrickWinner = lastTrickWinner,
                teamAScore = teamAScore,
                teamBScore = teamBScore,
                targetPointsTeamA = targetPointsTeamA,
                targetPointsTeamB = targetPointsTeamB,
                lastEventMessage = lastEventMessage,
                matchRoundNumber = matchRoundNumber,
                houseRules = houseRules
            )

            Pair(restoredState, phase2Cards)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load saved match for mode $mode: ${e.message}", e)
            null
        }
    }

    private fun serializeTrick(trick: Trick): JSONObject {
        val obj = JSONObject()
        obj.put("trickNumber", trick.trickNumber)
        obj.put("leadPlayer", trick.leadPlayer.name)
        obj.put("winner", trick.winner?.name ?: "")
        val playsArr = JSONArray()
        trick.plays.forEach { play ->
            val pObj = JSONObject()
            pObj.put("player", play.player.name)
            pObj.put("card", "${play.card.suit.name}|${play.card.rank.name}")
            playsArr.put(pObj)
        }
        obj.put("plays", playsArr)
        return obj
    }

    private fun deserializeTrick(obj: JSONObject?): Trick? {
        if (obj == null) return null
        return try {
            val num = obj.optInt("trickNumber", 0)
            val lead = PlayerPosition.valueOf(obj.getString("leadPlayer"))
            val winnerStr = obj.optString("winner", "")
            val winner = if (winnerStr.isNotEmpty()) PlayerPosition.valueOf(winnerStr) else null
            val playsArr = obj.optJSONArray("plays")
            val plays = mutableListOf<PlayedCard>()
            if (playsArr != null) {
                for (i in 0 until playsArr.length()) {
                    val pObj = playsArr.getJSONObject(i)
                    val pos = PlayerPosition.valueOf(pObj.getString("player"))
                    val card = parseCard(pObj.getString("card"))
                    if (card != null) {
                        plays.add(PlayedCard(pos, card))
                    }
                }
            }
            Trick(
                trickNumber = num,
                leadPlayer = lead,
                plays = plays,
                winner = winner
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseCard(str: String): Card? {
        if (!str.contains("|")) return null
        return try {
            val parts = str.split("|")
            val suit = Suit.valueOf(parts[0])
            val rank = Rank.valueOf(parts[1])
            Card(suit, rank)
        } catch (e: Exception) {
            null
        }
    }
}
