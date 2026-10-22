package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

import androidx.compose.ui.unit.TextUnit

enum class ButtonVariant {
    GOLD,
    DIAMOND,
    PRIMARY,
    SECONDARY,
    DANGER,
    EMERALD,
    GLASS_GOLD,
    TRANSLUCENT
}

/**
 * AAA Tactile 3D Action Button with metallic gradient, beveled bottom bevel,
 * animated light sweep, and spring touch feedback.
 */
@Composable
fun Aaa3DButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant? = null,
    icon: ImageVector? = null,
    iconEmoji: String? = null,
    gradient: Brush? = null,
    textColor: Color? = null,
    shadowColor: Color? = null,
    height: Dp = 38.dp,
    enabled: Boolean = true,
    isTranslucent: Boolean = false,
    horizontalPadding: Dp = 14.dp,
    verticalPadding: Dp = 6.dp,
    fontSize: TextUnit = 11.5.sp,
    testTag: String = "aaa_button"
) {
    val (resolvedGradient, resolvedTextColor, resolvedShadowColor) = when {
        gradient != null -> Triple(
            gradient,
            textColor ?: Color(0xFF0F172A),
            shadowColor ?: Color(0xFF78350F)
        )
        variant == ButtonVariant.GLASS_GOLD || isTranslucent -> Triple(
            Brush.verticalGradient(
                listOf(
                    Color(0xFFFFD700).copy(alpha = 0.20f),
                    Color(0xFFB45309).copy(alpha = 0.10f)
                )
            ),
            textColor ?: Color(0xFFFFD700),
            shadowColor ?: Color(0xFFFFD700).copy(alpha = 0.25f)
        )
        variant == ButtonVariant.TRANSLUCENT -> Triple(
            Brush.verticalGradient(
                listOf(
                    Color(0xFF334155).copy(alpha = 0.30f),
                    Color(0xFF0F172A).copy(alpha = 0.40f)
                )
            ),
            textColor ?: Color(0xFFF1F5F9),
            shadowColor ?: Color(0xFF0F172A).copy(alpha = 0.25f)
        )
        variant == ButtonVariant.DIAMOND -> Triple(
            DiamondGlowBrush,
            textColor ?: Color(0xFF082F49),
            shadowColor ?: Color(0xFF0369A1)
        )
        variant == ButtonVariant.PRIMARY -> Triple(
            Brush.verticalGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))),
            textColor ?: Color.White,
            shadowColor ?: Color(0xFF1E3A8A)
        )
        variant == ButtonVariant.SECONDARY -> Triple(
            Brush.verticalGradient(listOf(Color(0xFF334155), Color(0xFF1E293B))),
            textColor ?: Color(0xFFE2E8F0),
            shadowColor ?: Color(0xFF0F172A)
        )
        variant == ButtonVariant.DANGER -> Triple(
            RubyRedBrush,
            textColor ?: Color.White,
            shadowColor ?: Color(0xFF7F1D1D)
        )
        variant == ButtonVariant.EMERALD -> Triple(
            EmeraldWinnerBrush,
            textColor ?: Color(0xFF022C22),
            shadowColor ?: Color(0xFF064E3B)
        )
        else -> Triple(
            GoldMetallicBrush,
            textColor ?: Color(0xFF0F172A),
            shadowColor ?: Color(0xFF78350F)
        )
    }

    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 600f),
        label = "btn_press_scale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 6.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "btn_elevation"
    )

    // Animated light shimmer across the button surface
    val infiniteTransition = rememberInfiniteTransition(label = "btn_shimmer")
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 400f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_x"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(elevation, RoundedCornerShape(10.dp), spotColor = resolvedShadowColor, ambientColor = resolvedShadowColor)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (enabled) resolvedGradient
                else Brush.verticalGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
            )
            .border(
                1.dp,
                if (enabled) {
                    if (isTranslucent || variant == ButtonVariant.GLASS_GOLD) Color(0xFFFFD700).copy(alpha = 0.75f)
                    else Color(0xFFFFF0A5).copy(alpha = 0.8f)
                } else Color(0xFF475569),
                RoundedCornerShape(10.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            )
            .testTag(testTag)
            .drawWithContent {
                drawContent()
                if (enabled) {
                    // Specular light sweep
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.35f), Color.Transparent),
                            start = Offset(shimmerTranslate, 0f),
                            end = Offset(shimmerTranslate + 60f, size.height)
                        ),
                        start = Offset(shimmerTranslate, 0f),
                        end = Offset(shimmerTranslate + 60f, size.height),
                        strokeWidth = 35f
                    )
                }
            }
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
            .heightIn(min = height),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = if (enabled) resolvedTextColor else Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            } else if (iconEmoji != null) {
                Text(text = iconEmoji, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(5.dp))
            }

            Text(
                text = text,
                color = if (enabled) resolvedTextColor else Color(0xFF94A3B8),
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.6.sp
            )
        }
    }
}

