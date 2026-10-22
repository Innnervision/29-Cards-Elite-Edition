package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.*
import com.example.game.social.ShareManager
import com.example.ui.components.LuxuryScreenBackground
import com.example.ui.components.luxuryInteractiveScale

enum class FriendsTab(val label: String) {
    FRIENDS("Friends"),
    REQUESTS("Requests"),
    SUGGESTED("Suggested"),
    RECENT("Recent"),
    BLOCKED("Blocked")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsZoneScreen(
    friends: List<SocialUser>,
    incomingRequests: List<FriendRequest>,
    suggestedPlayers: List<SocialUser>,
    recentPlayers: List<SocialUser>,
    blockedUsers: List<BlockedUser>,
    userPlayerId: String,
    userDisplayName: String,
    onAcceptRequest: (String) -> Unit,
    onDeclineRequest: (String) -> Unit,
    onAddFriend: (SocialUser) -> Unit,
    onRemoveFriend: (String) -> Unit,
    onBlockUser: (SocialUser) -> Unit,
    onUnblockUser: (String) -> Unit,
    onInviteToMatch: (SocialUser) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(FriendsTab.FRIENDS) }
    var searchQuery by remember { mutableStateOf("") }
    var reportedUserDialog by remember { mutableStateOf<SocialUser?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "FRIENDS ZONE",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("friends_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = {
                            ShareManager.shareGameInvitation(context, userDisplayName, userPlayerId)
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFD97706),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("share_invite_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Invite via Apps", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        },
        containerColor = Color(0xFF070B14)
    ) { paddingValues ->
        LuxuryScreenBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("friends_search_field"),
                placeholder = { Text("Search by username or Player ID...", color = Color(0xFF64748B), fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFFFD700)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF94A3B8))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFD700),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedContainerColor = Color(0xFF0F172A),
                    unfocusedContainerColor = Color(0xFF0F172A),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            // Tabs Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = Color.Transparent,
                contentColor = Color(0xFFFFD700),
                edgePadding = 0.dp,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                FriendsTab.values().forEach { tab ->
                    val badgeCount = if (tab == FriendsTab.REQUESTS) incomingRequests.size else 0
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    tab.label,
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                                if (badgeCount > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEF4444)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("$badgeCount", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                FriendsTab.FRIENDS -> {
                    val filtered = friends.filter {
                        it.displayName.contains(searchQuery, ignoreCase = true) ||
                                it.username.contains(searchQuery, ignoreCase = true)
                    }
                    val liveFriends = filtered.filter { it.isLive }
                    val offlineFriends = filtered.filter { !it.isLive }

                    if (filtered.isEmpty()) {
                        EmptyListState("No friends yet. Check 'Suggested' to add challengers or invite via WhatsApp!")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Live Now Section Header & Cards
                            if (liveFriends.isNotEmpty()) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF14532D).copy(alpha = 0.4f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.6f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF22C55E))
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "LIVE FRIENDS NOW (${liveFriends.size} PLAYING AT TABLES)",
                                                color = Color(0xFF4ADE80),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }
                                }

                                items(liveFriends, key = { "live_${it.id}" }) { friend ->
                                    FriendCard(
                                        user = friend,
                                        onInvite = { onInviteToMatch(friend) },
                                        onRemove = { onRemoveFriend(friend.id) },
                                        onBlock = { onBlockUser(friend) },
                                        onReport = { reportedUserDialog = friend }
                                    )
                                }

                                item {
                                    Text(
                                        text = "ALL FRIENDS (${offlineFriends.size})",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                                    )
                                }
                            }

                            items(offlineFriends, key = { "offline_${it.id}" }) { friend ->
                                FriendCard(
                                    user = friend,
                                    onInvite = { onInviteToMatch(friend) },
                                    onRemove = { onRemoveFriend(friend.id) },
                                    onBlock = { onBlockUser(friend) },
                                    onReport = { reportedUserDialog = friend }
                                )
                            }
                        }
                    }
                }

                FriendsTab.REQUESTS -> {
                    if (incomingRequests.isEmpty()) {
                        EmptyListState("No pending friend requests at this time.")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(incomingRequests, key = { it.requestId }) { req ->
                                FriendRequestCard(
                                    request = req,
                                    onAccept = { onAcceptRequest(req.requestId) },
                                    onDecline = { onDeclineRequest(req.requestId) }
                                )
                            }
                        }
                    }
                }

                FriendsTab.SUGGESTED -> {
                    val filtered = suggestedPlayers.filter {
                        it.displayName.contains(searchQuery, ignoreCase = true) ||
                                it.username.contains(searchQuery, ignoreCase = true)
                    }
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filtered, key = { it.id }) { user ->
                            SuggestedPlayerCard(
                                user = user,
                                onAddFriend = { onAddFriend(user) }
                            )
                        }
                    }
                }

                FriendsTab.RECENT -> {
                    if (recentPlayers.isEmpty()) {
                        EmptyListState("No recent match opponents yet. Play a Classic 29 or Tournament match!")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(recentPlayers, key = { it.id }) { user ->
                                SuggestedPlayerCard(
                                    user = user,
                                    onAddFriend = { onAddFriend(user) }
                                )
                            }
                        }
                    }
                }

                FriendsTab.BLOCKED -> {
                    if (blockedUsers.isEmpty()) {
                        EmptyListState("No blocked players. Your match atmosphere is clean and serene.")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(blockedUsers, key = { it.userId }) { blocked ->
                                BlockedUserCard(
                                    blocked = blocked,
                                    onUnblock = { onUnblockUser(blocked.userId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

    // Report Dialog
    reportedUserDialog?.let { targetUser ->
        AlertDialog(
            onDismissRequest = { reportedUserDialog = null },
            title = { Text("Report Player", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Submit an anonymous report for ${targetUser.displayName} (${targetUser.username}) for inappropriate behavior or unfair play? Our moderation team reviews all reports within 24 hours.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onBlockUser(targetUser)
                        reportedUserDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Report & Block", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { reportedUserDialog = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun FriendCard(
    user: SocialUser,
    onInvite: () -> Unit,
    onRemove: () -> Unit,
    onBlock: () -> Unit,
    onReport: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with Status indicator
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFB8860B)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👑", fontSize = 20.sp)
                }
                val statusColor = when (user.status) {
                    UserStatus.ONLINE -> Color(0xFF22C55E)
                    UserStatus.IN_MATCH -> Color(0xFFF59E0B)
                    UserStatus.HOSTING_ROOM -> Color(0xFF38BDF8)
                    UserStatus.OFFLINE -> Color(0xFF64748B)
                }
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                        .border(2.dp, Color(0xFF0F172A), CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(user.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(user.vipTitle, color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
                Text("${user.username} • Level ${user.level}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                if (user.isLive) {
                    Text(
                        text = "🟢 ${user.liveActivity}",
                        color = Color(0xFF4ADE80),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "${user.status.label} • ${user.gamesWon} wins (${user.winRate}% win rate)",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp
                    )
                }
            }

            // Dynamic Action Button (Join Room / Spectate / Challenge / Invite)
            FilledTonalButton(
                onClick = onInvite,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = when (user.status) {
                        UserStatus.HOSTING_ROOM -> Color(0xFF0284C7)
                        UserStatus.IN_MATCH -> Color(0xFFD97706)
                        else -> Color(0xFF16A34A)
                    },
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    when (user.status) {
                        UserStatus.HOSTING_ROOM -> Icons.Default.MeetingRoom
                        UserStatus.IN_MATCH -> Icons.Default.Visibility
                        else -> Icons.Default.PlayArrow
                    },
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = when (user.status) {
                        UserStatus.HOSTING_ROOM -> "Join"
                        UserStatus.IN_MATCH -> "Spectate"
                        else -> "Invite"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box {
                IconButton(onClick = { expandedMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = Color(0xFF94A3B8))
                }
                DropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false },
                    modifier = Modifier.background(Color(0xFF1E293B))
                ) {
                    DropdownMenuItem(
                        text = { Text("Remove Friend", color = Color(0xFFCBD5E1)) },
                        onClick = {
                            expandedMenu = false
                            onRemove()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Block Player", color = Color(0xFFF87171)) },
                        onClick = {
                            expandedMenu = false
                            onBlock()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Report User", color = Color(0xFFEF4444)) },
                        onClick = {
                            expandedMenu = false
                            onReport()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FriendRequestCard(
    request: FriendRequest,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp)),
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
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3B82F6)),
                contentAlignment = Alignment.Center
            ) {
                Text("♠️", fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(request.sender.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("${request.sender.username} • Level ${request.sender.level}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Text("Wants to be your 29 card partner", color = Color(0xFFFFD700), fontSize = 10.sp)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onAccept,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Accept", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onDecline,
                    shape = RoundedCornerShape(8.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(Color(0xFF475569), Color(0xFF475569)))),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Decline", color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun SuggestedPlayerCard(
    user: SocialUser,
    onAddFriend: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)),
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
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8B5CF6)),
                contentAlignment = Alignment.Center
            ) {
                Text("💎", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(user.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("${user.username} • Level ${user.level} • ${user.vipTitle}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Text("${user.gamesWon} wins • ${user.winRate}% win rate", color = Color(0xFF64748B), fontSize = 10.sp)
            }

            Button(
                onClick = onAddFriend,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun BlockedUserCard(
    blocked: BlockedUser,
    onUnblock: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Block, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(blocked.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(blocked.username, color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
            OutlinedButton(
                onClick = onUnblock,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700))
            ) {
                Text("Unblock", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun EmptyListState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = Color(0xFF64748B),
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}
