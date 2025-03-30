package com.example.snapquest.repositories

import android.util.Log
import com.example.snapquest.models.Challenge
import com.example.snapquest.models.Quest
import com.example.snapquest.models.UserQuest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
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
                creatorId = quest.creatorId,
                totalChallenges = challenges.size,
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

    suspend fun deleteQuest(questId: String) {
        val challenges = getQuestChallenges(questId)
        val batch = db.batch()

        challenges.forEach { challenge ->
            val ref = questsRef.document(questId)
                .collection("challenges")
                .document(challenge.id)
            batch.delete(ref)
        }

        batch.delete(questsRef.document(questId))
        batch.commit().await()
    }

    suspend fun joinQuest(userId: String, questId: String): Result<Unit> {
        return try {
            val quest = questsRef.document(questId).get().await()
            if (!quest.exists()) {
                return Result.failure(IllegalStateException("Quest not found"))
            }

            val userQuestRef = db.collection("userQuests").document("${userId}_$questId")
            userQuestRef.set(
                mapOf(
                    "userId" to userId,
                    "questId" to questId,
                    "completedChallenges" to emptyList<String>(),
                    "isQuestCompleted" to false,
                    "joinedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            try {
                questsRef.document(questId).update("participants", FieldValue.arrayUnion(userId)).await()
            } catch (e: Exception) {
                Log.w("JoinQuest", "Couldn't update participants list", e)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserQuests(userId: String): Flow<Result<List<UserQuest>>> = flow {
        try {
            val snapshot = db.collection("userQuests")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val userQuests = snapshot.documents.mapNotNull {
                it.toObject<UserQuest>()
            }
            emit(Result.success(userQuests))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getActiveQuests(): Flow<Result<List<Quest>>> = flow {
        try {
            val snapshot = questsRef
                .whereEqualTo("active", true)
                .get()
                .await()

            val quests = snapshot.documents.mapNotNull { doc ->
                doc.toObject<Quest>()?.copy(id = doc.id)
            }
            emit(Result.success(quests))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun getQuestChallenges(questId: String): List<Challenge> {
        return try {
            val snapshot = questsRef.document(questId)
                .collection("challenges")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject<Challenge>()?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getQuestById(questId: String): Flow<Result<Quest?>> = flow {
        try {
            val snapshot = questsRef.document(questId).get().await()
            emit(Result.success(snapshot.toObject<Quest>()?.copy(id = snapshot.id)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getUserQuest(userId: String, questId: String): Flow<Result<UserQuest?>> = flow {
        try {
            val snapshot = db.collection("userQuests")
                .document("${userId}_$questId")
                .get()
                .await()
            emit(Result.success(snapshot.toObject<UserQuest>()))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun completeChallenge(userId: String, questId: String, challengeId: String): Result<Unit> {
        return try {
            val userQuestRef = db.collection("userQuests").document("${userId}_$questId")
            val userRef = db.collection("users").document(userId)

            db.runTransaction { transaction ->
                val userQuestSnapshot = transaction.get(userQuestRef)
                if (!userQuestSnapshot.exists()) {
                    throw IllegalStateException("User hasn't joined this quest")
                }
                val userQuest = userQuestSnapshot.toObject(UserQuest::class.java)!!
                if (userQuest.completedChallenges.contains(challengeId)) {
                    return@runTransaction
                }

                val updatedCompleted = userQuest.completedChallenges + challengeId

                val totalChallenges = transaction.get(questsRef.document(questId)).getLong("totalChallenges")?.toInt() ?: 0
                val isQuestCompleted = updatedCompleted.size == totalChallenges

                transaction.update(userQuestRef,
                    "completedChallenges", updatedCompleted,
                    "isQuestCompleted", isQuestCompleted
                )
                if (isQuestCompleted) {
                    transaction.update(userRef,
                        "questsCompleted", FieldValue.increment(1),
                        "challengesCompleted", FieldValue.increment(totalChallenges.toLong())
                    )
                } else {
                    transaction.update(userRef,
                        "challengesCompleted", FieldValue.increment(1)
                    )
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun canAccessChallenge(userId: String, questId: String, challengeId: String): Boolean {
        return try {
            val userQuestRef = db.collection("userQuests").document("${userId}_$questId")
            val userQuest = userQuestRef.get().await().toObject<UserQuest>()

            val challenges = getQuestChallenges(questId).sortedBy { it.orderInQuest }
            val currentChallengeIndex = challenges.indexOfFirst { it.id == challengeId }

            currentChallengeIndex == 0 ||
                    userQuest?.completedChallenges?.contains(challenges[currentChallengeIndex - 1].id) == true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getChallengeById(questId: String, challengeId: String): Challenge? {
        return try {
            val snapshot = questsRef.document(questId)
                .collection("challenges")
                .document(challengeId)
                .get()
                .await()

            snapshot.toObject<Challenge>()?.copy(id = snapshot.id)
        } catch (e: Exception) {
            null
        }
    }
}