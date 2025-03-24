package com.example.snapquest.models

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE uid = :uid")
    suspend fun getUserById(uid: String): User?

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Query("UPDATE users SET challengesCompleted = challengesCompleted + 1 WHERE uid = :uid")
    suspend fun incrementChallengesCompleted(uid: String)

    @Query("UPDATE users SET questsCompleted = questsCompleted + 1 WHERE id = :uid")
    suspend fun incrementQuestsCompleted(uid: String)
}