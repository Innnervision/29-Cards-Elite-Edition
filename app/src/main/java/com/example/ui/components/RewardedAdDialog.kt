package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.game.model.ActiveAdSession

@Composable
fun RewardedAdDialog(
    adSession: ActiveAdSession,
    onClaimReward: () -> Unit,
    onCloseAd: () -> Unit
) {
    Dialog(
        onDismissRequest = {
            if (adSession.isRewardClaimable || adSession.isInterstitial) {
                onCloseAd()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = adSession.isRewardClaimable || adSession.isInterstitial,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "ad_pulse")
        val glowScale by infiniteTransition.animateFloat(
            initialValue = 0.98f,
            targetValue = 1.02f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF030712).copy(alpha = 0.97f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.92f)
                    .shadow(24.dp, RoundedCornerShape(20.dp))
                    .border(
                        1.5.dp,
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFFFD700).copy(alpha = 0.8f),
                                Color(0xFF38BDF8).copy(alpha = 0.5f),
                                Color(0xFF1E293B)
                            )
                        ),
                        RoundedCornerShape(20.dp)
                    ),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0B0F19)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top Bar: Ad indicator, timer, and close / skip button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFFFD700).copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700))
                            ) {
                                Text(
                                    text = if (adSession.isInterstitial) "SPONSORED AD" else "REWARDED VIDEO AD",
                                    color = Color(0xFFFFD700),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            if (!adSession.isRewardClaimable) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "Timer",
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Reward in ${adSession.currentRemainingSec}s",
                                        color = Color(0xFFE2E8F0),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Complete",
                                        tint = Color(0xFF22C55E),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Reward Ready!",
                                        color = Color(0xFF4ADE80),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        // Close Button (Only active when completed or skip allowed)
                        if (adSession.isRewardClaimable || adSession.isInterstitial) {
                            IconButton(
                                onClick = {
                                    if (adSession.isRewardClaimable) onClaimReward() else onCloseAd()
                                },
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color(0xFF334155), CircleShape)
                                    .testTag("close_ad_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Ad",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF1E293B)
                            ) {
                                Text(
                                    text = "${adSession.currentRemainingSec}s",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Linear Ad Progress Bar
                    val progress = 1.0f - (adSession.currentRemainingSec.toFloat() / adSession.totalDurationSec.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp),
                        color = Color(0xFF38BDF8),
                        trackColor = Color(0xFF1E293B),
                    )

                    // Video / Creative Area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_ad_game_promo),
                            contentDescription = adSession.adCreativeTitle,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Vignette & Overlays
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent,
                                            Color.Transparent,
                                            Color(0xFF030712).copy(alpha = 0.95f)
                                        )
                                    )
                                )
                        )

                        // Ad Badge & CTA at bottom of video
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFEF4444)
                                ) {
                                    Text(
                                        text = "HOT 🔥 5★ TOP RATED",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "10M+ Downloads",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = adSession.adCreativeTitle,
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = adSession.adCreativeTagline,
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                maxLines = 2,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    // Bottom Action & Reward Delivery Banner
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF0F172A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (!adSession.isRewardClaimable) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Reward for watching:",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 10.5.sp
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("💎", fontSize = 16.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "+${adSession.placement.rewardDiamonds} Diamonds & +${adSession.placement.rewardCoins} Coins",
                                                color = Color(0xFF38BDF8),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { /* Install demo action */ },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "PLAY FREE",
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            } else {
                                // Claim Reward Mode
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "🎉 AD COMPLETED!",
                                        color = Color(0xFFFFD700),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("💎", fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "+${adSession.placement.rewardDiamonds} DIAMONDS & +${adSession.placement.rewardCoins} COINS",
                                            color = Color(0xFF38BDF8),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = onClaimReward,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(44.dp)
                                            .testTag("claim_ad_reward_button"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                                    ) {
                                        Text(
                                            text = "CLAIM REWARD NOW 💎",
                                            color = Color(0xFF0F172A),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
