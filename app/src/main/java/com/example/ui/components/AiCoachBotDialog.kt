package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CasinoGold
import kotlinx.coroutines.launch

data class BotChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "bot" or "user"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun AiCoachBotDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onAskGeminiQuery: (suspend (String) -> String)? = null
) {
    if (!isOpen) return

    val coroutineScope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf(0) } // 0 = Doubts (Chat), 1 = Rules Guide
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val chatMessages = remember {
        mutableStateListOf(
            BotChatMessage(
                sender = "bot",
                message = "Namaste! I am Guru 29, your AI Coach. 🤖\n\nAsk me any doubt about card rankings, trump revelation rules, marriages/royal pairs, bidding targets, or 7th card mechanics. Tap a quick doubt below or type your question!"
            )
        )
    }

    var isBotThinking by remember { mutableStateOf(false) }

    // Pulsing animation for the "Online" indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_bot")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bot_pulse_scale"
    )

    fun sendUserMessage(question: String) {
        if (question.isBlank() || isBotThinking) return
        val userQ = question.trim()
        chatMessages.add(BotChatMessage(sender = "user", message = userQ))
        inputText = ""

        isBotThinking = true
        coroutineScope.launch {
            // Scroll to latest
            listState.animateScrollToItem((chatMessages.size - 1).coerceAtLeast(0))

            val answer = if (onAskGeminiQuery != null) {
                try {
                    onAskGeminiQuery(userQ)
                } catch (_: Exception) {
                    getCoachBuiltinAnswer(userQ)
                }
            } else {
                getCoachBuiltinAnswer(userQ)
            }

            chatMessages.add(BotChatMessage(sender = "bot", message = answer))
            isBotThinking = false
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.72f))
                .padding(horizontal = 14.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.88f)
                    .clip(RoundedCornerShape(22.dp))
                    .border(
                        1.5.dp,
                        Brush.linearGradient(listOf(Color(0xFF38BDF8), CasinoGold, Color(0xFF10B981))),
                        RoundedCornerShape(22.dp)
                    )
                    .testTag("ai_coach_bot_window"),
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF0F172A)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF0B192C),
                                    Color(0xFF0F172A),
                                    Color(0xFF020617)
                                )
                            )
                        )
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B).copy(alpha = 0.85f))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Bot Avatar with pulsing green light
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0284C7).copy(alpha = 0.25f))
                                    .border(1.5.dp, Color(0xFF38BDF8), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = "AI Coach Bot",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(24.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .scale(pulseScale)
                                        .align(Alignment.BottomEnd)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                        .border(1.dp, Color.White, CircleShape)
                                )
                            }

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "GURU 29",
                                        color = CasinoGold,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF0284C7).copy(alpha = 0.35f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8))
                                    ) {
                                        Text(
                                            text = "AI COACH BOT",
                                            color = Color(0xFF38BDF8),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Ask Doubts & Check Official 29 Rules",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Close Button
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF334155).copy(alpha = 0.6f))
                                .testTag("coach_dialog_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Coach",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Tab Selector Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B)),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Tab 0: Doubts & Bot Chat
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { currentTab = 0 }
                                .padding(3.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = if (currentTab == 0) Color(0xFF0284C7) else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 7.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QuestionAnswer,
                                    contentDescription = null,
                                    tint = if (currentTab == 0) Color.White else Color(0xFF94A3B8),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Ask Doubts (Chat)",
                                    color = if (currentTab == 0) Color.White else Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }

                        // Tab 1: Rule Book
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { currentTab = 1 }
                                .padding(3.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = if (currentTab == 1) CasinoGold else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 7.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = if (currentTab == 1) Color(0xFF0F172A) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Official 29 Rules",
                                    color = if (currentTab == 1) Color(0xFF0F172A) else Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Content Area
                    if (currentTab == 0) {
                        // CHAT / DOUBTS TAB
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            // Quick Suggestion Chips
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val chips = listOf(
                                    "🃏 Card Points (J=3, 9=2...)",
                                    "👑 When to Reveal Trump?",
                                    "💍 Royal Pair (+4 / -4)",
                                    "🎯 Minimum Bids & Targets",
                                    "🂠 7th Card Secret Trump",
                                    "🔥 Single Hand 29 Solo",
                                    "⚡ Double & Redouble Pips",
                                    "🛡️ Defense Strategy"
                                )
                                items(chips) { chip ->
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color(0xFF1E293B),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                                        modifier = Modifier.clickable { sendUserMessage(chip) }
                                    ) {
                                        Text(
                                            text = chip,
                                            color = Color(0xFFE2E8F0),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }

                            // Message Stream
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(chatMessages) { msg ->
                                    ChatBubble(msg = msg)
                                }

                                if (isBotThinking) {
                                    item {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(vertical = 6.dp)
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = Color(0xFF38BDF8)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Guru 29 is analyzing...",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 12.sp,
                                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                            )
                                        }
                                    }
                                }
                            }

                            // Input Box Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E293B).copy(alpha = 0.9f))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = inputText,
                                    onValueChange = { inputText = it },
                                    placeholder = {
                                        Text(
                                            text = "Ask any doubt on 29...",
                                            color = Color(0xFF64748B),
                                            fontSize = 12.sp
                                        )
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("coach_chat_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF38BDF8),
                                        unfocusedBorderColor = Color(0xFF334155),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color(0xFF0F172A),
                                        unfocusedContainerColor = Color(0xFF0F172A)
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(
                                    onClick = { sendUserMessage(inputText) },
                                    enabled = inputText.isNotBlank() && !isBotThinking,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(if (inputText.isNotBlank()) Color(0xFF0284C7) else Color(0xFF334155))
                                        .testTag("coach_chat_send_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Send doubt",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // OFFICIAL RULES GUIDE TAB
                        RulesGuideView()
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: BotChatMessage) {
    val isUser = msg.sender == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0284C7).copy(alpha = 0.3f))
                    .border(1.dp, Color(0xFF38BDF8), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isUser) 14.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 14.dp
            ),
            color = if (isUser) Color(0xFF0369A1) else Color(0xFF1E293B),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isUser) Color(0xFF38BDF8).copy(alpha = 0.6f) else Color(0xFF334155)
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = msg.message,
                color = Color.White,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
            )
        }
    }
}

