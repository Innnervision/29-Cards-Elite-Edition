package com.example.game.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchRecordDao {
    @Query("SELECT * FROM match_records ORDER BY timestamp DESC")
    fun getAllMatches(): Flow<List<MatchRecord>>

    @Query("SELECT * FROM match_records ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMatches(limit: Int): Flow<List<MatchRecord>>

    @Query("SELECT COUNT(*) FROM match_records")
    fun getTotalMatchCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM match_records WHERE isWin = 1")
    fun getWinMatchCount(): Flow<Int>

    @Query("SELECT COALESCE(MAX(originalBid), 0) FROM match_records WHERE isWin = 1")
    fun getHighestBidWon(): Flow<Int>

    @Query("SELECT COUNT(*) FROM match_records WHERE pairDeclared = 1")
    fun getTotalPairsDeclared(): Flow<Int>

    @Query("SELECT COUNT(*) FROM match_records WHERE isSingleHand = 1 AND isWin = 1")
    fun getSingleHandWinsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchRecord): Long

    @Query("DELETE FROM match_records")
    suspend fun clearHistory()
}
