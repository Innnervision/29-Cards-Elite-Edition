package com.example.game.engine

import com.example.game.model.*
import kotlin.random.Random

object TwentyNineAi {

    /**
     * AI determines whether to bid, raise, or pass during Phase 1 (4 cards).
     */
    fun evaluateBid(
        position: PlayerPosition,
        hand: List<Card>,
        currentBid: Int,
        highestBidder: PlayerPosition?,
        minBid: Int = 16,
        difficulty: AiDifficulty = AiDifficulty.NORMAL,
        playerWinStreak: Int = 0
    ): Int? {
        val effectiveDifficulty = resolveDifficulty(difficulty, playerWinStreak)

        val partner = if (position == PlayerPosition.WEST) PlayerPosition.EAST else PlayerPosition.NORTH
        
        // If partner already holds the highest bid, don't outbid them unless we have an extraordinary hand
        if (highestBidder == partner && currentBid >= 16) {
            val handScore = calculateHandScore(hand)
            val threshold = if (effectiveDifficulty == AiDifficulty.EXPERT) 18 else 16
            if (handScore < threshold) return null // Pass
        }

        val handScore = calculateHandScore(hand)

        val maxSafeBid = when (effectiveDifficulty) {
            AiDifficulty.EASY -> when {
                handScore >= 14 -> 18
                handScore >= 10 -> 17
                handScore >= 7 -> 16
                else -> 15
            }
            AiDifficulty.NORMAL -> when {
                handScore >= 16 -> 22
                handScore >= 13 -> 20
                handScore >= 10 -> 18
                handScore >= 8 -> 17
                handScore >= 6 -> 16
                else -> 15
            }
            AiDifficulty.HARD -> when {
                handScore >= 16 -> 23
                handScore >= 13 -> 21
                handScore >= 10 -> 19
                handScore >= 7 -> 18
                handScore >= 5 -> 17
                else -> 16
            }
            AiDifficulty.EXPERT, AiDifficulty.ADAPTIVE -> when {
                handScore >= 17 -> 25
                handScore >= 14 -> 23
                handScore >= 11 -> 21
                handScore >= 8 -> 19
                handScore >= 5 -> 17
                else -> 16
            }
        }

        val nextBid = if (currentBid < minBid) minBid else currentBid + 1

        return if (nextBid <= maxSafeBid && nextBid <= 28) {
            nextBid
        } else {
            null // Pass
        }
    }

    /**
     * AI selects the best Trump suit from first 4 cards.
     */
    fun selectTrumpSuit(
        hand: List<Card>,
        difficulty: AiDifficulty = AiDifficulty.NORMAL
    ): Suit {
        val suitScores = mutableMapOf<Suit, Int>()
        for (suit in Suit.values()) {
            val cardsInSuit = hand.filter { it.suit == suit }
            var score = cardsInSuit.size * 2
            if (cardsInSuit.any { it.rank == Rank.JACK }) score += 5
            if (cardsInSuit.any { it.rank == Rank.NINE }) score += 3
            if (cardsInSuit.any { it.rank == Rank.ACE }) score += 2
            if (cardsInSuit.any { it.rank == Rank.KING && cardsInSuit.any { c -> c.rank == Rank.QUEEN } }) score += 3
            suitScores[suit] = score
        }

        // On Easy difficulty, occasionally picks second-best suit for realism
        if (difficulty == AiDifficulty.EASY && suitScores.size > 1 && Random.nextFloat() < 0.25f) {
            val sorted = suitScores.toList().sortedByDescending { it.second }
            if (sorted.size >= 2) return sorted[1].first
        }

        return suitScores.maxByOrNull { it.value }?.key ?: hand.first().suit
    }