@Composable
private fun RulesGuideView() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            RuleCard(
                title = "1. Card Point Values & Hierarchy",
                icon = Icons.Default.Style,
                color = CasinoGold,
                content = """
                    • Jack (J) = 3 Points (Most powerful card!)
                    • Nine (9) = 2 Points (Second highest power)
                    • Ace (A) = 1 Point
                    • Ten (10) = 1 Point
                    • K, Q, 8, 7 = 0 Points (Used for trick winning power)
                    • 8th Trick Bonus = 1 Point
                    • Total points in deck = 28 + 1 = 29 Points!
                """.trimIndent()
            )
        }

        item {
            RuleCard(
                title = "2. Dealing & Bidding Target",
                icon = Icons.Default.Gavel,
                color = Color(0xFF38BDF8),
                content = """
                    • 4 cards are dealt to each player in Phase 1.
                    • Bidding starts at 16 (or 20 in High Roller).
                    • Players bid their estimated team target (16 to 28).
                    • Highest bidder secretly selects the TRUMP SUIT face-down.
                    • Remaining 4 cards are dealt to complete hands of 8.
                """.trimIndent()
            )
        }

        item {
            RuleCard(
                title = "3. Trump Revelation Rule",
                icon = Icons.Default.Visibility,
                color = Color(0xFFA855F7),
                content = """
                    • Trump remains secret until a player CANNOT follow the lead suit!
                    • You CANNOT request trump revelation if you hold a card of the suit led.
                    • Once revealed, trump overrides any lead suit card.
                    • If trump was chosen via 7th card rule, it is flipped immediately upon revelation.
                """.trimIndent()
            )
        }

        item {
            RuleCard(
                title = "4. Royal Pair (Marriage) Rule",
                icon = Icons.Default.Diamond,
                color = Color(0xFFEC4899),
                content = """
                    • A player holding King and Queen of the trump suit holds a Pair!
                    • Pair can only be declared AFTER trump has been revealed.
                    • If Bidding team declares: their required points drop by 4 (or add +4 points).
                    • If Defending team declares: bidder's target requirement effectively increases!
                """.trimIndent()
            )
        }

        item {
            RuleCard(
                title = "5. Single Hand (29 Solo) Rule",
                icon = Icons.Default.Whatshot,
                color = Color(0xFFEF4444),
                content = """
                    • A player may bid 29 Single Hand!
                    • Partner's cards are folded face-down.
                    • Bidder plays 1 vs 2 solo and MUST win all 8 tricks without losing a single trick!
                """.trimIndent()
            )
        }

        item {
            RuleCard(
                title = "6. Game Points (Pips) & Victory",
                icon = Icons.Default.EmojiEvents,
                color = Color(0xFF10B981),
                content = """
                    • Match is tracked with Red (won) and Black (lost) tokens.
                    • A standard match is won at 6 Game Points (Pips).
                    • If bidder makes target: +1 pip (or +2 if doubled, +4 if redoubled).
                    • If bidder fails target: -1 pip (or -2 if doubled, -4 if redoubled).
                """.trimIndent()
            )
        }
    }
}

