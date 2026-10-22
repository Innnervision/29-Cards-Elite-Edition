package com.example.game.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val playerId: String,
    val username: String,
    val displayName: String,
    val bio: String,
    val avatarId: String,
    val email: String? = null,
    val phoneNumber: String? = null,
    val facebookHandle: String? = null,
    val instagramHandle: String? = null,
    val googleId: String? = null,
    val coins: Long = 10000L,
    val diamonds: Int = 100,
    val vipTier: String = "BRONZE",
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val winStreak: Int = 0,
    val total29TrickPoints: Long = 0L,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE playerId = :id LIMIT 1")
    suspend fun getProfileById(id: String): UserProfileEntity?

    @Query("SELECT * FROM user_profiles ORDER BY lastUpdated DESC LIMIT 1")
    fun getLatestProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)
}

@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey val id: String,
    val username: String,
    val displayName: String,
    val avatarId: String,
    val level: Int,
    val vipTitle: String,
    val status: String,
    val gamesWon: Int,
    val winRate: Int,
    val lastActive: String,
    val liveActivity: String = "Waiting in Lobby",
    val roomCode: String? = null
)

@Dao
interface FriendDao {
    @Query("SELECT * FROM friends")
    fun getAllFriends(): Flow<List<FriendEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriends(friends: List<FriendEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: FriendEntity)

    @Query("DELETE FROM friends WHERE id = :friendId")
    suspend fun deleteFriend(friendId: String)
}

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomCode: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String,
    val messageText: String,
    val emojiReaction: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE roomCode = :roomCode ORDER BY timestamp ASC LIMIT 50")
    fun getMessagesForRoom(roomCode: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE roomCode = :roomCode")
    suspend fun clearRoomChat(roomCode: String)
}
