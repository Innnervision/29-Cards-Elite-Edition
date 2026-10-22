package com.example.ui.components

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.game.ads.AdConfig
import com.example.game.model.PlayerEconomy
import com.example.ui.theme.CasinoDiamond
import com.example.ui.theme.CasinoGold

/**
 * Modal displayed when a player attempts to unlock an adventure level or purchase
 * an item with insufficient diamonds, giving them an instant Rewarded Ad shortcut.
 */
@Composable
fun InsufficientDiamondsModal(
    requiredDiamonds: Int,
    currentDiamonds: Int,
    itemName: String = "Locked Content",
    onWatchAd: () -> Unit,
    onDismiss: () -> Unit
) {
    val needed = (requiredDiamonds - currentDiamonds).coerceAtLeast(1)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .wrapContentHeight()
                .shadow(24.dp, RoundedCornerShape(20.dp))
                .border(1.5.dp, Brush.horizontalGradient(listOf(Color(0xFF38BDF8), Color(0xFF818CF8))), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AaaBadge(text = "DIAMOND SHORTFALL", color = CasinoDiamond)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Diamond Icon Graphic
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0284C7).copy(alpha = 0.2f))
                        .border(1.5.dp, Color(0xFF38BDF8), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💎", fontSize = 32.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "🔒 NOT ENOUGH DIAMONDS",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "To unlock $itemName, you need $requiredDiamonds Diamonds.\nYou currently have $currentDiamonds ($needed more needed).",
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Balance Comparison Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("CURRENT BALANCE", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("💎 $currentDiamonds Diamonds", color = CasinoDiamond, fontSize = 13.sp, fontWeight = FontWeight.Black)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("COST", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("💎 $requiredDiamonds Diamonds", color = CasinoGold, fontSize = 13.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Primary Watch Ad CTA
                Aaa3DButton(
                    text = "WATCH AD (+1 💎)",
                    icon = Icons.Default.PlayArrow,
                    variant = ButtonVariant.EMERALD,
                    height = 44.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_watch_ad_insufficient_diamonds"),
                    onClick = {
                        onWatchAd()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("CANCEL", color = Color(0xFF94A3B8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Dedicated modal for earning free diamonds via Rewarded Video Ads.
 */
@Composable
fun FreeDiamondsModal(
    economy: PlayerEconomy,
    onWatchAd: (Activity) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var rewardEarnedNotice by remember { mutableStateOf(false) }
    val remainingToday = (AdConfig.MAX_DIAMOND_REWARDED_ADS_PER_DAY - economy.dailyAdsWatched).coerceAtLeast(0)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .wrapContentHeight()
                .shadow(28.dp, RoundedCornerShape(22.dp))
                .border(2.dp, Brush.horizontalGradient(listOf(CasinoDiamond, CasinoGold)), RoundedCornerShape(22.dp)),
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF0B1120)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AaaBadge(text = "REWARDED AD SPONSOR", color = CasinoDiamond)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Diamond Vault Badge
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0369A1).copy(alpha = 0.25f))
                        .border(2.dp, CasinoDiamond, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💎", fontSize = 34.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "GET FREE DIAMONDS",
                    color = CasinoGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )

                Text(
                    text = "Watch a sponsor video and claim an instant diamond.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.5.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Reward Breakdown Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF131D31),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💎", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("REWARD", color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("+1 Diamond", color = CasinoDiamond, fontSize = 12.sp, fontWeight = FontWeight.Black)
                            }
                        }

                        Divider(
                            modifier = Modifier
                                .height(24.dp)
                                .width(1.dp),
                            color = Color(0xFF334155)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🪙", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("BONUS", color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("+250 Coins", color = CasinoGold, fontSize = 12.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Daily Cap Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Daily Remaining: $remainingToday / ${AdConfig.MAX_DIAMOND_REWARDED_ADS_PER_DAY}",
                        color = if (remainingToday > 0) Color(0xFF94A3B8) else Color(0xFFEF4444),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Your Balance: 💎 ${economy.diamonds}",
                        color = CasinoDiamond,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (remainingToday > 0) {
                    Aaa3DButton(
                        text = "WATCH AD (+1 💎)",
                        icon = Icons.Default.PlayArrow,
                        variant = ButtonVariant.GOLD,
                        height = 44.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_watch_free_diamond_ad"),
                        onClick = {
                            if (activity != null) {
                                onWatchAd(activity)
                            }
                        }
                    )
                } else {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "DAILY CAP REACHED • Resets Tomorrow",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onDismiss, modifier = Modifier.height(34.dp)) {
                    Text("CLOSE", color = Color(0xFF94A3B8), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
