package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.chat.LiveChatMessage
import com.example.ui.theme.CasinoGold
import kotlinx.coroutines.launch

val QUICK_TACTICAL_BANTER = listOf(
    "👑 Nice Trick!",
    "🔥 Double that!",
    "🛡️ Play Safe Vikram!",
    "🎯 Need 16 Points!",
    "🤔 Reveal Trump?",
    "⚡ Single Hand!",
    "👏 Well Played!"
)

/**
 * Floating Table In-Game Chat Overlay & Tactical Banter Drawer.
 * Built for smooth communication during 29 live card tricks without blocking the board.
 */
@Composable
fun TableLiveChatDrawer(
    isOpen: Boolean,
    messages: List<LiveChatMessage>,
    onSendMessage: (String) -> Unit,
    onAddReaction: (String, String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.CenterEnd
    ) {
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(min = 280.dp, max = 340.dp)
                .clickable(enabled = false, onClick = {}) // Prevent clicks leaking
                .shadow(16.dp, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                .border(1.dp, CasinoGold.copy(alpha = 0.4f), RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)),
            shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.98f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Forum,
                            contentDescription = "Chat",
                            tint = CasinoGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TABLE LIVE CHAT",
                            color = CasinoGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 6.dp),
                    thickness = 0.8.dp,
                    color = Color(0xFF334155)
                )

                // Quick Banter Chips Horizontal Bar
                Text(
                    text = "QUICK TACTICAL CALLOUTS",
                    color = Color(0xFF94A3B8),
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(QUICK_TACTICAL_BANTER) { banter ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E293B),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFF475569)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onSendMessage(banter)
                                }
                        ) {
                            Text(
                                text = banter,
                                color = Color(0xFFE2E8F0),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Messages Stream
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(messages) { msg ->
                        ChatMessageBubble(
                            message = msg,
                            onReact = { emoji -> onAddReaction(msg.id, emoji) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Input Box
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        placeholder = { Text("Say something...", color = Color(0xFF64748B), fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CasinoGold,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText)
                                inputText = ""
                                coroutineScope.launch {
                                    if (messages.isNotEmpty()) {
                                        listState.animateScrollToItem(messages.size - 1)
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFD97706))))
                            .testTag("chat_send_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: LiveChatMessage,
    onReact: (String) -> Unit
) {
    if (message.isSystemNotice) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF1E293B).copy(alpha = 0.7f),
            border = androidx.compose.foundation.BorderStroke(0.6.dp, CasinoGold.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(message.senderAvatar, fontSize = 12.sp)
                Text(
                    text = message.text,
                    color = CasinoGold,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 12.sp
                )
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(message.senderName, color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                if (message.emojiReaction != null) {
                    Text(message.emojiReaction, fontSize = 10.sp)
                }
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1E293B),
                border = androidx.compose.foundation.BorderStroke(0.6.dp, Color(0xFF334155)),
                modifier = Modifier.padding(top = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.text,
                        color = Color.White,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}
