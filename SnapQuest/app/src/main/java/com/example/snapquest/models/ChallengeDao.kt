package com.example.snapquest.models

import androidx.room.*
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
}