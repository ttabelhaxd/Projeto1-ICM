package com.example.snapquest.repositories

import com.example.snapquest.models.Challenge
import com.example.snapquest.models.Quest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirestoreQuestRepository {
    private val db = FirebaseFirestore.getInstance()
    private val questsRef = db.collection("quests")

    suspend fun createQuest(quest: Quest, challenges: List<Challenge>) {
        return try {
            val questRef = questsRef.document()
            val newQuest = quest.copy(
                id = questRef.id,
                name = quest.name,
                description = quest.description,
                photoUrl = quest.photoUrl,
                latitude = quest.latitude,
                startDate = quest.startDate,
                endDate = quest.endDate,
                isActive = quest.isActive,
                creatorId = quest.creatorId
            )
            questRef.set(newQuest).await()

            challenges.forEach { challenge ->
                questRef.collection("challenges")
                    .document()
                    .set(challenge.copy(questId = questRef.id))
                    .await()
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun joinQuest(userId: String, questId: String) {
        db.collection("userQuests").document("${userId}_$questId").set(
            mapOf(
                "userId" to userId,
                "questId" to questId,
                "completedChallenges" to emptyList<String>(),
                "isQuestCompleted" to false
            )
        ).await()
    }

    fun getActiveQuests(): Flow<Result<List<Quest>>> = flow {
        try {
            val snapshot = questsRef
                .whereEqualTo("isActive", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val quests = snapshot.documents.mapNotNull { it.toObject<Quest>() }
            emit(Result.success(quests))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun getQuestChallenges(questId: String): List<Challenge> {
        return questsRef.document(questId)
            .collection("challenges")
            .get()
            .await()
            .documents
            .map { it.toObject<Challenge>()!! }
    }
}