package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.TwentyNineViewModel
import com.example.game.model.LinkedSocialAccount
import com.example.game.model.SocialPlatform

@Composable
fun SocialAccountsAndShareModal(
    viewModel: TwentyNineViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val economy by viewModel.playerEconomy.collectAsState()
    val linkedAccounts by viewModel.linkedSocialAccounts.collectAsState()
    val referralCount by viewModel.referralCount.collectAsState()
    val userInviteCode = viewModel.userReferralCode

    var friendCodeInput by remember { mutableStateOf("") }
    var editingPlatform by remember { mutableStateOf<SocialPlatform?>(null) }
    var inputHandle by remember { mutableStateOf("") }
    var rewardEarnedNotice by remember { mutableStateOf<Pair<String, Int>?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .shadow(28.dp, RoundedCornerShape(22.dp))
                .border(
                    2.dp,
                    Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFD97706), Color(0xFF2563EB))),
                    RoundedCornerShape(22.dp)
                ),
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF0B132B)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SHARE & EARN 💎 VAULT",
                            color = Color(0xFFFFD700),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Link your social channels & earn free diamonds on every share!",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }

                    // Balance Pill
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("💎", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "${economy.diamonds}",
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Reward Banner Alert or Daily Status
                val claimedToday = viewModel.hasClaimedDailyShareToday()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = if (claimedToday) Color(0xFF1E293B) else Color(0xFF065F46)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (claimedToday) Color(0xFF475569) else Color(0xFFFFD700))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (claimedToday) "✅" else "💎", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (claimedToday) "Daily Share Reward Claimed" else "Earn +1 Free Diamond Today!",
                                color = if (claimedToday) Color(0xFF94A3B8) else Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (claimedToday) "You've already received your 💎 reward today. Come back tomorrow!" else "Share on any social platform once today to claim your free diamond!",
                                color = Color(0xFFE2E8F0),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = rewardEarnedNotice != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    rewardEarnedNotice?.let { (platformName, diamonds) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF047857)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🎉", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (diamonds > 0) "+$diamonds Diamond Claimed Successfully!" else "Shared Successfully!",
                                        color = Color(0xFFFFD700),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = if (diamonds > 0) "Thanks for sharing on $platformName!" else "Daily reward already claimed today. Thanks for spreading the word!",
                                        color = Color(0xFFD1FAE5),
                                        fontSize = 10.sp
                                    )
                                }
                                TextButton(onClick = { rewardEarnedNotice = null }) {
                                    Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // List of Sharing & Social Referral Sections
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Direct Universal Share Card (Facebook, WhatsApp, Instagram, Telegram, etc.)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                            border = androidx.compose.foundation.BorderStroke(
                                1.2.dp,
                                Brush.horizontalGradient(listOf(Color(0xFFEC4899), Color(0xFF8B5CF6), Color(0xFF38BDF8)))
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "🚀 DIRECT SHARE TO ALL APPS",
                                            color = Color(0xFFFFD700),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            "Opens Facebook, WhatsApp, Instagram, SMS & more installed apps to share directly with friends!",
                                            color = Color(0xFFCBD5E1),
                                            fontSize = 10.sp,
                                            lineHeight = 13.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Aaa3DButton(
                                        text = "SHARE NOW",
                                        onClick = {
                                            performSocialShare(
                                                context = context,
                                                inviteCode = userInviteCode,
                                                platform = SocialPlatform.FACEBOOK,
                                                onSuccess = {
                                                    val granted = viewModel.recordSocialShare(SocialPlatform.FACEBOOK)
                                                    rewardEarnedNotice = Pair("Shared to Apps", if (granted) 1 else 0)
                                                    if (granted) {
                                                        Toast.makeText(context, "+1 💎 Diamond earned for sharing!", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "Shared! (Daily diamond already claimed)", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            )
                                        },
                                        gradient = Brush.horizontalGradient(listOf(Color(0xFFEC4899), Color(0xFF8B5CF6))),
                                        textColor = Color.White,
                                        height = 28.dp,
                                        horizontalPadding = 10.dp,
                                        verticalPadding = 3.dp,
                                        fontSize = 10.sp,
                                        testTag = "universal_share_btn"
                                    )
                                }
                            }
                        }
                    }

                    // 2. Personal Invite Code Card with 1-Tap Copy
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("YOUR SHAREABLE REFERRAL CODE", color = Color(0xFF94A3B8), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                    Text(userInviteCode, color = Color(0xFFFFD700), fontSize = 15.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(userInviteCode))
                                            Toast.makeText(context, "Copied code: $userInviteCode", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("📋 COPY", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Aaa3DButton(
                                        text = "INVITE",
                                        onClick = {
                                            performSocialShare(
                                                context = context,
                                                inviteCode = userInviteCode,
                                                platform = SocialPlatform.WHATSAPP,
                                                onSuccess = {
                                                    viewModel.recordSocialShare(SocialPlatform.WHATSAPP)
                                                }
                                            )
                                        },
                                        variant = ButtonVariant.GLASS_GOLD,
                                        isTranslucent = true,
                                        height = 28.dp,
                                        horizontalPadding = 8.dp,
                                        verticalPadding = 2.dp,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    // 3. New User Download & Referral Bounty Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B).copy(alpha = 0.45f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.7f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("👥 NEW USER DOWNLOAD BOUNTY", color = Color(0xFF34D399), fontSize = 10.5.sp, fontWeight = FontWeight.Black)
                                    Text("When a new player downloads via your link or code: you get +10,000 🪙 & +5 💎!", color = Color(0xFFE2E8F0), fontSize = 9.5.sp)
                                    Text("Friends Joined: $referralCount  •  Coins Earned: ${referralCount * 10000} 🪙", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Aaa3DButton(
                                    text = "CLAIM (+10K 🪙)",
                                    onClick = {
                                        viewModel.recordReferralInstall()
                                        Toast.makeText(context, "New user joined! +10,000 Coins & +5 Diamonds claimed!", Toast.LENGTH_SHORT).show()
                                    },
                                    variant = ButtonVariant.EMERALD,
                                    height = 28.dp,
                                    horizontalPadding = 8.dp,
                                    verticalPadding = 2.dp,
                                    fontSize = 9.5.sp,
                                    testTag = "test_friend_install_btn"
                                )
                            }
                        }
                    }

                    // 4. Redeem Friend's Code Row
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = friendCodeInput,
                                onValueChange = { friendCodeInput = it.uppercase() },
                                placeholder = { Text("Enter friend's referral code", fontSize = 10.5.sp, color = Color(0xFF64748B)) },
                                singleLine = true,
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFFFD700),
                                    unfocusedBorderColor = Color(0xFF475569)
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Aaa3DButton(
                                text = "REDEEM (+5K 🪙)",
                                onClick = {
                                    val (success, msg) = viewModel.redeemReferralCode(friendCodeInput)
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    if (success) friendCodeInput = ""
                                },
                                variant = ButtonVariant.GOLD,
                                height = 34.dp,
                                horizontalPadding = 10.dp,
                                verticalPadding = 3.dp,
                                fontSize = 10.sp,
                                testTag = "redeem_friend_code_btn"
                            )
                        }
                    }

                    item {
                        Text(
                            text = "SHARE ON SPECIFIC CHANNELS (+1 💎 EACH)",
                            color = Color(0xFF94A3B8),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    items(SocialPlatform.values()) { platform ->
                        val linked = linkedAccounts.find { it.platform == platform }
                        SocialPlatformCard(
                            platform = platform,
                            linkedAccount = linked,
                            onLinkClick = {
                                editingPlatform = platform
                                inputHandle = linked?.handleOrUrl ?: ""
                            },
                            onShareClick = {
                                performSocialShare(
                                    context = context,
                                    inviteCode = userInviteCode,
                                    platform = platform,
                                    onSuccess = {
                                        val granted = viewModel.recordSocialShare(platform)
                                        rewardEarnedNotice = Pair(platform.title, if (granted) 1 else 0)
                                        if (granted) {
                                            Toast.makeText(context, "+1 💎 Diamond claimed for sharing on ${platform.title}!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Shared on ${platform.title}! (Daily reward already claimed today)", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Close Button
                LuxuryActionButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().testTag("close_social_modal")
                ) {
                    Text("DONE", color = Color(0xFF0F172A), fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }
    }

    // Link Account Dialog
    editingPlatform?.let { platform ->
        AlertDialog(
            onDismissRequest = { editingPlatform = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(platform.iconEmoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Link ${platform.title} Channel", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text(
                        "Add your ${platform.title} handle or profile link to connect your gaming identity and receive an instant bonus of +15 💎 Diamonds!",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputHandle,
                        onValueChange = { inputHandle = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(platform.defaultHandlePrompt, color = Color(0xFF64748B), fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color(0xFF475569),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputHandle.isNotBlank()) {
                            viewModel.linkSocialAccount(platform, inputHandle.trim())
                            editingPlatform = null
                            rewardEarnedNotice = Pair(platform.title, 15)
                            Toast.makeText(context, "${platform.title} Linked! +15 💎 Diamonds Bonus!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                ) {
                    Text("Save & Claim +15 💎", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPlatform = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun SocialPlatformCard(
    platform: SocialPlatform,
    linkedAccount: LinkedSocialAccount?,
    onLinkClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(platform.brandColor).copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D38)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Platform Icon & Name
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(platform.brandColor).copy(alpha = 0.25f))
                        .border(1.dp, Color(platform.brandColor), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(platform.iconEmoji, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        platform.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (linkedAccount != null) {
                        Text(
                            linkedAccount.handleOrUrl,
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    } else {
                        Text(
                            "Not linked (+15 💎 on link)",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    }
                }

                // Link / Edit Button
                OutlinedButton(
                    onClick = onLinkClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (linkedAccount != null) Color(0xFF38BDF8) else Color(0xFFFFD700)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (linkedAccount != null) Color(0xFF38BDF8) else Color(0xFFFFD700)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        if (linkedAccount != null) "Edit" else "Link +15 💎",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Share Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (linkedAccount != null && linkedAccount.totalSharesCount > 0)
                        "Shared ${linkedAccount.totalSharesCount} times • +${linkedAccount.totalDiamondsEarned} 💎 earned"
                    else "Share publicly to earn +${platform.diamondRewardPerShare} 💎",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )

                Button(
                    onClick = onShareClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(platform.brandColor).copy(alpha = 0.9f),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Share & Get +${platform.diamondRewardPerShare} 💎",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

/**
 * Triggers native Android share sheet with universal app chooser (Facebook, WhatsApp, Instagram, SMS, etc.).
 */
private fun performSocialShare(
    context: Context,
    inviteCode: String,
    platform: SocialPlatform,
    onSuccess: () -> Unit
) {
    val shareText = "🃏 Playing Royal 29 Card Game Grandmaster! Authentic Indian 29 with 24K gold cards & multiplayer. Download now & use my invite code $inviteCode to claim 5,000 bonus coins & diamonds! 👑"
    val shareUrl = "https://play.google.com/store/apps/details?id=${context.packageName}&referrer=$inviteCode"

    try {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Royal 29 Card Game Challenge")
            putExtra(Intent.EXTRA_TEXT, "$shareText\n$shareUrl")
        }

        val shareChooser = Intent.createChooser(sendIntent, "Share 29 Card Game via ${platform.title} or other apps...")
        shareChooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareChooser)

        onSuccess()
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to open share sheet. Please try again.", Toast.LENGTH_SHORT).show()
    }
}
