package com.example.snapquest.models

data class User(
    var uid: String = "",
    var name: String = "",
    var email: String = "",
    var photoUrl: String = "",
    var isAdmin: Boolean = false,
    var questsCompleted: Int = 0,
    var challengesCompleted: Int = 0,
)