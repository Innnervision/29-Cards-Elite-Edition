package com.example.game.data

import kotlinx.coroutines.flow.Flow

class GameStatsRepository(private val dao: MatchRecordDao) {
    val allMatches: Flow<List<MatchRecord>> = dao.getAllMatches()
    val recentMatches: Flow<List<MatchRecord>> = dao.getRecentMatches(10)
    val totalMatches: Flow<Int> = dao.getTotalMatchCount()
    val totalWins: Flow<Int> = dao.getWinMatchCount()
    val highestBidWon: Flow<Int> = dao.getHighestBidWon()
    val totalPairsDeclared: Flow<Int> = dao.getTotalPairsDeclared()
    val singleHandWins: Flow<Int> = dao.getSingleHandWinsCount()

    suspend fun recordMatch(match: MatchRecord) {
        dao.insertMatch(match)
    }

    suspend fun resetStats() {
        dao.clearHistory()
    }
}
