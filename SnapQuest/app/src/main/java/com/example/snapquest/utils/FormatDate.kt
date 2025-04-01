package com.example.snapquest.utils

import android.icu.text.SimpleDateFormat
import java.util.*

fun formatDate(date: Date): String {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return dateFormat.format(date)
}