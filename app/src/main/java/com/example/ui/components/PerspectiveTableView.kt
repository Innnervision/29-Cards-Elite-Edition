package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class FeltTheme(val displayName: String, val primary: Color, val secondary: Color, val border: Color) {
    EMERALD_CASINO("Emerald Velvet", Color(0xFF0F3826), Color(0xFF072115), Color(0xFFD4AF37)),
    ROYAL_SAPPHIRE("Royal Sapphire", Color(0xFF0F2B48), Color(0xFF081829), Color(0xFF60A5FA)),
    CRIMSON_LOUNGE("Crimson Lounge", Color(0xFF4A101D), Color(0xFF28070E), Color(0xFFF59E0B)),
    ONYX_NOIR("Onyx Noir", Color(0xFF1E242B), Color(0xFF0F1316), Color(0xFFE2E8F0)),
    MUGHAL_DARBAR("Mughal Darbar", Color(0xFF0D5C52), Color(0xFF042B26), Color(0xFFFFD700)),
    NEON_CYBER("Neon Cyber", Color(0xFF2E1065), Color(0xFF0F0728), Color(0xFF06B6D4)),
    ROYAL_AMETHYST("Royal Amethyst", Color(0xFF581C87), Color(0xFF2E0854), Color(0xFFE9D5FF))
}

@Composable
fun PerspectiveTableView(
    modifier: Modifier = Modifier,
    feltTheme: FeltTheme = FeltTheme.EMERALD_CASINO,
    cameraTiltX: Float = 14f,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1E293B),
                        Color(0xFF0F172A),
                        Color(0xFF050811)
                    ),
                    radius = 1400f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Overhead warm chandelier spotlight glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFD700).copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        radius = 800f,
                        center = Offset.Unspecified
                    )
                )
        )

        // Angled side-view 3D table container
        Box(
            modifier = Modifier
                .fillMaxWidth(0.97f)
                .fillMaxHeight(0.92f)
                .graphicsLayer {
                    rotationX = cameraTiltX
                    cameraDistance = 15f * density
                    scaleY = 0.97f
                }
                .shadow(
                    elevation = 32.dp,
                    shape = RoundedCornerShape(percent = 45),
                    ambientColor = Color.Black,
                    spotColor = Color.Black
                )
                // Outer Mahogany & Rosewood polished rim
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF451A03), Color(0xFF280D02), Color(0xFF140501))
                    ),
                    shape = RoundedCornerShape(percent = 45)
                )
                .padding(7.dp)
                // Inner Filigree Metallic Bevel
                .border(
                    width = 2.5.dp,
                    brush = Brush.sweepGradient(
                        listOf(
                            feltTheme.border,
                            feltTheme.border.copy(alpha = 0.35f),
                            feltTheme.border,
                            feltTheme.border.copy(alpha = 0.5f),
                            feltTheme.border
                        )
                    ),
                    shape = RoundedCornerShape(percent = 45)
                )
                .padding(5.dp)
                // Main Felt Surface
                .clip(RoundedCornerShape(percent = 45))
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            feltTheme.primary.copy(alpha = 0.98f),
                            feltTheme.primary,
                            feltTheme.secondary
                        ),
                        center = Offset.Unspecified,
                        radius = 950f
                    )
                )
        ) {
            // Felt microtexture & stitch perimeter
            FeltTextureOverlay(feltTheme)

            // Center Table Watermark & Gold Stamp
            TableCenterStamp(feltTheme)

            // Slot for cards, trick area, and player pods
            content()
        }
    }
}

@Composable
private fun BoxScope.FeltTextureOverlay(theme: FeltTheme) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val inset = 24.dp.toPx()
        // Primary dashed betting border
        drawRoundRect(
            color = theme.border.copy(alpha = 0.22f),
            topLeft = Offset(inset, inset),
            size = androidx.compose.ui.geometry.Size(size.width - inset * 2, size.height - inset * 2),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height * 0.42f, size.height * 0.42f),
            style = Stroke(
                width = 1.5.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)
            )
        )

        // Secondary subtle inner stitch
        val innerInset = 34.dp.toPx()
        drawRoundRect(
            color = theme.border.copy(alpha = 0.08f),
            topLeft = Offset(innerInset, innerInset),
            size = androidx.compose.ui.geometry.Size(size.width - innerInset * 2, size.height - innerInset * 2),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height * 0.38f, size.height * 0.38f),
            style = Stroke(
                width = 1.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )
        )
    }
}

@Composable
private fun BoxScope.TableCenterStamp(theme: FeltTheme) {
    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .graphicsLayer { alpha = 0.24f },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "TWENTY NINE",
            color = theme.border,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp,
            fontFamily = FontFamily.Serif
        )
        Text(
            text = "♠  ♥  ♦  ♣",
            color = theme.border,
            fontSize = 18.sp,
            letterSpacing = 6.sp
        )
    }
}

