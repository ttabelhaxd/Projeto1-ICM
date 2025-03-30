package com.example.snapquest.repositories

import android.util.Log
import com.example.snapquest.models.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
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

    suspend fun refreshUserData(uid: String) {
        try {
            _currentUser.value = fetchCurrentUser(uid)
        } catch (e: Exception) {
            Log.e("FirestoreUserRepo", "Error refreshing user data", e)
        }
    }

    fun resetCurrentUser() {
        _currentUser.value = null
    }
}