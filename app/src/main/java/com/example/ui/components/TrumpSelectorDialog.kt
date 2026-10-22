package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.game.model.Suit
import com.example.ui.theme.*

@Composable
fun TrumpSelectorDialog(
    onSuitSelected: (Suit) -> Unit
) {
    Dialog(
        onDismissRequest = { /* Modal */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        AaaGlassCard(
            modifier = Modifier.fillMaxWidth(0.78f),
            borderBrush = GoldMetallicBrush,
            glowColor = CasinoGold
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "👑 YOU WON THE BID!",
                    color = CasinoGold,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp
                )

                Text(
                    text = "Select your Secret Hidden Trump Suit:",
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (suit in Suit.entries) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(78.dp)
                                .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = if (suit.isRed) Color(0xFFEF4444) else CasinoGold)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF070B14))
                                    )
                                )
                                .border(
                                    1.2.dp,
                                    if (suit.isRed) Color(0xFFEF4444).copy(alpha = 0.65f) else CasinoGold.copy(alpha = 0.65f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onSuitSelected(suit) }
                                .padding(6.dp)
                                .testTag("select_suit_${suit.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = suit.symbol,
                                    color = if (suit.isRed) Color(0xFFEF4444) else Color.White,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = suit.displayName,
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
