package com.example.game.ai

import com.example.BuildConfig
import com.example.game.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiCommentaryService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Generates dynamic AI commentary or strategic advice based on game state.
     */
    suspend fun getTacticalAdvice(
        hand: List<Card>,
        currentTrick: Trick,
        trumpSuit: Suit?,
        isTrumpRevealed: Boolean,
        highestBid: Int,
        highestBidder: PlayerPosition?
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getLocalFallbackAdvice(hand, currentTrick, trumpSuit, isTrumpRevealed, highestBid)
        }

        try {
            val handStr = hand.joinToString { it.shortName }
            val trickStr = currentTrick.plays.joinToString { "${it.player.name}: ${it.card.shortName}" }
            val prompt = """
                You are a grandmaster coach for the Indian 29 (Twenty-Nine) Card Game.
                Hand: $handStr
                Current Trick plays: [$trickStr]
                Trump Suit: ${trumpSuit?.displayName ?: "Hidden"} (Revealed: $isTrumpRevealed)
                Bid: $highestBid by ${highestBidder?.displayName ?: "None"}
                
                Give 1 punchy, expert tactical advice sentence (under 25 words) for South's turn.
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            if (response.isSuccessful && responseBody != null) {
                val json = JSONObject(responseBody)
                val candidates = json.optJSONArray("candidates")
                val text = candidates?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")
                if (!text.isNullOrBlank()) {
                    return@withContext text.trim()
                }
            }
        } catch (_: Exception) {}

        return@withContext getLocalFallbackAdvice(hand, currentTrick, trumpSuit, isTrumpRevealed, highestBid)
    }

    /**
     * Answers user questions about Indian 29 strategy, rules, and advanced tactics.
     */
    suspend fun askGrandmasterQuestion(question: String): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = """
                    You are Grandmaster Vikram, a world-class Indian 29 (Twenty-Nine) Card Game expert.
                    Question from student: "$question"
                    
                    Provide a concise, encouraging, and master-level answer (under 50 words) specific to Indian 29 card game rules/tactics.
                """.trimIndent()

                val jsonBody = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", prompt)
                                })
                            })
                        })
                    })
                }

                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    val json = JSONObject(responseBody)
                    val candidates = json.optJSONArray("candidates")
                    val text = candidates?.optJSONObject(0)
                        ?.optJSONObject("content")
                        ?.optJSONArray("parts")
                        ?.optJSONObject(0)
                        ?.optString("text")
                    if (!text.isNullOrBlank()) {
                        return@withContext text.trim()
                    }
                }
            } catch (_: Exception) {}
        }

        // Offline smart answer fallback based on keywords
        val lower = question.lowercase()
        return@withContext when {
            "bid" in lower -> "In 29, only bid 16+ if you hold a Jack (3 pts) or 9 (2 pts) in a strong suit, plus supporting Aces. Never bid high on empty suits!"
            "trump" in lower || "reveal" in lower -> "Keep trump hidden until heavy points (Jacks/Nines) hit the board or you have no suit to follow and need to cut to secure the trick."
            "pair" in lower || "royalty" in lower || "marriage" in lower -> "Holding King and Queen of the trump suit gives you Pair (Royalty). Declaring it alters the target by 4 points in your favor!"
            "points" in lower || "cards" in lower -> "Remember: Jacks are 3 points, Nines are 2 points, Aces and 10s are 1 point. Kings, Queens, 8s, 7s are 0 points. The 8th trick gives 1 bonus point!"
            "single" in lower -> "A Single Hand bid is a bid of 29 where you play all 8 tricks solo without partner help and must win every trick!"
            else -> "Always communicate with your partner through careful discards, bank your Aces early, and protect your Jacks until trump is revealed!"
        }
    }

    private fun getLocalFallbackAdvice(
        hand: List<Card>,
        currentTrick: Trick,
        trumpSuit: Suit?,
        isTrumpRevealed: Boolean,
        highestBid: Int
    ): String {
        val ledSuit = currentTrick.ledSuit
        if (ledSuit == null) {
            val hasAces = hand.any { it.rank == Rank.ACE && it.suit != trumpSuit }
            if (hasAces) return "Lead your off-suit Ace to safely bank the 1 point before opponents can cut!"
            return "Lead a low card from your longest suit to establish control."
        }

        val hasLedSuit = hand.any { it.suit == ledSuit }
        if (!hasLedSuit) {
            if (!isTrumpRevealed) {
                if (currentTrick.points >= 2) {
                    return "Heavy points in the middle! Tap 'Reveal Trump' to uncover and snatch this trick!"
                }
                return "You can reveal trump to cut, or discard a low non-point card."
            } else {
                return "Trump is active! Play a trump card to overpower this trick."
            }
        }

        val partnerWinning = currentTrick.findWinner(trumpSuit, isTrumpRevealed).team == Team.TEAM_A
        if (partnerWinning) {
            return "Vikram is currently winning this trick! Smear your Jack or 9 to load up our score."
        }
        return "Play the lowest winning card to beat the opponent without wasting high power."
    }
}
