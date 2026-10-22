package com.example.game.engine

import com.example.game.model.*

object TwentyNineEngine {

    const val MAX_GAME_POINTS = 6
    const val MIN_BID = 16
    const val MAX_BID = 28
    const val SINGLE_HAND_BID = 29

    /**
     * Determines legal cards a player can play for the current trick state.
     */
    fun getPlayableCards(
        hand: List<Card>,
        currentTrick: Trick,
        trumpSuit: Suit?,
        isTrumpRevealed: Boolean,
        hasRequestedTrumpRevealThisTurn: Boolean = false
    ): List<Card> {
        if (hand.isEmpty()) return emptyList()

        val ledSuit = currentTrick.ledSuit

        // If player is leading the trick:
        if (ledSuit == null) {
            return hand
        }

        // Must follow suit if player has the led suit
        val suitCards = hand.filter { it.suit == ledSuit }
        if (suitCards.isNotEmpty()) {
            return suitCards
        }

        // If player doesn't have led suit:
        if (hasRequestedTrumpRevealThisTurn && trumpSuit != null) {
            // Player just asked to reveal trump! Must play trump if they have it
            val trumps = hand.filter { it.suit == trumpSuit }
            if (trumps.isNotEmpty()) {
                return trumps
            }
        }

        // If trump is already revealed, or player has no trumps, any card in hand is playable
        return hand
    }

    /**
     * Checks if a player can request trump revelation.
     * Player can request reveal if trump is hidden, they are in a trick, and don't have the led suit.
     */
    fun canRequestTrumpReveal(
        hand: List<Card>,
        currentTrick: Trick,
        isTrumpRevealed: Boolean
    ): Boolean {
        if (isTrumpRevealed) return false
        val ledSuit = currentTrick.ledSuit ?: return false
        val hasLedSuit = hand.any { it.suit == ledSuit }
        return !hasLedSuit
    }

    /**
     * Checks if player can declare Pair/Royalty (King and Queen of Trump suit).
     */
    fun canDeclarePair(
        hand: List<Card>,
        trumpSuit: Suit?,
        isTrumpRevealed: Boolean,
        pairAlreadyDeclared: Boolean
    ): Boolean {
        if (!isTrumpRevealed || trumpSuit == null || pairAlreadyDeclared) return false
        val hasKing = hand.any { it.suit == trumpSuit && it.rank == Rank.KING }
        val hasQueen = hand.any { it.suit == trumpSuit && it.rank == Rank.QUEEN }
        return hasKing && hasQueen
    }

    /**
     * Evaluates a completed trick, returning the winning player and total points.
     */
    fun evaluateTrick(trick: Trick, trumpSuit: Suit?, isTrumpRevealed: Boolean): Pair<PlayerPosition, Int> {
        val winner = trick.findWinner(trumpSuit, isTrumpRevealed)
        val points = trick.points
        return Pair(winner, points)
    }

    /**
     * Shuffles and deals 32-card deck into 2 phases of 4 cards per player.
     */
    fun dealDeck(): Pair<Map<PlayerPosition, List<Card>>, Map<PlayerPosition, List<Card>>> {
        val deck = Card.createDeck().shuffled()
        
        val phase1 = mutableMapOf<PlayerPosition, List<Card>>()
        val phase2 = mutableMapOf<PlayerPosition, List<Card>>()

        // Deal 4 cards each
        var idx = 0
        for (pos in PlayerPosition.values()) {
            phase1[pos] = deck.subList(idx, idx + 4)
            idx += 4
        }
        for (pos in PlayerPosition.values()) {
            phase2[pos] = deck.subList(idx, idx + 4)
            idx += 4
        }

        return Pair(phase1, phase2)
    }

    /**
     * Calculates the game points delta at the end of a round.
     */
    fun calculateRoundScore(
        highestBidder: PlayerPosition,
        bidAmount: Int,
        teamAPoints: Int,
        teamBPoints: Int,
        isDoubled: Boolean,
        isRedoubled: Boolean,
        pairDeclaredBy: Team?
    ): RoundResult {
        val bidderTeam = highestBidder.team

        // Adjust target based on pair declaration
        var adjustedBid = bidAmount
        if (pairDeclaredBy != null) {
            if (pairDeclaredBy == bidderTeam) {
                adjustedBid = (adjustedBid - 4).coerceAtLeast(MIN_BID)
            } else {
                adjustedBid += 4
            }
        }

        val bidderScore = if (bidderTeam == Team.TEAM_A) teamAPoints else teamBPoints
        val isSuccess = bidderScore >= adjustedBid

        var baseGamePoints = if (bidAmount >= 21) 2 else 1
        if (isDoubled) baseGamePoints *= 2
        if (isRedoubled) baseGamePoints *= 2

        val teamAGamePointsDelta: Int
        val teamBGamePointsDelta: Int

        if (bidderTeam == Team.TEAM_A) {
            if (isSuccess) {
                teamAGamePointsDelta = baseGamePoints
                teamBGamePointsDelta = 0
            } else {
                teamAGamePointsDelta = -baseGamePoints
                teamBGamePointsDelta = 0
            }
        } else {
            if (isSuccess) {
                teamAGamePointsDelta = 0
                teamBGamePointsDelta = baseGamePoints
            } else {
                teamAGamePointsDelta = 0
                teamBGamePointsDelta = -baseGamePoints
            }
        }

        return RoundResult(
            bidder = highestBidder,
            bidderTeam = bidderTeam,
            originalBid = bidAmount,
            adjustedBid = adjustedBid,
            bidderScore = bidderScore,
            isSuccess = isSuccess,
            teamAGamePointsDelta = teamAGamePointsDelta,
            teamBGamePointsDelta = teamBGamePointsDelta,
            pairDeclaredBy = pairDeclaredBy
        )
    }
}

data class RoundResult(
    val bidder: PlayerPosition,
    val bidderTeam: Team,
    val originalBid: Int,
    val adjustedBid: Int,
    val bidderScore: Int,
    val isSuccess: Boolean,
    val teamAGamePointsDelta: Int,
    val teamBGamePointsDelta: Int,
    val pairDeclaredBy: Team?
)
