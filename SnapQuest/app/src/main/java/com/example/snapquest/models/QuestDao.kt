package com.example.snapquest.models

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuest(quest: Quest)

    @Query("SELECT * FROM quests WHERE id = :id")
    suspend fun getQuestById(id: String): Quest?

    @Update
    suspend fun updateQuest(quest: Quest)

    @Delete
    suspend fun deleteQuest(quest: Quest)

    @Query("SELECT * FROM quests")
    fun getAllQuests(): Flow<List<Quest>>
}