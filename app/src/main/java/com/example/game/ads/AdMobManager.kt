package com.example.game.ads

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.webkit.WebView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import java.io.File
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Production-ready Google AdMob Manager for Royal 29 Royale.
 * Centralized, lifecycle-aware manager handling:
 * - App Open Ads (launch & resume)
 * - Banner Ads (adaptive non-gameplay screens)
 * - Interstitial Ads (natural game breaks with cooldown & match interval)
 * - Rewarded Ads (opt-in 💎 Diamonds & 🪙 2x coins)
 * - Rewarded Interstitial Ads (optional stage transitions)
 */
object AdMobManager {
    private const val TAG = "AdMobManager"

    private var isInitialized = false
    private val isInitializing = AtomicBoolean(false)

    // Preloaded Ad Instances
    private var rewardedAd: RewardedAd? = null
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private var interstitialAd: InterstitialAd? = null
    private var appOpenAd: AppOpenAd? = null

    // Loading flags
    private var isLoadingRewarded = false
    private var isLoadingRewardedInterstitial = false
    private var isLoadingInterstitial = false
    private var isLoadingAppOpen = false

    // App Open Ad freshness timestamp (valid for 4 hours per AdMob policy)
    private var appOpenAdLoadTime: Long = 0
    private const val APP_OPEN_MAX_HOURS = 4

    // Interstitial pacing & frequency control
    private var lastInterstitialTimeMs: Long = 0
    private var matchesSinceLastInterstitial: Int = 0
    private var isShowingFullScreenAd = false

    /**
     * Detects if the app is currently running in a virtualized, headless, or containerized
     * environment without physical GPU DRM rendernodes (/dev/dri/renderD128, Mali, Adreno).
     */
    fun isRunningInEmulator(): Boolean {
        val fingerprint = Build.FINGERPRINT ?: ""
        val model = Build.MODEL ?: ""
        val manufacturer = Build.MANUFACTURER ?: ""
        val brand = Build.BRAND ?: ""
        val device = Build.DEVICE ?: ""
        val product = Build.PRODUCT ?: ""
        val hardware = Build.HARDWARE ?: ""

        val isKnownEmulator = fingerprint.startsWith("generic")
                || fingerprint.startsWith("unknown")
                || model.contains("google_sdk", ignoreCase = true)
                || model.contains("Emulator", ignoreCase = true)
                || model.contains("Android SDK built for", ignoreCase = true)
                || manufacturer.contains("Genymotion", ignoreCase = true)
                || (brand.startsWith("generic") && device.startsWith("generic"))
                || product.contains("sdk", ignoreCase = true)
                || product.contains("google_sdk", ignoreCase = true)
                || product.contains("emulator", ignoreCase = true)
                || hardware.contains("goldfish", ignoreCase = true)
                || hardware.contains("ranchu", ignoreCase = true)

        val hasHardwareGpu = File("/dev/dri/renderD128").exists()
                || File("/dev/kgsl-3d0").exists()
                || File("/dev/mali0").exists()
                || File("/dev/pvr").exists()
                || File("/dev/nvhost-as-gpu").exists()

        return isKnownEmulator || !hasHardwareGpu
    }

