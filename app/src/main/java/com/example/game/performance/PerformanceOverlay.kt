package com.example.game.performance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DeveloperPerformanceOverlay(
    metrics: PerformanceMetrics,
    modifier: Modifier = Modifier
) {
    val fpsColor = when {
        metrics.fps >= 55 -> Color(0xFF4ADE80)
        metrics.fps >= 30 -> Color(0xFFFACC15)
        else -> Color(0xFFF87171)
    }

    Box(
        modifier = modifier
            .testTag("developer_performance_overlay")
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.88f))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "⚡",
                fontSize = 11.sp
            )
            Text(
                text = "FPS: ${metrics.fps}",
                color = fpsColor,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "• Smooth",
                color = Color(0xFF94A3B8),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
