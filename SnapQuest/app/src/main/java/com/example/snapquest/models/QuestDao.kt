package com.example.snapquest.models

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
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

    @Query("SELECT * FROM quests WHERE creatorId = :userId")
    suspend fun getQuestsCreatedByUser(userId: Int): List<Quest>

    @Transaction
    @Query("SELECT * FROM quests WHERE id IN (SELECT questId FROM userquestcrossref WHERE userId = :userId)")
    suspend fun getQuestsForUser(userId: Int): List<Quest>
}