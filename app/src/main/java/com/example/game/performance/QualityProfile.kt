package com.example.game.performance

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.util.Log
import android.view.Display
import android.view.WindowManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import kotlin.math.roundToInt

enum class QualityProfile(
    val title: String,
    val description: String,
    val maxParticles: Int,
    val shadowElevationFactor: Float,
    val targetFps: Int,
    val enableVfxShimmer: Boolean,
    val enableCardBlur: Boolean,
    val enableAmbientGlow: Boolean,
    val aiEvaluationDepth: Int
) {
    LOW(
        title = "Battery Saver / Low",
        description = "Optimized for older chips (2-3 GB RAM). Maximum battery life.",
        maxParticles = 12,
        shadowElevationFactor = 0f,
        targetFps = 30,
        enableVfxShimmer = false,
        enableCardBlur = false,
        enableAmbientGlow = false,
        aiEvaluationDepth = 1
    ),
    MEDIUM(
        title = "Balanced / Medium",
        description = "Smooth 60 FPS for mid-range devices (4-6 GB RAM).",
        maxParticles = 30,
        shadowElevationFactor = 0.5f,
        targetFps = 60,
        enableVfxShimmer = true,
        enableCardBlur = false,
        enableAmbientGlow = true,
        aiEvaluationDepth = 2
    ),
    HIGH(
        title = "High Quality",
        description = "Rich lighting and particle density for modern flagships.",
        maxParticles = 65,
        shadowElevationFactor = 0.85f,
        targetFps = 60,
        enableVfxShimmer = true,
        enableCardBlur = true,
        enableAmbientGlow = true,
        aiEvaluationDepth = 3
    ),
    ULTRA(
        title = "Ultra / High Refresh",
        description = "Full fidelity up to 120Hz for high-end gaming devices.",
        maxParticles = 110,
        shadowElevationFactor = 1.0f,
        targetFps = 120,
        enableVfxShimmer = true,
        enableCardBlur = true,
        enableAmbientGlow = true,
        aiEvaluationDepth = 4
    ),
    AUTO(
        title = "Auto (Adaptive)",
        description = "Dynamically scales graphics based on thermal state and frame time.",
        maxParticles = 50,
        shadowElevationFactor = 0.7f,
        targetFps = 60,
        enableVfxShimmer = true,
        enableCardBlur = false,
        enableAmbientGlow = true,
        aiEvaluationDepth = 2
    )
}

enum class ThermalLevel(val label: String, val isSevere: Boolean) {
    COOL("Cool", false),
    NORMAL("Normal", false),
    WARM("Warm", false),
    HOT("Hot - Throttling Avoidance", true),
    CRITICAL("Critical - Power Throttle", true)
}

data class PerformanceMetrics(
    val fps: Int = 60,
    val frameTimeMs: Float = 16.6f,
    val usedMemoryMb: Long = 0,
    val totalMemoryMb: Long = 0,
    val maxHeapMb: Long = 0,
    val activeProfile: QualityProfile = QualityProfile.AUTO,
    val effectiveProfile: QualityProfile = QualityProfile.HIGH,
    val thermalLevel: ThermalLevel = ThermalLevel.NORMAL,
    val cpuCores: Int = Runtime.getRuntime().availableProcessors(),
    val displayRefreshRate: Float = 60f,
    val activeParticlesCount: Int = 0,
    val droppedFramesRecent: Int = 0
)

class PerformanceManager(private val context: Context) {

