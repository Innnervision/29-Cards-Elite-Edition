package com.example.game.model

import com.example.R

enum class CardRarity(val label: String, val color: Long, val badgeBg: Long) {
    COMMON("COMMON", 0xFF94A3B8, 0xFF1E293B),
    RARE("RARE", 0xFF38BDF8, 0xFF0C4A6E),
    EPIC("EPIC", 0xFFA855F7, 0xFF581C87),
    LEGENDARY("LEGENDARY", 0xFFFFD700, 0xFF78350F),
    MYTHIC("MYTHIC", 0xFFFF007A, 0xFF881337)
}

enum class CardSkin(
    val id: String,
    val title: String,
    val description: String,
    val priceDiamonds: Int,
    val priceCoins: Long,
    val rarity: CardRarity = CardRarity.COMMON,
    val imageRes: Int? = null,
    val accentColor: Long = 0xFFFFD700,
    val iconEmoji: String = "🃏"
) {
    CLASSIC_ROYAL(
        id = "CLASSIC_ROYAL",
        title = "Royal Blue Classic",
        description = "Traditional authentic 29 casino navy felt backing with gold filigree.",
        priceDiamonds = 0,
        priceCoins = 0,
        rarity = CardRarity.COMMON,
        imageRes = null,
        accentColor = 0xFF3B82F6,
        iconEmoji = "♠️"
    ),
    GOLDEN_MANDALA(
        id = "GOLDEN_MANDALA",
        title = "Maharaja 24K Mandala",
        description = "Intricate 24-karat gold Indian filigree on imperial crimson velvet.",
        priceDiamonds = 3,
        priceCoins = 3000,
        rarity = CardRarity.RARE,
        imageRes = R.drawable.img_cardback_royal_gold,
        accentColor = 0xFFFFD700,
        iconEmoji = "👑"
    ),
    CYBER_NEON(
        id = "CYBER_NEON",
        title = "Cyberpunk Synthwave 29",
        description = "High-voltage electric cyan & magenta glowing synth circuitry lines.",
        priceDiamonds = 6,
        priceCoins = 6000,
        rarity = CardRarity.RARE,
        imageRes = null,
        accentColor = 0xFF06B6D4,
        iconEmoji = "⚡"
    ),
    CRIMSON_DRAGON(
        id = "CRIMSON_DRAGON",
        title = "Crimson Emperor Dragon",
        description = "Fierce blazing ruby dragon scales with imperial golden dynastic seal.",
        priceDiamonds = 9,
        priceCoins = 9000,
        rarity = CardRarity.EPIC,
        imageRes = null,
        accentColor = 0xFFEF4444,
        iconEmoji = "🐉"
    ),
    DIAMOND_LUXURY(
        id = "DIAMOND_LUXURY",
        title = "Grandmaster Diamond VIP",
        description = "Prestige sparkling diamond crystal casing with platinum bevel borders.",
        priceDiamonds = 12,
        priceCoins = 12000,
        rarity = CardRarity.EPIC,
        imageRes = null,
        accentColor = 0xFF38BDF8,
        iconEmoji = "💎"
    ),
    MIDNIGHT_OBSIDIAN(
        id = "MIDNIGHT_OBSIDIAN",
        title = "Midnight Shadow Obsidian",
        description = "Matte stealth black backing with iridescent rainbow-foil pips & silver trim.",
        priceDiamonds = 16,
        priceCoins = 16000,
        rarity = CardRarity.LEGENDARY,
        imageRes = null,
        accentColor = 0xFFA855F7,
        iconEmoji = "🌌"
    ),
    SOLAR_ECLIPSE(
        id = "SOLAR_ECLIPSE",
        title = "Celestial Solar Eclipse",
        description = "Radiant corona flares with a burnished solar ring over deep cosmic nebula.",
        priceDiamonds = 20,
        priceCoins = 20000,
        rarity = CardRarity.LEGENDARY,
        imageRes = null,
        accentColor = 0xFFF59E0B,
        iconEmoji = "☀️"
    ),
    EMERALD_SERPENT(
        id = "EMERALD_SERPENT",
        title = "Jade Cobra Sovereign",
        description = "Lustrous polished jade dragon scales with sacred gilded cobra inlay.",
        priceDiamonds = 24,
        priceCoins = 24000,
        rarity = CardRarity.LEGENDARY,
        imageRes = null,
        accentColor = 0xFF10B981,
        iconEmoji = "🐍"
    ),
    ROYAL_RAJPUTANA(
        id = "ROYAL_RAJPUTANA",
        title = "Rajputana Royal Dynasty",
        description = "Heritage Rajasthani palace tapestry with peacock plumes and uncut rubies.",
        priceDiamonds = 28,
        priceCoins = 28000,
        rarity = CardRarity.MYTHIC,
        imageRes = null,
        accentColor = 0xFFEC4899,
        iconEmoji = "🦚"
    ),
    PHANTOM_GHOST(
        id = "PHANTOM_GHOST",
        title = "Phantom Spectre Ghost",
        description = "Ethereal spectral violet soul flame with glowing ultraviolet holographic foil.",
        priceDiamonds = 35,
        priceCoins = 35000,
        rarity = CardRarity.MYTHIC,
        imageRes = null,
        accentColor = 0xFF8B5CF6,
        iconEmoji = "👻"
    ),
    VICTORIAN_ROYAL(
        id = "VICTORIAN_ROYAL",
        title = "Victorian London Velvet",
        description = "19th-century royal London heraldry with 24K gold filigree lions & crowns.",
        priceDiamonds = 14,
        priceCoins = 14000,
        rarity = CardRarity.EPIC,
        imageRes = null,
        accentColor = 0xFFD4AF37,
        iconEmoji = "🏰"
    ),
    BENGAL_TIGER(
        id = "BENGAL_TIGER",
        title = "Royal Bengal Sovereign",
        description = "Majestic golden Bengal tiger motif with glowing emerald eyes & imperial stripes.",
        priceDiamonds = 18,
        priceCoins = 18000,
        rarity = CardRarity.LEGENDARY,
        imageRes = null,
        accentColor = 0xFFF97316,
        iconEmoji = "🐯"
    ),
    ANIME_SAMURAI(
        id = "ANIME_SAMURAI",
        title = "Neo-Tokyo Blade Anime",
        description = "Japanese anime-styled cyber katana with radiant sakura blossom holographic foil.",
        priceDiamonds = 22,
        priceCoins = 22000,
        rarity = CardRarity.LEGENDARY,
        imageRes = null,
        accentColor = 0xFFEC4899,
        iconEmoji = "⚔️"
    ),
    AZTEC_GOLD(
        id = "AZTEC_GOLD",
        title = "Aztec Sun Sovereign",
        description = "Pre-Columbian sun medallion inlaid with turquoise beads and hammered Aztec gold.",
        priceDiamonds = 26,
        priceCoins = 26000,
        rarity = CardRarity.MYTHIC,
        imageRes = null,
        accentColor = 0xFFEAB308,
        iconEmoji = "🌞"
    ),
    FANTASY_ELVEN(
        id = "FANTASY_ELVEN",
        title = "Ethereal Elven Moonlight",
        description = "Luminous starlight silver filigree enchanted with ancient glowing mythical runes.",
        priceDiamonds = 30,
        priceCoins = 30000,
        rarity = CardRarity.MYTHIC,
        imageRes = null,
        accentColor = 0xFF38BDF8,
        iconEmoji = "✨"
    )
}

