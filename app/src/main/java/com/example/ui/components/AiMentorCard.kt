package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

@Composable
fun AiMentorCard(
    tip: String?,
    isThinking: Boolean,
    onDismiss: () -> Unit,
    onRequestHint: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // AI Mentor Hint Pill Button
        Surface(
            modifier = Modifier
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .clickable(onClick = onRequestHint)
                .border(1.dp, Color(0xFFFFD700), CircleShape)
                .testTag("ai_hint_button"),
            color = Color(0xFF1E1B4B).copy(alpha = 0.95f),
            shape = CircleShape
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Mentor Hint",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = if (isThinking) "ANALYZING..." else "AI HINT",
                    color = Color(0xFFFFD700),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Live Tactical Tip Balloon
        AnimatedVisibility(
            visible = tip != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .shadow(8.dp, RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.96f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_avatar_ai_master),
                        contentDescription = "AI Coach",
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = tip ?: "",
                        color = Color(0xFFF1F5F9),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 13.sp,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(18.dp).padding(start = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Tip",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}
