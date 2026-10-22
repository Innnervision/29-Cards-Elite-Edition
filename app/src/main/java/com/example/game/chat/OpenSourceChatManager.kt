package com.example.game.chat

import android.content.Context
import com.example.game.data.AppDatabase
import com.example.game.data.ChatMessageEntity
import com.example.game.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LiveChatMessage(
    val id: String = "${System.currentTimeMillis()}_${(100..999).random()}",
    val roomCode: String = "TABLE_LIVE",
    val senderId: String,
    val senderName: String,
    val senderAvatar: String,
    val text: String,
    val isSystemNotice: Boolean = false,
    val emojiReaction: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Open Source Real-Time Chat & Tactical In-Game Banter Engine
 * Based on open-source pub/sub chat patterns (Matrix / IRC / WebSocket).
 * Persists chat messages to local Room SQLite database and supports tactical table banter.
 */
class OpenSourceChatManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val db = AppDatabase.getDatabase(context)

    private val _messages = MutableStateFlow<List<LiveChatMessage>>(emptyList())
    val messages: StateFlow<List<LiveChatMessage>> = _messages.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    init {
        populateInitialTacticalBanter()
    }

    private fun populateInitialTacticalBanter() {
        val initialList = listOf(
            LiveChatMessage(
                senderId = "system_01",
                senderName = "ROYAL ARENA",
                senderAvatar = "👑",
                text = "Welcome to the 29 High Roller Table. Play fair and respect your partner!",
                isSystemNotice = true,
                timestamp = System.currentTimeMillis() - 60000
            ),
            LiveChatMessage(
                senderId = "partner_vikram",
                senderName = "Vikram Malhotra",
                senderAvatar = "🤴",
                text = "Partner! Lead your high cards early if you have trumps.",
                timestamp = System.currentTimeMillis() - 40000
            ),
            LiveChatMessage(
                senderId = "opp_dev",
                senderName = "Dev Sharma",
                senderAvatar = "🐯",
                text = "Good luck everyone. Don't let me get the 20 bid! 🔥",
                timestamp = System.currentTimeMillis() - 20000
            )
        )
        _messages.value = initialList
    }

    fun sendMessage(userProfile: UserProfile, text: String, roomCode: String = "TABLE_LIVE") {
        val clean = text.trim()
        if (clean.isEmpty()) return

        val msg = LiveChatMessage(
            roomCode = roomCode,
            senderId = userProfile.playerId,
            senderName = userProfile.displayName,
            senderAvatar = userProfile.avatarId,
            text = clean,
            timestamp = System.currentTimeMillis()
        )

        _messages.update { current ->
            (current + msg).takeLast(60)
        }

        // Persist to Room SQLite Database
        scope.launch {
            try {
                db.chatMessageDao().insertMessage(
                    ChatMessageEntity(
                        roomCode = roomCode,
                        senderId = userProfile.playerId,
                        senderName = userProfile.displayName,
                        senderAvatar = userProfile.avatarId,
                        messageText = clean,
                        timestamp = System.currentTimeMillis()
                    )
                )
            } catch (_: Exception) {}
        }
    }

    fun sendSystemNotice(noticeText: String, roomCode: String = "TABLE_LIVE") {
        val notice = LiveChatMessage(
            roomCode = roomCode,
            senderId = "system_notice",
            senderName = "TABLE REFEREE",
            senderAvatar = "⚖️",
            text = noticeText,
            isSystemNotice = true,
            timestamp = System.currentTimeMillis()
        )
        _messages.update { (it + notice).takeLast(60) }
        _unreadCount.update { it + 1 }
    }

    fun addReaction(messageId: String, emoji: String) {
        _messages.update { list ->
            list.map { msg ->
                if (msg.id == messageId) msg.copy(emojiReaction = emoji) else msg
            }
        }
    }

    fun markAllRead() {
        _unreadCount.value = 0
    }

    fun clearChat() {
        _messages.value = emptyList()
        _unreadCount.value = 0
    }
}
