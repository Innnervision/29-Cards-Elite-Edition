package com.example.game.model

enum class PlayerPosition(val displayName: String, val index: Int) {
    SOUTH("You", 0),
    WEST("Rohan (AI)", 1),
    NORTH("Vikram (Partner)", 2),
    EAST("Aarav (AI)", 3);

    val next: PlayerPosition
        get() = values()[(ordinal + 1) % 4]

    val team: Team
        get() = if (this == SOUTH || this == NORTH) Team.TEAM_A else Team.TEAM_B

    val isUser: Boolean
        get() = this == SOUTH
}

enum class Team(val displayName: String, val shortName: String) {
    TEAM_A("Team You & Vikram", "Us"),
    TEAM_B("Team East & West", "Them");

    val opponent: Team
        get() = if (this == TEAM_A) TEAM_B else TEAM_A
}

data class Player(
    val position: PlayerPosition,
    val name: String,
    val isAi: Boolean,
    val avatarRes: String = "avatar_${position.name.lowercase()}",
    val cards: List<Card> = emptyList(),
    val isThinking: Boolean = false,
    val speechBubble: String? = null
)