    /**
     * Initializes Google Mobile Ads SDK with test device configuration and pre-warms cache.
     */
    fun initialize(context: Context, onInitComplete: () -> Unit = {}) {
        if (isInitialized) {
            onInitComplete()
            return
        }
        if (isInitializing.getAndSet(true)) {
            return
        }

        prewarmWebViewEnvironment(context)

        try {
            val requestConfigBuilder = RequestConfiguration.Builder()
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)

            if (AdConfig.IS_TEST_MODE) {
                requestConfigBuilder.setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
            }
            MobileAds.setRequestConfiguration(requestConfigBuilder.build())

            MobileAds.initialize(context) { status ->
                isInitialized = true
                isInitializing.set(false)
                Log.d(TAG, "AdMob SDK Initialized (testMode=${AdConfig.IS_TEST_MODE}): $status")

                // Preload ad inventory asynchronously
                preloadAll(context)
                onInitComplete()
            }
        } catch (e: Throwable) {
            Log.e(TAG, "AdMob initialization warning: ${e.message}")
            isInitialized = true
            isInitializing.set(false)
            onInitComplete()
        }
    }

    /**
     * Pre-warms the internal Chromium WebView cache directory structure
     * to eliminate virtualized SimpleBackend directory creation errors.
     */
    fun prewarmWebViewEnvironment(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val processName = android.app.Application.getProcessName()
                if (context.packageName != processName) {
                    try {
                        WebView.setDataDirectorySuffix(processName)
                    } catch (_: Throwable) {}
                }
            }

            val cacheDir = context.cacheDir
            val defaultDir = File(cacheDir, "WebView/Default")
            val httpCacheDir = File(defaultDir, "HTTP Cache")
            val codeCacheDir = File(httpCacheDir, "Code Cache")
            val jsDir = File(codeCacheDir, "js")
            val wasmDir = File(codeCacheDir, "wasm")

            if (!jsDir.exists()) jsDir.mkdirs()
            if (!wasmDir.exists()) wasmDir.mkdirs()
        } catch (t: Throwable) {
            Log.w(TAG, "Notice setting up webview cache: ${t.message}")
        }
    }

    /**
     * Preloads all ad formats in the background so gameplay is never blocked.
     */
    fun preloadAll(context: Context) {
        if (AdConfig.ENABLE_REWARDED) loadRewardedAd(context)
        if (AdConfig.ENABLE_INTERSTITIAL) loadInterstitialAd(context)
        if (AdConfig.ENABLE_REWARDED_INTERSTITIAL) loadRewardedInterstitialAd(context)
        if (AdConfig.ENABLE_APP_OPEN) loadAppOpenAd(context)
    }

    // ==========================================
    // REWARDED ADS (PRIMARY MONETIZATION)
    // ==========================================

    fun isRewardedAdReady(): Boolean = rewardedAd != null

    fun loadRewardedAd(context: Context) {
        if (!AdConfig.ENABLE_REWARDED) return
        if (rewardedAd != null || isLoadingRewarded) return
        isLoadingRewarded = true

        try {
            val adRequest = AdRequest.Builder().build()
            val adUnitId = AdConfig.getRewardedId()

            RewardedAd.load(
                context,
                adUnitId,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        isLoadingRewarded = false
                        Log.d(TAG, "Rewarded Ad preloaded successfully ($adUnitId)")
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        rewardedAd = null
                        isLoadingRewarded = false
                        Log.w(TAG, "Rewarded Ad failed to load: ${error.message} (code ${error.code})")
                    }
                }
            )
        } catch (e: Throwable) {
            rewardedAd = null
            isLoadingRewarded = false
            Log.w(TAG, "Exception loading rewarded ad: ${e.message}")
        }
    }

    /**
     * Shows a Rewarded Ad with strict atomic verification:
     * - [onUserEarnedReward] is called ONLY when the user legitimately finishes watching.
     * - Protects against double-rewarding by using an atomic execution guard.
     * - Preloads the next ad immediately after dismissal.
     * - Exempts user from immediate interstitials right after watching.
     */
    fun showRewardedAd(
        activity: Activity,
        onUserEarnedReward: (rewardAmount: Int) -> Unit,
        onAdClosed: () -> Unit = {},
        onFallback: () -> Unit = {}
    ) {
        if (!AdConfig.ENABLE_REWARDED) {
            onFallback()
            return
        }

        val ad = rewardedAd
        if (ad != null) {
            isShowingFullScreenAd = true
            var hasEarnedReward = false

            try {
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        isShowingFullScreenAd = false
                        rewardedAd = null
                        // Give player interstitial exemption after engaging with a rewarded ad
                        lastInterstitialTimeMs = System.currentTimeMillis()
                        loadRewardedAd(activity)
                        onAdClosed()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        isShowingFullScreenAd = false
                        Log.w(TAG, "Rewarded Ad failed to show: ${adError.message}")
                        rewardedAd = null
                        loadRewardedAd(activity)
                        onFallback()
                    }
                }

                ad.show(activity) { rewardItem ->
                    if (!hasEarnedReward) {
                        hasEarnedReward = true
                        val amount = if (rewardItem.amount > 0) rewardItem.amount else AdConfig.DIAMONDS_PER_REWARDED_AD
                        Log.d(TAG, "AdMob User Earned Reward: $amount")
                        onUserEarnedReward(amount)
                    }
                }
            } catch (e: Throwable) {
                isShowingFullScreenAd = false
                Log.w(TAG, "Exception displaying rewarded ad: ${e.message}")
                rewardedAd = null
                loadRewardedAd(activity)
                onFallback()
            }
        } else {
            // Ad not ready yet, trigger background reload and call graceful fallback
            loadRewardedAd(activity)
            onFallback()
        }
    }

    // ==========================================
    // REWARDED INTERSTITIAL ADS
    // ==========================================

    fun isRewardedInterstitialReady(): Boolean = rewardedInterstitialAd != null

    fun loadRewardedInterstitialAd(context: Context) {
        if (!AdConfig.ENABLE_REWARDED_INTERSTITIAL) return
        if (rewardedInterstitialAd != null || isLoadingRewardedInterstitial) return
        isLoadingRewardedInterstitial = true

        try {
            val adRequest = AdRequest.Builder().build()
            val adUnitId = AdConfig.getRewardedInterstitialId()

            RewardedInterstitialAd.load(
                context,
                adUnitId,
                adRequest,
                object : RewardedInterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedInterstitialAd) {
                        rewardedInterstitialAd = ad
                        isLoadingRewardedInterstitial = false
                        Log.d(TAG, "Rewarded Interstitial preloaded successfully")
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        rewardedInterstitialAd = null
                        isLoadingRewardedInterstitial = false
                        Log.w(TAG, "Rewarded Interstitial failed to load: ${error.message}")
                    }
                }
            )
        } catch (e: Throwable) {
            rewardedInterstitialAd = null
            isLoadingRewardedInterstitial = false
            Log.w(TAG, "Exception loading rewarded interstitial: ${e.message}")
        }
    }

    fun showRewardedInterstitialAd(
        activity: Activity,
        onUserEarnedReward: (rewardAmount: Int) -> Unit,
        onAdClosed: () -> Unit = {},
        onFallback: () -> Unit = {}
    ) {
        if (!AdConfig.ENABLE_REWARDED_INTERSTITIAL) {
            onFallback()
            return
        }

        val ad = rewardedInterstitialAd
        if (ad != null) {
            isShowingFullScreenAd = true
            var hasEarnedReward = false

            try {
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        isShowingFullScreenAd = false
                        rewardedInterstitialAd = null
                        lastInterstitialTimeMs = System.currentTimeMillis()
                        loadRewardedInterstitialAd(activity)
                        onAdClosed()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        isShowingFullScreenAd = false
                        rewardedInterstitialAd = null
                        loadRewardedInterstitialAd(activity)
                        onFallback()
                    }
                }

                ad.show(activity) { rewardItem ->
                    if (!hasEarnedReward) {
                        hasEarnedReward = true
                        val amount = if (rewardItem.amount > 0) rewardItem.amount else 1
                        onUserEarnedReward(amount)
                    }
                }
            } catch (e: Throwable) {
                isShowingFullScreenAd = false
                rewardedInterstitialAd = null
                loadRewardedInterstitialAd(activity)
                onFallback()
            }
        } else {
            loadRewardedInterstitialAd(activity)
            onFallback()
        }
    }

    // ==========================================
    // INTERSTITIAL ADS (BREAKS & PACED TRANSITIONS)
    // ==========================================

    fun isInterstitialReady(): Boolean = interstitialAd != null

    fun recordMatchCompletion() {
        matchesSinceLastInterstitial++
    }

    /**
     * Checks whether an interstitial is currently eligible based on:
     * - Interstitial enabled flag
     * - Cooldown period (INTERSTITIAL_COOLDOWN_SECONDS)
     * - Match/level interval (INTERSTITIAL_LEVEL_INTERVAL)
     */
    fun canShowInterstitial(): Boolean {
        if (!AdConfig.ENABLE_INTERSTITIAL) return false
        if (interstitialAd == null) return false
        if (isShowingFullScreenAd) return false

        val elapsedMs = System.currentTimeMillis() - lastInterstitialTimeMs
        val cooldownPassed = elapsedMs >= (AdConfig.INTERSTITIAL_COOLDOWN_SECONDS * 1000L)
        val intervalPassed = matchesSinceLastInterstitial >= AdConfig.INTERSTITIAL_LEVEL_INTERVAL

        return cooldownPassed && intervalPassed
    }

    fun loadInterstitialAd(context: Context) {
        if (!AdConfig.ENABLE_INTERSTITIAL) return
        if (interstitialAd != null || isLoadingInterstitial) return
        isLoadingInterstitial = true

        try {
            val adRequest = AdRequest.Builder().build()
            val adUnitId = AdConfig.getInterstitialId()

            InterstitialAd.load(
                context,
                adUnitId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isLoadingInterstitial = false
                        Log.d(TAG, "Interstitial Ad preloaded successfully")
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        interstitialAd = null
                        isLoadingInterstitial = false
                        Log.w(TAG, "Interstitial Ad failed to load: ${error.message}")
                    }
                }
            )
        } catch (e: Throwable) {
            interstitialAd = null
            isLoadingInterstitial = false
            Log.w(TAG, "Exception loading interstitial ad: ${e.message}")
        }
    }

    /**
     * Shows interstitial only if eligible under cooldown/interval rules,
     * or bypasses and immediately continues if ineligible.
     */
    fun showInterstitialIfEligible(
        activity: Activity,
        forceShow: Boolean = false,
        onAdClosed: () -> Unit
    ) {
        if (!forceShow && !canShowInterstitial()) {
            onAdClosed()
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            isShowingFullScreenAd = true
            try {
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        isShowingFullScreenAd = false
                        interstitialAd = null
                        lastInterstitialTimeMs = System.currentTimeMillis()
                        matchesSinceLastInterstitial = 0
                        loadInterstitialAd(activity)
                        onAdClosed()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        isShowingFullScreenAd = false
                        Log.w(TAG, "Interstitial Ad failed to show: ${adError.message}")
                        interstitialAd = null
                        loadInterstitialAd(activity)
                        onAdClosed()
                    }
                }
                ad.show(activity)
            } catch (e: Throwable) {
                isShowingFullScreenAd = false
                Log.w(TAG, "Exception showing interstitial: ${e.message}")
                interstitialAd = null
                loadInterstitialAd(activity)
                onAdClosed()
            }
        } else {
            loadInterstitialAd(activity)
            onAdClosed()
        }
    }

    fun showInterstitialAd(
        activity: Activity,
        onAdClosed: () -> Unit = {}
    ) {
        showInterstitialIfEligible(activity, forceShow = true, onAdClosed = onAdClosed)
    }

    // ==========================================
    // APP OPEN ADS (LAUNCH & RESUME)
    // ==========================================

    private fun isAppOpenAdAvailable(): Boolean {
        val now = Date().time
        val ageMs = now - appOpenAdLoadTime
        val maxAgeMs = APP_OPEN_MAX_HOURS * 3600 * 1000L
        return appOpenAd != null && ageMs < maxAgeMs
    }

    fun loadAppOpenAd(context: Context) {
        if (!AdConfig.ENABLE_APP_OPEN) return
        if (isAppOpenAdAvailable() || isLoadingAppOpen) return
        isLoadingAppOpen = true

        try {
            val adRequest = AdRequest.Builder().build()
            val adUnitId = AdConfig.getAppOpenId()

            AppOpenAd.load(
                context,
                adUnitId,
                adRequest,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        appOpenAd = ad
                        appOpenAdLoadTime = Date().time
                        isLoadingAppOpen = false
                        Log.d(TAG, "App Open Ad preloaded successfully")
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        appOpenAd = null
                        isLoadingAppOpen = false
                        Log.w(TAG, "App Open Ad failed to load: ${error.message}")
                    }
                }
            )
        } catch (e: Throwable) {
            appOpenAd = null
            isLoadingAppOpen = false
            Log.w(TAG, "Exception loading app open ad: ${e.message}")
        }
    }

    /**
     * Displays the App Open Ad if available and if the player is not currently in active gameplay.
     */
    fun showAppOpenAdIfAvailable(
        activity: Activity,
        isGameplayActive: Boolean,
        onFinished: () -> Unit = {}
    ) {
        if (!AdConfig.ENABLE_APP_OPEN || isGameplayActive || isShowingFullScreenAd) {
            onFinished()
            return
        }

        if (isAppOpenAdAvailable()) {
            val ad = appOpenAd
            if (ad != null) {
                isShowingFullScreenAd = true
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        isShowingFullScreenAd = false
                        appOpenAd = null
                        loadAppOpenAd(activity)
                        onFinished()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        isShowingFullScreenAd = false
                        appOpenAd = null
                        loadAppOpenAd(activity)
                        onFinished()
                    }
                }
                ad.show(activity)
                return
            }
        }

        // If not available, reload for next time
        loadAppOpenAd(activity)
        onFinished()
    }
}
