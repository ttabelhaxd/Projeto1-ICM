package com.example.snapquest.repositories

import com.example.snapquest.models.User
import com.example.snapquest.models.UserDao
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    companion object {
        var currentUser = User()
    }

    suspend fun registerUser(user: User) {
        if (user.uid.isBlank()) return

        val existingUser = userDao.getUserById(user.uid)
        if (existingUser != null) {
            currentUser = existingUser
            userDao.updateUser(user)
        } else {
            userDao.insertUser(user)
            currentUser = user
        }
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
}