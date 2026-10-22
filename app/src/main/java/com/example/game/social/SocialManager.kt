package com.example.game.social

import android.content.Context
import com.example.game.crash.SafeDataManager
import com.example.game.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class SocialManager(private val context: Context) {

    private val saveFile = File(context.filesDir, "twenty_nine_social_data.json")

    private val _friends = MutableStateFlow<List<SocialUser>>(emptyList())
    val friends: StateFlow<List<SocialUser>> = _friends.asStateFlow()

    private val _incomingRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val incomingRequests: StateFlow<List<FriendRequest>> = _incomingRequests.asStateFlow()

    private val _suggestedPlayers = MutableStateFlow<List<SocialUser>>(emptyList())
    val suggestedPlayers: StateFlow<List<SocialUser>> = _suggestedPlayers.asStateFlow()

    private val _recentPlayers = MutableStateFlow<List<SocialUser>>(emptyList())
    val recentPlayers: StateFlow<List<SocialUser>> = _recentPlayers.asStateFlow()

    private val _blockedUsers = MutableStateFlow<List<BlockedUser>>(emptyList())
    val blockedUsers: StateFlow<List<BlockedUser>> = _blockedUsers.asStateFlow()

    init {
        loadSocialData()
    }

    private fun loadSocialData() {
        val jsonStr = SafeDataManager.readTextSafely(saveFile)
        if (jsonStr != null) {
            try {
                val root = JSONObject(jsonStr)
                // Load friends
                val friendsList = mutableListOf<SocialUser>()
                val fArr = root.optJSONArray("friends")
                if (fArr != null) {
                    for (i in 0 until fArr.length()) {
                        parseSocialUser(fArr.getJSONObject(i))?.let { friendsList.add(it) }
                    }
                }
                _friends.value = friendsList

                // Load requests
                val reqList = mutableListOf<FriendRequest>()
                val rArr = root.optJSONArray("incomingRequests")
                if (rArr != null) {
                    for (i in 0 until rArr.length()) {
                        val rObj = rArr.getJSONObject(i)
                        val sender = parseSocialUser(rObj.optJSONObject("sender"))
                        if (sender != null) {
                            reqList.add(
                                FriendRequest(
                                    requestId = rObj.getString("requestId"),
                                    sender = sender,
                                    timestamp = rObj.optLong("timestamp", System.currentTimeMillis()),
                                    status = RequestStatus.valueOf(rObj.optString("status", RequestStatus.PENDING.name))
                                )
                            )
                        }
                    }
                }
                _incomingRequests.value = reqList

                // Load blocked
                val bList = mutableListOf<BlockedUser>()
                val bArr = root.optJSONArray("blockedUsers")
                if (bArr != null) {
                    for (i in 0 until bArr.length()) {
                        val bObj = bArr.getJSONObject(i)
                        bList.add(
                            BlockedUser(
                                userId = bObj.getString("userId"),
                                username = bObj.getString("username"),
                                displayName = bObj.getString("displayName"),
                                blockedTimestamp = bObj.optLong("blockedTimestamp", System.currentTimeMillis())
                            )
                        )
                    }
                }
                _blockedUsers.value = bList

            } catch (_: Exception) {
                populateDefaults()
            }
        } else {
            populateDefaults()
        }

        // Always ensure suggested and recent players are filled with lively opponents
        populateSuggestedAndRecent()
    }

    private fun populateDefaults() {
        _friends.value = listOf(
            SocialUser(
                id = "u_101",
                username = "@KolkataRaja",
                displayName = "Raja Mukherjee",
                avatarId = "avatar_king",
                level = 16,
                vipTitle = "Grandmaster VIP",
                status = UserStatus.IN_MATCH,
                gamesWon = 142,
                winRate = 68,
                lastActive = "Live Now",
                liveActivity = "Trick 5/8 • 20 Bid • vs MumbaiAce",
                roomCode = "2948"
            ),
            SocialUser(
                id = "u_102",
                username = "@QueenOfHearts",
                displayName = "Priya Sen",
                avatarId = "avatar_queen",
                level = 14,
                vipTitle = "High Roller VIP",
                status = UserStatus.HOSTING_ROOM,
                gamesWon = 118,
                winRate = 65,
                lastActive = "Live Now",
                liveActivity = "Hosting Private Room #8812 (Waiting for 1 player)",
                roomCode = "8812"
            ),
            SocialUser(
                id = "u_105",
                username = "@MumbaiAce",
                displayName = "Dev Sharma",
                avatarId = "avatar_ace",
                level = 15,
                vipTitle = "Platinum VIP",
                status = UserStatus.IN_MATCH,
                gamesWon = 135,
                winRate = 70,
                lastActive = "Live Now",
                liveActivity = "Round 4/6 • Leading 4-2 Pips",
                roomCode = "2948"
            ),
            SocialUser(
                id = "u_104",
                username = "@DelhiSultan",
                displayName = "Vikram Malhotra",
                avatarId = "avatar_player",
                level = 12,
                vipTitle = "Gold VIP",
                status = UserStatus.ONLINE,
                gamesWon = 110,
                winRate = 59,
                lastActive = "Live Now",
                liveActivity = "Browsing High Roller Lobby",
                roomCode = null
            ),
            SocialUser(
                id = "u_103",
                username = "@BengalTiger29",
                displayName = "Arjun Roy",
                avatarId = "avatar_tiger",
                level = 8,
                vipTitle = "Silver VIP",
                status = UserStatus.OFFLINE,
                gamesWon = 54,
                winRate = 55,
                lastActive = "2 hours ago",
                liveActivity = "Offline",
                roomCode = null
            )
        )

        _incomingRequests.value = listOf(
            FriendRequest(
                requestId = "req_201",
                sender = SocialUser("u_106", "@DiamondQueen", "Ananya Das", "avatar_diamond", 10, "Gold VIP", UserStatus.ONLINE, 88, 61, liveActivity = "In Lobby")
            )
        )

        saveSocialData()
    }

    private fun populateSuggestedAndRecent() {
        _suggestedPlayers.value = listOf(
            SocialUser("u_301", "@MumbaiAce", "Dev Sharma", "avatar_ace", 16, "Grandmaster VIP", UserStatus.ONLINE, 185, 71),
            SocialUser("u_302", "@DiamondQueen", "Ananya Das", "avatar_diamond", 9, "Gold VIP", UserStatus.ONLINE, 78, 58),
            SocialUser("u_303", "@TrickTaker29", "Rohit Sen", "avatar_king", 13, "Platinum VIP", UserStatus.OFFLINE, 120, 64, "1 hour ago")
        )

        _recentPlayers.value = listOf(
            SocialUser("u_401", "@ChennaiExpress", "Karthik R.", "avatar_tiger", 7, "Silver VIP", UserStatus.OFFLINE, 46, 52, "Yesterday"),
            SocialUser("u_402", "@RoyalJoker", "Sunil Ghosh", "avatar_player", 10, "Gold VIP", UserStatus.ONLINE, 89, 60)
        )
    }

    private fun saveSocialData() {
        try {
            val root = JSONObject()

            val fArr = JSONArray()
            _friends.value.forEach { f ->
                fArr.put(serializeSocialUser(f))
            }
            root.put("friends", fArr)

            val rArr = JSONArray()
            _incomingRequests.value.forEach { r ->
                val rObj = JSONObject()
                rObj.put("requestId", r.requestId)
                rObj.put("sender", serializeSocialUser(r.sender))
                rObj.put("timestamp", r.timestamp)
                rObj.put("status", r.status.name)
                rArr.put(rObj)
            }
            root.put("incomingRequests", rArr)

            val bArr = JSONArray()
            _blockedUsers.value.forEach { b ->
                val bObj = JSONObject()
                bObj.put("userId", b.userId)
                bObj.put("username", b.username)
                bObj.put("displayName", b.displayName)
                bObj.put("blockedTimestamp", b.blockedTimestamp)
                bArr.put(bObj)
            }
            root.put("blockedUsers", bArr)

            SafeDataManager.writeTextSafely(saveFile, root.toString(2))
        } catch (_: Exception) {}
    }

    fun acceptFriendRequest(requestId: String) {
        val req = _incomingRequests.value.find { it.requestId == requestId } ?: return
        _friends.update { it + req.sender }
        _incomingRequests.update { list -> list.filter { it.requestId != requestId } }
        saveSocialData()
    }

    fun addFriend(user: SocialUser) {
        if (!_friends.value.any { it.id == user.id }) {
            _friends.update { it + user }
            _suggestedPlayers.update { list -> list.filter { it.id != user.id } }
            _recentPlayers.update { list -> list.filter { it.id != user.id } }
            saveSocialData()
        }
    }

    fun declineFriendRequest(requestId: String) {
        _incomingRequests.update { list -> list.filter { it.requestId != requestId } }
        saveSocialData()
    }

    fun removeFriend(userId: String) {
        _friends.update { list -> list.filter { it.id != userId } }
        saveSocialData()
    }

    fun blockUser(user: SocialUser) {
        removeFriend(user.id)
        _incomingRequests.update { list -> list.filter { it.sender.id != user.id } }
        _suggestedPlayers.update { list -> list.filter { it.id != user.id } }
        _recentPlayers.update { list -> list.filter { it.id != user.id } }
        _blockedUsers.update { it + BlockedUser(user.id, user.username, user.displayName) }
        saveSocialData()
    }

    fun unblockUser(userId: String) {
        _blockedUsers.update { list -> list.filter { it.userId != userId } }
        saveSocialData()
    }

    fun sendFriendRequest(user: SocialUser) {
        // Move from suggested/recent to optimistic pending feedback
        _suggestedPlayers.update { list -> list.filter { it.id != user.id } }
        _recentPlayers.update { list -> list.filter { it.id != user.id } }
        // In real backend, network call fires here. In local manager, add simulated success
    }

    private fun serializeSocialUser(u: SocialUser): JSONObject {
        return JSONObject().apply {
            put("id", u.id)
            put("username", u.username)
            put("displayName", u.displayName)
            put("avatarId", u.avatarId)
            put("level", u.level)
            put("vipTitle", u.vipTitle)
            put("status", u.status.name)
            put("gamesWon", u.gamesWon)
            put("winRate", u.winRate)
            put("lastActive", u.lastActive)
            put("liveActivity", u.liveActivity)
            put("roomCode", u.roomCode ?: "")
        }
    }

    private fun parseSocialUser(obj: JSONObject?): SocialUser? {
        if (obj == null) return null
        return try {
            val rCode = obj.optString("roomCode", "")
            SocialUser(
                id = obj.getString("id"),
                username = obj.getString("username"),
                displayName = obj.getString("displayName"),
                avatarId = obj.optString("avatarId", "avatar_player"),
                level = obj.optInt("level", 1),
                vipTitle = obj.optString("vipTitle", "Classic"),
                status = UserStatus.valueOf(obj.optString("status", UserStatus.OFFLINE.name)),
                gamesWon = obj.optInt("gamesWon", 0),
                winRate = obj.optInt("winRate", 50),
                lastActive = obj.optString("lastActive", "Recently"),
                liveActivity = obj.optString("liveActivity", "Waiting in Lobby"),
                roomCode = if (rCode.isNotEmpty()) rCode else null
            )
        } catch (_: Exception) {
            null
        }
    }
}
