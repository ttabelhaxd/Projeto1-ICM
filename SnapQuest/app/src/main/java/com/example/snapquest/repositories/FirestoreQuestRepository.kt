package com.example.snapquest.repositories

import com.example.snapquest.models.Challenge
import com.example.snapquest.models.Quest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirestoreQuestRepository {
    private val db = FirebaseFirestore.getInstance()
    private val questsRef = db.collection("quests")

    suspend fun createQuest(quest: Quest, challenges: List<Challenge>) {
        val questRef = questsRef.document()
        questRef.set(quest.copy(id = questRef.id.toInt())).await()

        challenges.forEach { challenge ->
            questRef.collection("challenges").document().set(challenge).await()
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

    fun getActiveQuests(): Flow<List<Quest>> = flow {
        val snapshot = questsRef.whereEqualTo("isActive", true).get().await()
        val quests = snapshot.documents.map { it.toObject<Quest>()!! }
        emit(quests)
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