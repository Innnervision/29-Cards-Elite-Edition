package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.TwentyNineViewModel
import com.example.game.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

enum class StoreTab(val title: String, val icon: String) {
    DIAMOND_VAULT("Free 💎 Vault", "💎"),
    CARD_BACKS("Card Skins", "🃏"),
    TABLE_THEMES("Table Felts", "🎲"),
    QUESTS("Quests", "📜")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    viewModel: TwentyNineViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val economy by viewModel.playerEconomy.collectAsState()
    var selectedTab by remember { mutableStateOf(StoreTab.CARD_BACKS) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var showFreeDiamondsModal by remember { mutableStateOf(false) }
    var insufficientItem by remember { mutableStateOf<Pair<String, Int>?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(
                                    text = "ROYAL 29 STORE",
                                    color = CasinoGold,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "VIP Tier: ${economy.vipTier.title}",
                                    color = Color(economy.vipTier.badgeColor),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Currency Balances & Social Share
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            // Share & Earn Diamond Trigger
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF1E1B4B),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA855F7)),
                                modifier = Modifier
                                    .luxuryInteractiveScale()
                                    .clickable {
                                        viewModel.soundManager.playPositiveAction()
                                        viewModel.openSocialShareModal()
                                    }
                                    .testTag("share_earn_store_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Text("🎁", fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Share & Earn", color = Color(0xFFE9D5FF), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                            }

                            AaaCurrencyPill(
                                type = CurrencyType.DIAMOND,
                                amount = economy.diamonds.toLong(),
                                onClick = { showFreeDiamondsModal = true }
                            )

                            AaaCurrencyPill(
                                type = CurrencyType.COIN,
                                amount = economy.coins,
                                onClick = { selectedTab = StoreTab.DIAMOND_VAULT }
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("store_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        },
        containerColor = Color(0xFF090D16)
    ) { padding ->
        LuxuryScreenBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            imageResId = R.drawable.img_store_diamonds,
            overlayAlpha = 0.55f
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Tab Selector Row with AAA styling
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0B0F19).copy(alpha = 0.85f))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StoreTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 2.dp)
                                .shadow(if (isSelected) 8.dp else 0.dp, RoundedCornerShape(10.dp), spotColor = CasinoGold)
                                .clip(RoundedCornerShape(10.dp))
                                .luxuryInteractiveScale()
                                .clickable {
                                    viewModel.soundManager.playNavTabClick()
                                    selectedTab = tab
                                }
                                .testTag("store_tab_${tab.name}"),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) CasinoGold else Color(0xFF1E293B),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFF0A5)) else androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF334155))
                        ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tab.icon, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tab.title,
                                color = if (isSelected) Color(0xFF0F172A) else Color(0xFFE2E8F0),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // Snackbar alert if any
            snackbarMessage?.let { msg ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0284C7)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = msg, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "DISMISS",
                            color = CasinoGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.clickable { snackbarMessage = null }
                        )
                    }
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                when (selectedTab) {
                    StoreTab.DIAMOND_VAULT -> DiamondVaultSection(
                        economy = economy,
                        onWatchAd = { placement ->
                            viewModel.watchAd(placement, activity)
                        },
                        onExchange = { diamonds ->
                            if (economy.diamonds >= diamonds) {
                                viewModel.exchangeDiamondsForCoins(diamonds)
                                snackbarMessage = "Exchanged $diamonds Diamonds for 10,000 Coins!"
                            } else {
                                snackbarMessage = "Need more Diamonds! Watch a quick ad to get +25 💎!"
                            }
                        }
                    )
                    StoreTab.CARD_BACKS -> CardSkinsSection(
                        economy = economy,
                        onEquip = { skin ->
                            viewModel.equipCardSkin(skin)
                            snackbarMessage = "Equipped ${skin.title}!"
                        },
                        onBuy = { skin ->
                            if (economy.diamonds >= skin.priceDiamonds) {
                                viewModel.buyCardSkin(skin)
                                snackbarMessage = "Unlocked ${skin.title}!"
                            } else {
                                insufficientItem = Pair(skin.title, skin.priceDiamonds)
                            }
                        },
                        onWatchAdForDiamonds = {
                            if (activity != null) {
                                viewModel.watchRewardedAdForDiamonds(activity)
                            }
                        }
                    )
                    StoreTab.TABLE_THEMES -> TableThemesSection(
                        economy = economy,
                        onEquip = { theme ->
                            viewModel.equipTableTheme(theme)
                            snackbarMessage = "Equipped ${theme.title}!"
                        },
                        onBuy = { theme ->
                            if (economy.diamonds >= theme.priceDiamonds) {
                                viewModel.buyTableTheme(theme)
                                snackbarMessage = "Unlocked ${theme.title}!"
                            } else {
                                insufficientItem = Pair(theme.title, theme.priceDiamonds)
                            }
                        },
                        onWatchAdForDiamonds = {
                            if (activity != null) {
                                viewModel.watchRewardedAdForDiamonds(activity)
                            }
                        }
                    )
                    StoreTab.QUESTS -> QuestsSection(
                        economy = economy,
                        onClaimQuest = { qId ->
                            viewModel.claimQuestReward(qId)
                            snackbarMessage = "Claimed Quest Reward!"
                        },
                        onWatchAd = {
                            if (activity != null) {
                                viewModel.watchRewardedAdForDiamonds(activity)
                            }
                        }
                    )
                }
            }
        }

        val showSocialModal by viewModel.showSocialShareModal.collectAsState()
        if (showSocialModal) {
            SocialAccountsAndShareModal(
                viewModel = viewModel,
                onDismiss = { viewModel.closeSocialShareModal() }
            )
        }

        if (insufficientItem != null) {
            val (itemName, price) = insufficientItem!!
            InsufficientDiamondsModal(
                requiredDiamonds = price,
                currentDiamonds = economy.diamonds,
                itemName = itemName,
                onWatchAd = {
                    insufficientItem = null
                    if (activity != null) {
                        viewModel.watchRewardedAdForDiamonds(activity)
                    }
                },
                onDismiss = { insufficientItem = null }
            )
        }

        if (showFreeDiamondsModal) {
            FreeDiamondsModal(
                economy = economy,
                onWatchAd = { act ->
                    viewModel.watchRewardedAdForDiamonds(act)
                },
                onDismiss = { showFreeDiamondsModal = false }
            )
        }
    }
}
}

