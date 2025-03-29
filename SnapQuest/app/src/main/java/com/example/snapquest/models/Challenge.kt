package com.example.snapquest.models

data class Challenge(
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var hint: String = "",
    var hintPhotoUrl: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var isCompleted: Boolean = false,
    var orderInQuest: Int = 0,
    var questId: String = ""
)