enum class TableTheme(
    val id: String,
    val title: String,
    val description: String,
    val priceDiamonds: Int,
    val priceCoins: Long,
    val feltGradients: List<Long>
) {
    EMERALD_CASINO(
        id = "EMERALD_CASINO",
        title = "Emerald Casino Felt",
        description = "Classic authentic Indian 29 green gaming felt with royal mahogany rim.",
        priceDiamonds = 0,
        priceCoins = 0,
        feltGradients = listOf(0xFF0D5C3A, 0xFF064128, 0xFF032616)
    ),
    ROYAL_SAPPHIRE(
        id = "ROYAL_SAPPHIRE",
        title = "Royal Sapphire Blue",
        description = "Deep velvet sapphire felt with silver filigree table trim.",
        priceDiamonds = 2,
        priceCoins = 2000,
        feltGradients = listOf(0xFF1E3A8A, 0xFF0F265C, 0xFF081430)
    ),
    MIDNIGHT_VELVET(
        id = "MIDNIGHT_VELVET",
        title = "Midnight Onyx Velvet",
        description = "Dark luxury VIP table with golden championship card rail.",
        priceDiamonds = 5,
        priceCoins = 5000,
        feltGradients = listOf(0xFF18181B, 0xFF09090B, 0xFF000000)
    ),
    CRIMSON_GOLD(
        id = "CRIMSON_GOLD",
        title = "Imperial Crimson Gold",
        description = "High-stakes palace felt with radiant ember accents.",
        priceDiamonds = 10,
        priceCoins = 10000,
        feltGradients = listOf(0xFF881337, 0xFF4C0519, 0xFF2A000D)
    ),
    MUGHAL_COURT(
        id = "MUGHAL_COURT",
        title = "Mughal Darbar Silk",
        description = "Imperial Persian turquoise silk felt with gold leaf floral jaali embroidery.",
        priceDiamonds = 8,
        priceCoins = 8000,
        feltGradients = listOf(0xFF0D9488, 0xFF115E59, 0xFF042F2E)
    ),
    NEON_CYBER(
        id = "NEON_CYBER",
        title = "Neo Tokyo Cyber Matrix",
        description = "Futuristic dark neon hex grid with glowing violet and cyan borders.",
        priceDiamonds = 12,
        priceCoins = 12000,
        feltGradients = listOf(0xFF4C1D95, 0xFF1E1B4B, 0xFF020617)
    ),
    ROYAL_AMETHYST(
        id = "ROYAL_AMETHYST",
        title = "Royal Amethyst Velvet",
        description = "Rich imperial purple velvet felt with gilded crown emblems.",
        priceDiamonds = 15,
        priceCoins = 15000,
        feltGradients = listOf(0xFF701A75, 0xFF4A044E, 0xFF1F0124)
    )
}

