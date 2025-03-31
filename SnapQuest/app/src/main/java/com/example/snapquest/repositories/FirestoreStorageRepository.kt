package com.example.snapquest.repositories

import android.net.Uri
import com.example.snapquest.utils.ImageCompressor
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID

class FirestoreStorageRepository {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    suspend fun uploadQuestPhoto(localFilePath: String): String {
        val originalFile = File(localFilePath)
        val compressedFile = ImageCompressor.compressImage(originalFile, maxSizeKB = 200)
        val photoRef = storageRef.child("quests/${UUID.randomUUID()}.jpg")
        photoRef.putFile(Uri.fromFile(compressedFile)).await()
        return photoRef.downloadUrl.await().toString()
    }

    suspend fun uploadChallengePhoto(localFilePath: String): String {
        val originalFile = File(localFilePath)
        val compressedFile = ImageCompressor.compressImage(originalFile, maxSizeKB = 200)
        val photoRef = storageRef.child("challenges/${UUID.randomUUID()}.jpg")
        photoRef.putFile(Uri.fromFile(compressedFile)).await()
        return photoRef.downloadUrl.await().toString()
    }
}