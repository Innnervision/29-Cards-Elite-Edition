package com.example.game.model

enum class ProfilePrivacy(val title: String, val description: String) {
    PUBLIC("Public", "Visible to all players on leaderboards and social search."),
    FRIENDS_ONLY("Friends Only", "Only confirmed friends can view match history and stats."),
    PRIVATE("Private", "Hidden from public search and friend discovery.")
}

data class AchievementBadge(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val currentProgress: Int,
    val targetProgress: Int,
    val isUnlocked: Boolean = currentProgress >= targetProgress,
    val rewardDiamonds: Int = 10
)

data class UserProfile(
    val playerId: String = "P29-${(100000..999999).random()}",
    val username: String = "RoyalPlayer_${(100..999).random()}",
    val displayName: String = "Grandmaster Player",
    val bio: String = "Mastering the 28-pip trick-taking strategy in 29 Royale.",
    val avatarId: String = "avatar_player",
    val joinedDate: String = "September 2026",
    val privacy: ProfilePrivacy = ProfilePrivacy.PUBLIC,
    val gamesPlayed: Int = 18,
    val gamesWon: Int = 14,
    val gamesLost: Int = 4,
    val winStreak: Int = 3,
    val bestWinStreak: Int = 7,
    val total29TrickPoints: Long = 342L,
    val royaltyPairsDeclared: Int = 8,
    val singleHandVictories: Int = 2,
    val tournamentsWon: Int = 1,
    val achievements: List<AchievementBadge> = createDefaultAchievements(),
    // Social Media & Connected Accounts
    val email: String? = null,
    val phoneNumber: String? = null,
    val facebookHandle: String? = null,
    val instagramHandle: String? = null,
    val googleId: String? = null,
    val isEmailLinked: Boolean = false,
    val isPhoneLinked: Boolean = false,
    val isFacebookLinked: Boolean = false,
    val isInstagramLinked: Boolean = false,
    val isGoogleLinked: Boolean = false,
    val lastCloudBackupMs: Long = System.currentTimeMillis()
) {
    val winRatePercent: Int
        get() = if (gamesPlayed > 0) ((gamesWon.toFloat() / gamesPlayed) * 100).toInt() else 0

    companion object {
        fun createDefaultAchievements(): List<AchievementBadge> {
            return listOf(
                AchievementBadge("first_blood", "First Blood", "Win your first Classic 29 match", "MilitaryTech", 1, 1),
                AchievementBadge("streak_5", "Hot Streak", "Achieve a 5-match winning streak", "LocalFireDepartment", 3, 5),
                AchievementBadge("royalty_master", "Royalty King", "Declare King & Queen Pairs 10 times", "WorkspacePremium", 8, 10),
                AchievementBadge("centurion", "Centurion", "Win 50 total 29 matches", "Shield", 14, 50),
                AchievementBadge("single_hand", "Solo Legend", "Win a Single Hand solo with 29 points", "Star", 2, 5),
                AchievementBadge("high_roller", "Diamond Elite", "Amass 500 Vault Diamonds", "Diamond", 320, 500),
                AchievementBadge("tournament_champ", "Grand Champion", "Win the Grand 29 Cup Knockout Trophy", "EmojiEvents", 1, 1),
                AchievementBadge("academy_scholar", "Strategy Scholar", "Ask Grandmaster AI 10 strategy questions", "School", 4, 10)
            )
        }
    }
}
