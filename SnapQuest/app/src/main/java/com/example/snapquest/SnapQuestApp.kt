package com.example.snapquest

import android.app.Application
import androidx.room.Room
import com.example.snapquest.manages.LocationManager
import com.example.snapquest.models.AppDatabase
import com.example.snapquest.repositories.FirestoreQuestRepository
import com.example.snapquest.repositories.UserRepository
import com.google.firebase.FirebaseApp

class SnapQuestApp : Application() {
    val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "snapquest-database"
        )
            .fallbackToDestructiveMigration() //only for development!!
            .build()
    }

    //val userRepository by lazy { UserRepository(database.userDao()) }
    val userRepository by lazy { UserRepository() }
    val questRepository by lazy { FirestoreQuestRepository() }

    companion object {
        @Volatile private var instance: SnapQuestApp? = null

        fun getInstance(): SnapQuestApp {
            return instance ?: throw IllegalStateException("Application not initialized")
        }
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        instance = this
        LocationManager.initialize(this)
    }
}