package com.example.snapquest.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "challenges")
data class Challenge(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var name: String = "",
    var description: String = "",
    var hint: String = "",
    var hintPhotoUrl: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var isCompleted: Boolean = false
)