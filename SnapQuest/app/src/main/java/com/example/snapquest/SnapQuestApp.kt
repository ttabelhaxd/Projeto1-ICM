package com.example.snapquest

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.snapquest.models.AppDatabase
import com.example.snapquest.repositories.UserRepository

class SnapQuestApp : Application() {
    lateinit var database: AppDatabase
    lateinit var userRepository: UserRepository

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "snapquest-database"
        ).build()
        userRepository = UserRepository(database.userDao())
    }

    companion object {
        private var instance: SnapQuestApp? = null

        fun getContext(): Context {
            return instance!!.applicationContext
        }

        fun getUserRepository(): UserRepository {
            return instance!!.userRepository
        }
    }
}