@Composable
private fun DiamondVaultSection(
    economy: PlayerEconomy,
    onWatchAd: (AdPlacement) -> Unit,
    onExchange: (Int) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Hero Showcase Banner
        item {
            AaaGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp),
                borderBrush = GoldMetallicBrush,
                glowColor = CasinoGold
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_store_diamonds),
                        contentDescription = "Diamond Vault",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF030712).copy(alpha = 0.92f),
                                        Color(0xFF0F172A).copy(alpha = 0.70f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .padding(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxHeight(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            AaaBadge(text = "FREE DIAMONDS VAULT", color = CasinoDiamond)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Earn Unlimited Diamonds",
                                color = CasinoGold,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Watch sponsor video ads to unlock premium card backs, felt themes, & VIP perks.",
                                color = Color(0xFFE2E8F0),
                                fontSize = 10.5.sp,
                                lineHeight = 14.sp,
                                modifier = Modifier.widthIn(max = 240.dp)
                            )
                        }
                    }
                }
            }
        }

        // Rewarded Ad Options
        item {
            AaaSectionHeader(
                title = "WATCH ADS & EARN DIAMONDS",
                subtitle = "Free instant gems with zero limits",
                accentColor = CasinoDiamond
            )
        }

        // Standard Rewarded Ad Card (+1 💎 & +2,500 🪙)
        item {
            AdVaultCard(
                title = "Sponsor Video Booster",
                tagline = "Watch a short video to claim +2,500 Gold Coins and +1 Diamond",
                diamonds = 1,
                coins = 2500,
                buttonText = "WATCH AD (+2.5K 🪙)",
                variant = ButtonVariant.GOLD,
                tag = "watch_standard_ad_button",
                onClick = { onWatchAd(AdPlacement.REWARDED_VAULT_DIAMONDS) }
            )
        }

        // Dedicated Instant Coin Drop (+2,500 🪙)
        item {
            AdVaultCard(
                title = "Instant Coin Bag (+2,500 🪙)",
                tagline = "Pure gold coins injection with no cooldown or wait",
                diamonds = 0,
                coins = 2500,
                buttonText = "CLAIM COINS (+2.5K 🪙)",
                variant = ButtonVariant.PRIMARY,
                tag = "watch_coin_bag_ad_button",
                onClick = { onWatchAd(AdPlacement.REWARDED_COIN_BAG) }
            )
        }

        // Streak Sponsor Ad Card (+2 💎 & +5,000 🪙)
        item {
            AdVaultCard(
                title = "Daily Sponsor Streak (+5,000 🪙 & +2 💎)",
                tagline = "Boost your game progression with +5,000 Coins & +2 Diamonds",
                diamonds = 2,
                coins = 5000,
                buttonText = "CLAIM STREAK (+5K 🪙)",
                variant = ButtonVariant.GOLD,
                isHot = true,
                tag = "watch_mega_ad_button",
                onClick = { onWatchAd(AdPlacement.REWARDED_MEGA_DIAMONDS) }
            )
        }

        // High Roller Stash Ad Card (+5,000 🪙 & +1 💎)
        item {
            AdVaultCard(
                title = "High Roller Stash (+5,000 🪙)",
                tagline = "Sponsor reward designed for high stakes players",
                diamonds = 1,
                coins = 5000,
                buttonText = "WATCH STASH (+5K 🪙)",
                variant = ButtonVariant.DIAMOND,
                tag = "watch_high_roller_ad_button",
                onClick = { onWatchAd(AdPlacement.REWARDED_HIGH_ROLLER_STASH) }
            )
        }

        // Jackpot Coin Vault (+10,000 🪙 & +2 💎)
        item {
            AdVaultCard(
                title = "Jackpot Coin Vault (+10,000 🪙)",
                tagline = "Huge jackpot coin bonus to enter any high-stakes arena",
                diamonds = 2,
                coins = 10000,
                buttonText = "CLAIM JACKPOT (+10K 🪙)",
                variant = ButtonVariant.GOLD,
                isHot = true,
                tag = "watch_jackpot_ad_button",
                onClick = { onWatchAd(AdPlacement.REWARDED_JACKPOT_COIN_VAULT) }
            )
        }

        // Mystery Diamond Chest with Diamond / Rewarded Interstitial Instant Open
        item {
            AaaGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderBrush = Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFFC084FC))),
                glowColor = Color(0xFF8B5CF6)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🎁", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Timed Diamond Treasure Chest",
                                    color = Color.White,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Contains +50 Diamonds & +3,000 Gold Coins",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        AaaBadge(text = "INSTANT UNLOCK", color = Color(0xFFC084FC))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Unlock before time using Diamonds (universal source of truth) or watch a Rewarded Interstitial Ad to crack it instantly for free!",
                        color = Color(0xFFCBD5E1),
                        fontSize = 10.5.sp,
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Aaa3DButton(
                            text = "⚡ WATCH AD (FREE)",
                            onClick = { onWatchAd(AdPlacement.REWARDED_INTERSTITIAL_INSTANT_UNLOCK) },
                            modifier = Modifier.weight(1f).testTag("watch_interstitial_chest_button"),
                            variant = ButtonVariant.PRIMARY
                        )

                        Aaa3DButton(
                            text = "💎 15 DIAMONDS",
                            onClick = { onWatchAd(AdPlacement.REWARDED_MYSTERY_CHEST) },
                            modifier = Modifier.weight(1f).testTag("open_chest_diamonds_button"),
                            variant = ButtonVariant.DIAMOND
                        )
                    }
                }
            }
        }

        // AdMob Banner Unit
        item {
            Spacer(modifier = Modifier.height(4.dp))
            AdMobBanner(modifier = Modifier.fillMaxWidth())
        }

        // Currency Exchange Section
        item {
            AaaSectionHeader(
                title = "DIAMOND TO COIN EXCHANGE",
                subtitle = "Convert your gems into high-stakes table chips",
                accentColor = CasinoGold
            )
        }

        item {
            AaaGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderBrush = GoldMetallicBrush,
                glowColor = CasinoGold
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💎 30", fontSize = 16.sp, fontWeight = FontWeight.Black, color = CasinoDiamond)
                        Text(" ➔ ", fontSize = 16.sp, color = Color.White)
                        Text("🪙 10,000", fontSize = 16.sp, fontWeight = FontWeight.Black, color = CasinoGold)
                    }

                    Aaa3DButton(
                        text = "EXCHANGE",
                        onClick = { onExchange(30) },
                        variant = ButtonVariant.GOLD,
                        modifier = Modifier.testTag("exchange_diamonds_button")
                    )
                }
            }
        }
    }
}

