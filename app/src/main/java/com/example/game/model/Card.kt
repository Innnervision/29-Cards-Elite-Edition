package com.example.game.model

enum class Suit(val symbol: String, val displayName: String, val isRed: Boolean) {
    SPADES("♠", "Spades", false),
    HEARTS("♥", "Hearts", true),
    CLUBS("♣", "Clubs", false),
    DIAMONDS("♦", "Diamonds", true)
}

enum class Rank(val symbol: String, val displayName: String, val points: Int, val power: Int) {
    SEVEN("7", "7", 0, 1),
    EIGHT("8", "8", 0, 2),
    QUEEN("Q", "Queen", 0, 3),
    KING("K", "King", 0, 4),
    TEN("10", "10", 1, 5),
    ACE("A", "Ace", 1, 6),
    NINE("9", "9", 2, 7),
    JACK("J", "Jack", 3, 8)
}

data class Card(
    val suit: Suit,
    val rank: Rank,
    val id: String = "${rank.name}_${suit.name}"
) {
    val points: Int get() = rank.points
    val power: Int get() = rank.power

    val shortName: String get() = "${rank.symbol}${suit.symbol}"

    companion object {
        fun createDeck(): List<Card> {
            val deck = mutableListOf<Card>()
            for (suit in Suit.values()) {
                for (rank in Rank.values()) {
                    deck.add(Card(suit, rank))
                }
            }
            return deck
        }
    }
}