enum class VipTier(val title: String, val minLevel: Int, val badgeColor: Long, val coinMultiplier: Float) {
    BRONZE("Bronze Rookie", 1, 0xFFCD7F32, 1.0f),
    SILVER("Silver Cardist", 5, 0xFFC0C0C0, 1.2f),
    GOLD("Gold Master", 10, 0xFFFFD700, 1.5f),
    PLATINUM("Platinum Pro", 20, 0xFFE5E4E2, 1.8f),
    DIAMOND("Diamond Legend", 35, 0xFF38BDF8, 2.2f),
    GRANDMASTER("Grandmaster 29", 50, 0xFFA855F7, 3.0f);

    companion object {
        fun fromLevel(level: Int): VipTier {
            return when {
                level >= 50 -> GRANDMASTER
                level >= 35 -> DIAMOND
                level >= 20 -> PLATINUM
                level >= 10 -> GOLD
                level >= 5 -> SILVER
                else -> BRONZE
            }
        }
    }
}

enum class AdPlacement(
    val title: String,
    val rewardDiamonds: Int,
    val rewardCoins: Long,
    val rewardDescription: String,
    val adType: String = "REWARDED" // REWARDED, REWARDED_INTERSTITIAL, INTERSTITIAL, BANNER
) {
    REWARDED_VAULT_DIAMONDS("Diamond Vault Ad", 1, 2500, "+1 Free Diamond 💎 & +2,500 Coins 🪙", "REWARDED"),
    REWARDED_DOUBLE_MATCH("2X Match Coins", 0, 2500, "2X Match Coin Winnings (+2,500 🪙)!", "REWARDED"),
    REWARDED_REVIVE_ROUND("Contract Shield", 0, 0, "Shield Round & Cancel Set Penalty 🛡️", "REWARDED"),
    REWARDED_AI_MENTOR("AI Grandmaster Pass", 1, 1000, "Free Tactical Hint & +1 💎 & +1,000 🪙", "REWARDED"),
    REWARDED_MYSTERY_CHEST("Mystery Diamond Chest", 5, 5000, "Chest Unlocked Instantly (+5,000 🪙)!", "REWARDED"),
    REWARDED_INTERSTITIAL_INSTANT_UNLOCK("Instant Chest Skip", 2, 3000, "Chest Unlocked Instantly (+3,000 🪙)!", "REWARDED_INTERSTITIAL"),
    REWARDED_INTERSTITIAL_TOURNAMENT_RUSH("Tournament Rush", 2, 3000, "Stage Cooldown Skipped (+3,000 🪙)!", "REWARDED_INTERSTITIAL"),
    INTERSTITIAL_MATCH_END("Match Breakdown", 0, 0, "Match Complete", "INTERSTITIAL"),
    DAILY_STREAK_BONUS("Daily Ad Streak Bonus", 2, 3000, "+2 Streak Diamonds 💎 & +3,000 🪙", "REWARDED"),
    REWARDED_MEGA_DIAMONDS("Mega Streak Diamond Bonus", 2, 5000, "+2 Diamonds 💎 & +5,000 🪙", "REWARDED"),
    REWARDED_COIN_BAG("Instant Coin Bag", 0, 2500, "+2,500 Free Gold Coins 🪙", "REWARDED"),
    REWARDED_HIGH_ROLLER_STASH("High Roller Stash", 1, 5000, "+5,000 Coins 🪙 & +1 Diamond 💎", "REWARDED"),
    REWARDED_JACKPOT_COIN_VAULT("Jackpot Coin Vault", 2, 10000, "+10,000 Coins 🪙 & +2 Diamonds 💎", "REWARDED")
}

