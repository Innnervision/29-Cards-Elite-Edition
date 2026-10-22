package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CasinoGold,
    onPrimary = Color(0xFF0F172A),
    secondary = CasinoGoldDark,
    onSecondary = Color.White,
    tertiary = CasinoEmerald,
    background = CasinoMidnight,
    surface = CasinoSurface,
    surfaceVariant = CasinoSurfaceVariant,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalSpacing provides Spacing()) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = Typography,
            content = content
        )
    }
}
