package com.example.snapquest.models

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: Challenge)

    @Query("SELECT * FROM challenges WHERE id = :id")
    suspend fun getChallengeById(id: String): Challenge?

    @Update
    suspend fun updateChallenge(challenge: Challenge)

    @Delete
    suspend fun deleteChallenge(challenge: Challenge)

    @Query("SELECT * FROM challenges")
    fun getAllChallenges(): Flow<List<Challenge>>

    @Query("SELECT * FROM challenges WHERE questId = :questId ORDER BY orderInQuest ASC")
    suspend fun getChallengesForQuest(questId: Int): List<Challenge>

    @Query("SELECT * FROM challenges WHERE questId = :questId AND orderInQuest = :order")
    suspend fun getNextChallenge(questId: Int, order: Int): Challenge?
}