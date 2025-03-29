package com.example.snapquest

import android.app.Application
import com.example.snapquest.manages.LocationManager
import com.example.snapquest.repositories.FirestoreQuestRepository
import com.example.snapquest.repositories.FirestoreUserRepository
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings

class SnapQuestApp : Application() {
    val userRepository by lazy { FirestoreUserRepository() }
    val questRepository by lazy { FirestoreQuestRepository() }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        LocationManager.initialize(this)

        val db = Firebase.firestore
        val settings = firestoreSettings {
            isPersistenceEnabled = true
        }
        db.firestoreSettings = settings
    }
}