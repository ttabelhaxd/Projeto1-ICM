package com.example.snapquest.models

import java.util.Date

data class Quest(
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var photoUrl: String = "",
    var startDate: Date = Date(),
    var endDate: Date = Date(),
    var isActive: Boolean = false,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var creatorId: String = "",
    val participants: List<String> = emptyList(),
    val createdAt: Date = Date(),
    val totalChallenges: Int = 0
)