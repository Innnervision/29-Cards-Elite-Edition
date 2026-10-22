package com.example.game.firebase

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.example.game.crash.ErrorManager
import com.example.game.model.MatchMode
import com.example.game.model.UserProfile
import com.example.game.performance.QualityProfile

object FirebaseGameBridge {

    private const val TAG = "FirebaseGameBridge"

    fun logEvent(context: Context, eventName: String, params: Map<String, Any> = emptyMap()) {
        ErrorManager.recordBreadcrumb("Analytics", "Event: $eventName", params.mapValues { it.value.toString() })
        Log.d(TAG, "Logged Analytics Event: $eventName, params: $params")
    }

    fun logGameStarted(context: Context, mode: MatchMode, quality: QualityProfile) {
        logEvent(context, "game_started", mapOf(
            "mode" to mode.name,
            "quality_profile" to quality.name,
            "device_model" to Build.MODEL,
            "android_version" to Build.VERSION.SDK_INT
        ))
    }

    fun logMatchCompleted(context: Context, mode: MatchMode, won: Boolean, scoreA: Int, scoreB: Int) {
        logEvent(context, "match_completed", mapOf(
            "mode" to mode.name,
            "won" to won,
            "team_a_score" to scoreA,
            "team_b_score" to scoreB
        ))
    }

    fun logFriendRequestSent(context: Context, targetUserId: String) {
        logEvent(context, "friend_request_sent", mapOf("target_user_id" to targetUserId))
    }

    fun logInviteShared(context: Context, playerCode: String) {
        logEvent(context, "invite_sent", mapOf("player_code" to playerCode))
    }

    fun logAchievementUnlocked(context: Context, achievementId: String) {
        logEvent(context, "achievement_unlocked", mapOf("achievement_id" to achievementId))
    }

    fun setCrashlyticsCustomKeys(userId: String, qualityProfile: QualityProfile) {
        ErrorManager.recordBreadcrumb("CrashlyticsKey", "User: $userId, Profile: ${qualityProfile.name}")
    }
}
