package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
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
import com.example.game.model.AiDifficulty
import com.example.game.performance.QualityProfile

@Composable
fun SettingsDialog(
    isSoundEnabled: Boolean,
    onSoundToggled: (Boolean) -> Unit,
    isHapticsEnabled: Boolean,
    onHapticsToggled: (Boolean) -> Unit,
    selectedFeltTheme: FeltTheme,
    onFeltThemeSelected: (FeltTheme) -> Unit,
    isMusicEnabled: Boolean = true,
    onMusicToggled: (Boolean) -> Unit = {},
    qualityProfile: QualityProfile = QualityProfile.AUTO,
    onQualityProfileSelected: (QualityProfile) -> Unit = {},
    aiDifficulty: AiDifficulty = AiDifficulty.ADAPTIVE,
    onAiDifficultySelected: (AiDifficulty) -> Unit = {},
    isPerformanceOverlayEnabled: Boolean = false,
    onPerformanceOverlayToggled: (Boolean) -> Unit = {},
    onShareGame: () -> Unit = {},
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .shadow(24.dp, RoundedCornerShape(20.dp))
                .border(2.dp, Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFB8860B))), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "GAME SETTINGS",
                    color = Color(0xFFFFD700),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Ambient Background Music Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ambient Casino Music (BGM)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Gentle, soothing background melody", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    Switch(
                        checked = isMusicEnabled,
                        onCheckedChange = onMusicToggled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFFFD700),
                            checkedTrackColor = Color(0xFFD97706)
                        ),
                        modifier = Modifier.testTag("bgm_switch")
                    )
                }

                HorizontalDivider(color = Color(0xFF1E293B), modifier = Modifier.padding(vertical = 6.dp))

                // Sound Effects Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sound Effects (SFX)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Card slides, snaps, trump fanfares", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    Switch(
                        checked = isSoundEnabled,
                        onCheckedChange = onSoundToggled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFFFD700),
                            checkedTrackColor = Color(0xFFD97706)
                        ),
                        modifier = Modifier.testTag("sfx_switch")
                    )
                }

                HorizontalDivider(color = Color(0xFF1E293B), modifier = Modifier.padding(vertical = 6.dp))

                // Haptics Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Haptic Feedback (VFX)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Vibration on card slams & trump reveal", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    Switch(
                        checked = isHapticsEnabled,
                        onCheckedChange = onHapticsToggled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFFFD700),
                            checkedTrackColor = Color(0xFFD97706)
                        ),
                        modifier = Modifier.testTag("haptics_switch")
                    )
                }

                HorizontalDivider(color = Color(0xFF1E293B), modifier = Modifier.padding(vertical = 6.dp))

                // Graphics Quality Profile
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("Graphics Quality & Thermals", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Controls particle density, shadows, and thermal throttling", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        QualityProfile.values().forEach { profile ->
                            val isSelected = qualityProfile == profile
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFFD97706) else Color(0xFF1E293B))
                                    .clickable { onQualityProfileSelected(profile) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    profile.name,
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF1E293B), modifier = Modifier.padding(vertical = 6.dp))

                // AI Difficulty Level
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("AI Opponent Intelligence", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Tuning: ${aiDifficulty.description}", color = Color(0xFFFFD700), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AiDifficulty.values().forEach { diff ->
                            val isSelected = aiDifficulty == diff
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF2563EB) else Color(0xFF1E293B))
                                    .clickable { onAiDifficultySelected(diff) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    diff.title,
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF1E293B), modifier = Modifier.padding(vertical = 6.dp))

                // Dev Performance Overlay Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Frame Rate Counter (FPS)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Show live frame rate and display smoothness on screen", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    Switch(
                        checked = isPerformanceOverlayEnabled,
                        onCheckedChange = onPerformanceOverlayToggled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFFFD700),
                            checkedTrackColor = Color(0xFFD97706)
                        ),
                        modifier = Modifier.testTag("perf_overlay_switch")
                    )
                }

                HorizontalDivider(color = Color(0xFF1E293B), modifier = Modifier.padding(vertical = 6.dp))

                // Felt Table Theme Selection
                Text(
                    text = "Casino Felt Table Theme",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (theme in FeltTheme.values()) {
                        val isSelected = selectedFeltTheme == theme
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.primary)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFFFFD700) else Color(0xFF475569),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onFeltThemeSelected(theme) }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = theme.displayName.substringBefore(" "),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Share Button
                OutlinedButton(
                    onClick = onShareGame,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFB8860B))))
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Invite Friends & Share Game", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().testTag("close_settings_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                ) {
                    Text("DONE", color = Color(0xFF0F172A), fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