@Composable
private fun RuleCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    content: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E293B),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = color,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = content,
                color = Color(0xFFCBD5E1),
                fontSize = 11.5.sp,
                lineHeight = 16.sp
            )
        }
    }
}

private fun getCoachBuiltinAnswer(query: String): String {
    val q = query.lowercase()
    return when {
        "point" in q || "value" in q || "card" in q || "j=" in q ->
            "In 29, points are: Jack = 3, Nine = 2, Ace = 1, Ten = 1. Kings, Queens, 8s, and 7s have 0 points. The 8th trick gives 1 bonus point, making a total of 29 points in every round!"

        "trump" in q || "reveal" in q ->
            "Trump remains face-down until a player is VOID (has no cards) in the led suit. You are strictly forbidden from revealing trump if you can follow suit! Once revealed, any trump card played beats the led suit."

        "pair" in q || "marriage" in q || "royalty" in q ->
            "Royal Pair (Marriage) consists of King and Queen of the trump suit! It can be declared after trump is revealed. If the bidding team holds it, their target is reduced by 4 points (or +4 to their score). If opponents hold it, it tightens the bidder's target!"

        "bid" in q || "target" in q || "minimum" in q ->
            "Bids start at 16 (or 20 in High Roller arena). You should only bid high if you hold strong Jacks and Nines with supporting Aces. The bidder chooses trump from their first 4 cards."

        "single" in q || "solo" in q ->
            "Single Hand (29 Solo) is the ultimate play! You bid 29, your partner folds their cards face-down, and you play solo against both opponents. You must win all 8 tricks to claim victory!"

        "double" in q || "redouble" in q ->
            "If opponents believe the bidder cannot reach their target, they can call DOUBLE! This doubles the victory pips at stake (+2 or -2). The bidder can counter with REDOUBLE for 4x pips!"

        "seventh" in q || "7th" in q ->
            "Under the Seventh Card Rule, the bidder places the 7th card of the deal face-down as trump without looking at it! It stays hidden until revealed in battle."

        "defend" in q || "partner" in q || "strategy" in q ->
            "Defending tip: Never waste your high cards early! Let your partner win tricks when possible. If you hold trump, hold them to cut the bidder's Aces and Jacks."

        else ->
            "Great question! In 29, remember: always follow suit if possible, protect your high Jacks (3 pts) and Nines (2 pts), count the 29 points on the table, and coordinate with your partner Vikram!"
    }
}