@Composable
private fun AdVaultCard(
    title: String,
    tagline: String,
    diamonds: Int,
    coins: Long,
    buttonText: String,
    variant: ButtonVariant,
    isHot: Boolean = false,
    tag: String,
    onClick: () -> Unit
) {
    AaaGlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderBrush = if (isHot) GoldMetallicBrush else Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF1E293B))),
        glowColor = if (isHot) CasinoGold else Color.Transparent
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                )

                if (isHot) {
                    AaaBadge(text = "BEST VALUE 🔥", color = Color(0xFFEF4444))
                }
            }

            Text(
                text = tagline,
                color = Color(0xFF94A3B8),
                fontSize = 10.5.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CasinoDiamond.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CasinoDiamond.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "+$diamonds 💎",
                            color = CasinoDiamond,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CasinoGold.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CasinoGold.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "+$coins 🪙",
                            color = CasinoGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Aaa3DButton(
                    text = buttonText,
                    onClick = onClick,
                    variant = variant,
                    modifier = Modifier.testTag(tag)
                )
            }
        }
    }
}

@Composable
private fun CardSkinsSection(
    economy: PlayerEconomy,
    onEquip: (CardSkin) -> Unit,
    onBuy: (CardSkin) -> Unit,
    onWatchAdForDiamonds: () -> Unit
) {
    var inspectedSkin by remember {
        mutableStateOf(CardSkin.values().firstOrNull { it.id == economy.equippedCardSkin } ?: CardSkin.CLASSIC_ROYAL)
    }
    var selectedRarityFilter by remember { mutableStateOf<CardRarity?>(null) }

    val filteredSkins = remember(selectedRarityFilter) {
        if (selectedRarityFilter == null) {
            CardSkin.values().toList()
        } else {
            CardSkin.values().filter { it.rarity == selectedRarityFilter }
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. High-End Animated 3D Model Deck Showcase
        item {
            AnimatedCardModelShowcase(
                skin = inspectedSkin,
                economy = economy,
                onEquip = { skin ->
                    onEquip(skin)
                },
                onBuy = { skin ->
                    onBuy(skin)
                },
                onWatchAdForDiamonds = onWatchAdForDiamonds
            )
        }

        // 2. Free Diamonds Earning Callout
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onWatchAdForDiamonds),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0F172A),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(Color(0xFF38BDF8), Color(0xFFA855F7))))
            ) {
                Row(
                    modifier = Modifier
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF0C4A6E).copy(alpha = 0.35f), Color(0xFF581C87).copy(alpha = 0.25f))
                            )
                        )
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF38BDF8).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFF38BDF8), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💎", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "DIAMONDS ARE RARE & VALUABLE",
                                color = CasinoDiamond,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Watch sponsored video ads to collect diamonds for exclusive decks.",
                                color = Color(0xFFE2E8F0),
                                fontSize = 10.sp,
                                lineHeight = 13.5.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Aaa3DButton(
                        text = "EARN +1 💎",
                        icon = Icons.Default.PlayArrow,
                        onClick = onWatchAdForDiamonds,
                        height = 32.dp,
                        gradient = DiamondGlowBrush,
                        textColor = Color(0xFF0F172A)
                    )
                }
            }
        }

        // 3. Rarity Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedRarityFilter == null,
                    onClick = { selectedRarityFilter = null },
                    label = { Text("ALL (${CardSkin.values().size})", fontSize = 10.5.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CasinoGold,
                        selectedLabelColor = Color(0xFF0F172A),
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFFCBD5E1)
                    )
                )

                CardRarity.values().reversed().forEach { rarity ->
                    FilterChip(
                        selected = selectedRarityFilter == rarity,
                        onClick = { selectedRarityFilter = if (selectedRarityFilter == rarity) null else rarity },
                        label = { Text(rarity.label, fontSize = 10.5.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(rarity.color),
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(rarity.color)
                        )
                    )
                }
            }
        }

        // 4. Collectible Decks List
        items(filteredSkins) { skin ->
            val isUnlocked = economy.unlockedCardSkins.contains(skin.id) || skin.priceDiamonds == 0
            val isEquipped = economy.equippedCardSkin == skin.id
            val isInspected = inspectedSkin.id == skin.id

            AaaGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { inspectedSkin = skin },
                borderBrush = when {
                    isEquipped -> GoldMetallicBrush
                    isInspected -> Brush.sweepGradient(listOf(Color(skin.accentColor), Color.White, Color(skin.accentColor)))
                    else -> Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
                },
                glowColor = if (isEquipped) CasinoGold else if (isInspected) Color(skin.accentColor) else Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 3D Card Back Preview Thumbnail
                    Box(
                        modifier = Modifier
                            .width(62.dp)
                            .height(88.dp)
                            .shadow(8.dp, RoundedCornerShape(8.dp), spotColor = Color(skin.accentColor))
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.5.dp, Color(skin.accentColor), RoundedCornerShape(8.dp))
                    ) {
                        CardBackView(skinId = skin.id)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Skin Details
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = skin.title,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            AaaBadge(text = skin.rarity.label, color = Color(skin.rarity.color))
                            Spacer(modifier = Modifier.width(6.dp))
                            if (isEquipped) {
                                AaaBadge(text = "EQUIPPED", color = CasinoEmerald)
                            } else if (isUnlocked) {
                                AaaBadge(text = "OWNED", color = Color(0xFF38BDF8))
                            } else {
                                Text(
                                    text = "💎 ${skin.priceDiamonds} Diamonds",
                                    color = CasinoDiamond,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Text(
                            text = skin.description,
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            lineHeight = 13.5.sp,
                            maxLines = 2
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Action Controls
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isUnlocked) {
                            Aaa3DButton(
                                text = if (isEquipped) "IN USE" else "EQUIP",
                                icon = if (isEquipped) Icons.Default.Check else null,
                                onClick = {
                                    inspectedSkin = skin
                                    onEquip(skin)
                                },
                                enabled = !isEquipped,
                                height = 32.dp,
                                gradient = if (isEquipped) Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF1E293B))) else GoldMetallicBrush,
                                textColor = if (isEquipped) Color(0xFF94A3B8) else Color(0xFF0F172A),
                                testTag = "equip_skin_${skin.id}"
                            )
                        } else {
                            if (economy.diamonds >= skin.priceDiamonds) {
                                Aaa3DButton(
                                    text = "BUY 💎",
                                    icon = Icons.Default.ShoppingCart,
                                    onClick = {
                                        inspectedSkin = skin
                                        onBuy(skin)
                                    },
                                    height = 32.dp,
                                    gradient = DiamondGlowBrush,
                                    textColor = Color(0xFF0F172A),
                                    testTag = "buy_skin_${skin.id}"
                                )
                            } else {
                                Aaa3DButton(
                                    text = "VIEW",
                                    onClick = { inspectedSkin = skin },
                                    height = 30.dp,
                                    gradient = Brush.linearGradient(listOf(Color(0xFF475569), Color(0xFF334155))),
                                    textColor = Color.White
                                )
                                Aaa3DButton(
                                    text = "+1 💎 AD",
                                    icon = Icons.Default.PlayArrow,
                                    onClick = onWatchAdForDiamonds,
                                    height = 28.dp,
                                    gradient = DiamondGlowBrush,
                                    textColor = Color(0xFF0F172A)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TableThemesSection(
    economy: PlayerEconomy,
    onEquip: (TableTheme) -> Unit,
    onBuy: (TableTheme) -> Unit,
    onWatchAdForDiamonds: () -> Unit
) {
    var inspectedTheme by remember {
        mutableStateOf(TableTheme.values().firstOrNull { it.id == economy.equippedTableTheme } ?: TableTheme.EMERALD_CASINO)
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. High-End Animated 3D Casino Table Model Showcase
        item {
            AnimatedTableModelShowcase(
                theme = inspectedTheme,
                economy = economy,
                onEquip = { theme ->
                    onEquip(theme)
                },
                onBuy = { theme ->
                    onBuy(theme)
                },
                onWatchAdForDiamonds = onWatchAdForDiamonds
            )
        }

        item {
            Text(
                text = "CASINO FELT COLLECTION",
                color = Color(0xFFFFD700),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )
        }

        items(TableTheme.values()) { theme ->
            val isUnlocked = economy.unlockedTableThemes.contains(theme.id) || theme.priceDiamonds == 0
            val isEquipped = economy.equippedTableTheme == theme.id
            val isInspected = inspectedTheme.id == theme.id

            AaaGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { inspectedTheme = theme },
                borderBrush = if (isEquipped) GoldMetallicBrush else if (isInspected) Brush.linearGradient(listOf(Color(0xFF38BDF8), Color(0xFF0284C7))) else Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF1E293B))),
                glowColor = if (isEquipped) CasinoGold else if (isInspected) Color(0xFF38BDF8) else Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Felt Color Swatch with 3D ring
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(theme.feltGradients.map { Color(it) })
                            )
                            .border(2.dp, CasinoGold, CircleShape)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = theme.title,
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Black
                            )
                            if (isEquipped) {
                                Spacer(modifier = Modifier.width(6.dp))
                                AaaBadge(text = "ACTIVE", color = CasinoEmerald)
                            }
                        }

                        Text(
                            text = theme.description,
                            color = Color(0xFF94A3B8),
                            fontSize = 10.5.sp,
                            lineHeight = 14.sp,
                            modifier = Modifier.padding(vertical = 3.dp)
                        )

                        if (!isUnlocked) {
                            Text(
                                text = "💎 ${theme.priceDiamonds} Diamonds",
                                color = CasinoDiamond,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    if (isUnlocked) {
                        Aaa3DButton(
                            text = if (isEquipped) "ACTIVE" else "USE",
                            onClick = { onEquip(theme) },
                            enabled = !isEquipped,
                            variant = if (isEquipped) ButtonVariant.SECONDARY else ButtonVariant.GOLD,
                            modifier = Modifier.testTag("use_theme_${theme.id}")
                        )
                    } else {
                        Aaa3DButton(
                            text = "UNLOCK",
                            onClick = { onBuy(theme) },
                            variant = ButtonVariant.DIAMOND,
                            modifier = Modifier.testTag("buy_theme_${theme.id}")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestsSection(
    economy: PlayerEconomy,
    onClaimQuest: (String) -> Unit,
    onWatchAd: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            // Level & XP Bar Card
            AaaGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderBrush = GoldMetallicBrush,
                glowColor = CasinoGold
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LEVEL ${economy.level} PROGRESS",
                                color = CasinoGold,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "VIP Status: ${economy.vipTier.title}",
                                color = Color(economy.vipTier.badgeColor),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "${economy.currentXp} / ${economy.xpForNextLevel} XP",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { economy.xpProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = CasinoGold,
                        trackColor = Color(0xFF1E293B)
                    )
                }
            }
        }

        item {
            AaaSectionHeader(
                title = "DAILY OBJECTIVES",
                subtitle = "Complete daily challenges for massive diamond bounty",
                accentColor = CasinoGold
            )
        }

        items(economy.quests) { quest ->
            AaaGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderBrush = if (quest.isCompleted && !quest.isClaimed) GoldMetallicBrush else Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF1E293B))),
                glowColor = if (quest.isCompleted && !quest.isClaimed) CasinoGold else Color.Transparent
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = quest.title,
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = quest.description,
                                color = Color(0xFF94A3B8),
                                fontSize = 10.5.sp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "+${quest.diamondReward} 💎",
                                color = CasinoDiamond,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "+${quest.coinReward} 🪙",
                                color = CasinoGold,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            LinearProgressIndicator(
                                progress = { quest.progressPercent },
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (quest.isCompleted) CasinoEmerald else CasinoDiamond,
                                trackColor = Color(0xFF1E293B)
                            )
                            Text(
                                text = "${quest.current}/${quest.target}",
                                color = Color(0xFF64748B),
                                fontSize = 9.5.sp,
                                modifier = Modifier.padding(top = 3.dp)
                            )
                        }

                        if (quest.isClaimed) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E293B)
                            ) {
                                Text(
                                    text = "CLAIMED ✓",
                                    color = Color(0xFF64748B),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        } else if (quest.isCompleted) {
                            Aaa3DButton(
                                text = "CLAIM",
                                onClick = { onClaimQuest(quest.id) },
                                variant = ButtonVariant.GOLD,
                                modifier = Modifier.testTag("claim_quest_${quest.id}")
                            )
                        } else if (quest.id == "quest_watch_ads") {
                            Aaa3DButton(
                                text = "WATCH AD",
                                onClick = onWatchAd,
                                variant = ButtonVariant.DIAMOND,
                                modifier = Modifier.testTag("quest_watch_ad_btn")
                            )
                        } else {
                            Text(
                                text = "IN PROGRESS",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
