package com.example.snapquest.repositories

import android.util.Log
import com.example.snapquest.models.Notification
import com.example.snapquest.models.User
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreUserRepository @Inject constructor() {
    private val db: FirebaseFirestore = Firebase.firestore
    private val usersRef = db.collection("users")

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    suspend fun fetchCurrentUser(uid: String): User? {
        return try {
            if (uid.isBlank()) return null

            val snapshot = usersRef.document(uid).get().await()
            if (snapshot.exists()) {
                snapshot.toObject(User::class.java)?.copy(
                    uid = uid,
                    questsCompleted = snapshot.getLong("questsCompleted")?.toInt() ?: 0,
                    challengesCompleted = snapshot.getLong("challengesCompleted")?.toInt() ?: 0
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("FirestoreUserRepo", "Error fetching user", e)
            null
        }
    }

    suspend fun registerUser(user: User): Result<Unit> {
        return try {
            require(user.uid.isNotBlank()) { "User ID cannot be blank" }

            val userData = hashMapOf(
                "uid" to user.uid,
                "name" to user.name,
                "email" to user.email,
                "photoUrl" to user.photoUrl,
                "isAdmin" to user.isAdmin,
                "questsCompleted" to user.questsCompleted,
                "challengesCompleted" to user.challengesCompleted,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            usersRef.document(user.uid)
                .set(userData, SetOptions.merge())
                .await()

            _currentUser.value = user
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreUserRepo", "Error ${e.stackTraceToString()}", e)
            Result.failure(e)
        }
    }

    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            val updateData = mapOf(
                "name" to user.name,
                "email" to user.email,
                "photoUrl" to user.photoUrl,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            usersRef.document(user.uid)
                .update(updateData)
                .await()

            _currentUser.value = user
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreUserRepo", "Error updating user", e)
            Result.failure(e)
        }
    }

    suspend fun deleteUser(uid: String): Result<Unit> {
        return try {
            usersRef.document(uid).delete().await()
            _currentUser.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreUserRepo", "Error deleting user", e)
            Result.failure(e)
        }
    }

    suspend fun getUsersByIds(userIds: List<String>): List<User> {
        return try {
            if (userIds.isEmpty()) return emptyList()

            userIds.chunked(10).flatMap { chunk ->
                usersRef
                    .whereIn(FieldPath.documentId(), chunk)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { doc ->
                        doc.toObject(User::class.java)?.copy(
                            uid = doc.id,
                            name = doc.getString("name") ?: "",
                            email = doc.getString("email") ?: "",
                            photoUrl = doc.getString("photoUrl") ?: ""
                        ).also {
                            if (it != null) {
                                Log.d("FirestoreUserRepo", "Loaded user: ${it.uid}")
                            }
                        }
                    }
            }
        } catch (e: Exception) {
            Log.e("FirestoreUserRepo", "Error fetching users by IDs: ${e.message}")
            emptyList()
        }
    }

    suspend fun refreshUserData(uid: String) {
        try {
            val snapshot = usersRef.document(uid).get().await()
            if (snapshot.exists()) {
                _currentUser.value = snapshot.toObject(User::class.java)?.copy(
                    uid = uid,
                    questsCompleted = snapshot.getLong("questsCompleted")?.toInt() ?: 0,
                    challengesCompleted = snapshot.getLong("challengesCompleted")?.toInt() ?: 0
                )
            }
        } catch (e: Exception) {
            Log.e("FirestoreUserRepo", "Error refreshing user data", e)
            throw e
        }
    }

    fun resetCurrentUser() {
        _currentUser.value = null
    }

    suspend fun addNotification(
        userId: String,
        title: String,
        message: String,
        type: String,
        relatedId: String
    ) {
        try {
            val notificationData = hashMapOf(
                "title" to title,
                "message" to message,
                "timestamp" to FieldValue.serverTimestamp(),
                "read" to false,
                "type" to type,
                "relatedId" to relatedId
            )

            usersRef
                .document(userId)
                .collection("notifications")
                .add(notificationData)
                .await()
        } catch (e: Exception) {
            Log.e("FirestoreUserRepo", "Error adding notification", e)
        }
    }

    fun getNotifications(userId: String): Flow<List<Notification>> = callbackFlow {
        val listener = usersRef
            .document(userId)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firestore", "Listen error", error)
                    close(error)
                    return@addSnapshotListener
                }

                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Notification(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            message = doc.getString("message") ?: "",
                            timestamp = doc.getDate("timestamp") ?: Date(),
                            read = doc.getBoolean("read") ?: false,
                            type = doc.getString("type") ?: "",
                            relatedId = doc.getString("relatedId") ?: ""
                        )
                    } catch (e: Exception) {
                        Log.e("Firestore", "Error parsing doc ${doc.id}", e)
                        null
                    }
                } ?: emptyList()

                trySend(notifications).isSuccess
            }

        awaitClose { listener.remove() }
    }.catch { e ->
        Log.e("Firestore", "Flow error", e)
        emit(emptyList())
    }

    suspend fun markNotificationAsRead(userId: String, notificationId: String) {
        try {
            usersRef
                .document(userId)
                .collection("notifications")
                .document(notificationId)
                .update("read", true)
                .await()
        } catch (e: Exception) {
            Log.e("FirestoreUserRepo", "Error marking notification as read", e)
        }
    }
}