    companion object {
        private const val TAG = "PerformanceManager"
        private const val PREFS_NAME = "twenty_nine_perf_prefs"
        private const val KEY_PROFILE = "selected_quality_profile"
        private const val KEY_OVERLAY_ENABLED = "dev_perf_overlay_enabled"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _selectedProfile = MutableStateFlow(loadSavedProfile())
    val selectedProfile: StateFlow<QualityProfile> = _selectedProfile.asStateFlow()

    private val _metrics = MutableStateFlow(PerformanceMetrics())
    val metrics: StateFlow<PerformanceMetrics> = _metrics.asStateFlow()

    private val _isOverlayEnabled = MutableStateFlow(prefs.getBoolean(KEY_OVERLAY_ENABLED, false))
    val isOverlayEnabled: StateFlow<Boolean> = _isOverlayEnabled.asStateFlow()

    private var thermalListenerRegistered = false
    private var powerManager: PowerManager? = null

    // Frame tracking
    private var lastFrameTimestampNano: Long = 0L
    private var frameCountInInterval = 0
    private var lastFpsUpdateTimestamp = System.currentTimeMillis()

    init {
        detectHardwareCapabilities()
        setupThermalMonitoring()
        startMetricsMonitoringLoop()
    }

    private fun loadSavedProfile(): QualityProfile {
        val name = prefs.getString(KEY_PROFILE, QualityProfile.AUTO.name) ?: QualityProfile.AUTO.name
        return try {
            QualityProfile.valueOf(name)
        } catch (_: Exception) {
            QualityProfile.AUTO
        }
    }

    fun setQualityProfile(profile: QualityProfile) {
        _selectedProfile.value = profile
        prefs.edit().putString(KEY_PROFILE, profile.name).apply()
        recalculateEffectiveProfile()
    }

    fun setOverlayEnabled(enabled: Boolean) {
        _isOverlayEnabled.value = enabled
        prefs.edit().putBoolean(KEY_OVERLAY_ENABLED, enabled).apply()
    }

    fun getEffectiveProfile(): QualityProfile {
        return _metrics.value.effectiveProfile
    }

    fun onFrameRendered() {
        val nowNano = System.nanoTime()
        if (lastFrameTimestampNano != 0L) {
            val deltaMs = (nowNano - lastFrameTimestampNano) / 1_000_000f
            frameCountInInterval++
            val nowMs = System.currentTimeMillis()
            if (nowMs - lastFpsUpdateTimestamp >= 800) {
                val measuredFps = ((frameCountInInterval * 1000f) / (nowMs - lastFpsUpdateTimestamp)).roundToInt()
                frameCountInInterval = 0
                lastFpsUpdateTimestamp = nowMs

                _metrics.update {
                    it.copy(
                        fps = measuredFps.coerceIn(1, 144),
                        frameTimeMs = deltaMs.coerceIn(4f, 100f)
                    )
                }
            }
        }
        lastFrameTimestampNano = nowNano
    }

    private fun detectHardwareCapabilities() {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)

        val totalRamMb = memoryInfo.totalMem / (1024 * 1024)
        val maxHeapMb = Runtime.getRuntime().maxMemory() / (1024 * 1024)
        val cores = Runtime.getRuntime().availableProcessors()

        var refreshRate = 60f
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val display = context.display
                refreshRate = display?.refreshRate ?: 60f
            } else {
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                @Suppress("DEPRECATION")
                refreshRate = wm?.defaultDisplay?.refreshRate ?: 60f
            }
        } catch (_: Throwable) {}

        Log.d(TAG, "Hardware Detected: RAM=${totalRamMb}MB, Cores=$cores, Heap=${maxHeapMb}MB, RefreshRate=${refreshRate}Hz")

        val autoSelected = when {
            totalRamMb < 3000 || cores <= 4 -> QualityProfile.LOW
            totalRamMb < 6000 -> QualityProfile.MEDIUM
            totalRamMb < 10000 -> QualityProfile.HIGH
            else -> if (refreshRate > 75f) QualityProfile.ULTRA else QualityProfile.HIGH
        }

        _metrics.update {
            it.copy(
                totalMemoryMb = totalRamMb,
                maxHeapMb = maxHeapMb,
                cpuCores = cores,
                displayRefreshRate = refreshRate,
                effectiveProfile = if (_selectedProfile.value == QualityProfile.AUTO) autoSelected else _selectedProfile.value
            )
        }
    }

    private fun setupThermalMonitoring() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            try {
                powerManager?.addThermalStatusListener { status ->
                    val level = when (status) {
                        PowerManager.THERMAL_STATUS_NONE -> ThermalLevel.COOL
                        PowerManager.THERMAL_STATUS_LIGHT -> ThermalLevel.NORMAL
                        PowerManager.THERMAL_STATUS_MODERATE -> ThermalLevel.WARM
                        PowerManager.THERMAL_STATUS_SEVERE -> ThermalLevel.HOT
                        PowerManager.THERMAL_STATUS_CRITICAL,
                        PowerManager.THERMAL_STATUS_EMERGENCY,
                        PowerManager.THERMAL_STATUS_SHUTDOWN -> ThermalLevel.CRITICAL
                        else -> ThermalLevel.NORMAL
                    }
                    Log.i(TAG, "Thermal state changed: $level")
                    _metrics.update { it.copy(thermalLevel = level) }
                    recalculateEffectiveProfile()
                }
                thermalListenerRegistered = true
            } catch (e: Throwable) {
                Log.w(TAG, "Thermal listener registration skipped: ${e.message}")
            }
        }
    }

    private fun recalculateEffectiveProfile() {
        val chosen = _selectedProfile.value
        val currentThermal = _metrics.value.thermalLevel

        val baseEffective = if (chosen == QualityProfile.AUTO) {
            val totalRam = _metrics.value.totalMemoryMb
            when {
                totalRam < 3000 -> QualityProfile.LOW
                totalRam < 6000 -> QualityProfile.MEDIUM
                totalRam < 10000 -> QualityProfile.HIGH
                else -> QualityProfile.ULTRA
            }
        } else {
            chosen
        }

        // Adaptive Thermal Throttling Mitigation:
        // If device is HOT or CRITICAL, step down quality gracefully to avoid frame dips & heat
        val finalEffective = when {
            currentThermal.isSevere && baseEffective == QualityProfile.ULTRA -> QualityProfile.HIGH
            currentThermal.isSevere && baseEffective == QualityProfile.HIGH -> QualityProfile.MEDIUM
            currentThermal == ThermalLevel.CRITICAL -> QualityProfile.LOW
            else -> baseEffective
        }

        _metrics.update {
            it.copy(
                activeProfile = chosen,
                effectiveProfile = finalEffective
            )
        }
    }

    private fun startMetricsMonitoringLoop() {
        scope.launch {
            val runtime = Runtime.getRuntime()
            while (isActive) {
                val usedMem = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
                _metrics.update {
                    it.copy(usedMemoryMb = usedMem)
                }
                delay(1200)
            }
        }
    }

    fun cleanUp() {
        scope.cancel()
    }
}
