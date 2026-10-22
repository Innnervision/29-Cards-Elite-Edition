package com.example.game.ads

/**
 * Central configuration for Google AdMob monetization system.
 * Contains feature toggles, Google test ad IDs, production ad IDs,
 * cooldown timers, and economic exchange rates.
 */
object AdConfig {

    /**
     * Automatically uses Google-provided test ad unit IDs during debug development,
     * and switches to verified production AdMob ad units in release builds.
     */
    var IS_TEST_MODE: Boolean = com.example.BuildConfig.DEBUG

    // ==========================================
    // GOOGLE OFFICIAL TEST AD UNIT IDs
    // https://developers.google.com/admob/android/test-ads
    // ==========================================
    const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"
    const val TEST_REWARDED_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/5354046379"
    const val TEST_APP_OPEN_ID = "ca-app-pub-3940256099942544/9257395921"

    // ==========================================
    // PRODUCTION ADMOB AD UNIT IDs
    // ==========================================
    const val PROD_BANNER_ID = "ca-app-pub-8759032363251002/7670256356"
    const val PROD_INTERSTITIAL_ID = "ca-app-pub-8759032363251002/4488674760"
    const val PROD_REWARDED_ID = "ca-app-pub-8759032363251002/1862511421"
    const val PROD_REWARDED_INTERSTITIAL_ID = "ca-app-pub-8759032363251002/1451447887"
    const val PROD_APP_OPEN_ID = "ca-app-pub-8759032363251002/7670256356" // Default to publisher app ID

    fun getBannerId(): String = if (IS_TEST_MODE) TEST_BANNER_ID else PROD_BANNER_ID
    fun getInterstitialId(): String = if (IS_TEST_MODE) TEST_INTERSTITIAL_ID else PROD_INTERSTITIAL_ID
    fun getRewardedId(): String = if (IS_TEST_MODE) TEST_REWARDED_ID else PROD_REWARDED_ID
    fun getRewardedInterstitialId(): String = if (IS_TEST_MODE) TEST_REWARDED_INTERSTITIAL_ID else PROD_REWARDED_INTERSTITIAL_ID
    fun getAppOpenId(): String = if (IS_TEST_MODE) TEST_APP_OPEN_ID else PROD_APP_OPEN_ID

    // ==========================================
    // FEATURE TOGGLES
    // ==========================================
    var ENABLE_BANNER: Boolean = true
    var ENABLE_INTERSTITIAL: Boolean = true
    var ENABLE_REWARDED: Boolean = true
    var ENABLE_REWARDED_INTERSTITIAL: Boolean = true
    var ENABLE_APP_OPEN: Boolean = true

    /**
     * Set to false so real Google Ads are always loaded and rendered via Google Mobile Ads SDK.
     */
    var AVOID_EMULATOR_RENDERER_ERRORS: Boolean = false

    // ==========================================
    // ECONOMY & COOLDOWNS
    // ==========================================
    /** Primary diamond reward per watched rewarded video ad */
    const val DIAMONDS_PER_REWARDED_AD: Int = 1

    /** Generous coin reward granted alongside diamond per watched video ad */
    const val COINS_PER_REWARDED_AD: Long = 2500L

    /** Diamonds required to instantly unlock a locked adventure level */
    const val DIAMOND_UNLOCK_LEVEL_COST: Int = 5

    /** Cooldown period in seconds before another interstitial ad can be shown */
    const val INTERSTITIAL_COOLDOWN_SECONDS: Long = 180L // 3 minutes

    /** Number of completed matches/levels required between interstitial showings */
    const val INTERSTITIAL_LEVEL_INTERVAL: Int = 3

    /** Maximum rewarded ads for diamond generation per day to protect economy */
    const val MAX_DIAMOND_REWARDED_ADS_PER_DAY: Int = 30

    /** Base starting economy for a newly created player */
    const val INITIAL_PLAYER_DIAMONDS: Int = 5
    const val INITIAL_PLAYER_COINS: Long = 5000L
}
