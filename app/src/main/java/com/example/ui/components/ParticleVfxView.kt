package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.game.model.VfxEvent
import com.example.game.performance.QualityProfile

@Composable
fun ParticleVfxView(
    vfxEvent: VfxEvent?,
    qualityProfile: QualityProfile = QualityProfile.HIGH,
    modifier: Modifier = Modifier
) {
    AaaParticleVfxSystem(
        vfxEvent = vfxEvent,
        qualityProfile = qualityProfile,
        modifier = modifier
    )
}

