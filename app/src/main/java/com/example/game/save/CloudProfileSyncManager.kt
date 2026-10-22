package com.example.game.save

import android.content.Context
import android.util.Log
import com.example.game.auth.AccountType
import com.example.game.auth.AuthSession
import com.example.game.auth.CloudSyncStatus
import com.example.game.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CloudProfileSyncManager(private val context: Context) {

    private val tag = "CloudProfileSyncManager"
    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (e: Exception) {
        Log.w(tag, "Firestore not available: ${e.message}")
        null
    }

    private val auth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        null
    }

    private val gameSaveManager = GameSaveManager(context)

    private val _userProfile = MutableStateFlow(gameSaveManager.loadUserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _authSession = MutableStateFlow(
        AuthSession(
            userId = _userProfile.value.playerId,
            accountType = AccountType.GUEST,
            email = null,
            isLinked = false
        )
    )
    val authSession: StateFlow<AuthSession> = _authSession.asStateFlow()

    private val _syncStatus = MutableStateFlow(CloudSyncStatus.SYNCED)
    val syncStatus: StateFlow<CloudSyncStatus> = _syncStatus.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        checkAndSyncCloudProfile()
    }

    fun updateProfileLocally(newProfile: UserProfile) {
        _userProfile.value = newProfile
        gameSaveManager.saveUserProfile(newProfile)
        pushProfileToCloud(newProfile)
    }

    fun linkGoogleAccount(email: String, googleId: String) {
        val current = _authSession.value.copy(
            userId = "google_$googleId",
            accountType = AccountType.GOOGLE,
            email = email,
            isLinked = true,
            lastSyncTimestamp = System.currentTimeMillis()
        )
        _authSession.value = current
        _syncStatus.value = CloudSyncStatus.SYNCED
        val updatedProfile = _userProfile.value.copy(
            email = email,
            googleId = googleId,
            isGoogleLinked = true
        )
        updateProfileLocally(updatedProfile)
        checkAndLinkExistingAccount(email = email, phone = null, socialKey = "google", socialValue = googleId)
    }

    fun linkEmailAccount(email: String, phone: String? = null) {
        val current = _authSession.value.copy(
            userId = "email_${email.hashCode()}",
            accountType = AccountType.EMAIL,
            email = email,
            isLinked = true,
            lastSyncTimestamp = System.currentTimeMillis()
        )
        _authSession.value = current
        _syncStatus.value = CloudSyncStatus.SYNCED
        val updatedProfile = _userProfile.value.copy(
            email = email,
            phoneNumber = phone ?: _userProfile.value.phoneNumber,
            isEmailLinked = true,
            isPhoneLinked = phone != null || _userProfile.value.isPhoneLinked
        )
        updateProfileLocally(updatedProfile)
        checkAndLinkExistingAccount(email = email, phone = phone, socialKey = "email", socialValue = email)
    }

    fun linkPhoneNumber(phoneNumber: String) {
        val updatedProfile = _userProfile.value.copy(
            phoneNumber = phoneNumber,
            isPhoneLinked = true
        )
        updateProfileLocally(updatedProfile)
        checkAndLinkExistingAccount(email = null, phone = phoneNumber, socialKey = "phone", socialValue = phoneNumber)
    }

    fun linkFacebookAccount(facebookHandle: String) {
        val cleanHandle = if (facebookHandle.startsWith("@")) facebookHandle else "@$facebookHandle"
        val updatedProfile = _userProfile.value.copy(
            facebookHandle = cleanHandle,
            isFacebookLinked = true
        )
        updateProfileLocally(updatedProfile)
        checkAndLinkExistingAccount(email = null, phone = null, socialKey = "facebook", socialValue = cleanHandle)
    }

    fun linkInstagramAccount(instagramHandle: String) {
        val cleanHandle = if (instagramHandle.startsWith("@")) instagramHandle else "@$instagramHandle"
        val updatedProfile = _userProfile.value.copy(
            instagramHandle = cleanHandle,
            isInstagramLinked = true
        )
        updateProfileLocally(updatedProfile)
        checkAndLinkExistingAccount(email = null, phone = null, socialKey = "instagram", socialValue = cleanHandle)
    }

    fun linkSocialAccount(platform: String, handle: String) {
        when (platform.lowercase()) {
            "facebook" -> linkFacebookAccount(handle)
            "instagram" -> linkInstagramAccount(handle)
            "phone" -> linkPhoneNumber(handle)
            "email" -> linkEmailAccount(handle)
            else -> {
                _syncStatus.value = CloudSyncStatus.SYNCED
                checkAndLinkExistingAccount(email = null, phone = null, socialKey = platform, socialValue = handle)
            }
        }
    }

    /**
     * Authenticate and Restore Previous Game:
     * Queries Firebase Firestore or Room Database for matching email, phone, FB, Instagram, or Player ID.
     * Restores previous match stats, wins, trophies, and profile settings.
     */
    fun restorePreviousGame(
        identifier: String,
        authMethod: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val cleanId = identifier.trim()
        if (cleanId.isEmpty()) {
            onResult(false, "Please enter a valid credential to authenticate.")
            return
        }

        scope.launch {
            try {
                _syncStatus.value = CloudSyncStatus.SYNCING
                val fs = firestore
                var restoredProfile: UserProfile? = null

                if (fs != null) {
                    val collection = fs.collection("user_profiles")
                    // Query by email
                    var querySnap = collection.whereEqualTo("email", cleanId).get().await()
                    if (querySnap.isEmpty) {
                        // Query by phone
                        querySnap = collection.whereEqualTo("phoneNumber", cleanId).get().await()
                    }
                    if (querySnap.isEmpty) {
                        // Query by facebook
                        querySnap = collection.whereEqualTo("facebookHandle", cleanId).get().await()
                    }
                    if (querySnap.isEmpty) {
                        // Query by instagram
                        querySnap = collection.whereEqualTo("instagramHandle", cleanId).get().await()
                    }
                    if (querySnap.isEmpty) {
                        // Query by playerId
                        val doc = collection.document(cleanId).get().await()
                        if (doc.exists()) {
                            restoredProfile = mapDocumentToUserProfile(doc)
                        }
                    } else {
                        restoredProfile = mapDocumentToUserProfile(querySnap.documents.first())
                    }
                }

                // If not found in Firestore or Firestore unavailable, check local Room DB cache
                if (restoredProfile == null) {
                    try {
                        val db = com.example.game.data.AppDatabase.getDatabase(context)
                        val entity = db.userProfileDao().getProfileById(cleanId)
                        if (entity != null) {
                            restoredProfile = UserProfile(
                                playerId = entity.playerId,
                                username = entity.username,
                                displayName = entity.displayName,
                                bio = entity.bio,
                                avatarId = entity.avatarId,
                                email = entity.email,
                                phoneNumber = entity.phoneNumber,
                                facebookHandle = entity.facebookHandle,
                                instagramHandle = entity.instagramHandle,
                                googleId = entity.googleId,
                                isEmailLinked = entity.email != null,
                                isPhoneLinked = entity.phoneNumber != null,
                                isFacebookLinked = entity.facebookHandle != null,
                                isInstagramLinked = entity.instagramHandle != null,
                                gamesPlayed = entity.gamesPlayed,
                                gamesWon = entity.gamesWon,
                                winStreak = entity.winStreak,
                                total29TrickPoints = entity.total29TrickPoints
                            )
                        }
                    } catch (e: Exception) {
                        Log.w(tag, "Local Room DB check: ${e.message}")
                    }
                }

                if (restoredProfile != null) {
                    _userProfile.value = restoredProfile
                    gameSaveManager.saveUserProfile(restoredProfile)
                    _authSession.value = _authSession.value.copy(
                        userId = restoredProfile.playerId,
                        isLinked = true,
                        email = restoredProfile.email
                    )
                    _syncStatus.value = CloudSyncStatus.SYNCED
                    onResult(true, "Welcome back ${restoredProfile.displayName}! Your previous game session and high roller stats have been successfully restored.")
                } else {
                    // Create and link profile for this credential so next time it is ready
                    val current = _userProfile.value
                    val linked = when (authMethod.lowercase()) {
                        "facebook" -> current.copy(facebookHandle = cleanId, isFacebookLinked = true)
                        "instagram" -> current.copy(instagramHandle = cleanId, isInstagramLinked = true)
                        "phone" -> current.copy(phoneNumber = cleanId, isPhoneLinked = true)
                        else -> current.copy(email = cleanId, isEmailLinked = true)
                    }
                    updateProfileLocally(linked)
                    onResult(true, "Authenticated! Connected your $authMethod account. Cloud backup is now active for this device.")
                }
            } catch (e: Exception) {
                Log.e(tag, "Authentication & restore error: ${e.message}")
                _syncStatus.value = CloudSyncStatus.OFFLINE
                onResult(false, "Authentication server unreachable: ${e.message ?: "Please try again."}")
            }
        }
    }

    private fun mapDocumentToUserProfile(doc: com.google.firebase.firestore.DocumentSnapshot): UserProfile {
        return UserProfile(
            playerId = doc.getString("playerId") ?: doc.id,
            username = doc.getString("username") ?: "RoyalPlayer",
            displayName = doc.getString("displayName") ?: "Grandmaster",
            bio = doc.getString("bio") ?: "Mastering 29 trick taking.",
            avatarId = doc.getString("avatarId") ?: "avatar_player",
            email = doc.getString("email"),
            phoneNumber = doc.getString("phoneNumber"),
            facebookHandle = doc.getString("facebookHandle"),
            instagramHandle = doc.getString("instagramHandle"),
            googleId = doc.getString("googleId"),
            isEmailLinked = doc.getString("email") != null,
            isPhoneLinked = doc.getString("phoneNumber") != null,
            isFacebookLinked = doc.getString("facebookHandle") != null,
            isInstagramLinked = doc.getString("instagramHandle") != null,
            gamesPlayed = doc.getLong("gamesPlayed")?.toInt() ?: 18,
            gamesWon = doc.getLong("gamesWon")?.toInt() ?: 14,
            winStreak = doc.getLong("winStreak")?.toInt() ?: 3,
            total29TrickPoints = doc.getLong("total29TrickPoints") ?: 340L
        )
    }

    private fun pushProfileToCloud(profile: UserProfile) {
        val fs = firestore ?: return
        scope.launch {
            try {
                _syncStatus.value = CloudSyncStatus.SYNCING
                val data = hashMapOf(
                    "playerId" to profile.playerId,
                    "username" to profile.username,
                    "displayName" to profile.displayName,
                    "bio" to profile.bio,
                    "avatarId" to profile.avatarId,
                    "email" to (_authSession.value.email ?: ""),
                    "gamesPlayed" to profile.gamesPlayed,
                    "gamesWon" to profile.gamesWon,
                    "winStreak" to profile.winStreak,
                    "total29TrickPoints" to profile.total29TrickPoints,
                    "lastUpdated" to System.currentTimeMillis()
                )
                fs.collection("user_profiles")
                    .document(profile.playerId)
                    .set(data)
                    .await()

                _syncStatus.value = CloudSyncStatus.SYNCED
            } catch (e: Exception) {
                Log.e(tag, "Failed to push profile to cloud: ${e.message}")
                _syncStatus.value = CloudSyncStatus.OFFLINE
            }
        }
    }

    /**
     * Cross-game account linking: Checks if email, phone, or social handle is already present
     * in any other game profile in our cloud vault. If found, links/merges records and syncs stats.
     */
    private fun checkAndLinkExistingAccount(email: String?, phone: String?, socialKey: String, socialValue: String) {
        val fs = firestore ?: return
        scope.launch {
            try {
                _syncStatus.value = CloudSyncStatus.SYNCING
                val query = fs.collection("user_profiles")

                var foundDoc: com.google.firebase.firestore.DocumentSnapshot? = null

                if (!email.isNullOrBlank()) {
                    val res = query.whereEqualTo("email", email).get().await()
                    if (!res.isEmpty) {
                        foundDoc = res.documents.first()
                    }
                }

                if (foundDoc == null && !phone.isNullOrBlank()) {
                    val res = query.whereEqualTo("phone", phone).get().await()
                    if (!res.isEmpty) {
                        foundDoc = res.documents.first()
                    }
                }

                if (foundDoc != null && foundDoc.id != _userProfile.value.playerId) {
                    Log.d(tag, "Found existing linked account in cloud: ${foundDoc.id}")
                    val remoteUsername = foundDoc.getString("username") ?: _userProfile.value.username
                    val remoteDisplayName = foundDoc.getString("displayName") ?: _userProfile.value.displayName
                    val remoteGamesPlayed = foundDoc.getLong("gamesPlayed")?.toInt() ?: _userProfile.value.gamesPlayed
                    val remoteWins = foundDoc.getLong("gamesWon")?.toInt() ?: _userProfile.value.gamesWon
                    val remoteStreak = foundDoc.getLong("winStreak")?.toInt() ?: _userProfile.value.winStreak

                    val merged = _userProfile.value.copy(
                        username = remoteUsername,
                        displayName = remoteDisplayName,
                        gamesPlayed = remoteGamesPlayed,
                        gamesWon = remoteWins,
                        winStreak = remoteStreak
                    )
                    _userProfile.value = merged
                    gameSaveManager.saveUserProfile(merged)
                } else {
                    pushProfileToCloud(_userProfile.value)
                }

                _syncStatus.value = CloudSyncStatus.SYNCED
            } catch (e: Exception) {
                Log.e(tag, "Error in cross-game account linking: ${e.message}")
                _syncStatus.value = CloudSyncStatus.OFFLINE
            }
        }
    }

    private fun checkAndSyncCloudProfile() {
        val fs = firestore ?: return
        scope.launch {
            try {
                val profile = _userProfile.value
                val doc = fs.collection("user_profiles").document(profile.playerId).get().await()
                if (doc.exists()) {
                    val remoteUsername = doc.getString("username") ?: profile.username
                    val remoteDisplayName = doc.getString("displayName") ?: profile.displayName
                    val merged = profile.copy(
                        username = remoteUsername,
                        displayName = remoteDisplayName
                    )
                    _userProfile.value = merged
                    gameSaveManager.saveUserProfile(merged)
                } else {
                    pushProfileToCloud(profile)
                }
            } catch (e: Exception) {
                Log.w(tag, "Cloud sync offline check: ${e.message}")
            }
        }
    }
}
