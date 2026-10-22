package com.example.game.social

import android.content.Context
import android.content.Intent

object ShareManager {

    private const val DEEP_LINK_BASE = "https://universeinvedors.twentynine/invite"

    fun generateInviteLink(playerCode: String): String {
        return "$DEEP_LINK_BASE/$playerCode"
    }

    fun shareGameInvitation(context: Context, playerName: String, playerCode: String) {
        val inviteLink = generateInviteLink(playerCode)
        val message = """
            👑 Join me in 29 Card Game (Twenty-Nine Royale)!
            
            Challenge me in live trick-taking multiplayer or play against grandmaster AI.
            
            Use my player code: $playerCode
            Direct Match Link: $inviteLink
            
            Download now and claim 5,000 bonus coins + 50 Diamonds!
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            putExtra(Intent.EXTRA_SUBJECT, "Challenge me in 29 Card Game!")
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "Invite Friends via...")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    fun shareMatchVictory(context: Context, pointsWon: Int, modeName: String) {
        val message = """
            🏆 I just conquered a $modeName match in 29 Card Game with $pointsWon points!
            
            Can you out-bid and out-trick my partnership?
            Join the table: $DEEP_LINK_BASE
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            putExtra(Intent.EXTRA_SUBJECT, "29 Card Game Victory!")
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "Share Victory...")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}
