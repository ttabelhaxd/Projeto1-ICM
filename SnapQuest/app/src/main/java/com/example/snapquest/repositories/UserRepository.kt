package com.example.snapquest.repositories

import com.example.snapquest.models.User
import com.example.snapquest.models.UserDao
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    companion object {
        @Volatile
        private var instance: UserRepository? = null

        @Volatile
        var currentUser: User? = null
            private set

        fun getInstance(userDao: UserDao): UserRepository {
            return instance ?: synchronized(this) {
                instance ?: UserRepository(userDao).also { instance = it }
            }
        }
    }

    suspend fun registerUser(user: User) {
        if (user.uid.isBlank()) return

        val existingUser = userDao.getUserById(user.uid)
        if (existingUser != null) {
            currentUser = existingUser
            userDao.updateUser(user.apply {
                id = existingUser.id
            })
        } else {
            userDao.insertUser(user)
            currentUser = userDao.getUserById(user.uid)
        }
    }

    fun getCurrentUser(): User? {
        return currentUser
    }

    suspend fun getUser(uid: String): User? {
        return userDao.getUserById(uid)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user)
    }

    fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers()
    }

    fun resetCurrentUser() {
        currentUser = User()
    }

    suspend fun incrementChallengesCompleted(uid: String) {
        userDao.incrementChallengesCompleted(uid)
        currentUser?.challengesCompleted = (currentUser?.challengesCompleted ?: 0) + 1
    }

    suspend fun incrementQuestsCompleted(uid: String) {
        userDao.incrementQuestsCompleted(uid)
        currentUser?.questsCompleted = (currentUser?.questsCompleted ?: 0) + 1
    }

    suspend fun getCompletedQuestsCount(uid: String): Int {
        return userDao.getUserById(uid)?.questsCompleted ?: 0
    }

    suspend fun getCompletedChallengesCount(uid: String): Int {
        return userDao.getUserById(uid)?.challengesCompleted ?: 0
    }
}