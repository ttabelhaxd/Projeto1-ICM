package com.example.snapquest.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quests")
data class Quest(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var name: String = "",
    var description: String = "",
    var photoUrl: String = "",
    var startDate: Long = 0,
    var endDate: Long = 0,
    var isActive: Boolean = false,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var creatorId: Int = 0
)