    /**
     * AI decides on playing a card and whether to request trump reveal.
     */
    fun chooseMove(
        position: PlayerPosition,
        hand: List<Card>,
        currentTrick: Trick,
        trumpSuit: Suit?,
        isTrumpRevealed: Boolean,
        hasDeclaredPair: Boolean,
        difficulty: AiDifficulty = AiDifficulty.NORMAL,
        playerWinStreak: Int = 0
    ): AiMoveDecision {
        val effectiveDifficulty = resolveDifficulty(difficulty, playerWinStreak)
        val ledSuit = currentTrick.ledSuit

        // Case 1: AI is leading the trick
        if (ledSuit == null) {
            val chosenCard = chooseLeadCard(hand, trumpSuit, isTrumpRevealed, effectiveDifficulty)
            return AiMoveDecision(card = chosenCard, requestTrumpReveal = false)
        }

        // Case 2: AI has cards in led suit -> Must follow suit
        val suitCards = hand.filter { it.suit == ledSuit }
        if (suitCards.isNotEmpty()) {
            val chosenCard = chooseFollowCard(suitCards, currentTrick, trumpSuit, isTrumpRevealed, position, effectiveDifficulty)
            return AiMoveDecision(card = chosenCard, requestTrumpReveal = false)
        }

        // Case 3: AI doesn't have led suit!
        // Should AI request trump reveal?
        if (!isTrumpRevealed && trumpSuit != null) {
            val hasTrumps = hand.any { it.suit == trumpSuit }
            val trickPoints = currentTrick.points
            val currentWinner = currentTrick.findWinner(trumpSuit, false)
            val isPartnerWinning = currentWinner.team == position.team

            val shouldReveal = when (effectiveDifficulty) {
                AiDifficulty.EASY -> hasTrumps && !isPartnerWinning && trickPoints >= 4
                AiDifficulty.NORMAL -> hasTrumps && (!isPartnerWinning || trickPoints >= 3)
                AiDifficulty.HARD, AiDifficulty.EXPERT, AiDifficulty.ADAPTIVE -> {
                    hasTrumps && (!isPartnerWinning || trickPoints >= 2 || hand.any { it.suit == trumpSuit && it.rank == Rank.JACK })
                }
            }

            if (shouldReveal) {
                val trumps = hand.filter { it.suit == trumpSuit }
                val chosenTrump = trumps.minByOrNull { it.power } ?: trumps.first()
                return AiMoveDecision(card = chosenTrump, requestTrumpReveal = true)
            }
        }

        // Trump is already revealed or AI cannot/doesn't want to reveal
        if (isTrumpRevealed && trumpSuit != null) {
            val trumps = hand.filter { it.suit == trumpSuit }
            val currentWinner = currentTrick.findWinner(trumpSuit, true)
            val isPartnerWinning = currentWinner.team == position.team

            if (trumps.isNotEmpty() && !isPartnerWinning) {
                val winningTrumps = trumps.filter { trump ->
                    val trickWithTrump = currentTrick.copy(plays = currentTrick.plays + PlayedCard(position, trump))
                    trickWithTrump.findWinner(trumpSuit, true) == position
                }
                if (winningTrumps.isNotEmpty()) {
                    val chosenTrump = winningTrumps.minByOrNull { it.power }!!
                    return AiMoveDecision(card = chosenTrump, requestTrumpReveal = false)
                } else if (effectiveDifficulty != AiDifficulty.EXPERT) {
                    // Discard lowest trump if cannot beat opponent trump (unless expert saves trumps)
                    return AiMoveDecision(card = trumps.minByOrNull { it.power }!!, requestTrumpReveal = false)
                }
            }
        }

        // Discard lowest value/junk card
        val nonPoints = hand.filter { it.points == 0 }
        val chosenCard = if (nonPoints.isNotEmpty()) {
            nonPoints.minByOrNull { it.power } ?: hand.minByOrNull { it.points }!!
        } else {
            hand.minByOrNull { it.points }!!
        }

        return AiMoveDecision(card = chosenCard, requestTrumpReveal = false)
    }

    private fun resolveDifficulty(difficulty: AiDifficulty, playerWinStreak: Int): AiDifficulty {
        if (difficulty != AiDifficulty.ADAPTIVE) return difficulty
        return when {
            playerWinStreak >= 3 -> AiDifficulty.EXPERT
            playerWinStreak in 1..2 -> AiDifficulty.HARD
            playerWinStreak <= -2 -> AiDifficulty.EASY
            else -> AiDifficulty.NORMAL
        }
    }

