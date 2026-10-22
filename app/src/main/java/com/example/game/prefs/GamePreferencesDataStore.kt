package com.example.game.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "twenty_nine_game_settings")

/**
 * DataStore repository for persisting player settings including independent toggles
 * for Background Music (BGM), Sound Effects (SFX), Haptics, and Visual configurations.
 */
class GamePreferencesDataStore(private val context: Context) {

    companion object {
        val KEY_MUSIC_ENABLED = booleanPreferencesKey("bg_music_enabled")
        val KEY_SOUND_EFFECTS_ENABLED = booleanPreferencesKey("sound_effects_enabled")
        val KEY_HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val KEY_FELT_THEME = stringPreferencesKey("felt_theme")
        val KEY_QUALITY_PROFILE = stringPreferencesKey("quality_profile")
        val KEY_AI_DIFFICULTY = stringPreferencesKey("ai_difficulty")
        val KEY_FPS_OVERLAY = booleanPreferencesKey("fps_overlay_enabled")
    }

    val isMusicEnabledFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_MUSIC_ENABLED] ?: true
        }

    val isSoundEffectsEnabledFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_SOUND_EFFECTS_ENABLED] ?: true
        }

    val isHapticsEnabledFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_HAPTICS_ENABLED] ?: true
        }

    val feltThemeFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_FELT_THEME] ?: "EMERALD_CASINO"
        }

    val qualityProfileFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_QUALITY_PROFILE] ?: "AUTO"
        }

    val aiDifficultyFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_AI_DIFFICULTY] ?: "ADAPTIVE"
        }

    val isFpsOverlayEnabledFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_FPS_OVERLAY] ?: false
        }

    suspend fun setMusicEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_MUSIC_ENABLED] = enabled
        }
    }

    suspend fun setSoundEffectsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SOUND_EFFECTS_ENABLED] = enabled
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun setFeltTheme(themeName: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_FELT_THEME] = themeName
        }
    }

    suspend fun setQualityProfile(profileName: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_QUALITY_PROFILE] = profileName
        }
    }

    suspend fun setAiDifficulty(diffName: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AI_DIFFICULTY] = diffName
        }
    }

    suspend fun setFpsOverlayEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_FPS_OVERLAY] = enabled
        }
    }
}
