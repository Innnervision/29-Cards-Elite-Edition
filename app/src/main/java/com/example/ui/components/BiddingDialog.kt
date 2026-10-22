package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.game.engine.TwentyNineEngine
import com.example.game.model.Card
import com.example.game.model.PlayerPosition
import com.example.ui.theme.*

@Composable
fun BiddingDialog(
    currentHighestBid: Int,
    highestBidder: PlayerPosition?,
    minBid: Int = 16,
    userCards: List<Card> = emptyList(),
    cardSkin: String = "CLASSIC_ROYAL",
    onSortToggle: (() -> Unit)? = null,
    onBidPlaced: (Int) -> Unit,
    onPass: () -> Unit,
    onDouble: () -> Unit,
    canDouble: Boolean = false,
    canRedouble: Boolean = false,
    onRedouble: () -> Unit,
    onSingleHand: (() -> Unit)? = null
) {
    val startingBid = (currentHighestBid + 1).coerceAtLeast(minBid)
    var selectedBid by remember(startingBid) { mutableStateOf(startingBid) }

    val bidOptions = remember(startingBid) {
        (startingBid..28).toList()
    }

    Dialog(
        onDismissRequest = { /* Prevent modal dismiss on outside click */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        AaaGlassCard(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            borderBrush = GoldMetallicBrush,
            glowColor = CasinoGold
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header & Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ROYAL AUCTION",
                            color = CasinoGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = if (currentHighestBid >= minBid)
                                "Current High: $currentHighestBid by ${highestBidder?.displayName ?: "None"}"
                            else "Opening Minimum Bid: $minBid",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.5.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (onSortToggle != null) {
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(onClick = onSortToggle)
                                    .testTag("bidding_sort_button"),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E293B),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CasinoGold.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "⇅ Sort Hand",
                                    color = CasinoGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CasinoGold.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CasinoGold)
                        ) {
                            Text(
                                text = "Target: $selectedBid",
                                color = CasinoGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Dealt 4-Cards Preview Strip
                if (userCards.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0F172A).copy(alpha = 0.8f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Your 4 Cards:",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            userCards.forEachIndexed { idx, card ->
                                AnimatedPlayingCard(
                                    card = card,
                                    isPlayable = true,
                                    isSelected = false,
                                    isTrump = false,
                                    skinId = cardSkin,
                                    dealIndex = idx,
                                    animateDeal = true,
                                    width = 40.dp,
                                    height = 56.dp,
                                    onClick = {}
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Casino Chip Selection Slider
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    items(bidOptions) { bid ->
                        val isSelected = selectedBid == bid
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .shadow(if (isSelected) 8.dp else 2.dp, CircleShape, spotColor = CasinoGold)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) {
                                        Brush.radialGradient(listOf(Color(0xFFFFE082), Color(0xFFB45309)))
                                    } else {
                                        Brush.radialGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
                                    }
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color(0xFF475569),
                                    shape = CircleShape
                                )
                                .clickable { selectedBid = bid },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$bid",
                                color = if (isSelected) Color(0xFF0F172A) else Color(0xFFE2E8F0),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons Row (Pass, Double, Redouble, Single Hand 29, Place Bid)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pass Button
                    Aaa3DButton(
                        text = "PASS",
                        icon = Icons.Default.Close,
                        onClick = onPass,
                        variant = ButtonVariant.DANGER,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pass_bid_button")
                    )

                    if (canDouble) {
                        Aaa3DButton(
                            text = "DOUBLE (2x)",
                            icon = Icons.Default.Security,
                            onClick = onDouble,
                            variant = ButtonVariant.DANGER,
                            modifier = Modifier
                                .weight(1.1f)
                                .testTag("double_bid_button")
                        )
                    }

                    if (canRedouble) {
                        Aaa3DButton(
                            text = "REDOUBLE (4x)",
                            icon = Icons.Default.Whatshot,
                            onClick = onRedouble,
                            variant = ButtonVariant.PRIMARY,
                            modifier = Modifier
                                .weight(1.1f)
                                .testTag("redouble_bid_button")
                        )
                    }

                    if (onSingleHand != null) {
                        Aaa3DButton(
                            text = "SOLO 29",
                            icon = Icons.Default.Bolt,
                            onClick = onSingleHand,
                            variant = ButtonVariant.DIAMOND,
                            modifier = Modifier
                                .weight(1.1f)
                                .testTag("single_hand_button")
                        )
                    }

                    // Confirm Bid Button
                    Aaa3DButton(
                        text = "BID $selectedBid",
                        icon = Icons.Default.Check,
                        onClick = { onBidPlaced(selectedBid) },
                        variant = ButtonVariant.GOLD,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("place_bid_button")
                    )
                }
            }
        }
    }
}