data class DailyQuest(
    val id: String,
    val title: String,
    val description: String,
    val current: Int,
    val target: Int,
    val diamondReward: Int,
    val coinReward: Long,
    val xpReward: Int,
    val isClaimed: Boolean = false
) {
    val isCompleted: Boolean get() = current >= target
    val progressPercent: Float get() = (current.toFloat() / target).coerceIn(0f, 1f)
}

fun defaultDailyQuests(): List<DailyQuest> = listOf(
    DailyQuest(
        id = "quest_win_match",
        title = "29 Champion",
        description = "Win 2 full matches in any game mode",
        current = 0,
        target = 2,
        diamondReward = 1,
        coinReward = 1000,
        xpReward = 200
    ),
    DailyQuest(
        id = "quest_watch_ads",
        title = "Ad Diamond Hunter",
        description = "Watch 3 rewarded ads to sponsor the club",
        current = 0,
        target = 3,
        diamondReward = 2,
        coinReward = 1500,
        xpReward = 300
    ),
    DailyQuest(
        id = "quest_win_tricks",
        title = "Trick Collector",
        description = "Win 8 total tricks in gameplay",
        current = 0,
        target = 8,
        diamondReward = 1,
        coinReward = 800,
        xpReward = 150
    ),
    DailyQuest(
        id = "quest_declare_pair",
        title = "Royalty Declarer",
        description = "Declare a King & Queen Pair or Reveal Trump",
        current = 0,
        target = 1,
        diamondReward = 1,
        coinReward = 1000,
        xpReward = 250
    )
)

data class PlayerEconomy(
    val coins: Long = 2500,
    val diamonds: Int = 1,
    val level: Int = 1,
    val currentXp: Int = 50,
    val totalAdsWatched: Int = 0,
    val dailyAdsWatched: Int = 0,
    val lastAdDate: String = "",
    val unlockedCardSkins: Set<String> = setOf("CLASSIC_ROYAL"),
    val equippedCardSkin: String = "CLASSIC_ROYAL",
    val unlockedTableThemes: Set<String> = setOf("EMERALD_CASINO"),
    val equippedTableTheme: String = "EMERALD_CASINO",
    val quests: List<DailyQuest> = defaultDailyQuests(),
    val isVipPassActive: Boolean = false,
    val doubleXpActiveMatches: Int = 0,
    val mysteryChestCooldownEndMs: Long = 0L,
    val tournamentCooldownEndMs: Long = 0L
) {
    val xpForNextLevel: Int get() = level * 300
    val xpProgress: Float get() = (currentXp.toFloat() / xpForNextLevel).coerceIn(0f, 1f)
    val vipTier: VipTier get() = VipTier.fromLevel(level)
    val isMysteryChestReady: Boolean get() = System.currentTimeMillis() >= mysteryChestCooldownEndMs
    val chestRemainingSeconds: Long get() = ((mysteryChestCooldownEndMs - System.currentTimeMillis()) / 1000L).coerceAtLeast(0L)
}

data class ActiveAdSession(
    val isVisible: Boolean = false,
    val placement: AdPlacement = AdPlacement.REWARDED_VAULT_DIAMONDS,
    val totalDurationSec: Int = 5,
    val currentRemainingSec: Int = 5,
    val isRewardClaimable: Boolean = false,
    val isCompleted: Boolean = false,
    val isInterstitial: Boolean = false,
    val isRewardedInterstitial: Boolean = false,
    val adCreativeTitle: String = "Royal Jackpot Slots RPG",
    val adCreativeTagline: String = "Claim 10,000,000 Free Coins, Spin Lucky Dragons & Win Massive Jackpots!",
    val adCreativeCta: String = "INSTALL & PLAY FREE"
)
