package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.auth.AccountType
import com.example.game.auth.AuthSession
import com.example.game.auth.CloudSyncStatus
import com.example.game.model.AchievementBadge
import com.example.game.model.ProfilePrivacy
import com.example.game.model.UserProfile
import com.example.ui.components.LuxuryScreenBackground
import com.example.ui.components.luxuryInteractiveScale

val AVATAR_PRESETS = listOf(
    "avatar_player" to "👑",
    "avatar_king" to "🤴",
    "avatar_queen" to "👸",
    "avatar_tiger" to "🐯",
    "avatar_diamond" to "💎",
    "avatar_ace" to "♠️"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerProfileScreen(
    profile: UserProfile,
    authSession: AuthSession,
    cloudSyncStatus: CloudSyncStatus,
    onUpdateProfile: (UserProfile) -> Unit,
    onLinkGoogle: (String, String) -> Unit,
    onLinkEmail: (String) -> Unit,
    onLinkFacebook: (String) -> Unit = {},
    onLinkInstagram: (String) -> Unit = {},
    onLinkPhone: (String) -> Unit = {},
    onRestorePreviousGame: (String, String, (Boolean, String) -> Unit) -> Unit = { _, _, _ -> },
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var isEditingBio by remember { mutableStateOf(false) }
    var editedBio by remember { mutableStateOf(profile.bio) }
    var showAvatarPicker by remember { mutableStateOf(false) }
    var showAccountLinkDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreIdentifier by remember { mutableStateOf("") }
    var restoreAuthMethod by remember { mutableStateOf("Email") }
    var isRestoring by remember { mutableStateOf(false) }
    var restoreResultMessage by remember { mutableStateOf<String?>(null) }

    // Dialog states for individual social connects
    var activeSocialDialog by remember { mutableStateOf<String?>(null) } // "FACEBOOK", "INSTAGRAM", "PHONE", "EMAIL"
    var socialInputText by remember { mutableStateOf("") }
    var otpInputText by remember { mutableStateOf("") }
    var isOtpStep by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "PLAYER PROFILE",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("profile_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        containerColor = Color(0xFF070B14)
    ) { paddingValues ->
        LuxuryScreenBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
            // Header Card with Avatar, Name, Handle, Bio & Cloud Status
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .border(1.dp, Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFB8860B))), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFD97706))))
                                    .clickable { showAvatarPicker = true }
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val emoji = AVATAR_PRESETS.firstOrNull { it.first == profile.avatarId }?.second ?: "👑"
                                Text(emoji, fontSize = 32.sp)
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(profile.displayName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                Text("@${profile.username}", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                
                                // Player ID & Copy
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text("ID: ${profile.playerId}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy ID",
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable {
                                                clipboardManager.setText(AnnotatedString(profile.playerId))
                                                Toast.makeText(context, "Player ID copied to clipboard", Toast.LENGTH_SHORT).show()
                                            }
                                    )
                                }
                            }

                            // Cloud Account Link Button / Status
                            FilledTonalButton(
                                onClick = { showAccountLinkDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (authSession.isLinked) Color(0xFF16A34A) else Color(0xFF0284C7),
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    if (authSession.isLinked) Icons.Default.CloudDone else Icons.Default.CloudSync,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (authSession.isLinked) "Linked" else "Link Account",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Bio
                        if (isEditingBio) {
                            OutlinedTextField(
                                value = editedBio,
                                onValueChange = { editedBio = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Edit Bio", color = Color(0xFFFFD700)) },
                                singleLine = false,
                                maxLines = 3,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFFD700),
                                    unfocusedBorderColor = Color(0xFF475569),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { isEditingBio = false }) {
                                    Text("Cancel", color = Color(0xFF94A3B8))
                                }
                                Button(
                                    onClick = {
                                        onUpdateProfile(profile.copy(bio = editedBio))
                                        isEditingBio = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                                ) {
                                    Text("Save", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    profile.bio,
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { isEditingBio = true }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Bio", tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Restore Previous Game & Cloud Vault Action Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.2.dp, Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFF38BDF8))), RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("☁️", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "RETURNING PLAYER RESTORE",
                                    color = Color(0xFFFFD700),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    "Switching device or coming back? Authenticate with FB, Insta, Email, or Phone to retrieve your previous matches, diamonds, and game state.",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.5.sp,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                restoreResultMessage = null
                                showRestoreDialog = true
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("authenticate_restore_game_button")
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SIGN IN & RESTORE PROGRESS", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            // Connected Social Channels & Accounts
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "CONNECTED ACCOUNTS & RECOVERY",
                        color = Color(0xFFFFD700),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        "Cloud Sync Active",
                        color = Color(0xFF38BDF8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Facebook Card
                    SocialChannelCard(
                        title = "Facebook",
                        emoji = "📘",
                        brandColor = Color(0xFF1877F2),
                        handle = profile.facebookHandle,
                        isLinked = profile.isFacebookLinked,
                        onConnectClick = {
                            socialInputText = profile.facebookHandle?.removePrefix("@") ?: ""
                            activeSocialDialog = "FACEBOOK"
                        }
                    )

                    // Instagram Card
                    SocialChannelCard(
                        title = "Instagram",
                        emoji = "📸",
                        brandColor = Color(0xFFE4405F),
                        handle = profile.instagramHandle,
                        isLinked = profile.isInstagramLinked,
                        onConnectClick = {
                            socialInputText = profile.instagramHandle?.removePrefix("@") ?: ""
                            activeSocialDialog = "INSTAGRAM"
                        }
                    )

                    // Email Address Card
                    SocialChannelCard(
                        title = "Google Account & Email",
                        emoji = "✉️",
                        brandColor = Color(0xFF0284C7),
                        handle = profile.email,
                        isLinked = profile.isEmailLinked,
                        onConnectClick = {
                            socialInputText = profile.email ?: ""
                            activeSocialDialog = "EMAIL"
                        }
                    )

                    // Phone Number Card
                    SocialChannelCard(
                        title = "Mobile Phone Number",
                        emoji = "📱",
                        brandColor = Color(0xFF16A34A),
                        handle = profile.phoneNumber,
                        isLinked = profile.isPhoneLinked,
                        onConnectClick = {
                            socialInputText = profile.phoneNumber ?: ""
                            isOtpStep = false
                            activeSocialDialog = "PHONE"
                        }
                    )
                }
            }

            // Career Statistics Grid
            item {
                Text(
                    "CAREER STATISTICS",
                    color = Color(0xFFFFD700),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("Matches Played", "${profile.gamesPlayed}", "🏆", Modifier.weight(1f))
                    StatCard("Win Rate", "${profile.winRatePercent}%", "📈", Modifier.weight(1f))
                    StatCard("Win Streak", "${profile.winStreak} (Best: ${profile.bestWinStreak})", "🔥", Modifier.weight(1f))
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("29 Trick Points", "${profile.total29TrickPoints}", "🎯", Modifier.weight(1f))
                    StatCard("Pairs Declared", "${profile.royaltyPairsDeclared}", "👑", Modifier.weight(1f))
                    StatCard("Single Hand Wins", "${profile.singleHandVictories}", "⚡", Modifier.weight(1f))
                }
            }

            // Achievements Showcase
            item {
                Text(
                    "ACHIEVEMENTS & BADGES",
                    color = Color(0xFFFFD700),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            items(profile.achievements) { badge ->
                AchievementCard(badge)
            }

            // Privacy Settings
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Profile Privacy", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ProfilePrivacy.values().forEach { privacy ->
                                val isSelected = profile.privacy == privacy
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFFD97706) else Color(0xFF1E293B))
                                        .clickable { onUpdateProfile(profile.copy(privacy = privacy)) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        privacy.title,
                                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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

    // Avatar Picker Dialog
    if (showAvatarPicker) {
        AlertDialog(
            onDismissRequest = { showAvatarPicker = false },
            title = { Text("Choose Avatar", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AVATAR_PRESETS.forEach { (id, emoji) ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (profile.avatarId == id) Color(0xFFFFD700) else Color(0xFF1E293B))
                                .clickable {
                                    onUpdateProfile(profile.copy(avatarId = id))
                                    showAvatarPicker = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 24.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAvatarPicker = false }) {
                    Text("Close", color = Color(0xFFFFD700))
                }
            },
            containerColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Account Link Dialog
    if (showAccountLinkDialog) {
        var emailInput by remember { mutableStateOf(authSession.email ?: "") }

        AlertDialog(
            onDismissRequest = { showAccountLinkDialog = false },
            title = { Text("Cloud Account Vault", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Link your profile to preserve game data, friends, and high roller VIP status across devices and prevent data loss.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Google Play / Google Account
                    Button(
                        onClick = {
                            onLinkGoogle("player@gmail.com", "goog_${(1000..9999).random()}")
                            showAccountLinkDialog = false
                            Toast.makeText(context, "Google Account Linked!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Link Google Account", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Or Email Link
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter your email address...", color = Color(0xFF64748B), fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color(0xFF475569),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            if (emailInput.contains("@")) {
                                onLinkEmail(emailInput)
                                showAccountLinkDialog = false
                                Toast.makeText(context, "Account linked to $emailInput", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Link Email", color = Color(0xFFFFD700))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAccountLinkDialog = false }) {
                    Text("Close", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Individual Social Channel Connect Dialog (Facebook, Instagram, Phone, Email)
    if (activeSocialDialog != null) {
        val dialogType = activeSocialDialog!!
        AlertDialog(
            onDismissRequest = {
                activeSocialDialog = null
                isOtpStep = false
            },
            title = {
                Text(
                    text = when (dialogType) {
                        "FACEBOOK" -> "Connect Facebook"
                        "INSTAGRAM" -> "Connect Instagram"
                        "PHONE" -> if (isOtpStep) "Verify SMS OTP Code" else "Connect Mobile Phone"
                        else -> "Connect Email Address"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = when (dialogType) {
                            "FACEBOOK" -> "Enter your Facebook profile handle or username to link and secure your account."
                            "INSTAGRAM" -> "Enter your Instagram username (@handle) to connect and showcase on your 29 card player profile."
                            "PHONE" -> if (isOtpStep) "Enter the 6-digit verification code sent to $socialInputText (e.g. 749281)" else "Enter your mobile phone number with country code (e.g. +91 9876543210) for instant SMS verification."
                            else -> "Enter your email address to sync cross-game stats and restore games on any Android device."
                        },
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.5.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (dialogType == "PHONE" && isOtpStep) {
                        OutlinedTextField(
                            value = otpInputText,
                            onValueChange = { if (it.length <= 6) otpInputText = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("6-digit code (e.g. 749281)", color = Color(0xFF64748B)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    } else {
                        OutlinedTextField(
                            value = socialInputText,
                            onValueChange = { socialInputText = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    when (dialogType) {
                                        "FACEBOOK" -> "facebook.com/username or @handle"
                                        "INSTAGRAM" -> "@instagram_handle"
                                        "PHONE" -> "+91 98765 43210"
                                        else -> "your_email@gmail.com"
                                    },
                                    color = Color(0xFF64748B),
                                    fontSize = 12.sp
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (dialogType) {
                            "FACEBOOK" -> {
                                if (socialInputText.isNotBlank()) {
                                    onLinkFacebook(socialInputText)
                                    activeSocialDialog = null
                                    Toast.makeText(context, "Facebook connected!", Toast.LENGTH_SHORT).show()
                                }
                            }
                            "INSTAGRAM" -> {
                                if (socialInputText.isNotBlank()) {
                                    onLinkInstagram(socialInputText)
                                    activeSocialDialog = null
                                    Toast.makeText(context, "Instagram connected!", Toast.LENGTH_SHORT).show()
                                }
                            }
                            "EMAIL" -> {
                                if (socialInputText.contains("@")) {
                                    onLinkEmail(socialInputText)
                                    activeSocialDialog = null
                                    Toast.makeText(context, "Email linked successfully!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                                }
                            }
                            "PHONE" -> {
                                if (!isOtpStep) {
                                    if (socialInputText.length >= 8) {
                                        isOtpStep = true
                                        otpInputText = "749281" // Pre-filled realistic OTP simulator
                                        Toast.makeText(context, "SMS OTP sent to $socialInputText", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Enter valid phone number", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    onLinkPhone(socialInputText)
                                    activeSocialDialog = null
                                    isOtpStep = false
                                    Toast.makeText(context, "Mobile number verified & linked!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                ) {
                    Text(
                        if (dialogType == "PHONE" && !isOtpStep) "Send OTP" else "Confirm & Link",
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    activeSocialDialog = null
                    isOtpStep = false
                }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Authenticate & Restore Previous Game Dialog
    if (showRestoreDialog) {
        val authMethods = listOf("Email", "Phone", "Facebook", "Instagram", "Player ID")

        AlertDialog(
            onDismissRequest = {
                if (!isRestoring) showRestoreDialog = false
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restore Previous Game", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text(
                        "Sign in using your connected credential to restore previous match statistics, trophies, and profile settings.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.5.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("SELECT AUTHENTICATION METHOD:", color = Color(0xFFFFD700), fontSize = 9.sp, fontWeight = FontWeight.Black)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        authMethods.forEach { method ->
                            val isSelected = restoreAuthMethod == method
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { restoreAuthMethod = method }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        method,
                                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = restoreIdentifier,
                        onValueChange = { restoreIdentifier = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("restore_identifier_input"),
                        placeholder = {
                            Text(
                                when (restoreAuthMethod) {
                                    "Phone" -> "Enter registered phone number..."
                                    "Email" -> "Enter registered email address..."
                                    "Facebook" -> "Enter Facebook handle or ID..."
                                    "Instagram" -> "Enter Instagram username..."
                                    else -> "Enter 6-digit Player ID..."
                                },
                                color = Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color(0xFF475569),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    if (restoreResultMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = restoreResultMessage!!,
                            color = if (restoreResultMessage!!.contains("Welcome back") || restoreResultMessage!!.contains("Authenticated")) Color(0xFF4ADE80) else Color(0xFFF87171),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (isRestoring) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color(0xFFFFD700), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Restoring your game progress...", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (restoreIdentifier.isNotBlank() && !isRestoring) {
                            isRestoring = true
                            restoreResultMessage = null
                            onRestorePreviousGame(restoreIdentifier, restoreAuthMethod) { success, msg ->
                                isRestoring = false
                                restoreResultMessage = msg
                                if (success) {
                                    Toast.makeText(context, "Previous game restored successfully!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    enabled = !isRestoring && restoreIdentifier.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("Authenticate & Restore", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Close", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun SocialChannelCard(
    title: String,
    emoji: String,
    brandColor: Color,
    handle: String?,
    isLinked: Boolean,
    onConnectClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isLinked) brandColor.copy(alpha = 0.6f) else Color(0xFF1E293B), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(brandColor.copy(alpha = 0.2f))
                    .border(1.2.dp, brandColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    if (isLinked) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF16A34A).copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(0.6.dp, Color(0xFF22C55E))
                        ) {
                            Text(
                                "LINKED",
                                color = Color(0xFF22C55E),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = if (isLinked && !handle.isNullOrBlank()) handle else "Not connected",
                    color = if (isLinked) Color(0xFFFFD700) else Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontWeight = if (isLinked) FontWeight.SemiBold else FontWeight.Normal
                )
            }

            Button(
                onClick = onConnectClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLinked) Color(0xFF1E293B) else brandColor
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = if (isLinked) "Edit" else "Connect",
                    fontSize = 11.sp,
                    color = if (isLinked) Color(0xFFCBD5E1) else Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, emoji: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 14.sp)
            Text(label, color = Color(0xFF94A3B8), fontSize = 10.sp)
        }
    }
}

@Composable
fun AchievementCard(badge: AchievementBadge) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (badge.isUnlocked) Color(0xFFFFD700).copy(alpha = 0.6f) else Color(0xFF1E293B),
                shape = RoundedCornerShape(10.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (badge.isUnlocked) Color(0xFFFFD700) else Color(0xFF334155)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (badge.isUnlocked) "🏆" else "🔒",
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(badge.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    if (badge.isUnlocked) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("COMPLETED", color = Color(0xFF22C55E), fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                }
                Text(badge.description, color = Color(0xFF94A3B8), fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { (badge.currentProgress.toFloat() / badge.targetProgress).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color(0xFFFFD700),
                    trackColor = Color(0xFF334155),
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                "${badge.currentProgress}/${badge.targetProgress}",
                color = Color(0xFFCBD5E1),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