/**
 * AAA Obsidian Glassmorphism Container with Specular Highlights
 */
@Composable
fun AaaGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    borderColor: Color = Color(0xFF334155),
    borderBrush: Brush? = null,
    glowColor: Color = Color(0xFF0F172A),
    backgroundColor: Color = CasinoSurface.copy(alpha = 0.92f),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .shadow(16.dp, shape, ambientColor = Color.Black, spotColor = glowColor)
            .then(
                if (borderBrush != null) Modifier.border(1.5.dp, borderBrush, shape)
                else Modifier.border(1.dp, borderColor, shape)
            ),
        shape = shape,
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF1E293B).copy(alpha = 0.65f),
                            Color(0xFF0F172A).copy(alpha = 0.95f),
                            Color(0xFF070B14)
                        )
                    )
                )
                .padding(14.dp)
        ) {
            content()
        }
    }
}

/**
 * AAA Currency Pill with Diamond & Coin glows
 */
@Composable
fun AaaCurrencyPill(
    type: CurrencyType,
    amount: Long,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    showAddBadge: Boolean = true,
    hasNotification: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "curr_pulse")
    val badgeGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badge_alpha"
    )

    val isDiamond = type == CurrencyType.DIAMOND
    val primaryColor = if (isDiamond) Color(0xFF38BDF8) else Color(0xFFFFD700)
    val bgGlow = if (isDiamond) Color(0xFF0284C7).copy(alpha = 0.22f) else Color(0xFF78350F).copy(alpha = 0.25f)
    val borderColor = if (isDiamond) Color(0xFF38BDF8).copy(alpha = 0.65f) else Color(0xFFFFD700).copy(alpha = 0.65f)

    Surface(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = primaryColor.copy(alpha = 0.35f))
            .clip(RoundedCornerShape(20.dp))
            .border(1.2.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = CasinoObsidian.copy(alpha = 0.94f)
    ) {
        Row(
            modifier = Modifier
                .background(bgGlow)
                .padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (isDiamond) {
                Text("💎", fontSize = 12.sp)
            } else {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = "Coins",
                    tint = primaryColor,
                    modifier = Modifier.size(15.dp)
                )
            }

            Text(
                text = if (isDiamond) "$amount" else "%,d".format(amount),
                color = primaryColor,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Black
            )

            if (showAddBadge) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = 0.25f))
                        .border(0.8.dp, primaryColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        color = primaryColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 10.sp
                    )
                }
            }

            if (hasNotification) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .scale(badgeGlow)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                )
            }
        }
    }
}

enum class CurrencyType {
    DIAMOND, COIN
}

/**
 * AAA Holographic & Metallic Ribbon Badge
 */
@Composable
fun AaaBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = CasinoGold,
    textColor: Color = Color(0xFF0F172A),
    isPulsing: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "badge_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_anim"
    )

    Surface(
        modifier = modifier
            .then(if (isPulsing) Modifier.scale(pulseScale) else Modifier)
            .shadow(6.dp, RoundedCornerShape(6.dp), spotColor = color)
            .border(1.dp, color, RoundedCornerShape(6.dp)),
        shape = RoundedCornerShape(6.dp),
        color = color
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

/**
 * AAA Section Header with Golden Jewel Divider
 */
@Composable
fun AaaSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    badge: String? = null,
    accentColor: Color = CasinoGold
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(4.dp, 16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title.uppercase(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    if (badge != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        AaaBadge(text = badge, color = accentColor)
                    }
                }
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = Color(0xFF94A3B8),
                        fontSize = 9.5.sp
                    )
                }
            }
        }
    }
}

/**
 * AAA Tactile Back Button with golden highlight and arrow
 */
@Composable
fun AaaBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "BACK",
    testTag: String = "screen_back_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Surface(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 34.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag),
        shape = RoundedCornerShape(10.dp),
        color = if (isPressed) Color(0xFF1E293B) else Color(0xFF0B1329).copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isPressed) CasinoGold else Color(0xFF334155)
        ),
        shadowElevation = if (isPressed) 1.dp else 3.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = label,
                tint = CasinoGold,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label.uppercase(),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
        }
    }
}

