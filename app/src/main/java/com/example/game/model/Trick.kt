package com.example.game.model

data class PlayedCard(
    val player: PlayerPosition,
    val card: Card
)

data class Trick(
    val trickNumber: Int,
    val leadPlayer: PlayerPosition,
    val plays: List<PlayedCard> = emptyList(),
    val winner: PlayerPosition? = null
) {
    val isComplete: Boolean get() = plays.size == 4
    val ledSuit: Suit? get() = plays.firstOrNull()?.card?.suit

    val points: Int
        get() = plays.sumOf { it.card.points }

    fun findWinner(trumpSuit: Suit?, isTrumpRevealed: Boolean): PlayerPosition {
        if (plays.isEmpty()) return leadPlayer
        val firstPlay = plays.first()
        val firstSuit = firstPlay.card.suit

        var winningPlay = firstPlay

        for (i in 1 until plays.size) {
            val play = plays[i]
            val currentBest = winningPlay.card

            if (isTrumpRevealed && trumpSuit != null) {
                if (play.card.suit == trumpSuit) {
                    if (currentBest.suit != trumpSuit) {
                        winningPlay = play
                    } else if (play.card.power > currentBest.power) {
                        winningPlay = play
                    }
                } else if (currentBest.suit != trumpSuit && play.card.suit == firstSuit && play.card.power > currentBest.power) {
                    winningPlay = play
                }
            } else {
                if (play.card.suit == firstSuit && play.card.power > currentBest.power) {
                    winningPlay = play
                }
            }
        }
        return winningPlay.player
    }
}
