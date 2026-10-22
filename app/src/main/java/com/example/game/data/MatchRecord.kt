package com.example.game.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "match_records")
data class MatchRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val matchMode: String = "Classic",
    val isWin: Boolean,
    val bidderTeam: String = "Team A",
    val bidderPlayer: String = "South (You)",
    val originalBid: Int = 16,
    val adjustedBid: Int = 16,
    val teamCardPoints: Int = 0,
    val opponentCardPoints: Int = 0,
    val gamePointsDelta: Int = 1,
    val trumpSuit: String = "Spades",
    val isSingleHand: Boolean = false,
    val isDoubled: Boolean = false,
    val isRedoubled: Boolean = false,
    val pairDeclared: Boolean = false,
    val mvpPlayer: String = "South (You)",
    val coinsEarned: Long = 750L,
    val xpEarned: Int = 150
)
