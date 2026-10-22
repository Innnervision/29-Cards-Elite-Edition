package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.model.Card
import com.example.game.model.Rank
import com.example.game.model.Suit
import com.example.ui.components.LuxuryScreenBackground
import com.example.ui.components.PlayingCard3DView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAcademyScreen(
    grandmasterAnswer: String? = null,
    isAsking: Boolean = false,
    onAskQuestion: (String) -> Unit = {},
    onOpenRulesGuide: () -> Unit = {},
    onBackToGame: () -> Unit
) {
    var selectedLesson by remember { mutableStateOf(0) }
    var quizAnswered by remember { mutableStateOf<Int?>(null) }
    var questionInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Academy",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "AI 29 MASTERCLASS",
                            color = Color(0xFFFFD700),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackToGame,
                        modifier = Modifier.testTag("academy_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = onOpenRulesGuide,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1B4B)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("📖 RULES GUIDE", color = Color(0xFFFFD700), fontSize = 10.5.sp, fontWeight = FontWeight.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        containerColor = Color(0xFF090D14)
    ) { padding ->
        LuxuryScreenBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
            // Mentor Header Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(16.dp))
                        .border(1.5.dp, Color(0xFFFFD700), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E1B4B)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_avatar_ai_master),
                            contentDescription = "AI Master",
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color(0xFFFFD700), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Grandmaster Vikram's Academy",
                                color = Color(0xFFFFD700),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Master the authentic Indian 29 card game rules, bidding mathematics, and trick tactics.",
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Lesson Tabs
            item {
                val tabs = listOf("1. Card Values", "2. Bidding", "3. Trump Rules", "4. Royalty", "5. Quiz")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedLesson == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFFFFD700) else Color(0xFF1E293B))
                                .clickable { selectedLesson = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title.substringBefore("."),
                                color = if (isSelected) Color(0xFF0F172A) else Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Active Lesson Content
            item {
                when (selectedLesson) {
                    0 -> LessonCardValues()
                    1 -> LessonBidding()
                    2 -> LessonTrumpRules()
                    3 -> LessonRoyalty()
                    4 -> LessonQuiz(quizAnswered) { quizAnswered = it }
                }
            }

            // Interactive Ask Grandmaster Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0F172A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Coach",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ask Grandmaster Vikram",
                                color = Color(0xFFFFD700),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Ask any tactical or rule question (e.g., 'When should I double?', 'How to manage Jacks?')",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = questionInput,
                                onValueChange = { questionInput = it },
                                placeholder = { Text("Type your question...", fontSize = 12.sp, color = Color(0xFF64748B)) },
                                modifier = Modifier.weight(1f),
                                maxLines = 2,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFFFD700),
                                    unfocusedBorderColor = Color(0xFF334155)
                                )
                            )

                            Button(
                                onClick = {
                                    if (questionInput.isNotBlank()) {
                                        onAskQuestion(questionInput)
                                        questionInput = ""
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                modifier = Modifier.testTag("ask_grandmaster_button")
                            ) {
                                if (isAsking) {
                                    CircularProgressIndicator(
                                        color = Color(0xFF0F172A),
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = Color(0xFF0F172A),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        if (grandmasterAnswer != null) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF1E1B4B)
                            ) {
                                Text(
                                    text = "💡 Vikram: \"$grandmasterAnswer\"",
                                    color = Color(0xFFF1F5F9),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(10.dp)
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

@Composable
private fun LessonCardValues() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "🃏 Card Hierarchy & Point System",
                color = Color(0xFFFFD700),
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "The 29 deck has 32 cards (8 in each suit). Total card points = 28. Winner of the 8th (last) trick gets +1 point, making 29 total points!",
                color = Color(0xFFE2E8F0),
                fontSize = 12.5.sp,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Point Cards Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PlayingCard3DView(card = Card(Suit.SPADES, Rank.JACK), width = 64.dp, height = 90.dp)
                    Text("Jack (J)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    Text("3 Points", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PlayingCard3DView(card = Card(Suit.HEARTS, Rank.NINE), width = 64.dp, height = 90.dp)
                    Text("Nine (9)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    Text("2 Points", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PlayingCard3DView(card = Card(Suit.CLUBS, Rank.ACE), width = 64.dp, height = 90.dp)
                    Text("Ace (A)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    Text("1 Point", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PlayingCard3DView(card = Card(Suit.DIAMONDS, Rank.TEN), width = 64.dp, height = 90.dp)
                    Text("Ten (10)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    Text("1 Point", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Note: King, Queen, 8, and 7 carry 0 points, but King/Queen have higher trick-taking power than 8/7.",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun LessonBidding() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "💰 Bidding & Double Rules",
                color = Color(0xFFFFD700),
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "1. After first 4 cards are dealt, bidding opens starting from 16 to 28.\n" +
                        "2. Highest bidder chooses the Secret Trump Suit (placed face down).\n" +
                        "3. Remaining 4 cards are dealt to make 8 cards each.\n" +
                        "4. Opponents can 'Double' if confident bidder will fail, doubling game point stakes!\n" +
                        "5. Bidder can 'Redouble' to quadruple stakes.",
                color = Color(0xFFE2E8F0),
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun LessonTrumpRules() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "🔒 Trump Hiding & Revelation",
                color = Color(0xFFFFD700),
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "• The Trump card is kept FACE DOWN by the bidder.\n" +
                        "• Players MUST follow the led suit if they have it.\n" +
                        "• When a player does NOT have the led suit, they can ask to 'SHOW TRUMP'.\n" +
                        "• When revealed, the requesting player MUST play a trump if they hold one.\n" +
                        "• Highest trump card wins the trick, otherwise highest card of led suit wins.",
                color = Color(0xFFE2E8F0),
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun LessonRoyalty() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "👑 'Pair' / Royalty (Marriage)",
                color = Color(0xFFFFD700),
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "If any player holds both KING and QUEEN of the Trump suit, after trump is revealed they can declare 'Pair':\n\n" +
                        "• If Bidder's team declares Pair: Target bid is REDUCED by 4 points!\n" +
                        "• If Defender's team declares Pair: Target bid is INCREASED by 4 points!",
                color = Color(0xFFE2E8F0),
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun LessonQuiz(answered: Int?, onAnswer: (Int) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "🧠 Masterclass Checkpoint",
                color = Color(0xFFFFD700),
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Question: In Indian 29, which card carries the highest point value?",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            val options = listOf("A. Ace (1 pt)", "B. Jack (3 pts)", "C. Nine (2 pts)", "D. King (4 pts)")
            options.forEachIndexed { index, opt ->
                val isCorrect = index == 1
                val isSelected = answered == index
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) {
                                if (isCorrect) Color(0xFF166534) else Color(0xFF991B1B)
                            } else Color(0xFF1E293B)
                        )
                        .clickable { onAnswer(index) }
                        .padding(12.dp)
                ) {
                    Text(
                        text = opt,
                        color = Color.White,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (answered != null) {
                Text(
                    text = if (answered == 1) "🎉 Correct! Jack is the most valuable card at 3 points." else "❌ Incorrect! Jack is worth 3 points.",
                    color = if (answered == 1) Color(0xFF4ADE80) else Color(0xFFF87171),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
