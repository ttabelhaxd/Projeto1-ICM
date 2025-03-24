package com.example.snapquest.models

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        User::class,
        Quest::class,
        Challenge::class,
        UserQuestCrossRef::class,
        QuestChallengeCrossRef::class
    ],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun questDao(): QuestDao
    abstract fun challengeDao(): ChallengeDao
}