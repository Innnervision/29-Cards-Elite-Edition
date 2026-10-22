package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// AAA Luxury Casino Gold & Metallic Palettes
val CasinoGold = Color(0xFFFFD700)
val CasinoGoldLight = Color(0xFFFFF0A5)
val CasinoGoldDark = Color(0xFFD97706)
val CasinoGoldRich = Color(0xFFB45309)
val CasinoBronze = Color(0xFFCD7F32)

// Gemstone Accents
val CasinoEmerald = Color(0xFF10B981)
val CasinoEmeraldDark = Color(0xFF047857)
val CasinoRuby = Color(0xFFEF4444)
val CasinoRubyDark = Color(0xFF991B1B)
val CasinoDiamond = Color(0xFF38BDF8)
val CasinoDiamondLight = Color(0xFFBAE6FD)
val CasinoDiamondDark = Color(0xFF0284C7)
val CasinoAmethyst = Color(0xFFA855F7)

// AAA Obsidian and Luxury Surface Tones
val CasinoMidnight = Color(0xFF060913)
val CasinoObsidian = Color(0xFF0B101E)
val CasinoSurface = Color(0xFF0F172A)
val CasinoSurfaceVariant = Color(0xFF1E293B)
val CasinoSurfaceGlow = Color(0xFF334155)

// Metallic Gradient Brushes for AAA UI
val GoldMetallicBrush = Brush.linearGradient(
    listOf(Color(0xFFFFF5C0), Color(0xFFFFD700), Color(0xFFB45309), Color(0xFFFFD700), Color(0xFFFFF5C0))
)

val DiamondGlowBrush = Brush.linearGradient(
    listOf(Color(0xFFBAE6FD), Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF38BDF8))
)

val ObsidianCardBrush = Brush.verticalGradient(
    listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF080C16))
)

val RubyStakesBrush = Brush.horizontalGradient(
    listOf(Color(0xFFEF4444), Color(0xFFB91C1C), Color(0xFF7F1D1D))
)

val RubyRedBrush = Brush.verticalGradient(
    listOf(Color(0xFFEF4444), Color(0xFFB91C1C), Color(0xFF991B1B))
)

val EmeraldWinnerBrush = Brush.verticalGradient(
    listOf(Color(0xFF34D399), Color(0xFF10B981), Color(0xFF047857))
)
