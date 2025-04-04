package com.example.snapquest.repositories

import android.util.Log
import com.example.snapquest.models.Challenge
import com.example.snapquest.models.Quest
import com.example.snapquest.models.UserQuest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
                    .set(challenge.copy(questId = questRef.id, id = questRef.id))
                    .await()
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun deleteQuest(questId: String, storageRepository: FirestoreStorageRepository) {
        val batch = db.batch()

        try {
            val challenges = getQuestChallenges(questId)
            val userQuests = getUserQuestsForQuest(questId)
            val quest = questsRef.document(questId).get().await().toObject<Quest>()

            challenges.forEach { challenge ->
                val challengeRef = questsRef.document(questId)
                    .collection("challenges")
                    .document(challenge.id)
                batch.delete(challengeRef)
            }

            userQuests.forEach { userQuest ->
                val userQuestRef = db.collection("userQuests").document("${userQuest.userId}_$questId")
                batch.delete(userQuestRef)
            }

            batch.delete(questsRef.document(questId))
            batch.commit().await()

            val photoUrls = mutableListOf<String>()

            quest?.photoUrl?.let { photoUrls.add(it) }
            challenges.map { it.hintPhotoUrl }.let { photoUrls.addAll(it) }

            val userPhotoUrls = userQuests
                .flatMap { it.completedPhotos.values ?: emptyList() }
                .filter { it.isNotBlank() }

            photoUrls.addAll(userPhotoUrls)

            if (photoUrls.isNotEmpty()) {
                storageRepository.deletePhotosByUrls(photoUrls)
            }

        } catch (e: Exception) {
            Log.e("FirestoreQuestRepo", "Error deleting quest", e)
            throw e
        }
    }

    private suspend fun getUserQuestsForQuest(questId: String): List<UserQuest> {
        return try {
            val snapshot = db.collection("userQuests")
                .whereEqualTo("questId", questId)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject<UserQuest>() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun joinQuest(userId: String, questId: String): Result<Unit> {
        return try {
            val questRef = questsRef.document(questId)
            val quest = questRef.get().await()

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

            val updateData = hashMapOf<String, Any>(
                "participants" to FieldValue.arrayUnion(userId)
            )

            questRef.set(updateData, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreQuestRepo", "Error joining quest", e)
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
            val participants = snapshot.get("participants") as? List<String> ?: emptyList()
            Log.d("FirestoreQuestRepo", "Found ${participants.size} participants for quest $questId")

            emit(Result.success(snapshot.toObject<Quest>()?.copy(
                id = snapshot.id,
                participants = participants
            )))
        } catch (e: Exception) {
            Log.e("FirestoreQuestRepo", "Error getting quest", e)
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

    fun completeChallengeWithPhoto(
        userId: String,
        questId: String,
        challengeId: String,
        photoUrl: String
    ): Result<Unit> {
        return try {
            val userQuestRef = db.collection("userQuests").document("${userId}_$questId")

            db.runTransaction { transaction ->
                val snapshot = transaction.get(userQuestRef)
                val currentCompleted = snapshot.get("completedChallenges") as? List<String> ?: emptyList()
                val currentPhotos = snapshot.get("completedPhotos") as? Map<String, String> ?: emptyMap()
                val updatedCompleted = currentCompleted + challengeId
                val updatedPhotos = currentPhotos + (challengeId to photoUrl)
                val quest = transaction.get(questsRef.document(questId)).toObject(Quest::class.java)
                    ?: throw IllegalStateException("Quest not found")
                val isQuestCompleted = updatedCompleted.size >= quest.totalChallenges
                val updates = mutableMapOf<String, Any>(
                    "completedChallenges" to updatedCompleted,
                    "completedPhotos" to updatedPhotos,
                    "isQuestCompleted" to isQuestCompleted,
                    "completedAt" to FieldValue.serverTimestamp()
                )
                transaction.update(userQuestRef, updates)

                if (isQuestCompleted) {
                    val userRef = db.collection("users").document(userId)
                    transaction.update(userRef,
                        "questsCompleted", FieldValue.increment(1),
                        "challengesCompleted", FieldValue.increment(quest.totalChallenges.toLong())
                    )
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreQuestRepo", "Error completing challenge with photo", e)
            Result.failure(e)
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