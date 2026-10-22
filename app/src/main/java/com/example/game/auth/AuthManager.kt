package com.example.game.auth

import android.content.Context
import com.example.game.crash.SafeDataManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.io.File
import java.util.UUID

enum class AccountType(val label: String) {
    GUEST("Guest Account"),
    GOOGLE("Google Account"),
    EMAIL("Email Account")
}

enum class CloudSyncStatus(val label: String) {
    SYNCED("Cloud Vault Synced"),
    SYNCING("Syncing with Cloud..."),
    OFFLINE("Local Cache (Offline)"),
    FAILED("Sync Paused")
}

data class AuthSession(
    val userId: String,
    val accountType: AccountType,
    val email: String? = null,
    val isLinked: Boolean = accountType != AccountType.GUEST,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)

class AuthManager(private val context: Context) {

    private val authFile = File(context.filesDir, "twenty_nine_auth_session.json")

    private val _session = MutableStateFlow(loadSession())
    val session: StateFlow<AuthSession> = _session.asStateFlow()

    private val _syncStatus = MutableStateFlow(CloudSyncStatus.SYNCED)
    val syncStatus: StateFlow<CloudSyncStatus> = _syncStatus.asStateFlow()

    private fun loadSession(): AuthSession {
        val jsonStr = SafeDataManager.readTextSafely(authFile)
        if (jsonStr != null) {
            try {
                val obj = JSONObject(jsonStr)
                return AuthSession(
                    userId = obj.getString("userId"),
                    accountType = AccountType.valueOf(obj.getString("accountType")),
                    email = if (obj.has("email") && !obj.isNull("email")) obj.getString("email") else null,
                    isLinked = obj.optBoolean("isLinked", false),
                    lastSyncTimestamp = obj.optLong("lastSyncTimestamp", System.currentTimeMillis())
                )
            } catch (_: Exception) {}
        }
        // Initial guest session
        val guest = AuthSession(
            userId = "guest_${UUID.randomUUID().toString().take(8)}",
            accountType = AccountType.GUEST,
            email = null,
            isLinked = false
        )
        saveSession(guest)
        return guest
    }

    private fun saveSession(session: AuthSession) {
        try {
            val obj = JSONObject().apply {
                put("userId", session.userId)
                put("accountType", session.accountType.name)
                put("email", session.email ?: "")
                put("isLinked", session.isLinked)
                put("lastSyncTimestamp", session.lastSyncTimestamp)
            }
            SafeDataManager.writeTextSafely(authFile, obj.toString())
        } catch (_: Exception) {}
    }

    fun linkGoogleAccount(email: String, googleId: String) {
        val updated = _session.value.copy(
            userId = "google_$googleId",
            accountType = AccountType.GOOGLE,
            email = email,
            isLinked = true,
            lastSyncTimestamp = System.currentTimeMillis()
        )
        _session.value = updated
        saveSession(updated)
        _syncStatus.value = CloudSyncStatus.SYNCED
    }

    fun linkEmailAccount(email: String) {
        val updated = _session.value.copy(
            userId = "email_${email.hashCode()}",
            accountType = AccountType.EMAIL,
            email = email,
            isLinked = true,
            lastSyncTimestamp = System.currentTimeMillis()
        )
        _session.value = updated
        saveSession(updated)
        _syncStatus.value = CloudSyncStatus.SYNCED
    }

    fun setSyncStatus(status: CloudSyncStatus) {
        _syncStatus.value = status
    }
}
