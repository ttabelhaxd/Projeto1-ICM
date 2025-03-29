package com.example.snapquest.repositories

import com.example.snapquest.models.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersRef = db.collection("users")
    private var _currentUser: User? = null

    val currentUser: User?
        get() = _currentUser

    suspend fun fetchCurrentUser(uid: String) {
        _currentUser = usersRef.document(uid).get().await().toObject(User::class.java)
    }

    fun resetCurrentUser() {
        _currentUser = null
    }

    suspend fun registerUser(user: User) {
        if (user.uid.isBlank()) return
        usersRef.document(user.uid).set(user).await()
    }

    suspend fun getUser(uid: String): User? {
        return usersRef.document(uid).get().await().toObject(User::class.java)
    }

    suspend fun updateUser(user: User) {
        usersRef.document(user.uid).set(user).await()
    }

    suspend fun deleteUser(uid: String) {
        usersRef.document(uid).delete().await()
    }

    fun getAllUsers(): Flow<List<User>> = flow {
        val snapshot = usersRef.get().await()
        emit(snapshot.toObjects(User::class.java))
    }

    suspend fun incrementChallengesCompleted(uid: String) {
        usersRef.document(uid).update("challengesCompleted", FieldValue.increment(1)).await()
    }

    suspend fun incrementQuestsCompleted(uid: String) {
        usersRef.document(uid).update("questsCompleted", FieldValue.increment(1)).await()
    }
}