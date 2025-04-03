package com.example.snapquest

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.snapquest.manages.LocationManager
import com.example.snapquest.repositories.FirestoreQuestRepository
import com.example.snapquest.repositories.FirestoreStorageRepository
import com.example.snapquest.repositories.FirestoreUserRepository
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings

class SnapQuestApp : Application() {
    val userRepository by lazy { FirestoreUserRepository() }
    val questRepository by lazy { FirestoreQuestRepository() }
    val storageRepository by lazy { FirestoreStorageRepository() }

    companion object {
        const val CHANNEL_ID = "snapquest_channel_id"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        LocationManager.initialize(this)

        val db = Firebase.firestore
        val settings = firestoreSettings {
            isPersistenceEnabled = true
        }
        db.firestoreSettings = settings

        val channel = NotificationChannel(
            CHANNEL_ID,
            "SnapQuest Notifications",
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}