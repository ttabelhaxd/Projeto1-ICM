package com.example.snapquest.models

import java.util.Date

data class Notification(
    val id: String = "",
    val title: String,
    val message: String,
    val timestamp: Date,
    val read: Boolean,
    val type: String,
    val relatedId: String
)