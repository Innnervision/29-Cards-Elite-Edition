package com.example.game.model

enum class UserStatus(val label: String, val isLive: Boolean) {
    ONLINE("Live in Lobby", true),
    IN_MATCH("Live in Match", true),
    HOSTING_ROOM("Hosting Table", true),
    OFFLINE("Offline", false)
}

data class SocialUser(
    val id: String,
    val username: String,
    val displayName: String,
    val avatarId: String,
    val level: Int,
    val vipTitle: String,
    val status: UserStatus,
    val gamesWon: Int = 10,
    val winRate: Int = 65,
    val lastActive: String = "Just now",
    val liveActivity: String = "Waiting in Lobby",
    val roomCode: String? = null
) {
    val isLive: Boolean get() = status != UserStatus.OFFLINE
}

enum class RequestStatus {
    PENDING,
    ACCEPTED,
    DECLINED
}

data class FriendRequest(
    val requestId: String,
    val sender: SocialUser,
    val timestamp: Long = System.currentTimeMillis(),
    val status: RequestStatus = RequestStatus.PENDING
)

data class BlockedUser(
    val userId: String,
    val username: String,
    val displayName: String,
    val blockedTimestamp: Long = System.currentTimeMillis()
)

enum class SocialPlatform(
    val title: String,
    val iconEmoji: String,
    val brandColor: Long,
    val defaultHandlePrompt: String,
    val shareUrlPrefix: String,
    val diamondRewardPerShare: Int = 20
) {
    FACEBOOK(
        title = "Facebook",
        iconEmoji = "📘",
        brandColor = 0xFF1877F2,
        defaultHandlePrompt = "facebook.com/username",
        shareUrlPrefix = "https://www.facebook.com/sharer/sharer.php?u="
    ),
    INSTAGRAM(
        title = "Instagram",
        iconEmoji = "📸",
        brandColor = 0xFFE4405F,
        defaultHandlePrompt = "@instagram_handle",
        shareUrlPrefix = "https://www.instagram.com/"
    ),
    WHATSAPP(
        title = "WhatsApp",
        iconEmoji = "💬",
        brandColor = 0xFF25D366,
        defaultHandlePrompt = "+880 1712-345678",
        shareUrlPrefix = "https://api.whatsapp.com/send?text="
    ),
    TWITTER_X(
        title = "X (Twitter)",
        iconEmoji = "🐦",
        brandColor = 0xFF000000,
        defaultHandlePrompt = "@twitter_handle",
        shareUrlPrefix = "https://twitter.com/intent/tweet?text="
    ),
    YOUTUBE(
        title = "YouTube",
        iconEmoji = "▶️",
        brandColor = 0xFFCD201F,
        defaultHandlePrompt = "youtube.com/@channel",
        shareUrlPrefix = "https://www.youtube.com/"
    ),
    TELEGRAM(
        title = "Telegram",
        iconEmoji = "✈️",
        brandColor = 0xFF0088CC,
        defaultHandlePrompt = "@telegram_username",
        shareUrlPrefix = "https://t.me/share/url?url="
    )
}

data class LinkedSocialAccount(
    val platform: SocialPlatform,
    val handleOrUrl: String,
    val linkedTimestamp: Long = System.currentTimeMillis(),
    val totalSharesCount: Int = 0,
    val totalDiamondsEarned: Int = 0
)

data class SocialShareRewardResult(
    val platform: SocialPlatform,
    val diamondsAwarded: Int,
    val message: String
)