    private fun chooseLeadCard(
        hand: List<Card>,
        trumpSuit: Suit?,
        isTrumpRevealed: Boolean,
        difficulty: AiDifficulty
    ): Card {
        // 1. Lead Aces in non-trump suits (solid points winner)
        val nonTrumpAces = hand.filter { it.rank == Rank.ACE && it.suit != trumpSuit }
        if (nonTrumpAces.isNotEmpty()) return nonTrumpAces.random()

        // 2. If Trump revealed, lead Jack of Trumps to draw out opponent trumps if we hold 9 as well
        if (isTrumpRevealed && trumpSuit != null) {
            val jackTrump = hand.firstOrNull { it.suit == trumpSuit && it.rank == Rank.JACK }
            val nineTrump = hand.firstOrNull { it.suit == trumpSuit && it.rank == Rank.NINE }
            if (jackTrump != null && nineTrump != null) return jackTrump
        }

        // 3. Lead low card from suit with 2+ cards
        val suitGroups = hand.filter { it.suit != trumpSuit }.groupBy { it.suit }
        val longestGroup = suitGroups.maxByOrNull { it.value.size }
        if (longestGroup != null && longestGroup.value.size >= 2) {
            val lowCard = longestGroup.value.minByOrNull { it.power }
            if (lowCard != null) return lowCard
        }

        // 4. Default to lowest power card
        return hand.minByOrNull { it.power } ?: hand.first()
    }

    private fun chooseFollowCard(
        suitCards: List<Card>,
        currentTrick: Trick,
        trumpSuit: Suit?,
        isTrumpRevealed: Boolean,
        position: PlayerPosition,
        difficulty: AiDifficulty
    ): Card {
        val currentWinner = currentTrick.findWinner(trumpSuit, isTrumpRevealed)
        val isPartnerWinning = currentWinner.team == position.team

        if (isPartnerWinning) {
            // Partner is winning: smear maximum points (Jack, 9, Ace, 10) if partner's card is safe (like Jack or high trump)
            val winningCard = currentTrick.plays.firstOrNull { it.player == currentWinner }?.card
            val isHighWinner = winningCard?.rank == Rank.JACK || (winningCard?.suit == trumpSuit && isTrumpRevealed)
            if (isHighWinner || difficulty == AiDifficulty.EXPERT) {
                return suitCards.maxByOrNull { it.points } ?: suitCards.first()
            } else {
                // Partner's card might get overtaken, play low points
                return suitCards.filter { it.points == 0 }.minByOrNull { it.power } ?: suitCards.minByOrNull { it.points }!!
            }
        } else {
            // Opponent is winning: try to beat opponent with the lowest card that wins
            val winningCards = suitCards.filter { card ->
                val testTrick = currentTrick.copy(plays = currentTrick.plays + PlayedCard(position, card))
                testTrick.findWinner(trumpSuit, isTrumpRevealed) == position
            }
            if (winningCards.isNotEmpty()) {
                // Win with lowest power among winning cards
                return winningCards.minByOrNull { it.power }!!
            } else {
                // Cannot win: play lowest junk card to avoid throwing points
                return suitCards.filter { it.points == 0 }.minByOrNull { it.power } ?: suitCards.minByOrNull { it.points }!!
            }
        }
    }

    private fun calculateHandScore(hand: List<Card>): Int {
        var score = 0
        for (card in hand) {
            score += when (card.rank) {
                Rank.JACK -> 5
                Rank.NINE -> 3
                Rank.ACE -> 2
                Rank.TEN -> 1
                else -> 0
            }
        }
        val suitCounts = hand.groupBy { it.suit }
        for ((_, cards) in suitCounts) {
            if (cards.size >= 3) score += 3
            if (cards.any { it.rank == Rank.JACK } && cards.any { it.rank == Rank.NINE }) score += 4
        }
        return score
    }
}

data class AiMoveDecision(
    val card: Card,
    val requestTrumpReveal: Boolean
)
