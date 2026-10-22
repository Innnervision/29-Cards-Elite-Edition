package com.example.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundEffectsManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default)
    var isSoundEnabled: Boolean = true
    var isHapticsEnabled: Boolean = true
    var isMusicEnabled: Boolean = true
        set(value) {
            field = value
            if (value) {
                startBackgroundMusic()
            } else {
                stopBackgroundMusic()
            }
        }

    private var backgroundMusicTrack: AudioTrack? = null
    private var isMusicStarting: Boolean = false

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun playCardSwoosh() {
        if (!isSoundEnabled) return
        scope.launch {
            synthesizeSound(sampleRate = 44100, durationMs = 140) { sampleIndex, totalSamples ->
                val progress = sampleIndex.toFloat() / totalSamples
                val envelope = (1.0f - progress) * sin(progress * Math.PI.toFloat())
                val freq = 320f + progress * 580f
                val tone = sin(2.0 * Math.PI * freq * (sampleIndex / 44100.0)).toFloat()
                val flutter = sin(2.0 * Math.PI * 45.0 * (sampleIndex / 44100.0)).toFloat()
                (tone * 0.7f + flutter * 0.3f) * envelope * 0.45f
            }
        }
    }

    fun playCardSnap() {
        triggerHaptic(45, 170)
        if (!isSoundEnabled) return
        scope.launch {
            synthesizeSound(sampleRate = 44100, durationMs = 130) { sampleIndex, totalSamples ->
                val progress = sampleIndex.toFloat() / totalSamples
                val decay = Math.exp(-progress * 28.0).toFloat()
                val bodyTone = sin(2.0 * Math.PI * 220.0 * (sampleIndex / 44100.0)).toFloat()
                val snapTone = sin(2.0 * Math.PI * 920.0 * (sampleIndex / 44100.0)).toFloat()
                val click = if (sampleIndex < 180) (Math.random() * 1.8 - 0.9).toFloat() else 0f
                (bodyTone * 0.55f + snapTone * 0.35f + click * 0.7f) * decay * 0.65f
            }
        }
    }

    fun playTrumpReveal() {
        triggerHaptic(140, 255)
        if (!isSoundEnabled) return
        scope.launch {
            // Majestic orchestral brass chord progression (C4 -> G4 -> C5 -> E5 -> G5)
            val freqs = floatArrayOf(261.63f, 392.00f, 523.25f, 659.25f, 783.99f)
            synthesizeSound(sampleRate = 44100, durationMs = 850) { sampleIndex, totalSamples ->
                val progress = sampleIndex.toFloat() / totalSamples
                val decay = Math.exp(-progress * 3.5).toFloat()
                var sum = 0f
                for ((idx, f) in freqs.withIndex()) {
                    val delay = idx * 0.05f
                    if (progress >= delay) {
                        val localProg = (progress - delay) / (1f - delay)
                        val fundamental = sin(2.0 * Math.PI * f * (sampleIndex / 44100.0)).toFloat()
                        val harmonic = sin(4.0 * Math.PI * f * (sampleIndex / 44100.0)).toFloat() * 0.3f
                        sum += (fundamental + harmonic) * (1f - localProg * 0.7f)
                    }
                }
                sum * 0.22f * decay
            }
        }
    }

    fun playBidChime() {
        triggerHaptic(30, 120)
        if (!isSoundEnabled) return
        scope.launch {
            synthesizeSound(sampleRate = 44100, durationMs = 260) { sampleIndex, totalSamples ->
                val progress = sampleIndex.toFloat() / totalSamples
                val decay = Math.exp(-progress * 14.0).toFloat()
                val fundamental = sin(2.0 * Math.PI * 987.77 * (sampleIndex / 44100.0)).toFloat()
                val overtone = sin(2.0 * Math.PI * 1975.53 * (sampleIndex / 44100.0)).toFloat() * 0.45f
                (fundamental + overtone) * decay * 0.42f
            }
        }
    }

    fun playTrickWin() {
        triggerHaptic(40, 160)
        if (!isSoundEnabled) return
        scope.launch {
            val freqs = floatArrayOf(440f, 554.37f, 659.25f, 880f)
            synthesizeSound(sampleRate = 44100, durationMs = 450) { sampleIndex, totalSamples ->
                val progress = sampleIndex.toFloat() / totalSamples
                val step = (progress * 4).toInt().coerceIn(0, 3)
                val f = freqs[step]
                val decay = Math.exp(-progress * 5.0).toFloat()
                val tone1 = sin(2.0 * Math.PI * f * (sampleIndex / 44100.0)).toFloat()
                val tone2 = sin(4.0 * Math.PI * f * (sampleIndex / 44100.0)).toFloat() * 0.25f
                (tone1 + tone2) * decay * 0.45f
            }
        }
    }

    fun playPairRoyalty() {
        triggerHaptic(110, 230)
        if (!isSoundEnabled) return
        scope.launch {
            synthesizeSound(sampleRate = 44100, durationMs = 600) { sampleIndex, totalSamples ->
                val progress = sampleIndex.toFloat() / totalSamples
                val decay = Math.exp(-progress * 6.0).toFloat()
                val bell1 = sin(2.0 * Math.PI * 1174.66 * (sampleIndex / 44100.0)).toFloat()
                val bell2 = sin(2.0 * Math.PI * 1760.00 * (sampleIndex / 44100.0)).toFloat()
                val bell3 = sin(2.0 * Math.PI * 2349.32 * (sampleIndex / 44100.0)).toFloat() * 0.3f
                (bell1 * 0.5f + bell2 * 0.4f + bell3) * decay * 0.48f
            }
        }
    }

    fun playVictoryFanfare() {
        triggerHaptic(220, 255)
        if (!isSoundEnabled) return
        scope.launch {
            // Rich multi-harmonic royal trumpet fanfare (C4, E4, G4, C5, E5, G5, C6)
            val arpeggio = floatArrayOf(261.63f, 329.63f, 392.00f, 523.25f, 659.25f, 783.99f, 1046.50f)
            synthesizeSound(sampleRate = 44100, durationMs = 1350) { sampleIndex, totalSamples ->
                val progress = sampleIndex.toFloat() / totalSamples
                val noteDuration = 0.11f
                val noteIdx = (progress / noteDuration).toInt().coerceIn(0, arpeggio.size - 1)
                val isFinalChord = progress >= 0.77f

                if (isFinalChord) {
                    // Sustained triumphant major triad (C5 + G5 + C6)
                    val c5 = sin(2.0 * Math.PI * 523.25 * (sampleIndex / 44100.0)).toFloat()
                    val g5 = sin(2.0 * Math.PI * 783.99 * (sampleIndex / 44100.0)).toFloat()
                    val c6 = sin(2.0 * Math.PI * 1046.50 * (sampleIndex / 44100.0)).toFloat()
                    val chordDecay = (1.0f - ((progress - 0.77f) / 0.23f)).coerceIn(0f, 1f)
                    (c5 * 0.4f + g5 * 0.35f + c6 * 0.35f) * chordDecay * 0.55f
                } else {
                    val f = arpeggio[noteIdx]
                    val localP = ((progress % noteDuration) / noteDuration)
                    val env = (1.0f - localP * 0.6f)
                    val mainTone = sin(2.0 * Math.PI * f * (sampleIndex / 44100.0)).toFloat()
                    val octaveTone = sin(4.0 * Math.PI * f * (sampleIndex / 44100.0)).toFloat() * 0.35f
                    (mainTone + octaveTone) * env * 0.52f
                }
            }
        }
    }

    fun playDefeatSound() {
        triggerHaptic(160, 200)
        if (!isSoundEnabled) return
        scope.launch {
            // Melancholy descending minor cadence (F4 -> Eb4 -> Db4 -> C4 -> Ab3)
            val notes = floatArrayOf(349.23f, 311.13f, 277.18f, 261.63f, 207.65f)
            synthesizeSound(sampleRate = 44100, durationMs = 1200) { sampleIndex, totalSamples ->
                val progress = sampleIndex.toFloat() / totalSamples
                val noteDuration = 0.20f
                val noteIdx = (progress / noteDuration).toInt().coerceIn(0, notes.size - 1)
                val f = notes[noteIdx]
                val localP = (progress % noteDuration) / noteDuration
                val env = (1.0f - localP * 0.5f) * (1.0f - progress * 0.55f)
                val main = sin(2.0 * Math.PI * f * (sampleIndex / 44100.0)).toFloat()
                val sub = sin(1.0 * Math.PI * f * (sampleIndex / 44100.0)).toFloat() * 0.3f
                (main * 0.7f + sub * 0.3f) * env * 0.45f
            }
        }
    }

    fun playButtonClick() {
        triggerHaptic(12, 90)
        if (!isSoundEnabled) return
        scope.launch {
            synthesizeSound(sampleRate = 44100, durationMs = 45) { sampleIndex, totalSamples ->
                val progress = sampleIndex.toFloat() / totalSamples
                val decay = Math.exp(-progress * 22.0).toFloat()
                val clickTone = sin(2.0 * Math.PI * 1400.0 * (sampleIndex / 44100.0)).toFloat()
                clickTone * decay * 0.35f
            }
        }
    }

    fun playEquipSound() {
        triggerHaptic(40, 180)
        if (!isSoundEnabled) return
        scope.launch {
            synthesizeSound(sampleRate = 44100, durationMs = 280) { sampleIndex, totalSamples ->
                val progress = sampleIndex.toFloat() / totalSamples
                val decay = (1.0f - progress)
                val f = if (progress < 0.5f) 587.33f else 880.0f
                val tone = sin(2.0 * Math.PI * f * (sampleIndex / 44100.0)).toFloat()
                val shimmer = sin(4.0 * Math.PI * f * (sampleIndex / 44100.0)).toFloat() * 0.3f
                (tone + shimmer) * decay * 0.42f
            }
        }
    }

    fun playCoinCollect() {
        triggerHaptic(25, 140)
        if (!isSoundEnabled) return
        scope.launch {
            synthesizeSound(sampleRate = 44100, durationMs = 180) { sampleIndex, totalSamples ->
                val progress = sampleIndex.toFloat() / totalSamples
                val decay = Math.exp(-progress * 15.0).toFloat()
                val tone1 = sin(2.0 * Math.PI * 1318.51 * (sampleIndex / 44100.0)).toFloat()
                val tone2 = sin(2.0 * Math.PI * 2093.00 * (sampleIndex / 44100.0)).toFloat()
                (tone1 * 0.5f + tone2 * 0.5f) * decay * 0.4f
            }
        }
    }

    fun playChestOpen() {
        triggerHaptic(80, 200)
        if (!isSoundEnabled) return
        scope.launch {
            synthesizeSound(sampleRate = 44100, durationMs = 450) { sampleIndex, totalSamples ->
                val progress = sampleIndex.toFloat() / totalSamples
                val decay = (1.0f - progress)
                val f1 = 523.25f + progress * 400f
                val f2 = 783.99f + progress * 600f
                val t1 = sin(2.0 * Math.PI * f1 * (sampleIndex / 44100.0)).toFloat()
                val t2 = sin(2.0 * Math.PI * f2 * (sampleIndex / 44100.0)).toFloat()
                (t1 * 0.5f + t2 * 0.5f) * decay * 0.45f
            }
        }
    }

    fun playLevelUp() {
        triggerHaptic(150, 240)
        if (!isSoundEnabled) return
        scope.launch {
            val arpeggio = floatArrayOf(440f, 554.37f, 659.25f, 880f, 1108.73f)
            synthesizeSound(sampleRate = 44100, durationMs = 600) { sampleIndex, totalSamples ->
                val progress = sampleIndex.toFloat() / totalSamples
                val noteIdx = (progress * arpeggio.size).toInt().coerceIn(0, arpeggio.size - 1)
                val env = (1.0f - progress * 0.5f)
                sin(2.0 * Math.PI * arpeggio[noteIdx] * (sampleIndex / 44100.0)).toFloat() * env * 0.45f
            }
        }
    }

    fun playDealCard() {
        triggerHaptic(20, 100)
        if (!isSoundEnabled) return
        scope.launch {
            synthesizeSound(sampleRate = 44100, durationMs = 90) { sampleIndex, totalSamples ->
                val progress = sampleIndex.toFloat() / totalSamples
                val decay = (1.0f - progress) * (1.0f - progress)
                val noise = (Math.random() * 2.0 - 1.0).toFloat()
                val snap = sin(2.0 * Math.PI * 300.0 * (sampleIndex / 44100.0)).toFloat()
                (noise * 0.6f + snap * 0.4f) * decay * 0.35f
            }
        }
    }

    fun playDiamondCollect() {
        triggerHaptic(35, 170)
        if (!isSoundEnabled) return
        scope.launch {
            synthesizeSound(sampleRate = 44100, durationMs = 280) { sampleIndex, totalSamples ->
                val progress = sampleIndex.toFloat() / totalSamples
                val decay = Math.exp(-progress * 10.0).toFloat()
                val t1 = sin(2.0 * Math.PI * 1760.0 * (sampleIndex / 44100.0)).toFloat()
                val t2 = sin(2.0 * Math.PI * 2637.0 * (sampleIndex / 44100.0)).toFloat()
                val t3 = sin(2.0 * Math.PI * 3520.0 * (sampleIndex / 44100.0)).toFloat()
                (t1 * 0.35f + t2 * 0.4f + t3 * 0.25f) * decay * 0.45f
            }
        }
    }

    fun playPurchaseSuccess() {
        triggerHaptic(60, 210)
        if (!isSoundEnabled) return
        scope.launch {
            synthesizeSound(sampleRate = 44100, durationMs = 400) { sampleIndex, totalSamples ->
                val progress = sampleIndex.toFloat() / totalSamples
                val decay = (1.0f - progress)
                val note = if (progress < 0.33f) 587.33f else if (progress < 0.66f) 739.99f else 880.0f
                val tone = sin(2.0 * Math.PI * note * (sampleIndex / 44100.0)).toFloat()
                val harmonic = sin(2.0 * Math.PI * note * 2.0 * (sampleIndex / 44100.0)).toFloat()
                (tone * 0.7f + harmonic * 0.3f) * decay * 0.45f
            }
        }
    }

    fun playAdReward() {
        triggerHaptic(120, 230)
        if (!isSoundEnabled) return
        scope.launch {
            val chord = floatArrayOf(523.25f, 659.25f, 783.99f, 1046.5f)
            synthesizeSound(sampleRate = 44100, durationMs = 500) { sampleIndex, totalSamples ->
                val progress = sampleIndex.toFloat() / totalSamples
                val noteIdx = (progress * chord.size).toInt().coerceIn(0, chord.size - 1)
                val env = (1.0f - progress * 0.4f)
                val tone = sin(2.0 * Math.PI * chord[noteIdx] * (sampleIndex / 44100.0)).toFloat()
                tone * env * 0.45f
            }
        }
    }

    private fun triggerHaptic(durationMs: Long, amplitude: Int = 180) {
        if (!isHapticsEnabled || vibrator == null || !vibrator!!.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255)))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun synthesizeSound(sampleRate: Int, durationMs: Int, generator: (Int, Int) -> Float) {
        val totalSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(totalSamples)

        val attackSamples = 60
        val releaseSamples = 120

        for (i in 0 until totalSamples) {
            var rawSample = generator(i, totalSamples).coerceIn(-1.0f, 1.0f)
            // Apply smoothing window to prevent any pop/click at audio start or end
            if (i < attackSamples) {
                val factor = (1.0 - kotlin.math.cos(i.toDouble() / attackSamples * Math.PI * 0.5)).toFloat()
                rawSample *= factor
            } else if (i > totalSamples - releaseSamples) {
                val remaining = totalSamples - i
                val factor = (1.0 - kotlin.math.cos(remaining.toDouble() / releaseSamples * Math.PI * 0.5)).toFloat()
                rawSample *= factor
            }
            buffer[i] = (rawSample * Short.MAX_VALUE).toInt().toShort()
        }

        try {
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(Math.max(buffer.size * 2, minBufSize))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()

            // Release after playing
            scope.launch {
                kotlinx.coroutines.delay(durationMs.toLong() + 100)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }

    fun playHoverTick() {
        triggerHaptic(5, 30)
        if (!isSoundEnabled) return
        scope.launch {
            synthesizeSound(sampleRate = 44100, durationMs = 25) { sampleIndex, totalSamples ->
                val progress = sampleIndex.toFloat() / totalSamples
                val decay = Math.exp(-progress * 40.0).toFloat()
                sin(2.0 * Math.PI * 1800.0 * (sampleIndex / 44100.0)).toFloat() * decay * 0.18f
            }
        }
    }

    fun playNavTabClick() {
        triggerHaptic(10, 60)
        if (!isSoundEnabled) return
        scope.launch {
            synthesizeSound(sampleRate = 44100, durationMs = 60) { sampleIndex, totalSamples ->
                val progress = sampleIndex.toFloat() / totalSamples
                val decay = (1.0f - progress)
                val freq = 587.33f + progress * 293.66f
                val tone = sin(2.0 * Math.PI * freq * (sampleIndex / 44100.0)).toFloat()
                tone * decay * 0.28f
            }
        }
    }

    fun playPositiveAction() {
        triggerHaptic(20, 110)
        if (!isSoundEnabled) return
        scope.launch {
            synthesizeSound(sampleRate = 44100, durationMs = 150) { sampleIndex, totalSamples ->
                val progress = sampleIndex.toFloat() / totalSamples
                val freq = if (progress < 0.45f) 523.25f else 659.25f
                val localProg = if (progress < 0.45f) progress / 0.45f else (progress - 0.45f) / 0.55f
                val decay = (1.0f - localProg)
                sin(2.0 * Math.PI * freq * (sampleIndex / 44100.0)).toFloat() * decay * 0.38f
            }
        }
    }

    fun playCancelClick() {
        triggerHaptic(8, 50)
        if (!isSoundEnabled) return
        scope.launch {
            synthesizeSound(sampleRate = 44100, durationMs = 50) { sampleIndex, totalSamples ->
                val progress = sampleIndex.toFloat() / totalSamples
                val decay = Math.exp(-progress * 25.0).toFloat()
                sin(2.0 * Math.PI * 220.0 * (sampleIndex / 44100.0)).toFloat() * decay * 0.32f
            }
        }
    }

    fun playChipClink() {
        triggerHaptic(15, 80)
        if (!isSoundEnabled) return
        scope.launch {
            synthesizeSound(sampleRate = 44100, durationMs = 80) { sampleIndex, totalSamples ->
                val progress = sampleIndex.toFloat() / totalSamples
                val decay = Math.exp(-progress * 30.0).toFloat()
                val f1 = sin(2.0 * Math.PI * 2400.0 * (sampleIndex / 44100.0)).toFloat()
                val f2 = sin(2.0 * Math.PI * 3600.0 * (sampleIndex / 44100.0)).toFloat() * 0.5f
                (f1 + f2) * decay * 0.45f
            }
        }
    }

    fun playDiamondShine() {
        triggerHaptic(30, 160)
        if (!isSoundEnabled) return
        scope.launch {
            val notes = floatArrayOf(1046.50f, 1318.51f, 1567.98f, 2093.00f)
            synthesizeSound(sampleRate = 44100, durationMs = 380) { sampleIndex, totalSamples ->
                val progress = sampleIndex.toFloat() / totalSamples
                val idx = (progress * notes.size).toInt().coerceIn(0, notes.size - 1)
                val freq = notes[idx]
                val decay = (1.0f - progress)
                val tone = sin(2.0 * Math.PI * freq * (sampleIndex / 44100.0)).toFloat()
                val shimmer = sin(4.0 * Math.PI * freq * (sampleIndex / 44100.0)).toFloat() * 0.35f
                (tone + shimmer) * decay * 0.42f
            }
        }
    }

    fun playSocialShareChime() {
        triggerHaptic(50, 200)
        if (!isSoundEnabled) return
        scope.launch {
            val fanfare = floatArrayOf(587.33f, 739.99f, 880.00f, 1174.66f)
            synthesizeSound(sampleRate = 44100, durationMs = 520) { sampleIndex, totalSamples ->
                val progress = sampleIndex.toFloat() / totalSamples
                val idx = (progress * fanfare.size).toInt().coerceIn(0, fanfare.size - 1)
                val freq = fanfare[idx]
                val localProg = (progress * fanfare.size) - idx
                val decay = (1.0f - localProg * 0.6f) * (1.0f - progress * 0.3f)
                val fundamental = sin(2.0 * Math.PI * freq * (sampleIndex / 44100.0)).toFloat()
                val harmonic = sin(4.0 * Math.PI * freq * (sampleIndex / 44100.0)).toFloat() * 0.25f
                (fundamental + harmonic) * decay * 0.45f
            }
        }
    }

    fun startBackgroundMusic() {
        if (!isMusicEnabled || backgroundMusicTrack != null || isMusicStarting) return
        isMusicStarting = true
        scope.launch {
            try {
                val sampleRate = 22050
                val totalSeconds = 16.0
                val totalSamples = (sampleRate * totalSeconds).toInt()
                val buffer = ShortArray(totalSamples)

                // Luxurious warm jazz lounge chord progression (Cmaj9 -> Am9 -> Dm9 -> G13)
                val chords = listOf(
                    floatArrayOf(261.63f, 329.63f, 392.00f, 493.88f, 587.33f), // Cmaj9
                    floatArrayOf(220.00f, 261.63f, 329.63f, 392.00f, 493.88f), // Am9
                    floatArrayOf(146.83f, 174.61f, 220.00f, 261.63f, 329.63f), // Dm9
                    floatArrayOf(196.00f, 246.94f, 293.66f, 349.23f, 440.00f)  // G13
                )
                val samplesPerChord = totalSamples / chords.size

                for (i in 0 until totalSamples) {
                    val chordIdx = (i / samplesPerChord).coerceIn(0, chords.size - 1)
                    val chord = chords[chordIdx]
                    val chordTime = (i % samplesPerChord).toDouble() / sampleRate
                    var chordSample = 0.0

                    for ((noteIdx, freq) in chord.withIndex()) {
                        val noteDelay = noteIdx * 0.35
                        if (chordTime >= noteDelay) {
                            val timeSinceNote = chordTime - noteDelay
                            val decay = Math.exp(-timeSinceNote * 1.2)
                            val osc = sin(2.0 * Math.PI * freq * timeSinceNote) +
                                      sin(4.0 * Math.PI * freq * timeSinceNote) * 0.3 +
                                      sin(6.0 * Math.PI * freq * timeSinceNote) * 0.1
                            chordSample += osc * decay * 0.18
                        }
                    }

                    // Warm sub-bass pulse
                    val bassFreq = chord[0] * 0.5
                    val bass = sin(2.0 * Math.PI * bassFreq * (i.toDouble() / sampleRate)) * 0.2 * Math.exp(-((chordTime % 2.0)) * 0.8)
                    chordSample += bass

                    var mixed = (chordSample * 0.15).coerceIn(-1.0, 1.0)
                    
                    // Smooth fade in/out at loop boundaries to avoid any click
                    val edgeSamples = sampleRate
                    if (i < edgeSamples) {
                        mixed *= (i.toDouble() / edgeSamples)
                    } else if (i > totalSamples - edgeSamples) {
                        mixed *= ((totalSamples - i).toDouble() / edgeSamples)
                    }

                    buffer[i] = (mixed * Short.MAX_VALUE).toInt().toShort()
                }

                val minBufSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(Math.max(buffer.size * 2, minBufSize))
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.setLoopPoints(0, buffer.size, -1)
                track.play()
                backgroundMusicTrack = track
            } catch (_: Exception) {} finally {
                isMusicStarting = false
            }
        }
    }

    fun stopBackgroundMusic() {
        try {
            backgroundMusicTrack?.stop()
            backgroundMusicTrack?.release()
        } catch (_: Exception) {} finally {
            backgroundMusicTrack = null
        }
    }

    fun release() {
        stopBackgroundMusic()
        try {
            scope.cancel()
        } catch (_: Exception) {}
    }
}
